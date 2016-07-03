package moonlightsw.com.myweather.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import moonlightsw.com.myweather.R;
import moonlightsw.com.myweather.listener.MyClickListener;

/**
 * Created by MoonlightSW on 2016/6/26.
 */
public class MyViewHolder extends  RecyclerView.ViewHolder {
    private TextView titleName;
    private ImageView imageView;
    private MyClickListener clickListener;

    public MyViewHolder(final View itemView, MyClickListener myClickListener) {
        super(itemView);
        this.clickListener = myClickListener;
        titleName = (TextView) itemView.findViewById(R.id.aera_name);
        imageView = (ImageView) itemView.findViewById(R.id.aera_pic);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  待实现动画效果
                if (clickListener != null)
                    clickListener.onMyItemClick(v,getLayoutPosition());
            }
        });
    }

    public TextView getTitleName() {
        return titleName;
    }

    public ImageView getImageView() {
        return imageView;
    }
}
