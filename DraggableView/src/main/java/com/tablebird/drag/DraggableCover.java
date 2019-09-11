package com.tablebird.drag;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.tablebird.drag.animation.DampingAnimator;
import com.tablebird.drag.animation.DisappearAnimator;
import com.tablebird.drag.element.BezierElement;
import com.tablebird.drag.element.CoverElement;
import com.tablebird.drag.element.TargetElement;
import com.tablebird.drag.ref.WeakDrawableCache;

/**
 * @author tablebird
 * @date 2018/1/7
 */

@SuppressLint("ViewConstructor")
class DraggableCover extends SurfaceView implements SurfaceHolder.Callback,
        CoverElement.Callback, Animator.AnimatorListener {

    /**
     * 是否绘制内容
     */
    private boolean mIsDraw = false;

    /**
     * 是否可以绘制
     */
    private boolean mCanDraw = false;

    /**
     * 是否绘制贝塞尔曲线
     */
    private boolean mIsDrawBezier;

    /**
     * 绘制拖动图标
     */
    private TargetElement mTargetElement;

    /**
     * 绘制贝塞尔曲线图形
     */
    private BezierElement mBezierElement;

    /**
     * surface view起始位置
     */
    private int[] mLocation = new int[2];

    public DraggableCover(View view) {
        super(view.getContext());

        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().addCallback(this);
        setFocusable(false);
        setClickable(false);
        setFocusableInTouchMode(false);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        attachedToWindow(view);
        mTargetElement = new TargetElement(this);
        mBezierElement = new BezierElement(mTargetElement);
    }

    public void attachedToWindow(View view) {
        if (getParent() != null) {
            ((ViewGroup)getParent()).removeView(this);
        }
        View rootView = view.getRootView();
        if (rootView instanceof ViewGroup) {
            ((ViewGroup) rootView).addView(this, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        } else {
            Context context = view.getContext();
            if (context instanceof Activity) {
                ViewGroup viewGroup = ((ViewGroup) ((Activity) context).getWindow().getDecorView());
                viewGroup.addView(this, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            }
        }
    }


    void start(@NonNull Picture picture, int draggableBezierColor, Rect targetRect) {
        mBezierElement.setAnchorCenter(targetRect.centerX(), targetRect.centerY());
        mBezierElement.setTargetHalf(picture.getHeight() / 2.0f, picture.getWidth() / 2.0f);
        mBezierElement.setBezierColor(draggableBezierColor);

        mIsDraw = true;
        mTargetElement.setTarget(picture, targetRect);
        mIsDrawBezier = true;
    }

    void update(Rect targetRect, float anchorRadius, boolean isDrawBezier) {
        mIsDrawBezier = isDrawBezier;
        mBezierElement.setAnchorRadius(anchorRadius);
        mTargetElement.setRect(targetRect);
    }

    /**
     * 结束
     */
    void stop(Rect targetRect, boolean canDraw) {
        if (canDraw) {
            mTargetElement.setRect(targetRect);
        } else {
            mTargetElement.clean();
        }
        clean(canDraw);
    }

    private void clean(boolean canDraw) {
        if (!canDraw) {
            mTargetElement.clean();
            mBezierElement.clean();
            WeakDrawableCache.clean();
            if (getParent() != null) {
                ViewGroup viewGroup = (ViewGroup) getParent();
                viewGroup.removeView(this);
            }
            return;
        }
        mIsDraw = true;
        mIsDrawBezier = false;
        drawDrop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mIsDraw;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDraw = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mIsDraw = true;
        mCanDraw = holder.isCreating();
        calculationViewRectOnScreen();
        drawDrop();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void invalidate(CoverElement coverElement) {
        drawDrop();
    }

    private void drawDrop() {
        if (!mCanDraw) {
            return;
        }
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            canvas.translate(-mLocation[0], -mLocation[1]);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if (mIsDraw) {
                if (mIsDrawBezier) {
                    mBezierElement.draw(canvas);
                }
                mTargetElement.draw(canvas);
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void calculationViewRectOnScreen() {
        getLocationOnScreen(mLocation);
    }

    DisappearAnimator newDisappearAnimator() {
        DisappearAnimator disappearAnimator = new DisappearAnimator(getContext(), getHolder());
        disappearAnimator.addListener(this);
        disappearAnimator.setTranslate(mLocation[0], mLocation[1]);
        return disappearAnimator;
    }

    DampingAnimator newDampingAnimator() {
        DampingAnimator dampingAnimator = new DampingAnimator(mTargetElement);
        dampingAnimator.addListener(this);
        return dampingAnimator;
    }

    @Override
    public void onAnimationStart(Animator animation) {
        mIsDraw = true;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        clean(false);
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        mIsDraw = false;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        mIsDraw = true;
    }
}
