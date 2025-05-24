package com.two.channelmyanmar.fragment.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.two.channelmyanmar.databinding.DefaultFragmentBinding;
import com.two.my_libs.base.BaseThemeFragment;

/*
 * Created by Toewaioo on 4/20/25
 * Description: [Add class description here]
 */
public class DefaultFragment extends BaseThemeFragment {
    private final String TAG = this.getClass().getSimpleName();
    private DefaultFragmentBinding binding;


    public DefaultFragment() {
        // constructor code
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DefaultFragmentBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
}
