package com.two.channelmyanmar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;

import com.two.channelmyanmar.adapter.GridLoadMoreAdapter;
import com.two.channelmyanmar.api.CmSearchApi;
import com.two.channelmyanmar.api.SearchCm;
import com.two.channelmyanmar.databinding.ActivityTagBinding;
import com.two.channelmyanmar.model.MovieModel;
import com.two.channelmyanmar.preference.PreferenceHelper;
import com.two.channelmyanmar.util.Utils;
import com.two.channelmyanmar.viewmodel.ApiViewModel;
import com.two.my_libs.ActivityTheme;
import com.two.my_libs.base.BaseThemeActivity;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Created by Toewaioo on 4/8/25
 * Description: [Add class description here]
 */
public class TagActivity extends BaseThemeActivity implements SearchCm.CallBack {
    private final String TAG = this.getClass().getSimpleName();
    ActivityTagBinding binding;
    private GridLoadMoreAdapter<MovieModel> adapter;
    private final ArrayList<MovieModel> data = new ArrayList<>();
    PreferenceHelper helper;
    private String url = "";
    private boolean isLastPage = false;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 15;
    private ExecutorService service = Executors.newFixedThreadPool(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTagBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        helper = new PreferenceHelper(this);
        String title = getIntent().getStringExtra("tag");
        url = getIntent().getStringExtra("url");
        if (title != null) binding.toolbarText.setText(title);
        setupRecyclerView();
        service.execute(new SearchCm(helper, url, this));

    }

    private void loadMoreData() {
        String pageParam = currentPage > 1 ? "page/" + currentPage + "/" : "";
        String finalUrl = url + pageParam;
        service.execute(new SearchCm(helper, finalUrl, this));
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        binding.tagRecycler.setLayoutManager(layoutManager);
        adapter = new GridLoadMoreAdapter<>();
        adapter.setItemClickListener(new GridLoadMoreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieModel data) {
                Intent i = new Intent(getApplicationContext(), DetailActivity.class);
                i.putExtra("baseUrl", data.getBaseUrl());
                i.putExtra("name",data.getName());
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });
        adapter.setLoadMoreListener(new GridLoadMoreAdapter.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!isLastPage && Utils.isNetworkAvailable(getApplicationContext())) {
                    loadMoreData();
                } else {
                    adapter.setLoading(false);
                    adapter.setError(true);
                }
            }
        });
        adapter.showShimmer(true);
        binding.tagRecycler.setAdapter(adapter);
    }

    @Override
    protected void configTheme(ActivityTheme activityTheme) {
        activityTheme.setThemes(new int[]{R.style.AppTheme_Light, R.style.AppTheme_Black});
        activityTheme.setStatusBarColorAttrRes(R.attr.colorPrimary);
        activityTheme.setSupportMenuItemThemeEnable(true);
    }

    @Override
    public void onSuccess(ArrayList<MovieModel> data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (currentPage == 1) {
                    adapter.setItems(data);
                } else {
                    adapter.addItems(data);
                }
                currentPage++;
                adapter.showShimmer(false);
                adapter.setLoading(false);
                if (data.size() < PAGE_SIZE) {
                    isLastPage = true;
                }
            }
        });

    }

    @Override
    public void onFail(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            }
        });

    }
}
