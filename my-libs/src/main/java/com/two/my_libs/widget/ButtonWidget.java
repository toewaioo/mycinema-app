package com.two.my_libs.widget;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;

import com.two.my_libs.annotation.MultiThemeAttrs;

@MultiThemeAttrs({
        android.R.attr.textColor,
        android.R.attr.textColorHint,
        android.R.attr.textColorLink,
        android.R.attr.drawableBottom,
        android.R.attr.drawableTop,
        android.R.attr.drawableLeft,
        android.R.attr.drawableRight,
        android.R.attr.background,
        android.R.attr.textAllCaps  // Optional: controls whether text is displayed in all caps
})
public class ButtonWidget extends ViewWidget {
    private static final String TAG = "ButtonWidget";

    @SuppressLint("RestrictedApi")
    @Override
    public void applyElementTheme(View view, @AttrRes int themeElementKey, @AnyRes int resId) {
        Log.d(this.getClass().toString(),"working...");
                if (view instanceof Button) {
            Button button = (Button) view;
            // Example: update background using a drawable from the theme
            Drawable drawable = getDrawable(view, resId);
            if (drawable != null) {
                button.setBackground(drawable);
                button.invalidate();
                button.requestLayout();
            }
         //You might also want to update text color, etc.
         }
        super.applyElementTheme(view, themeElementKey, resId);

    }


}
