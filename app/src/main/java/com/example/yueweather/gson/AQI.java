package com.example.yueweather.gson;
/**
 * 2018/12/12
 * 作者：GuoYongze
 * 空气质量指数的实体类
 */
public class AQI {

    public AQIcity city;

    public class AQIcity{
        public String aqi;
        public String pm25;
    }
}
