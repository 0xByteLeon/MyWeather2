package moonlightsw.com.myweather.util;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import moonlightsw.com.myweather.activity.MainActivity;
import moonlightsw.com.myweather.db.MyWeatherDB;
import moonlightsw.com.myweather.model.City;
import moonlightsw.com.myweather.model.County;
import moonlightsw.com.myweather.model.Province;

/**
 * Created by MoonlightSW on 2016/3/14.
 */
public class Utility {
    public synchronized static boolean handleProvincesResponse(MyWeatherDB myWeatherDB,String response) {
        if (!TextUtils.isEmpty(response)) {
            String[] allProvince = response.split(",");
            if (allProvince != null && allProvince.length >0) {
                for (String p : allProvince) {
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceName(array[1]);
                    province.setProvinceCode(array[0]);
                    myWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean handleCitiesResponse(MyWeatherDB myWeatherDB, String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityName(array[1]);
                    city.setCityCode(array[0]);
                    city.setProvinceId(provinceId);
                    myWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean handleCountiesResponse(MyWeatherDB myWeatherDB, String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length > 0) {
                for (String c : allCounties) {
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyName(array[1]);
                    county.setCountyCode(array[0]);
                    county.setCityId(cityId);
                    myWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    public static void handleWeatherResponse(Context context,String response) {
        try {
            JSONObject obj= new JSONObject(response);
            JSONObject weatherInfo = obj.getJSONObject("weatherinfo");
            String cityName = weatherInfo.getString("city");
            String weatherCode = weatherInfo.getString("cityid");
            String temp1 = weatherInfo.getString("temp1");
            String temp2 = weatherInfo.getString("temp2");
            String weatherDesp = weatherInfo.getString("weather");
            String publishTime = weatherInfo.getString("ptime");
            saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected",true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date",sdf.format(new Date()));
        Log.d("Utility", "执行完毕。。。。。。");
        editor.commit();
    }

    public static int isQuerySucceed() {
        return isQuerySucceed;
    }

    public static void setQuerySucceed(int querySucceed) {
        isQuerySucceed = querySucceed;
    }

    /*  判断后台网络线程是否加载完毕和是否成功
    * 1表示还未开始加载
    * 2表示开启后台网络加载数据线程
    * 3表示加载成功
    * 4表示加载失败
    * */

    private static int isQuerySucceed = 1;

    //  从服务器查询数据
    public static void queryFromServer(String code,final String type,final int id) {
        String address = null;
        final MyWeatherDB myWeatherDB = MyWeatherDB.getInstance(null);
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city"+ code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        Utility.setQuerySucceed(2);
        Log.e("QueryFromServer","--->begin,Value:" + isQuerySucceed());
        HttpUtil.sendHttpResquest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                Log.e("QueryFromServer","Result:" + response);
                if ("province".equals(type)) {
                    result = Utility.handleProvincesResponse(myWeatherDB, response);
                } else if ("city".equals(type)) {
                    result = Utility.handleCitiesResponse(myWeatherDB, response, id);
                } else if ("county".equals(type)) {
                    result = Utility.handleCountiesResponse(myWeatherDB, response, id);
                }
                if (result) {
                    Utility.setQuerySucceed(3);
                    Log.e("queryFromServer","--->end:" + Utility.isQuerySucceed());
                } else {
                    Utility.setQuerySucceed(4);
                    Log.e("queryFromServer","--->end:" + Utility.isQuerySucceed());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("queryFromServer","--->end-->onError:" + Utility.isQuerySucceed());
                Utility.setQuerySucceed(4);
            }
        });
    }
}
