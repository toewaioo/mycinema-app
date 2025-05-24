package com.two.my_libs.base;


import androidx.fragment.app.Fragment;

import com.two.my_libs.ThemeViewEntities;

public class BaseThemeFragment extends Fragment {

    private ThemeViewEntities themeViewEntities = new ThemeViewEntities();

    public ThemeViewEntities getThemeViewEntities() {
        return themeViewEntities;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        themeViewEntities.clear();
    }
}
