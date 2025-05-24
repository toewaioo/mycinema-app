package com.two.my_libs.widget;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;

import com.two.my_libs.R;
import com.two.my_libs.annotation.MultiThemeAttrs;


@MultiThemeAttrs({
        android.R.attr.divider
})
public class LinearLayoutWidget extends ViewWidget {
    @Override
    public void applyElementTheme(View view, @AttrRes int themeElementKey, @AnyRes int resId) {

        super.applyElementTheme(view, themeElementKey, resId);
        Log.d("CuteIndicator","Working..."+String.valueOf(themeElementKey)+":"+String.valueOf(R.attr.IndicatorColor));

        LinearLayout linearLayout = (LinearLayout) view;
        if (themeElementKey == android.R.attr.divider) {
            setDividerDrawable(linearLayout, resId);
        }
    }

    public static void setDividerDrawable(LinearLayout linearLayout, @DrawableRes int drawableResId) {
        if (linearLayout == null) {
            return;
        }

        linearLayout.setDividerDrawable(getDrawable(linearLayout,drawableResId));
    }
}
