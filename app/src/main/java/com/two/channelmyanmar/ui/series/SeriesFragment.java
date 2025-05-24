package com.two.channelmyanmar.ui.series;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.two.channelmyanmar.DetailActivity;
import com.two.channelmyanmar.R;
import com.two.channelmyanmar.adapter.GridLoadMoreAdapter;
import com.two.channelmyanmar.adapter.LoadMoreAdapter;
import com.two.channelmyanmar.databinding.FragmentNotificationsBinding;
import com.two.channelmyanmar.databinding.SeriesFragmentBinding;
import com.two.channelmyanmar.model.MovieModel;
import com.two.channelmyanmar.util.Utils;
import com.two.channelmyanmar.viewmodel.ApiViewModel;
import com.two.my_libs.base.BaseThemeFragment;
import com.two.my_libs.views.ptr.PtrFrameLayout;
import com.two.my_libs.views.ptr.PtrHandler;
import com.two.my_libs.views.ptr.PtrUIHandler;
import com.two.my_libs.views.ptr.header.StoreHouseHeader;
import com.two.my_libs.views.ptr.indicator.PtrIndicator;
import com.two.my_libs.views.ptr.util.PtrLocalDisplay;

import java.util.ArrayList;
import java.util.List;

public class SeriesFragment extends BaseThemeFragment {

    private boolean isLastPage = false;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 15;

    private ApiViewModel viewModel;
    private SeriesFragmentBinding binding; // View Binding
    private GridLoadMoreAdapter<MovieModel> adapter;
    private final ArrayList<MovieModel> data = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Use View Binding instead of findViewById
        binding = SeriesFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot(); // Return root view
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ApiViewModel.class);

        // Setup RecyclerView & PtrFrameLayout
        setupRecyclerView();
        setupPtrFrame();

        // Observe error messages
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            binding.seriesPtrFrame.refreshComplete();
            adapter.setLoading(false);
            adapter.setError(true);
        });

        // Observe movie data
        viewModel.getLiveData(ApiViewModel.ApiType.SERIES)
                .observe(getViewLifecycleOwner(), result -> {
                    if (result instanceof List) {
                        binding.seriesPtrFrame.refreshComplete();
                        List<MovieModel> movies = (List<MovieModel>) result;
                        if (currentPage == 1) {
                            adapter.setItems(movies);
                        } else {
                            adapter.addItems(movies);
                        }
                        data.addAll(movies);
                        adapter.showShimmer(false);
                        adapter.setLoading(false);
                        currentPage++;
                        if (movies.size() < PAGE_SIZE) {
                            isLastPage = true;
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        binding.seriesRecycler.setLayoutManager(layoutManager);

        adapter = new GridLoadMoreAdapter<>();
        adapter.setItemClickListener(new GridLoadMoreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieModel data) {
                Intent intent = new Intent(requireActivity(), DetailActivity.class);
                intent.putExtra("baseUrl", data.getBaseUrl());
                intent.putExtra("name",data.getName());
                requireActivity().startActivity(intent);
                (requireActivity()).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        adapter.setLoadMoreListener(() -> {
            //Toast.makeText(getContext(), "On load More", Toast.LENGTH_SHORT).show();
            if (!isLastPage && Utils.isNetworkAvailable(getContext())) {
                loadMoreData();
            } else {
                adapter.setLoading(false);
                adapter.setError(true);
            }
        });

        adapter.showShimmer(true);
        binding.seriesRecycler.setAdapter(adapter);

        binding.seriesRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                GridLayoutManager lm = (GridLayoutManager) recyclerView.getLayoutManager();
                if (lm != null && lm.findFirstVisibleItemPosition() == 0) {
                    binding.seriesPtrFrame.enableScrolling();
                } else {
                    binding.seriesPtrFrame.disableScrolling();
                }
            }
        });
    }

    private void setupPtrFrame() {
        StoreHouseHeader header = new StoreHouseHeader(getContext());
        header.setPadding(0, PtrLocalDisplay.dp2px(15), 0, 0);
        header.initWithString("Loading");

        binding.seriesPtrFrame.addPtrUIHandler(new PtrUIHandler() {
            private int mLoadTime = 0;
            @Override
            public void onUIReset(PtrFrameLayout frame) {
                mLoadTime++;
                header.initWithString("Updating");
            }
            @Override public void onUIRefreshPrepare(PtrFrameLayout frame) { }
            @Override public void onUIRefreshBegin(PtrFrameLayout frame) { }
            @Override public void onUIRefreshComplete(PtrFrameLayout frame) { }
            @Override public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) { }
        });

        binding.seriesPtrFrame.setDurationToCloseHeader(3000);
        binding.seriesPtrFrame.setHeaderView(header);
        binding.seriesPtrFrame.addPtrUIHandler(header);
        binding.seriesPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return true;
            }
            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                viewModel.refreshData(ApiViewModel.ApiType.SERIES);
            }
        });
    }

    private void loadMoreData() {
        String pageParam = currentPage > 1 ? "page/" + currentPage + "/" : "";
        String url = "https://channelmyanmar.to/tvshows/" + pageParam;
        viewModel.loadMore(ApiViewModel.ApiType.MORE_SERIES, url);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevent memory leaks
    }
}
