package com.example.yueweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 2018/12/12
 * 作者：GuoYongze
 * 合并天气信息的实体类
 */
public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;//存储未来几天天气情况

}
