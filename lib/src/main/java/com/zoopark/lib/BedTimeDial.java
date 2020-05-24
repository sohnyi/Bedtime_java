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

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;


/**
 * 就寝自定义 View
 */

public class BedTimeDial extends View {

    private Context mContext;

    /* xml设定的值 *********************************************************************************/
    // 视图宽度
    private int mWidth;

    // 视图高度
    private int mHeight;

    // 睡眠时间带宽度
    private float mStroke;

    // 表盘半径
    private float mRadius;

    // 表盘渐变起始颜色
    private int mSleepColor;

    // 表盘渐变终点颜色
    private int mWeakColor;

    // 睡觉点图片
    private int mSleepResID;

    // 起床点图片
    private int mWeakResID;

    // 睡觉时间_初始小时值, 默认-23
    private int mSleepHr;

    // 睡觉时间_初始分钟值, 默认-0
    private int mSleepMin;

    // 起床时间_初始小时值， 默认-7
    private int mWeakUpHr;

    // 起床时间_初始分钟值， 默认-0
    private int mWeakUpMin;

    /* 画笔 ****************************************************************************************/
    // 表盘画笔
    private Paint mDialPaint;

    // 睡眠时间带画笔
    private Paint mBedtimePaint;
    private Paint mSleepFillPaint;

    // 起床时间设定按钮画笔
    private Paint mWeakPaint;

    // 睡眠时长数字画笔
    private Paint mTextNumPaint;

    // 睡眠时长单位画笔
    private Paint mTextUnitPaint;

    private Path mSleepPath;
    private Rect mSleepRect;
    private Paint mSleepPain;

    private Path mWeakPath;
    private Rect mWeakReact;
    private Paint mWeakPointPaint;
    private SweepGradient mSweepGradient;


    private Bitmap mSleepBtm;
    private Bitmap mWeakBtm;
    private int[] mGradientColors = new int[2];
    private float[] mGradientPos = new float[2];
    private int[] mBedTime = new int[2];

    // 是否为睡眠时间点移动
    private boolean isSleepTimeMove;
    // 是否为起床时间点移动
    private boolean isWeakTimeMove;
    // 是否为睡眠时间带移动
    private boolean isBedtimeMove;

    // 中心点坐标
    private float mCenterX, mCenterY;
    // 上一次睡觉设定的时间位置
    private float mSleepX, mSleepY;
    // 上一次起床设定的时间位置
    private float mWeakUpX, mWeakUpY;
    // 睡觉时间点角度
    private float mSleepAngle;
    // 起床床时间点角度
    private float mWeakUpAngle;
    // 上一次移动时的角度
    private float mLastMoveAngle;
    // 旋转的角度
    private float mDegrees;

    private float mNumTextHeight;

    /* Listener ***********************************************************************************/
    // 就寝时间改动监听
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
        mStroke = typedArray.getDimension(R.styleable.BedTimeDial_btd_stroke, 0);
        mSleepColor = typedArray.getColor(R.styleable.BedTimeDial_sleep_color,
                ContextCompat.getColor(mContext, android.R.color.holo_orange_dark));
        mWeakColor = typedArray.getColor(R.styleable.BedTimeDial_weakUp_color,
                ContextCompat.getColor(mContext, android.R.color.holo_orange_light));
        mGradientColors[0] = mSleepColor;
        mGradientColors[1] = mWeakColor;

        mSleepHr = typedArray.getInt(R.styleable.BedTimeDial_sleep_hr, 23);
        mSleepMin = typedArray.getInt(R.styleable.BedTimeDial_sleep_min, 0);
        mWeakUpHr = typedArray.getInt(R.styleable.BedTimeDial_weakUp_hr, 7);
        mWeakUpMin = typedArray.getInt(R.styleable.BedTimeDial_weakUp_min, 0);

        mSleepResID = typedArray.getResourceId(R.styleable.BedTimeDial_sleepSrc, R.drawable.ic_sleep);
        mWeakResID = typedArray.getResourceId(R.styleable.BedTimeDial_weakUpSrc, R.drawable.ic_sun_up);

        typedArray.recycle();

