package com.dawn.http.wifi;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * WiFi操作工厂类（单例）
 * <p>
 * 提供WiFi的打开、扫描、连接、断开、清除配置等功能。
 * 通过 {@link OnWifiListener} 回调通知WiFi状态变化。
 * <p>
 * 使用方式：
 * <pre>
 * WifiFactory wifiFactory = WifiFactory.getInstance(context);
 * wifiFactory.setListener(new OnWifiListener() { ... });
 * wifiFactory.openWifi();
 * wifiFactory.connectWifi("SSID", "password");
 * </pre>
 */
@SuppressWarnings("deprecation")
public class WifiFactory {

    private static volatile WifiFactory instance;

    private final Context mContext;
    private final LWifiMgr mWifiMgr;
    private LWifiBroadcastReceiver mReceiver;
    private List<ScanResult> mScanResults;
    private OnWifiListener mListener;

    public static WifiFactory getInstance(Context context) {
        if (instance == null) {
            synchronized (WifiFactory.class) {
                if (instance == null) {
                    instance = new WifiFactory(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private WifiFactory(Context context) {
        this.mContext = context;
        this.mWifiMgr = new LWifiMgr(context);
    }

    /**
     * 设置WiFi状态监听器
     *
     * @param listener 监听器，传null清除监听
     */
    public void setListener(OnWifiListener listener) {
        this.mListener = listener;
    }

    /**
     * 打开WiFi并注册广播接收器，开始扫描
     */
    public void openWifi() {
        mWifiMgr.openWifi();
        registerWifiReceiver();
        mWifiMgr.startScan();
    }

    /**
     * 关闭WiFi广播接收器
     */
    public void closeWifiReceiver() {
        if (mReceiver != null) {
            try {
                mContext.unregisterReceiver(mReceiver);
            } catch (Exception ignored) {
            }
            mReceiver = null;
        }
    }

    /**
     * 获取当前已连接的WiFi SSID
     *
     * @return WiFi SSID，未连接返回空字符串
     */
    public String getConnectWifiSsid() {
        return mWifiMgr.getConnectedSSID();
    }

    /**
     * 连接WiFi
     *
     * @param ssid     WiFi名称
     * @param password WiFi密码
     */
    public void connectWifi(String ssid, String password) {
        mWifiMgr.connectWifi(ssid, password);
    }

    /**
     * 断开当前WiFi连接
     *
     * @param ssid WiFi名称
     */
    public void disconnectWifi(String ssid) {
        mWifiMgr.disconnectWifi(ssid);
    }

    /**
     * 清除所有WiFi配置
     */
    public void clearWifiConfig() {
        mWifiMgr.clearAllWifiConfig();
    }

    /**
     * 清除指定WiFi配置
     *
     * @param ssid WiFi名称
     */
    public void clearWifiConfig(String ssid) {
        mWifiMgr.clearWifiConfig(ssid);
    }

    /**
     * WiFi是否已打开
     */
    public boolean isWifiEnabled() {
        return mWifiMgr.isWifiEnabled();
    }

    /**
     * 是否已连接WiFi
     */
    public boolean isWifiConnected() {
        return mWifiMgr.isWifiConnected();
    }

    /**
     * 获取WiFi信号强度（RSSI）
     */
    public int getWifiSignalStrength() {
        return mWifiMgr.getWifiSignalStrength();
    }

    /**
     * 获取WiFi信号等级
     *
     * @param numLevels 等级数量
     */
    public int getWifiSignalLevel(int numLevels) {
        return mWifiMgr.getWifiSignalLevel(numLevels);
    }

    /**
     * 获取WiFi IP地址
     */
    public String getWifiIpAddress() {
        return mWifiMgr.getIpAddressFromWifi();
    }

    /**
     * 获取SSID名称列表（去重）
     */
    private List<String> getSsidNameList() {
        if (mScanResults == null || mScanResults.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> ssidSet = new HashSet<>();
        List<String> ssidList = new ArrayList<>();
        for (ScanResult result : mScanResults) {
            if (result.SSID != null && !result.SSID.isEmpty() && ssidSet.add(result.SSID)) {
                ssidList.add(result.SSID);
            }
        }
        return ssidList;
    }

    /**
     * 注册WiFi广播接收器
     */
    private void registerWifiReceiver() {
        if (mReceiver != null) {
            return;
        }
        mReceiver = new LWifiBroadcastReceiver() {
            @Override
            public void onWifiEnabled() {
                mWifiMgr.startScan();
            }

            @Override
            public void onWifiDisabled() {
                // WiFi关闭
            }

            @Override
            public void onScanResultsAvailable(List<ScanResult> scanResults) {
                mScanResults = scanResults;
                List<String> ssidList = getSsidNameList();
                if (mListener != null) {
                    mListener.refreshWifiList(ssidList);
                }
            }

            @Override
            public void onWifiConnected(String ssid) {
                if (mListener != null) {
                    mListener.wifiConnectSuccess(ssid);
                }
            }

            @Override
            public void onWifiDisconnected() {
                if (mListener != null) {
                    mListener.wifiDisconnect();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mReceiver, filter);
    }
}
