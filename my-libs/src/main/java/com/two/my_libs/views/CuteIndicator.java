package com.two.my_libs.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.viewpager.widget.ViewPager;

import com.two.my_libs.R;


public class CuteIndicator extends LinearLayout {

    private int itemCount = 0;

    private float selectedWidth = 0f;

    private float dia = 0f;

    private float space = 0f;

    private float shadowRadius = 0f;

    private RectF rectf = new RectF();

    private Paint paint;

    private float lastPositionOffset = 0f;

    private int firstVisiblePosition = 0;

    private int indicatorColor = 0xffffffff;

    private int shadowColor = 0x88000000;

    private boolean isAnimation = true;

    private boolean isShadow = true;

    public CuteIndicator(Context context) {
        super(context);
        init(null, 0);
    }

    public CuteIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CuteIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        // Default values
        selectedWidth = dp2px(20f);
        dia = dp2px(10f);
        space = dp2px(5f);
        shadowRadius = dp2px(2f);

        setWillNotDraw(false);

        // Load attributes
        if (attrs != null) {
            Context context = getContext();
            TypedArray a = context.obtainStyledAttributes(
                    attrs, R.styleable.CuteIndicator, defStyle, 0);

            indicatorColor = a.getColor(R.styleable.CuteIndicator_IndicatorColor, indicatorColor);
            shadowColor = a.getColor(R.styleable.CuteIndicator_IndicatorShadowColor, shadowColor);
            selectedWidth = a.getDimension(R.styleable.CuteIndicator_IndicatorSelectedWidthDimension, selectedWidth);
            dia = a.getDimension(R.styleable.CuteIndicator_IndicatorDiaDimension, dia);
            space = a.getDimension(R.styleable.CuteIndicator_IndicatorSpaceDimension, space);
            shadowRadius = a.getDimension(R.styleable.CuteIndicator_IndicatorShadowRadiusDimension, shadowRadius);
            isAnimation = a.getBoolean(R.styleable.CuteIndicator_IndicatorIsAnimation, isAnimation);
            isShadow = a.getBoolean(R.styleable.CuteIndicator_IndicatorIsShadow, isShadow);

            a.recycle();
        }

        if (isShadow) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor((int) indicatorColor);
        paint.setStyle(Paint.Style.FILL);
        if (isShadow) {
            paint.setShadowLayer(shadowRadius, shadowRadius / 2, shadowRadius / 2, (int) shadowColor);
        }
    }
    public void setIndicatorColor(int color) {
        this.indicatorColor = color;
        paint.setColor(indicatorColor);
        invalidate();
    }

    public void setupWithViewPager(ViewPager viewPager) {
        if (viewPager.getAdapter() == null) {
            throw new IllegalArgumentException("ViewPager adapter must not be null");
        }

        itemCount = viewPager.getAdapter().getCount();

        if (isShadow) {
            getLayoutParams().width = (int) ((itemCount - 1) * (space + dia) + selectedWidth + shadowRadius);
            getLayoutParams().height = (int) (dia + shadowRadius);
        } else {
            getLayoutParams().width = (int) ((itemCount - 1) * (space + dia) + selectedWidth);
            getLayoutParams().height = (int) dia;
        }

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (isAnimation) {
                    firstVisiblePosition = position;
                    lastPositionOffset = positionOffset;
                    invalidate();
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (!isAnimation) {
                    firstVisiblePosition = position;
                    invalidate();
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || itemCount == 0) {
            return;
        }

        for (int i = 0; i < itemCount; i++) {
            float left;
            float right;

            if (i < firstVisiblePosition) {
                left = i * (dia + space);
                right = left + dia;
            } else if (i == firstVisiblePosition) {
                left = i * (dia + space);
                right = left + dia + (selectedWidth - dia) * (1 - lastPositionOffset);
            } else if (i == firstVisiblePosition + 1) {
                left = (i - 1) * (space + dia) + dia + (selectedWidth - dia) * (1 - lastPositionOffset) + space;
                right = i * (space + dia) + selectedWidth;
            } else {
                left = (i - 1) * (dia + space) + (selectedWidth + space);
                right = (i - 1) * (dia + space) + (selectedWidth + space) + dia;
            }

            float top = 0f;
            float bottom = dia;

            rectf.left = left;
            rectf.top = top;
            rectf.right = right;
            rectf.bottom = bottom;

            canvas.drawRoundRect(rectf, dia / 2, dia / 2, paint);
        }
    }

    private float dp2px(float dpValue) {
        return dpValue * getResources().getDisplayMetrics().density;
    }


}



