package com.two.my_libs.widget;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.Toolbar;

import com.two.my_libs.IThemeObserver;
import com.two.my_libs.MultiTheme;

public class ToolBarWidget extends ViewWidget implements IThemeObserver {

    @Override
    protected void initializeLibraryElements() {
        super.initializeLibraryElements();
        addThemeElementKey(androidx.appcompat.R.attr.logo);
        addThemeElementKey(androidx.appcompat.R.attr.navigationIcon);
        addThemeElementKey(androidx.appcompat.R.attr.subtitleTextColor);
        addThemeElementKey(androidx.appcompat.R.attr.titleTextColor);
    }

    @Override
    public void applyElementTheme(View view, @AttrRes int themeElementKey, @AnyRes int resId) {
        super.applyElementTheme(view, themeElementKey, resId);
        Log.d(this.getClass().toString(),"working...");
        Toolbar toolbar = (Toolbar) view;
        if (android.R.attr.logo == themeElementKey) {
            setLogo(toolbar, resId);
        } else if (androidx.appcompat.R.attr.subtitleTextColor == themeElementKey) {
            setSubTitleTextColor(toolbar, resId);
        } else if (androidx.appcompat.R.attr.navigationIcon == themeElementKey) {
            setNavigationIcon(toolbar, resId);
        } else if (androidx.appcompat.R.attr.titleTextColor == themeElementKey) {
            setTitleTextColor(toolbar, resId);
        }
        if(MultiTheme.getAppTheme()==0){
            toolbar.getOverflowIcon().setColorFilter(view.getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_ATOP);
            toolbar.setTitleTextColor(view.getResources().getColor(android.R.color.black));
            Log.d("Applying toolbar theme","Workingggggg....."+themeElementKey+":");
        }else {
            toolbar.setTitleTextColor(view.getResources().getColor(android.R.color.white));
            toolbar.getOverflowIcon().setColorFilter(view.getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        }
     //        toolbar.getOverflowIcon().setColorFilter(view.getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);

   }

    public static void setLogo(Toolbar toolBar, @DrawableRes int drawableResId) {
        if (toolBar == null) {
            return;
        }

        Drawable logoDrawable = getDrawable(toolBar, drawableResId);
        toolBar.setLogo(logoDrawable);
    }

    public static void setNavigationIcon(Toolbar toolBar, @DrawableRes int drawableResId) {
        if (toolBar == null) {
            return;
        }

        Drawable iconDrawable = getDrawable(toolBar, drawableResId);
        toolBar.setNavigationIcon(iconDrawable);
    }

    public static void setTitleTextColor(Toolbar toolBar, @ColorRes int colorResId) {

        if (toolBar == null) {
            return;
        }

        toolBar.setTitleTextColor(getColor(toolBar, colorResId));
    }

    public static void setSubTitleTextColor(Toolbar toolBar, @ColorRes int colorResId) {

        if (toolBar == null) {
            return;
        }

        toolBar.setSubtitleTextColor(getColor(toolBar, colorResId));
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void onThemeChanged(int whichTheme) {


    }

    @Override
    public int compareTo(IThemeObserver o) {
        return 0;
    }
}