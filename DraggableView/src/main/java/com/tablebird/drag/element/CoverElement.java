package com.tablebird.drag.element;

import android.graphics.Canvas;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * 封面元素
 *
 * @author tablebird
 * @date 2019/7/14
 */
public abstract class CoverElement {
    private WeakReference<Callback> mCallbackWeakReference;

    public void setCallback(Callback callback) {
        mCallbackWeakReference = callback != null ? new WeakReference<Callback>(callback) : null;
    }

    @Nullable
    public Callback getCallback() {
        return mCallbackWeakReference != null ? mCallbackWeakReference.get() : null;
    }

    void invalidateSelf() {
        Callback callback = getCallback();
        if (callback != null) {
            callback.invalidate(this);
        }
    }

    public abstract void draw(Canvas canvas);

    public abstract void clean();

    public interface Callback {
        void invalidate(CoverElement coverElement);
    }
}
