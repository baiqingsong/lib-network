package com.dawn.http;

import com.dawn.http.http.entity.HttpConfig;
import com.dawn.http.http.net.HTTPCaller;
import com.dawn.http.http.net.Header;
import com.dawn.http.http.net.NameValuePair;
import com.dawn.http.http.net.RequestDataCallback;
import com.dawn.http.http.ui.ProgressUIListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    private static volatile HttpUtil _instance = null;

    public static HttpUtil getInstance() {
        if (_instance == null) {
            synchronized (HttpUtil.class) {
                if (_instance == null) {
                    _instance = new HttpUtil();
                }
            }
        }
        return _instance;
    }

    private HttpUtil() {
    }

    // ==================== 初始化 ====================

    /**
     * 初始化HTTP配置（必须在使用前调用）
     *
     * @param httpConfig 配置信息
     */
    public void init(HttpConfig httpConfig) {
        HTTPCaller.getInstance().setHttpConfig(httpConfig);
    }

    /**
     * 获取HTTP配置
     */
    public HttpConfig getHttpConfig() {
        return HTTPCaller.getInstance().getHttpConfig();
    }

    // ==================== GET 请求 ====================

    public <T> void get(Class<T> clazz, String url, RequestDataCallback<T> callback) {
        get(clazz, url, null, callback);
    }

    public <T> void get(Class<T> clazz, String url, Map<String, String> headersMap, RequestDataCallback<T> callback) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().get(clazz, url, headers, callback);
    }

    public <T> T getSync(Class<T> clazz, String url) {
        return HTTPCaller.getInstance().getSync(clazz, url);
    }

    public <T> T getSync(Class<T> clazz, String url, Map<String, String> headersMap) {
        Header[] headers = convertHeaders(headersMap);
        return HTTPCaller.getInstance().getSync(clazz, url, headers);
    }

    // ==================== POST 表单请求 ====================

    public <T> void post(Class<T> clazz, String url, Map<String, String> paramsMap, RequestDataCallback<T> callback) {
        post(clazz, url, null, paramsMap, callback);
    }

    public <T> void post(Class<T> clazz, String url, Map<String, String> headersMap, Map<String, String> paramsMap, RequestDataCallback<T> callback) {
        Header[] headers = convertHeaders(headersMap);
        List<NameValuePair> params = convertParams(paramsMap);
        HTTPCaller.getInstance().post(clazz, url, headers, params, callback, true);
    }

    // ==================== POST JSON请求 ====================

    /**
     * POST JSON字符串请求
     *
     * @param clazz    响应类型
     * @param url      请求URL
     * @param json     JSON字符串
     * @param callback 回调
     */
    public <T> void postJson(Class<T> clazz, String url, String json, RequestDataCallback<T> callback) {
        postJson(clazz, url, null, json, callback);
    }

    public <T> void postJson(Class<T> clazz, String url, Map<String, String> headersMap, String json, RequestDataCallback<T> callback) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().post(clazz, url, headers, json, callback);
    }

    // ==================== POST 同步请求 ====================

    public <T> T postSync(Class<T> clazz, String url, Map<String, String> paramsMap) {
        return postSync(clazz, url, null, paramsMap);
    }

    public <T> T postSync(Class<T> clazz, String url, Map<String, String> headersMap, Map<String, String> paramsMap) {
        Header[] headers = convertHeaders(headersMap);
        List<NameValuePair> params = convertParams(paramsMap);
        return HTTPCaller.getInstance().postSync(clazz, url, params, headers);
    }

    // ==================== POST 同步JSON请求 ====================

    /**
     * POST同步JSON请求
     */
    public <T> T postSyncJson(Class<T> clazz, String url, String json) {
        return postSyncJson(clazz, url, null, json);
    }

    public <T> T postSyncJson(Class<T> clazz, String url, Map<String, String> headersMap, String json) {
        Header[] headers = convertHeaders(headersMap);
        return HTTPCaller.getInstance().postSyncJson(clazz, url, json, headers);
    }

    /**
     * POST同步JSON请求，返回原始字节数据
     */
    public byte[] postSyncJsonRaw(String url, String json) {
        return postSyncJsonRaw(url, null, json);
    }

    public byte[] postSyncJsonRaw(String url, Map<String, String> headersMap, String json) {
        Header[] headers = convertHeaders(headersMap);
        return HTTPCaller.getInstance().postSyncJsonRaw(url, json, headers);
    }

    // ==================== POST 自定义Content-Type请求 ====================

    /**
     * 自定义Content-Type的POST请求（异步，字符串body）
     *
     * @param clazz       响应类型
     * @param url         请求URL
     * @param headersMap  请求头
     * @param body        请求体字符串
     * @param contentType 内容类型，如 "application/xml; charset=utf-8"、"text/plain" 等
     * @param callback    回调
     */
    public <T> void postBody(Class<T> clazz, String url, Map<String, String> headersMap, String body, String contentType, RequestDataCallback<T> callback) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().postBody(clazz, url, headers, body, contentType, callback);
    }

    /**
     * 自定义Content-Type的POST请求（异步，字节数组body）
     *
     * @param clazz       响应类型
     * @param url         请求URL
     * @param headersMap  请求头
     * @param body        请求体字节数组
     * @param contentType 内容类型
     * @param callback    回调
     */
    public <T> void postBody(Class<T> clazz, String url, Map<String, String> headersMap, byte[] body, String contentType, RequestDataCallback<T> callback) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().postBody(clazz, url, headers, body, contentType, callback);
    }

    /**
     * 自定义Content-Type的POST请求（同步）
     */
    public <T> T postBodySync(Class<T> clazz, String url, Map<String, String> headersMap, String body, String contentType) {
        Header[] headers = convertHeaders(headersMap);
        return HTTPCaller.getInstance().postBodySync(clazz, url, headers, body, contentType);
    }

    /**
     * 自定义Content-Type的POST请求（同步，返回原始字节）
     */
    public byte[] postBodySyncRaw(String url, Map<String, String> headersMap, String body, String contentType) {
        Header[] headers = convertHeaders(headersMap);
        return HTTPCaller.getInstance().postBodySyncRaw(url, headers, body, contentType);
    }

    // ==================== PUT 请求 ====================

    public <T> void put(Class<T> clazz, String url, Map<String, String> headersMap, Map<String, Object> paramsMap, RequestDataCallback<T> callback) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().put(clazz, url, headers, paramsMap, callback);
    }

    public <T> void putJson(Class<T> clazz, String url, Map<String, String> headersMap, String json, RequestDataCallback<T> callback) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().put(clazz, url, headers, json, callback);
    }

    // ==================== DELETE 请求 ====================

    public <T> void delete(Class<T> clazz, String url, Map<String, String> headersMap, RequestDataCallback<T> callback) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().delete(clazz, url, headers, callback);
    }

    public <T> void deleteJson(Class<T> clazz, String url, Map<String, String> headersMap, String json, RequestDataCallback<T> callback) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().delete(clazz, url, headers, json, callback);
    }

    // ==================== PATCH 请求 ====================

    public <T> void patch(Class<T> clazz, String url, Map<String, String> headersMap, Map<String, Object> paramsMap, RequestDataCallback<T> callback) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().patch(clazz, url, headers, paramsMap, callback);
    }

    // ==================== 文件上传 ====================

    /**
     * 上传文件（通过NameValuePair列表传递文件和表单字段）
     */
    public <T> void postFile(Class<T> clazz, String url, Map<String, String> headersMap, List<NameValuePair> form, RequestDataCallback<T> callback) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().postFile(clazz, url, headers, form, callback);
    }

    /**
     * 上传文件（带进度回调）
     */
    public <T> void postFile(Class<T> clazz, String url, Map<String, String> headersMap, List<NameValuePair> form, RequestDataCallback<T> callback, ProgressUIListener progressListener) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().postFile(clazz, url, headers, form, callback, progressListener);
    }

    /**
     * 上传文件（字节数组方式）
     */
    public <T> void postFile(Class<T> clazz, String url, Map<String, String> headersMap, String name, String fileName, byte[] fileContent, RequestDataCallback<T> callback) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().postFile(clazz, url, headers, name, fileName, fileContent, callback);
    }

    /**
     * 上传文件（字节数组方式，带进度回调）
     */
    public <T> void postFile(Class<T> clazz, String url, Map<String, String> headersMap, String name, String fileName, byte[] fileContent, RequestDataCallback<T> callback, ProgressUIListener progressListener) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().postFile(clazz, url, headers, name, fileName, fileContent, callback, progressListener);
    }

    // ==================== 文件下载 ====================

    /**
     * 下载文件
     *
     * @param url              下载URL
     * @param saveFilePath     保存文件路径
     * @param headersMap       请求头
     * @param progressListener 进度回调
     */
    public void downloadFile(String url, String saveFilePath, Map<String, String> headersMap, ProgressUIListener progressListener) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().downloadFile(url, saveFilePath, headers, progressListener);
    }

    public void downloadFile(String url, String saveFilePath, ProgressUIListener progressListener) {
        HTTPCaller.getInstance().downloadFile(url, saveFilePath, null, progressListener);
    }

    // ==================== 请求管理 ====================

    /**
     * 取消指定URL的请求
     */
    public void cancelRequest(String url) {
        HTTPCaller.getInstance().cancelRequest(url);
    }

    /**
     * 取消所有请求
     */
    public void cancelAllRequests() {
        HTTPCaller.getInstance().cancelAllRequests();
    }

    // ==================== 公共字段管理 ====================

    public void addCommonField(String key, String value) {
        HTTPCaller.getInstance().addCommonField(key, value);
    }

    public void updateCommonField(String key, String value) {
        HTTPCaller.getInstance().updateCommonField(key, value);
    }

    public void removeCommonField(String key) {
        HTTPCaller.getInstance().removeCommonField(key);
    }

    // ==================== 公共请求头管理 ====================

    public void addHeaderField(String key, String value) {
        HTTPCaller.getInstance().getHttpConfig().addHeaderField(key, value);
    }

    public void updateHeaderField(String key, String value) {
        HTTPCaller.getInstance().getHttpConfig().updateHeaderField(key, value);
    }

    public void removeHeaderField(String key) {
        HTTPCaller.getInstance().getHttpConfig().removeHeaderField(key);
    }

    // ==================== POST 同步原始字节 ====================

    /**
     * POST同步请求，返回原始字节数据
     */
    public byte[] postSyncRaw(String url, Map<String, String> paramsMap) {
        return postSyncRaw(url, null, paramsMap);
    }

    public byte[] postSyncRaw(String url, Map<String, String> headersMap, Map<String, String> paramsMap) {
        Header[] headers = convertHeaders(headersMap);
        List<NameValuePair> params = convertParams(paramsMap);
        return HTTPCaller.getInstance().postSyncRaw(url, params, headers);
    }

    // ==================== PATCH JSON请求 ====================

    public <T> void patchJson(Class<T> clazz, String url, Map<String, String> headersMap, String json, RequestDataCallback<T> callback) {
        Header[] headers = convertHeaders(headersMap);
        HTTPCaller.getInstance().patch(clazz, url, headers, json, callback);
    }

    // ==================== 内部工具方法 ====================

    private Header[] convertHeaders(Map<String, String> headersMap) {
        if (headersMap == null || headersMap.isEmpty()) {
            return null;
        }
        Header[] headers = new Header[headersMap.size()];
        int index = 0;
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            headers[index++] = new Header(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    private List<NameValuePair> convertParams(Map<String, String> paramsMap) {
        if (paramsMap == null || paramsMap.isEmpty()) {
            return null;
        }
        List<NameValuePair> params = new ArrayList<>();
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            params.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        return params;
    }
}
