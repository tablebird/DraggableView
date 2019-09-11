package com.tablebird.drag.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;

import com.tablebird.drag.element.TargetElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author tablebird
 * @date 2019/8/14
 */
public class DisappearAnimatorSet extends Animator {
    private AnimatorSet mAnimatorSet;

    public DisappearAnimatorSet() {
        mAnimatorSet = new AnimatorSet();
    }

    @Override
    public void start() {
        Set<Animator.AnimatorListener> animatorListenerSet = new HashSet<>();
        for (Animator animator: mAnimatorSet.getChildAnimations()) {
            ArrayList<AnimatorListener> animatorListeners = animator.getListeners();
            animatorListenerSet.addAll(animatorListeners);
            animator.removeAllListeners();
        }
        for (AnimatorListener animatorListener : animatorListenerSet) {
            mAnimatorSet.addListener(animatorListener);
        }
        mAnimatorSet.start();
    }

    @Override
    public void cancel() {
        mAnimatorSet.cancel();
    }

    @Override
    public void end() {
        mAnimatorSet.end();
    }

    @Override
    public void pause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAnimatorSet.pause();
        }
    }

    @Override
    public void resume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAnimatorSet.resume();
        }
    }

    @Override
    public boolean isPaused() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return mAnimatorSet.isPaused();
        }
        return super.isPaused();
    }

    @Override
    public long getTotalDuration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return mAnimatorSet.getTotalDuration();
        }
        return super.getTotalDuration();
    }

    @Override
    public TimeInterpolator getInterpolator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return mAnimatorSet.getInterpolator();
        }
        return super.getInterpolator();
    }

    @Override
    public boolean isStarted() {
        return mAnimatorSet.isStarted();
    }

    @Override
    public void addListener(AnimatorListener listener) {
        mAnimatorSet.addListener(listener);
    }

    @Override
    public void removeListener(AnimatorListener listener) {
        mAnimatorSet.removeListener(listener);
    }

    @Override
    public ArrayList<AnimatorListener> getListeners() {
        return mAnimatorSet.getListeners();
    }

    @Override
    public void addPauseListener(AnimatorPauseListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAnimatorSet.addPauseListener(listener);
        } else {
            super.addPauseListener(listener);
        }
    }

    @Override
    public void removePauseListener(AnimatorPauseListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAnimatorSet.removePauseListener(listener);
        } else {
            super.removePauseListener(listener);
        }
    }

    @Override
    public void removeAllListeners() {
        mAnimatorSet.removeAllListeners();
    }

    @Override
    public Animator clone() {
        return mAnimatorSet.clone();
    }

    @Override
    public void setupStartValues() {
        mAnimatorSet.setupStartValues();
    }

    @Override
    public void setupEndValues() {
        mAnimatorSet.setupEndValues();
    }

    @Override
    public void setTarget(Object target) {
        mAnimatorSet.setTarget(target);
    }

    @Override
    public long getStartDelay() {
        return mAnimatorSet.getStartDelay();
    }

    @Override
    public void setStartDelay(long l) {
        mAnimatorSet.setStartDelay(l);
    }

    @Override
    public Animator setDuration(long l) {
        mAnimatorSet.setDuration(l);
        return this;
    }

    @Override
    public long getDuration() {
        return mAnimatorSet.getDuration();
    }

    @Override
    public void setInterpolator(TimeInterpolator timeInterpolator) {
        mAnimatorSet.setInterpolator(timeInterpolator);
    }

    @Override
    public boolean isRunning() {
        return mAnimatorSet.isRunning();
    }

    public Builder play(DisappearAnimator animator) {
        if (animator != null) {
            return new Builder(mAnimatorSet.play(animator), animator);
        }
        return null;
    }

    private void addPlaceholder(TargetElement targetElement) {
        ArrayList<Animator> animators = mAnimatorSet.getChildAnimations();
        for (Animator animator : animators) {
            DisappearAnimator disappearAnimator = (DisappearAnimator) animator;
            disappearAnimator.addTargetElement(targetElement);
        }
    }

    public class Builder {

        private AnimatorSet.Builder mBuilder;
        private DisappearAnimator mDisappearAnimator;

        Builder(AnimatorSet.Builder builder, DisappearAnimator disappearAnimator) {
            mBuilder = builder;
            mDisappearAnimator = disappearAnimator;
        }

        public Builder with(Rect rect) {
            mDisappearAnimator.addRect(rect);
            return this;
        }

        public Builder before(DisappearAnimator animator) {
            mBuilder.before(animator);
            Builder builder = play(animator);
            mBuilder = builder.mBuilder;
            mDisappearAnimator = builder.mDisappearAnimator;
            return this;
        }

        public void addListener(AnimatorListener listener) {
            DisappearAnimatorSet.this.addListener(listener);
        }

        public void addPlaceholder(TargetElement targetElement) {
            DisappearAnimatorSet.this.addPlaceholder(targetElement);
        }
    }
}
