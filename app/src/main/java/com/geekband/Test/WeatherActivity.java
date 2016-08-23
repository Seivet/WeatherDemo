package com.geekband.Test;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.geekband.Test.bean.WeatherInfo;
import com.geekband.Test.bean.WeatherInfoStatus;
import com.geekband.Test.provider.WeatherProvider;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/6/22.
 */
public class WeatherActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mEditText;
    private Button mGetWeatherButton;
    private TextView mCityText;
    private WeatherInfoStatus mWeatherInfoStatus;
    private WeatherInfo mWeatherInfo;
    public static final String httpUrl = "http://apis.baidu.com/heweather/weather/free";
    String httpArg = "city=beijing";
    private TextView mTodayWeatherText;
    private TextView mTomorrowWeatherText;
    private Button mUpdateWeatherButton;
    private boolean shouldUpdate;
    public static final int UPDATE_WEATHER_CODE = 123456;
    private boolean isFirst = true;

    private Uri mUri = Uri.parse(WeatherProvider.WeatherURI);
    private Handler mHandler;

//    private ContentResolver mContentResolver = getContentResolver();
//    private String mEditTextContent = mEditText.getText().toString();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        findViews();
        setListeners();

    }

    private void findViews() {
        mEditText = (EditText) findViewById(R.id.input_city_et);
        mGetWeatherButton = (Button) findViewById(R.id.get_weather_button);
        mCityText = (TextView) findViewById(R.id.city_txt);
        mTodayWeatherText = (TextView) findViewById(R.id.today_text_view);
        mTomorrowWeatherText = (TextView) findViewById(R.id.tomorrow_text_view);
        mUpdateWeatherButton = (Button) findViewById(R.id.update_weather_button);
    }

    private void setListeners() {
        mGetWeatherButton.setOnClickListener(this);
        mUpdateWeatherButton.setOnClickListener(this);
    }

