package com.dawn.libhttp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dawn.http.HttpUtil;
import com.dawn.http.http.entity.HttpConfig;
import com.dawn.http.http.net.HTTPCaller;
import com.dawn.http.http.net.Header;
import com.dawn.http.http.net.RequestDataCallback;
import com.dawn.http.http.util.Util;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initHttp();
        post_ceshi();
    }

    public void getMachineMsg(View view){
        post_ceshi();
    }

    private void initHttp(){
        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setAgent(true);//有代理的情况能不能访问
        httpConfig.setDebug(true);//是否debug模式 如果是debug模式打印log
        httpConfig.setTagName("dawn");//打印log的tagname

        //可以添加一些公共字段 每个接口都会带上
        httpConfig.addCommonField("pf", "android");
        httpConfig.addCommonField("version_code", "1.0.1");
        httpConfig.addHeaderField("version", "7");

        HTTPCaller.getInstance().setHttpConfig(httpConfig);
    }

    private void getMsg(){
        Log.e("dawn", "getMsg: ");
        String url = "http://jargee.cn/photomachines/photomachines/EA5C80FD363900000000";
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", Util.getAuthorization("", ""));
        HttpUtil.getInstance().get(MachineMsgBean.class, url, headersMap, new RequestDataCallback<MachineMsgBean>() {
            @Override
            public void dataCallback(MachineMsgBean obj) {
                Log.e("dawn", "dataCallback: " + (obj == null ? "null" : obj.toString() ));
            }
        });
    }

    private void get_ceshi(){
        String url = "http://tt.jargee.cn/basic/cm/banner/E24FC559653600000000";
        Map<String, String> headers = new HashMap<>();
        headers.put("ce", "s");
        HttpUtil.getInstance().get(Object.class, url, headers, new RequestDataCallback<Object>() {
            @Override
            public void dataCallback(Object obj) {
                super.dataCallback(obj);
            }
        });
    }

    private void post_ceshi(){
        String url = "http://tt.jargee.cn/basic/factory/device/detection/data?sn=E24FC559653600000000";
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("self_test_num", "2");
        Map<String, String> headers = new HashMap<>();
        headers.put("ce", "s");
        HttpUtil.getInstance().post(Object.class, url, headers, paramsMap, new RequestDataCallback<Object>() {
            @Override
            public void dataCallback(Object obj) {
                super.dataCallback(obj);
            }
        });

    }


}