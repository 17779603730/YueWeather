package com.example.yueweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.jar.Manifest;

/**
 * 2018/12/12
 * 作者：GuoYongze
 * 未来每一天天气信息的实体类
 */
public class Forecast {
    public String data;
    @SerializedName("cond")
    public More more;
    @SerializedName("tmp")
    public Tmperature tmperature;
    public class Tmperature{
        public String max;
        public String min;
    }
    public class More{
        @SerializedName("txt_d")
        public String info;
    }
}
