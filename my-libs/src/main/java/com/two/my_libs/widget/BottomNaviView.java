package com.two.my_libs.widget;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.View;

import com.two.my_libs.MultiTheme;
import com.two.my_libs.R;
import com.two.my_libs.annotation.MultiThemeAttrs;
import com.two.my_libs.bottomnavigation.NavigationView;


public class BottomNaviView extends ViewWidget{
    @Override
    protected void initializeLibraryElements() {
        super.initializeLibraryElements();
        addThemeElementKey(R.attr.activeTextColor);
        addThemeElementKey(R.attr.unactiveTextColor);
    }

    @Override
    public void applyElementTheme(View view, int themeElementKey, int resId) {
        super.applyElementTheme(view, themeElementKey, resId);
        Log.d(this.getClass().toString(),"working...");
        try {
            NavigationView navigationView = (NavigationView)view;
            if(MultiTheme.getAppTheme()==0){
                navigationView.setActiveTextColor(Color.RED);
                navigationView.setUnactiveTextColor(Color.BLACK);

            }else {
                navigationView.setActiveTextColor(Color.RED);
                navigationView.setUnactiveTextColor(Color.WHITE);
            }
        }catch (Exception e){

        }


//        //}
       // navigationView.refreshLayout();
    }
}
