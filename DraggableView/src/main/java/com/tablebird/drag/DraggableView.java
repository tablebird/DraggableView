package com.tablebird.drag;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.tablebird.drag.animation.DampingAnimator;
import com.tablebird.drag.animation.DisappearAnimator;
import com.tablebird.drag.animation.DisappearAnimatorSet;
import com.tablebird.drag.element.TargetElement;
import com.tablebird.drag.ref.DraggableViewWeakReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author tablebird
 * @date 2018/1/7
 */

public class DraggableView extends AppCompatTextView implements Animator.AnimatorListener,
        Comparable<DraggableView> {

    public interface OnDragListener {
        /**
         * 拖拽结束销毁监听
         *
         * @param draggableView this
         */
        void onDragComplete(DraggableView draggableView);

        /**
         * 销毁动画播放结束监听
         *
         * @param draggableView this
         */
        void onDisappearComplete(DraggableView draggableView);
    }

    /**
     * 跟随消失模式
     */
    public enum FollowMode {

        /**
         * 同时消失
         */
        SIMULTANEOUSLY,

        /**
         * 尾随消失
         */
        TRAILING
    }

    /**
     * 是否启用拖动
     */
    private boolean mDragEnable;

    /**
     * 是否启用跟随消失模式
     */
    private boolean mFollowDisappearEnable;

    /**
     * 跟随消失模式
     */
    private FollowMode mFollowMode = FollowMode.SIMULTANEOUSLY;

    /**
     * 最远距离
     */
    private int mMaxDistanceWeights;

    /**
     * 原位置圆的最小半径
     */
    private int mMinAnchorRadius;

    /**
     * 原位置圆的最爱半径
     */
    private int mMaxAnchorRadius;

    /**
     * 可以恢复距离
     */
    private int mCanRecoverDistance;

    /**
     * 标记当前{@link #onTouchEvent(MotionEvent)}可以处理拖动事件
     */
    private boolean mHolderEventFlag;

    /**
     * 消失动画
     */
    private int[] mDisappearAnimationArray;

    /**
     * 消失动画的持续时间
     */
    private int mDisappearAnimationDuration;

    /**
     * 消失动画的大小
     */
    private float mDisappearAnimationHalfSize;

    /**
     * 减震动画的持续时间
     */
    private int mDampingAnimationDuration;

    /**
     * 减震动画的回弹次数
     */
    private int mDampingAnimationCount;

    /**
     * 原位置中心X坐标
     */
    private float mAnchorCenterX;
    /**
     * 原位置中心Y坐标
     */
    private float mAnchorCenterY;

    /**
     * 拖动图标中心X坐标
     */
    private float mTargetCenterX;
    /**
     * 拖动图标中心Y坐标
     */
    private float mTargetCenterY;

    /**
     * 视图宽
     */
    private int mWidth;

    /**
     * 视图高
     */
    private int mHeight;

    /**
     * 点击事件与视图left的偏移量
     */
    private float mOffsetX;

    /**
     * 点击事件与视图Top的偏移量
     */
    private float mOffsetY;

    /**
     * 是否已经超出最远距离
     */
    private boolean mIsExceedMaxDistance = false;

    /**
     * 是否绘制自身，在拖动的过程中本视图会被隐藏
     */
    private boolean mDrawSelf;

    /**
     * 绘制拖拽动画的贝塞尔曲线的颜色
     */
    private int mDraggableBezierColor;

    /**
     * 拖拽有效的监听
     */
    private OnDragListener mOnDragListener;

    /**
     * 视图标记
     */
    private String mMark;

    /**
     * 领导视图标记
     */
    private String mLeaderMark;

    /**
     * 销毁排序字段
     * 如果{@link #mFollowMode}为{@link FollowMode#TRAILING}，决定拖拽销毁时的销毁顺序，非负整数（大于等于0）
     */
    private int mSort = -1;
    /**
     * 从属视图
     */
    private List<DraggableViewWeakReference> mSubordinateList = new ArrayList<>();

    public DraggableView(Context context) {
        this(context, null);
    }

    public DraggableView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.DraggableViewStyle);
    }

    public DraggableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DraggableView,
                defStyleAttr, R.style.DefaultDraggableViewStyle);

        int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.DraggableView_draggableEnable) {
                mDragEnable = typedArray.getBoolean(attr, mDragEnable);
            } else if (attr == R.styleable.DraggableView_maxDragDistance) {
                mMaxDistanceWeights = typedArray.getDimensionPixelSize(attr, mMaxDistanceWeights);
            } else if (attr == R.styleable.DraggableView_canRecoverDistance) {
                mCanRecoverDistance = typedArray.getDimensionPixelSize(attr, mCanRecoverDistance);
            } else if (attr == R.styleable.DraggableView_minAnchorRadius) {
                mMinAnchorRadius = typedArray.getDimensionPixelSize(attr, mMinAnchorRadius);
                if (mMinAnchorRadius <= 0) {
                    throw new IllegalArgumentException("minAnchorRadius should be greater than " +
                            "zero");
                }
            } else if (attr == R.styleable.DraggableView_maxAnchorRadius) {
                mMaxAnchorRadius = typedArray.getDimensionPixelSize(attr, mMaxAnchorRadius);
                if (mMaxAnchorRadius <= 0) {
                    throw new IllegalArgumentException("maxAnchorRadius should be greater than " +
                            "zero");
                }
            } else if (attr == R.styleable.DraggableView_disappearAnimationArray) {
                int explosionId = typedArray.getResourceId(attr, -1);
                TypedArray explosionTypeArray =
                        context.getResources().obtainTypedArray(explosionId);
                setAnimationArray(explosionTypeArray);
                explosionTypeArray.recycle();
            } else if (attr == R.styleable.DraggableView_disappearAnimationDuration) {
                mDisappearAnimationDuration = typedArray.getInt(attr, mDisappearAnimationDuration);
            } else if (attr == R.styleable.DraggableView_disappearAnimationSize) {
                mDisappearAnimationHalfSize = typedArray.getDimensionPixelSize(attr, -1) / 2.0f;
            } else if (attr == R.styleable.DraggableView_draggableBezierColor) {
                mDraggableBezierColor = typedArray.getColor(attr, mDraggableBezierColor);
            } else if (attr == R.styleable.DraggableView_dampingAnimationDuration) {
                mDampingAnimationDuration = typedArray.getInt(attr, mDampingAnimationDuration);
            } else if (attr == R.styleable.DraggableView_dampingAnimationCount) {
                mDampingAnimationCount = typedArray.getInt(attr, mDampingAnimationCount);
            } else if (attr == R.styleable.DraggableView_followDisappearEnable) {
                mFollowDisappearEnable = typedArray.getBoolean(attr, mFollowDisappearEnable);
            } else if (attr == R.styleable.DraggableView_followDisappearMode) {
                int index = typedArray.getInt(attr, mFollowMode.ordinal());
                mFollowMode = FollowMode.values()[index];
            }
        }
        typedArray.recycle();

        Drawable drawable = getBackground();
        if (drawable == null) {
            setBackgroundResource(R.drawable.def_draggable_view_bg);
        }

        calibrationCanRecoverDistance();

        calibrationAnchorRadius();

        mDrawSelf = true;
    }

    private void calibrationAnchorRadius() {
        calibrationAnchorRadius(getWidth(), getHeight());
    }

    private void calibrationAnchorRadius(int width, int height) {
        if (mMaxAnchorRadius <= 0) {
            if (width <= 0 || height <= 0) {
                return;
            }
            mMaxAnchorRadius = (int) (width < height ? width / 2.0f : height / 2.0f);
        }
        if (mMinAnchorRadius <= 0 || mMinAnchorRadius >= mMaxAnchorRadius) {
            mMinAnchorRadius = (int) (mMaxAnchorRadius * 0.4f);
        }
    }

    private void calibrationCanRecoverDistance() {
        if (mCanRecoverDistance > mMaxDistanceWeights) {
            mCanRecoverDistance = mMaxDistanceWeights;
        }
    }

    private void setAnimationArray(TypedArray explosionTypeArray) {
        mDisappearAnimationArray = new int[explosionTypeArray.length()];
        for (int i = 0; i < mDisappearAnimationArray.length; i++) {
            mDisappearAnimationArray[i] = explosionTypeArray.getResourceId(i, 0);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawSelf) {
            super.onDraw(canvas);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mDrawSelf) {
            super.draw(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (oldw <= 0 || oldh <= 0) {
            calibrationAnchorRadius(w, h);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mDragEnable) {
            //拖动效果未开启
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHolderEventFlag = DraggableManager.getInstance().lockDraw(this);
                if (mHolderEventFlag) {
                    startDraggable(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mHolderEventFlag) {
                    updateDraggable(event);
                }
                break;
            default:
                if (mHolderEventFlag) {
                    mHolderEventFlag = false;
                    stopDraggable(event);
                }
                break;
        }
        return mHolderEventFlag || super.onTouchEvent(event);
    }

    private void startDraggable(MotionEvent event) {
        mIsExceedMaxDistance = false;
        ViewGroup parent = getScrollableParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }

        Rect rect = getViewRectOnScreen();

        mWidth = getWidth();
        mHeight = getHeight();

        mOffsetX = event.getRawX() - rect.left;
        mOffsetY = event.getRawY() - rect.top;

        mAnchorCenterX = rect.centerX();
        mAnchorCenterY = rect.centerY();

        mTargetCenterX = mAnchorCenterX;
        mTargetCenterY = mAnchorCenterY;

        Picture picture = getViewToPicture();
        setDrawSelf(false);
        DraggableManager.getInstance().start(picture, mDraggableBezierColor, rect);
    }

    private Rect getViewRectOnScreen() {
        final int[] location = new int[2];
        getLocationOnScreen(location);
        return new Rect(location[0], location[1] ,
                location[0] + getWidth(),
                location[1] + getHeight());
    }

    private void updateDraggable(MotionEvent event) {

        Rect rect = getTargetRect(event);
        mTargetCenterX = rect.centerX();
        mTargetCenterY = rect.centerY();

        double distance = getDistance();
        if (distance > mMaxDistanceWeights) {
            mIsExceedMaxDistance = true;
        }
        //计算当前移动距离占最远距离的百分比
        // 1减去当前距离百分比乘以半径的可变动范围得到剩余可变动半径
        // 用最小半径加上剩余可变动半径得到当前半径
        float anchorRadius = mIsExceedMaxDistance ? 0 :
                (float) (1.0f - distance / mMaxDistanceWeights) * (getMaxRadius() - getMinRadius()) + getMinRadius();
        DraggableManager.getInstance().update(rect, anchorRadius, !mIsExceedMaxDistance);
    }

    @NonNull
    private Rect getTargetRect(MotionEvent event) {
        int left = (int) (event.getRawX() - mOffsetX);
        int top = (int) (event.getRawY() - mOffsetY);
        return new Rect(left, top, left + mWidth, top + mHeight);
    }

    private void stopDraggable(MotionEvent event) {
        ViewGroup parent = getScrollableParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(false);
        }

        Rect rect = getTargetRect(event);
        mTargetCenterX = rect.centerX();
        mTargetCenterY = rect.centerY();

        boolean draggableValid = mIsExceedMaxDistance && getDistance() > mCanRecoverDistance;
        boolean playDamping = (draggableValid && checkDisappearAnimation())
                || (!mIsExceedMaxDistance && checkDampingAnimation());
        DraggableManager.getInstance().stop(rect, playDamping);
        if (draggableValid) {
            if (mOnDragListener != null) {
                mOnDragListener.onDragComplete(this);
            }
            playDisappearAnimation(mTargetCenterX, mTargetCenterY, new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (mOnDragListener != null) {
                        mOnDragListener.onDisappearComplete(DraggableView.this);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        } else if (!mIsExceedMaxDistance) {
            playDampingAnimation();
        } else {
            setDrawSelf(true);
        }
    }

    /**
     * 检查震荡动画是否有效
     *
     * @return true 有效, false 无效
     */
    private boolean checkDampingAnimation() {
        return mDampingAnimationCount > 0 && mDampingAnimationDuration > 0;
    }

    protected Picture getViewToPicture() {
        int width = getWidth();
        int height = getHeight();
        Picture picture = new Picture();
        Canvas canvas = picture.beginRecording(width, height);
        draw(canvas);
        picture.endRecording();
        return picture;
    }

    @Nullable
    protected Picture getViewToVisiblePicture() {
        Rect rect = new Rect();
        boolean visible = getLocalVisibleRect(rect);
        if (visible) {
            int width = getWidth();
            int height = getHeight();
            Picture picture = new Picture();
            Canvas canvas = picture.beginRecording(width, height);
            canvas.clipRect(rect);
            draw(canvas);
            picture.endRecording();
            return picture;
        }
        return null;
    }

    /**
     * 获取原位置与拖动点的距离
     *
     * @return 距离
     */
    public double getDistance() {
        return Math.sqrt(Math.pow(mAnchorCenterX - mTargetCenterX, 2) + Math.pow(mAnchorCenterY - mTargetCenterY, 2));
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    /**
     * 获取拖动失效最大距离
     *
     * @return 最大距离
     */
    public int getMaxDistance() {
        return mMaxDistanceWeights;
    }

    /**
     * 设置拖动失效最大距离
     *
     * @param maxDistanceWeights 最大距离
     */
    public void setMaxDistance(int maxDistanceWeights) {
        mMaxDistanceWeights = maxDistanceWeights;
        calibrationCanRecoverDistance();
    }

    /**
     * 获取原位置最小半径
     *
     * @return 最小半径
     */
    public int getMinRadius() {
        return mMinAnchorRadius;
    }

    /**
     * 设置原位置最小半径
     *
     * @param minRadius 最小半径
     */
    public void setMinRadius(int minRadius) {
        if (minRadius <= 0) {
            throw new IllegalArgumentException("miniRadius should be greater than zero");
        }
        mMinAnchorRadius = minRadius;
        calibrationAnchorRadius();
    }

    /**
     * 获取原位置最大半径
     *
     * @return 最大半径
     */
    public int getMaxRadius() {
        return mMaxAnchorRadius;
    }

    /**
     * 获取原位置最大半径
     *
     * @param maxRadius  最大半径
     */
    public void setMaxRadius(int maxRadius) {
        if (maxRadius <= 0) {
            throw new IllegalArgumentException("maxRadius should be greater than zero");
        }
        mMaxAnchorRadius = maxRadius;
        calibrationAnchorRadius();
    }

    /**
     * 获取可以恢复距离
     *
     * @return 可以恢复距离
     */
    public int getCanRecoverDistance() {
        return mCanRecoverDistance;
    }

    /**
     * 设置可以恢复距离
     *
     * @param canRecoverDistance 可以恢复距离
     */
    public void setCanRecoverDistance(int canRecoverDistance) {
        this.mCanRecoverDistance = canRecoverDistance;
        calibrationCanRecoverDistance();
    }

    /**
     * 获取消失动画的持续时间
     *
     * @return 消失动画的持续时间
     */
    public int getAnimationDuration() {
        return mDisappearAnimationDuration;
    }

    /**
     * 设置消失动画的持续时间
     *
     * @param animationDuration 消失动画的持续时间
     */
    public void setAnimationDuration(int animationDuration) {
        this.mDisappearAnimationDuration = animationDuration;
    }

    /**
     * 获取消失动画
     *
     * @return 消失动画
     */
    public int[] getAnimationArray() {
        return mDisappearAnimationArray;
    }

    /**
     * 设置消失动画
     *
     * @param animationArray 消失动画
     */
    public void setAnimationArray(int[] animationArray) {
        this.mDisappearAnimationArray = animationArray;
    }

    /**
     * 获取消失动画的大小
     *
     * @return 消失动画的大小
     */
    public int getAnimationSize() {
        return (int) (mDisappearAnimationHalfSize * 2);
    }

    /**
     * 设置消失动画的大小
     *
     * @param mAnimationHalfSize 消失动画的大小
     */
    public void setAnimationHalfSize(int mAnimationHalfSize) {
        this.mDisappearAnimationHalfSize = mAnimationHalfSize / 2.0f;
    }

    /**
     * 获取绘制拖拽动画的贝塞尔曲线的颜色
     *
     * @return 绘制拖拽动画的贝塞尔曲线的颜色
     */
    public int getDraggableBezierColor() {
        return mDraggableBezierColor;
    }

    /**
     * 设置绘制拖拽动画的贝塞尔曲线的颜色
     *
     * @param draggableBezierColor 绘制拖拽动画的贝塞尔曲线的颜色
     */
    public void setDraggableBezierColor(int draggableBezierColor) {
        mDraggableBezierColor = draggableBezierColor;
    }

    /**
     * 设置是否允许拖动
     *
     * @param enabled true 允许,false 禁止
     */
    public void setDragEnabled(boolean enabled) {
        mDragEnable = enabled;
    }

    /**
     * 是否允许拖动
     *
     * @return true 允许,false 禁止
     */
    public boolean isDragEnable() {
        return mDragEnable;
    }

    /**
     * 设置是否启用跟随消失模式
     *
     * @return true 启用跟随，false 不启用跟随
     */
    public boolean isFollowDisappearEnable() {
        return mFollowDisappearEnable;
    }

    /**
     * 是否启用跟随消失模式
     *
     * @param followDisappearEnable true 启用跟随，false 不启用跟随
     */
    public void setFollowDisappearEnable(boolean followDisappearEnable) {
        mFollowDisappearEnable = followDisappearEnable;
    }

    /**
     * 跟随消失模式 {@link FollowMode}
     *
     * @return {@link FollowMode}
     */
    public FollowMode getFollowMode() {
        return mFollowMode;
    }

    /**
     * 跟随消失模式 {@link FollowMode}
     *
     * @param followMode @link FollowMode}
     */
    public void setFollowMode(FollowMode followMode) {
        mFollowMode = followMode;
        if (mFollowMode == null) {
            mFollowMode = FollowMode.SIMULTANEOUSLY;
        }
    }

    /**
     * 获取减震动画的持续时间
     *
     * @return 减震动画的持续时间
     */
    public int getDampingAnimationDuration() {
        return mDampingAnimationDuration;
    }

    /**
     * 设置减震动画的持续时间
     *
     * @param dampingAnimationDuration 减震动画的持续时间
     */
    public void setDampingAnimationDuration(int dampingAnimationDuration) {
        this.mDampingAnimationDuration = dampingAnimationDuration;
    }

    /**
     * 获取减震动画的回弹次数
     *
     * @return 减震动画的回弹次数
     */
    public int getDampingAnimationCount() {
        return mDampingAnimationCount;
    }

    /**
     * 设置减震动画的回弹次数
     *
     * @param dampingAnimationCount 减震动画的回弹次数
     */
    public void setDampingAnimationCount(int dampingAnimationCount) {
        this.mDampingAnimationCount = dampingAnimationCount;
    }

    /**
     * 获取拖拽监听
     *
     * @return 拖拽监听
     */
    public OnDragListener getOnDragCompeteListener() {
        return mOnDragListener;
    }

    /**
     * 设置拖拽监听
     *
     * @param onDragListener 拖拽监听
     */
    public void setOnDragCompeteListener(OnDragListener onDragListener) {
        this.mOnDragListener = onDragListener;
    }

    /**
     * 获取视图标记
     *
     * @return 视图标记
     */
    public String getMark() {
        return mMark;
    }

    /**
     * 设置视图标记
     *
     * @param mark 视图标记
     */
    public void setMark(String mark) {
        if (TextUtils.equals(mMark, mark)) {
            return;
        }
        String oldMark = mMark;
        mMark = mark;
        mSubordinateList.clear();
        DraggableManager.getInstance().encaseDraggableView(oldMark, this);
    }

    /**
     * 获取领导视图标记
     *
     * @return 领导视图标记
     */
    public String getLeaderMark() {
        return mLeaderMark;
    }

    /**
     * 设置领导标记
     *
     * @param leaderMark 领导标记
     */
    public void setLeaderMark(String leaderMark) {
        if (TextUtils.equals(mLeaderMark, leaderMark)) {
            return;
        }
        DraggableViewWeakReference draggableViewWeakReference =
                new DraggableViewWeakReference(this);
        if (!TextUtils.isEmpty(mLeaderMark)) {
            DraggableView leader = DraggableManager.getInstance().findLeader(this);
            if (leader != null) {
                leader.mSubordinateList.remove(draggableViewWeakReference);
            }
        }
        mLeaderMark = leaderMark;
        if (!TextUtils.isEmpty(mLeaderMark)) {
            DraggableView leader = DraggableManager.getInstance().findLeader(this);
            if (leader == null) {
                throw new RuntimeException("Not find leader,Please set the leader mark first.");
            }
            List<DraggableViewWeakReference> draggableViewWeakReferences = leader.mSubordinateList;
            draggableViewWeakReferences.removeAll(Arrays.asList(draggableViewWeakReference,
                    DraggableViewWeakReference.NULL));
            draggableViewWeakReferences.add(draggableViewWeakReference);
        }
    }

    /**
     * 获取销毁排序字段， {@link #mFollowMode}为{@link FollowMode#TRAILING}该字段有效
     *
     * @return 销毁排序字段
     */
    public int getSort() {
        return mSort;
    }

    /**
     * 设置销毁排序字段，{@link #mFollowMode}为{@link FollowMode#TRAILING}该字段有效
     * @param sort
     */
    public void setSort(int sort) {
        if (sort < 0) {
            throw new IllegalArgumentException("Sort field cannot be less than 0");
        }
        mSort = sort;
    }


    @Override
    public int compareTo(@NonNull DraggableView draggableView) {
        return compare(this, draggableView);
    }

    public int compare(DraggableView draggableView1, DraggableView draggableView2) {
        return (draggableView1.mSort < draggableView2.mSort) ? -1 :
                ((draggableView1.mSort == draggableView2.mSort) ? 0 : 1);
    }

    /**
     * 交接下属
     *
     * @param toDraggableView 被交接人
     */
    protected void handoverSubordinate(DraggableView toDraggableView) {
        toDraggableView.mSubordinateList.addAll(mSubordinateList);
        mSubordinateList.clear();
    }

    /**
     * 获取可滑动的父视图
     *
     * @return 父视图
     */
    private ViewGroup getScrollableParent() {
        View target = this;
        while (true) {
            View parent;
            try {
                parent = (View) target.getParent();
            } catch (Exception e) {
                return null;
            }
            if (parent == null) {
                return null;
            }
            if (parent instanceof ListView || parent instanceof ScrollView) {
                return (ViewGroup) parent;
            }
            target = parent;
        }
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof DraggableView && !TextUtils.isEmpty(getMark()) && !TextUtils.isEmpty(((DraggableView) obj).getMark())) {
            return TextUtils.equals(getMark(), ((DraggableView) obj).getMark());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        if (!TextUtils.isEmpty(getMark())) {
            return getMark().hashCode();
        }
        return super.hashCode();
    }

    /**
     * 当前是否可见
     *
     * @return true 可见,false 不可见
     */
    protected boolean isCover() {
        if (getVisibility() != VISIBLE) {
            return false;
        }
        if (getParent() == null) {
            return false;
        }
        Rect rect = new Rect();
        return getGlobalVisibleRect(rect);
    }

    protected void setDrawSelf(boolean drawSelf) {
        if (mDrawSelf == drawSelf) {
            return;
        }
        mDrawSelf = drawSelf;
        invalidate();
    }

    /**
     * 视图有位移后，播放减震动画
     */
    private void playDampingAnimation() {
        DampingAnimator dampingAnimator = DraggableManager.getInstance().newDampingAnimation();
        if (dampingAnimator != null) {
            dampingAnimator.setAnchor(mAnchorCenterX - mWidth / 2.0f,
                    mAnchorCenterY - mHeight / 2.0f);
            dampingAnimator.setDuration(mDampingAnimationDuration);
            dampingAnimator.setDampingCount(mDampingAnimationCount);
            dampingAnimator.addListener(this);
            dampingAnimator.start();
        } else {
            setDrawSelf(true);
        }
    }

    /**
     * 播放消失动画
     *
     * @param listener 动画监听
     * @return true 开始播放，false 播放失败
     */
    public boolean playDisappearAnimation(@Nullable Animator.AnimatorListener listener) {
        if (!isCover()) {
            return false;
        }
        calculationCenterPoint();
        return playDisappearAnimation(mAnchorCenterX, mAnchorCenterY, listener);
    }

    private void calculationCenterPoint() {
        int[] location = new int[2];
        getLocationOnScreen(location);
        mAnchorCenterX = location[0] + getWidth() / 2.0f;
        mAnchorCenterY = location[1] + getHeight() / 2.0f;
    }

    /**
     * 播放消失动画
     *
     * @param x 中心点X坐标
     * @param y 中心点Y坐标
     * @return true 开始播放，false 播放失败
     */
    private boolean playDisappearAnimation(float x, float y,
                                           @Nullable Animator.AnimatorListener listener) {

        DisappearAnimator disappearAnimator = buildDisappearAnimator(x, y);
        if (disappearAnimator == null) {
            setDrawSelf(true);
            return false;
        }
        DisappearAnimatorSet disappearAnimatorSet = new DisappearAnimatorSet();

        if (listener != null) {
            disappearAnimatorSet.addListener(listener);
        }
        disappearAnimatorSet.addListener(this);
        DisappearAnimatorSet.Builder builder = disappearAnimatorSet.play(disappearAnimator);
        recursiveSubordinate(this, builder);

        disappearAnimatorSet.start();
        return true;
    }

    private DisappearAnimator buildDisappearAnimator() {
        if (!isCover()) {
            return null;
        }
        calculationCenterPoint();
        return buildDisappearAnimator(mAnchorCenterX, mAnchorCenterY);
    }

    private DisappearAnimator buildDisappearAnimator(float x, float y) {
        if (!checkDisappearAnimation()) {
            return null;
        }
        DisappearAnimator disappearAnimator =
                DraggableManager.getInstance().newDisappearAnimation();
        if (disappearAnimator == null) {
            return null;
        }

        Rect rect = getDisappearAnimationRect(x, y);
        disappearAnimator.setDuration(mDisappearAnimationDuration);
        disappearAnimator.setDisappear(mDisappearAnimationArray);
        disappearAnimator.addRect(rect);
        return disappearAnimator;
    }

    /**
     * 检查销毁动画是否有效
     *
     * @return true 销毁动画有效，false 销毁动画无效
     */
    private boolean checkDisappearAnimation() {
        return mDisappearAnimationDuration > 0 && mDisappearAnimationArray != null && mDisappearAnimationArray.length != 0 && mDisappearAnimationHalfSize > 0;
    }

    @NonNull
    private Rect getDisappearAnimationRect() {
        calculationCenterPoint();
        return getDisappearAnimationRect(mAnchorCenterX, mAnchorCenterY);
    }

    @NonNull
    private Rect getDisappearAnimationRect(float centerX, float centerY) {
        return new Rect((int) (centerX - mDisappearAnimationHalfSize),
                (int) (centerY - mDisappearAnimationHalfSize),
                (int) (centerX + mDisappearAnimationHalfSize),
                (int) (centerY + mDisappearAnimationHalfSize));
    }

    /**
     * 递归获取所有的跟随视图
     *
     * @param draggableView 主视图
     * @param builder       动画
     */
    private void recursiveSubordinate(DraggableView draggableView,
                                      DisappearAnimatorSet.Builder builder) {
        if (draggableView.isFollowDisappearEnable() && !TextUtils.isEmpty(draggableView.getMark())) {
            FollowMode followMode = draggableView.getFollowMode();
            List<DraggableViewWeakReference> draggableViewWeakReferenceList =
                    draggableView.mSubordinateList;
            if (draggableViewWeakReferenceList.isEmpty()) {
                return;
            }
            Collections.sort(draggableViewWeakReferenceList);
            for (DraggableViewWeakReference draggableViewWeakReference :
                    draggableViewWeakReferenceList) {
                DraggableView subDraggableView = draggableViewWeakReference.get();
                if (subDraggableView != null) {
                    if (subDraggableView.isCover()) {
                        Log.e("XYQ","subordinate id : " + subDraggableView.getSort());
                        if (followMode == FollowMode.SIMULTANEOUSLY) {
                            builder.with(subDraggableView.getDisappearAnimationRect());
                            builder.addListener(subDraggableView);
                        } else if (followMode == FollowMode.TRAILING) {
                            DisappearAnimator disappearAnimator =
                                    subDraggableView.buildDisappearAnimator();
                            if (disappearAnimator != null) {
                                Picture picture = subDraggableView.getViewToVisiblePicture();
                                if (picture != null) {
                                    Rect rect = subDraggableView.getViewRectOnScreen();
                                    builder.addPlaceholder(new TargetElement(rect, picture));
                                }
                                builder.before(disappearAnimator);
                                builder.addListener(subDraggableView);
                            }
                        }
                    }
                    recursiveSubordinate(subDraggableView, builder);
                }
            }
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
        setDrawSelf(false);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        setDrawSelf(true);
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

}
