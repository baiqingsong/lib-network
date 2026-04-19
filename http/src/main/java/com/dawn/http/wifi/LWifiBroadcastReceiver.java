package com.dawn.http.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * WiFi广播接收器
 * <p>
 * 监听WiFi状态变化、扫描结果、连接/断开事件
 */
public abstract class LWifiBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        switch (action) {
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    onWifiEnabled();
                } else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                    onWifiDisabled();
                }
                break;
            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    List<ScanResult> results = wifiManager.getScanResults();
                    onScanResultsAvailable(results);
                }
                break;
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.isConnected()) {
                        WifiManager wm = (WifiManager) context.getApplicationContext()
                                .getSystemService(Context.WIFI_SERVICE);
                        if (wm != null && wm.getConnectionInfo() != null) {
                            String ssid = wm.getConnectionInfo().getSSID();
                            if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                                ssid = ssid.substring(1, ssid.length() - 1);
                            }
                            onWifiConnected(ssid);
                        }
                    } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                        onWifiDisconnected();
                    }
                }
                break;
        }
    }

    /**
     * WiFi已开启
     */
    public abstract void onWifiEnabled();

    /**
     * WiFi已关闭
     */
    public abstract void onWifiDisabled();

    /**
     * WiFi扫描结果可用
     *
     * @param scanResults 扫描结果列表
     */
    public abstract void onScanResultsAvailable(List<ScanResult> scanResults);

    /**
     * WiFi已连接
     *
     * @param ssid 已连接的WiFi名称
     */
    public abstract void onWifiConnected(String ssid);

    /**
     * WiFi已断开
     */
    public abstract void onWifiDisconnected();
}
