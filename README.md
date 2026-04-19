# lib-network

基于 OkHttp3 封装的 Android HTTP 请求库，提供简洁的 API 进行网络请求，支持 GET / POST / PUT / DELETE / PATCH、文件上传下载、进度回调、同步/异步请求等功能。

## 依赖

```groovy
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
implementation 'com.google.code.gson:gson:2.10.1'
```

- **minSdk**: 28
- **compileSdk**: 34
- **Java**: 1.8
- **Gradle**: 7.6
- **AGP**: 7.4.2

## 项目结构

```
com.dawn.http
├── HttpUtil                          // 对外统一入口（门面类）
└── http
    ├── entity
    │   └── HttpConfig                // HTTP 配置类（超时、代理、公共参数、请求头等）
    ├── net
    │   ├── HTTPCaller                // 核心 HTTP 执行引擎（单例）
    │   ├── HttpResponseHandler       // OkHttp Callback 抽象实现
    │   ├── RequestDataCallback<T>    // 业务层请求回调（泛型）
    │   ├── NameValuePair             // 键值对（支持标记文件类型）
    │   └── Header                    // 请求头键值对
    ├── ui
    │   ├── ProgressUIListener        // 进度回调（自动切换到主线程）
    │   ├── ProgressListener          // 进度回调基类（含速度计算、去重）
    │   ├── ProgressHelper            // 进度包装工厂
    │   ├── ProgressRequestBody       // 带进度的请求体
    │   ├── ProgressResponseBody      // 带进度的响应体
    │   ├── ProgressOutputStream      // 带进度的输出流
    │   ├── ProgressInputStream       // 带进度的输入流
    │   └── ProgressCallback          // 进度回调接口
    └── util
        └── Util                      // URL 拼接、文件名解析等工具
```

## 初始化

在 `Application.onCreate()` 中完成初始化：

```java
HttpConfig httpConfig = new HttpConfig();
httpConfig.setAgent(true);          // 是否允许代理访问（false=有代理时阻止请求）
httpConfig.setDebug(true);          // 开启日志
httpConfig.setTagName("HttpLib");   // 日志 Tag
httpConfig.setUserAgent("MyApp/1.0");                    // 自定义 User-Agent
httpConfig.setConnectTimeout(10);   // 连接超时（秒），默认 10
httpConfig.setWriteTimeout(10);     // 写入超时（秒），默认 10
httpConfig.setReadTimeout(30);      // 读取超时（秒），默认 30

// 添加公共参数（每个请求自动附带）
httpConfig.addCommonField("pf", "android");
httpConfig.addCommonField("version_code", "100");

// 添加公共请求头
httpConfig.addHeaderField("Authorization", "Bearer xxx");

HttpUtil.getInstance().init(httpConfig);
```

## HttpConfig 配置类

| 方法 | 说明 | 默认值 |
|-----|------|-------|
| `setDebug(boolean)` | 是否开启日志 | `true` |
| `setTagName(String)` | 日志 Tag | `"Http"` |
| `setUserAgent(String)` | User-Agent 请求头 | `""` |
| `setAgent(boolean)` | 有代理时是否允许访问 | `true` |
| `setConnectTimeout(int)` | 连接超时（秒） | `10` |
| `setWriteTimeout(int)` | 写入超时（秒） | `10` |
| `setReadTimeout(int)` | 读取超时（秒） | `30` |
| `addCommonField(key, value)` | 添加公共参数（GET 拼接 URL，POST 加入表单） | - |
| `updateCommonField(key, value)` | 更新公共参数（不存在则添加） | - |
| `removeCommonField(key)` | 删除公共参数 | - |
| `clearCommonField()` | 清空所有公共参数 | - |
| `addHeaderField(key, value)` | 添加公共请求头 | - |
| `updateHeaderField(key, value)` | 更新公共请求头（不存在则添加） | - |
| `removeHeaderField(key)` | 删除公共请求头 | - |
| `clearHeaderField()` | 清空所有公共请求头 | - |

## RequestDataCallback 回调

```java
public abstract class RequestDataCallback<T> {
    // 返回 JSON 解析后的对象
    public void dataCallback(T obj) {}

    // 返回 HTTP 状态码 + JSON 对象
    public void dataCallback(int status, T obj) {}

    // 返回 HTTP 状态码 + JSON 对象 + 原始字节数据
    public void dataCallback(int status, T obj, byte[] body) {}

    // 请求失败回调（网络异常、JSON 解析失败、代理阻止等）
    public void onError(int status, String errorMsg) {}
}
```

