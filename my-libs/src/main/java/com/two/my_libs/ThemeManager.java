package com.two.my_libs;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.LayoutInflaterCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.two.my_libs.base.CoverImageView;
import com.two.my_libs.custom.CoverImageWidget;
import com.two.my_libs.views.CuteIndicator;
import com.two.my_libs.widget.AbstractThemeWidget;
import com.two.my_libs.widget.BottomNaviView;
import com.two.my_libs.widget.BottomNavigationWidget;
import com.two.my_libs.widget.ButtonWidget;
import com.two.my_libs.widget.CuteIndicatorWidget;
import com.two.my_libs.widget.IThemeWidget;
import com.two.my_libs.widget.ImageViewWidget;
import com.two.my_libs.widget.LinearLayoutWidget;
import com.two.my_libs.widget.TextViewWidget;
import com.two.my_libs.widget.ToolBarWidget;
import com.two.my_libs.widget.ViewWidget;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * @Author: jeek
 * @Date: 2023/2/8
 * @Description:
 */

class ThemeManager {

    private static final String TAG = ThemeManager.class.getSimpleName();

    private final Map<Class<? extends View>, AbstractThemeWidget> themeWidgetMap;

    private final ThemeViewCreator themeViewCreator;

    private int appTheme;//-1;

    private final Application application;

    private final Set<IThemeObserver> themeObserverSet;

    ThemeManager(Application application) {
        this.application = application;
        this.themeWidgetMap = new HashMap<>();
        this.themeWidgetMap.put(CuteIndicator.class, new CuteIndicatorWidget());
        this.themeWidgetMap.put(FrameLayout.class, new BottomNavigationWidget());
        this.themeWidgetMap.put(View.class, new ViewWidget());
        this.themeWidgetMap.put(TextView.class, new TextViewWidget());
        this.themeWidgetMap.put(ImageView.class, new ImageViewWidget());
        this.themeWidgetMap.put(AppCompatButton.class, new ButtonWidget());
        //this.themeWidgetMap.put(CompoundButton.class, new CompoundButtonWidget());
        //  this.themeWidgetMap.put(ProgressBar.class, new ProgressBarWidget());
        //  this.themeWidgetMap.put(ListView.class, new ListViewWidget());
        //this.themeWidgetMap.put(SeekBar.class, new SeekBarWidget());
        this.themeWidgetMap.put(LinearLayout.class, new LinearLayoutWidget());
        //this.themeWidgetMap.put(AbsListView.class, new AbsListViewWidget());
        this.themeWidgetMap.put(Toolbar.class, new ToolBarWidget());
        this.themeWidgetMap.put(CoverImageView.class, new CoverImageWidget());

        this.themeObserverSet = new TreeSet<>();
        this.themeViewCreator = new ThemeViewCreator();
        this.appTheme = ThemePreferences.getAppTheme(this.application);

    }

    /**
     * 判断系统是否是暗黑模式
     *
     * @return true or false
     */
    boolean isSystemDarkMode() {
        return ThemeUtils.isSystemDarkMode(this.application);
    }

    /**
     * 获取设置的暗黑模式
     *
     * @return true or false
     */
    DarkMode getDarkMode() {
        return ThemePreferences.getDarkMode(this.application);
    }

    /**
     * 设置跟跟随系统颜色模式调整主题
     *
     * @param mode 跟随与否
     */
    void setDarkMode(DarkMode mode) {
        switch (mode) {
            case on:
                setAppTheme(MultiTheme.DARK_THEME);
                break;
            case followSystem:
                if (isSystemDarkMode()) {
                    setAppTheme(MultiTheme.DARK_THEME);
                } else {
                    setAppTheme(MultiTheme.sDefaultThemeIndex);
                }
                break;
            default:
                setAppTheme(MultiTheme.sDefaultThemeIndex);
        }
        ThemePreferences.setDarkMode(this.application, mode);
    }

    void addThemeWidget(Class<? extends View> widgetKey, AbstractThemeWidget themeWidget) {
        this.themeWidgetMap.put(widgetKey, themeWidget);
    }

    AbstractThemeWidget getThemeWidget(Class<? extends View> clazz) {
        Class<? extends View> widgetKey = findProperThemeWidgetKey(clazz);
        return this.themeWidgetMap.get(widgetKey);
    }

    void addObserver(IThemeObserver observer) {
        this.themeObserverSet.add(observer);
    }

    void removeObserver(IThemeObserver observer) {
        Iterator<IThemeObserver> obsIterator = this.themeObserverSet.iterator();
        while (obsIterator.hasNext()) {
            IThemeObserver obs = obsIterator.next();
            if (observer.equals(obs)) {
                obsIterator.remove();
                return;
            }
        }
    }

