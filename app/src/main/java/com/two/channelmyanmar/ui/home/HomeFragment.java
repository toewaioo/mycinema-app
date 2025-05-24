package com.two.channelmyanmar.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.appbar.AppBarLayout;
import com.two.channelmyanmar.DetailActivity;
import com.two.channelmyanmar.R;
import com.two.channelmyanmar.activity.BrowserActivity;
import com.two.channelmyanmar.adapter.MyAdapter;
import com.two.channelmyanmar.adapter.NewReleaseAdapter;
import com.two.channelmyanmar.api.CmHomeApi;
import com.two.channelmyanmar.api.SuggestApi;
import com.two.channelmyanmar.databinding.FragmentHomeBinding;
import com.two.channelmyanmar.model.MovieModel;
import com.two.channelmyanmar.model.SuggestModel;
import com.two.channelmyanmar.preference.PreferenceHelper;
import com.two.channelmyanmar.viewmodel.ApiViewModel;
import com.two.channelmyanmar.viewpager.AutoScrollViewPager;
import com.two.channelmyanmar.watchlater.WatchLater;
import com.two.my_libs.DarkMode;
import com.two.my_libs.MultiTheme;
import com.two.my_libs.base.BaseThemeFragment;
import com.two.my_libs.views.ptr.PtrFrameLayout;
import com.two.my_libs.views.ptr.PtrHandler;
import com.two.my_libs.views.ptr.PtrUIHandler;
import com.two.my_libs.views.ptr.header.StoreHouseHeader;
import com.two.my_libs.views.ptr.indicator.PtrIndicator;
import com.two.my_libs.views.ptr.util.PtrLocalDisplay;
import com.two.my_libs.views.taptarget.TapTarget;
import com.two.my_libs.views.taptarget.TapTargetSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class HomeFragment extends BaseThemeFragment implements SuggestApi.CallBack, CmHomeApi.CallBack, MyAdapter.OnItemClickListener {
    private FragmentHomeBinding binding;
    private NewReleaseAdapter newReleaseAdapter;
    private MyAdapter  movieAdapter, serieAdapter;
    private ApiViewModel viewModel;
    PtrFrameLayout frame;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.toolBar.inflateMenu(R.menu.main_menu);
        binding.shimmerLayout.startShimmer();
        //

        //
        viewModel = new ViewModelProvider(requireActivity()).get(ApiViewModel.class);
        //
        binding.watchLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), WatchLater.class);
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });

        //
        frame = binding.storeHousePtrFrame;
        final StoreHouseHeader header = new StoreHouseHeader(getContext());
        header.setPadding(0, PtrLocalDisplay.dp2px(5), 0, 0);

        header.initWithString("Loading");

        frame.addPtrUIHandler(new PtrUIHandler() {

            private int mLoadTime = 0;

            @Override
            public void onUIReset(PtrFrameLayout frame) {
                mLoadTime++;
                // String string = mStringList[mLoadTime % mStringList.length];
                header.initWithString("Updating");
            }

            @Override
            public void onUIRefreshPrepare(PtrFrameLayout frame) {

            }

            @Override
            public void onUIRefreshBegin(PtrFrameLayout frame) {

            }

            @Override
            public void onUIRefreshComplete(PtrFrameLayout frame) {

            }

            @Override
            public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {

            }
        });

        frame.setDurationToCloseHeader(1500);
        frame.setHeaderView(header);
        frame.addPtrUIHandler(header);
        frame.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return true;
            }

            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                viewModel.refreshData(ApiViewModel.ApiType.SUGGESTIONS);
                viewModel.refreshData(ApiViewModel.ApiType.SERIES);

            }
        });
        //
        binding.toolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.amt_dark_theme_off) {
                    Toast.makeText(getContext(),"Made heart by Toewaioo.",Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
        binding.appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
               // Log.d("Appbar", String.valueOf(verticalOffset));
                if (verticalOffset == 0) {
                    binding.storeHousePtrFrame.enableScrolling();
                } else {
                    binding.storeHousePtrFrame.disableScrolling();
                }
            }
        });
        binding.nestedScroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v12, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            boolean isAtTop = scrollY == 0;
            //Log.d("HomeFragment", "current:" + String.valueOf(scrollY) + ",old:" + String.valueOf(oldScrollY));
            if (isAtTop) {
                binding.scrollContainer.setVisibility(View.VISIBLE);
                binding.toolBar.setVisibility(View.INVISIBLE);

            } else {
                binding.scrollContainer.setVisibility(View.INVISIBLE);
                binding.toolBar.setVisibility(View.VISIBLE);
                binding.storeHousePtrFrame.disableScrolling();


            }
        });
        binding.appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isToolbarVisible = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // Determine the total scroll range once.
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                // When fully collapsed, show the toolbar.
                if (scrollRange + verticalOffset == 0) {
                    if (!isToolbarVisible) {

                        isToolbarVisible = true;

                    }
                } else {
                    // When expanded, hide the toolbar.
                    if (isToolbarVisible) {
                        binding.toolBar.setVisibility(View.INVISIBLE);
                        isToolbarVisible = false;


                    }
                }
            }
        });
        //
        newReleaseAdapter = new NewReleaseAdapter(new ArrayList<>(), new NewReleaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieModel data) {
                if (!isAdded())
                    return;
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(getContext(), data.getBaseUrl(), Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getContext(), DetailActivity.class);
                        i.putExtra("baseUrl", data.getBaseUrl());
                        i.putExtra("name",data.getName());
                        requireActivity().startActivity(i);
                        (requireActivity()).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                    }
                });
            }
        });
        newReleaseAdapter.setRecyclerView(binding.viewPagernewrelease);
        binding.viewPagernewrelease.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.viewPagernewrelease.setAdapter(newReleaseAdapter);
        newReleaseAdapter.startAutoScroll();

        viewModel.getLiveData(ApiViewModel.ApiType.SUGGESTIONS)
                .observe(getViewLifecycleOwner(), result -> {
                    if (result instanceof List) {
                        ArrayList<SuggestModel> suggestions = (ArrayList<SuggestModel>) result;

                        binding.shimmerLayout.stopShimmer();
                        binding.shimmerLayout.setVisibility(View.GONE);

                       // Toast.makeText(getContext(), String.valueOf(suggestions.size()), Toast.LENGTH_SHORT).show();
                        binding.viewPager.setAdapter(new SuggestPagerAdapter(getContext(), suggestions));
                        binding.indicatorcute.setupWithViewPager(binding.viewPager);
                        binding.viewPager.setInterval(3000);
                        binding.viewPager.startAutoScroll();
                        binding.viewPager.setSlideBorderMode(AutoScrollViewPager.SLIDE_BORDER_MODE_TO_PARENT);
                    }


                });
        viewModel.getLiveData(ApiViewModel.ApiType.NEW_RELEASE)
                .observe(getViewLifecycleOwner(), result -> {
                    binding.storeHousePtrFrame.refreshComplete();
                    // Toast.makeText(getContext(), String.valueOf(result.toString()), Toast.LENGTH_SHORT).show();
                    if (result instanceof List) {
                        ArrayList<MovieModel> random = (ArrayList<MovieModel>) result;
                        newReleaseAdapter.setShowShimmer(false);
                        newReleaseAdapter.updateData(random);
                        newReleaseAdapter.startAutoScroll();
                    }
                });
        viewModel.getLiveData(ApiViewModel.ApiType.MOVIES)
                .observe(getViewLifecycleOwner(), result -> {
                    // Toast.makeText(getContext(), String.valueOf(result.toString()), Toast.LENGTH_SHORT).show();
                    if (result instanceof List) {
                        ArrayList<MovieModel> movies = (ArrayList<MovieModel>) result;
                        movieAdapter.setShowShimmer(false);
                        movieAdapter.updateData(movies);

                    }
                });
        viewModel.getLiveData(ApiViewModel.ApiType.SERIES)
                .observe(getViewLifecycleOwner(), result -> {
                    // Toast.makeText(getContext(), String.valueOf(result.toString()), Toast.LENGTH_SHORT).show();
                    if (result instanceof List) {
                        ArrayList<MovieModel> series = (ArrayList<MovieModel>) result;
                        serieAdapter.setShowShimmer(false);
                        serieAdapter.updateData(series);


                    }
                });


