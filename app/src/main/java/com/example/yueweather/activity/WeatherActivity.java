package com.example.yueweather.activity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yueweather.*;
import com.example.yueweather.gson.Forecast;
import com.example.yueweather.gson.Weather;
import com.example.yueweather.util.HttpUtil;
import com.example.yueweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    /**
     * 2018/12/13
     * 作者：GuoYongze
     * 天气页面
     */
    private ScrollView mWeatherLayout;//activity_weather.xml
    private TextView mTitleCity;//title.xml
    private TextView mTitleUpdateTime;//title.xml
    private TextView mDegreeText;//now.xml
    private TextView mWeatherInfoText;//now.xml
    private LinearLayout mForecastLayout;//forecast_layout.xml
    private TextView mAqiText;//aqi.xml
    private TextView mPm25Text;//aqi.xml
    private TextView mComfortText;//suggestion.xml
    private TextView mCarCrashText;//suggestion.xml
    private TextView mSportText;//suggestion.xml
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        initWidget();//初始化控件
        initData();//本地缓存读取
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);//从SharedPreferences读取本地的天气数据
        if (weatherString != null){//如果存在就直接解析
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);//把解析到Weather实体类的数据显示出来
        }
        else {//不存在就去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");
            mWeatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
    }

    private void initData() {
        /**
         * 从本地查询已缓存的天气数据
         */

    }

    /**
     * 从服务器中根据天气id请求城市天气信息
     */
    private void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +weatherId +"&key=a3da6e2f53f24990b7f3998eb3c790c2";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {//发出请求

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();//获取服务器返回的数据
                final Weather weather = Utility.handleWeatherResponse(responseText);//解析到Weather类，并在这里转换成Weather对象
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)){//如果天气请求成功
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();//获取SharedPreferences.Editor实例
                            editor.putString("weather",responseText);//把服务器端的天气数据存储到本地中
                            editor.apply();
                            showWeatherInfo(weather);
                        }
                        else {
                            Toast.makeText(WeatherActivity.this, "查询天气失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "查询天气失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
    }

    /**
     * 处理并展示Weather实体类的信息
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime;
        String degree = weather.now.Tmperature + "℃";
        String weatherInfo = weather.now.more.info;
        mTitleCity.setText(cityName);
        mTitleUpdateTime.setText(updateTime);
        mDegreeText.setText(degree);
        mWeatherInfoText.setText(weatherInfo);
        mForecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList){//遍历数组，取出未来的天气情况的数据
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    mForecastLayout,false);//找到未来天气情况的子项布局，父容器/父布局
            TextView mDataText = view.findViewById(R.id.date_text);//forecast_item.xml
            TextView mInfoText = view.findViewById(R.id.info_text);//forecast_item.xml
            TextView mMaxText = view.findViewById(R.id.max_text);//forecast_item.xml
            TextView mMinText = view.findViewById(R.id.min_text);//forecast_item.xml
            mDataText.setText(forecast.date);
            mInfoText.setText(forecast.more.info);
            mMaxText.setText(forecast.tmperature.max);
            mMinText.setText(forecast.tmperature.min);
            mForecastLayout.addView(view);
        }
        /**
         * 判断Weather实体类中的Aqi是否存在
         */
        if (weather.aqi != null){
            mAqiText.setText(weather.aqi.city.aqi);
            mPm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动指数：" + weather.suggestion.sport.info;
        mComfortText.setText(comfort);
        mCarCrashText.setText(carWash);
        mSportText.setText(sport);
        mWeatherLayout.setVisibility(View.VISIBLE);//全部信息接收完毕，显示界面
    }

    private void initWidget() {
        mWeatherLayout = findViewById(R.id.weather_layout);
        mTitleCity = findViewById(R.id.title_city);
        mTitleUpdateTime = findViewById(R.id.title_update_time);
        mDegreeText = findViewById(R.id.degree_text);
        mWeatherInfoText = findViewById(R.id.weather_info_text);
        mForecastLayout = findViewById(R.id.forecast_layout);
        mAqiText = findViewById(R.id.aqi_text);
        mPm25Text = findViewById(R.id.pm25_text);
        mComfortText = findViewById(R.id.comfort_text);
        mCarCrashText = findViewById(R.id.car_wash_text);
        mSportText = findViewById(R.id.sport_text);
    }
}
