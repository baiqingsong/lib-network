package com.dawn.http.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * WiFi管理器封装
 * <p>
 * 封装 Android WifiManager 的常用操作：开关WiFi、扫描、连接、断开、清除配置等
 */
@SuppressWarnings("deprecation")
class LWifiMgr {

    private final Context mContext;
    private final WifiManager mWifiManager;

    public LWifiMgr(Context context) {
        this.mContext = context.getApplicationContext();
        this.mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 打开WiFi
     */
    public void openWifi() {
        if (mWifiManager != null && !mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭WiFi
     */
    public void closeWifi() {
        if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * WiFi是否已打开
     */
    public boolean isWifiEnabled() {
        return mWifiManager != null && mWifiManager.isWifiEnabled();
    }

    /**
     * 是否通过WiFi连接
     */
    public boolean isWifiConnected() {
        if (mWifiManager == null) {
            return false;
        }
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        return wifiInfo != null && wifiInfo.getNetworkId() != -1;
    }

    /**
     * 开始扫描WiFi
     */
    public boolean startScan() {
        return mWifiManager != null && mWifiManager.startScan();
    }

    /**
     * 获取扫描结果
     */
    public List<ScanResult> getScanResults() {
        if (mWifiManager == null) {
            return new ArrayList<>();
        }
        List<ScanResult> results = mWifiManager.getScanResults();
        return results != null ? results : new ArrayList<>();
    }

    /**
     * 获取当前连接的WiFi SSID
     */
    public String getConnectedSSID() {
        if (mWifiManager == null) {
            return "";
        }
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return "";
        }
        String ssid = wifiInfo.getSSID();
        if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid != null ? ssid : "";
    }

    /**
     * 连接WiFi
     *
     * @param ssid     WiFi名称
     * @param password WiFi密码
     * @return 是否成功发起连接
     */
    public boolean connectWifi(String ssid, String password) {
        if (mWifiManager == null || ssid == null) {
            return false;
        }

        // 先断开当前连接
        mWifiManager.disconnect();

        // 检查是否已有此网络的配置
        WifiConfiguration existingConfig = getExistingConfig(ssid);
        if (existingConfig != null) {
            // 移除旧配置
            mWifiManager.removeNetwork(existingConfig.networkId);
        }

        // 创建新的WiFi配置
        WifiConfiguration config = createWifiConfig(ssid, password);
        int networkId = mWifiManager.addNetwork(config);
        if (networkId == -1) {
            return false;
        }

        mWifiManager.enableNetwork(networkId, true);
        return mWifiManager.reconnect();
    }

    /**
     * 断开指定WiFi
     *
     * @param ssid WiFi名称
     */
    public boolean disconnectWifi(String ssid) {
        if (mWifiManager == null) {
            return false;
        }
        WifiConfiguration config = getExistingConfig(ssid);
        if (config != null) {
            mWifiManager.disableNetwork(config.networkId);
        }
        return mWifiManager.disconnect();
    }

    /**
     * 清除指定WiFi配置
     *
     * @param ssid WiFi名称
     */
    public void clearWifiConfig(String ssid) {
        if (mWifiManager == null || ssid == null) {
            return;
        }
        WifiConfiguration config = getExistingConfig(ssid);
        if (config != null) {
            mWifiManager.removeNetwork(config.networkId);
            mWifiManager.saveConfiguration();
        }
    }

    /**
     * 清除所有WiFi配置
     */
    public void clearAllWifiConfig() {
        if (mWifiManager == null) {
            return;
        }
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                mWifiManager.removeNetwork(config.networkId);
            }
            mWifiManager.saveConfiguration();
        }
    }

    /**
     * 获取当前WiFi信号强度
     *
     * @return RSSI值
     */
    public int getWifiSignalStrength() {
        if (mWifiManager == null) {
            return 0;
        }
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        return wifiInfo != null ? wifiInfo.getRssi() : 0;
    }

    /**
     * 获取WiFi信号等级
     *
     * @param numLevels 等级数量
     * @return 信号等级（0 到 numLevels-1）
     */
    public int getWifiSignalLevel(int numLevels) {
        return WifiManager.calculateSignalLevel(getWifiSignalStrength(), numLevels);
    }

    /**
     * 获取当前WiFi连接信息
     */
    public WifiInfo getCurrentWifiInfo() {
        return mWifiManager != null ? mWifiManager.getConnectionInfo() : null;
    }

    /**
     * 获取WiFi的IP地址（热点场景下）
     */
    public String getIpAddressFromWifi() {
        if (mWifiManager == null) {
            return "";
        }
        int ip = mWifiManager.getConnectionInfo().getIpAddress();
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                ((ip >> 24) & 0xFF);
    }

    /**
     * 查找已保存的WiFi配置
     */
    private WifiConfiguration getExistingConfig(String ssid) {
        if (mWifiManager == null || ssid == null) {
            return null;
        }
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs == null) {
            return null;
        }
        String quotedSsid = "\"" + ssid + "\"";
        for (WifiConfiguration config : configs) {
            if (quotedSsid.equals(config.SSID) || ssid.equals(config.SSID)) {
                return config;
            }
        }
        return null;
    }

    /**
     * 创建WiFi配置
     */
    private WifiConfiguration createWifiConfig(String ssid, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\"";

        if (password == null || password.isEmpty()) {
            // 开放网络
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else {
            // WPA/WPA2加密
            config.preSharedKey = "\"" + password + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        }

        config.status = WifiConfiguration.Status.ENABLED;
        return config;
    }
}
