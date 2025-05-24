package com.two.channelmyanmar.watchlater;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.two.channelmyanmar.DetailActivity;
import com.two.channelmyanmar.R;
import com.two.channelmyanmar.adapter.GridLoadMoreAdapter;
import com.two.channelmyanmar.adapter.MyAdapter;
import com.two.channelmyanmar.databinding.ActivityFavBinding;
import com.two.channelmyanmar.model.MovieModel;
import com.two.my_libs.ActivityTheme;
import com.two.my_libs.base.BaseThemeActivity;
import com.two.my_libs.base.BaseThemeFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class WatchLater extends BaseThemeActivity {
    ActivityFavBinding mbinding;
    WatchLaterCacher cacher;
    private GridLoadMoreAdapter<MovieModel> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mbinding = ActivityFavBinding.inflate(getLayoutInflater());
        setContentView(mbinding.getRoot());
        cacher = new WatchLaterCacher(this);
        ArrayList<MovieModel> list = (ArrayList<MovieModel>) cacher.getWatchLaterList();
        list.sort(new SortComparator());

        adapter = new GridLoadMoreAdapter<>();
        adapter.setLoading(false);
        adapter.showShimmer(false);
        mbinding.favRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        mbinding.favRecycler.setAdapter(adapter);
        adapter.setItems(list);
        adapter.setItemClickListener(new GridLoadMoreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieModel data) {
                Intent i = new Intent(getApplicationContext(), DetailActivity.class);
                i.putExtra("baseUrl", data.getBaseUrl());
                i.putExtra("name",data.getName());
                startActivity(i);
            }
        });
    }

    @Override
    protected void configTheme(ActivityTheme activityTheme) {
        activityTheme.setThemes(new int[]{R.style.AppTheme_Light, R.style.AppTheme_Black});
        activityTheme.setStatusBarColorAttrRes(R.attr.colorPrimary);
        activityTheme.setSupportMenuItemThemeEnable(true);
    }
}
