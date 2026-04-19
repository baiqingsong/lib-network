package com.dawn.http.wifi;

import java.util.List;

/**
 * WiFi状态监听器
 */
public interface OnWifiListener {
    /**
     * WiFi列表刷新回调
     *
     * @param wifiList 可用WiFi的SSID列表
     */
    void refreshWifiList(List<String> wifiList);

    /**
     * WiFi连接成功回调
     *
     * @param ssid 已连接的WiFi名称
     */
    void wifiConnectSuccess(String ssid);

    /**
     * WiFi断开连接回调
     */
    void wifiDisconnect();
}
