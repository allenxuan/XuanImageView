package com.allenxuan.xuanyihuang.xuanimageview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.AttributeSet;
import android.util.Log;
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
    private int XuanImageViewWidth;
    private int XuanImageViewHeight;
    private int XuanImageViewCenterX;
    private int XuanImageViewCenterY;
    private int ImageCenterX;
    private int ImageCenterY;
    private boolean mImageLoadedFirstTime;
    private float mInitScale;
    private float mMaxScale;
    private float mDoubleTabScale;
    private Matrix mScaleMatrix;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;
    private float mLastScaleFocusX;
    private float mLastScaleFocusY;
    private boolean isAutoScale;
    private boolean isScaling;
    private float mSpringBackGradientScaleUpLevel;
    private float mSpringBackGradientScaleDownLevel;
    private float mDoubleTapGradientScaleUpLevel;
    private float mDoubleTapGradientScaleDownLevel;
    private float mTouchSlop;
    private boolean isDrag;
    private int mLastPointerCount;
    private float mLastX;
    private float mLastY;
    private float mAngle;
    private float mPreviousAngle;
    private RotationGestureDetector mRotateGestureDetector;
    private boolean canStillRotate;
    private boolean isAutoRotated;
    private int allowablePixelError;
    private float currentScaleLevel;
    private long autoRotationRunnableDelay;
    private long autoRotationRunnableTimes;
    private long doubleTabScaleRunnableDelay;

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
                currentScaleLevel = getCurrentScaleLevel();
                float x = e.getX();
                float y = e.getY();

                if(isAutoScale)
                    return true;


                if(currentScaleLevel < mDoubleTabScale){
                    postDelayed(new AutoScaleRunnable(mDoubleTabScale, x, y, mDoubleTapGradientScaleUpLevel, mDoubleTapGradientScaleDownLevel),doubleTabScaleRunnableDelay);
                    isAutoScale = true;
                }
                else{
                    postDelayed(new AutoScaleRunnable(mInitScale, x, y, mDoubleTapGradientScaleUpLevel, mDoubleTapGradientScaleDownLevel),doubleTabScaleRunnableDelay);
                    isAutoScale = true;
                }

                return true;
            }
        });

        isAutoScale = false;
        isScaling = false;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        isDrag = false;
        mLastPointerCount = 0;
        mRotateGestureDetector = new RotationGestureDetector(this);
        mAngle = 0;
        mPreviousAngle = 0;
        mImageLoadedFirstTime = false;
        allowablePixelError = 1;
        currentScaleLevel = 1;
        autoRotationRunnableDelay = 10;
        autoRotationRunnableTimes = 10;
