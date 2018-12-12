package com.example.yueweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 2018/12/12
 * 作者：GuoYongze
 * 基本信息的实体类
 */
public class Basic {

    @SerializedName("city")//把cityName伪装成city
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName("loc")//loc是更新时间
        public String updateTime;
    }
}
