package com.allenxuan.xuanyihuang.xuanimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import com.allenxuan.xuanyihuang.xuanimageview.constants.XuanImageViewSettings;

/**
 * Created by xuanyihuang on 8/30/16.
 */

public class XuanImageView extends ImageView{
    private int XuanImageViewWidth;
    private int XuanImageViewHeight;
    private int XuanImageViewCenterX;
    private int XuanImageViewCenterY;
    private int ImageCenterX;
    private int ImageCenterY;
    private int mOrientation;   //1 for portrait, 2 for landscape
    private int mAutoRotateCategory;
    private float mInitScale;   //for landscape
    private float mInitPortraitScale;
    private float mTempInitPortraitScale;
    private float mMaxScale;
    private float mPortraitMaxScale;
    private float mTempPortraitMaxScale;
    private boolean mRotationToggle;
    private float mMaxScaleMultiple;
    private float mDoubleTabScaleMultiple;
    private float mDoubleTabScale;
    private float mPortraitDoubleTabScale;
    private float mTempPortraitDoubleTabScale;
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
    private double allowablePortraitFloatError;
    private float currentScaleLevel;
    private float currentAbsScaleLevel;
    private float autoRotationTrigger;
    private int autoRotationRunnableDelay;
    private int autoRotationRunnableTimes;
    private int doubleTabScaleRunnableDelay;
    private int springBackRunnableDelay;
    private boolean hasDrawable;
    private boolean knowViewSize;

    public XuanImageView(Context context) {
        this(context, null);
    }

    public XuanImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XuanImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        setScaleType(ScaleType.MATRIX);

        mScaleMatrix = new Matrix();

        mScaleGestureDetector = new ScaleGestureDetector(context, constructOnScaleGestureListener());
        mGestureDetector = new GestureDetector(context, constructOnGestureListener());

        initCustomAttrs(context, attrs);

