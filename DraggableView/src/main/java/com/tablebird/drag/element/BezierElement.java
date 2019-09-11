package com.tablebird.drag.element;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import androidx.annotation.ColorInt;

/**
 * @author tablebird
 * @date 2019/7/14
 */
public class BezierElement extends CoverElement {

    /**
     * 原位置圆心大小
     */
    private float mAnchorRadius;

    /**
     * 原位置中心X坐标
     */
    private float mAnchorCenterX;
    /**
     * 原位置中心Y坐标
     */
    private float mAnchorCenterY;

    /**
     * 拖动图标的宽度
     */
    private float mTargetHalfWidth;
    /**
     * 拖动图标的高度
     */
    private float mTargetHalfHeight;

    private Paint mPaint = new Paint();

    private Target mTarget;

    public BezierElement(Target target) {
        mTarget = target;
        initPaint();
    }

    private void initPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public void setAnchorRadius(float anchorRadius) {
        mAnchorRadius = anchorRadius;
    }

    public void setAnchorCenter(float anchorCenterX, float anchorCenterY) {
        mAnchorCenterX = anchorCenterX;
        mAnchorCenterY = anchorCenterY;
    }

    public void setTargetHalf(float targetHalfWidth, float targetHalfHeight) {
        mTargetHalfWidth = targetHalfWidth;
        mTargetHalfHeight = targetHalfHeight;
    }

    public void setBezierColor(@ColorInt int bezierColor) {
        mPaint.setColor(bezierColor);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mTarget == null || mTarget.getRect() == null) {

            return;
        }
        canvas.drawCircle(mAnchorCenterX, mAnchorCenterY, mAnchorRadius, mPaint);
        drawBezier(canvas);
    }

    private void drawBezier(Canvas canvas) {
        Point[] points = calculate(new Point(mAnchorCenterX, mAnchorCenterY), new Point(mTarget.getRect().centerX(), mTarget.getRect().centerY()));
        float centerX = (points[0].x + points[1].x + points[2].x + points[3].x) / 4f;
        float centerY = (points[0].y + points[1].y + points[2].y + points[3].y) / 4f;
        Path path1 = new Path();
        path1.moveTo(points[0].x, points[0].y);
        path1.quadTo(centerX, centerY, points[1].x, points[1].y);
        path1.lineTo(points[3].x, points[3].y);
        path1.quadTo(centerX, centerY, points[2].x, points[2].y);
        path1.lineTo(points[0].x, points[0].y);
        canvas.drawPath(path1, mPaint);
    }

    /**
     * 计算退拽效果Point[]
     *
     * @param start 开始
     * @param end   结束
     * @return 获取贝塞尔曲线的关键点
     */
    private Point[] calculate(Point start, Point end) {
        float a = end.x - start.x;
        float b = end.y - start.y;

        float v = a * a / (a * a + b * b);
        float y1 = (float) Math.sqrt((v) * (mAnchorRadius) * (mAnchorRadius));
        float x1 = -b / a * y1;

        float y2 = (float) Math.sqrt((v) * mTargetHalfWidth * mTargetHalfHeight);
        float x2 = -b / a * y2;

        Point[] result = new Point[4];

        result[0] = new Point(start.x + x1, start.y + y1);
        //防止贝塞尔曲线溢出边界
        float resultX1 = constraintOverflow((int) (x2 + end.x), mTarget.getRect().left, mTarget.getRect().right);
        float resultY1 = constraintOverflow((int) (y2 + end.y), mTarget.getRect().top, mTarget.getRect().bottom);
        result[1] = new Point(resultX1, resultY1);

        result[2] = new Point(start.x - x1, start.y - y1);
        //防止贝塞尔曲线溢出边界
        float resultX3 = constraintOverflow((int) (end.x - x2), mTarget.getRect().left, mTarget.getRect().right);
        float resultY3 = constraintOverflow((int) (end.y - y2), mTarget.getRect().top, mTarget.getRect().bottom);
        result[3] = new Point(resultX3, resultY3);

        return result;
    }

    /**
     * 约束溢出的数字在规定的范围内
     *
     * @param digital 数据
     * @param min     最小值
     * @param max     最大值
     * @return 数据
     */
    private float constraintOverflow(int digital, int min, int max) {
        float result = digital;
        if (result < min) {
            result = min;
        } else if (result > max) {
            result = max;
        }
        return result;
    }

    @Override
    public void clean() {

    }

    public interface Target {
        Rect getRect();
    }

    class Point {
        float x, y;

        Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
