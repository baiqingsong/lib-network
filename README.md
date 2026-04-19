# lib-network

Android 网络库，包含 HTTP 请求（基于 OkHttp3）和 WiFi 管理、网络工具等功能。

## 引用

Step 1. Add the JitPack repository to your build file

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

```groovy
dependencies {
    implementation 'com.github.baiqingsong:lib-network:Tag'
}
```

## 权限

```xml
<!-- HTTP 请求 -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- WiFi 管理 -->
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />

<!-- 网络状态 -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
```

## 初始化

在 `Application.onCreate()` 中完成 HTTP 初始化：

```java
HttpConfig httpConfig = new HttpConfig();
httpConfig.setAgent(true);
httpConfig.setDebug(true);
httpConfig.setTagName("HttpLib");
httpConfig.setUserAgent("MyApp/1.0");
httpConfig.setConnectTimeout(10);
httpConfig.setWriteTimeout(10);
httpConfig.setReadTimeout(30);
httpConfig.addCommonField("pf", "android");
httpConfig.addHeaderField("Authorization", "Bearer xxx");
HttpUtil.getInstance().init(httpConfig);
```

## 类说明

### `com.dawn.http.HttpUtil` HTTP 请求工具类

| 方法 | 说明 |
|------|------|
| `getInstance()` | 获取单例实例 |
| `init(HttpConfig)` | 初始化 HTTP 配置 |
| `getHttpConfig()` | 获取当前 HTTP 配置 |
| `get(Class, String, RequestDataCallback)` | 异步 GET 请求 |
| `get(Class, String, Map, RequestDataCallback)` | 带请求头的异步 GET 请求 |
| `getSync(Class, String)` | 同步 GET 请求 |
| `getSync(Class, String, Map)` | 带请求头的同步 GET 请求 |
| `post(Class, String, Map, RequestDataCallback)` | 异步 POST 表单请求 |
| `post(Class, String, Map, Map, RequestDataCallback)` | 带请求头的异步 POST 表单请求 |
| `postJson(Class, String, String, RequestDataCallback)` | 异步 POST JSON 请求 |
| `postJson(Class, String, Map, String, RequestDataCallback)` | 带请求头的异步 POST JSON 请求 |
| `postSync(Class, String, Map)` | 同步 POST 表单请求 |
| `postSync(Class, String, Map, Map)` | 带请求头的同步 POST 表单请求 |
| `postSyncJson(Class, String, String)` | 同步 POST JSON 请求 |
| `postSyncJson(Class, String, Map, String)` | 带请求头的同步 POST JSON 请求 |
| `postSyncJsonRaw(String, String)` | 同步 POST JSON 请求（返回原始字节） |
| `postSyncJsonRaw(String, Map, String)` | 带请求头的同步 POST JSON 请求（返回原始字节） |
| `postSyncRaw(String, Map)` | 同步 POST 表单请求（返回原始字节） |
| `postSyncRaw(String, Map, Map)` | 带请求头的同步 POST 表单请求（返回原始字节） |
| `postBody(Class, String, Map, String, String, RequestDataCallback)` | 自定义 Content-Type 的异步 POST 请求（字符串 body） |
| `postBody(Class, String, Map, byte[], String, RequestDataCallback)` | 自定义 Content-Type 的异步 POST 请求（字节数组 body） |
| `postBodySync(Class, String, Map, String, String)` | 自定义 Content-Type 的同步 POST 请求 |
| `postBodySyncRaw(String, Map, String, String)` | 自定义 Content-Type 的同步 POST 请求（返回原始字节） |
| `put(Class, String, Map, Map, RequestDataCallback)` | 异步 PUT 请求（Map 参数） |
| `putJson(Class, String, Map, String, RequestDataCallback)` | 异步 PUT 请求（JSON 参数） |
| `delete(Class, String, Map, RequestDataCallback)` | 异步 DELETE 请求（无 body） |
| `deleteJson(Class, String, Map, String, RequestDataCallback)` | 异步 DELETE 请求（JSON body） |
| `patch(Class, String, Map, Map, RequestDataCallback)` | 异步 PATCH 请求（Map 参数） |
| `patchJson(Class, String, Map, String, RequestDataCallback)` | 异步 PATCH 请求（JSON 参数） |
| `postFile(Class, String, Map, List, RequestDataCallback)` | 文件上传 |
| `postFile(Class, String, Map, List, RequestDataCallback, ProgressUIListener)` | 文件上传（带进度回调） |
| `postFile(Class, String, Map, String, String, byte[], RequestDataCallback)` | 文件上传（字节数组方式） |
| `postFile(Class, String, Map, String, String, byte[], RequestDataCallback, ProgressUIListener)` | 文件上传（字节数组方式，带进度回调） |
| `downloadFile(String, String, Map, ProgressUIListener)` | 文件下载（带请求头） |
| `downloadFile(String, String, ProgressUIListener)` | 文件下载 |
| `cancelRequest(String)` | 取消指定 URL 的请求 |
| `cancelAllRequests()` | 取消所有请求 |
| `addCommonField(String, String)` | 添加公共参数 |
| `updateCommonField(String, String)` | 更新公共参数 |
| `removeCommonField(String)` | 删除公共参数 |
| `addHeaderField(String, String)` | 添加公共请求头 |
| `updateHeaderField(String, String)` | 更新公共请求头 |
| `removeHeaderField(String)` | 删除公共请求头 |

