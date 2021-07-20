package com.example.ximalaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.ximalaya.R;
import com.example.ximalaya.base.BaseApplication;
import com.lcodecore.tkrefreshlayout.header.progresslayout.CircleImageView;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

public class PlayerTrackPagerAdapter extends PagerAdapter {

    private List<Track> mData = new ArrayList<>();

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View itemView = LayoutInflater.from(container.getContext()).inflate(R.layout.item_track_page, container, false);

        container.addView(itemView);
        //设置数据
        //找到控件
        ImageView item = itemView.findViewById(R.id.track_pager_item);
        //设置图片
        Track track = mData.get(position);
        String coverUrlLarge = track.getCoverUrlLarge();
        Picasso.with(container.getContext()).load(coverUrlLarge).into(item);
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public void setData(List<Track> list) {
        mData.clear();
        mData.addAll(list);
        notifyDataSetChanged();
    }


}
