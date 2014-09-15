package com.weidongjian.weigan.sunshine.service;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.weidongjian.weigan.sunshine.R;

/**
 * Created by Weigan on 2014/9/14.
 */
public class MyDireactionView extends View {
    private Paint circlePaint;
    private Paint indicatorPaint;
    private Paint textPaint;
    private int mIndicatorRadius;
    private int mRimRadius;
    private int mFaceRadius;
    private int mCenterX;
    private int mCenterY;
    private float mDirection;
    private Path arrow;

    public MyDireactionView(Context context) {
        super(context);
        initTools();
    }

    public MyDireactionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTools();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircle(canvas);
        drawIndicator(canvas);
        drawText(canvas);
        invalidate();
    }

    private void initTools() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private void drawCircle(Canvas canvas) {
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

        mFaceRadius = mRimRadius * 7 / 8;
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(mCenterX, mCenterY, mFaceRadius, circlePaint);

        circlePaint.reset();
        mIndicatorRadius = mFaceRadius / 10;
    }

    private void drawIndicator(Canvas canvas) {
        indicatorPaint.reset();
        indicatorPaint.setColor(Color.RED);
        indicatorPaint.setStyle(Paint.Style.FILL);

        arrow = new Path();
        arrow.moveTo(mCenterX, mCenterY);
        arrow.lineTo(mCenterX - mIndicatorRadius, mCenterY);
        arrow.lineTo(mCenterX, mCenterY - mIndicatorRadius * 6);
        arrow.lineTo(mCenterX + mIndicatorRadius, mCenterY);
        arrow.lineTo(mCenterX, mCenterY);
        arrow.close();

//        RectF bounds = new RectF();
//        arrow.computeBounds(bounds, true);
        Matrix matrix = new Matrix();
        matrix.setRotate(mDirection, mCenterX, mCenterY);
        arrow.transform(matrix);

        Path centerCircle = new Path();
        centerCircle.addCircle(mCenterX, mCenterY, mIndicatorRadius, Path.Direction.CCW);
        centerCircle.close();

        canvas.drawPath(arrow, indicatorPaint);
        canvas.drawPath(centerCircle, indicatorPaint);
    }

    private void drawText(Canvas canvas) {
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize((float)mIndicatorRadius * 2);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float dim = mIndicatorRadius * 8f;

        canvas.drawText("N", mCenterX, mCenterY - dim, textPaint);
        canvas.drawText("W", mCenterX - dim, mCenterY, textPaint);
        canvas.drawText("E", mCenterX + dim, mCenterY, textPaint);
        canvas.drawText("S", mCenterX, mCenterY + dim, textPaint);
    }

    public void updateDirection(float direction) {
//        System.out.println("direction: " + direction);
//        Matrix matrix = new Matrix();
//        matrix.setRotate(direction, mCenterX, mCenterY);
//        arrow.transform(matrix);
//        invalidate();
        mDirection = direction;
    }
}
