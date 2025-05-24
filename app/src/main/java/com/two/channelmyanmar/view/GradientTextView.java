package com.two.channelmyanmar.view;

/*
 * Created by Toewaioo on 4/8/25
 * Description: [Add class description here]
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class GradientTextView extends AppCompatTextView {

    public GradientTextView(Context context) {
        super(context);
    }

    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GradientTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Get the text content and text size
        String text = getText().toString();
        float textSize = getTextSize();

        // Create a paint object for drawing text
        Paint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.setTextSize(textSize);

        // Create a gradient shader for the text color
        LinearGradient shader = new LinearGradient(
                0, 0, getWidth(), textSize,
                new int[]{0xFF0099CC, 0xFFAA66CC}, // Gradient colors
                null,
                Shader.TileMode.CLAMP);

        // Set the shader to the paint
        paint.setShader(shader);

        // Draw the text with gradient color
        canvas.drawText(text, 0, textSize, paint);
    }
}

