package com.dawn.http.http.net;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * HTTP响应处理器
 */
abstract class HttpResponseHandler implements Callback {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public HttpResponseHandler() {
    }

    @Override
    public void onFailure(Call call, IOException e) {
        String msg = e != null && e.getMessage() != null ? e.getMessage() : "unknown error";
        onFailure(-1, msg.getBytes(UTF_8));
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        try {
            int code = response.code();
            ResponseBody responseBody = response.body();
            byte[] body = responseBody != null ? responseBody.bytes() : new byte[0];
            if (code > 299) {
                onFailure(code, body);
            } else {
                Headers headers = response.headers();
                Header[] hs = new Header[headers.size()];
                for (int i = 0; i < headers.size(); i++) {
                    hs[i] = new Header(headers.name(i), headers.value(i));
                }
                onSuccess(code, hs, body);
            }
        } finally {
            response.close();
        }
    }

    public void onFailure(int status, byte[] data) {
    }

    public abstract void onSuccess(int statusCode, Header[] headers, byte[] responseBody);
}
