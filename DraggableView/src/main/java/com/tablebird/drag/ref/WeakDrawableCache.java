package com.tablebird.drag.ref;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * 减少相同动画资源重复加载
 *
 * @author tablebird
 * @date 2019/8/16
 */
public class WeakDrawableCache {

    private static SparseArray<WeakReference<Drawable>> mWeakReferences;

    @NonNull
    public static Drawable getDrawable(@NonNull Context context, @DrawableRes int resId) {
        if (mWeakReferences == null) {
            mWeakReferences = new SparseArray<WeakReference<Drawable>>();
        }
        WeakReference<Drawable> drawableWeakReference = mWeakReferences.get(resId);
        if (drawableWeakReference == null || drawableWeakReference.get() == null) {
            Drawable drawable = context.getResources().getDrawable(resId);
            mWeakReferences.put(resId, new WeakReference<Drawable>(drawable));
            return drawable;
        } else {
            return drawableWeakReference.get();
        }
    }

    public static void clean() {
        if (mWeakReferences != null) {
            mWeakReferences.clear();
        }
    }
}
