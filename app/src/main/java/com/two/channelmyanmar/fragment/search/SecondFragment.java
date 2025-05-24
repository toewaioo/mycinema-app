package com.two.channelmyanmar.fragment.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.two.channelmyanmar.DetailActivity;
import com.two.channelmyanmar.R;
import com.two.channelmyanmar.adapter.GridLoadMoreAdapter;
import com.two.channelmyanmar.api.SearchCm;
import com.two.channelmyanmar.databinding.SearchSecondFragmentBinding;
import com.two.channelmyanmar.fragment.detail.MovieChildFragment;
import com.two.channelmyanmar.model.MovieModel;
import com.two.channelmyanmar.preference.PreferenceHelper;
import com.two.channelmyanmar.util.Utils;
import com.two.my_libs.base.BaseThemeFragment;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Created by Toewaioo on 4/8/25
 * Description: [Add class description here]
 */
public class SecondFragment extends BaseThemeFragment implements SearchCm.CallBack {
    private final String TAG = this.getClass().getSimpleName();
    SearchSecondFragmentBinding binding;
    private GridLoadMoreAdapter<MovieModel> adapter;
    private final ArrayList<MovieModel> data= new ArrayList<>();
    private ExecutorService service = Executors.newFixedThreadPool(2);
    private static String ARG_TEXT = "url";
    private String keyword = "";
    private boolean isLastPage = false;
    private int currentPage = 1;
    private PreferenceHelper helper;
    private static final int PAGE_SIZE = 15;
    public SecondFragment() {
        // Required empty public constructor.
    }

    public static SecondFragment newInstance(String text) {
        SecondFragment fragment = new SecondFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            keyword = getArguments().getString(ARG_TEXT);

            // Use baseUrl here
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       binding = SearchSecondFragmentBinding.inflate(inflater,container,false);
       helper= new PreferenceHelper(getContext());
       load();
       setupRecyclerView();
       binding.ssearchEdittext.setText(keyword);
       binding.searchgoBack.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               requireActivity().getOnBackPressedDispatcher().onBackPressed();
           }
       });
       binding.searchgoSearch.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               load();
               currentPage =1;
           }
       });
       binding.ssearchEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId== EditorInfo.IME_ACTION_SEARCH){
                    if (!binding.ssearchEdittext.getText().toString().isEmpty()) {
                        keyword=binding.ssearchEdittext.getText().toString();

                        load();
                        currentPage =1;
                        Toast.makeText(getContext(), binding.ssearchEdittext.getText().toString(), Toast.LENGTH_SHORT).show();
                        //  helper.saveString(binding.searchEdittext.getText().toString());
                        // reset();
                        return true;
                    }
                }
                return false;
            }
        });
       return binding.getRoot();
    }
    public void load(){
        service.execute(new SearchCm(helper,"https://channelmyanmar.to/?s="+keyword,this));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        binding.searchRecycler.setLayoutManager(layoutManager);
        adapter = new GridLoadMoreAdapter<>();
        adapter.setItemClickListener(new GridLoadMoreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieModel data) {
                Intent i =new Intent(getContext(), DetailActivity.class);
                i.putExtra("baseUrl",data.getBaseUrl());
                i.putExtra("name",data.getName());
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

            }
        });
        adapter.setLoadMoreListener(new GridLoadMoreAdapter.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!isLastPage && Utils.isNetworkAvailable(getContext())) {
                    loadMoreData();
                } else {
                    adapter.setLoading(false);
                    adapter.setError(true);
                }
            }
        });
        adapter.showShimmer(true);
        binding.searchRecycler.setAdapter(adapter);
    }
public void loadMoreData(){
        String url = "https://channelmyanmar.to/page/"+String.valueOf(currentPage)+"/?s="+keyword;
        service.execute(new SearchCm(helper,url,this));
}
    @Override
    public void onSuccess(ArrayList<MovieModel> data) {
        if (!isAdded())
            return;
        requireActivity().runOnUiThread(new Runnable() {
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
        if (!isAdded())
            return;
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
            }
        });

    }
}
