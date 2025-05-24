package com.two.channelmyanmar.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.two.channelmyanmar.DetailActivity;
import com.two.channelmyanmar.R;
import com.two.channelmyanmar.activity.BrowserActivity;
import com.two.channelmyanmar.adapter.RecyclerPagerAdapter;
import com.two.channelmyanmar.model.SuggestModel;
import com.two.my_libs.shimmer.Shimmer;
import com.two.my_libs.shimmer.ShimmerDrawable;
import com.two.my_libs.shimmer.ShimmerTextViewDrawable;

import java.util.ArrayList;

public class SuggestPagerAdapter extends RecyclerPagerAdapter {
    private  Context mContext;
    private  ArrayList<SuggestModel> imageIdList;

    private int size;
    private boolean isInfiniteLoop;
    public SuggestPagerAdapter(Context mContext,ArrayList<SuggestModel> modelArrayList){
        this.mContext= mContext;
        this.imageIdList = modelArrayList;
        this.size = imageIdList.size();
        isInfiniteLoop = false;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    private int getPosition(int position) {
        return isInfiniteLoop ? position % size : position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.suggest_pager_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext,"Click",Toast.LENGTH_SHORT).show();
                if (imageIdList.get(position).getLink().contains("channelmyanmar")) {
                    Intent i = new Intent(mContext, DetailActivity.class);
                    i.putExtra("baseUrl", imageIdList.get(position).getLink());
                    i.putExtra("name",imageIdList.get(position).getLink());
                    mContext.startActivity(i);
                    ((Activity) v.getContext()).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                }else {
                    Intent i = new Intent(mContext, BrowserActivity.class);
                    i.putExtra("baseUrl", imageIdList.get(position).getLink());
                    mContext.startActivity(i);
                    ((AppCompatActivity) v.getContext()).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                }
            }
        });
        Shimmer shimmer = new Shimmer.AlphaHighlightBuilder()
                .setDuration(1000)
                .setBaseAlpha(0.7f)
                .setHighlightAlpha(1.0f)
                .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                .build();

        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);
        ShimmerTextViewDrawable drawable = new ShimmerTextViewDrawable((holder).imageView.getContext(),"Loading..");
        drawable.setTextSize(50 ); // 18sp
        drawable.setTypeface(Typeface.DEFAULT_BOLD);
        drawable.setShimmerDuration(1200); // change shimmer animation duration
        //Log.d("SuggestAdapter",imageIdList.get(position).getImgUrl());
        Glide.with(mContext).load(imageIdList.get(position).getImgUrl()).placeholder(drawable).error(R.drawable.err_image).into(holder.imageView);
        return convertView;
    }

    @Override
    public int getCount() {
        return isInfiniteLoop ? Integer.MAX_VALUE : imageIdList.size();
    }

    private static class ViewHolder {

        ImageView imageView;

        public ViewHolder(View convertView) {
            this.imageView = (ImageView) convertView
                    .findViewById(R.id.image_view);
        }
    }

    /**
     * @return the isInfiniteLoop
     */
    public boolean isInfiniteLoop() {
        return isInfiniteLoop;
    }

    /**
     * @param isInfiniteLoop
     *            the isInfiniteLoop to set
     */
    public SuggestPagerAdapter setInfiniteLoop(boolean isInfiniteLoop) {
        this.isInfiniteLoop = isInfiniteLoop;
        return this;
    }
}
