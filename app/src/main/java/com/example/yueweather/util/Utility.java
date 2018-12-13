package com.example.yueweather.util;

import android.text.TextUtils;

import com.example.yueweather.db.City;
import com.example.yueweather.db.County;
import com.example.yueweather.db.Province;
import com.example.yueweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
/**
 * 2018/12/11
 * 作者：GuoYongze
 */
public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvince = new JSONArray(response);
                for (int i=0;i<allProvince.length();i++){
                    JSONObject provinceJSONObject = allProvince.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceJSONObject.getString("name"));
                    province.setProvinceCode(provinceJSONObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCitys = new JSONArray(response);
                for (int i=0;i<allCitys.length();i++){
                    JSONObject citysJSONObject = allCitys.getJSONObject(i);
                    City city = new City();
                    city.setCityName(citysJSONObject.getString("name"));
                    city.setCityCode(citysJSONObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties = new JSONArray(response);//将服务器返回的数据传入到JSONArray对象
                for (int i=0;i<allCounties.length();i++){//循环遍历之后取出的每一个元素都是一个JSONObject对象
                    JSONObject allCountiesJSONObject = allCounties.getJSONObject(i);//解析JSONObject对象,
                    County county = new County();//创建County对象,将解析出来的数据设置到County实体类中
                    county.setCountyName(allCountiesJSONObject.getString("name"));
                    county.setWeatherId(allCountiesJSONObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的JSON数据解析成Weather实体类
     * @param response
     * @return
     */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");//HeWeather是天气数据的JSON格式的数组名
            String weatherContent = jsonArray.getJSONObject(0).toString();//每一个JSON数据就是一个JSONObject对象;解析JSONObject对象
            return new Gson().fromJson(weatherContent,Weather.class);//解析到Weather类
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
