package moonlightsw.com.myweather.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import moonlightsw.com.myweather.adapter.MyAdapter;
import moonlightsw.com.myweather.R;
import moonlightsw.com.myweather.db.MyWeatherDB;
import moonlightsw.com.myweather.listener.MyClickListener;
import moonlightsw.com.myweather.model.City;
import moonlightsw.com.myweather.model.County;
import moonlightsw.com.myweather.model.Province;
import moonlightsw.com.myweather.util.Utility;

/**
 * Created by MoonlightSW on 2016/6/24.
 */
public class ChooseAeraFragment extends Fragment implements MyClickListener {

    private TextView titleText;
    private RecyclerView recyclerView;
    private MyWeatherDB myWeatherDB;
    private List aeraList;
    private MyAdapter adapter;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        InitDate();
        adapter = new MyAdapter(getActivity().getApplicationContext(), aeraList);
        View view = inflater.inflate(R.layout.choose_aera_fragment, container, false);
        adapter.setClickListener(this);
        titleText = (TextView) view.findViewById(R.id.title_text);
        recyclerView = (RecyclerView) view.findViewById(R.id.aera_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        return view;
    }

    void InitDate() {
        myWeatherDB = MyWeatherDB.getInstance(getActivity().getApplicationContext());
        aeraList = myWeatherDB.loadProvinces();
    }

    @Override
    public void onMyItemClick(View view, int position) {
        Log.e("ObjectType:--->", aeraList.get(position).toString());
        refreshData(position);
    }

    //  监控线程，数据是否加载完成
    private class RefreshRecyclerView extends AsyncTask<Void, Boolean, Void> {
        private Province selectedProvince;
        private City selectedCity;
        private County selectedCounty;

        public RefreshRecyclerView(Province chooseAera) {
            this.selectedProvince = chooseAera;
        }

        public RefreshRecyclerView(City chooseAera) {
            this.selectedCity = chooseAera;
        }

        public RefreshRecyclerView(County chooseAera) {
            this.selectedCounty = chooseAera;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Log.e("doInBackground--->begin", "Values:" + Utility.isQuerySucceed());
            try {
                while (true) {
                    Log.e("doInBackground--->while", "Values:" + Utility.isQuerySucceed());
                    Thread.sleep(100);
                    if (Utility.isQuerySucceed() == 3 | Utility.isQuerySucceed() == 4) {
                        Log.e("isQuerySucceed", "Values:" + Utility.isQuerySucceed());
                        publishProgress(true);
                        break;
                    }
                }
                Log.e("AeraList:--->", String.valueOf(aeraList.size()));
            } catch (InterruptedException e) {
                Utility.setQuerySucceed(1);
                publishProgress(false);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            if (values[0] == true) {
                Log.e("onProgressUpdate", "Refresh Data");
                aeraList.clear();

                //  判断需要获取的数据
                if (selectedProvince != null) {
                    aeraList.addAll(myWeatherDB.loadCities(selectedProvince.getId()));
                } else if (selectedCity != null) {
                    aeraList.addAll(myWeatherDB.loadCounty(selectedCity.getId()));
                } else {
                    aeraList.addAll(myWeatherDB.loadCounty(selectedCounty.getId()));
                }   //  地址不能变，不是数据___需要用List.addAll()方法来更新List内容,否则adapter.notifyDataSetChanged()方法无效

                closeProgressDialog();
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(0);
                Utility.setQuerySucceed(1);
            } else {
                closeProgressDialog();
                Toast.makeText(getActivity().getApplicationContext(), "数据加载失败", Toast.LENGTH_SHORT).show();
            }
            super.onProgressUpdate(values);
        }
    }

    //  点击事件更新列表数据
    private void refreshData(int position) {
        //  查询市级数据
        if (aeraList.get(position).getClass() == Province.class) {
            Province chooseAera = (Province) aeraList.get(position);
            showProgressDialog();
            Utility.setQuerySucceed(1);
            if (myWeatherDB.loadCities(chooseAera.getId()).size() > 0) {
                aeraList.clear();
                aeraList.addAll(myWeatherDB.loadCities(chooseAera.getId()));
                Log.e("onMyItemClick", "CitiesList" + aeraList);
                closeProgressDialog();
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(0);
            } else {
                Utility.queryFromServer(chooseAera.getProvinceCode(), "city", chooseAera.getId());
                new RefreshRecyclerView(chooseAera).execute();
            }
        }

        //  查询县级数据
        if (aeraList.get(position).getClass() == City.class) {
            City chooseAera = (City) aeraList.get(position);
            showProgressDialog();
            Utility.setQuerySucceed(1);
            if (myWeatherDB.loadCounty(chooseAera.getId()).size() > 0) {
                aeraList.clear();
                aeraList.addAll( myWeatherDB.loadCounty(chooseAera.getId()));
                Log.e("onMyItemClick", "CountiesList" + aeraList);
                closeProgressDialog();
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(0);
            } else {
                Utility.queryFromServer(chooseAera.getCityCode(), "county", chooseAera.getId());
                new RefreshRecyclerView(chooseAera).execute();
            }
        }
    }

    //  显示加载数据提示框
    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this.getActivity());
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

}