//        autoRotationRunnableTimes = 500;  //for Debug
        doubleTabScaleRunnableDelay = 10;
        mSpringBackGradientScaleUpLevel = 1.01f;
        mSpringBackGradientScaleDownLevel = 0.99f;
        mDoubleTapGradientScaleUpLevel = 1.05f;
        mDoubleTapGradientScaleDownLevel = 0.95f;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /**
     * ImageView初次加载图片时,需要图片在控件内居中显示
     */
    @Override
    public void onGlobalLayout() {
        if(!mImageLoadedFirstTime){
            getViewTreeObserver().removeGlobalOnLayoutListener(this);

             //得到控件宽高
            XuanImageViewWidth = getWidth();
            XuanImageViewHeight = getHeight();

            //get the center point of XuanImageView
            XuanImageViewCenterX = XuanImageViewWidth / 2;
            XuanImageViewCenterY = XuanImageViewHeight / 2;

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
            float scale = Math.min(XuanImageViewWidth * 1.0f / imageWidth, XuanImageViewHeight * 1.0f /imageHeight);

            //设置初始的缩放比例;
            mInitScale = scale;
            mMaxScale = 4 * scale;
            mDoubleTabScale = 2* scale;

            //图片移至中心位置
            int deltaX = XuanImageViewWidth / 2 -imageWidth / 2;
            int deltaY = XuanImageViewHeight / 2 - imageHeight / 2;

            mScaleMatrix.postTranslate(deltaX, deltaY);
            mScaleMatrix.postScale(scale, scale, XuanImageViewWidth / 2, XuanImageViewHeight/2);
            setImageMatrix(mScaleMatrix);

            mImageLoadedFirstTime = true;

        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // for DoubleTap gesture
        mGestureDetector.onTouchEvent(motionEvent);

        // for Scale gesture
        mScaleGestureDetector.onTouchEvent(motionEvent);
        /**
         * 不能这么写
         * if(mScaleGestureDetector.onTouchEvent(motionEvent))
         *  return ture;
         */

        currentScaleLevel = getCurrentScaleLevel();

//        Log.d("isScaling before rotate",""+isScaling);
//        Log.d("isRotated before rotate", ""+isRotated);
//        //The image can only be rotated when currentScaleLevel is equal to mInitScale
//        if((!isScaling) && (isRotated || currentScaleLevel == mInitScale)) {
//            mRotateGestureDetector.onTouchEvent(motionEvent);
//            isRotated = mRotateGestureDetector.IsRotated();
//        }

        if(mRotateGestureDetector.IsRotated() || currentScaleLevel == mInitScale){
            mRotateGestureDetector.onTouchEvent(motionEvent);
        }

//        mRotateGestureDetector.onTouchEvent(motionEvent);

        //pointerCount won't be 0
        int pointerCount = motionEvent.getPointerCount();
        float pivotX = 0;
        float pivotY = 0;
        for(int i = 0; i < pointerCount; i++){
            pivotX += motionEvent.getX(i);
            pivotY += motionEvent.getY(i);
        }
        pivotX /= pointerCount;   //get integer result of the division
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
                if((rectF.width() - XuanImageViewWidth > allowablePixelError )|| (rectF.height() - XuanImageViewHeight > allowablePixelError))
                    if(getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if((rectF.width() - XuanImageViewWidth > allowablePixelError )|| (rectF.height() - XuanImageViewHeight > allowablePixelError))
                    if(getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(true);
                float deltaX = pivotX - mLastX;
                float deltaY = pivotY - mLastY;
                if(!isDrag)
                    isDrag = isMoveAction(deltaX, deltaY);
                if(isDrag){
                    if(getDrawable() != null){
                        if(!mRotateGestureDetector.IsRotated()) {
                            if (rectF.width() < XuanImageViewWidth)
                                deltaX = 0;
                            if (rectF.height() < XuanImageViewHeight)
                                deltaY = 0;
                        }

                        mScaleMatrix.postTranslate(deltaX, deltaY);

                        if(!mRotateGestureDetector.IsRotated())
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
                    if((currentScaleLevel < mInitScale) && !isAutoRotated){
                        postDelayed(new AutoScaleRunnable(mInitScale, mLastScaleFocusX, mLastScaleFocusY, mSpringBackGradientScaleUpLevel, mSpringBackGradientScaleDownLevel), 16);
                        isAutoScale = true;
                    }
                    else if((currentScaleLevel > mMaxScale) && !isAutoRotated){
                        postDelayed(new AutoScaleRunnable(mMaxScale, mLastScaleFocusX, mLastScaleFocusY, mSpringBackGradientScaleUpLevel, mSpringBackGradientScaleDownLevel), 16);
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
//        if(!isRotated)
//            isScaling = true;
//        else
//            isScaling = false;
//        Log.d("isScaling in onScale()",""+isScaling);

        /**
         * scaleFactor > 1.0f表示放大
         * scaleFactor < 1.0f表示缩小
         * currentScaleLevel表示当前图片所积累的缩放水平
         */
        float scaleFactor = scaleGestureDetector.getScaleFactor();
        currentScaleLevel = getCurrentScaleLevel();

        if((currentScaleLevel <= mMaxScale && scaleFactor > 1.0f) || (currentScaleLevel >= mInitScale && scaleFactor < 1.0f) || mRotateGestureDetector.IsRotated()){
            /**
             * 让图片放大缩小。
             * 经过这次放大或者缩小后,图片当前的缩放水平可能小于mInitScale或者大于mMaxScale。
             * 若图片当前的缩放水平小于mInitScale, 继续尝试缩小手势时,不符合if条件,则不会进行任何变换。
             * 若图片当前的缩放水平大于mMaxScale, 继续尝试放大手势时,不符合if条件,则不会进行任何变换。
             *
             */
            mScaleMatrix.postScale(scaleFactor, scaleFactor, scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());

            if(!mRotateGestureDetector.IsRotated())
                checkBorderAndCenterWhenScale();

            setImageMatrix(mScaleMatrix);
            mLastScaleFocusX = scaleGestureDetector.getFocusX();
            mLastScaleFocusY = scaleGestureDetector.getFocusY();

            Log.d("currentScaleLevel", ""+getCurrentScaleLevel());
        }

        return true;
    }

    private void checkBorderAndCenterWhenScale() {
        RectF rectF = getMatrixRectF();

        float deltaX = 0;
        float deltaY = 0;

        /**
         * 若图片宽度或高度大于控件的宽度或高度,则要避免白边现象
         */
        if(rectF.width() >= XuanImageViewWidth){
            if(rectF.left > 0)
                deltaX = - rectF.left;
            if(rectF.right < XuanImageViewWidth)
                deltaX = XuanImageViewWidth - rectF.right;

        }
        if(rectF.height() >= XuanImageViewHeight){
            if(rectF.top > 0)
                deltaY = - rectF.top;
            if(rectF.bottom < XuanImageViewHeight)
                deltaY = XuanImageViewHeight - rectF.bottom;
        }

        /**
         * 若图片的宽度或高度小于控件的宽度或高度,则令图片在宽度维度或高度维度上是居中的
         */

        if(rectF.width() < XuanImageViewWidth){
            deltaX = XuanImageViewWidth / 2.0f - rectF.left - rectF.width() / 2.0f;
        }
        if(rectF.height() < XuanImageViewHeight){
            deltaY = XuanImageViewHeight / 2.0f - rectF.top - rectF.height() / 2.0f;

        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    private void checkBorderAndCenterWhenTranslate() {
        //实际上不用检查Center, 因为若图片宽度或高度小于控件宽度或高度时, 图片在宽度维度或者高度维度上无法移动
        //这是在调用checkBorderAndCenterWhenTranslate()之前就已经处理好的。
        RectF rectF = getMatrixRectF();

        float deltaX = 0;
        float deltaY = 0;

        /**
         * 若图片宽度或高度大于控件的宽度或高度,则要避免白边现象
         */
        if(rectF.width() >= XuanImageViewWidth){
            if(rectF.left > 0)
                deltaX = - rectF.left;
            if(rectF.right < XuanImageViewWidth)
                deltaX = XuanImageViewWidth - rectF.right;

        }
        if(rectF.height() >= XuanImageViewHeight){
            if(rectF.top > 0)
                deltaY = - rectF.top;
            if(rectF.bottom < XuanImageViewHeight)
                deltaY = XuanImageViewHeight - rectF.bottom;
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }


    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        Log.d("onScaleBegin","-->");
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        Log.d("onScaleEnd","-->");
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

    private boolean calculateImageCenterCoordinates(){
        RectF rectF = getMatrixRectF();
        ImageCenterX = (int)((rectF.left + rectF.right) / 2);
        ImageCenterY = (int)((rectF.top + rectF.bottom) / 2);

        return true;
    }


    @Override
    public boolean OnRotate(RotationGestureDetector rotationGestureDetector) {
        mAngle = rotationGestureDetector.getAngle();
        mPreviousAngle = rotationGestureDetector.getPreviousAngle();
        mScaleMatrix.postRotate(mAngle - mPreviousAngle, rotationGestureDetector.getPivotX(), rotationGestureDetector.getPivotY());
        setImageMatrix(mScaleMatrix);

        return true;
    }

    @Override
    public boolean StopRotate(RotationGestureDetector rotationGestureDetector) {
        RectF rectF = getMatrixRectF();
        Log.d("Rect left","" + rectF.left);
        Log.d("Rect top","" + rectF.top);
        Log.d("Rect right","" + rectF.right);
        Log.d("Rect bottom","" + rectF.bottom);
        mAngle = rotationGestureDetector.getAngle();//mAngle is within [-180 degress, 180 degress]
        float autoScaleAngle;
        if(mAngle >= 60)
            autoScaleAngle = 360 - mAngle;
        else if(mAngle <= -60)
            autoScaleAngle = -360 - mAngle;
        else
            autoScaleAngle = -mAngle;

        postDelayed(new AutoRotateRunnable(autoScaleAngle, getCurrentScaleLevel() / (float)Math.cos(Math.toRadians(mAngle)), autoRotationRunnableTimes), autoRotationRunnableDelay);
        isAutoRotated = true;

        rotationGestureDetector.setAngle(0.0f);
        rotationGestureDetector.setmPreviousAngle(0.0f);

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
            currentScaleLevel = getCurrentScaleLevel();
            if(currentScaleLevel < targetScale)
                scaleFacotr = GradientScaleUp;
            else if(currentScaleLevel > targetScale)
                scaleFacotr = GradientScaleDown;
            else if(currentScaleLevel == targetScale)
                scaleFacotr = 1.0f;
        }

        @Override
        public void run() {
            mScaleMatrix.postScale(scaleFacotr, scaleFacotr, FocusX, FocusY);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);

            currentScaleLevel = getCurrentScaleLevel();
            if((scaleFacotr < 1.0f && currentScaleLevel > targetScale)
                    ||(scaleFacotr > 1.0f && currentScaleLevel < targetScale)){
                postDelayed(this,16);
            }
            else{
                scaleFacotr = targetScale / currentScaleLevel;

                mScaleMatrix.postScale(scaleFacotr, scaleFacotr, FocusX, FocusY);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);

                isAutoScale = false;
            }

        }
    }


    private class AutoRotateRunnable implements Runnable {
        float targetRotateAngle;
        long TotalRotateTimes;
        float AnglePerTime;
        float AccumulativeRotateTimes;
        float AccumulativeRotateAngles;
        float initScaleLevel;
        double ScalePerTime;
        boolean calulateCenterCoordinateDiffFlag;


        public AutoRotateRunnable(float targetRotateAngle, float initScaleLevel, long TotalRotateTimes) {
            this.targetRotateAngle = targetRotateAngle;
            this.TotalRotateTimes = TotalRotateTimes;
            AnglePerTime = targetRotateAngle / this.TotalRotateTimes;
            AccumulativeRotateTimes = 0;
            AccumulativeRotateAngles = 0.0f;
            this.initScaleLevel = initScaleLevel;
            ScalePerTime = Math.pow(mInitScale/initScaleLevel, 1.0/TotalRotateTimes);
            calulateCenterCoordinateDiffFlag = false;
        }

        @Override
        public void run() {
            calculateImageCenterCoordinates();

            mScaleMatrix.postRotate(AnglePerTime, ImageCenterX, ImageCenterY);
            mScaleMatrix.postScale((float)ScalePerTime, (float)ScalePerTime, ImageCenterX, ImageCenterY);
            mScaleMatrix.postTranslate((XuanImageViewCenterX - ImageCenterX) / (TotalRotateTimes - AccumulativeRotateTimes), (XuanImageViewCenterY - ImageCenterY) / (TotalRotateTimes - AccumulativeRotateTimes));
            setImageMatrix(mScaleMatrix);
            AccumulativeRotateTimes++;
            AccumulativeRotateAngles += AnglePerTime;


            if(AccumulativeRotateTimes < TotalRotateTimes) {
                postDelayed(this, autoRotationRunnableDelay);
            }
            else{
                calculateImageCenterCoordinates();
                mScaleMatrix.postRotate(targetRotateAngle - AccumulativeRotateAngles, ImageCenterX, ImageCenterY);
                mScaleMatrix.postScale(mInitScale / getCurrentScaleLevel(), mInitScale / getCurrentScaleLevel(), ImageCenterX, ImageCenterY);
                mScaleMatrix.postTranslate(XuanImageViewCenterX - ImageCenterX, XuanImageViewCenterY - ImageCenterY);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                isAutoRotated = false;
            }


        }
    }

    private  boolean isMoveAction(float dx, float dy){
        return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
    }

    public void setAutoRotationRunnableDelay(long delay){
        autoRotationRunnableDelay = delay;
    }

    public void setAutoRotationRunnableTimes(long times){
        autoRotationRunnableTimes = times;
    }

    public void setDoubleTapScaleRunnableDelay(long delay){
        doubleTabScaleRunnableDelay = delay;
    }

    public void setSpringBackGradientScaleUpLevel(float springBackGradientScaleUpLevel){
        mSpringBackGradientScaleUpLevel = springBackGradientScaleUpLevel;
    }

    public void setmSpringBackGradientScaleDownLevel(float springBackGradientScaleDownLevel){
        mSpringBackGradientScaleDownLevel = springBackGradientScaleDownLevel;
    }

    public void setmDoubleTapGradientScaleUpLevel(float doubleTapGradientScaleUpLevel){
        mDoubleTapGradientScaleUpLevel = doubleTapGradientScaleUpLevel;
    }

    public void setDoubleTabGradientScaleDownLevel(float doubleTapGradientScaleDownLevel){
        mDoubleTapGradientScaleDownLevel = doubleTapGradientScaleDownLevel;
    }
}