### `com.dawn.http.http.entity.HttpConfig` HTTP 配置类

| 方法 | 说明 |
|------|------|
| `setDebug(boolean)` | 设置是否开启日志，默认 `true` |
| `isDebug()` | 获取是否开启日志 |
| `setTagName(String)` | 设置日志 Tag，默认 `"Http"` |
| `getTagName()` | 获取日志 Tag |
| `setUserAgent(String)` | 设置 User-Agent 请求头 |
| `getUserAgent()` | 获取 User-Agent |
| `setAgent(boolean)` | 设置有代理时是否允许访问，默认 `true` |
| `isAgent()` | 获取代理访问设置 |
| `setConnectTimeout(int)` | 设置连接超时（秒），默认 10 |
| `setWriteTimeout(int)` | 设置写入超时（秒），默认 10 |
| `setReadTimeout(int)` | 设置读取超时（秒），默认 30 |
| `getConnectTimeout()` | 获取连接超时 |
| `getWriteTimeout()` | 获取写入超时 |
| `getReadTimeout()` | 获取读取超时 |
| `addCommonField(String, String)` | 添加公共参数（GET 拼接 URL，POST 加入表单） |
| `updateCommonField(String, String)` | 更新公共参数（不存在则添加） |
| `removeCommonField(String)` | 删除公共参数 |
| `clearCommonField()` | 清空所有公共参数 |
| `addHeaderField(String, String)` | 添加公共请求头 |
| `updateHeaderField(String, String)` | 更新公共请求头（不存在则添加） |
| `removeHeaderField(String)` | 删除公共请求头 |
| `clearHeaderField()` | 清空所有公共请求头 |

### `com.dawn.http.http.net.RequestDataCallback<T>` HTTP 请求回调类

| 方法 | 说明 |
|------|------|
| `dataCallback(T)` | 请求成功回调，返回 JSON 解析后的对象 |
| `dataCallback(int, T)` | 请求成功回调，返回 HTTP 状态码 + JSON 对象 |
| `dataCallback(int, T, byte[])` | 请求成功回调，返回 HTTP 状态码 + JSON 对象 + 原始字节 |
| `onError(int, String)` | 请求失败回调（网络异常、JSON 解析失败、代理阻止等） |

> 回调链路：`dataCallback(status, obj, body)` → `dataCallback(status, obj)` → `dataCallback(obj)`，重写任意一级即可。所有回调均在主线程执行。

### `com.dawn.http.http.net.NameValuePair` 键值对类

| 方法 | 说明 |
|------|------|
| `NameValuePair(String, String)` | 构造普通键值对 |
| `NameValuePair(String, String, boolean)` | 构造键值对，第三个参数标记是否为文件 |
| `getName()` | 获取键名 |
| `setName(String)` | 设置键名 |
| `getValue()` | 获取键值 |
| `setValue(String)` | 设置键值 |
| `isFile()` | 是否为文件字段 |
| `setFile(boolean)` | 设置是否为文件字段 |

### `com.dawn.http.http.net.Header` 请求头键值对类

| 方法 | 说明 |
|------|------|
| `Header(String, String)` | 构造请求头键值对 |

> 继承自 `NameValuePair`。

### `com.dawn.http.http.ui.ProgressUIListener` 进度回调类

| 方法 | 说明 |
|------|------|
| `onUIProgressStart(long)` | 进度开始，参数为总大小（字节） |
| `onUIProgressChanged(long, long, float, float)` | 进度更新（已传输字节、总字节、百分比、速度 bytes/ms） |
| `onUIProgressFinish()` | 进度完成 |

> 所有回调自动切换到主线程，可直接更新 UI。当总大小无法获取时，所有参数值为 `-1`。回调最小间隔 100ms。

### `com.dawn.http.http.ui.ProgressHelper` 进度包装工具类

| 方法 | 说明 |
|------|------|
| `withProgress(RequestBody, ProgressListener)` | 包装请求体为带进度的请求体 |
| `withProgress(ResponseBody, ProgressListener)` | 包装响应体为带进度的响应体 |

### `com.dawn.http.http.util.Util` HTTP 工具类

| 方法 | 说明 |
|------|------|
| `getFileName(String)` | 从文件路径中获取文件名 |
| `getMosaicParameter(String, List)` | 拼接公共参数到 URL |
| `getAuthorization(String, String)` | 生成 Basic Auth 认证字符串 |

---

### `com.dawn.http.wifi.WifiFactory` WiFi 操作工厂类

