package com.dawn.http.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 通用 Retrofit 工厂类
 * <p>
 * 通过 Builder 模式配置拦截器、超时等参数，创建 Retrofit Service 实例。
 * 默认开启 SSL 全信任（适用于内网/测试环境）。
 *
 * <pre>
 * ApiService service = new RetrofitClient.Builder()
 *         .addInterceptor(new HttpLoggingInterceptor(msg -> Log.d("HTTP", msg))
 *                 .setLevel(HttpLoggingInterceptor.Level.BODY))
 *         .addInterceptor(myHeaderInterceptor)
 *         .build()
 *         .createService(ApiService.class, "https://example.com");
 * </pre>
 */
public class RetrofitClient {

    private static final String SSL_PROTOCOL = "TLS";

    private final OkHttpClient okHttpClient;

    private RetrofitClient(Builder builder) {
        TrustManager[] trustAllCerts = createTrustAllCerts();
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(builder.connectTimeout, TimeUnit.SECONDS)
                .readTimeout(builder.readTimeout, TimeUnit.SECONDS)
                .writeTimeout(builder.writeTimeout, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> true);
        configureSsl(clientBuilder, trustAllCerts);
        for (Interceptor interceptor : builder.interceptors) {
            clientBuilder.addInterceptor(interceptor);
        }
        this.okHttpClient = clientBuilder.build();
    }

    /**
     * 创建指定接口类的 Retrofit Service 实例
     *
     * @param serviceClass Retrofit 接口 class
     * @param baseUrl      请求根地址（必须以 / 结尾或不含路径）
     */
    public <T> T createService(Class<T> serviceClass, String baseUrl) {
        if (serviceClass == null || baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("serviceClass and baseUrl cannot be null or empty");
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(serviceClass);
    }

    // ====================== SSL ======================

    private TrustManager[] createTrustAllCerts() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
    }

    private void configureSsl(OkHttpClient.Builder builder, TrustManager[] trustAllCerts) {
        try {
            SSLContext sslContext = SSLContext.getInstance(SSL_PROTOCOL);
            sslContext.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(socketFactory, (X509TrustManager) trustAllCerts[0]);
        } catch (Exception ignored) {
        }
    }

    // ====================== Builder ======================

    public static class Builder {

        private int connectTimeout = 30;
        private int readTimeout = 30;
        private int writeTimeout = 30;
        private final List<Interceptor> interceptors = new ArrayList<>();

        public Builder connectTimeout(int seconds) {
            this.connectTimeout = seconds;
            return this;
        }

        public Builder readTimeout(int seconds) {
            this.readTimeout = seconds;
            return this;
        }

        public Builder writeTimeout(int seconds) {
            this.writeTimeout = seconds;
            return this;
        }

        /**
         * 添加 OkHttp 拦截器（顺序即生效顺序）
         */
        public Builder addInterceptor(Interceptor interceptor) {
            if (interceptor != null) {
                interceptors.add(interceptor);
            }
            return this;
        }

        public RetrofitClient build() {
            return new RetrofitClient(this);
        }
    }
}
