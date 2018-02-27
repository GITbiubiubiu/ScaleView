package xyy.scaleview.scaleview;

import java.math.BigDecimal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import xyy.scaleview.R;

/**
 * Copyright (c) 2017, Bongmi
 * All rights reserved
 * Author: xuyuanyi@bongmi.com
 */

public class VerticalScaleView extends View {

    private final int SCALE_WIDTH_BIG = 4;//大刻度线宽度
    private final int SCALE_WIDTH_SMALL = 2;//小刻度线宽度
    private final int LINE_WIDTH = 6;//指针线宽度

    private int rectPadding = 20;//圆角矩形间距
    private int rectHeight;//圆角矩形高

    private int maxScaleLength;//大刻度长度
    private int midScaleLength;//中刻度长度
    private int minScaleLength;//小刻度长度
    private int scaleSpace;//刻度间距
    private int scaleSpaceUnit;//每大格刻度间距
    private int height, width;//view高宽
    private int ruleWidth;//刻度尺宽

    private int max;//最大刻度
    private int min;//最小刻度
    private int borderUp, borderDown;//上下边界值坐标
    private float midY;//当前中心刻度y坐标
    private float originMidY;//初始中心刻度y坐标
    private float minY;//最小刻度y坐标,从最小刻度开始画刻度

    private float lastY;

    private float originValue;//初始刻度对应的值
    private float currentValue;//当前刻度对应的值

    private Paint paint;//画笔

    private Context context;

    private String descri = "身高";//描述
    private String unit = "cm";//刻度单位

    private VelocityTracker velocityTracker;//速度监测
    private float velocity;//当前滑动速度
    private float a = 1000000;//加速度
    private boolean continueScroll;//是否继续滑动

    private boolean isMeasured;

