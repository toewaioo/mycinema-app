package com.two.my_libs.widget;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;

import com.two.my_libs.annotation.MultiThemeAttrs;


@MultiThemeAttrs({
        android.R.attr.src,
})
public class ImageViewWidget extends ViewWidget {

    @Override
    protected void initializeLibraryElements() {
        super.initializeLibraryElements();
        addThemeElementKey(androidx.appcompat.R.attr.srcCompat);
    }

    @Override
    public void applyElementTheme(View view, @AttrRes int themeElementKey, @AnyRes int resId) {
        Log.d(this.getClass().toString(),"working...");
        super.applyElementTheme(view, themeElementKey, resId);
        ImageView imageView = (ImageView) view;
        if (themeElementKey == android.R.attr.src) {
            setImageDrawable(imageView, resId);
        } else if (themeElementKey == androidx.appcompat.R.attr.srcCompat) {
            setImageCompatDrawable(imageView, resId);
        }
    }

    public static void setImageDrawable(ImageView imageView, @DrawableRes int drawableResId) {
        if (imageView == null) {
            return;
        }

        imageView.setImageDrawable(getDrawable(imageView, drawableResId));
    }

    public static void setImageCompatDrawable(ImageView imageView, @DrawableRes int drawableResId) {
        if (imageView == null) {
            return;
        }

        imageView.setImageDrawable(getDrawable(imageView, drawableResId));
    }

}
