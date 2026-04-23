package com.dawn.http.retrofit;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

/**
 * OkHttp HTTP 日志拦截器
 * <p>
 * 支持 NONE / BASIC / HEADERS / BODY 四个日志级别。
 * 对某个请求跳过日志记录：在 Retrofit 接口上添加 {@code @Headers("No-Logging:true")}。
 */
public final class HttpLoggingInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public enum Level {
        /** 不记录日志 */
        NONE,
        /** 仅记录请求/响应行 */
        BASIC,
        /** 记录请求/响应行 + 请求头 */
        HEADERS,
        /** 记录请求/响应行 + 请求头 + 响应体 */
        BODY
    }

    public interface Logger {
        void log(String message);
    }

    private final Logger logger;
    private volatile Set<String> headersToRedact = Collections.emptySet();
    private volatile Level level = Level.NONE;

    public HttpLoggingInterceptor(Logger logger) {
        this.logger = logger;
    }

    public void redactHeader(String name) {
        Set<String> newSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        newSet.addAll(headersToRedact);
        newSet.add(name);
        headersToRedact = newSet;
    }

    public HttpLoggingInterceptor setLevel(Level level) {
        if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.level = level;
        return this;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Level level = this.level;
        Request request = chain.request();

        // 支持通过请求头跳过日志（@Headers("No-Logging:true")）
        if ("true".equalsIgnoreCase(request.header("No-Logging"))) {
            return chain.proceed(request);
        }

        if (level == Level.NONE) {
            return chain.proceed(request);
        }

        boolean logBody = level == Level.BODY;
        boolean logHeaders = logBody || level == Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        Connection connection = chain.connection();

        String requestStartMessage = "--> "
                + request.method()
                + ' ' + request.url()
                + (connection != null ? " " + connection.protocol() : "");
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
        }
        logger.log(requestStartMessage);

        if (logHeaders) {
            if (hasRequestBody) {
                if (requestBody.contentType() != null) {
                    logger.log("Content-Type: " + requestBody.contentType());
                }
                if (requestBody.contentLength() != -1) {
                    logger.log("Content-Length: " + requestBody.contentLength());
                }
            }

            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    logHeader(headers, i);
                }
            }

            if (!logBody || !hasRequestBody) {
                logger.log("--> END " + request.method());
            } else if (bodyHasUnknownEncoding(request.headers())) {
                logger.log("--> END " + request.method() + " (encoded body omitted)");
            } else if (requestBody.isDuplex()) {
                logger.log("--> END " + request.method() + " (duplex request body omitted)");
            } else {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                if (isPlaintext(buffer)) {
                    logger.log(buffer.readString(charset));
                    logger.log("--> END " + request.method() + " (" + requestBody.contentLength() + "-byte body)");
                } else {
                    logger.log("--> END " + request.method() + " (binary " + requestBody.contentLength() + "-byte body omitted)");
                }
            }
        }

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            logger.log("<-- HTTP FAILED: " + e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody != null ? responseBody.contentLength() : -1;
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        logger.log("<-- "
                + response.code()
                + (response.message().isEmpty() ? "" : ' ' + response.message())
                + ' ' + response.request().url()
                + " (" + tookMs + "ms" + (!logHeaders ? ", " + bodySize + " body" : "") + ')');

        if (logHeaders) {
            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                logHeader(headers, i);
            }

            if (!logBody || responseBody == null || !hasBody(response)) {
                logger.log("<-- END HTTP");
            } else if (bodyHasUnknownEncoding(response.headers())) {
                logger.log("<-- END HTTP (encoded body omitted)");
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE);
                Buffer buffer = source.getBuffer();

                Long gzippedLength = null;
                if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
                    gzippedLength = buffer.size();
                    try (GzipSource gzippedResponseBody = new GzipSource(buffer.clone())) {
                        buffer = new Buffer();
                        buffer.writeAll(gzippedResponseBody);
                    }
                }

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                if (!isPlaintext(buffer)) {
                    logger.log("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
                    return response;
                }

                if (contentLength != 0) {
                    logger.log(buffer.clone().readString(charset));
                }

                if (gzippedLength != null) {
                    logger.log("<-- END HTTP (" + buffer.size() + "-byte, " + gzippedLength + "-gzipped-byte body)");
                } else {
                    logger.log("<-- END HTTP (" + buffer.size() + "-byte body)");
                }
            }
        }

        return response;
    }

    private void logHeader(Headers headers, int i) {
        String value = headersToRedact.contains(headers.name(i)) ? "██" : headers.value(i);
        logger.log(headers.name(i) + ": " + value);
    }

    private static boolean hasBody(Response response) {
        // HEAD 请求和 1xx/204/304 响应没有 body
        if (response.request().method().equals("HEAD")) return false;
        int code = response.code();
        if ((code < 200 || code == 204 || code == 304)) return false;
        if (response.body() == null) return false;
        ResponseBody body = response.body();
        return body.contentLength() != 0;
    }

    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) break;
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false;
        }
    }

    private static boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
                && !contentEncoding.equalsIgnoreCase("identity")
                && !contentEncoding.equalsIgnoreCase("gzip");
    }
}