    private OnValueChangeListener onValueChangeListener;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (null != onValueChangeListener) {
                float v = (float) (Math.round(currentValue * 10)) / 10;//保留一位小数
                onValueChangeListener.onValueChanged(v);
            }
        }
    };

    public VerticalScaleView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public VerticalScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public VerticalScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        //初始化画笔
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);

    }

    //设置刻度范围
    public void setRange(int min, int max) {
        this.min = min;
        this.max = max;
        originValue = (max + min) / 2;
        currentValue = originValue;
    }

    //设置刻度单位
    public void setUnit(String unit) {
        this.unit = unit;
    }

    //设置刻度描述
    public void setDescri(String descri) {
        this.descri = descri;
    }

    //设置value变化监听
    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!isMeasured) {
            width = getMeasuredWidth();
            height = getMeasuredHeight();
            ruleWidth = width * 2 / 3;
            maxScaleLength = width / 5;
            midScaleLength = width / 6;
            minScaleLength = maxScaleLength / 2;
            scaleSpace = height / 80 > 8 ? height / 80 : 8;
            scaleSpaceUnit = scaleSpace * 10 + SCALE_WIDTH_BIG + SCALE_WIDTH_SMALL * 9;
            rectHeight = scaleSpaceUnit / 2;

            borderUp = height / 2 - ((min + max) / 2 - min) * scaleSpaceUnit;
            borderDown = height / 2 + ((min + max) / 2 - min) * scaleSpaceUnit;
            midY = (borderUp + borderDown) / 2;
            originMidY = midY;
            minY = borderDown;
            isMeasured = true;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //画刻度线
        for (int i = min; i <= max; i++) {
            //画刻度数字
            canvas.rotate(90);
            Rect rect = new Rect();
            String str = String.valueOf(i);
            paint.setColor(getResources().getColor(R.color.black));
            paint.setTextSize(40);
            paint.getTextBounds(str, 0, str.length(), rect);
            int w = rect.width();
            int h = rect.height();
            canvas.drawText(str, minY - (i - min) * scaleSpaceUnit - w / 2 - SCALE_WIDTH_BIG / 2, -(ruleWidth - maxScaleLength - h - minScaleLength / 2), paint);
            canvas.rotate(-90);
            //画大刻度线
            paint.setStrokeWidth(SCALE_WIDTH_BIG);
            canvas.drawLine(ruleWidth, minY - (i - min) * scaleSpaceUnit, ruleWidth - maxScaleLength, minY - (i - min) * scaleSpaceUnit, paint);

            if (i == min) {
                continue;//最后一条不画中小刻度线
            }
            //画中刻度线
            paint.setStrokeWidth(SCALE_WIDTH_SMALL);
            canvas.drawLine(ruleWidth, minY - (i - min) * scaleSpaceUnit + scaleSpaceUnit / 2, ruleWidth - midScaleLength, minY - (i - min) * scaleSpaceUnit + scaleSpaceUnit / 2, paint);
            //画小刻度线
            for (int j = 1; j < 10; j++) {
                if (j == 5) {
                    continue;
                }
                canvas.drawLine(ruleWidth, minY - (i - min) * scaleSpaceUnit + (SCALE_WIDTH_SMALL + scaleSpace) * j, ruleWidth - minScaleLength, minY - (i - min) * scaleSpaceUnit + (SCALE_WIDTH_SMALL + scaleSpace) * j, paint);
            }

        }

        //画竖线
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setColor(getResources().getColor(R.color.gray));
        canvas.drawLine(ruleWidth + LINE_WIDTH / 2, minY + SCALE_WIDTH_BIG / 2, ruleWidth + LINE_WIDTH / 2, minY - (max - min) * scaleSpaceUnit - SCALE_WIDTH_BIG / 2, paint);
        //画指针线
        paint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        canvas.drawLine(0, height / 2, ruleWidth, height / 2, paint);
        //画圆角矩形
        paint.setStyle(Paint.Style.FILL);
        RectF r = new RectF();
        r.left = ruleWidth + rectPadding;
        r.top = height / 2 - rectHeight / 2;
        r.right = width;
        r.bottom = height / 2 + rectHeight / 2;
        canvas.drawRoundRect(r, 10, 10, paint);
        //画小三角形指针
        Path path = new Path();
        path.moveTo(ruleWidth + rectPadding, height / 2 - scaleSpace);
        path.lineTo(ruleWidth + rectPadding - 8, height / 2);
        path.lineTo(ruleWidth + rectPadding, height / 2 + scaleSpace);
        path.close();
        canvas.drawPath(path, paint);
        //绘制文字
        paint.setColor(getResources().getColor(R.color.black));
        Rect rect1 = new Rect();
        paint.getTextBounds(descri, 0, descri.length(), rect1);
        int w1 = rect1.width();
        canvas.drawText(descri, width - (width - ruleWidth - 20) / 2 - w1 / 2, height / 2 - rectHeight / 2 - 10, paint);
        //绘制当前刻度值数字
        paint.setColor(getResources().getColor(R.color.white));
        float v = (float) (Math.round(currentValue * 10)) / 10;//保留一位小数
        String value = String.valueOf(v) + unit;
        Rect rect2 = new Rect();
        paint.getTextBounds(value, 0, value.length(), rect2);
        int w2 = rect2.width();
        int h2 = rect2.height();
        canvas.drawText(value, width - (width - ruleWidth - 20) / 2 - w2 / 2, height / 2 + h2 / 2, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = y;
                continueScroll = false;
                //初始化速度追踪
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
                int offsetY = (int) (lastY - y);
                minY -= offsetY;
                midY -= offsetY;
                calculateCurrentScale();
                invalidate();
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                confirmBorder();
                //当前滑动速度
                velocityTracker.computeCurrentVelocity(1000);
                velocity = velocityTracker.getYVelocity();
                float minVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
                if (Math.abs(velocity) > minVelocity) {
                    continueScroll = true;
                    continueScroll();
                } else {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                velocityTracker.recycle();
                velocityTracker = null;
                break;
        }
        return true;
    }

    //计算当前刻度
    private void calculateCurrentScale() {
        float offsetTotal = originMidY - midY;
        int offsetBig = (int) (offsetTotal / scaleSpaceUnit);//移动的大刻度数
        float offsetS = offsetTotal % scaleSpaceUnit;
        int offsetSmall = (new BigDecimal(offsetS / (scaleSpace + SCALE_WIDTH_SMALL)).setScale(0, BigDecimal.ROUND_HALF_UP)).intValue();//移动的小刻度数 四舍五入取整
        float offset = offsetBig + offsetSmall * 0.1f;
        if (originValue - offset > max) {
            currentValue = max;
        } else if (originValue - offset < min) {
            currentValue = min;
        } else {
            currentValue = originValue - offset;
        }
        mHandler.sendEmptyMessage(0);
    }

    //指针线超出范围时 重置回边界处
    private void confirmBorder() {
        if (midY < borderUp) {
            midY = borderUp;
            minY = borderDown + (borderUp - borderDown) / 2;
            postInvalidate();
        } else if (midY > borderDown) {
            midY = borderDown;
            minY = borderDown - (borderUp - borderDown) / 2;
            postInvalidate();
        }
    }

    //手指抬起后继续惯性滑动
    private void continueScroll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                float velocityAbs = 0;//速度绝对值
                if (velocity > 0 && continueScroll) {
                    velocity -= 50;
                    minY += velocity * velocity / a;
                    midY += velocity * velocity / a;
                    velocityAbs = velocity;
                } else if (velocity < 0 && continueScroll) {
                    velocity += 50;
                    minY -= velocity * velocity / a;
                    midY -= velocity * velocity / a;
                    velocityAbs = -velocity;
                }
                calculateCurrentScale();
                confirmBorder();
                postInvalidate();
                if (continueScroll && velocityAbs > 0) {
                    post(this);
                } else {
                    continueScroll = false;
                }
            }
        }).start();
    }
}