> 回调链路：`dataCallback(status, obj, body)` → `dataCallback(status, obj)` → `dataCallback(obj)`，重写任意一级即可。所有回调均在主线程执行。

## GET 请求

### 异步 GET

```java
HttpUtil.getInstance().get(UserInfo.class, url, new RequestDataCallback<UserInfo>() {
    @Override
    public void dataCallback(UserInfo obj) {
        // 请求成功，obj 为自动解析的对象
    }

    @Override
    public void onError(int status, String errorMsg) {
        // 请求失败
    }
});
```

### 带请求头的 GET

```java
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", Util.getAuthorization("username", "password"));

HttpUtil.getInstance().get(UserInfo.class, url, headers, new RequestDataCallback<UserInfo>() {
    @Override
    public void dataCallback(UserInfo obj) {
    }
});
```

### 同步 GET

> 注意：同步请求不能在主线程调用。

```java
UserInfo result = HttpUtil.getInstance().getSync(UserInfo.class, url);
```

## POST 请求

### 表单 POST

```java
Map<String, String> params = new HashMap<>();
params.put("username", "test");
params.put("password", "123456");

HttpUtil.getInstance().post(Result.class, url, params, new RequestDataCallback<Result>() {
    @Override
    public void dataCallback(Result obj) {
    }
});
```

### 带请求头的表单 POST

```java
Map<String, String> headers = new HashMap<>();
headers.put("Token", "xxx");

Map<String, String> params = new HashMap<>();
params.put("key", "value");

HttpUtil.getInstance().post(Result.class, url, headers, params, new RequestDataCallback<Result>() {
    @Override
    public void dataCallback(Result obj) {
    }
});
```

### JSON POST

```java
String json = "{\"name\":\"test\",\"age\":18}";

HttpUtil.getInstance().postJson(Result.class, url, json, new RequestDataCallback<Result>() {
    @Override
    public void dataCallback(Result obj) {
    }
});
```

### 同步 POST（表单）

```java
Result result = HttpUtil.getInstance().postSync(Result.class, url, params);
```

### 同步 POST（返回原始字节）

```java
byte[] raw = HttpUtil.getInstance().postSyncRaw(url, params);
```

### 同步 JSON POST

```java
Result result = HttpUtil.getInstance().postSyncJson(Result.class, url, json);
// 带请求头
Result result = HttpUtil.getInstance().postSyncJson(Result.class, url, headers, json);
```

### 同步 JSON POST（返回原始字节）

```java
byte[] raw = HttpUtil.getInstance().postSyncJsonRaw(url, json);
```

## 自定义 Content-Type 请求

适用于 XML、Protobuf、纯文本等非 JSON/表单格式的服务端：

### XML POST

```java
String xml = "<request><name>test</name></request>";

HttpUtil.getInstance().postBody(Result.class, url, headers, xml,
    "application/xml; charset=utf-8",
    new RequestDataCallback<Result>() {
        @Override
        public void dataCallback(Result obj) {
        }
    });
```

### 纯文本 POST

```java
HttpUtil.getInstance().postBody(Result.class, url, null, "hello",
    "text/plain; charset=utf-8", callback);
```

### 二进制数据 POST（如 Protobuf）

```java
byte[] protobufData = ...; // 序列化后的二进制数据

HttpUtil.getInstance().postBody(Result.class, url, headers, protobufData,
    "application/x-protobuf", callback);
```

### 同步自定义 Content-Type

```java
// 返回解析对象
Result result = HttpUtil.getInstance().postBodySync(Result.class, url, headers, xml, "application/xml; charset=utf-8");

// 返回原始字节
byte[] raw = HttpUtil.getInstance().postBodySyncRaw(url, headers, xml, "application/xml; charset=utf-8");
```

> **POST 请求体格式汇总：**
>
> | 方法 | Content-Type | 说明 |
> |------|-------------|------|
> | `post()` | `application/x-www-form-urlencoded` | 表单键值对 |
> | `postJson()` | `application/json` | JSON 字符串 |
> | `postBody(String)` | 自定义 | 任意文本格式（XML / 纯文本等） |
> | `postBody(byte[])` | 自定义 | 任意二进制格式（Protobuf 等） |
> | `postFile()` | `multipart/form-data` | 文件上传 |

