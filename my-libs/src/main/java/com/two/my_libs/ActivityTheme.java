package com.two.my_libs;


import static com.two.my_libs.ThemeUtils.IS_KITKAT;
import static com.two.my_libs.ThemeUtils.IS_LOLLIPOP;
import static com.two.my_libs.ThemeUtils.getStatusBarHeight;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;


/**
 * @Author: jeek
 * @Date: 2023/2/8
 * @Description:
 */

public class ActivityTheme implements IThemeObserver {

    private AppCompatActivity activity;

    @StyleRes
    private int[] themes;

    private int darkThemeIndex = -1;

    private boolean isSupportMenuItemEnable = false;

    @AttrRes
    private int statusBarColorAttrResId = 0;

    private View statusBarPlaceHolder = null;

    public ActivityTheme(AppCompatActivity activity) {
        this.activity = activity;
    }

    /**
     * 设置主题资源数组
     *
     * @param themes 主题资源数组
     */
    public final void setThemes(@StyleRes int[] themes) {
        this.themes = themes;
    }

    /**
     * 设置支持暗黑模式的主题资源数组
     *
     * @param darkThemeIndex 暗黑主题在主题资源数组中的位置
     * @param themes         主题资源数组
     */
    public final void setThemes(int darkThemeIndex, @StyleRes int[] themes) {
        this.themes = themes;
        this.darkThemeIndex = darkThemeIndex;
        if (this.darkThemeIndex < 0 || this.darkThemeIndex >= (this.themes.length - 1)) {
            throw new IllegalArgumentException("please check you param,there ara some error.");
        }
    }

    /**
     * 开启主题切换支持菜单功能
     *
     * @param enable
     */
    public final void setSupportMenuItemThemeEnable(boolean enable) {
        this.isSupportMenuItemEnable = enable;
    }

    /**
     * 设置通知栏的颜色，4.4以下的设备无效
     *
     * @param statusBarColorAttrResId 指向颜色值的AttrRes
     */
    public final void setStatusBarColorAttrRes(@AttrRes int statusBarColorAttrResId) {
        this.statusBarColorAttrResId = statusBarColorAttrResId;
    }

    public final void setStatusBarColor(@ColorInt int statusBarColor) {
        if (!IS_KITKAT) {
            return;
        }

        if (IS_LOLLIPOP) {
            setStatusBarColorOnLollipop(statusBarColor);
        } else if (IS_KITKAT) {
            setStatusBarColorKitKat(statusBarColor);
        }
    }

    public void assembleThemeBeforeInflate() {
        MultiTheme.addObserver(this);
        int whichTheme = MultiTheme.getAppTheme();
        int theme = getTheme(whichTheme);
        if (theme > 0) {
            activity.setTheme(theme);
        }
        MultiTheme.assembleThemeBeforeInflate(activity);
       // Log.d(this.getClass().getSimpleName(),"Activity theme working..");
        initializeStatusBarTheme();
    }

    private void initializeStatusBarTheme() {
       // MultiTheme.d(MultiTheme.TAG, "initializeStatusBarTheme sdk version:" + Build.VERSION.SDK_INT);
        if (!IS_KITKAT || statusBarColorAttrResId == 0) {
            return;
        }

        int statusColor = ThemeUtils.getColor(this.activity, statusBarColorAttrResId);
        if (IS_LOLLIPOP) {
            initializeStatusBarColorOnLollipop(statusColor);
        } else {
            initializeStatusBarColorKitKat(statusColor);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initializeStatusBarColorKitKat(int statusBarColor) {
        //MultiTheme.d(MultiTheme.TAG, "setStatusBarColorKitkat");
        int statusBarHeight = getStatusBarHeight(this.activity);
        Window window = activity.getWindow();
        ViewGroup mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);

        //First translucent status bar.
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        ViewGroup actionBarRoot = (ViewGroup) mContentView.getParent();

        statusBarPlaceHolder = new View(activity);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight);
        //向 ContentView 中添加假 View
        actionBarRoot.addView(statusBarPlaceHolder, 0, lp);
        setStatusBarColorKitKat(statusBarColor);
    }

