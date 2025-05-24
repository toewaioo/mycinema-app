package com.two.my_libs.widget;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;

import com.two.my_libs.annotation.MultiThemeAttrs;


@MultiThemeAttrs({
        android.R.attr.background
})
public class ViewWidget extends AbstractThemeWidget {

    @Override
    protected void initializeLibraryElements() {
        super.initializeLibraryElements();
    }

    @Override
    public void applyElementTheme(View view, @AttrRes int themeElementKey, @AnyRes int resId) {
        super.applyElementTheme(view, themeElementKey, resId);
        switch (themeElementKey) {
            case android.R.attr.background:
                setBackground(view, resId);
                break;
        }
    }

    @SuppressWarnings("NewApi")
    public static void setBackground(View view, @AnyRes int resId) {
        if (view == null) {
            return;
        }
        Log.d("ViewWidget","setBackground");

        Drawable background = getDrawable(view, resId);


            view.setBackground(background);
    }

}
