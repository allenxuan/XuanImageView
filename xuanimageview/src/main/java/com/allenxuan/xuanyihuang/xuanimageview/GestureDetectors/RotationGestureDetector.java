package com.allenxuan.xuanyihuang.xuanimageview.GestureDetectors;

import android.view.MotionEvent;

/**
 * Created by xuanyihuang on 9/4/16.
 */

public class RotationGestureDetector {
    private static final int INVALID_POINTER_ID = -1;
    private float fX, fY, sX, sY;
    private int ptrID1, ptrID2;
    private float mAngle;
    private float mPreviousAngle;
    private float mPivotX;
    private float mPivotY;


    private OnRotationGestureListener mListener;

    public float getAngle() {
        return mAngle;
    }

    public float getPreviousAngle(){
        return  mPreviousAngle;
    }

    public float getPivotX(){
        return mPivotX;
    }

    public float getPivotY(){
        return mPivotY;
    }

    public void setAngle(float angle){
        mAngle = 0;
    }

    public void setmPreviousAngle(float angle){
        mPreviousAngle = 0;
    }

    public boolean canStillRotate() {
        if((ptrID1 != INVALID_POINTER_ID) && (ptrID2 != INVALID_POINTER_ID))
            return true;
        else
            return false;
    }

    public RotationGestureDetector(OnRotationGestureListener listener){
        mListener = listener;
        ptrID1 = INVALID_POINTER_ID;
        ptrID2 = INVALID_POINTER_ID;
        mAngle = 0;
        mPreviousAngle = 0;
        mPivotX = 0;
        mPivotY = 0;
    }

    public boolean onTouchEvent(MotionEvent event){
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                //第一个手指按下
                ptrID1 = event.getPointerId(event.getActionIndex());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //第二个,第三个,第四个,第n个手指按下均执行,最后一个按下的手指的id保存在ptrID2中
                ptrID2 = event.getPointerId(event.getActionIndex());
                sX = event.getX(event.findPointerIndex(ptrID1));
                sY = event.getY(event.findPointerIndex(ptrID1));
                fX = event.getX(event.findPointerIndex(ptrID2));
                fY = event.getY(event.findPointerIndex(ptrID2));
                mPivotX = (sX + fX) / 2.0f;
                mPivotY = (sY + fY) / 2.0f;
                break;
            case MotionEvent.ACTION_MOVE:
                if(ptrID1 != INVALID_POINTER_ID && ptrID2 != INVALID_POINTER_ID){
                    float nfX, nfY, nsX, nsY;
                    nsX = event.getX(event.findPointerIndex(ptrID1));
                    nsY = event.getY(event.findPointerIndex(ptrID1));
                    nfX = event.getX(event.findPointerIndex(ptrID2));
                    nfY = event.getY(event.findPointerIndex(ptrID2));
                    mPivotX = (nsX + nfX) / 2.0f;
                    mPivotY = (nsY + nfY) / 2.0f;

                    mPreviousAngle = mAngle;
                    mAngle = angleBetweenLines(fX, fY, sX, sY, nfX, nfY, nsX, nsY);


                    if (mListener != null) {
                        mListener.OnRotate(this);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                ptrID1 = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                ptrID2 = INVALID_POINTER_ID;
                if(mListener != null){
                    mListener.StopRotate(this);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                ptrID1 = INVALID_POINTER_ID;
                ptrID2 = INVALID_POINTER_ID;
                break;
        }
        return true;
    }

    private float angleBetweenLines (float fX, float fY, float sX, float sY, float nfX, float nfY, float nsX, float nsY)
    {
        float angle1 = (float) Math.atan2( (fY - sY), (fX - sX) );
        float angle2 = (float) Math.atan2( (nfY - nsY), (nfX - nsX) );

        //角度范围[-180度, +180度]
        float angle = ((float)Math.toDegrees(angle2 - angle1)) % 360;
        if (angle < -180.f) angle += 360.0f;
        if (angle > 180.f) angle -= 360.0f;
        return angle;
    }

    public static interface OnRotationGestureListener {
        public boolean OnRotate(RotationGestureDetector rotationDetector);
        public boolean StopRotate(RotationGestureDetector rotationGestureDetector);
    }
}
