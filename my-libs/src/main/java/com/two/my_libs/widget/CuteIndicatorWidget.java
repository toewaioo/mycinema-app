package com.two.my_libs.widget;

import android.util.Log;
import android.view.View;

import com.two.my_libs.R;
import com.two.my_libs.annotation.MultiThemeAttrs;
import com.two.my_libs.views.CuteIndicator;


public class CuteIndicatorWidget extends ViewWidget{
    @Override
    public void applyElementTheme(View view, int themeElementKey, int resId) {
        super.applyElementTheme(view, themeElementKey, resId);
        CuteIndicator indicator = (CuteIndicator) view;
        Log.d("CuteIndicator","Working..."+String.valueOf(themeElementKey)+":"+String.valueOf(R.attr.IndicatorColor));
        if (themeElementKey == R.attr.IndicatorColor){
            indicator.setIndicatorColor(resId);
            Log.d("CuteIndicator","Working...");
        }
    }
}
