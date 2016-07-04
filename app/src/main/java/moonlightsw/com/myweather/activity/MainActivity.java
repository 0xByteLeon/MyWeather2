package moonlightsw.com.myweather.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import moonlightsw.com.myweather.R;
import moonlightsw.com.myweather.db.MyWeatherDB;
import moonlightsw.com.myweather.fragment.ChooseAeraFragment;
import moonlightsw.com.myweather.fragment.WeatherFragment;
import moonlightsw.com.myweather.model.City;
import moonlightsw.com.myweather.model.County;
import moonlightsw.com.myweather.model.Province;
import moonlightsw.com.myweather.util.HttpCallbackListener;
import moonlightsw.com.myweather.util.HttpUtil;
import moonlightsw.com.myweather.util.Utility;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private MyWeatherDB myWeatherDB;
    private List<String> dataList = new ArrayList<String>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;
    private boolean isFromWeatherActivity;

    private String lock = "lock";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleText = (TextView) findViewById(R.id.title_text);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "添加一个新的地方？", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Log.e("isQuerySucceed", "Values:" + Utility.isQuerySucceed());
        myWeatherDB = MyWeatherDB.getInstance(this);
        //  检测数据库中是否有数据
        if (myWeatherDB.loadProvinces().size() <= 0) {
            showProgressDialog();
            //  开启线程获取网络数据
            Utility.queryFromServer(null, "province", 1);
            //  开启数据获取监控线程
            startMonitorAsyncTask();
        } else {
            android.app.FragmentManager manager = getFragmentManager();
            android.app.FragmentTransaction transaction = manager.beginTransaction();
            ChooseAeraFragment chooseAeraFragment = new ChooseAeraFragment();
            transaction.add(R.id.fragment_container, chooseAeraFragment).commit();
        }


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //  显示加载数据提示框
    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();

    }

    //  关闭加载数据提示框
    public void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    //  开启数据查询监控线程
    private void startMonitorAsyncTask() {

        new AsyncTask<Void, Integer, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    while (true) {
                        if (Utility.isQuerySucceed() == 3 | Utility.isQuerySucceed() == 4) {
                            Log.e("isQuerySucceed", "Values:" + Utility.isQuerySucceed());
                            break;
                        }
                        Thread.sleep(100);

                    }
                } catch (InterruptedException e) {
                    Utility.setQuerySucceed(1);
                    e.printStackTrace();
                }
                publishProgress(Utility.isQuerySucceed());

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                closeProgressDialog();
                if (4 == Utility.isQuerySucceed()) {
                    Toast.makeText(MainActivity.this, "加载失败，请检查网络是否连接", Toast.LENGTH_SHORT).show();
                }
                Utility.setQuerySucceed(1);
                getFragmentManager().beginTransaction().add(R.id.fragment_container, new ChooseAeraFragment()).commit();
                super.onProgressUpdate(values);
            }
        }.execute();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_weather) {
            if (getFragmentManager().findFragmentById(R.layout.weather_layout) != null) {
                getFragmentManager().beginTransaction().hide(getFragmentManager().findFragmentById(R.layout.choose_aera_fragment)).commit();
                getFragmentManager().beginTransaction().show(getFragmentManager().findFragmentById(R.layout.weather_layout)).commit();
            } else {
                getFragmentManager().beginTransaction().add(R.id.fragment_container, new WeatherFragment()).commit();
            }


        } else if (id == R.id.nav_place) {
            if (getFragmentManager().findFragmentById(R.layout.choose_aera_fragment) != null) {
                getFragmentManager().beginTransaction().hide(getFragmentManager().findFragmentById(R.layout.weather_layout)).commit();
                getFragmentManager().beginTransaction().show(getFragmentManager().findFragmentById(R.layout.choose_aera_fragment)).commit();
            } else {
                getFragmentManager().beginTransaction().add(R.id.fragment_container, new ChooseAeraFragment()).commit();
            }

            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new ChooseAeraFragment()).commit();

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
