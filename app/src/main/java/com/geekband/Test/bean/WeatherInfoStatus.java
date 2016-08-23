package com.geekband.Test.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2016/6/24.
 */
public class WeatherInfoStatus {

    @SerializedName("HeWeather data service 3.0")
    private List<WeatherInfo> weatherInfo;

    public List<WeatherInfo> getWeatherInfo() {
        return weatherInfo;
    }

    public void setWeatherInfo(List<WeatherInfo> weatherInfo) {
        this.weatherInfo = weatherInfo;
    }

}
