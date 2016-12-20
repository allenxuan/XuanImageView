package com.allenxuan.xuanyihuang.xuanimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.allenxuan.xuanyihuang.xuanimageview.GestureDetectors.RotationGestureDetector;

/**
 * Created by xuanyihuang on 8/30/16.
 */

public class XuanImageView extends ImageView
        implements ViewTreeObserver.OnGlobalLayoutListener,
                    View.OnTouchListener,
                    ScaleGestureDetector.OnScaleGestureListener,
                    RotationGestureDetector.OnRotationGestureListener{
    private int XuanImageViewWidth;
    private int XuanImageViewHeight;
    private int XuanImageViewCenterX;
    private int XuanImageViewCenterY;
    private int ImageCenterX;
    private int ImageCenterY;
    private boolean mImageLoadedFirstTime;
    private float mInitScale;
    private float mMaxScale;
    private boolean mRotationToggle;
    private float mMaxScaleMultiple;
    private float mDoubleTabScaleMultiple;
    private float mDoubleTabScale;
    private Matrix mScaleMatrix;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;
    private float mLastScaleFocusX;
    private float mLastScaleFocusY;
    private boolean isAutoScale;
    private float mSpringBackGradientScaleUpLevel;
    private float mSpringBackGradientScaleDownLevel;
    private float mDoubleTapGradientScaleUpLevel;
    private float mDoubleTapGradientScaleDownLevel;
    private int mLastPointerCount;
    private float mLastX;
    private float mLastY;
    private float mAngle;
    private float mPreviousAngle;
    private RotationGestureDetector mRotateGestureDetector;
    private boolean isAutoRotated;
    private double allowableFloatError;
    private float currentScaleLevel;
    private float autoRotationTrigger;
    private int autoRotationRunnableDelay;
    private int autoRotationRunnableTimes;
    private int doubleTabScaleRunnableDelay;
    private int springBackRunnableDelay;

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

        setOnTouchListener(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.xuanimageview);
        mRotationToggle = a.getBoolean(R.styleable.xuanimageview_RotationToggle, true);
        mMaxScaleMultiple = a.getFloat(R.styleable.xuanimageview_MaxScaleMultiple, 4);
        mDoubleTabScaleMultiple = a.getFloat(R.styleable.xuanimageview_DoubleTabScaleMultiple, 2);
        mSpringBackGradientScaleUpLevel = a.getFloat(R.styleable.xuanimageview_SpringBackGradientScaleUpLevel, 1.01f);
        mSpringBackGradientScaleDownLevel = a.getFloat(R.styleable.xuanimageview_SpringBackGradientScaleDownLevel, 0.99f);
        mDoubleTapGradientScaleUpLevel = a.getFloat(R.styleable.xuanimageview_DoubleTapGradientScaleUpLevel, 1.05f);
        mDoubleTapGradientScaleDownLevel = a.getFloat(R.styleable.xuanimageview_DoubleTapGradientScaleDownLevel, 0.95f);
        autoRotationTrigger = a.getFloat(R.styleable.xuanimageview_AutoRotationTrigger, 60);
        springBackRunnableDelay = a.getInteger(R.styleable.xuanimageview_SpringBackRunnableDelay, 10);
        doubleTabScaleRunnableDelay = a.getInteger(R.styleable.xuanimageview_DoubleTapScaleRunnableDelay, 10);
        autoRotationRunnableDelay = a.getInteger(R.styleable.xuanimageview_AutoRotationRunnableDelay, 5);
        autoRotationRunnableTimes = a.getInteger(R.styleable.xuanimageview_AutoRotationRunnableTimes, 10);
        a.recycle();


        isAutoScale = false;
        mLastPointerCount = 0;
        mAngle = 0;
        mPreviousAngle = 0;
        mImageLoadedFirstTime = false;
        allowableFloatError = 1E-6;
        currentScaleLevel = 1;


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
     * Image should be centered after XuanImageView is initialized.
     */
    @Override
    public void onGlobalLayout() {
        if(!mImageLoadedFirstTime){
            getViewTreeObserver().removeGlobalOnLayoutListener(this);

            // get width and height of XuanImageView
            XuanImageViewWidth = getWidth();
            XuanImageViewHeight = getHeight();

            //get the center point of XuanImageView
            XuanImageViewCenterX = XuanImageViewWidth / 2;
            XuanImageViewCenterY = XuanImageViewHeight / 2;

            // instantiate mRotationGestureDetector after dimension of XuanImageView is got.
            mRotateGestureDetector = new RotationGestureDetector(this, XuanImageViewWidth);

            //get width and height of the image
            Drawable imageDrawable = getDrawable();
            if(imageDrawable == null)
                return;
            int imageWidth = imageDrawable.getIntrinsicWidth();
            int imageHeight = imageDrawable.getIntrinsicHeight();

            //image is scaled to fit the size of XuanImageView at the very beginning.
            float scale = Math.min(XuanImageViewWidth * 1.0f / imageWidth, XuanImageViewHeight * 1.0f /imageHeight);

            mInitScale = scale;
            mMaxScale = mMaxScaleMultiple * scale;
            mDoubleTabScale = mDoubleTabScaleMultiple * scale;

            //center of image overlaps with that of XuanImageView
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


        currentScaleLevel = getCurrentScaleLevel();
        if(mRotationToggle)
            if(mRotateGestureDetector.IsRotated() || Math.abs(currentScaleLevel - mInitScale) < allowableFloatError){
                // for Rotation gesture
                mRotateGestureDetector.onTouchEvent(motionEvent);
            }

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

        // when image is being dragged, generally, pointCount == mLastCount holds, so old state is not saved.
        if(pointerCount != mLastPointerCount){
            mLastX = pivotX;
            mLastY = pivotY;
            mLastPointerCount = pointerCount;
        }

        RectF rectF = getMatrixRectF();

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                currentScaleLevel = getCurrentScaleLevel();
                if(Math.abs(currentScaleLevel - mInitScale) >= allowableFloatError)
                    if(getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                currentScaleLevel = getCurrentScaleLevel();
                if(Math.abs(currentScaleLevel - mInitScale) >= allowableFloatError)
                    if(getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(true);
                float deltaX = pivotX - mLastX;
                float deltaY = pivotY - mLastY;

                if(getDrawable() != null){
                    if(!mRotateGestureDetector.IsRotated()) {
                        if (rectF.width() <= XuanImageViewWidth)
                            deltaX = 0;
                        if (rectF.height() <= XuanImageViewHeight)
                            deltaY = 0;
                    }
                    mScaleMatrix.postTranslate(deltaX, deltaY);

                    if(!mRotateGestureDetector.IsRotated())
                        checkBorderAndCenterWhenTranslate();
                    setImageMatrix(mScaleMatrix);
                }
                mLastX = pivotX;
                mLastY = pivotY;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //one finger loosening (multi-touch)
                if(pointerCount -1 < 2){

                    //spring back to mInitScale or mMaxScale
                    if((currentScaleLevel < mInitScale) && !isAutoRotated){
                        postDelayed(new AutoScaleRunnable(mInitScale, mLastScaleFocusX, mLastScaleFocusY, mSpringBackGradientScaleUpLevel, mSpringBackGradientScaleDownLevel), springBackRunnableDelay);
                        isAutoScale = true;
                    }
                    else if((currentScaleLevel > mMaxScale) && !isAutoRotated){
                        postDelayed(new AutoScaleRunnable(mMaxScale, mLastScaleFocusX, mLastScaleFocusY, mSpringBackGradientScaleUpLevel, mSpringBackGradientScaleDownLevel), springBackRunnableDelay);
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
        float scaleFactor = scaleGestureDetector.getScaleFactor();
        currentScaleLevel = getCurrentScaleLevel();

        if((currentScaleLevel <= mMaxScale && scaleFactor > 1.0f) || (currentScaleLevel >= mInitScale && scaleFactor < 1.0f) || mRotateGestureDetector.IsRotated()){
            if(!mRotateGestureDetector.IsRotated()) {
                mScaleMatrix.postScale(scaleFactor, scaleFactor, scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
                checkBorderAndCenterWhenScale();
            }else{
                mScaleMatrix.postScale(scaleFactor, scaleFactor, mRotateGestureDetector.getPivotX(), mRotateGestureDetector.getPivotY());
            }

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
         * If width or height of image is bigger than that of XuanImageView,
         * should prevent image's edge being far away from XuanImageView's edge.
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
         * If width or height of image is smaller than that of XuanImageView,
         * make sure the image, in width dimension or height dimension, is centered.
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
        /**
         *  No need to check Center here because it has been handled before checkBorderAndCenterWhenTranslate() is invoked.
         *  See "case MotionEvent.ACTION_MOVE: " in  onTouch(View view, MotionEvent motionEvent).
         */
        RectF rectF = getMatrixRectF();

        float deltaX = 0;
        float deltaY = 0;

        /**
         * If width or height of image is bigger than that of XuanImageView,
         * should prevent image's edge being far away from XuanImageView's edge.
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
        Log.d("onScaleBegin-->","");
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        Log.d("onScaleEnd-->","");
    }

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
        mAngle = rotationGestureDetector.getAngle();//mAngle's range: [-180 degress, 180 degress]
        float autoScaleAngle;
        if(mAngle >= autoRotationTrigger)
            autoScaleAngle = 360 - mAngle;
        else if(mAngle <= - autoRotationTrigger)
            autoScaleAngle = -360 - mAngle;
        else
            autoScaleAngle = -mAngle;

        postDelayed(new AutoRotateRunnable(autoScaleAngle, getCurrentScaleLevel() / (float)Math.cos(Math.toRadians(mAngle)), autoRotationRunnableTimes), autoRotationRunnableDelay);
        isAutoRotated = true;

        rotationGestureDetector.setAngle(0.0f);
        rotationGestureDetector.setPreviousAngle(0.0f);

        return true;
    }

    private class AutoScaleRunnable implements Runnable {
        float targetScale;
        float FocusX;
        float FocusY;
        float scaleFactor;

        AutoScaleRunnable(float targetScale, float FocusX, float FocusY, float GradientScaleUp, float GradientScaleDown) {
            this.targetScale = targetScale;
            this.FocusX = FocusX;
            this.FocusY = FocusY;
            currentScaleLevel = getCurrentScaleLevel();
            if(currentScaleLevel < targetScale)
                scaleFactor = GradientScaleUp;
            else if(currentScaleLevel > targetScale)
                scaleFactor = GradientScaleDown;
            else if(currentScaleLevel == targetScale)
                scaleFactor = 1.0f;
        }

        @Override
        public void run() {
            mScaleMatrix.postScale(scaleFactor, scaleFactor, FocusX, FocusY);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);

            currentScaleLevel = getCurrentScaleLevel();
            if((scaleFactor < 1.0f && currentScaleLevel > targetScale)
                    ||(scaleFactor > 1.0f && currentScaleLevel < targetScale)){
                postDelayed(this, doubleTabScaleRunnableDelay);
            }
            else{
                scaleFactor = targetScale/ currentScaleLevel;

                mScaleMatrix.postScale(scaleFactor, scaleFactor, FocusX, FocusY);
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

        AutoRotateRunnable(float targetRotateAngle, float initScaleLevel, long TotalRotateTimes) {
            this.targetRotateAngle = targetRotateAngle;
            this.TotalRotateTimes = TotalRotateTimes;
            AnglePerTime = targetRotateAngle / this.TotalRotateTimes;
            AccumulativeRotateTimes = 0;
            AccumulativeRotateAngles = 0.0f;
            this.initScaleLevel = initScaleLevel;
            ScalePerTime = Math.pow(mInitScale/initScaleLevel, 1.0/TotalRotateTimes);
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

    /**
     * Set a boolean value to determine whether rotation function is turned on.
     * @param toggle determine whether rotation function is turned on
     */
    public void setRotationToggle(boolean toggle){
        mRotationToggle = toggle;
    }

    /**
     * @return current RotationToggle
     */
    public boolean getRotationToggle(){
        return mRotationToggle;
    }

    /**
     * An image is scaled to an InitScale to fit the size of XuanImageView at the very beginning.
     * MaxScale = MaxScaleMultiple * InitScale holds.
     * @param maxScaleMultiple
     */
    public void setMaxScaleMultiple(float maxScaleMultiple){
        mMaxScaleMultiple = maxScaleMultiple;
    }

    /**
     * @return current MaxScaleMultiple
     */
    public float getMaxScaleMultiple(){
        return mMaxScaleMultiple;
    }

    /**
     * When image's current scale level is smaller than DoubleTabScale, the image will scale up to DoubleTapScale if an double-tap gesture is detected.
     * DoubleTapScale = DoubleTabScaleMultiple * InitScale holds.
     * @param doubleTabScaleMultiple
     */
    public void setDoubleTabScaleMultiple(float doubleTabScaleMultiple){
        mDoubleTabScaleMultiple = doubleTabScaleMultiple;
    }

    /**
     *
     * @return current DoubleTabScaleMultiple
     */
    public float getDoubleTabScaleMultiple(){
        return mDoubleTabScaleMultiple;
    }

    /**
     * If current scale level is smaller than InitScale and image is not in rotation state,
     * the image will scale up to InitScale with SpringBackGradientScaleUpLevel step by step.
     * Default springBackGradientScaleUpLevel is  1.01f.
     * @param springBackGradientScaleUpLevel
     */
    public void setSpringBackGradientScaleUpLevel(float springBackGradientScaleUpLevel){
        mSpringBackGradientScaleUpLevel = springBackGradientScaleUpLevel;
    }

    /**
     *
     * @return current SpringBackGradientScaleUpLevel
     */
    public float getSpringBackGradientScaleUpLevel(){
        return mSpringBackGradientScaleUpLevel;
    }

    /**
     * If current scale level is bigger than MaxScale and image is not in rotation state,
     * the image will scale down to MaxScale with SpringBackGradientScaleDownLevel step by step.
     * Default springBackGradientScaleDownLevel is 0.99f.
     * @param springBackGradientScaleDownLevel
     */
    public void setSpringBackGradientScaleDownLevel(float springBackGradientScaleDownLevel){
        mSpringBackGradientScaleDownLevel = springBackGradientScaleDownLevel;
    }

    /**
     *
     * @return current SpringBackGradientScaleDownLevel
     */
    public float getSpringBackGradientScaleDownLevel(){
        return mSpringBackGradientScaleDownLevel;
    }

    /**
     * When image's current scale level is smaller than DoubleTabScale,
     * the image will scale up to DoubleTapScale with DoubleTapGradientScaleUpLevel step by step if a double-tap gesture is detected.
     * Default doubleTalGradientScaleUpLevel is 1.05f.
     * @param doubleTapGradientScaleUpLevel
     */
    public void setDoubleTapGradientScaleUpLevel(float doubleTapGradientScaleUpLevel){
        mDoubleTapGradientScaleUpLevel = doubleTapGradientScaleUpLevel;
    }

    /**
     * @return current DoubleTapGradientScaleUpLevel
     */
    public float getDoubleTapGradientScaleUpLevel(){
        return mDoubleTapGradientScaleUpLevel;
    }

    /**
     * When image's current scale level is bigger than DoubleTabScale,
     * the image will scale down to InitScale with DoubleTapGradientScaleDownLevel step by step if a double-tap gesture is detected.
     * Default doubleTabGradientScaleDownLevel is 0.95f.
     * @param doubleTapGradientScaleDownLevel
     */
    public void setDoubleTabGradientScaleDownLevel(float doubleTapGradientScaleDownLevel){
        mDoubleTapGradientScaleDownLevel = doubleTapGradientScaleDownLevel;
    }

    /**
     * @return current DoubleTapGradientScaleDownLevel
     */
    public float getDoubleTapGradientScaleDownLevel(){
        return mDoubleTapGradientScaleDownLevel;
    }

    /**
     * When image's current rotation angle is bigger than AutoRotationTrigger, the image will rotate in the same direction and scale back to it's initial state if rotation gesture is released.
     * When image's current rotation angle is smaller than AutoRotationTrigger, the image will rotate in the opposite direction and scale back to it's initial state if rotation gesture is released.
     * Default AutoRotationTrigger is 60 (degrees).
     * @param autoRotationTrigger
     */
    public void setAutoRotationTrigger(float autoRotationTrigger){
        this.autoRotationTrigger = autoRotationTrigger;
    }

    /**
     * @return current AutoRotationTrigger
     */
    public float getAutoRotationTrigger(){
        return autoRotationTrigger;
    }

    /**
     * Default SpringBackRunnableDelay is 10 (milliseconds).
     * @param delay
     */
    public void setSpringBackRunnableDelay(int delay){
        springBackRunnableDelay = delay;
    }

    /**
     * @return current SpringBackRunnableDelay
     */
    public int getSpringBackRunnableDelay(){
        return springBackRunnableDelay;
    }

    /**
     * Default DoubleTapRunnableDelay is 10 (milliseconds).
     * @param delay
     */
    public void setDoubleTapScaleRunnableDelay(int delay){
        doubleTabScaleRunnableDelay = delay;
    }

    /**
     * @return current DoubleTabScaleRunnableDelay
     */
    public int getDoubleTabScaleRunnableDelay(){
        return doubleTabScaleRunnableDelay;
    }

    /**
     * Default AutoRotationRunnableDelay is 5 (milliseconds).
     * @param delay
     */
    public void setAutoRotationRunnableDelay(int delay){
        autoRotationRunnableDelay = delay;
    }

    /**
     * @return current AutoRotationRunnableDelay
     */
    public int getAutoRotationRunnalbleDelay(){
        return autoRotationRunnableDelay;
    }

    /**
     * Default AutoRotationRunnableTimes is 10 (times).
     * @param times
     */
    public void setAutoRotationRunnableTimes(int times){
        autoRotationRunnableTimes = times;
    }

    /**
     * @return current AutoRotationRunnableTimes
     */
    public int getAutoRotationRunnableTimes(){
        return autoRotationRunnableTimes;
    }

}
