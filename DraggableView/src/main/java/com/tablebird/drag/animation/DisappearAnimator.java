package com.tablebird.drag.animation;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tablebird.drag.element.TargetElement;
import com.tablebird.drag.ref.WeakDrawableCache;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tablebird
 * @date @date 2018/1/7
 */

public class DisappearAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener {

    private int[] mResIds;
    private WeakReference<Context> mContext;
    @NonNull
    private SurfaceHolder mSurfaceHolder;
    private Set<Rect> mDstRect = new HashSet<>();

    @Nullable
    private Picture mPlaceholderPicture;
    private Canvas mPlaceholderCanvas;

    private int mTranslateX = 0;

    private int mTranslateY = 0;

    public DisappearAnimator(@NonNull Context context, @NonNull SurfaceHolder surfaceHolder) {
        mContext = new WeakReference<>(context);
        mSurfaceHolder = surfaceHolder;
        addUpdateListener(this);
    }

    public void setDisappear(@NonNull int[] resIds) {
        mResIds = resIds;
        setIntValues(0, mResIds.length - 1);
    }

    public void setTranslate(int x,int y) {
        mTranslateX = x;
        mTranslateY = y;
    }

    public void addRect(Rect rect) {
        mDstRect.add(rect);
    }

    public void addTargetElement(TargetElement targetElement) {
        if (targetElement == null) {
            return;
        }
        if (mPlaceholderPicture == null) {
            mPlaceholderPicture = new Picture();
            Rect rect = mSurfaceHolder.getSurfaceFrame();

            mPlaceholderCanvas = mPlaceholderPicture.beginRecording(rect.width(), rect.height());
        }

        targetElement.draw(mPlaceholderCanvas);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        int curValue = (int) animation.getAnimatedValue();
        draw(curValue);
    }

    private void draw(int index) {
        if (mResIds == null || mDstRect.isEmpty()) {
            return;
        }
        Context context = mContext.get();
        if (context != null && isRunning() && index >= 0 && index < mResIds.length) {
            Canvas canvas = mSurfaceHolder.lockCanvas();
            if (canvas != null) {
                canvas.translate(-mTranslateX, -mTranslateY);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                if (mPlaceholderPicture != null) {
                    mPlaceholderPicture.draw(canvas);
                }
                Drawable drawable = WeakDrawableCache.getDrawable(context, mResIds[index]);
                for (Rect viewRect : mDstRect) {
                    drawable.setBounds(viewRect);
                    drawable.draw(canvas);
                }
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void start() {
        mPlaceholderCanvas = null;
        if (mPlaceholderPicture != null) {
            mPlaceholderPicture.endRecording();
        }
        super.start();
    }

    @Override
    public void cancel() {
        super.cancel();
        stopAnimation();
    }

    @Override
    public void end() {
        super.end();
        stopAnimation();
    }

    private void stopAnimation() {
        mDstRect.clear();
        mPlaceholderPicture = null;
    }
}
