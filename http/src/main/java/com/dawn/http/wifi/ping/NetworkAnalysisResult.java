package com.dawn.http.wifi.ping;

import java.util.List;

/**
 * 网络质量分析结果
 * <p>
 * 包含Ping测试的延迟数据、丢包率、抖动等指标
 */
public class NetworkAnalysisResult {
    private List<Double> delays;
    private int packetsTransmitted;
    private int packetsReceived;
    private double packetLoss;
    private double avgDelay;
    private double minDelay;
    private double maxDelay;
    private double jitter;

    /**
     * 根据延迟数据计算统计指标
     */
    public void calculateMetrics() {
        if (delays == null || delays.isEmpty()) {
            return;
        }
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        for (double d : delays) {
            if (d < min) min = d;
            if (d > max) max = d;
            sum += d;
        }
        this.minDelay = min;
        this.maxDelay = max;
        this.avgDelay = sum / delays.size();

        // 计算延迟波动（标准差）
        double varianceSum = 0;
        for (double d : delays) {
            varianceSum += Math.pow(d - avgDelay, 2);
        }
        this.jitter = Math.sqrt(varianceSum / delays.size());
    }

    /**
     * 生成网络质量分析报告
     */
    public String generateReport() {
        return String.format(
                "====== 网络质量分析报告 ======\n" +
                        "延迟数据（ms）: %s\n" +
                        "发送/接收包: %d/%d\n" +
                        "丢包率: %.1f%%\n" +
                        "平均延迟: %.2f ms\n" +
                        "延迟波动: %.2f ms\n" +
                        "网络质量: %s\n" +
                        "============================",
                delays != null ? delays.toString() : "[]",
                packetsTransmitted,
                packetsReceived,
                packetLoss,
                avgDelay,
                jitter,
                getQualityRating()
        );
    }

    /**
     * 获取网络质量评级
     */
    public String getQualityRating() {
        if (packetLoss > 5) return "差（高丢包）";
        if (jitter > 20) return "中（延迟不稳定）";
        if (avgDelay > 100) return "中（高延迟）";
        return "优（低延迟、零丢包）";
    }

    // ==================== Getter / Setter ====================

    public List<Double> getDelays() {
        return delays;
    }

    public void setDelays(List<Double> delays) {
        this.delays = delays;
    }

    public int getPacketsTransmitted() {
        return packetsTransmitted;
    }

    public void setPacketsTransmitted(int packetsTransmitted) {
        this.packetsTransmitted = packetsTransmitted;
    }

    public int getPacketsReceived() {
        return packetsReceived;
    }

    public void setPacketsReceived(int packetsReceived) {
        this.packetsReceived = packetsReceived;
    }

    public double getPacketLoss() {
        return packetLoss;
    }

    public void setPacketLoss(double packetLoss) {
        this.packetLoss = packetLoss;
    }

    public double getAvgDelay() {
        return avgDelay;
    }

    public void setAvgDelay(double avgDelay) {
        this.avgDelay = avgDelay;
    }

    public double getMinDelay() {
        return minDelay;
    }

    public void setMinDelay(double minDelay) {
        this.minDelay = minDelay;
    }

    public double getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(double maxDelay) {
        this.maxDelay = maxDelay;
    }

    public double getJitter() {
        return jitter;
    }

    public void setJitter(double jitter) {
        this.jitter = jitter;
    }
}
