package com.two.my_libs.widget;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.two.my_libs.MultiTheme;
import com.two.my_libs.bottomnavigation.NavigationView;

public class BottomNavigationWidget extends ViewWidget{
    @Override
    public void applyElementTheme(View view, int themeElementKey, int resId) {
        super.applyElementTheme(view, themeElementKey, resId);
        Log.d(this.getClass().toString(),"working...");
        try {
            BottomNavigationView navigationView = (BottomNavigationView) view;
            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_checked},new int[]{android.R.attr.state_checked}
            };

            if(MultiTheme.getAppTheme()==0){
                int[] colors = new int[]{
                        Color.GRAY,Color.BLUE
                };
                ColorStateList colorStateList = new ColorStateList(states,colors);
                navigationView.setItemTextColor(colorStateList);
                navigationView.setItemIconTintList(colorStateList);

            }else {
                int[] colors = new int[]{
                        Color.WHITE,Color.YELLOW
                };
                ColorStateList colorStateList = new ColorStateList(states,colors);
                navigationView.setItemTextColor(colorStateList);
                navigationView.setItemIconTintList(colorStateList);
            }
        }catch (Exception e){

        }
    }
}
