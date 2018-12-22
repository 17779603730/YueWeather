package com.example.yueweather.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yueweather.R;
import com.example.yueweather.activity.MainActivity;
import com.example.yueweather.activity.WeatherActivity;
import com.example.yueweather.db.City;
import com.example.yueweather.db.County;
import com.example.yueweather.db.Province;
import com.example.yueweather.util.HttpUtil;
import com.example.yueweather.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/***
 * 2018/12/11
 * 作者：GuoYongze
 * 遍历省市县的数据的碎片
 */
public class ChooseAreaFagment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView mTitleTextview;
    private Button mBackButton;
    private ListView mListView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    /**
     * 省列表
     */
    private List<Province> provinceList;
    /**
     * 市列表
     */
    private List<City> cityList;
    /**
     * 县列表
     */
    private List<County> countyList;
    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的城市
     */
    private City selectedCity;
    /**
     * 当前选中的级别
     */
    private int currentLevel;

    /**
     * 初始化布局文件和控件。
     * dataList 是ArrayList对象
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        mTitleTextview = view.findViewById(R.id.title_text);
        mBackButton = view.findViewById(R.id.back_button);
        mListView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        mListView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /**
                 * 判断选中的级别
                 * provinceList省列表
                 */
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }
                else if (currentLevel == LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }
                /**
                 * 如果到了最小级别地点，就跳转到天气界面
                 */
                else if (currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    /**
                     * 侧边栏判断与设置
                     * instanceof关键字判断一个对象是否属于某个类的实例
                     */
                    if (getActivity()instanceof MainActivity){//如果属于MainActivity那么处理逻辑不变
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);//获取天气的id
                        startActivity(intent);
                        getActivity().finish();
                    }
                    else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.mDrawerLayout.closeDrawers();//关闭侧边栏
                        activity.mSwipeRefreshLayout.setRefreshing(true);//显示下拉刷新进度条
                        activity.requestWeather(weatherId);//请求新城市的天气信息
                    }
                }
            }
        });
        /**
         * 通过级别判断调用哪个查询方法
         */
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                }
                else if (currentLevel ==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    /**
     * 查询省
     */
    private void queryProvinces() {

        mTitleTextview.setText("中国");
        mBackButton.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);//找到省份的实体类.
        if (provinceList.size() > 0 ){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }
        else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    /**
     * 查询市
     */
    private void queryCities() {

        mTitleTextview.setText(selectedProvince.getProvinceName());
        mBackButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceId = ?",
                String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
        else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address,"city");
        }
    }
    /**
     * 查询县
     */
    private void queryCounties() {

        mTitleTextview.setText(selectedCity.getCityName());
        mBackButton.setVisibility(View.VISIBLE);
        //先从数据库中查询
        countyList = LitePal.where("cityId = ?",
                String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }
        else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    private void queryFromServer(String address, final String possion) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {//向服务器发送请求,返回的数据会回调到onResponse

            /**
             * 根据传入的地址和类型从服务器上查询省市县的数据
             */
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();//得到服务器返回的具体内容
                boolean result = false;

                /**
                 * 调用Utility中的方法进行解析和处理省市县的数据,且存储数据
                 */
                if ("province".equals(possion)){
                    result = Utility.handleProvinceResponse(responseText);
                }
                else if ("city".equals(possion)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }
                else if ("county".equals(possion)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(possion)){
                                queryProvinces();
                            }
                            else if ("city".equals(possion)){
                                queryCities();
                            }
                            else if ("county".equals(possion)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            /**
             * 活动显示异常处理
             */
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

}
