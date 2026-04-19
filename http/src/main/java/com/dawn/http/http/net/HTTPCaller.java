package com.dawn.http.http.net;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.dawn.http.http.entity.HttpConfig;
import com.dawn.http.http.ui.ProgressHelper;
import com.dawn.http.http.ui.ProgressUIListener;
import com.dawn.http.http.util.Util;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * HTTP请求发起和数据解析转换
 */
public class HTTPCaller {
    private static volatile HTTPCaller _instance = null;
    private OkHttpClient client;
    private Map<String, Call> requestHandleMap = null;
    private CacheControl cacheControl = null;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String CHARSET_UTF8 = "utf-8";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private Gson gson = null;

    private HttpConfig httpConfig = new HttpConfig();
    private volatile boolean initialized = false;

    private HTTPCaller() {
    }

    public static HTTPCaller getInstance() {
        if (_instance == null) {
            synchronized (HTTPCaller.class) {
                if (_instance == null) {
                    _instance = new HTTPCaller();
                }
            }
        }
        return _instance;
    }

    /**
     * 设置配置信息 这个方法必需要调用一次
     *
     * @param httpConfig
     */
    public synchronized void setHttpConfig(HttpConfig httpConfig) {
        if (httpConfig == null) {
            throw new IllegalArgumentException("httpConfig must not be null");
        }
        // 取消并清理之前的所有请求
        if (requestHandleMap != null && !requestHandleMap.isEmpty()) {
            for (Map.Entry<String, Call> entry : requestHandleMap.entrySet()) {
                Call call = entry.getValue();
                if (call != null && !call.isCanceled()) {
                    call.cancel();
                }
            }
            requestHandleMap.clear();
        }
        this.httpConfig = httpConfig;

        client = new OkHttpClient.Builder()
                .connectTimeout(httpConfig.getConnectTimeout(), TimeUnit.SECONDS)
                .writeTimeout(httpConfig.getWriteTimeout(), TimeUnit.SECONDS)
                .readTimeout(httpConfig.getReadTimeout(), TimeUnit.SECONDS)
                .build();
        gson = new Gson();
        if (requestHandleMap == null) {
            requestHandleMap = new ConcurrentHashMap<>();
        }
        cacheControl = new CacheControl.Builder().noStore().noCache().build();
        initialized = true;
    }

