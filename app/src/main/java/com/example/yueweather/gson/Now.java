package com.example.yueweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 2018/12/12
 * 作者：GuoYongze
 * 实时天气情况的实体类，比如阵雨，小雨等
 */
public class Now {

    @SerializedName("tmp")
    public String Tmperature;
    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }
}
