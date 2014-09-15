package com.weidongjian.weigan.sunshine.service;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.weidongjian.weigan.sunshine.R;

/**
 * Created by Weigan on 2014/9/15.
 */
public class TestView extends View {

    public TestView(Context context) {
        super(context);
    }

    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int mRimRadius;
        int mCenterX, mCenterY;
        Paint circlePaint = new Paint();
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        if (height > width) {
            mRimRadius = width / 2;
        }else  {
            mRimRadius = height / 2;
        }
        mCenterX = mRimRadius;
        mCenterY = mRimRadius;

        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(getResources().getColor(R.color.sunshine_blue));
        canvas.drawCircle(mCenterX, mCenterY, mRimRadius, circlePaint);
    }

}
