package com.example.yueweather.util;


import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 2018/12/11
 * 作者：GuoYongze
 * 从服务器上获取各省份的名字以及id
 */
public class HttpUtil {
    public static void sendOkHttpRequest(String address, Callback callback){
        OkHttpClient okHttpClient = new OkHttpClient();
        //发起一条HTTP请求
        Request request = new Request.Builder().url(address).build();
        //发送请求
        okHttpClient.newCall(request).enqueue(callback);
    }
}
