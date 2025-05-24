package com.two.my_libs.shimmer;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import android.animation.ValueAnimator;
import android.content.Context;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;

import android.os.Build;

public class ShimmerTextViewDrawable extends Drawable implements ValueAnimator.AnimatorUpdateListener {

    private final Paint textPaint;
    private String text;
    private ValueAnimator animator;
    private LinearGradient shimmerGradient;
    private final Matrix gradientMatrix = new Matrix();
    private float shimmerTranslate; // value between 0 and 1
    private int width, height;
    private int shimmerDuration = 1000; // default duration in milliseconds
    private Typeface typeface;

    public ShimmerTextViewDrawable(Context context, String text) {
        this.text = text;
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(50); // default text size; can be changed via setTextSize()
        textPaint.setTextAlign(Paint.Align.CENTER);
        initAnimator();
    }

    private void initAnimator() {
        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(shimmerDuration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(this);
        animator.start();
    }

    // Dynamic text setter/getter
    public void setText(String text) {
        this.text = text;
        invalidateSelf();
    }

    public String getText() {
        return text;
    }

    // Allow dynamic text size adjustment
    public void setTextSize(float textSize) {
        textPaint.setTextSize(textSize);
        invalidateSelf();
    }
    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
        textPaint.setTypeface(typeface);
        invalidateSelf();
    }

    // Allow dynamic shimmer duration adjustment
    public void setShimmerDuration(int durationMillis) {
        shimmerDuration = durationMillis;
        if (animator != null) {
            animator.setDuration(shimmerDuration);
        }
    }

    // Animation control methods

    // Pause the animation if running
    public void pauseAnimation() {
        if (animator != null && animator.isRunning()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                animator.pause();
            } else {
                // For older APIs, cancel and reinitialize later if needed
                animator.cancel();
            }
        }
    }

    // Resume the animation if paused
    public void resumeAnimation() {
        if (animator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                animator.resume();
            } else if (!animator.isRunning()) {
                // For older APIs, reinitialize the animator
                initAnimator();
            }
        }
    }

    // Stop the animation entirely
    public void stopAnimation() {
        if (animator != null) {
            animator.cancel();
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        width = bounds.width();
        height = bounds.height();

        // Create a horizontal gradient across the width
        shimmerGradient = new LinearGradient(
                0, 0, width, 0,
                new int[]{Color.LTGRAY, Color.WHITE, Color.LTGRAY},
                new float[]{0, 0.5f, 1},
                Shader.TileMode.CLAMP);
        textPaint.setShader(shimmerGradient);
    }

    @Override
    public void draw(Canvas canvas) {
        if (shimmerGradient != null) {
            // Update the gradient's matrix to animate the shimmer effect
            gradientMatrix.setTranslate(shimmerTranslate * width, 0);
            shimmerGradient.setLocalMatrix(gradientMatrix);
        }

        // Compute vertical centering of the text
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float textOffset = (textHeight / 2) - fm.descent;

        canvas.drawText(text, width / 2f, height / 2f + textOffset, textPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        textPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        textPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        shimmerTranslate = (float) animation.getAnimatedValue();
        invalidateSelf();
    }
}

