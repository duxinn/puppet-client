package com.mango.puppetsystem.floatball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.mango.puppetsystem.R;

public class FloatBall extends View {


    public int mWidth = 100;

    public int mHeight = 100;

    private String mText = "";

    private Paint ballPaint;

    private Paint textPaint;

    private int textColor;

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        postInvalidate();
    }

    public void setmText(String mText) {
        this.mText = mText;
    }

    public void setmWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public void setmHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public FloatBall(Context context) {
        super(context);
        init();
    }

    public FloatBall(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatBall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        ballPaint = new Paint();
        textPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        ballPaint.setColor(Color.GRAY);
        ballPaint.setAntiAlias(true);
        textPaint.setTextSize(getResources().getDimension(R.dimen.dimens_10_sp));
        textPaint.setColor(textColor);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);
        canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2, ballPaint);
        float textWidth = textPaint.measureText(mText);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float dy = -(fontMetrics.descent + fontMetrics.ascent) / 2;
        canvas.drawText(mText, mWidth / 2 - textWidth / 2, mHeight / 2 + dy, textPaint);
    }

    //设置当前移动状态
    public void setDragState() {
        invalidate();
    }
}
