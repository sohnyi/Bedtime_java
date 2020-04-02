package com.zoopark.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.SweepGradient;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;


/**
 * 就寝自定义 View
 */

public class BedTimeDial extends View {

    private static final int DEFAULT_SIZE = 288;
    private static final int DEFAULT_STROKE = 32;

    private Context mContext;

    private Paint mDialPaint; // 表盘画笔
    private Paint mBedtimePaint; // 睡眠时间带画笔
    private Paint mSleepFillPaint;
    private Paint mWeakPaint; // 起床时间设定按钮画笔
    private Paint mTextNumPaint; // 睡眠时长数字画笔
    private Paint mTextUnitPaint; // 睡眠时长单位画笔

    // 可设定的值
    private int mWidth; // 视图宽度
    private int mHeight; // 视图高度
    private float mStroke; // 睡眠时间带宽度
    private float mRadius; // 表盘半径
    private int mSleepColor; // 表盘渐变起始颜色
    private int mWeakColor; // 表盘渐变终点颜色
    private int mSleepResID; // 睡觉点图片
    private int mWeakResID; // 起床点图片
    private int mSleepHr; // 睡觉时间_小时
    private int mSleepMin; // 睡觉时间_分钟
    private int mWeakUpHr; // 起床时间_小时
    private int mWeakUpMin; // 起床时间_分钟

    private Bitmap mSleepBtm;
    private Bitmap mWeakBtm;
    private int[] mGradientColors = new int[2];
    private float[] mGradientPos = new float[2];
    private int[] mBedTime = new int[2];

    private boolean isSleepTimeMove; // 是否为睡眠时间点移动
    private boolean isWeakTimeMove; // 是否为起床时间点移动
    private boolean isBedtimeMove; // 是否为睡眠时间带移动

    private float mCenterX, mCenterY; // 中心点位置
    private float mSleepX, mSleepY; // 上一次睡觉设定的时间位置
    private float mWeakUpX, mWeakUpY; // 上一次起床设定的时间位置

    private float mSleepAngle; // 睡觉时间点角度
    private float mWeakUpAngle; // 亲床时间点角度
    private float mSweepAngle; // 睡眠时间带弧度
    private float mLastMoveAngle; // 上一次移动时的角度
    private float mDegrees; // 旋转的角度

    private float mNumTextHeight;

    private TimeChangedListener mChangedListener;

    public BedTimeDial(Context context) {
        this(context, null);
    }

    public BedTimeDial(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BedTimeDial(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mChangedListener = (TimeChangedListener) mContext;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BedTimeDial);
        mStroke = typedArray.getDimension(R.styleable.BedTimeDial_btd_stroke, DEFAULT_STROKE);
        mSleepColor = typedArray.getColor(R.styleable.BedTimeDial_sleep_color,
                ContextCompat.getColor(mContext, android.R.color.holo_orange_dark));
        mWeakColor = typedArray.getColor(R.styleable.BedTimeDial_weakUp_color,
                ContextCompat.getColor(mContext, android.R.color.holo_orange_light));
        mGradientColors[0] = mSleepColor;
        mGradientColors[1] = mWeakColor;

        mSleepHr = typedArray.getInt(R.styleable.BedTimeDial_sleep_hr, 22);
        mSleepMin = typedArray.getInt(R.styleable.BedTimeDial_sleep_min, 0);
        mWeakUpHr = typedArray.getInt(R.styleable.BedTimeDial_weakUp_hr, 7);
        mWeakUpMin = typedArray.getInt(R.styleable.BedTimeDial_weakUp_min, 0);

        mSleepResID = typedArray.getResourceId(R.styleable.BedTimeDial_sleepSrc, R.drawable.ic_sleep);
        mWeakResID = typedArray.getResourceId(R.styleable.BedTimeDial_weakUpSrc, R.drawable.ic_sun_up);

        initPain();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mCenterX = mWidth / 2.0f;
        mCenterY = mHeight / 2.0f;

        initParams();
    }