    private void ensureInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    setHttpConfig(httpConfig);
                }
            }
        }
    }

    public <T> void get(Class<T> clazz, final String url, final RequestDataCallback<T> callback) {
        this.get(clazz, url, null, callback, true);
    }

    public <T> void get(Class<T> clazz, final String url, Header[] header, final RequestDataCallback<T> callback) {
        this.get(clazz, url, header, callback, true);
    }

    /**
     * get请求
     *
     * @param clazz      json对应类的类型
     * @param url        请求url
     * @param header     请求头
     * @param callback   回调接口
     * @param autoCancel 是否自动取消 true:同一时间请求一个接口多次  只保留最后一个
     * @param <T>
     */
    public <T> void get(final Class<T> clazz, final String url, Header[] header, final RequestDataCallback<T> callback, boolean autoCancel) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        add(url, getBuilder(url, header, new MyHttpResponseHandler<>(clazz, url, callback)), autoCancel);
    }

    private Call getBuilder(String url, Header[] header, HttpResponseHandler responseCallback) {
        url = Util.getMosaicParameter(url, httpConfig.getCommonField());
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.get();
        return execute(builder, getCommonHeader(header), responseCallback);
    }

    private Header[] getCommonHeader(Header[] headerOld){
        List<NameValuePair> headerFields = httpConfig.getHeaderField();
        if (headerFields == null || headerFields.isEmpty()) {
            return headerOld;
        }
        // 过滤掉值为null的header字段
        List<Header> validHeaders = new ArrayList<>();
        for (NameValuePair field : headerFields) {
            if (field.getValue() != null) {
                validHeaders.add(new Header(field.getName(), field.getValue()));
            }
        }
        if (validHeaders.isEmpty()) {
            return headerOld;
        }
        if (headerOld == null) {
            return validHeaders.toArray(new Header[0]);
        }
        Header[] headerNew = new Header[headerOld.length + validHeaders.size()];
        System.arraycopy(headerOld, 0, headerNew, 0, headerOld.length);
        for (int i = 0; i < validHeaders.size(); i++) {
            headerNew[headerOld.length + i] = validHeaders.get(i);
        }
        return headerNew;
    }

    public <T> T getSync(Class<T> clazz, String url) {
        return getSync(clazz, url, null);
    }

    public <T> T getSync(Class<T> clazz, String url, Header[] header) {
        if (checkAgent()) {
            return null;
        }
        ensureInitialized();
        url = Util.getMosaicParameter(url, httpConfig.getCommonField());
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.get();
        byte[] bytes = execute(builder, getCommonHeader(header));
        try {
            String str = new String(bytes, CHARSET_UTF8);
            if (clazz != null && !TextUtils.isEmpty(str)) {
                T t = gson.fromJson(str, clazz);
                return t;
            }
        } catch (Exception e) {
            printLog("getSync HTTPCaller:" + e.toString());
        }
        return null;
    }

    public <T> void post(final Class<T> clazz, final String url, List<NameValuePair> params, RequestDataCallback<T> callback) {
        this.post(clazz, url, null, params, callback, true);
    }

    public <T> void post(final Class<T> clazz, final String url, Header[] header, List<NameValuePair> params, RequestDataCallback<T> callback) {
        this.post(clazz, url, header, params, callback, true);
    }

    public <T> void post(final Class<T> clazz, final String url, Header[] header, Map<String, Object> params, RequestDataCallback<T> callback) {
        this.post(clazz, url, header, params, callback, true);
    }

    /**
     * @param clazz      json对应类的类型
     * @param url        请求url
     * @param header     请求头
     * @param params     参数
     * @param callback   回调
     * @param autoCancel 是否自动取消 true:同一时间请求一个接口多次  只保留最后一个
     * @param <T>
     */
    public <T> void post(final Class<T> clazz, final String url, Header[] header, final List<NameValuePair> params, final RequestDataCallback<T> callback, boolean autoCancel) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        add(url, postBuilder(url, header, params, new MyHttpResponseHandler<>(clazz, url, callback)), autoCancel);
    }

    public <T> void post(final Class<T> clazz, final String url, Header[] header, final Map<String, Object> params, final RequestDataCallback<T> callback, boolean autoCancel) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        add(url, postBuilder(url, header, params, new MyHttpResponseHandler<>(clazz, url, callback)), autoCancel);
    }

    public <T> void post(final Class<T> clazz, final String url, Header[] header, final String params, final RequestDataCallback<T> callback) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        add(url, postBuilder(url, header, params, new MyHttpResponseHandler<>(clazz, url, callback)), true);
    }

    public <T> T postSync(Class<T> clazz, String url, List<NameValuePair> form) {
        return postSync(clazz, url, form, null);
    }

    public <T> T postSync(Class<T> clazz, String url, List<NameValuePair> form, Header[] header) {
        if (checkAgent()) {
            return null;
        }
        ensureInitialized();
        Request.Builder builder = getRequestBuild(url, form);
        byte[] bytes = execute(builder, getCommonHeader(header));
        try {
            String result = new String(bytes, CHARSET_UTF8);
            if (clazz != null && !TextUtils.isEmpty(result)) {
                T t = gson.fromJson(result, clazz);
                return t;
            }
        } catch (Exception e) {
            printLog("postSync error:" + e.toString());
        }
        return null;
    }

    public byte[] postSyncRaw(String url, List<NameValuePair> form) {
        return postSyncRaw(url, form, null);
    }

    public byte[] postSyncRaw(String url, List<NameValuePair> form, Header[] header) {
        if (checkAgent()) {
            return null;
        }
        ensureInitialized();
        Request.Builder builder = getRequestBuild(url, form);
        return execute(builder, getCommonHeader(header));
    }

    /**
     * 同步POST JSON请求
     */
    public <T> T postSyncJson(Class<T> clazz, String url, String json, Header[] header) {
        if (checkAgent()) {
            return null;
        }
        ensureInitialized();
        Request.Builder builder = getRequestBuild(url, json);
        byte[] bytes = execute(builder, getCommonHeader(header));
        try {
            String result = new String(bytes, CHARSET_UTF8);
            if (clazz != null && !TextUtils.isEmpty(result)) {
                return gson.fromJson(result, clazz);
            }
        } catch (Exception e) {
            printLog("postSyncJson error:" + e.toString());
        }
        return null;
    }

    /**
     * 同步POST JSON请求，返回原始字节
     */
    public byte[] postSyncJsonRaw(String url, String json, Header[] header) {
        if (checkAgent()) {
            return null;
        }
        ensureInitialized();
        Request.Builder builder = getRequestBuild(url, json);
        return execute(builder, getCommonHeader(header));
    }

    /**
     * 自定义Content-Type的POST请求（异步）
     *
     * @param clazz       响应类型
     * @param url         请求URL
     * @param header      请求头
     * @param body        请求体内容
     * @param contentType 内容类型，如 "application/xml; charset=utf-8"、"text/plain" 等
     * @param callback    回调
     */
    public <T> void postBody(final Class<T> clazz, final String url, Header[] header, final String body, final String contentType, final RequestDataCallback<T> callback) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        try {
            MediaType mediaType = MediaType.parse(contentType);
            String safeBody = body != null ? body : "";
            printLog("requestBody (" + contentType + "): " + safeBody);
            RequestBody requestBody = RequestBody.create(mediaType, safeBody);
            Request.Builder builder = new Request.Builder();
            builder.url(url);
            builder.post(requestBody);
            add(url, execute(builder, getCommonHeader(header), new MyHttpResponseHandler<>(clazz, url, callback)), true);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "unknown error";
            sendErrorCallback(-1, msg, callback);
        }
    }

    /**
     * 自定义Content-Type的POST请求（异步，字节数组）
     *
     * @param clazz       响应类型
     * @param url         请求URL
     * @param header      请求头
     * @param body        请求体字节数组
     * @param contentType 内容类型
     * @param callback    回调
     */
    public <T> void postBody(final Class<T> clazz, final String url, Header[] header, final byte[] body, final String contentType, final RequestDataCallback<T> callback) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        try {
            MediaType mediaType = MediaType.parse(contentType);
            byte[] safeBody = body != null ? body : new byte[0];
            printLog("requestBody bytes (" + contentType + "), length: " + safeBody.length);
            RequestBody requestBody = RequestBody.create(mediaType, safeBody);
            Request.Builder builder = new Request.Builder();
            builder.url(url);
            builder.post(requestBody);
            add(url, execute(builder, getCommonHeader(header), new MyHttpResponseHandler<>(clazz, url, callback)), true);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "unknown error";
            sendErrorCallback(-1, msg, callback);
        }
    }

    /**
     * 自定义Content-Type的POST请求（同步）
     */
    public <T> T postBodySync(Class<T> clazz, String url, Header[] header, String body, String contentType) {
        if (checkAgent()) {
            return null;
        }
        ensureInitialized();
        MediaType mediaType = MediaType.parse(contentType);
        String safeBody = body != null ? body : "";
        RequestBody requestBody = RequestBody.create(mediaType, safeBody);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(requestBody);
        byte[] bytes = execute(builder, getCommonHeader(header));
        try {
            String result = new String(bytes, CHARSET_UTF8);
            if (clazz != null && !TextUtils.isEmpty(result)) {
                return gson.fromJson(result, clazz);
            }
        } catch (Exception e) {
            printLog("postBodySync error:" + e.toString());
        }
        return null;
    }

    /**
     * 自定义Content-Type的POST请求（同步，返回原始字节）
     */
    public byte[] postBodySyncRaw(String url, Header[] header, String body, String contentType) {
        if (checkAgent()) {
            return null;
        }
        ensureInitialized();
        MediaType mediaType = MediaType.parse(contentType);
        String safeBody = body != null ? body : "";
        RequestBody requestBody = RequestBody.create(mediaType, safeBody);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(requestBody);
        return execute(builder, getCommonHeader(header));
    }

    private Call postBuilder(String url, Header[] header, List<NameValuePair> form, HttpResponseHandler responseCallback) {
        try {
            Request.Builder builder = getRequestBuild(url, form);
            return execute(builder, getCommonHeader(header), responseCallback);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "unknown error";
            if (responseCallback != null)
                responseCallback.onFailure(-1, msg.getBytes(UTF_8));
        }
        return null;
    }

    private Call postBuilder(String url, Header[] header, Map<String, Object> form, HttpResponseHandler responseCallback) {
        try {
            Request.Builder builder = getRequestBuild(url, form);
            return execute(builder, getCommonHeader(header), responseCallback);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "unknown error";
            if (responseCallback != null)
                responseCallback.onFailure(-1, msg.getBytes(UTF_8));
        }
        return null;
    }

    private Call postBuilder(String url, Header[] header, String json, HttpResponseHandler responseCallback) {
        try {
            Request.Builder builder = getRequestBuild(url, json);
            return execute(builder, getCommonHeader(header), responseCallback);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "unknown error";
            if (responseCallback != null)
                responseCallback.onFailure(-1, msg.getBytes(UTF_8));
        }
        return null;
    }

    private Request.Builder getRequestBuild(String url, List<NameValuePair> form) {
        if (form == null) {
            form = new ArrayList<>();
        } else {
            form = new ArrayList<>(form);
        }
        form.addAll(httpConfig.getCommonField());
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (NameValuePair item : form) {
            if (item.getValue() == null) {
                printLog("字段:" + item.getName() + "的值为null，已跳过");
                continue;
            }
            printLog("key:" + item.getName() + "  value:" + item.getValue());
            formBuilder.add(item.getName(), item.getValue());
        }
        RequestBody requestBody = formBuilder.build();
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(requestBody);
        return builder;
    }

    private Request.Builder getRequestBuild(String url, Map<String, Object> form) {
        if (form == null) {
            form = new HashMap<>();
        }
        String jsonStr = gson.toJson(form);
        printLog("requestBody json: " + jsonStr);
        RequestBody requestBody = RequestBody.create(JSON, jsonStr);

        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(requestBody);
        return builder;
    }

    private Request.Builder getRequestBuild(String url, String json){
        if(TextUtils.isEmpty(json)){
            json = "{}";
        }
        printLog("requestBody json: " + json);
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(requestBody);
        return builder;
    }

    /**
     * 上传文件
     *
     * @param clazz    json对应类的类型
     * @param url      请求url
     * @param header   请求头
     * @param form     请求参数
     * @param callback 回调
     * @param <T>
     */
    public <T> void postFile(final Class<T> clazz, final String url, Header[] header, List<NameValuePair> form, final RequestDataCallback<T> callback) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        add(url, postFile(url, header, form, new MyHttpResponseHandler<>(clazz, url, callback), null));
    }

    /**
     * 上传文件
     *
     * @param clazz              json对应类的类型
     * @param url                请求url
     * @param header             请求头
     * @param form               请求参数
     * @param callback           回调
     * @param progressUIListener 上传文件进度
     * @param <T>
     */
    public <T> void postFile(final Class<T> clazz, final String url, Header[] header, List<NameValuePair> form, final RequestDataCallback<T> callback, ProgressUIListener progressUIListener) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        add(url, postFile(url, header, form, new MyHttpResponseHandler<>(clazz, url, callback), progressUIListener));
    }

    /**
     * 上传文件
     *
     * @param clazz       json对应类的类型
     * @param url         请求url
     * @param header      请求头
     * @param name        名字
     * @param fileName    文件名
     * @param fileContent 文件内容
     * @param callback    回调
     * @param <T>
     */
    public <T> void postFile(final Class<T> clazz, final String url, Header[] header, String name, String fileName, byte[] fileContent, final RequestDataCallback<T> callback) {
        postFile(clazz, url, header, name, fileName, fileContent, callback, null);
    }

    /**
     * 上传文件
     *
     * @param clazz              json对应类的类型
     * @param url                请求url
     * @param header             请求头
     * @param name               名字
     * @param fileName           文件名
     * @param fileContent        文件内容
     * @param callback           回调
     * @param progressUIListener 回调上传进度
     * @param <T>
     */
    public <T> void postFile(Class<T> clazz, final String url, Header[] header, String name, String fileName, byte[] fileContent, final RequestDataCallback<T> callback, ProgressUIListener progressUIListener) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        add(url, postFile(url, header, name, fileName, fileContent, new MyHttpResponseHandler<>(clazz, url, callback), progressUIListener));
    }

    public void downloadFile(String url, String saveFilePath, Header[] header, ProgressUIListener progressUIListener) {
        downloadFile(url, saveFilePath, header, progressUIListener, true);
    }

    public void downloadFile(String url, String saveFilePath, Header[] header, ProgressUIListener progressUIListener, boolean autoCancel) {
        if (checkAgent()) {
            if (progressUIListener != null) {
                progressUIListener.onProgressFinish();
            }
            return;
        }
        ensureInitialized();
        add(url, downloadFileSendRequest(url, saveFilePath, header, progressUIListener), autoCancel);
    }

    private Call downloadFileSendRequest(String url, final String saveFilePath, Header[] header, final ProgressUIListener progressUIListener) {
        url = Util.getMosaicParameter(url, httpConfig.getCommonField());
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.get();
        return execute(builder, getCommonHeader(header), new DownloadFileResponseHandler(url, saveFilePath, progressUIListener));
    }

    private Call postFile(String url, Header[] header, List<NameValuePair> form, HttpResponseHandler responseCallback, ProgressUIListener progressUIListener) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            MediaType mediaType = MediaType.parse("application/octet-stream");

            if (form == null) {
                form = new ArrayList<>();
            } else {
                form = new ArrayList<>(form);
            }
            form.addAll(httpConfig.getCommonField());

            for (int i = 0; i < form.size(); i++) {
                NameValuePair item = form.get(i);
                if (item.isFile()) {
                    if (item.getValue() == null) {
                        printLog("文件字段:" + item.getName() + "的路径为null，已跳过");
                        continue;
                    }
                    File myFile = new File(item.getValue());
                    if (myFile.exists()) {
                        String fileName = Util.getFileName(item.getValue());
                        builder.addFormDataPart(item.getName(), fileName, RequestBody.create(mediaType, myFile));
                    }
                } else {
                    if (item.getValue() != null) {
                        builder.addFormDataPart(item.getName(), item.getValue());
                    }
                }
            }

            RequestBody requestBody;
            if (progressUIListener == null) {
                requestBody = builder.build();
            } else {
                requestBody = ProgressHelper.withProgress(builder.build(), progressUIListener);
            }
            Request.Builder requestBuider = new Request.Builder();
            requestBuider.url(url);
            requestBuider.post(requestBody);
            return execute(requestBuider, getCommonHeader(header), responseCallback);
        } catch (Exception e) {
            printLog("postFile error: " + e.toString());
            String msg = e.getMessage() != null ? e.getMessage() : "upload error";
            if (responseCallback != null)
                responseCallback.onFailure(-1, msg.getBytes(UTF_8));
        }
        return null;
    }

    private Call postFile(String url, Header[] header, String name, String filename, byte[] fileContent, HttpResponseHandler responseCallback, ProgressUIListener progressUIListener) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            MediaType mediaType = MediaType.parse("application/octet-stream");
            builder.addFormDataPart(name, filename, RequestBody.create(mediaType, fileContent));

            List<NameValuePair> form = new ArrayList<>(httpConfig.getCommonField());
            for (NameValuePair item : form) {
                if (item.getValue() != null) {
                    builder.addFormDataPart(item.getName(), item.getValue());
                }
            }

            RequestBody requestBody;
            if (progressUIListener == null) {
                requestBody = builder.build();
            } else {
                requestBody = ProgressHelper.withProgress(builder.build(), progressUIListener);
            }
            Request.Builder requestBuider = new Request.Builder();
            requestBuider.url(url);
            requestBuider.post(requestBody);
            return execute(requestBuider, getCommonHeader(header), responseCallback);
        } catch (Exception e) {
            printLog("postFileBytes error: " + e.toString());
            String msg = e.getMessage() != null ? e.getMessage() : "upload error";
            if (responseCallback != null)
                responseCallback.onFailure(-1, msg.getBytes(UTF_8));
        }
        return null;
    }

    //异步执行
    private Call execute(Request.Builder builder, Header[] header, Callback responseCallback) {
        Call call = getCall(builder, header);
        if (call != null) {
            call.enqueue(responseCallback);
        }
        return call;
    }

    //同步执行
    private byte[] execute(Request.Builder builder, Header[] header) {
        Call call = getCall(builder, header);
        if (call == null) {
            return new byte[0];
        }
        Response response = null;
        try {
            response = call.execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                return responseBody.bytes();
            }
        } catch (Exception e) {
            printLog("executeSync error: " + e.toString());
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return new byte[0];
    }

    private Call getCall(Request.Builder builder, Header[] header) {
        boolean hasUa = false;
        boolean hasConnection = false;
        boolean hasAccept = false;
        if (header != null) {
            for (Header h : header) {
                printLog("header:" + h.getName() + " value:" + h.getValue());
                builder.header(h.getName(), h.getValue());
                if ("User-Agent".equalsIgnoreCase(h.getName())) {
                    hasUa = true;
                } else if ("Connection".equalsIgnoreCase(h.getName())) {
                    hasConnection = true;
                } else if ("Accept".equalsIgnoreCase(h.getName())) {
                    hasAccept = true;
                }
            }
        }
        if (!hasConnection) {
            builder.header("Connection", "close");
        }
        if (!hasAccept) {
            builder.header("Accept", "*/*");
        }
        if (!hasUa && !TextUtils.isEmpty(httpConfig.getUserAgent())) {
            builder.header("User-Agent", httpConfig.getUserAgent());
        }
        Request request = builder.cacheControl(cacheControl).build();
        return client.newCall(request);
    }

    private class DownloadFileResponseHandler implements Callback {
        private final String saveFilePath;
        private final ProgressUIListener progressUIListener;
        private final String url;

        public DownloadFileResponseHandler(String url, String saveFilePath, ProgressUIListener progressUIListener) {
            this.url = url;
            this.saveFilePath = saveFilePath;
            this.progressUIListener = progressUIListener;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            clear(url);
            String msg = e != null && e.getMessage() != null ? e.getMessage() : "download failed";
            printLog(url + " -1 " + msg);
            if (progressUIListener != null) {
                progressUIListener.onProgressFinish();
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            printLog(url + " code:" + response.code());
            clear(url);
            if (!response.isSuccessful() || response.body() == null) {
                printLog(url + " download failed, code:" + response.code());
                response.close();
                if (progressUIListener != null) {
                    progressUIListener.onProgressFinish();
                }
                return;
            }
            BufferedSink sink = null;
            try {
                ResponseBody responseBody;
                if (progressUIListener != null) {
                    responseBody = ProgressHelper.withProgress(response.body(), progressUIListener);
                } else {
                    responseBody = response.body();
                }
                BufferedSource source = responseBody.source();

                File outFile = new File(saveFilePath);
                File parentDir = outFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                if (outFile.exists()) {
                    outFile.delete();
                }
                outFile.createNewFile();

                sink = Okio.buffer(Okio.sink(outFile));
                source.readAll(sink);
                sink.flush();
            } catch (Exception e) {
                printLog(url + " download write error: " + e.toString());
                if (progressUIListener != null) {
                    progressUIListener.onProgressFinish();
                }
            } finally {
                if (sink != null) {
                    try { sink.close(); } catch (Exception ignored) {}
                }
                response.close();
            }
        }
    }

    private class MyHttpResponseHandler<T> extends HttpResponseHandler {
        private final Class<T> clazz;
        private final String url;
        private final RequestDataCallback<T> callback;

        public MyHttpResponseHandler(Class<T> clazz, String url, RequestDataCallback<T> callback) {
            this.clazz = clazz;
            this.url = url;
            this.callback = callback;
        }

        @Override
        public void onFailure(int status, byte[] data) {
            clear(url);
            String errorMsg = "unknown error";
            try {
                if (data != null) {
                    errorMsg = new String(data, CHARSET_UTF8);
                }
                printLog(url + " " + status + " " + errorMsg);
            } catch (UnsupportedEncodingException e) {
                printLog("encoding error: " + e.toString());
            }
            sendErrorCallback(status, errorMsg, callback);
        }

        @Override
        public void onSuccess(int status, final Header[] headers, byte[] responseBody) {
            try {
                clear(url);
                String str = new String(responseBody, CHARSET_UTF8);
                printLog(url + " " + status + " " + str);
                T t = gson.fromJson(str, clazz);
                sendCallback(status, t, responseBody, callback);
            } catch (Exception e) {
                if (httpConfig.isDebug()) {
                    e.printStackTrace();
                    printLog("自动解析错误:" + e.toString());
                }
                sendErrorCallback(status, "json parse error: " + e.getMessage(), callback);
            }
        }
    }

    private String getUrlKey(String url) {
        if (url != null && url.contains("?")) {
            return url.substring(0, url.indexOf("?"));
        }
        return url;
    }

    private void autoCancel(String function) {
        if (requestHandleMap == null) return;
        Call call = requestHandleMap.remove(function);
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }

    private void add(String url, Call call) {
        add(url, call, true);
    }

    /**
     * 保存请求信息
     *
     * @param url        请求url
     * @param call       http请求call
     * @param autoCancel 自动取消
     */
    private void add(String url, Call call, boolean autoCancel) {
        if (requestHandleMap == null || TextUtils.isEmpty(url)) return;
        String key = getUrlKey(url);
        if (autoCancel) {
            autoCancel(key);
        }
        if (call != null) {
            requestHandleMap.put(key, call);
        }
    }

    private void clear(String url) {
        if (requestHandleMap == null) return;
        String key = getUrlKey(url);
        if (key != null) {
            requestHandleMap.remove(key);
        }
    }

    private void printLog(String content) {
        if (httpConfig != null && httpConfig.isDebug()) {
            Log.i(httpConfig.getTagName(), content != null ? content : "");
        }
    }

    /**
     * 检查代理
     */
    private boolean checkAgent() {
        if (httpConfig.isAgent()) {
            return false;
        } else {
            String proHost = android.net.Proxy.getDefaultHost();
            int proPort = android.net.Proxy.getDefaultPort();
            if (proHost == null || proPort < 0) {
                return false;
            } else {
                printLog("有代理,不能访问");
                return true;
            }
        }
    }

    /**
     * 检查代理，并在被代理阻止时通知回调
     */
    private <T> boolean checkAgentWithCallback(RequestDataCallback<T> callback) {
        if (checkAgent()) {
            sendErrorCallback(-1, "request blocked: proxy detected", callback);
            return true;
        }
        return false;
    }

    /**
     * 取消指定URL的请求
     *
     * @param url 请求URL
     */
    public void cancelRequest(String url) {
        if (requestHandleMap != null && !TextUtils.isEmpty(url)) {
            autoCancel(getUrlKey(url));
        }
    }

    /**
     * 取消所有请求
     */
    public void cancelAllRequests() {
        if (requestHandleMap != null) {
            for (Map.Entry<String, Call> entry : requestHandleMap.entrySet()) {
                Call call = entry.getValue();
                if (call != null && !call.isCanceled()) {
                    call.cancel();
                }
            }
            requestHandleMap.clear();
        }
    }

    /**
     * 获取配置信息
     */
    public HttpConfig getHttpConfig() {
        return httpConfig;
    }

    //更新字段值
    public void updateCommonField(String key, String value) {
        httpConfig.updateCommonField(key, value);
    }

    public void removeCommonField(String key) {
        httpConfig.removeCommonField(key);
    }

    public void addCommonField(String key, String value) {
        httpConfig.addCommonField(key, value);
    }

    private <T> void sendErrorCallback(int status, String errorMsg, RequestDataCallback<T> callback) {
        if (callback != null) {
            ErrorCallbackMessage<T> msgData = new ErrorCallbackMessage<>();
            msgData.status = status;
            msgData.errorMsg = errorMsg;
            msgData.callback = callback;

            Message msg = mainHandler.obtainMessage(MSG_ERROR);
            msg.obj = msgData;
            mainHandler.sendMessage(msg);
        }
    }

    private <T> void sendCallback(int status, T data, byte[] body, RequestDataCallback<T> callback) {
        if (callback == null) return;
        CallbackMessage<T> msgData = new CallbackMessage<>();
        msgData.body = body;
        msgData.status = status;
        msgData.data = data;
        msgData.callback = callback;
        msgData.hasData = true;

        Message msg = mainHandler.obtainMessage(MSG_DATA);
        msg.obj = msgData;
        mainHandler.sendMessage(msg);
    }

    private static final int MSG_DATA = 1;
    private static final int MSG_ERROR = 2;

    private final Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DATA:
                    CallbackMessage data = (CallbackMessage) msg.obj;
                    if (data != null) {
                        data.callback();
                    }
                    break;
                case MSG_ERROR:
                    ErrorCallbackMessage errorData = (ErrorCallbackMessage) msg.obj;
                    if (errorData != null) {
                        errorData.callback();
                    }
                    break;
            }
        }
    };

    private static class CallbackMessage<T> {
        RequestDataCallback<T> callback;
        T data;
        byte[] body;
        int status;
        boolean hasData;

        void callback() {
            if (callback != null) {
                if (hasData) {
                    callback.dataCallback(status, data, body);
                } else {
                    callback.dataCallback(null);
                }
            }
        }
    }

    private static class ErrorCallbackMessage<T> {
        RequestDataCallback<T> callback;
        int status;
        String errorMsg;

        void callback() {
            if (callback != null) {
                callback.onError(status, errorMsg);
            }
        }
    }

    // ==================== PUT 请求 ====================

    public <T> void put(final Class<T> clazz, final String url, Header[] header, final Map<String, Object> params, final RequestDataCallback<T> callback) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        Map<String, Object> safeParams = params != null ? params : new HashMap<String, Object>();
        String jsonStr = gson.toJson(safeParams);
        printLog("requestBody json: " + jsonStr);
        RequestBody requestBody = RequestBody.create(JSON, jsonStr);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.put(requestBody);
        add(url, execute(builder, getCommonHeader(header), new MyHttpResponseHandler<>(clazz, url, callback)), true);
    }

    public <T> void put(final Class<T> clazz, final String url, Header[] header, final String jsonBody, final RequestDataCallback<T> callback) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        String json = TextUtils.isEmpty(jsonBody) ? "{}" : jsonBody;
        printLog("requestBody json: " + json);
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.put(requestBody);
        add(url, execute(builder, getCommonHeader(header), new MyHttpResponseHandler<>(clazz, url, callback)), true);
    }

    // ==================== DELETE 请求 ====================

    public <T> void delete(final Class<T> clazz, final String url, Header[] header, final RequestDataCallback<T> callback) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.delete();
        add(url, execute(builder, getCommonHeader(header), new MyHttpResponseHandler<>(clazz, url, callback)), true);
    }

    public <T> void delete(final Class<T> clazz, final String url, Header[] header, final String jsonBody, final RequestDataCallback<T> callback) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        String json = TextUtils.isEmpty(jsonBody) ? "{}" : jsonBody;
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.delete(requestBody);
        add(url, execute(builder, getCommonHeader(header), new MyHttpResponseHandler<>(clazz, url, callback)), true);
    }

    // ==================== PATCH 请求 ====================

    public <T> void patch(final Class<T> clazz, final String url, Header[] header, final Map<String, Object> params, final RequestDataCallback<T> callback) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        Map<String, Object> safeParams = params != null ? params : new HashMap<String, Object>();
        String jsonStr = gson.toJson(safeParams);
        printLog("requestBody json: " + jsonStr);
        RequestBody requestBody = RequestBody.create(JSON, jsonStr);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.patch(requestBody);
        add(url, execute(builder, getCommonHeader(header), new MyHttpResponseHandler<>(clazz, url, callback)), true);
    }

    public <T> void patch(final Class<T> clazz, final String url, Header[] header, final String jsonBody, final RequestDataCallback<T> callback) {
        if (checkAgentWithCallback(callback)) {
            return;
        }
        ensureInitialized();
        String json = TextUtils.isEmpty(jsonBody) ? "{}" : jsonBody;
        printLog("requestBody json: " + json);
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.patch(requestBody);
        add(url, execute(builder, getCommonHeader(header), new MyHttpResponseHandler<>(clazz, url, callback)), true);
    }
}
