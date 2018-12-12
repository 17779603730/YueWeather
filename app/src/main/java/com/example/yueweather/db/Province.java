package com.example.yueweather.db;

import org.litepal.crud.LitePalSupport;

/**
 * 2018/12/11
 * 作者：GuoYongze
 * provinceName 记录省的名字
 * provinceCode 记录省的代号
 */
public class Province extends LitePalSupport {
    private int id;
    private String provinceName;
    private int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