        init();
        initPaint();

    }

    private void init() {
        isSleepTimeMove = false;
        isWeakTimeMove = false;
        isBedtimeMove = false;
    }

    private void initPaint() {

        mSleepPath = new Path();
        mSleepRect = new Rect();

        mWeakPath = new Path();
        mWeakReact = new Rect();

        mDialPaint = new Paint();
        mDialPaint.setStyle(Paint.Style.STROKE);
        mDialPaint.setColor(ContextCompat.getColor(mContext, android.R.color.background_light));
        mDialPaint.setAntiAlias(true);

        mBedtimePaint = new Paint();
        mBedtimePaint.setStyle(Paint.Style.STROKE);
        mBedtimePaint.setStrokeCap(Paint.Cap.ROUND);
        mBedtimePaint.setAntiAlias(true);

        mSleepBtm = BitmapFactory.decodeResource(mContext.getResources(), mSleepResID);
        mSleepBtm = Bitmap.createScaledBitmap(mSleepBtm, (int) mStroke, (int) mStroke, false);

        mSleepPain = new Paint();
        mSleepPain.setAntiAlias(true);


        mSleepFillPaint = new Paint();
        mSleepFillPaint.setStyle(Paint.Style.STROKE);
        mSleepFillPaint.setStrokeWidth(8);
        mSleepFillPaint.setColor(mSleepColor);
        mSleepFillPaint.setAntiAlias(true);

        mWeakPaint = new Paint();
        mWeakPaint.setStyle(Paint.Style.STROKE);
        mWeakPaint.setStrokeWidth(8);
        mWeakPaint.setColor(mWeakColor);
        mWeakPaint.setAntiAlias(true);

        mWeakPointPaint = new Paint();
        mWeakPointPaint.setAntiAlias(true);

        mWeakBtm = BitmapFactory.decodeResource(mContext.getResources(), mWeakResID);
        mWeakBtm = Bitmap.createScaledBitmap(mWeakBtm, (int) mStroke, (int) mStroke, false);


        mTextNumPaint = new Paint();
        int numTextSize = 64;
        mTextNumPaint.setTextSize(DensityUtils.sp2px(mContext, numTextSize));
        mTextNumPaint.setColor(ContextCompat.getColor(mContext, android.R.color.black));
        mTextNumPaint.setAntiAlias(true);

        mTextUnitPaint = new Paint();
        int unitTextSize = 36;
        mTextUnitPaint.setTextSize(DensityUtils.sp2px(mContext, unitTextSize));
        mTextUnitPaint.setColor(ContextCompat.getColor(mContext, android.R.color.black));
        mTextUnitPaint.setAntiAlias(true);

        Paint.FontMetrics fontMetrics = mTextNumPaint.getFontMetrics();
        mNumTextHeight = fontMetrics.descent - fontMetrics.ascent;

        Rect bounds = new Rect();
        mTextNumPaint.getTextBounds("1", 0, 1, bounds);
        mNumTextHeight = bounds.height();

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

        int shortWide = Math.min(mWidth, mHeight);

        if (mStroke <= 0) {
            mStroke = shortWide / 8.0f;
        } else if (mStroke > (shortWide / 6.0f)) {
            mStroke = shortWide / 6.0f;
        }

        mRadius = (shortWide - mStroke) / 2;
        mSleepAngle = getAngleByTime(mSleepHr, mSleepMin);
        mWeakUpAngle = getAngleByTime(mWeakUpHr, mWeakUpMin);

        mSleepX = getPosByAngle(mSleepAngle)[0];
        mSleepY = getPosByAngle(mSleepAngle)[1];
        mWeakUpX = getPosByAngle(mWeakUpAngle)[0];
        mWeakUpY = getPosByAngle(mWeakUpAngle)[1];

        updateParams();
        updateBedTime();
        updatePaint();
    }

    /**
     * 画笔初始化
     */
    private void updatePaint() {
        mDialPaint.setStrokeWidth(mStroke);
        mBedtimePaint.setStrokeWidth(mStroke);
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

        // 画就寝时间段弧线
        canvas.save();
        mBedtimePaint.setShader(mSweepGradient);
        canvas.rotate(mDegrees, mCenterX, mCenterY);
        canvas.drawArc(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius,
                mCenterY + mRadius, mSleepAngle - mDegrees, calPaintAngle(mBedTime), false, mBedtimePaint);
        canvas.restore();

        // 画睡觉时间点
        canvas.save();
        canvas.clipPath(mSleepPath);
        canvas.drawBitmap(mSleepBtm, null, mSleepRect, mSleepPain);
        canvas.restore();
        canvas.drawCircle(mSleepX, mSleepY, mStroke / 2 + 4, mSleepFillPaint);

        // 画起床时间点
        canvas.save();
        canvas.clipPath(mWeakPath);
        canvas.drawBitmap(mWeakBtm, null, mWeakReact, mWeakPointPaint);
        canvas.restore();
        canvas.drawCircle(mWeakUpX, mWeakUpY, mStroke / 2 + 4, mWeakPaint);

    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case ACTION_DOWN:
                performClick();
                mLastMoveAngle = getAngle(event.getX(), event.getY());
                if (canDrag(event.getX(), event.getY(), mSleepX, mSleepY)) {
                    isSleepTimeMove = true;
                } else if (canDrag(event.getX(), event.getY(), mWeakUpX, mWeakUpY)) {
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
                    onSleepMoved(diffAngle); // 睡觉时间点移动
                } else if (isWeakTimeMove) {
                    onWeakUpMoved(diffAngle); // 起床时间点移动
                } else if (isBedtimeMove) {
                    onBedtimeMoved(diffAngle); // 就寝时间带移动
                }
                mLastMoveAngle = currentAngle;
                if (isSleepTimeMove || isWeakTimeMove || isBedtimeMove) {
                    updateParams();
                    updateBedTime();
                    invalidate();
                }
                break;
            case ACTION_UP:
            case ACTION_CANCEL:
                isSleepTimeMove = false;
                isWeakTimeMove = false;
                isBedtimeMove = false;
            default:
                break;
        }
        return true;
    }



    /* PRIVATE METHOD ************************************************************************/


    /* ON DRAW ******************/

    /**
     * @param diffAngle moved angle
     *                  change the params after bedtime moved
     */
    private void onBedtimeMoved(float diffAngle) {
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

    /**
     * @param diffAngle moved angle
     *                  change the params after weak up moved
     */
    private void onWeakUpMoved(float diffAngle) {
        mWeakUpAngle = (mWeakUpAngle + diffAngle + 720) % 720;
        mWeakUpX = getPosByAngle(mWeakUpAngle)[0];
        mWeakUpY = getPosByAngle(mWeakUpAngle)[1];
        if (mChangedListener != null) {
            mChangedListener.onWeakUpTimeChanged(getAngleTime(mWeakUpAngle)[0], getAngleTime(mWeakUpAngle)[1]);
        }
    }

    /**
     * @param diffAngle moved angle
     *                  change the params after sleep moved
     */
    private void onSleepMoved(float diffAngle) {
        mSleepAngle = (mSleepAngle + diffAngle + 720) % 720;
        mSleepX = getPosByAngle(mSleepAngle)[0];
        mSleepY = getPosByAngle(mSleepAngle)[1];
        if (mChangedListener != null) {
            mChangedListener.onSleepTimeChanged(getAngleTime(mSleepAngle)[0], getAngleTime(mSleepAngle)[1]);
        }
    }

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
    private void updateParams() {

        mDegrees = getSweepOppositeAngle(mSleepAngle, mWeakUpAngle);
        mGradientPos[0] = (mSleepAngle - mDegrees + 360) % 360 / 360f;
        mGradientPos[1] = (mWeakUpAngle - mDegrees + 360) % 360 / 360f;
        mSweepGradient = new SweepGradient(mCenterX, mCenterY, mGradientColors, mGradientPos);

        mSleepPath.reset();
        mSleepPath.addCircle(mSleepX, mSleepY, mStroke / 2, Path.Direction.CW);

        mSleepRect.set((int) (mSleepX - mStroke / 2 - 1.5f), (int) (mSleepY - mStroke / 2 - 1.5f),
                (int) (mSleepX + mStroke / 2 + 2.5f), (int) (mSleepY + mStroke / 2 + 2.5f));


        mWeakPath.reset();
        mWeakPath.addCircle(mWeakUpX, mWeakUpY, mStroke / 2, Path.Direction.CW);
        mWeakReact.set((int) (mWeakUpX - mStroke / 2 - 1.5f), (int) (mWeakUpY - mStroke / 2 - 1.5f),
                (int) (mWeakUpX + mStroke / 2 + 2.5f), (int) (mWeakUpY + mStroke / 2 + 2.5f));
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
     * 获取就寝时间中心点对面点角度
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

    /* 函数计算 ************************************************************************************/

    /**
     * 获取反正切
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

    /**
     * 就寝时间变动接口
     **/
    public interface TimeChangedListener {

        /**
         * 睡觉时间点变动
         *
         * @param hr  变动后的小时值
         * @param min 变动后的的分钟值
         */
        void onSleepTimeChanged(int hr, int min);

        /**
         * 起床时间点变动
         *
         * @param hr  变动后的小时值
         * @param min 变动后的分钟值
         */
        void onWeakUpTimeChanged(int hr, int min);

        /**
         * 睡眠时间带变动
         *
         * @param sleepHr   变动后的睡觉寝小时值
         * @param sleepMin  变动后的睡觉分钟值
         * @param weakUpHr  变动后的起床小时值
         * @param weakUpMin 变动后的起床分钟值
         */
        void onBedtimeChanged(int sleepHr, int sleepMin, int weakUpHr, int weakUpMin);
    }
}
