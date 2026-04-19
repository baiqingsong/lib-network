package com.dawn.http.wifi;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;

/**
 * 网络工具类
 * <p>
 * 提供网络状态检测、WiFi信息获取、Ping测试等功能
 */
@SuppressWarnings({"unused", "deprecation"})
public class LNetUtil {

    private static final String NETWORK_TYPE_WIFI = "wifi";
    private static final String NETWORK_TYPE_UNKNOWN = "unknown";
    private static final String NETWORK_TYPE_DISCONNECT = "disconnect";

    /**
     * 获取网络类型
     *
     * @param context 上下文
     * @return 网络类型（ConnectivityManager.TYPE_*），无网络返回-1
     */
    public static int getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm != null ? cm.getActiveNetworkInfo() : null;
        return networkInfo != null ? networkInfo.getType() : -1;
    }

    /**
     * 网络是否可用
     *
     * @param context 上下文
     * @return 是否可用
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo info = cm.getActiveNetworkInfo();
                return info != null && info.isAvailable();
            }
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    /**
     * 网络是否已连接
     *
     * @param context 上下文
     * @return 是否已连接
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * 是否是WiFi连接
     *
     * @param context 上下文
     * @return 是否是WiFi
     */
    public static boolean isWiFi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo active = cm.getActiveNetworkInfo();
        return active != null && active.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 是否是移动网络
     *
     * @param context 上下文
     * @return 是否是移动网络
     */
    public static boolean isMobileNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * 获取网络类型名称
     *
     * @param context 上下文
     * @return 网络类型名称（wifi / 移动子类型名 / disconnect / unknown）
     */
    public static String getNetworkTypeName(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return NETWORK_TYPE_UNKNOWN;
        }
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return NETWORK_TYPE_DISCONNECT;
        }
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            return NETWORK_TYPE_WIFI;
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            return info.getSubtypeName();
        }
        return NETWORK_TYPE_UNKNOWN;
    }

    /**
     * 打开网络设置界面
     *
     * @param activity Activity
     */
    public static void openNetSetting(Activity activity) {
        Intent intent = new Intent();
        ComponentName cm = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        activity.startActivityForResult(intent, 0);
    }

    /**
     * 设置WiFi开关状态
     *
     * @param context 上下文
     * @param enabled 是否打开WiFi
     */
    public static void setWifiEnabled(Context context, boolean enabled) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            wifiManager.setWifiEnabled(enabled);
        }
    }

    /**
     * 获取WiFi信号强度
     *
     * @param context 上下文
     * @return RSSI值
     */
    public static int getWifiSignalStrength(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return 0;
        }
        return wifiManager.getConnectionInfo().getRssi();
    }

    /**
     * 获取当前连接的WiFi名称
     *
     * @param context 上下文
     * @return WiFi SSID
     */
    public static String getCurrentWifiName(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return "";
        }
        String ssid = wifiManager.getConnectionInfo().getSSID();
        if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid != null ? ssid : "";
    }

    /**
     * 获取本机IP地址
     *
     * @return IP地址字符串
     */
    public static String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = en.nextElement();
                Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    /**
     * Ping测试
     *
     * @param host        主机地址
     * @param pingCount   ping次数
     * @param resultBuffer 结果缓冲区（可为null）
     * @return 是否ping通
     */
    public static boolean ping(String host, int pingCount, StringBuffer resultBuffer) {
        String command = "ping -c " + pingCount + " " + host;
        Process process = null;
        BufferedReader reader = null;
        boolean isSuccess = false;
        try {
            process = Runtime.getRuntime().exec(command);
            if (process == null) {
                appendBuffer(resultBuffer, "ping fail: process is null.");
                return false;
            }
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                appendBuffer(resultBuffer, line);
            }
            int status = process.waitFor();
            isSuccess = (status == 0);
            appendBuffer(resultBuffer, isSuccess ? "exec cmd success: " + command : "exec cmd fail.");
        } catch (IOException | InterruptedException e) {
            // ignore
        } finally {
            if (process != null) {
                process.destroy();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return isSuccess;
    }

    /**
     * 简单ping测试（只测试一次）
     *
     * @param host 主机地址
     * @return 是否ping通
     */
    public static boolean simplePing(String host) {
        return ping(host, 1, null);
    }

    /**
     * 从ping输出中解析平均延迟（ms）
     *
     * @param host      主机地址
     * @param pingCount ping次数
     * @return 平均延迟，解析失败返回null
     */
    public static Double getPingAverageMs(String host, int pingCount) {
        StringBuffer sb = new StringBuffer();
        boolean ok = ping(host, pingCount, sb);
        if (!ok) {
            return null;
        }
        String out = sb.toString();
        for (String line : out.split("\n")) {
            if (line.contains("min/avg/max") || line.contains("/avg/")) {
                int eq = line.indexOf('=');
                if (eq >= 0 && eq + 1 < line.length()) {
                    String tail = line.substring(eq + 1).trim().replace(" ms", "").trim();
                    String[] parts = tail.split("/");
                    if (parts.length >= 2) {
                        try {
                            return Double.parseDouble(parts[1]);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 检查指定端口是否可连接
     *
     * @param host    主机地址
     * @param port    端口号
     * @param timeout 超时时间（毫秒）
     * @return 是否可连接
     */
    public static boolean isPortReachable(String host, int port, int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void appendBuffer(StringBuffer buffer, String text) {
        if (buffer != null) {
            buffer.append(text).append("\n");
        }
    }
}
