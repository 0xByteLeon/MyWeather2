package moonlightsw.com.myweather.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import moonlightsw.com.myweather.R;
import moonlightsw.com.myweather.service.AutoUpdateService;
import moonlightsw.com.myweather.util.HttpCallbackListener;
import moonlightsw.com.myweather.util.HttpUtil;
import moonlightsw.com.myweather.util.Utility;

/**
 * Created by MoonlightSW on 2016/3/14.
 */
public class WeatherFragment extends Fragment{

    private LinearLayout weatherInfoLayout;
    private TextView cityNameText;
    private TextView publishText;
    private TextView weatherDespText;
    private TextView temp1Text;
    private TextView temp2Text;
    private TextView currentDateText;

    private Activity activity;

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        mainContext = (Activity) context;
//    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weather_layout,container,false);
//        weatherInfoLayout = (LinearLayout) view.findViewById(R.id.weather_info_layout);
//        cityNameText = (TextView) view.findViewById(R.id.city_name);
//        publishText = (TextView) view.findViewById(R.id.publish_text);
//        weatherDespText = (TextView) view.findViewById(R.id.weather_desp);
//        temp1Text = (TextView) view.findViewById(R.id.temp1);
//        temp2Text = (TextView) view.findViewById(R.id.temp2);
//        currentDateText = (TextView) view.findViewById(R.id.current_date);
//
//            publishText.setText("正在同步中...");



        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address,"countyCode");
    }

    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpResquest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    Utility.handleWeatherResponse(activity, response);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                publishText.setText("同步失败");
            }
        });
    }

    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        cityNameText.setText(prefs.getString("city_name"," "));
        temp1Text.setText(prefs.getString("temp1"," "));
        temp2Text.setText(prefs.getString("temp2"," "));
        weatherDespText.setText(prefs.getString("weatherDesp"," "));
        publishText.setText("今天 " + prefs.getString("publish_time"," ") + " 发布");
        currentDateText.setText(prefs.getString("current_date"," "));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
    }

    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address,"weatherCode");
    }
}
