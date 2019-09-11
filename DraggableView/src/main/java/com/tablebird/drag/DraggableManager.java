package com.tablebird.drag;

import android.content.Context;
import android.graphics.Picture;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tablebird.drag.animation.DampingAnimator;
import com.tablebird.drag.animation.DisappearAnimator;
import com.tablebird.drag.ref.DraggableViewWeakReference;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author tablebird
 * @date 2018/1/7
 */

class DraggableManager {

    private WeakHashMap<Context, Map<String, DraggableViewWeakReference>> mDraggableViewWeakHashMap = new WeakHashMap<>();

    private WeakReference<DraggableCover> mCurrentDraggableCoverWeakReference;

    static DraggableManager getInstance() {
        return Hold.INSTANCE;
    }

    private static class Hold {
        private final static DraggableManager INSTANCE = new DraggableManager();
    }

    private DraggableManager() {
    }

    void encaseDraggableView(String oldMark, DraggableView draggableView) {
        Context context = draggableView.getContext();
        if (!mDraggableViewWeakHashMap.containsKey(context)) {
            mDraggableViewWeakHashMap.put(context, new HashMap<String,
                    DraggableViewWeakReference>());
        }

        Map<String, DraggableViewWeakReference> weakHashMap =
                mDraggableViewWeakHashMap.get(context);
        if (weakHashMap == null) {
            weakHashMap = new HashMap<>();
            mDraggableViewWeakHashMap.put(context, weakHashMap);
        }
        if (!TextUtils.isEmpty(oldMark)) {
            weakHashMap.remove(oldMark);
        }
        String mark = draggableView.getMark();
        if (!TextUtils.isEmpty(mark)) {
            DraggableViewWeakReference draggableViewWeakReference = weakHashMap.remove(mark);
            if (draggableViewWeakReference != null && draggableViewWeakReference.get() != null) {
                draggableViewWeakReference.get().handoverSubordinate(draggableView);
            }
            weakHashMap.put(mark, new DraggableViewWeakReference(draggableView));
        }
    }

    @Nullable
    DraggableView findLeader(DraggableView draggableView) {
        Context context = draggableView.getContext();
        if (mDraggableViewWeakHashMap.containsKey(context)) {
            Map<String, DraggableViewWeakReference> weakHashMap =
                    mDraggableViewWeakHashMap.get(context);
            if (weakHashMap == null) {
                weakHashMap = new HashMap<>();
                mDraggableViewWeakHashMap.put(context, weakHashMap);
            }
            String leaderMark = draggableView.getLeaderMark();
            if (weakHashMap.containsKey(leaderMark)) {
                DraggableViewWeakReference draggableViewWeakReference = weakHashMap.get(leaderMark);
                return draggableViewWeakReference != null ? draggableViewWeakReference.get() : null;
            }
        }
        return null;
    }


    /**
     * 是否正在绘制
     *
     * @return true 锁定成功,false 锁定失败
     */
    boolean lockDraw(View view) {
        if (mCurrentDraggableCoverWeakReference == null || mCurrentDraggableCoverWeakReference.get() == null) {
            mCurrentDraggableCoverWeakReference = new WeakReference<>(new DraggableCover(view));
            return true;
        } else {
            DraggableCover draggableCover = mCurrentDraggableCoverWeakReference.get();
            if (draggableCover.getParent() == null) {
                draggableCover.attachedToWindow(view);
                return true;
            }
        }
        return false;
    }

    @Nullable
    DisappearAnimator newDisappearAnimation() {
        if (checkDraggableCover()) {
            return null;
        }
        return mCurrentDraggableCoverWeakReference.get().newDisappearAnimator();
    }

    @Nullable
    DampingAnimator newDampingAnimation() {
        if (checkDraggableCover()) {
            return null;
        }
        return mCurrentDraggableCoverWeakReference.get().newDampingAnimator();
    }


    void start(@NonNull Picture picture, int draggableBezierColor, Rect rect) {
        if (checkDraggableCover()) {
            return;
        }
        mCurrentDraggableCoverWeakReference.get().start(picture, draggableBezierColor, rect);
    }

    private boolean checkDraggableCover() {
        return mCurrentDraggableCoverWeakReference == null || mCurrentDraggableCoverWeakReference.get() == null;
    }

    void update(Rect rect, float anchorRadius, boolean isExceedMaxDistance) {
        if (checkDraggableCover()) {
            return;
        }
        mCurrentDraggableCoverWeakReference.get().update(rect, anchorRadius, isExceedMaxDistance);
    }

    void stop(Rect rect, boolean canDraw) {
        if (checkDraggableCover()) {
            return;
        }
        mCurrentDraggableCoverWeakReference.get().stop(rect, canDraw);
    }
}
