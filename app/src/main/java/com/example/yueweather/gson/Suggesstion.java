package com.example.yueweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 2018/12/12
 * 作者：GuoYongze
 * 生活建议的实体类
 */
public class Suggesstion {

    @SerializedName("comf")
    public  Comfort com;
    public class Comfort{
        @SerializedName("txt")
        public String info;
    }

    @SerializedName("cw")
    public CarWash carWash;
    public class CarWash{
        @SerializedName("txt")
        public String info;
    }

    public Sport sport;
    public class Sport{
        @SerializedName("txt")
        public String info;
    }
}
