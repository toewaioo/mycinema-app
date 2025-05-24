package com.two.my_libs.widget;

import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.StyleRes;

public interface IThemeWidget {

    /**
     * 组装主题的信息到View的TAG中。
     *
     * @param view         View
     * @param attributeSet View在Inflate时的Attribute.
     */
    void assemble(View view, AttributeSet attributeSet);

    /**
     * view应用主题Theme
     *
     * @param view View
     */
    void applyTheme(View view);

    /**
     * View应用主题中的Style
     *
     * @param view
     * @param styleRes Style id
     */
    void applyStyle(View view, @StyleRes int styleRes);

}