    void assembleThemeBeforeInflate(final AppCompatActivity activity) {

        if (activity == null) {
            throw new NullPointerException();
        }
        LayoutInflaterCompat.setFactory2(activity.getLayoutInflater(), new LayoutInflater.Factory2() {
            @Override
            public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attributeSet) {
               // MultiTheme.d(TAG, "onCreateView name:" + name);
                return onCreateView(null, name, context, attributeSet);
            }

            @Override
            public View onCreateView(View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
               // MultiTheme.d(TAG, "onCreateView parent:" + parent + ",name:" + name);
                AppCompatDelegate appCompatDelegate = activity.getDelegate();
                View view = appCompatDelegate.createView(parent, name, context, attrs);


                if (view == null) {
                    view = themeViewCreator.createView(parent, name, context, attrs);
                }

                if (view == null) {
                    return null;
                }

                Class<? extends View> widgetKey = findProperThemeWidgetKey(view.getClass());
                assembleViewThemeElement(view, attrs, widgetKey);

                return view;
            }
        });
    }

    private Class<? extends View> findProperThemeWidgetKey(Class<? extends View> clazz) {

        Class<? extends View> tmpKey = null;
        for (Class<? extends View> widgetKey : this.themeWidgetMap.keySet()) {

            if (clazz.equals(widgetKey)) {
                return clazz;
            }

            if (widgetKey.isAssignableFrom(clazz)) {
                if (tmpKey == null) {
                    tmpKey = widgetKey;
                } else {
                    if (tmpKey.isAssignableFrom(widgetKey)) {
                        tmpKey = widgetKey;
                    }
                }
            }
        }

        return tmpKey;

    }

    /**
     * 指定View的主题WidgetKey
     *
     * @return AbstractThemeWidget
     */
    AbstractThemeWidget addViewThemeWidgetKeyTag(View view) {
        if (view != null) {
            Class<? extends View> widgetKey = findProperThemeWidgetKey(view.getClass());
            view.setTag(R.id.amt_tag_widget_key, widgetKey);
            view.setTag(R.id.amt_tag_view_current_theme, getAppTheme());
            return this.themeWidgetMap.get(widgetKey);
        }
        return null;
    }

    /**
     * 组装每个View的主题元素
     */
    private void assembleViewThemeElement(View view, AttributeSet attributeSet, Class<? extends View> widgetKey) {

        //MultiTheme.d(TAG, "assembleViewThemeElement  theme widget type:" + widgetKey + ", view:" + view);
        if (view == null) {
            return;
        }

        IThemeWidget themeWidget = this.themeWidgetMap.get(widgetKey);
        if (themeWidget != null) {
            view.setTag(R.id.amt_tag_widget_key, widgetKey);
            view.setTag(R.id.amt_tag_view_current_theme, getAppTheme());
            if (attributeSet != null) {
                int styleResId = attributeSet.getStyleAttribute();
                if (styleResId != 0) {
                    view.setTag(R.id.amt_tag_widget_style, styleResId);
                }
                themeWidget.assemble(view, attributeSet);
            }
            //MultiTheme.d(TAG, "assembleViewThemeElement  theme widget type: " + widgetKey + " themeWidget:" + themeWidget.getClass().getSimpleName());
        } else {
            view.setTag(R.id.amt_tag_widget_key, null);
          //  MultiTheme.i(TAG, "unsupported theme widget type " + widgetKey + ",is your custom theme widget?");
        }
    }

    /**
     * 改变主题，在默认主题与夜间主题之间进行切换
     *
     * @return true 改变为夜间主题，false 改变为默认主题
     */
    boolean setAppTheme(int whichTheme) {
     //   MultiTheme.d(MultiTheme.TAG, "setAppTheme whichTheme=" + whichTheme);
        if (whichTheme > -1 && whichTheme != appTheme) {
            ThemePreferences.setAppTheme(this.application, whichTheme);
            appTheme = whichTheme;
            // applyTheme(this.application);
            for (IThemeObserver themeObserver : themeObserverSet) {
                themeObserver.onThemeChanged(whichTheme);
            }
            return true;
        } else {
            return false;
        }

    }

    int getAppTheme() {
        return this.appTheme;
    }

    void setDefaultTheme(int defaultThemeIndex) {
        if (ThemePreferences.getAppTheme(this.application) == -1) {
            setAppTheme(defaultThemeIndex);
        }
    }

    /**
     * 改变主题，在默认主题与夜间主题之间进行切换
     *
     * @param activity   Activity
     * @param themeResId 主题资源id
     */
    void applyTheme(Activity activity, @StyleRes int themeResId) {
        activity.setTheme(themeResId);
        applyTheme(activity);
    }

    private void applyTheme(Activity activity) {
       // MultiTheme.d(TAG, "applyThemeForActivity");
        if (activity == null) {
            throw new IllegalArgumentException("activity  is null!");
        }

        View decorView = activity.getWindow().getDecorView();
        applyTheme(decorView);

    }

    /**
     * 对单个View应用主题
     *
     * @param view View
     */
    void applyTheme(View view) {

        int themeOfView = -1;
        int styleResOfView = -1;
        try {
            themeOfView = ThemeUtils.getViewTag(view, R.id.amt_tag_view_current_theme);
            styleResOfView = ThemeUtils.getViewTag(view, R.id.amt_tag_widget_style);
        } catch (ClassCastException | NullPointerException ignored) {
        }

        if (themeOfView == appTheme) {
            return;
        }

        Class<? extends View> widgetKey = getThemeWidgetKey(view);

        //MultiTheme.d(TAG, "applyTheme  theme widget type:" + widgetKey + " ,view:" + view);
        IThemeWidget themeWidget = this.themeWidgetMap.get(widgetKey);
        if (themeWidget != null) {
            if (styleResOfView != -1) {
                themeWidget.applyStyle(view, styleResOfView);
            }
            themeWidget.applyTheme(view);
           // MultiTheme.d(TAG, "applyTheme  theme widget type: " + widgetKey + " ,view:" + view + " themeWidget:" + themeWidget);
        } else {
           // MultiTheme.i(TAG, "applyTheme unsupport theme widget type:" + widgetKey + ", view:" + view);
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                applyTheme(child);
            }
        }

        view.setTag(R.id.amt_tag_view_current_theme, getAppTheme());
    }

    @SuppressWarnings("unchecked")
    Class<? extends View> getThemeWidgetKey(View view) {
        return (Class<? extends View>) view.getTag(R.id.amt_tag_widget_key);
    }
}
