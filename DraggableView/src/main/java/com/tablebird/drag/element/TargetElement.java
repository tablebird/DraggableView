package com.tablebird.drag.element;

import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;


/**
 * @author tablebird
 * @date 2019/7/14
 */
public class TargetElement extends CoverElement implements BezierElement.Target {

    private Rect mRect;
    private Drawable mDrawable;

    public TargetElement(Callback callback) {
        setCallback(callback);
    }

    public TargetElement(Rect rect, Picture picture) {
        mRect = rect;
        setPicture(picture);
    }

    public TargetElement(Rect rect, Drawable drawable) {
        mRect = rect;
        mDrawable = drawable;
    }

    public void setPicture(Picture picture) {
        mDrawable = new PictureDrawable(picture);
        setupRect();
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
        setupRect();
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setRect(Rect rect) {
        mRect = rect;
        setupRect();
    }

    public void setTarget(Picture picture, Rect rect) {
        mDrawable = new PictureDrawable(picture);
        mRect = rect;
        setupRect();
    }

    @Override
    public Rect getRect() {
        return mRect;
    }

    public void rectOffsetTo(int newLeft, int newTop) {
        if (mRect != null) {
            mRect.offsetTo(newLeft, newTop);
            setupRect();
        }
    }

    private void setupRect() {
        if (mDrawable != null && mRect != null) {
            mDrawable.setBounds(mRect);
            invalidateSelf();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mDrawable != null) {
            mDrawable.draw(canvas);
        }
    }

    @Override
    public void clean() {
        mDrawable = null;
        mRect = null;
    }

    @Override
    public String toString() {
        return "TargetElement{" + "mRect=" + mRect + ", mDrawable=" + mDrawable + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TargetElement that = (TargetElement) o;

        return mRect != null ? mRect.equals(that.mRect) : that.mRect == null;
    }

    @Override
    public int hashCode() {
        return mRect != null ? mRect.hashCode() : 0;
    }
}