    /**
     * 数据初始化
     */
    private void initParams() {

        isSleepTimeMove = false;
        isWeakTimeMove = false;
        isBedtimeMove = false;

        int shortWide = Math.min(mWidth, mHeight);

        mRadius = (Math.min(mWidth, mHeight) - mStroke) / 2;

        if (mStroke <= 0 || mStroke > shortWide / 2) {
            mStroke = Math.min(shortWide / 2, DensityUtils.dp2px(mContext, DEFAULT_STROKE));
        }

        mRadius = (shortWide - mStroke) / 2 - 8;

        mSleepAngle = getAngleByTime(mSleepHr, mSleepMin);
        mWeakUpAngle = getAngleByTime(mWeakUpHr, mWeakUpMin);

        mSleepX = getPosByAngle(mSleepAngle)[0];
        mSleepY = getPosByAngle(mSleepAngle)[1];
        mWeakUpX = getPosByAngle(mWeakUpAngle)[0];
        mWeakUpY = getPosByAngle(mWeakUpAngle)[1];

        setParams();

    }

    /**
     * 画笔初始化
     */
    private void initPain() {

        // 初始化表带画笔
        mDialPaint = new Paint();
        mDialPaint.setStyle(Paint.Style.STROKE);
        mDialPaint.setStrokeWidth(mStroke);
        mDialPaint.setColor(ContextCompat.getColor(mContext, android.R.color.background_light));
        mDialPaint.setAntiAlias(true);

        // 初始化睡眠时间带画笔
        mBedtimePaint = new Paint();
        mBedtimePaint.setStyle(Paint.Style.STROKE);
        mBedtimePaint.setStrokeWidth(mStroke);
        mBedtimePaint.setStrokeCap(Paint.Cap.ROUND);
        mBedtimePaint.setAntiAlias(true);


        // 初始化睡觉时间点图片
        mSleepBtm = BitmapFactory.decodeResource(mContext.getResources(), mSleepResID);
        mSleepBtm = Bitmap.createScaledBitmap(mSleepBtm, (int) mStroke, (int) mStroke, false);

        mSleepFillPaint = new Paint();
        mSleepFillPaint.setStyle(Paint.Style.STROKE);
        mSleepFillPaint.setStrokeWidth(8);
        mSleepFillPaint.setColor(mSleepColor);
        mSleepFillPaint.setAntiAlias(true);

        mWeakBtm = BitmapFactory.decodeResource(mContext.getResources(), mWeakResID);
        mWeakBtm = Bitmap.createScaledBitmap(mWeakBtm, (int) mStroke, (int) mStroke, false);
        mWeakPaint = new Paint();
        mWeakPaint.setStyle(Paint.Style.STROKE);
        mWeakPaint.setStrokeWidth(8);
        mWeakPaint.setColor(mWeakColor);
        mWeakPaint.setAntiAlias(true);


        mTextNumPaint = new Paint();
        int numTextSize = 64;
        mTextNumPaint.setTextSize(DensityUtils.sp2px(mContext, numTextSize));
        mTextNumPaint.setColor(ContextCompat.getColor(mContext, android.R.color.black));
//        mTextNumPaint.setTextAlign(Paint.Align.CENTER);
        mTextNumPaint.setAntiAlias(true);

        mTextUnitPaint = new Paint();
        int unitTextSize = 36;
        mTextUnitPaint.setTextSize(DensityUtils.sp2px(mContext, unitTextSize));
        mTextUnitPaint.setColor(ContextCompat.getColor(mContext, android.R.color.black));
        mTextUnitPaint.setAntiAlias(true);
//        mTextUnitPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fontMetrics = mTextNumPaint.getFontMetrics();
        mNumTextHeight = fontMetrics.descent - fontMetrics.ascent;

        Rect bounds = new Rect();
        mTextNumPaint.getTextBounds("1", 0, 1, bounds);
        mNumTextHeight = bounds.height();


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //draw dial
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mDialPaint);

        // draw duration text
        float hrWidth = mTextNumPaint.measureText(String.valueOf(mBedTime[0]));
        float unitHWidth = mTextUnitPaint.measureText("h");
        float minWidth = 0;
        float unitMWidth = 0;
        if (mBedTime[1] > 0) {
            minWidth = mTextNumPaint.measureText(String.valueOf(mBedTime[1]));
            unitMWidth = mTextUnitPaint.measureText("m");
        }

        float textWidth = hrWidth + unitHWidth + minWidth + unitMWidth;
        float startX = mCenterX - 0.5f * textWidth;

