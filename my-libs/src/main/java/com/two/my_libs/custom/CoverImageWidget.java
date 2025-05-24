package com.two.my_libs.custom;

import android.view.View;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;

import com.two.my_libs.R;
import com.two.my_libs.base.CoverImageView;
import com.two.my_libs.widget.ImageViewWidget;


public class CoverImageWidget extends ImageViewWidget {

    @Override
    protected void initializeLibraryElements() {
        super.initializeLibraryElements();
        addThemeElementKey(R.attr.coverColor);
    }

    @Override
    public void applyElementTheme(View view, @AttrRes int themeElementKey, @AnyRes int resId) {
        super.applyElementTheme(view, themeElementKey, resId);
        CoverImageView coverImageView = (CoverImageView) view;
        if (R.attr.coverColor == themeElementKey) {
            setCoverColor(coverImageView, resId);
        }
    }

    public static void setCoverColor(CoverImageView coverImageView, @ColorRes int colorResId) {

        if (coverImageView == null) {
            return;
        }

        coverImageView.setCoverColor(getColor(coverImageView, colorResId));
    }
}