        mOrientation = XuanImageViewSettings.ORIENTATION_LANDSCAPE;   //1 for portrait, 2 for landscape
        isAutoScale = false;
        mLastPointerCount = 0;
        mAngle = 0;
        mPreviousAngle = 0;
        allowableFloatError = 1E-6;
        allowablePortraitFloatError = 1E-12;
        currentScaleLevel = 1;
    }

    private ScaleGestureDetector.OnScaleGestureListener constructOnScaleGestureListener() {
        return new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                float scaleFactor = scaleGestureDetector.getScaleFactor();

                currentScaleLevel = getCurrentScaleLevel();
                currentAbsScaleLevel = Math.abs(currentScaleLevel);
                Log.d("currentScaleLevel", "" + currentScaleLevel);

                boolean isRotating = false;
                boolean justScale = false;

                if(mRotateGestureDetector.IsRotated()){
                    // is rotating
                    isRotating = true;
                }
                else{
                    // not rotating, just scaling
                    if(mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_RESTORATION){
                        if((currentScaleLevel <= mMaxScale && scaleFactor > 1.0f) || (currentScaleLevel >= mInitScale && scaleFactor < 1.0f))
                            justScale = true;

                    }
                    else if(mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_MAGNETISM){
                        if (mOrientation == XuanImageViewSettings.ORIENTATION_LANDSCAPE) {
                            if ((currentAbsScaleLevel <= mMaxScale && scaleFactor > 1.0f) || (currentAbsScaleLevel >= mInitScale && scaleFactor < 1.0f))
                                justScale = true;
                        } else if (mOrientation == XuanImageViewSettings.ORIENTATION_PORTRAIT) {
                            if ((currentAbsScaleLevel <= mTempPortraitMaxScale && scaleFactor > 1.0f) || (currentAbsScaleLevel >= mTempInitPortraitScale && scaleFactor < 1.0f))
                                justScale = true;
                        }
                    }
                }


                if(isRotating) {
                    mScaleMatrix.postScale(scaleFactor, scaleFactor, mRotateGestureDetector.getPivotX(), mRotateGestureDetector.getPivotY());
                }
                else if(justScale)
                {
                    mScaleMatrix.postScale(scaleFactor, scaleFactor, scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
                    checkBorderAndCenterWhenScale();
                }


                setImageMatrix(mScaleMatrix);
                mLastScaleFocusX = scaleGestureDetector.getFocusX();
                mLastScaleFocusY = scaleGestureDetector.getFocusY();

                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
                Log.d("onScaleBegin-->", "");
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
                Log.d("onScaleEnd-->", "");
            }
        };
    }

    private GestureDetector.OnGestureListener constructOnGestureListener() {
        return new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                currentScaleLevel = getCurrentScaleLevel();
                currentAbsScaleLevel = Math.abs(currentScaleLevel);
                float x = e.getX();
                float y = e.getY();

                if (isAutoScale)
                    return true;


                if (mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_RESTORATION) {
                    if (currentScaleLevel < mDoubleTabScale) {
                        postDelayed(new AutoScaleRunnable(mDoubleTabScale, x, y, mDoubleTapGradientScaleUpLevel, mDoubleTapGradientScaleDownLevel), doubleTabScaleRunnableDelay);
                        isAutoScale = true;
                    } else {
                        postDelayed(new AutoScaleRunnable(mInitScale, x, y, mDoubleTapGradientScaleUpLevel, mDoubleTapGradientScaleDownLevel), doubleTabScaleRunnableDelay);
                        isAutoScale = true;
                    }
                } else if (mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_MAGNETISM) {
                    if (mOrientation == XuanImageViewSettings.ORIENTATION_LANDSCAPE) {
                        if (currentAbsScaleLevel < mDoubleTabScale) {
                            postDelayed(new AutoScaleRunnable(mDoubleTabScale * (currentScaleLevel / currentAbsScaleLevel), x, y, mDoubleTapGradientScaleUpLevel, mDoubleTapGradientScaleDownLevel), doubleTabScaleRunnableDelay);
                            isAutoScale = true;
                        } else {
                            postDelayed(new AutoScaleRunnable(mInitScale * (currentScaleLevel / currentAbsScaleLevel), x, y, mDoubleTapGradientScaleUpLevel, mDoubleTapGradientScaleDownLevel), doubleTabScaleRunnableDelay);
                            isAutoScale = true;
                        }
                    } else if (mOrientation == XuanImageViewSettings.ORIENTATION_PORTRAIT) {
                        if (currentAbsScaleLevel < mTempPortraitDoubleTabScale) {
                            postDelayed(new AutoScaleRunnable(mTempPortraitDoubleTabScale * (currentScaleLevel / currentAbsScaleLevel), x, y, mDoubleTapGradientScaleUpLevel, mDoubleTapGradientScaleDownLevel), doubleTabScaleRunnableDelay);
                            isAutoScale = true;
                        } else {
                            postDelayed(new AutoScaleRunnable(mTempInitPortraitScale * (currentScaleLevel / currentAbsScaleLevel), x, y, mDoubleTapGradientScaleUpLevel, mDoubleTapGradientScaleDownLevel), doubleTabScaleRunnableDelay);
                            isAutoScale = true;
                        }
                    }
                }

                return true;
            }
        };
    }


    private RotationGestureDetector.OnRotationGestureListener constructOnRotationGestureListener(){
        return new RotationGestureDetector.OnRotationGestureListener() {
            @Override
            public boolean OnRotate(RotationGestureDetector rotationGestureDetector) {
                mAngle = rotationGestureDetector.getAngle();
                mPreviousAngle = rotationGestureDetector.getPreviousAngle();
                mScaleMatrix.postRotate(mAngle - mPreviousAngle, rotationGestureDetector.getPivotX(), rotationGestureDetector.getPivotY());
                Log.d("cuurrentScale onRotate", "" + getCurrentScaleLevel());
                setImageMatrix(mScaleMatrix);

                return true;
            }

            @Override
            public boolean StopRotate(RotationGestureDetector rotationGestureDetector) {
                if (mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_RESTORATION)
                    AutoRotateRestoration(rotationGestureDetector);
                else if (mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_MAGNETISM)
                    AutoRotateMagnetism(rotationGestureDetector);

                return true;
            }
        };
    }

    private void initCustomAttrs(Context context, AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.xuanimageview);
        mRotationToggle = a.getBoolean(R.styleable.xuanimageview_RotationToggle, true);
        mAutoRotateCategory = a.getInteger(R.styleable.xuanimageview_AutoRotateCategory, XuanImageViewSettings.AUTO_ROTATE_CATEGORY_RESTORATION);
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
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);

        if (drawable == null) {
            hasDrawable = false;
            return;
        }

        if (!drawableHasSize(drawable))
            return;

        hasDrawable = true;
        initDrawableMatrix();
    }

    @Override
    public void setImageResource(int resId) {
        Drawable drawable = null;
        try {
            drawable = getResources().getDrawable(resId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setImageDrawable(drawable);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        knowViewSize = true;
        initDrawableMatrix();
    }

    private boolean drawableHasSize(Drawable drawable) {
        if ((drawable.getIntrinsicHeight() <= 0 || drawable.getIntrinsicWidth() <= 0)
                && (drawable.getMinimumHeight() <= 0 || drawable.getMinimumWidth() <= 0)
                && (drawable.getBounds().height() <= 0 || drawable.getBounds().width() <= 0))
            return false;
        else
            return true;
    }

    private void initDrawableMatrix() {
        if (!hasDrawable)
            return;
        if (!knowViewSize)
            return;

        if (mScaleMatrix == null)
            mScaleMatrix = new Matrix();
        else
            mScaleMatrix.reset();

        // get width and height of XuanImageView
        XuanImageViewWidth = getWidth();
        XuanImageViewHeight = getHeight();

        //get the center point of XuanImageView
        XuanImageViewCenterX = XuanImageViewWidth / 2;
        XuanImageViewCenterY = XuanImageViewHeight / 2;

        // instantiate mRotationGestureDetector after dimension of XuanImageView is got.
        mRotateGestureDetector = new RotationGestureDetector(constructOnRotationGestureListener(), XuanImageViewWidth);

        //get width and height of the image
        Drawable imageDrawable = getDrawable();
        if (imageDrawable == null)
            return;
        int imageWidth = imageDrawable.getIntrinsicWidth();
        int imageHeight = imageDrawable.getIntrinsicHeight();

        //image is scaled to fit the size of XuanImageView at the very beginning.
        float scale = Math.min(XuanImageViewWidth * 1.0f / imageWidth, XuanImageViewHeight * 1.0f / imageHeight);
        float portraitscale = Math.min(XuanImageViewWidth * 1.0f / imageHeight, XuanImageViewHeight * 1.0f / imageWidth);

        mInitScale = scale;
        mInitPortraitScale = portraitscale;
        mMaxScale = mMaxScaleMultiple * scale;
        mPortraitMaxScale = mMaxScaleMultiple * portraitscale;
        mDoubleTabScale = mDoubleTabScaleMultiple * scale;
        mPortraitDoubleTabScale = mDoubleTabScaleMultiple * portraitscale;

        //center of image overlaps with that of XuanImageView
        int deltaX = XuanImageViewWidth / 2 - imageWidth / 2;
        int deltaY = XuanImageViewHeight / 2 - imageHeight / 2;

        mScaleMatrix.postTranslate(deltaX, deltaY);
        mScaleMatrix.postScale(scale, scale, XuanImageViewWidth / 2, XuanImageViewHeight / 2);
        setImageMatrix(mScaleMatrix);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (mRotateGestureDetector == null)
            return true;

        boolean parentDisallowInterceptTouchEventFlag = true;

        // for DoubleTap gesture
        mGestureDetector.onTouchEvent(motionEvent);

        // for Scale gesture
        mScaleGestureDetector.onTouchEvent(motionEvent);


        currentScaleLevel = getCurrentScaleLevel();
        currentAbsScaleLevel = Math.abs(currentScaleLevel);
        if (mRotationToggle) {
            if (mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_RESTORATION) {
                if (mRotateGestureDetector.IsRotated() || Math.abs(currentScaleLevel - mInitScale) < allowableFloatError) {
                    if (!mRotateGestureDetector.IsRotated())
                        parentDisallowInterceptTouchEventFlag = false;

                    // for Rotation gesture
                    mRotateGestureDetector.onTouchEvent(motionEvent);
                }
            } else if (mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_MAGNETISM) {
                if (mOrientation == XuanImageViewSettings.ORIENTATION_LANDSCAPE) {
                    if (mRotateGestureDetector.IsRotated() || Math.abs(currentAbsScaleLevel - mInitScale) < allowableFloatError) {
                        if (!mRotateGestureDetector.IsRotated())
                            parentDisallowInterceptTouchEventFlag = false;

                        // for Rotation gesture
                        mRotateGestureDetector.onTouchEvent(motionEvent);

                    }
                } else if (mOrientation == XuanImageViewSettings.ORIENTATION_PORTRAIT) {
                    if (mRotateGestureDetector.IsRotated() || Math.abs(currentAbsScaleLevel - mTempInitPortraitScale) < allowablePortraitFloatError) {
                        if (!mRotateGestureDetector.IsRotated())
                            parentDisallowInterceptTouchEventFlag = false;

                        // for Rotation gesture
                        mRotateGestureDetector.onTouchEvent(motionEvent);
                    }
                }
            }
        }

        //pointerCount won't be 0
        int pointerCount = motionEvent.getPointerCount();
        float pivotX = 0;
        float pivotY = 0;
        for (int i = 0; i < pointerCount; i++) {
            pivotX += motionEvent.getX(i);
            pivotY += motionEvent.getY(i);
        }
        pivotX /= pointerCount;   //get integer result of the division
        pivotY /= pointerCount;

        // when image is being dragged, generally, pointCount == mLastCount holds, so old state is not saved.
        if (pointerCount != mLastPointerCount) {
            mLastX = pivotX;
            mLastY = pivotY;
            mLastPointerCount = pointerCount;
        }

        RectF rectF = getMatrixRectF();

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (getParent() != null)
                    getParent().requestDisallowInterceptTouchEvent(parentDisallowInterceptTouchEventFlag);
                break;
            case MotionEvent.ACTION_MOVE:
                if (getParent() != null)
                    getParent().requestDisallowInterceptTouchEvent(parentDisallowInterceptTouchEventFlag);
                float deltaX = pivotX - mLastX;
                float deltaY = pivotY - mLastY;

                if (getDrawable() != null) {
                    if (!mRotateGestureDetector.IsRotated()) {
                        if (rectF.width() <= XuanImageViewWidth)
                            deltaX = 0;
                        if (rectF.height() <= XuanImageViewHeight)
                            deltaY = 0;
                    }
                    mScaleMatrix.postTranslate(deltaX, deltaY);

                    if (!mRotateGestureDetector.IsRotated())
                        checkBorderAndCenterWhenTranslate();
                    setImageMatrix(mScaleMatrix);
                }
                mLastX = pivotX;
                mLastY = pivotY;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //one finger loosening (multi-touch)
                if (pointerCount - 1 < 2) {
                    if (mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_RESTORATION) {
                        //spring back to mInitScale or mMaxScale
                        if ((currentScaleLevel < mInitScale) && !isAutoRotated) {
                            postDelayed(new AutoScaleRunnable(mInitScale, mLastScaleFocusX, mLastScaleFocusY, mSpringBackGradientScaleUpLevel, mSpringBackGradientScaleDownLevel), springBackRunnableDelay);
                            isAutoScale = true;
                        } else if ((currentScaleLevel > mMaxScale) && !isAutoRotated) {
                            postDelayed(new AutoScaleRunnable(mMaxScale, mLastScaleFocusX, mLastScaleFocusY, mSpringBackGradientScaleUpLevel, mSpringBackGradientScaleDownLevel), springBackRunnableDelay);
                            isAutoScale = true;
                        }
                    } else if (mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_MAGNETISM) {
                        if (mOrientation == XuanImageViewSettings.ORIENTATION_LANDSCAPE) {
                            if (currentAbsScaleLevel < mInitScale && !isAutoRotated) {
                                postDelayed(new AutoScaleRunnable(mInitScale * (currentScaleLevel / currentAbsScaleLevel), mLastScaleFocusX, mLastScaleFocusY, mSpringBackGradientScaleUpLevel, mSpringBackGradientScaleDownLevel), springBackRunnableDelay);
                                isAutoScale = true;
                            } else if ((currentAbsScaleLevel > mMaxScale) && !isAutoRotated) {
                                postDelayed(new AutoScaleRunnable(mMaxScale * (currentScaleLevel / currentAbsScaleLevel), mLastScaleFocusX, mLastScaleFocusY, mSpringBackGradientScaleUpLevel, mSpringBackGradientScaleDownLevel), springBackRunnableDelay);
                                isAutoScale = true;
                            }
                        } else if (mOrientation == XuanImageViewSettings.ORIENTATION_PORTRAIT) {
                            if (currentAbsScaleLevel < mTempInitPortraitScale && !isAutoRotated) {
                                postDelayed(new AutoScaleRunnable(mTempInitPortraitScale * (currentScaleLevel / currentAbsScaleLevel), mLastScaleFocusX, mLastScaleFocusY, mSpringBackGradientScaleUpLevel, mSpringBackGradientScaleDownLevel), springBackRunnableDelay);
                                isAutoScale = true;
                            } else if ((currentAbsScaleLevel > mTempPortraitMaxScale) && !isAutoRotated) {
                                postDelayed(new AutoScaleRunnable(mTempPortraitMaxScale * (currentScaleLevel / currentAbsScaleLevel), mLastScaleFocusX, mLastScaleFocusY, mSpringBackGradientScaleUpLevel, mSpringBackGradientScaleDownLevel), springBackRunnableDelay);
                                isAutoScale = true;
                            }
                        }
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

    private void checkBorderAndCenterWhenScale() {

        RectF rectF = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        /**
         * If width or height of image is bigger than that of XuanImageView,
         * should prevent image's edge being far away from XuanImageView's edge.
         */
        if (rectF.width() >= XuanImageViewWidth) {
            if (rectF.left > 0)
                deltaX = -rectF.left;
            if (rectF.right < XuanImageViewWidth)
                deltaX = XuanImageViewWidth - rectF.right;

        }
        if (rectF.height() >= XuanImageViewHeight) {
            if (rectF.top > 0)
                deltaY = -rectF.top;
            if (rectF.bottom < XuanImageViewHeight)
                deltaY = XuanImageViewHeight - rectF.bottom;
        }

        /**
         * If width or height of image is smaller than that of XuanImageView,
         * make sure the image, in width dimension or height dimension, is centered.
         */
        if (rectF.width() < XuanImageViewWidth) {
            deltaX = XuanImageViewWidth / 2.0f - rectF.left - rectF.width() / 2.0f;
        }
        if (rectF.height() < XuanImageViewHeight) {
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
        if (rectF.width() >= XuanImageViewWidth) {
            if (rectF.left > 0)
                deltaX = -rectF.left;
            if (rectF.right < XuanImageViewWidth)
                deltaX = XuanImageViewWidth - rectF.right;

        }
        if (rectF.height() >= XuanImageViewHeight) {
            if (rectF.top > 0)
                deltaY = -rectF.top;
            if (rectF.bottom < XuanImageViewHeight)
                deltaY = XuanImageViewHeight - rectF.bottom;
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }


    private float getCurrentScaleLevel() {
        float matrixArray[] = new float[9];
        mScaleMatrix.getValues(matrixArray);

        return matrixArray[Matrix.MSCALE_X];
    }

    private RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rectF = new RectF();
        Drawable image = getDrawable();
        if (image != null) {
            rectF.set(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }

        return rectF;
    }

    private boolean calculateImageCenterCoordinates() {
        RectF rectF = getMatrixRectF();
        ImageCenterX = (int) ((rectF.left + rectF.right) / 2);
        ImageCenterY = (int) ((rectF.top + rectF.bottom) / 2);

        return true;
    }

    public void AutoRotateMagnetism(RotationGestureDetector rotationGestureDetector) {
        mAngle = rotationGestureDetector.getAngle();//mAngle's range: [-180 degress, 180 degress]
        float autoRotateAngle;
        int quotient;
        float remainder;

        quotient = ((int) mAngle) / 90;
        remainder = mAngle % 90;

        if (remainder >= autoRotationTrigger) {
            autoRotateAngle = 90 - remainder;
            if ((quotient + 1) % 2 == 0)
                mOrientation = XuanImageViewSettings.ORIENTATION_LANDSCAPE;
            else
                mOrientation = XuanImageViewSettings.ORIENTATION_PORTRAIT;
        } else if (remainder <= -autoRotationTrigger) {
            autoRotateAngle = -90 - remainder;
            if ((quotient - 1) % 2 == 0)
                mOrientation = XuanImageViewSettings.ORIENTATION_LANDSCAPE;
            else
                mOrientation = XuanImageViewSettings.ORIENTATION_PORTRAIT;
        } else {
            autoRotateAngle = -remainder;
            if (quotient % 2 == 0)
                mOrientation = XuanImageViewSettings.ORIENTATION_LANDSCAPE;
            else
                mOrientation = XuanImageViewSettings.ORIENTATION_PORTRAIT;
        }

        postDelayed(new AutoRotateRunnable(autoRotateAngle, getCurrentScaleLevel() / (float) Math.cos(Math.toRadians(mAngle)), autoRotationRunnableTimes), autoRotationRunnableDelay);
        isAutoRotated = true;

        rotationGestureDetector.setAngle(mAngle + autoRotateAngle);
        rotationGestureDetector.setPreviousAngle(mAngle + autoRotateAngle);
    }

    public void AutoRotateRestoration(RotationGestureDetector rotationGestureDetector) {
        mAngle = rotationGestureDetector.getAngle();//mAngle's range: [-180 degress, 180 degress]
        float autoRotateAngle;

        if (mAngle >= autoRotationTrigger)
            autoRotateAngle = 360 - mAngle;
        else if (mAngle <= -autoRotationTrigger)
            autoRotateAngle = -360 - mAngle;
        else
            autoRotateAngle = -mAngle;

        postDelayed(new AutoRotateRunnable(autoRotateAngle, getCurrentScaleLevel() / (float) Math.cos(Math.toRadians(mAngle)), autoRotationRunnableTimes), autoRotationRunnableDelay);
        isAutoRotated = true;

        rotationGestureDetector.setAngle(0.0f);
        rotationGestureDetector.setPreviousAngle(0.0f);
    }

    private class AutoScaleRunnable implements Runnable {
        float targetScale;
        float targetAbsScale;
        float FocusX;
        float FocusY;
        float scaleFactor;

        AutoScaleRunnable(float targetScale, float FocusX, float FocusY, float GradientScaleUp, float GradientScaleDown) {
            this.targetScale = targetScale;
            this.targetAbsScale = Math.abs(targetScale);
            this.FocusX = FocusX;
            this.FocusY = FocusY;
            currentScaleLevel = getCurrentScaleLevel();
            currentAbsScaleLevel = Math.abs(currentScaleLevel);
            if (currentAbsScaleLevel < targetAbsScale)
                scaleFactor = GradientScaleUp;
            else if (currentAbsScaleLevel > targetAbsScale)
                scaleFactor = GradientScaleDown;
            else if (currentAbsScaleLevel == targetAbsScale)
                scaleFactor = 1.0f;
        }

        @Override
        public void run() {
            mScaleMatrix.postScale(scaleFactor, scaleFactor, FocusX, FocusY);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);

            currentScaleLevel = getCurrentScaleLevel();
            currentAbsScaleLevel = Math.abs(currentScaleLevel);
            if ((scaleFactor < 1.0f && currentAbsScaleLevel > targetAbsScale)
                    || (scaleFactor > 1.0f && currentAbsScaleLevel < targetAbsScale)) {
                postDelayed(this, springBackRunnableDelay);
            } else {
                scaleFactor = targetScale / currentScaleLevel;

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
            if (mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_RESTORATION) {
                ScalePerTime = Math.pow(mInitScale / initScaleLevel, 1.0 / TotalRotateTimes);
            } else if (mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_MAGNETISM) {
                if (mOrientation == XuanImageViewSettings.ORIENTATION_LANDSCAPE)
                    ScalePerTime = Math.pow(mInitScale / initScaleLevel, 1.0 / TotalRotateTimes);
                else if (mOrientation == XuanImageViewSettings.ORIENTATION_PORTRAIT)
                    ScalePerTime = Math.pow(mInitPortraitScale / initScaleLevel, 1.0 / TotalRotateTimes);
            }

        }

        @Override
        public void run() {
            calculateImageCenterCoordinates();

            mScaleMatrix.postRotate(AnglePerTime, ImageCenterX, ImageCenterY);
            mScaleMatrix.postScale((float) ScalePerTime, (float) ScalePerTime, ImageCenterX, ImageCenterY);
            mScaleMatrix.postTranslate((XuanImageViewCenterX - ImageCenterX) / (TotalRotateTimes - AccumulativeRotateTimes), (XuanImageViewCenterY - ImageCenterY) / (TotalRotateTimes - AccumulativeRotateTimes));
            setImageMatrix(mScaleMatrix);
            AccumulativeRotateTimes++;
            AccumulativeRotateAngles += AnglePerTime;


            if (AccumulativeRotateTimes < TotalRotateTimes) {
                postDelayed(this, autoRotationRunnableDelay);
            } else {
                currentScaleLevel = getCurrentScaleLevel();
                currentAbsScaleLevel = Math.abs(currentScaleLevel);
                calculateImageCenterCoordinates();
                mScaleMatrix.postRotate(targetRotateAngle - AccumulativeRotateAngles, ImageCenterX, ImageCenterY);
                if (mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_RESTORATION) {
                    mScaleMatrix.postScale(Math.abs(mInitScale / currentScaleLevel), Math.abs(mInitScale / currentScaleLevel), ImageCenterX, ImageCenterY);
                } else if (mAutoRotateCategory == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_MAGNETISM) {
                    if (mOrientation == XuanImageViewSettings.ORIENTATION_LANDSCAPE) {
                        mScaleMatrix.postScale(Math.abs(mInitScale / currentScaleLevel), Math.abs(mInitScale / currentScaleLevel), ImageCenterX, ImageCenterY);
                    } else if (mOrientation == XuanImageViewSettings.ORIENTATION_PORTRAIT) {
//                        mScaleMatrix.postScale(Math.abs(mInitPortraitScale / getCurrentScaleLevel()), Math.abs(mInitPortraitScale / getCurrentScaleLevel()), ImageCenterX, ImageCenterY);
                        mTempInitPortraitScale = Math.abs(currentScaleLevel);
                        mTempPortraitMaxScale = mTempInitPortraitScale * mMaxScaleMultiple;
                        mTempPortraitDoubleTabScale = mTempInitPortraitScale * mDoubleTabScaleMultiple;
                    }
                }
                mScaleMatrix.postTranslate(XuanImageViewCenterX - ImageCenterX, XuanImageViewCenterY - ImageCenterY);

                checkBorderAndCenterWhenScale();

                setImageMatrix(mScaleMatrix);
                isAutoRotated = false;
            }


        }
    }

    /**
     * Set a boolean value to determine whether rotation function is turned on.
     *
     * @param toggle determine whether rotation function is turned on
     */
    public void setRotationToggle(boolean toggle) {
        mRotationToggle = toggle;
    }

    /**
     * @return current RotationToggle
     */
    public boolean getRotationToggle() {
        return mRotationToggle;
    }

    /**
     * Set AutoRotateCategory, there are two alternative values of it : XuanImageViewSettings.AUTO_ROTATE_CATEGORY_RESTORATION, XuanImageViewSettings.AUTO_ROTATE_CATEGORY_MAGNETISM.
     *
     * @param category
     */
    public void setAutoRotateCategory(int category) {
        if (category == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_RESTORATION || category == XuanImageViewSettings.AUTO_ROTATE_CATEGORY_MAGNETISM)
            mAutoRotateCategory = category;
        else
            mAutoRotateCategory = XuanImageViewSettings.AUTO_ROTATE_CATEGORY_RESTORATION;
    }

    /**
     * @return current AutoRotateCategory
     */
    public int getAutoRotateCategory() {
        return mAutoRotateCategory;
    }

    /**
     * An image is scaled to an InitScale to fit the size of XuanImageView at the very beginning.
     * MaxScale = MaxScaleMultiple * InitScale holds.
     *
     * @param maxScaleMultiple
     */
    public void setMaxScaleMultiple(float maxScaleMultiple) {
        mMaxScaleMultiple = maxScaleMultiple;
    }

    /**
     * @return current MaxScaleMultiple
     */
    public float getMaxScaleMultiple() {
        return mMaxScaleMultiple;
    }

    /**
     * When image's current scale level is smaller than DoubleTabScale, the image will scale up to DoubleTapScale if an double-tap gesture is detected.
     * DoubleTapScale = DoubleTabScaleMultiple * InitScale holds.
     *
     * @param doubleTabScaleMultiple
     */
    public void setDoubleTabScaleMultiple(float doubleTabScaleMultiple) {
        mDoubleTabScaleMultiple = doubleTabScaleMultiple;
    }

    /**
     * @return current DoubleTabScaleMultiple
     */
    public float getDoubleTabScaleMultiple() {
        return mDoubleTabScaleMultiple;
    }

    /**
     * If current scale level is smaller than InitScale and image is not in rotation state,
     * the image will scale up to InitScale with SpringBackGradientScaleUpLevel step by step.
     * Default springBackGradientScaleUpLevel is  1.01f.
     *
     * @param springBackGradientScaleUpLevel
     */
    public void setSpringBackGradientScaleUpLevel(float springBackGradientScaleUpLevel) {
        mSpringBackGradientScaleUpLevel = springBackGradientScaleUpLevel;
    }

    /**
     * @return current SpringBackGradientScaleUpLevel
     */
    public float getSpringBackGradientScaleUpLevel() {
        return mSpringBackGradientScaleUpLevel;
    }

    /**
     * If current scale level is bigger than MaxScale and image is not in rotation state,
     * the image will scale down to MaxScale with SpringBackGradientScaleDownLevel step by step.
     * Default springBackGradientScaleDownLevel is 0.99f.
     *
     * @param springBackGradientScaleDownLevel
     */
    public void setSpringBackGradientScaleDownLevel(float springBackGradientScaleDownLevel) {
        mSpringBackGradientScaleDownLevel = springBackGradientScaleDownLevel;
    }

    /**
     * @return current SpringBackGradientScaleDownLevel
     */
    public float getSpringBackGradientScaleDownLevel() {
        return mSpringBackGradientScaleDownLevel;
    }

    /**
     * When image's current scale level is smaller than DoubleTabScale,
     * the image will scale up to DoubleTapScale with DoubleTapGradientScaleUpLevel step by step if a double-tap gesture is detected.
     * Default doubleTalGradientScaleUpLevel is 1.05f.
     *
     * @param doubleTapGradientScaleUpLevel
     */
    public void setDoubleTapGradientScaleUpLevel(float doubleTapGradientScaleUpLevel) {
        mDoubleTapGradientScaleUpLevel = doubleTapGradientScaleUpLevel;
    }

    /**
     * @return current DoubleTapGradientScaleUpLevel
     */
    public float getDoubleTapGradientScaleUpLevel() {
        return mDoubleTapGradientScaleUpLevel;
    }

    /**
     * When image's current scale level is bigger than DoubleTabScale,
     * the image will scale down to InitScale with DoubleTapGradientScaleDownLevel step by step if a double-tap gesture is detected.
     * Default doubleTabGradientScaleDownLevel is 0.95f.
     *
     * @param doubleTapGradientScaleDownLevel
     */
    public void setDoubleTabGradientScaleDownLevel(float doubleTapGradientScaleDownLevel) {
        mDoubleTapGradientScaleDownLevel = doubleTapGradientScaleDownLevel;
    }

    /**
     * @return current DoubleTapGradientScaleDownLevel
     */
    public float getDoubleTapGradientScaleDownLevel() {
        return mDoubleTapGradientScaleDownLevel;
    }

    /**
     * When image's current rotation angle is bigger than AutoRotationTrigger, the image will rotate in the same direction and scale back to it's initial state if rotation gesture is released.
     * When image's current rotation angle is smaller than AutoRotationTrigger, the image will rotate in the opposite direction and scale back to it's initial state if rotation gesture is released.
     * Default AutoRotationTrigger is 60 (degrees).
     *
     * @param autoRotationTrigger
     */
    public void setAutoRotationTrigger(float autoRotationTrigger) {
        this.autoRotationTrigger = autoRotationTrigger;
    }

    /**
     * @return current AutoRotationTrigger
     */
    public float getAutoRotationTrigger() {
        return autoRotationTrigger;
    }

    /**
     * Default SpringBackRunnableDelay is 10 (milliseconds).
     *
     * @param delay
     */
    public void setSpringBackRunnableDelay(int delay) {
        springBackRunnableDelay = delay;
    }

    /**
     * @return current SpringBackRunnableDelay
     */
    public int getSpringBackRunnableDelay() {
        return springBackRunnableDelay;
    }

    /**
     * Default DoubleTapRunnableDelay is 10 (milliseconds).
     *
     * @param delay
     */
    public void setDoubleTapScaleRunnableDelay(int delay) {
        doubleTabScaleRunnableDelay = delay;
    }

    /**
     * @return current DoubleTabScaleRunnableDelay
     */
    public int getDoubleTabScaleRunnableDelay() {
        return doubleTabScaleRunnableDelay;
    }

    /**
     * Default AutoRotationRunnableDelay is 5 (milliseconds).
     *
     * @param delay
     */
    public void setAutoRotationRunnableDelay(int delay) {
        autoRotationRunnableDelay = delay;
    }

    /**
     * @return current AutoRotationRunnableDelay
     */
    public int getAutoRotationRunnalbleDelay() {
        return autoRotationRunnableDelay;
    }

    /**
     * Default AutoRotationRunnableTimes is 10 (times).
     *
     * @param times
     */
    public void setAutoRotationRunnableTimes(int times) {
        autoRotationRunnableTimes = times;
    }

    /**
     * @return current AutoRotationRunnableTimes
     */
    public int getAutoRotationRunnableTimes() {
        return autoRotationRunnableTimes;
    }

}
