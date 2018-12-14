package com.example.yueweather.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.yueweather.R;
import com.example.yueweather.activity.WeatherActivity;
import com.example.yueweather.gson.Weather;
import com.example.yueweather.util.HttpUtil;
import com.example.yueweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    /**
     * 2018/12/14
     * 作者：GuoYongze
     */
    private Weather weather;
    private String weatherUrl;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        WeatherActivity activity = new WeatherActivity();
        activity.showWeatherInfo(weather);
        //新增---------------------------------------------
        //android8.1前台服务必须要有的
//        String CHANNEL_ONE_ID = "guo.fox.servicebestpractice";
//        String CHANNEL_ONE_NAME = "Channel One";
//        NotificationChannel notificationChannel = null;
//        //如果系统版本大于等于Android 8.0
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
//                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(R.color.colorPrimary);
//            notificationChannel.setShowBadge(true);
//            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
//            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            manager.createNotificationChannel(notificationChannel);
//        }
//--------------------------------------------------------新增
//        Intent intent = new Intent(this,AutoUpdateService.class);
//        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
//        Notification notification = new NotificationCompat.Builder(this)
//                .setContentText("YueWeather正在后台运行！")
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
//                .setContentIntent(pi)
//                .build();
//        startForeground(1,notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();//更新天气信息
        updateBingPic();//更新图片
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, i, 0);
        int time = 60*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + time;
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateBingPic() {
        final String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
    }

    private void updateWeather() {
        SharedPreferences prfs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prfs.getString("weather", null);
        if (weatherString != null){
            weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            weatherUrl = "http://guolin.tech/api/weather?cityid=" +weatherId
                    +"&key=a3da6e2f53f24990b7f3998eb3c790c2";
        }else {
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {//发出请求

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText = response.body().string();//获取服务器返回的数据
                    final Weather weather = Utility.handleWeatherResponse(responseText);//解析到Weather类，并在这里转换成Weather对象
                    if (weather != null && "ok".equals(weather.status)){//如果天气请求成功
                        SharedPreferences.Editor editor = PreferenceManager.
                                getDefaultSharedPreferences(AutoUpdateService.this).edit();//获取SharedPreferences.Editor实例
                        editor.putString("weather",responseText);//把服务器端的天气数据存储到本地中
                        editor.apply();
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                }

            });
        }
    }
}