// Observe errors
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            // Handle error
        });
        newReleaseAdapter.setShowShimmer(true);

        movieAdapter = new MyAdapter(new ArrayList<>(), this);
        binding.viewPagerMovie.setLayoutManager(new GridLayoutManager(getContext(), 3));

        binding.viewPagerMovie.setAdapter(movieAdapter);
        movieAdapter.setShowShimmer(true);
        serieAdapter = new MyAdapter(new ArrayList<>(), this);
        binding.viewPagerSesries.setLayoutManager(new GridLayoutManager(getContext(), 3));

        binding.viewPagerSesries.setAdapter(serieAdapter);
        serieAdapter.setShowShimmer(true);

    }


    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.amt_dark_theme_off) {
                    Toast.makeText(getContext(),"Made heart by Toewaioo.",Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

    }

    @Override
    public void onPause() {
        super.onPause();
        binding.viewPager.stopAutoScroll();

    }

    @Override
    public void onResume() {
        super.onResume();
        binding.viewPager.startAutoScroll();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        SuggestApi.removeListener(this);
        CmHomeApi.removeListener(this);
    }

    @Override
    public void onSuccessNewRelease(ArrayList<MovieModel> newRelease) {
        if (!isAdded())
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                newReleaseAdapter.setShowShimmer(false);
                newReleaseAdapter.updateData(newRelease);
                newReleaseAdapter.startAutoScroll();
            }
        });

    }

    @Override
    public void onMovies(ArrayList<MovieModel> movie) {
        if (!isAdded())
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                movieAdapter.setShowShimmer(false);
                movieAdapter.updateData(movie);
            }
        });
    }

    @Override
    public void onSeries(ArrayList<MovieModel> series) {
        if (!isAdded())
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serieAdapter.setShowShimmer(false);
                serieAdapter.updateData(series);
            }
        });
    }

    @Override
    public void onFail(String s) {
        if (!isAdded())
            return;
        binding.storeHousePtrFrame.refreshComplete();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onSuccess(ArrayList<SuggestModel> result) {
        if (!isAdded())
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.shimmerLayout.stopShimmer();
                binding.shimmerLayout.setVisibility(View.GONE);
                binding.storeHousePtrFrame.refreshComplete();
                //Toast.makeText(getContext(), String.valueOf(result.size()), Toast.LENGTH_SHORT).show();
                binding.viewPager.setAdapter(new SuggestPagerAdapter(getContext(), result));
                binding.indicatorcute.setupWithViewPager(binding.viewPager);
                binding.viewPager.setInterval(3000);
                binding.viewPager.startAutoScroll();
                binding.viewPager.setSlideBorderMode(AutoScrollViewPager.SLIDE_BORDER_MODE_TO_PARENT);
            }
        });

    }

    @Override
    public void onItemClick(MovieModel data) {
        if (!isAdded())
            return;
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getContext(), data.getBaseUrl(), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getContext(), DetailActivity.class);
                i.putExtra("baseUrl", data.getBaseUrl());
                i.putExtra("name",data.getName());
                requireActivity().startActivity(i);
                (requireActivity()).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });
    }
}