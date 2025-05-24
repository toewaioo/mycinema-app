package com.two.channelmyanmar.fragment.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.two.channelmyanmar.R;
import com.two.my_libs.base.BaseThemeFragment;

public class SerieDetail extends BaseThemeFragment {
    private static final String ARG_BASE_URL = "baseUrl";
    private String baseUrl;

    public SerieDetail() {
        // Required empty constructor
    }

    public static SerieDetail newInstance(String baseUrl) {
        SerieDetail fragment = new SerieDetail();
        Bundle args = new Bundle();
        args.putString(ARG_BASE_URL, baseUrl);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            baseUrl = getArguments().getString(ARG_BASE_URL);
            // Use baseUrl here
        }
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getChildFragmentManager().getBackStackEntryCount() > 1) {
                    // Pop the current child fragment to show the previous one.
                    getChildFragmentManager().popBackStack();
                } else {
                    // If only one fragment exists, disable callback and let the activity handle the back press.
                    setEnabled(false);
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout that contains the container for child fragments.
        return inflater.inflate(R.layout.series_detail_fragment_parent, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Add the initial ChildFragment.
        if (savedInstanceState == null) {
            addChildFragment(baseUrl);
        }
    }

    // Called by a ChildFragment to load a new child fragment.
    public void replaceChildFragment(String text) {

        addChildFragment(text);
    }

    // Helper method using add/hide so previous fragments remain in the container.
    private void addChildFragment(String text) {
        SeriesChildFragment newFragment = SeriesChildFragment.newInstance(text);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        // Find the current fragment (if any) and hide it.
        Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.series_fragment_parent_container);
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        // Add the new fragment with a unique tag.
        transaction.add(R.id.series_fragment_parent_container, newFragment, text);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
