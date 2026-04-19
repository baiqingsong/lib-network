# lib-network

基于 OkHttp3 封装的 Android HTTP 请求库，支持 GET / POST / PUT / DELETE / PATCH、文件上传下载、进度回调、同步/异步请求等功能。

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
<uses-permission android:name="android.permission.INTERNET" />
```

## 初始化

在 `Application.onCreate()` 中完成初始化：

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

### `com.dawn.http.HttpUtil` HTTP请求工具类

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
| `postSyncRaw(String, Map)` | 同步 POST 表单请求（返回原始字节） |
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

### `com.dawn.http.http.entity.HttpConfig` HTTP配置类

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
| `addCommonField(String, String)` | 添加公共参数（GET 拼接 URL，POST 加入表单） |
| `updateCommonField(String, String)` | 更新公共参数（不存在则添加） |
| `removeCommonField(String)` | 删除公共参数 |
| `clearCommonField()` | 清空所有公共参数 |
| `addHeaderField(String, String)` | 添加公共请求头 |
| `updateHeaderField(String, String)` | 更新公共请求头（不存在则添加） |
| `removeHeaderField(String)` | 删除公共请求头 |
| `clearHeaderField()` | 清空所有公共请求头 |

### `com.dawn.http.http.net.RequestDataCallback<T>` HTTP请求回调类

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

### `com.dawn.http.http.util.Util` 工具类

| 方法 | 说明 |
|------|------|
| `getFileName(String)` | 从文件路径中获取文件名 |
| `getMosaicParameter(String, List)` | 拼接公共参数到 URL |
| `getAuthorization(String, String)` | 生成 Basic Auth 认证字符串 |

```
MIT License
```