package com.dawn.http.wifi;

import com.dawn.http.wifi.ping.NetworkAnalysisResult;
import com.dawn.http.wifi.ping.PingAnalyzer;

/**
 * WiFi工具类
 * <p>
 * 提供通过Ping检测网络连通性的功能
 */
public class LWifiUtil {

    /**
     * 通过Ping检测网络是否正常
     *
     * @param host      主机地址
     * @param pingCount ping次数
     * @return 是否ping成功
     */
    public static boolean isPingSuccess(String host, int pingCount) {
        return LNetUtil.ping(host, pingCount, null);
    }

    /**
     * 通过Ping检测网络是否正常（默认4次）
     *
     * @param host 主机地址
     * @return 是否ping成功
     */
    public static boolean isPingSuccess(String host) {
        return isPingSuccess(host, 4);
    }

    /**
     * Ping并获取网络质量分析结果
     *
     * @param host      主机地址
     * @param pingCount ping次数
     * @return 分析结果
     */
    public static NetworkAnalysisResult pingAnalyze(String host, int pingCount) {
        StringBuffer buffer = new StringBuffer();
        LNetUtil.ping(host, pingCount, buffer);
        return PingAnalyzer.analyze(buffer.toString());
    }
}