## PUT 请求

### Map 参数

```java
Map<String, Object> params = new HashMap<>();
params.put("name", "newName");

HttpUtil.getInstance().put(Result.class, url, headers, params, callback);
```

### JSON 参数

```java
HttpUtil.getInstance().putJson(Result.class, url, headers, json, callback);
```

## DELETE 请求

### 无请求体

```java
HttpUtil.getInstance().delete(Result.class, url, headers, callback);
```

### JSON 请求体

```java
HttpUtil.getInstance().deleteJson(Result.class, url, headers, json, callback);
```

## PATCH 请求

### Map 参数

```java
HttpUtil.getInstance().patch(Result.class, url, headers, params, callback);
```

### JSON 参数

```java
HttpUtil.getInstance().patchJson(Result.class, url, headers, json, callback);
```

## 文件上传

### 通过 NameValuePair 列表上传

使用 `NameValuePair` 同时传递表单字段和文件字段：

```java
List<NameValuePair> form = new ArrayList<>();
form.add(new NameValuePair("description", "avatar"));           // 普通字段
form.add(new NameValuePair("file", "/sdcard/photo.jpg", true)); // 文件字段（第三个参数为 true）

HttpUtil.getInstance().postFile(Result.class, url, null, form,
    new RequestDataCallback<Result>() {
        @Override
        public void dataCallback(Result obj) {
        }
    });
```

### 带进度回调的文件上传

```java
HttpUtil.getInstance().postFile(Result.class, url, null, form, callback,
    new ProgressUIListener() {
        @Override
        public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
            // percent: 0.0 ~ 1.0，speed: bytes/ms
            int progress = (int) (percent * 100);
            progressBar.setProgress(progress);
        }

        @Override
        public void onUIProgressStart(long totalBytes) {
            // 上传开始
        }

        @Override
        public void onUIProgressFinish() {
            // 上传完成
        }
    });
```

### 通过字节数组上传

```java
byte[] fileContent = ...; // 文件字节内容
HttpUtil.getInstance().postFile(Result.class, url, null,
    "file", "photo.jpg", fileContent, callback);
```

## 文件下载

```java
HttpUtil.getInstance().downloadFile(url, "/sdcard/Download/file.zip",
    new ProgressUIListener() {
        @Override
        public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
            int progress = (int) (percent * 100);
            progressBar.setProgress(progress);
        }

        @Override
        public void onUIProgressFinish() {
            // 下载完成
        }
    });
```

### 带请求头的下载

```java
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer xxx");

HttpUtil.getInstance().downloadFile(url, savePath, headers, progressListener);
```

## 请求管理

```java
// 取消指定 URL 的请求
HttpUtil.getInstance().cancelRequest(url);

// 取消所有请求
HttpUtil.getInstance().cancelAllRequests();
```

> 同一 URL 的请求默认会自动取消前一个（autoCancel），避免重复请求。

## 公共参数 / 请求头动态管理

初始化后仍可动态增删改公共字段：

```java
// 公共参数（拼接到 GET URL 或 POST 表单）
HttpUtil.getInstance().addCommonField("token", "abc123");
HttpUtil.getInstance().updateCommonField("token", "newToken");
HttpUtil.getInstance().removeCommonField("token");

// 公共请求头
HttpUtil.getInstance().addHeaderField("X-Custom", "value");
HttpUtil.getInstance().updateHeaderField("X-Custom", "newValue");
HttpUtil.getInstance().removeHeaderField("X-Custom");
```

## ProgressUIListener 进度回调

继承 `ProgressUIListener` 实现进度监听，所有回调自动切换到主线程，可直接更新 UI：

| 回调方法 | 说明 |
|---------|------|
| `onUIProgressStart(long totalBytes)` | 进度开始，`totalBytes` 为总大小 |
| `onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed)` | 进度更新，`speed` 单位 bytes/ms |
| `onUIProgressFinish()` | 进度结束 |

> 当总大小无法获取时，所有参数值为 `-1`。回调最小间隔 100ms，避免频繁刷新。

## 混淆配置

```proguard
-keep class com.dawn.http.** { *; }
-dontwarn com.dawn.http.**

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
```

## License

```
MIT License
```