    private void setStatusBarColorKitKat(int statusBarColor) {
        if (statusBarPlaceHolder != null) {
            statusBarPlaceHolder.setBackgroundColor(statusBarColor);
        }
    }


    private void initializeStatusBarColorOnLollipop(int statusBarColor) {
        // Log the action (using a custom logging method from MultiTheme)
       // MultiTheme.d(MultiTheme.TAG, "setStatusBarColorOnLollipop");

        Window window = activity.getWindow();

        // Clear translucent status flag so that our content does not extend under the status bar
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // Enable drawing system bar backgrounds, required for setting the status bar color
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // Set the status bar color
        window.setStatusBarColor(statusBarColor);

        // Only proceed if we're running on API 23 or higher (for setting icon colors)
        // Calculate brightness using a standard luminance formula
        // The formula: brightness = 0.299 * R + 0.587 * G + 0.114 * B

    }


//        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
//            // Dark mode is active
//        } else if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
//            // Light mode is active
//        } else {
//            // Unknown or undefined mode
//        }

//        final int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
//        if (lightStatusEnabled) {
//            window.getDecorView().setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        } else {
//            window.getDecorView().setSystemUiVisibility(systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        }
 //   }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColorOnLollipop(int statusBarColor) {

        Window window = activity.getWindow();
        window.setStatusBarColor(statusBarColor);
    }

    public void destroy() {
        MultiTheme.removeObserver(this);
        this.activity = null;
    }

    @Override
    public int getPriority() {
        return PRIORITY_ACTIVITY;
    }

    @Override
    public final void onThemeChanged(int whichTheme) {
        MultiTheme.applyTheme(this.activity, getTheme(whichTheme));
        changeStatusBarColor();
        if (isSupportMenuItemEnable) {
            this.activity.supportInvalidateOptionsMenu();
        }
    }

    private void changeStatusBarColor() {
        int whichTheme = MultiTheme.getAppTheme();
        MultiTheme.applyTheme(this.activity,getTheme(whichTheme));
        if (statusBarColorAttrResId != 0) {
            int statusBarColor = ThemeUtils.getColor(this.activity, statusBarColorAttrResId);
            setStatusBarColor(statusBarColor);
            Window window = activity.getWindow();

            int red = Color.red(statusBarColor);
            int green = Color.green(statusBarColor);
            int blue = Color.blue(statusBarColor);
            double brightness = (0.299 * red + 0.587 * green + 0.114 * blue);

            View decorView = window.getDecorView();

            // If brightness is low, the color is dark, so use light icons (clear the flag)
            // Otherwise, if brightness is high, the color is light, so use dark icons (set the flag)
            final int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
            if (brightness < 128) {
                // Dark background: light icons
                decorView.setSystemUiVisibility(systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

                   } else {
                // Light background: dark icons
                decorView.setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

            }
        }
    }

    private int getTheme(int index) {
        if (this.themes == null) {
            MultiTheme.e(MultiTheme.TAG, "There is no theme array.");
            return -1;
        }

        if (index == MultiTheme.DARK_THEME) {
            return getDarkTheme();
        } else {
            return getNormalTheme(index);
        }
    }

    private int getNormalTheme(int index) {
        //MultiTheme.d(MultiTheme.TAG, "getNormalTheme index:" + index);
        if (index > this.themes.length || index < 0) {
           // MultiTheme.e(MultiTheme.TAG, "OutOfBound. we use the first theme.");
            return this.themes[0];
        }
        return this.themes[index];
    }

    private int getDarkTheme() {
        MultiTheme.d(MultiTheme.TAG, "getDarkTheme");
        if (darkThemeIndex < 0 || darkThemeIndex >= this.themes.length) {
          //  MultiTheme.e(MultiTheme.TAG, "You forgot setup a darkThemeIndex, we use the first theme indeed.");
            return this.themes[0];
        }
        return this.themes[this.darkThemeIndex];
    }

    @Override
    public int compareTo(IThemeObserver o) {
        return getPriority() > o.getPriority() ? 1 : -1;
    }
}
