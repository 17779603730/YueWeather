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

import com.example.yueweather.*;
import com.example.yueweather.gson.Forecast;
import com.example.yueweather.gson.Weather;
import com.example.yueweather.util.Utility;

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
    }

    private void initData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);//查询本的缓存天气数据
        if (weatherString != null){//判断是否存在缓存数据，如果存在就直接解析
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);//把解析到Weather实体类的数据显示出来
        }
        else {//不存在就去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");
            mWeatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
    }

    /**
     * 根据天气id请求城市天气信息
     */
    private void requestWeather(String weatherId) {
        
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
            TextView mDataText = view.findViewById(R.id.date_text);
            TextView mInfoText = view.findViewById(R.id.info_text);
            TextView mMaxText = view.findViewById(R.id.max_text);
            TextView mMinText = view.findViewById(R.id.min_text);
            mDataText.setText(forecast.data);
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
