package com.dawn.http.http.entity;

import com.dawn.http.http.net.NameValuePair;

import java.util.ArrayList;
import java.util.List;

public class HttpConfig {
    private boolean debug = true;//true:debug模式
    private String userAgent = "";//用户代理 它是一个特殊字符串头，使得服务器能够识别客户使用的操作系统及版本、CPU 类型、浏览器及版本、浏览器渲染引擎、浏览器语言、浏览器插件等。
    private boolean agent = true;//有代理的情况能不能访问，true:有代理能访问 false:有代理不能访问
    private String tagName = "Http";

    private int connectTimeout = 10;//连接超时时间 单位:秒
    private int writeTimeout = 10;//写入超时时间 单位:秒
    private int readTimeout = 30;//读取超时时间 单位:秒

    //通用字段
    private final List<NameValuePair> commonField = new ArrayList<>();
    private final List<NameValuePair> headerField = new ArrayList<>();//请求头字段

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent != null ? userAgent : "";
    }

    public boolean isAgent() {
        return agent;
    }

    public void setAgent(boolean agent) {
        this.agent = agent;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName != null ? tagName : "Http";
    }

    public synchronized List<NameValuePair> getCommonField() {
        return new ArrayList<>(commonField);
    }

    public synchronized List<NameValuePair> getHeaderField() {
        return new ArrayList<>(headerField);
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        if (connectTimeout < 0) {
            throw new IllegalArgumentException("connectTimeout must be >= 0");
        }
        this.connectTimeout = connectTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        if (writeTimeout < 0) {
            throw new IllegalArgumentException("writeTimeout must be >= 0");
        }
        this.writeTimeout = writeTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        if (readTimeout < 0) {
            throw new IllegalArgumentException("readTimeout must be >= 0");
        }
        this.readTimeout = readTimeout;
    }

    /**
     * 更新参数
     *
     * @param key
     * @param value
     */
    public synchronized void updateCommonField(String key, String value) {
        boolean result = true;
        for (int i = 0; i < commonField.size(); i++) {
            NameValuePair nameValuePair = commonField.get(i);
            if (key != null && key.equals(nameValuePair.getName())) {
                commonField.set(i, new NameValuePair(key, value));
                result = false;
                break;
            }
        }
        if (result) {
            commonField.add(new NameValuePair(key, value));
        }
    }

    /**
     * 删除公共参数
     *
     * @param key
     */
    public synchronized void removeCommonField(String key) {
        for (int i = commonField.size() - 1; i >= 0; i--) {
            if (key != null && key.equals(commonField.get(i).getName())) {
                commonField.remove(i);
            }
        }
    }

    /**
     * 清空公共参数
     */
    public synchronized void clearCommonField() {
        commonField.clear();
    }

    /**
     * 添加请求参数
     *
     * @param key
     * @param value
     */
    public synchronized void addCommonField(String key, String value) {
        commonField.add(new NameValuePair(key, value));
    }

    /**
     * 添加请求头字段
     *
     * @param key
     * @param value
     */
    public synchronized void addHeaderField(String key, String value) {
        headerField.add(new NameValuePair(key, value));
    }

    /**
     * 更新请求头字段
     *
     * @param key
     * @param value
     */
    public synchronized void updateHeaderField(String key, String value) {
        boolean found = false;
        for (int i = 0; i < headerField.size(); i++) {
            NameValuePair nameValuePair = headerField.get(i);
            if (key != null && key.equals(nameValuePair.getName())) {
                headerField.set(i, new NameValuePair(key, value));
                found = true;
                break;
            }
        }
        if (!found) {
            headerField.add(new NameValuePair(key, value));
        }
    }

    /**
     * 删除请求头字段
     *
     * @param key
     */
    public synchronized void removeHeaderField(String key) {
        for (int i = headerField.size() - 1; i >= 0; i--) {
            if (key != null && key.equals(headerField.get(i).getName())) {
                headerField.remove(i);
            }
        }
    }

    /**
     * 清空请求头字段
     */
    public synchronized void clearHeaderField() {
        headerField.clear();
    }
}
