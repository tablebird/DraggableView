package com.tablebird.drag.animation;

import android.animation.ValueAnimator;
import android.graphics.Rect;

import com.tablebird.drag.element.TargetElement;

import java.lang.ref.WeakReference;

/**
 * @author tablebird
 * @date 2019/7/14
 */
public class DampingAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener{

    /**
     * 原位置X坐标
     */
    private float mAnchorLeft;
    /**
     * 原位置Y坐标
     */
    private float mAnchorTop;

    /**
     * 简谐运动次数
     */
    private int mDampingCount = 2;

    private WeakReference<TargetElement> mTargetElement;

    private DampingInterpolator mDampingInterpolator = new DampingInterpolator();

    private float mDistanceX;
    private float mDistanceY;

    public DampingAnimator(TargetElement targetElement) {
        mTargetElement = new WeakReference<TargetElement>(targetElement);
        setInterpolator(mDampingInterpolator);
        setFloatValues(1.0f, 0.0f);
        addUpdateListener(this);
    }

    public void setAnchor(float left, float top) {
        mAnchorLeft = left;
        mAnchorTop = top;
    }

    private void setWeight(float weight) {
        if (mTargetElement.get() == null) {
            return;
        }
        mTargetElement.get().rectOffsetTo((int) (mAnchorLeft + mDistanceX * weight),(int) (mAnchorTop + mDistanceY * weight));
    }

    public void setDampingCount(int dampingCount) {
        this.mDampingCount = dampingCount;
        mDampingInterpolator.setOverShootCount(mDampingCount);
    }

    @Override
    public void start() {
        if (mTargetElement == null || mTargetElement.get() == null || mTargetElement.get().getRect() == null) {
            return;
        }
        Rect rect = mTargetElement.get().getRect();
        mDistanceX = rect.left - mAnchorLeft;
        mDistanceY = rect.top - mAnchorTop;
        super.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float weight = (float) valueAnimator.getAnimatedValue();
        setWeight(weight);
    }
}