        canvas.drawText(String.valueOf(mBedTime[0]), startX, mCenterY + 0.25f * mNumTextHeight, mTextNumPaint);
        canvas.drawText("h", startX + hrWidth, mCenterY + 0.25f * mNumTextHeight, mTextUnitPaint);
        if (mBedTime[1] > 0) {
            canvas.drawText(String.valueOf(mBedTime[1]), startX + hrWidth + unitHWidth + 16,
                    mCenterY + 0.25f * mNumTextHeight, mTextNumPaint);
            canvas.drawText("m", startX + hrWidth + unitHWidth + minWidth + 16,
                    mCenterY + 0.25f * mNumTextHeight, mTextUnitPaint);
        }

        // 画睡眠时间段弧线
        canvas.save();
        mBedtimePaint.setShader(new SweepGradient(mCenterX, mCenterY, mGradientColors, mGradientPos));
        canvas.rotate(mDegrees, mCenterX, mCenterY);
        canvas.drawArc(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius,
                mCenterY + mRadius, mSleepAngle - mDegrees, calPaintAngle(mBedTime), false, mBedtimePaint);
        canvas.restore();

        // 画睡觉时间点
        canvas.save();
        Path sleepPath = new Path();
        sleepPath.addCircle(mSleepX, mSleepY, mStroke / 2, Path.Direction.CW);
        canvas.clipPath(sleepPath);
        Rect sleepRect = new Rect((int) (mSleepX - mStroke / 2 - 1.5f), (int) (mSleepY - mStroke / 2 - 1.5f),
                (int) (mSleepX + mStroke / 2 + 2.5f), (int) (mSleepY + mStroke / 2 + 2.5f));
        Paint sleepPaint = new Paint();
        sleepPaint.setAntiAlias(true);
        canvas.drawBitmap(mSleepBtm, null, sleepRect, sleepPaint);
        canvas.restore();
        canvas.drawCircle(mSleepX, mSleepY, mStroke / 2 + 4, mSleepFillPaint);

