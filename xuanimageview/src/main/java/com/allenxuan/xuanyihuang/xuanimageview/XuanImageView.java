package com.allenxuan.xuanyihuang.xuanimageview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.allenxuan.xuanyihuang.xuanimageview.GestureDetectors.RotationGestureDetector;

/**
 * Created by xuanyihuang on 8/30/16.
 */

public class XuanImageView extends ImageView
        implements ViewTreeObserver.OnGlobalLayoutListener,View.OnTouchListener,ScaleGestureDetector.OnScaleGestureListener, RotationGestureDetector.OnRotationGestureListener{
    private boolean mImageLoadedFirstTime = false;
    private float mInitScale;
    private float mMaxScale;
    private float mDoubleTabScale;
    private Matrix mScaleMatrix;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;
    private float mLastScaleFocusX;
    private float mLastScaleFocusY;
    private boolean isAutoScale = false;
    private float mSpringBackGradientScaleUp = 1.01f;
    private float mSpringBackGradientScaleDown = 0.99f;
    private float mDoubleTapGradientScaleUp = 1.07f;
    private float mDoubleTapGradientScaleDown = 0.93f;
    private float mTouchSlope;
    private boolean isDrag;
    private int mLastPointerCount;
    private float mLastX;
    private float mLastY;
    private float mAngle;
    private float mPreviousAngle;
    private RotationGestureDetector mRotateGestureDetector;
    private boolean canStillRotate;
    private boolean isAutoRotate;

    public XuanImageView(Context context) {
        this(context, null);
    }

    public XuanImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XuanImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScaleMatrix= new Matrix();
        setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        setOnTouchListener(this);
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float currentScaleLevel = getCurrentScaleLevel();
                float x = e.getX();
                float y = e.getY();

                if(isAutoScale)
                    return true;


                if(currentScaleLevel < mDoubleTabScale){
                    postDelayed(new AutoScaleRunnable(mDoubleTabScale, x, y, mDoubleTapGradientScaleUp, mDoubleTapGradientScaleDown),16);
                    isAutoScale = true;
                }
                else{
                    postDelayed(new AutoScaleRunnable(mInitScale, x, y, mDoubleTapGradientScaleUp, mDoubleTapGradientScaleDown),16);
                    isAutoScale = true;
                }

                return true;
            }
        });
        mTouchSlope = ViewConfiguration.get(context).getScaledTouchSlop();
        isDrag = false;
        mLastPointerCount = 0;
        mRotateGestureDetector = new RotationGestureDetector(this);
        mAngle = 0;
        mPreviousAngle = 0;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    /**
     * ImageView初次加载图片时,需要图片在控件内居中显示
     */
    @Override
    public void onGlobalLayout() {
        if(!mImageLoadedFirstTime){
             //得到空间宽高
            int width = getWidth();
            int height = getHeight();

            //得到图片及其宽高
            Drawable imageDrawable = getDrawable();
            if(imageDrawable == null)
                return;
            int imageWidth = imageDrawable.getIntrinsicWidth();
            int imageHeight = imageDrawable.getIntrinsicHeight();

            //可优化
//            float scale = 1.0f;
//            if((imageWidth < width) && (imageHeight < height)){
//                scale = Math.min(width * 1.0f / imageWidth, height * 1.0f /imageHeight);
//            }
//            if((imageWidth > width) && (imageHeight > height)){
//                scale = Math.min(width * 1.0f / imageWidth, height * 1.0f /imageHeight);
//            }
//            if((imageWidth < width) && (imageHeight > height)){
//                scale = height * 1.0f /imageHeight;
//            }
//            if((imageWidth > width) && (imageHeight < height)){
//                scale = width * 1.0f / imageWidth;
//            }

            //优化为
            float scale = Math.min(width * 1.0f / imageWidth, height * 1.0f /imageHeight);

            //设置初始的缩放比例;
            mInitScale = scale;
            mMaxScale = 4 * scale;
            mDoubleTabScale = 2* scale;

            //图片移至中心位置
            int deltaX = width / 2 -imageWidth / 2;
            int deltaY = height / 2 - imageHeight / 2;

            mScaleMatrix.postTranslate(deltaX, deltaY);
            mScaleMatrix.postScale(scale, scale, width / 2, height /2);
            setImageMatrix(mScaleMatrix);

            mImageLoadedFirstTime = true;

        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(mGestureDetector.onTouchEvent(motionEvent))
            return  true;


        mScaleGestureDetector.onTouchEvent(motionEvent);

        /**
         * 不能这么写
         * if(mScaleGestureDetector.onTouchEvent(motionEvent))
         *  return ture;
         */
        mRotateGestureDetector.onTouchEvent(motionEvent);


        //pointerCount不可能为0
        int pointerCount = motionEvent.getPointerCount();
        float pivotX = 0;
        float pivotY = 0;
        for(int i = 0; i < pointerCount; i++){
            pivotX += motionEvent.getX(i);
            pivotY += motionEvent.getY(i);
        }
        pivotX /= pointerCount;
        pivotY /= pointerCount;


        //一般拖动图片的时候pointCount == mLastCount,因此,拖动图片时,此处不会保存上一次的状态
        if(pointerCount != mLastPointerCount){
            isDrag = false;
            mLastX = pivotX;
            mLastY = pivotY;
            mLastPointerCount = pointerCount;
        }

        RectF rectF = getMatrixRectF();
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                if(rectF.width() > getWidth() || rectF.height() > getHeight())
                    if(getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if(rectF.width() > getWidth() || rectF.height() > getHeight())
                    if(getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(true);
                float deltaX = pivotX - mLastX;
                float deltaY = pivotY - mLastY;
                int ViewWidth = getWidth();
                int ViewHeight = getHeight();
                if(!isDrag)
                    isDrag = isMoveAction(deltaX, deltaY);
                if(isDrag){
                    if(getDrawable() != null){
                        if(rectF.width() < ViewWidth)
                            deltaX = 0;
                        if(rectF.height() < ViewHeight)
                            deltaY = 0;

                        mScaleMatrix.postTranslate(deltaX, deltaY);
                        checkBorderAndCenterWhenTranslate();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastX = pivotX;
                mLastY = pivotY;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //多点触控时,有一个手指松开
                if(pointerCount -1 < 2){
                    //回弹至mInitScale或mMaxScale
                    if(getCurrentScaleLevel() < mInitScale){
                        postDelayed(new AutoScaleRunnable(mInitScale, mLastScaleFocusX, mLastScaleFocusY, mSpringBackGradientScaleUp, mSpringBackGradientScaleDown), 16);
                        isAutoScale = true;
                    }
                    if(getCurrentScaleLevel() > mMaxScale){
                        postDelayed(new AutoScaleRunnable(mMaxScale, mLastScaleFocusX, mLastScaleFocusY, mSpringBackGradientScaleUp, mSpringBackGradientScaleDown), 16);
                        isAutoScale = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mLastPointerCount = 0;
                break;
            case MotionEvent.ACTION_CANCEL:
                mLastPointerCount = 0;
        }



        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        /**
         * scaleFactor > 1.0f表示放大
         * scaleFactor < 1.0f表示缩小
         * currentScaleLevel表示当前图片所积累的缩放水平
         */
        float scaleFactor = scaleGestureDetector.getScaleFactor();
        float currentScaleLevel = getCurrentScaleLevel();

        if((currentScaleLevel <= mMaxScale && scaleFactor > 1.0f) || (currentScaleLevel >= mInitScale && scaleFactor < 1.0f)){
            /**
             * 让图片放大缩小。
             * 经过这次放大或者缩小后,图片当前的缩放水平可能小于mInitScale或者大于mMaxScale。
             * 若图片当前的缩放水平小于mInitScale, 继续尝试缩小手势时,不符合if条件,则不会进行任何变换。
             * 若图片当前的缩放水平大于mMaxScale, 继续尝试放大手势时,不符合if条件,则不会进行任何变换。
             *
             */
            mScaleMatrix.postScale(scaleFactor, scaleFactor, scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
            checkBorderAndCenterWhenScale();

            setImageMatrix(mScaleMatrix);
            mLastScaleFocusX = scaleGestureDetector.getFocusX();
            mLastScaleFocusY = scaleGestureDetector.getFocusY();
        }

        return true;
    }

    private void checkBorderAndCenterWhenScale() {
        int width = getWidth();
        int height = getHeight();
        RectF rectF = getMatrixRectF();

        float deltaX = 0;
        float deltaY = 0;

        /**
         * 若图片宽度或高度大于控件的宽度或高度,则要避免白边现象
         */
        if(rectF.width() >= width){
            if(rectF.left > 0)
                deltaX = - rectF.left;
            if(rectF.right < width)
                deltaX = width - rectF.right;

        }
        if(rectF.height() >= height){
            if(rectF.top > 0)
                deltaY = - rectF.top;
            if(rectF.bottom < height)
                deltaY = height - rectF.bottom;
        }

        /**
         * 若图片的宽度或高度小于控件的宽度或高度,则令图片在宽度维度或高度维度上是居中的
         */

        if(rectF.width() < width){
            deltaX = width / 2.0f - rectF.left - rectF.width() / 2.0f;
        }
        if(rectF.height() < height){
            deltaY = height / 2.0f - rectF.top - rectF.height() / 2.0f;

        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    private void checkBorderAndCenterWhenTranslate() {
        //实际上不用检查Center, 因为若图片宽度或高度小于控件宽度或高度时, 图片在宽度维度或者高度维度上无法移动
        //这是在调用checkBorderAndCenterWhenTranslate()之前就已经处理好的。
        int width = getWidth();
        int height = getHeight();
        RectF rectF = getMatrixRectF();

        float deltaX = 0;
        float deltaY = 0;

        /**
         * 若图片宽度或高度大于控件的宽度或高度,则要避免白边现象
         */
        if(rectF.width() >= width){
            if(rectF.left > 0)
                deltaX = - rectF.left;
            if(rectF.right < width)
                deltaX = width - rectF.right;

        }
        if(rectF.height() >= height){
            if(rectF.top > 0)
                deltaY = - rectF.top;
            if(rectF.bottom < height)
                deltaY = height - rectF.bottom;
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }


    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

    }


    /**
     * 获取当前图像的缩放水平
     * @return
     */
    private float getCurrentScaleLevel(){
        float matrixArray[] = new float[9];
        mScaleMatrix.getValues(matrixArray);
        return matrixArray[Matrix.MSCALE_X];
    }

    private RectF getMatrixRectF(){
        Matrix matrix = mScaleMatrix;
        RectF rectF = new RectF();
        Drawable image = getDrawable();
        if(image != null){
            rectF.set(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }

        return rectF;
    }

    @Override
    public boolean OnRotate(RotationGestureDetector rotationDetector) {
        mAngle = rotationDetector.getAngle();
        mPreviousAngle = rotationDetector.getPreviousAngle();
        mScaleMatrix.postRotate(mAngle - mPreviousAngle, rotationDetector.getPivotX(), rotationDetector.getPivotY());
        setImageMatrix(mScaleMatrix);

        return true;
    }

    @Override
    public boolean StopRotate(RotationGestureDetector rotationGestureDetector) {
        mAngle = rotationGestureDetector.getAngle();
        float ReverseReverseAngle = 0;
        float ReverseAngle = 0;
        if(mAngle < -180.0f)
            ReverseReverseAngle = mAngle + 360.0f;
        else if(mAngle > 180.0f)
            ReverseReverseAngle = mAngle - 360.0f;
        else
            ReverseReverseAngle = mAngle;
        ReverseAngle = -ReverseReverseAngle;
        postDelayed(new AutoRotateRunnable(ReverseAngle, rotationGestureDetector.getPivotX(), rotationGestureDetector.getPivotY(), 40), 16);

        return true;
    }

    private class AutoScaleRunnable implements Runnable {
        float targetScale;
        float FocusX;
        float FocusY;
        float scaleFacotr;

        public AutoScaleRunnable(float targetScale, float FocusX, float FocusY, float GradientScaleUp, float GradientScaleDown) {
            this.targetScale = targetScale;
            this.FocusX = FocusX;
            this.FocusY = FocusY;
            if(getCurrentScaleLevel() < targetScale)
                scaleFacotr = GradientScaleUp;
            else if(getCurrentScaleLevel() > targetScale)
                scaleFacotr = GradientScaleDown;
            else if(getCurrentScaleLevel() == targetScale)
                scaleFacotr = 1.0f;
        }

        @Override
        public void run() {
            mScaleMatrix.postScale(scaleFacotr, scaleFacotr, FocusX, FocusY);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);

            float currenScaleLevel = getCurrentScaleLevel();
            if((scaleFacotr < 1.0f && currenScaleLevel > targetScale)
                    ||(scaleFacotr > 1.0f && currenScaleLevel < targetScale)){
                postDelayed(this,16);
            }
            else{
                scaleFacotr = targetScale / currenScaleLevel;

                mScaleMatrix.postScale(scaleFacotr, scaleFacotr, FocusX, FocusY);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);

                isAutoScale = false;
            }

        }
    }


    private class AutoRotateRunnable implements Runnable {
        float targetRotateAngle;
        float PivotX;
        float PivotY;
        int TotalRotateTimes;
        float AnglePerTime;
        float AccumulativeRotateTimes;
        float AccumulativeRotateAngles;

        public AutoRotateRunnable(float targetRotateAngle, float PivotX, float PivotY, int TotalRotateTimes) {
            this.targetRotateAngle = targetRotateAngle;
            this.PivotX = PivotX;
            this.PivotY = PivotY;
            this.TotalRotateTimes = TotalRotateTimes;
            AnglePerTime = targetRotateAngle / this.TotalRotateTimes;
            AccumulativeRotateTimes = 0;
            AccumulativeRotateAngles = 0.0f;

        }

        @Override
        public void run() {
            mScaleMatrix.postRotate(AnglePerTime, PivotX, PivotY);
            setImageMatrix(mScaleMatrix);
            AccumulativeRotateTimes++;
            AccumulativeRotateAngles += AnglePerTime;


            if(AccumulativeRotateTimes < TotalRotateTimes)
                postDelayed(this, 16);
            else{
                mScaleMatrix.postRotate(targetRotateAngle - AccumulativeRotateAngles, PivotX, PivotY);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                isAutoRotate = false;
            }


        }
    }

    private  boolean isMoveAction(float dx, float dy){
        return Math.sqrt(dx * dx + dy * dy) > mTouchSlope;
    }
}