//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case UPDATE_WEATHER_CODE:
//
//                    if (isFirst) {
//                        Toast.makeText(WeatherActivity.this, "获取成功", Toast.LENGTH_SHORT).show();
//                        isFirst = false;
//                    }else {
//                        Toast.makeText(WeatherActivity.this, "更新完成", Toast.LENGTH_SHORT).show();
//                    }
//                    break;
//            }
//        }
//    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.get_weather_button:

                if( mEditText.getText() != null ) {
                    try {
                        httpArg = "city=" + URLEncoder.encode(mEditText.getText().toString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    new RequestNetworkDataTask().execute(httpArg);

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Message message = mHandler.obtainMessage();
                            message.what = UPDATE_WEATHER_CODE;
                            mHandler.sendMessage(message);
                            new RequestNetworkDataTask().execute(httpArg);
                        }
                    }, 1000, 60000);

                    mHandler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            switch (msg.what) {
                                case UPDATE_WEATHER_CODE:

                                    if (isFirst) {
                                        Toast.makeText(WeatherActivity.this, "获取成功", Toast.LENGTH_SHORT).show();
                                        isFirst = false;
                                    } else {
                                        Toast.makeText(WeatherActivity.this, "更新完成", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                            }
                        }
                    };
                }else {
                    Toast.makeText(WeatherActivity.this, "请输入城市名", Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.update_weather_button:

                if(mEditText.getText() != null ) {
                    new RequestNetworkDataTask().execute(httpArg);
                    Toast.makeText(WeatherActivity.this, "手动更新完成", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(WeatherActivity.this, "无更新数据，请输入城市", Toast.LENGTH_SHORT).show();
                }

                break;


        }

    }

    class RequestNetworkDataTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String[] params) {

            String result = requestData(httpUrl,params[0]);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Gson gson = new Gson();
            mWeatherInfoStatus = gson.fromJson(result, WeatherInfoStatus.class);
            mWeatherInfo = mWeatherInfoStatus.getWeatherInfo().get(0);

            saveWeatherInfo(mWeatherInfo);

            Cursor cursor = getContentResolver().query(mUri, null, "cityName = ?", new String[]{mEditText.getText().toString()}, null);
            if (cursor != null && cursor.moveToLast()) {
                updateUIFromDatabase(cursor);
            }
        }
    }


    public static String requestData(String httpUrl, String httpArg) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        httpUrl = httpUrl + "?" + httpArg;

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("apikey",  "54bebf8968cad247a655642ece7ad280");
            connection.connect();

            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public void saveWeatherInfo (WeatherInfo weatherInfo) {

        ContentValues cv = new ContentValues();

        SimpleDateFormat sDateFormat = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
        String date = sDateFormat.format(System.currentTimeMillis());

        cv.put("saveTime", date);
        cv.put("cityName", mEditText.getText().toString());

        cv.put("city", weatherInfo.getBasic().getCity());

        cv.put("txt", weatherInfo.getNow().getCond().getTxt());
        cv.put("tmp", weatherInfo.getNow().getTmp());
        cv.put("date_0", weatherInfo.getDaily_forecast().get(0).getDate());
        cv.put("hum_0", weatherInfo.getDaily_forecast().get(0).getHum());
        cv.put("max_0", weatherInfo.getDaily_forecast().get(0).getTmp().getMax());
        cv.put("min_0", weatherInfo.getDaily_forecast().get(0).getTmp().getMin());
        cv.put("dir_0", weatherInfo.getDaily_forecast().get(0).getWind().getDir());
        cv.put("sc_0", weatherInfo.getDaily_forecast().get(0).getWind().getSc());

        cv.put("date_1", weatherInfo.getDaily_forecast().get(1).getDate());
        cv.put("hum_1", weatherInfo.getDaily_forecast().get(1).getHum());
        cv.put("max_1", weatherInfo.getDaily_forecast().get(1).getTmp().getMax());
        cv.put("min_1", weatherInfo.getDaily_forecast().get(1).getTmp().getMin());
        cv.put("dir_1", weatherInfo.getDaily_forecast().get(1).getWind().getDir());
        cv.put("sc_1", weatherInfo.getDaily_forecast().get(1).getWind().getSc());
        cv.put("txt_d_1", weatherInfo.getDaily_forecast().get(1).getCond().getTxt_d());
        cv.put("txt_n_1", weatherInfo.getDaily_forecast().get(1).getCond().getTxt_n());

        getContentResolver().insert(mUri, cv);

    }

    public void updateUIFromDatabase (Cursor cursor){

        String city = cursor.getString(cursor.getColumnIndex("city"));

        String txt = cursor.getString(cursor.getColumnIndex("txt"));
        String tmp = cursor.getString(cursor.getColumnIndex("tmp"));
        String date_0 = cursor.getString(cursor.getColumnIndex("date_0"));
        String hum_0 = cursor.getString(cursor.getColumnIndex("hum_0"));
        String max_0 = cursor.getString(cursor.getColumnIndex("max_0"));
        String min_0 = cursor.getString(cursor.getColumnIndex("min_0"));
        String dir_0 = cursor.getString(cursor.getColumnIndex("dir_0"));
        String sc_0 = cursor.getString(cursor.getColumnIndex("sc_0"));

        String date_1 = cursor.getString(cursor.getColumnIndex("date_1"));
        String hum_1 = cursor.getString(cursor.getColumnIndex("hum_1"));
        String max_1 = cursor.getString(cursor.getColumnIndex("max_1"));
        String min_1 = cursor.getString(cursor.getColumnIndex("min_1"));
        String dir_1 = cursor.getString(cursor.getColumnIndex("dir_1"));
        String sc_1 = cursor.getString(cursor.getColumnIndex("sc_1"));
        String txt_d_1 = cursor.getString(cursor.getColumnIndex("txt_d_1"));
        String txt_n_1 = cursor.getString(cursor.getColumnIndex("txt_n_1"));


        String cityName = city + "天气";
        String todayWeather = "今日：" + date_0 + "\n" +
                "天气情况：" + txt + "\n" +
                "当前气温：" + tmp + "\n" +
                "最高气温：" + max_0 + "\n" +
                "最低气温：" + min_0 + "\n" +
                "今日湿度：" + hum_0 + "\n" +
                "今日风向：" + dir_0 + "\n" +
                "今日风力：" + sc_0;
        String tomorrowWeather = "明日：" + date_1 + "\n" +
                "日间天气：" + txt_d_1 + "\n" +
                "夜间天气：" + txt_n_1 + "\n" +
                "最高气温：" + max_1 + "\n" +
                "最低气温：" + min_1 + "\n" +
                "明日湿度：" + hum_1 + "\n" +
                "明日风向：" + dir_1 + "\n" +
                "明日风力：" + sc_1;

        mCityText.setText(cityName);
        mTodayWeatherText.setText(todayWeather);
        mTomorrowWeatherText.setText(tomorrowWeather);
    }

}