| 方法 | 说明 |
|------|------|
| `getInstance(Context)` | 获取单例实例 |
| `setListener(OnWifiListener)` | 设置 WiFi 状态监听器 |
| `openWifi()` | 打开 WiFi 并注册广播接收器 |
| `closeWifiReceiver()` | 关闭 WiFi 广播接收器 |
| `getConnectWifiSsid()` | 获取当前连接的 WiFi SSID |
| `connectWifi(String, String)` | 连接 WiFi（SSID, 密码） |
| `disconnectWifi(String)` | 断开 WiFi |
| `clearWifiConfig()` | 清除所有 WiFi 配置 |
| `clearWifiConfig(String)` | 清除指定 WiFi 配置 |
| `isWifiEnabled()` | WiFi 是否已打开 |
| `isWifiConnected()` | 是否已连接 WiFi |
| `getWifiSignalStrength()` | 获取 WiFi 信号强度（RSSI） |
| `getWifiSignalLevel(int)` | 获取 WiFi 信号等级 |
| `getWifiIpAddress()` | 获取 WiFi IP 地址 |

WiFi 使用示例：

```java
WifiFactory wifiFactory = WifiFactory.getInstance(context);
wifiFactory.setListener(new OnWifiListener() {
    @Override
    public void refreshWifiList(List<String> wifiList) {
        // WiFi 列表刷新
    }

    @Override
    public void wifiConnectSuccess(String ssid) {
        // WiFi 连接成功
    }

    @Override
    public void wifiDisconnect() {
        // WiFi 断开
    }
});
wifiFactory.openWifi();
wifiFactory.connectWifi("SSID", "password");
```

### `com.dawn.http.wifi.OnWifiListener` WiFi 状态监听接口

| 方法 | 说明 |
|------|------|
| `refreshWifiList(List<String>)` | WiFi 列表刷新回调 |
| `wifiConnectSuccess(String)` | WiFi 连接成功回调 |
| `wifiDisconnect()` | WiFi 断开回调 |

### `com.dawn.http.wifi.LWifiBroadcastReceiver` WiFi 广播接收器

| 方法 | 说明 |
|------|------|
| `onWifiEnabled()` | WiFi 已打开回调 |
| `onWifiDisabled()` | WiFi 已关闭回调 |
| `onScanResultsAvailable(List<ScanResult>)` | WiFi 扫描结果回调 |
| `onWifiConnected(String)` | WiFi 已连接回调（返回 SSID） |
| `onWifiDisconnected()` | WiFi 已断开回调 |

> 抽象类，监听 `WIFI_STATE_CHANGED`、`SCAN_RESULTS_AVAILABLE`、`NETWORK_STATE_CHANGED` 广播。

### `com.dawn.http.wifi.LNetUtil` 网络工具类

| 方法 | 说明 |
|------|------|
| `getNetworkType(Context)` | 获取网络类型（ConnectivityManager.TYPE_*） |
| `isNetworkAvailable(Context)` | 网络是否可用 |
| `isNetworkConnected(Context)` | 网络是否已连接 |
| `isWiFi(Context)` | 是否是 WiFi 连接 |
| `isMobileNetwork(Context)` | 是否是移动网络 |
| `getNetworkTypeName(Context)` | 获取网络类型名称（wifi / 移动子类型名 / disconnect / unknown） |
| `openNetSetting(Activity)` | 打开网络设置界面 |
| `setWifiEnabled(Context, boolean)` | 设置 WiFi 开关状态 |
| `getWifiSignalStrength(Context)` | 获取 WiFi 信号强度（RSSI） |
| `getCurrentWifiName(Context)` | 获取当前连接的 WiFi 名称 |
| `getLocalIPAddress()` | 获取本机 IP 地址 |
| `ping(String, int, StringBuffer)` | Ping 测试（指定次数） |
| `simplePing(String)` | 简单 Ping 测试（1 次） |
| `getPingAverageMs(String, int)` | 获取 Ping 平均延迟（ms） |
| `isPortReachable(String, int, int)` | 检查指定端口是否可连接 |

### `com.dawn.http.wifi.LWifiUtil` WiFi Ping 工具类

| 方法 | 说明 |
|------|------|
| `isPingSuccess(String, int)` | Ping 检测网络是否正常 |
| `isPingSuccess(String)` | Ping 检测（默认 4 次） |
| `pingAnalyze(String, int)` | Ping 并获取网络质量分析结果 |

### `com.dawn.http.wifi.ping.PingAnalyzer` Ping 输出解析器

| 方法 | 说明 |
|------|------|
| `analyze(String)` | 解析 ping 命令输出，返回 `NetworkAnalysisResult` |

### `com.dawn.http.wifi.ping.NetworkAnalysisResult` 网络质量分析结果

| 方法 | 说明 |
|------|------|
| `calculateMetrics()` | 根据延迟数据计算统计指标 |
| `generateReport()` | 生成网络质量分析报告 |
| `getQualityRating()` | 获取网络质量评级（优 / 中 / 差） |
| `getDelays()` | 获取延迟列表 |
| `getPacketsTransmitted()` | 获取发送包数 |
| `getPacketsReceived()` | 获取接收包数 |
| `getPacketLoss()` | 获取丢包率 |
| `getAvgDelay()` | 获取平均延迟 |
| `getMinDelay()` | 获取最小延迟 |
| `getMaxDelay()` | 获取最大延迟 |
| `getJitter()` | 获取延迟波动（抖动） |