        // 画起床时间点
        canvas.save();
        Path weakPath = new Path();
        weakPath.addCircle(mWeakUpX, mWeakUpY, mStroke / 2, Path.Direction.CW);
        canvas.clipPath(weakPath);
        Rect weakRect = new Rect((int) (mWeakUpX - mStroke / 2 - 1.5f), (int) (mWeakUpY - mStroke / 2 - 1.5f),
                (int) (mWeakUpX + mStroke / 2 + 2.5f), (int) (mWeakUpY + mStroke / 2 + 2.5f));
        Paint weakPaint = new Paint();
        weakPaint.setAntiAlias(true);
        canvas.drawBitmap(mWeakBtm, null, weakRect, weakPaint);
        canvas.restore();
        canvas.drawCircle(mWeakUpX, mWeakUpY, mStroke / 2 + 4, mWeakPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case ACTION_DOWN:
                mLastMoveAngle = getAngle(event.getX(), event.getY());
                if (canDrag(event.getX(), event.getY(), mSleepX, mSleepY)) {
                    isSleepTimeMove = true;
                } else if (canDrag(event.getX(), event.getY(), mWeakUpX, mWeakUpY)){
                    isWeakTimeMove = true;
                } else if (isInDial(event.getX(), event.getY())) {
                    isBedtimeMove = true;
                }
                break;
            case ACTION_MOVE:
                float currentAngle = getAngle(event.getX(), event.getY());
                // 移动的角度
                float diffAngle = getDiffAngle(currentAngle, mLastMoveAngle);
                if (isSleepTimeMove) {
                    // 睡觉时间点移动
                    mSleepAngle = (mSleepAngle + diffAngle + 720) % 720;
                    mSleepX = getPosByAngle(mSleepAngle)[0];
                    mSleepY = getPosByAngle(mSleepAngle)[1];
                    if (mChangedListener != null) {
                        mChangedListener.onSleepTimeChanged(getAngleTime(mSleepAngle)[0], getAngleTime(mSleepAngle)[1]);
                    }
                } else if (isWeakTimeMove) {
                    // 起床时间点移动
                    mWeakUpAngle = (mWeakUpAngle + diffAngle + 720) % 720;
                    mWeakUpX = getPosByAngle(mWeakUpAngle)[0];
                    mWeakUpY = getPosByAngle(mWeakUpAngle)[1];
                    if (mChangedListener != null) {
                        mChangedListener.onWeakUpTimeChanged(getAngleTime(mWeakUpAngle)[0], getAngleTime(mWeakUpAngle)[1]);
                    }
                } else if (isBedtimeMove) {
                    // 睡眠时间段移动
                    mSleepAngle = (mSleepAngle + diffAngle + 720) % 720;
                    mWeakUpAngle = (mWeakUpAngle + diffAngle + 720) % 720;
                    mSleepX = getPosByAngle(mSleepAngle)[0];
                    mSleepY = getPosByAngle(mSleepAngle)[1];
                    mWeakUpX = getPosByAngle(mWeakUpAngle)[0];
                    mWeakUpY = getPosByAngle(mWeakUpAngle)[1];
                    if (mChangedListener != null) {
                        mChangedListener.onBedtimeChanged(getAngleTime(mSleepAngle)[0], getAngleTime(mSleepAngle)[1],
                                getAngleTime(mWeakUpAngle)[0], getAngleTime(mWeakUpAngle)[1]);
                    }
                }
                mLastMoveAngle = currentAngle;
                if (isSleepTimeMove || isWeakTimeMove || isBedtimeMove) {
                    setParams();
                    invalidate();
                }
                break;
            case ACTION_UP:
                isSleepTimeMove = false;
                isWeakTimeMove = false;
                isBedtimeMove = false;
            default:
                break;
        }
        return true;
    }

    /** PRIVATE METHOD ************************************************************************/

    /**
     * @return the hour and minute angles
     */
    private float getAngleByTime(float hr, float min) {
        if (hr < 0 || hr > 23) {
            hr = 0;
        }

        if (min < 0) {
            min = 0;
        } else if (min > 55) {
            hr += 1;
            min = 0;
        } else {
            min = (min + 2) / 5 * 5; // set the minimum scale for the minutes to 5.
        }

        float angle = hr * 30 - 90; // 30 degrees per hour and coordinate with the clock have 90 degree difference
        angle += min * 0.5f;
        return angle;
    }


    /**
     * 设置参数
     */
    private void setParams() {
        if (!isBedtimeMove) {
            mSweepAngle = getSweepAngle(mSleepAngle, mWeakUpAngle);
        }
        mDegrees = getSweepOppositeAngle(mSleepAngle, mWeakUpAngle);
        mGradientPos[0] = (mSleepAngle - mDegrees + 360) % 360 / 360f;
        mGradientPos[1] = (mWeakUpAngle - mDegrees + 360) % 360 / 360f;

        updateBedTime();
    }

    /**
     * 是否可以拖动睡眠或起床时间点
     *
     * @param eventX 触摸点 X 坐标
     * @param eventY 触摸点 Y 坐标
     * @param lastX  上一次 X 坐标
     * @param lastY  上一次 Y 坐标
     */
    private boolean canDrag(float eventX, float eventY, float lastX, float lastY) {
        float dis = getPointDistance(eventX, eventY, lastX, lastY);
        return dis <= mStroke;
    }

    /**
     * 是否可以拖动睡眠时间带
     *
     * @param x 触摸点 X 坐标
     * @param y 触摸点 Y 坐标
     */
    private boolean canBedtimeDrag(float x, float y) {
        float angle = getAngle(x, y) % 360;
        float startAngle = mSleepAngle % 360;
        float endAngle = mWeakUpAngle % 360;
        if (startAngle > endAngle) {
            return (angle > startAngle) || (angle < endAngle);
        } else if (startAngle < endAngle) {
            return (angle > startAngle) && (angle < endAngle);
        }
        return false;
    }


    /**
     * 获取两点之间的距离
     *
     * @return 距离
     */
    private float getPointDistance(float srcX, float srcY, float dstX, float dstY) {
        return (float) Math.sqrt((srcX - dstX) * (srcX - dstX) + (srcY - dstY) * (srcY - dstY));
    }

    /**
     * 是否在表带内
     */
    private boolean isInDial(float x, float y) {
        float distance = getPointDistance(x, y, mCenterX, mCenterY);
        return (distance >= mRadius - mStroke / 2 && distance <= mRadius + mStroke / 2);
    }

    /**
     * 获取绘制划过的角度
     */
    private float getSweepAngle(float startAngle, float endAngle) {
        if (startAngle > endAngle) {
            return 720 - (startAngle - endAngle);
        } else {
            return endAngle - startAngle;
        }
    }

    /**
     * @return the angle need to pain of bedtime
     */
    private float calPaintAngle(int[] bedtime) {
        if (bedtime[0] == 12 && bedtime[1] == 0) {
            return 360;
        } else {
            return (bedtime[0] * 30 + bedtime[1] * 0.5f) % 360;
        }
    }

    /**
     * update the sleep and weak up time
     */
    private void updateBedTime() {
        if (!isBedtimeMove) {
            int hr, min;
            int sleepH = getAngleTime(mSleepAngle)[0];
            int sleepM = getAngleTime(mSleepAngle)[1];
            int weakUpH = getAngleTime(mWeakUpAngle)[0];
            int weakUpM = getAngleTime(mWeakUpAngle)[1];

            if (weakUpH >= sleepH) {
                hr = weakUpH - sleepH;
            } else {
                hr = 24 - sleepH + weakUpH;
            }

            min = weakUpM - sleepM;
            if (min < 0) {
                hr -= 1;
                min += 60;
            }
            if (hr < 0) {
                hr += 24;
            }
            mBedTime[0] = hr;
            mBedTime[1] = min;
        }
    }

    /**
     * 获取实际显示的位置
     *
     * @param eX 原 X 坐标
     * @param eY 原 Y 坐标
     */
    private float[] getShowPos(float eX, float eY) {
        float[] showPos = new float[2];
        showPos[0] = (float) Math.cos(getAtan2(eX, eY)) * mRadius + mCenterX;
        showPos[1] = (float) Math.sin(getAtan2(eX, eY)) * mRadius + mCenterY;
        return showPos;
    }

    /**
     * 获取移动一定角度后的坐标
     *
     * @param angle 移动后的角度
     * @return 移动后的坐标
     */
    private float[] getPosByAngle(float angle) {
        float[] pos = new float[2];
        pos[0] = (float) -Math.cos((180 - angle) / 180 * Math.PI) * mRadius + mCenterX;
        pos[1] = (float) Math.sin((180 - angle) / 180 * Math.PI) * mRadius + mCenterY;
        return pos;
    }

    /**
     * 获取移动的角度
     *
     * @param currAngle 当前角度
     * @param lastAngle 上一次的角度
     * @return 实际移动的角度
     */
    private float getDiffAngle(float currAngle, float lastAngle) {
        float diffAngle = currAngle - lastAngle;
        if (Math.abs(diffAngle) > 300) {
            diffAngle = Math.abs(diffAngle);
            diffAngle -= 360;
        }
        return diffAngle;
    }

    /**
     * 获取角度对应的时间点
     *
     * @param angle 角度
     * @return 时间点
     */
    private int[] getAngleTime(float angle) {
        int[] time = new int[2];
        angle += 90;

        // 获取小时
        int hour = (int) angle % 720 / 30;

        // 获取分钟 刻度为 5分钟
        int minute = (int) (angle - hour * 30) * 2;
        minute = (minute) / 5 * 5;

        // 四舍五入，向上进等于60时，小时进1
        if (minute == 60) {
            hour += 1;
        }

        minute = minute % 60;

        time[0] = hour;
        time[1] = minute;
        return time;
    }

    /**
     * 获取睡眠时间中心点对面点角度
     *
     * @param sleepAngle
     * @param weakUpAngle
     * @return
     */
    private float getSweepOppositeAngle(float sleepAngle, float weakUpAngle) {
        float angle;
        sleepAngle %= 360;
        weakUpAngle %= 360;
        // 中心点角度
        if (sleepAngle > weakUpAngle) {
            angle = (sleepAngle + weakUpAngle) / 2 + 180;
        } else {
            angle = (sleepAngle + weakUpAngle) / 2;
        }
        // 中心点对面点坐标
        return (angle + 180) % 360;
    }

    /** 函数计算 ***********************************************************************************/

    /**
     * 获取反正切
     *
     * @param x
     * @param y
     * @return
     */
    private float getAtan2(float x, float y) {
        return (float) (Math.atan2(y - mCenterY, x - mCenterX));
    }

    /**
     * 获取角度
     *
     * @param x x 坐标
     * @param y y 坐标
     * @return 角度
     */
    private float getAngle(float x, float y) {
        double angle = Math.atan2(y - mCenterY, x - mCenterX) * 180 / Math.PI;
        if (angle < 0) {
            angle += 360;
        }
        return (float) angle;
    }

    public interface TimeChangedListener {

        void onSleepTimeChanged(int hr, int min);

        void onWeakUpTimeChanged(int hr, int min);

        void onBedtimeChanged(int sleepHr, int sleepMin, int weakUpHr, int weakUpMin);
    }
}
