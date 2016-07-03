package moonlightsw.com.myweather.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import moonlightsw.com.myweather.R;
import moonlightsw.com.myweather.listener.MyClickListener;
import moonlightsw.com.myweather.model.City;
import moonlightsw.com.myweather.model.County;
import moonlightsw.com.myweather.model.MyViewHolder;
import moonlightsw.com.myweather.model.Province;

/**
 * Created by MoonlightSW on 2016/6/26.
 */
public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private List dataList;
    private Context context;
    private MyClickListener clickListener = null;

    //  自定义的adapter的构造函数，分别传入context和数据
    public MyAdapter(Context context, List list) {
        this.context = context;
        this.dataList = list;
    }

    //  解析viewholder的布局
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_aera_item, parent, false);
        MyViewHolder item = new MyViewHolder(view,clickListener);
        return item;
    }

    //  为recyclerview绑定数据
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Log.e("Draw", "onBindViewHolder: " + position);
        Bitmap aeraBitmap = ((BitmapDrawable)holder.getImageView().getDrawable()).getBitmap();
        if (dataList.get(position).getClass() == Province.class) {
            Province aera = (Province) dataList.get(position);
            holder.getTitleName().setText(aera.getProvinceName());
        } else if (dataList.get(position).getClass() == City.class) {
            City aera = (City) dataList.get(position);
            holder.getTitleName().setText(aera.getCityName());
        } else {
            County aera = (County) dataList.get(position);
            holder.getTitleName().setText(aera.getCountyName());
        }

        //  设置textview和text颜色
        Palette.from(aeraBitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                //  有活力的色彩
                Palette.Swatch vibrant = palette.getVibrantSwatch();
                holder.getTitleName().setBackgroundColor(vibrant.getRgb());
                holder.getTitleName().setTextColor(vibrant.getTitleTextColor());
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public MyClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(MyClickListener clickListener) {
        this.clickListener = clickListener;
    }

}
