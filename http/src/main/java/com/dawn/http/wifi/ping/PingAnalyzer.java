package com.dawn.http.wifi.ping;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ping输出解析器
 * <p>
 * 通过正则解析ping命令的输出，提取延迟数据、丢包率等信息，
 * 生成 {@link NetworkAnalysisResult} 分析结果。
 */
public class PingAnalyzer {

    // 匹配单行延迟：time=12.3 ms
    private static final Pattern DELAY_PATTERN = Pattern.compile("time[=<](\\d+\\.?\\d*)\\s*ms");
    // 匹配丢包统计：3 packets transmitted, 3 received, 0% packet loss
    private static final Pattern LOSS_PATTERN = Pattern.compile("(\\d+)\\s+packets transmitted.*?(\\d+)\\s+received.*?(\\d+\\.?\\d*)%\\s+packet loss");
    // 匹配汇总统计：rtt min/avg/max/mdev = 1.234/5.678/9.012/3.456 ms
    private static final Pattern RTT_PATTERN = Pattern.compile("=\\s*(\\d+\\.?\\d*)/(\\d+\\.?\\d*)/(\\d+\\.?\\d*)/(\\d+\\.?\\d*)\\s*ms");

    /**
     * 解析ping命令输出
     *
     * @param pingOutput ping命令的完整输出文本
     * @return 解析后的分析结果
     */
    public static NetworkAnalysisResult analyze(String pingOutput) {
        NetworkAnalysisResult result = new NetworkAnalysisResult();
        if (pingOutput == null || pingOutput.isEmpty()) {
            return result;
        }

        // 解析每一行的延迟值
        List<Double> delays = new ArrayList<>();
        Matcher delayMatcher = DELAY_PATTERN.matcher(pingOutput);
        while (delayMatcher.find()) {
            try {
                delays.add(Double.parseDouble(delayMatcher.group(1)));
            } catch (NumberFormatException ignored) {
            }
        }
        result.setDelays(delays);

        // 解析丢包统计
        Matcher lossMatcher = LOSS_PATTERN.matcher(pingOutput);
        if (lossMatcher.find()) {
            try {
                result.setPacketsTransmitted(Integer.parseInt(lossMatcher.group(1)));
                result.setPacketsReceived(Integer.parseInt(lossMatcher.group(2)));
                result.setPacketLoss(Double.parseDouble(lossMatcher.group(3)));
            } catch (NumberFormatException ignored) {
            }
        }

        // 解析汇总统计
        Matcher rttMatcher = RTT_PATTERN.matcher(pingOutput);
        if (rttMatcher.find()) {
            try {
                result.setMinDelay(Double.parseDouble(rttMatcher.group(1)));
                result.setAvgDelay(Double.parseDouble(rttMatcher.group(2)));
                result.setMaxDelay(Double.parseDouble(rttMatcher.group(3)));
                result.setJitter(Double.parseDouble(rttMatcher.group(4)));
            } catch (NumberFormatException ignored) {
            }
        } else {
            // 如果没有汇总行，从逐行数据计算
            result.calculateMetrics();
        }

        return result;
    }
}
