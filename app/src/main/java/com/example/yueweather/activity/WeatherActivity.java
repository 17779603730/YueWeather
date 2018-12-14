package com.example.yueweather.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yueweather.*;
import com.example.yueweather.gson.Forecast;
import com.example.yueweather.gson.Weather;
import com.example.yueweather.service.AutoUpdateService;
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
    private ImageView mBingPicImg;//activity_weather.xml
    public SwipeRefreshLayout mSwipeRefreshLayout;//activity_weather.xml
    private String mWeatherId;//记录天气id
    public DrawerLayout mDrawerLayout;
    private Button mNavButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        initWidget();//初始化控件
        initData();//本地缓存读取
        initTitle();//将状态栏和图片合并
        initNavButton();//侧边栏监听器
    }

    private void initNavButton() {
        mNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);//打开侧边栏
            }
        });
    }

    private void initTitle() {
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();//拿到当前活动的DecorView
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//改变系统UI的显示；这里调用这两个参数表示活动的布局会显示在状态栏上
            getWindow().setStatusBarColor(Color.TRANSPARENT);//将状态栏设置成透明色
        }
    }

    /**
     * 从本地查询已缓存的数据
     */
    public void initData() {
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorBlack);//刷新进度条
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);//从SharedPreferences读取本地的天气数据
        if (weatherString != null){//如果存在就直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);//把解析到Weather实体类的数据显示出来
        }
        else {//不存在就去服务器查询天气数据
            String weatherId = getIntent().getStringExtra("weather_id");
            mWeatherId = getIntent().getStringExtra("weather_id");
            mWeatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
            requestWeather(mWeatherId);
        }
        /**
         * 下拉刷新的监听器
         */
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);//下拉刷新的同时请求天气信息
                Toast.makeText(WeatherActivity.this, "天气已刷新！", Toast.LENGTH_SHORT).show();
            }
        });
        String bingPic = prefs.getString("bing_pic", null);//才能够本地读取缓存图片
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(mBingPicImg);//通过Glie加载这张图片
        }
        else {
            loadBingPic();
        }
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();//在WeatherActivity
                editor.putString("bing_pic",bingPic);//存储图片
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(mBingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    /**
     * 从服务器中根据天气id请求城市天气信息
     */
    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +weatherId
                +"&key=a3da6e2f53f24990b7f3998eb3c790c2";
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
                            /**
                             * 在这里添加服务，一旦获取了这个城市的天气，后台就会不定时更新天气
                             */

                        }
                        else {
                            Toast.makeText(WeatherActivity.this, "查询天气失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                        }
                        mSwipeRefreshLayout.setRefreshing(false);//setRefreshing设置刷新事件结束，并隐藏进度条
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "查询天气失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);//setRefreshing设置刷新事件结束，并隐藏进度条
                    }
                });
            }

        });
        loadBingPic();//每次请求天气的同时刷新背景
    }

    /**
     * 处理并展示Weather实体类的信息
     */
    public void showWeatherInfo(Weather weather) {
        if (weather != null && "ok".equals(weather.status)){
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
            if (weather.suggestion != null){
                String comfort = "舒适度：" + weather.suggestion.comfort.info;
                String carWash = "洗车指数：" + weather.suggestion.carWash.info;
                String sport = "运动指数：" + weather.suggestion.sport.info;
                mComfortText.setText(comfort);
                mCarCrashText.setText(carWash);
                mSportText.setText(sport);
            }
            mWeatherLayout.setVisibility(View.VISIBLE);//全部信息接收完毕，显示界面
            Intent intent = new Intent(this,AutoUpdateService.class);
            startService(intent);
        }


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
        mBingPicImg = findViewById(R.id.bing_pic_img);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavButton = findViewById(R.id.nav_button);
    }
}
