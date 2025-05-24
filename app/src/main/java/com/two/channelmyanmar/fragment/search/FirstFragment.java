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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.two.channelmyanmar.DetailActivity;
import com.two.channelmyanmar.R;
import com.two.channelmyanmar.TagActivity;
import com.two.channelmyanmar.adapter.GenresAdapter;
import com.two.channelmyanmar.adapter.MyAdapter;
import com.two.channelmyanmar.databinding.SearchFirstFragmentBinding;
import com.two.channelmyanmar.fragment.detail.MovieChildFragment;
import com.two.channelmyanmar.model.Genres;
import com.two.channelmyanmar.model.MovieModel;
import com.two.channelmyanmar.view.ExpandableListView;
import com.two.channelmyanmar.viewmodel.ApiViewModel;
import com.two.my_libs.base.BaseThemeFragment;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Toewaioo on 4/8/25
 * Description: [Add class description here]
 */
public class FirstFragment extends BaseThemeFragment {
    private final String TAG = this.getClass().getSimpleName();
    SearchFirstFragmentBinding binding;
    ApiViewModel viewModel;
    GenresAdapter adapter;
    ExpandableListView expandableListView;
    MyAdapter randomAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SearchFirstFragmentBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ApiViewModel.class);
        return binding.getRoot();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getLiveData(ApiViewModel.ApiType.NEW_RELEASE)
                .observe(getViewLifecycleOwner(), result -> {
                    if (result instanceof List) {
                        binding.progressBar.setVisibility(View.GONE);
                        ArrayList<MovieModel> random = (ArrayList<MovieModel>) result;
                        randomAdapter = new MyAdapter(random, new MyAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(MovieModel data) {
                                Intent i = new Intent(getContext(), DetailActivity.class);
                                i.putExtra("baseUrl", data.getBaseUrl());
                                i.putExtra("name", data.getName());
                                startActivity(i);
                                //Toast.makeText(getContext(), data.getBaseUrl(), Toast.LENGTH_SHORT).show();

                            }
                        });
                        binding.contentRandom.setLayoutManager(new GridLayoutManager(getContext(), 3));
                        binding.contentRandom.setAdapter(randomAdapter);
                        randomAdapter.setShowShimmer(false);
//                        newReleaseAdapter.setShowShimmer(false);
//                        newReleaseAdapter.updateData(random);
//                        newReleaseAdapter.startAutoScroll();
                    }
                });
        viewModel.getLiveData(ApiViewModel.ApiType.GENRES)
                .observe(getViewLifecycleOwner(), result -> {
                    if (result instanceof List) {
                        ArrayList<Genres> genres = (ArrayList<Genres>) result;
                        adapter = new GenresAdapter(genres, new GenresAdapter.CallBack() {
                            @Override
                            public void onClickG(String data) {
                                Intent i = new Intent(getContext(), TagActivity.class);
                                i.putExtra("tag", getTag(data));
                                i.putExtra("url", data);
                                startActivity(i);
                                //Toast.makeText(getContext(), data, Toast.LENGTH_SHORT).show();

                            }
                        }, getContext());
                        binding.genreslist.setAdapter(adapter);

                    }
                });
        binding.searchEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!binding.searchEdittext.getText().toString().isEmpty()) {
                        Toast.makeText(getContext(), binding.searchEdittext.getText().toString(), Toast.LENGTH_SHORT).show();
                        // helper.saveString(binding.searchEdittext.getText().toString());
                        addChildFragment("");
                        return true;
                    }
                }
                return false;
            }
        });
        binding.goSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addChildFragment("hello");
            }
        });
        binding.goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    public void replaceChildFragment(String text) {

        addChildFragment(text);
    }

    // Helper method using add/hide so previous fragments remain in the container.
    private void addChildFragment(String text) {
        SecondFragment newFragment = SecondFragment.newInstance(binding.searchEdittext.getText().toString());
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);

        // Add the new fragment with a unique tag.
        transaction.replace(R.id.search_container, newFragment, text);
        transaction.addToBackStack("second");
        transaction.commit();
    }

    public String getTag(String s) {
        String first = s.substring(0, s.length() - 1);
        return first.substring(first.lastIndexOf("/") + 1, first.length()).toUpperCase();

    }
}
