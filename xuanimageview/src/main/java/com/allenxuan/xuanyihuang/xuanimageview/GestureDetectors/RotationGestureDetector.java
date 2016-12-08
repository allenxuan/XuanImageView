package com.allenxuan.xuanyihuang.xuanimageview.GestureDetectors;

import android.util.Log;
import android.view.MotionEvent;

import com.allenxuan.xuanyihuang.xuanimageview.XuanImageView;

/**
 * Created by xuanyihuang on 9/4/16.
 */

public class RotationGestureDetector {
    private int mXuanImageViewWith;
    private static final int INVALID_POINTER_ID = -1;
    private float sX, sY, fX, fY;
    private float nfX, nfY, nsX, nsY;
    private int ptrID1, ptrID2;
    private int ptrID1_Index, ptrID2_Index, ptr_Index;
    private float mAngle;
    private float mAngleAtPresent;
    private float mPreviousAngle;
    private float mPivotX;
    private float mPivotY;
    private boolean mIsRotated;
    private int mPointerCount;
    private float mBasicRotationTrigger;
    private float mRotationTrigger;

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
        mAngle = angle;
    }

    public void setmPreviousAngle(float angle){
        mPreviousAngle = angle;
    }

    public boolean IsRotated(){
        return  mIsRotated;
    }

    public void invalidateTouchPointers(){
        ptrID1 = INVALID_POINTER_ID;
        ptrID2 = INVALID_POINTER_ID;
    }


    public boolean canStillRotate() {
        if((ptrID1 != INVALID_POINTER_ID) && (ptrID2 != INVALID_POINTER_ID))
            return true;
        else
            return false;
    }

    public RotationGestureDetector(OnRotationGestureListener listener, int XuanImageViewWidth){
        mXuanImageViewWith = XuanImageViewWidth;
        mListener = listener;
        ptrID1 = INVALID_POINTER_ID;
        ptrID2 = INVALID_POINTER_ID;
        ptrID1_Index = INVALID_POINTER_ID;
        ptrID2_Index = INVALID_POINTER_ID;
        ptr_Index = INVALID_POINTER_ID;
        mAngle = 0;
        mPreviousAngle = 0;
        mPivotX = 0;
        mPivotY = 0;
        mIsRotated = false;
        mPointerCount = 0;
        mBasicRotationTrigger = 10;
        mRotationTrigger = mBasicRotationTrigger;
    }

    public boolean onTouchEvent(MotionEvent event){
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //第一个手指按下
                ptrID1 = event.getPointerId(event.getActionIndex());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //第二个按下的手指的id保存在ptrID2中
                mPointerCount = event.getPointerCount();
                if(mPointerCount == 2) {
                    ptrID2 = event.getPointerId(event.getActionIndex());
                    ptrID1_Index = event.findPointerIndex(ptrID1);
                    ptrID2_Index = event.findPointerIndex(ptrID2);
                    try {
                        sX = event.getX(ptrID1_Index);
                        sY = event.getY(ptrID1_Index);
                        fX = event.getX(ptrID2_Index);
                        fY = event.getY(ptrID2_Index);

                        if(lineDistance(fX,fY,sX,sY) <= (mXuanImageViewWith / 3)){
                            mRotationTrigger = mBasicRotationTrigger * 2;
                        }
                        else
                            mRotationTrigger = mBasicRotationTrigger;
                    }catch(Exception e){
                        //pointer index out of range exception
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(ptrID1 != INVALID_POINTER_ID && ptrID2 != INVALID_POINTER_ID){
                    ptrID1_Index = event.findPointerIndex(ptrID1);
                    ptrID2_Index = event.findPointerIndex(ptrID2);
                    try {
                        nsX = event.getX(ptrID1_Index);
                        nsY = event.getY(ptrID1_Index);
                        nfX = event.getX(ptrID2_Index);
                        nfY = event.getY(ptrID2_Index);
                    } catch (Exception e){
                        //pointer index out of range exception
                        return true;
                    }
                    mPivotX = (nsX + nfX) / 2.0f;
                    mPivotY = (nsY + nfY) / 2.0f;

                    mAngleAtPresent = angleBetweenLines(fX, fY, sX, sY, nfX, nfY, nsX, nsY);



                    if (mListener != null) {
                        if(mIsRotated){
                            mPreviousAngle = mAngle;
                            mAngle = mAngleAtPresent;
                            mListener.OnRotate(this);
                        }
                        else if(Math.abs(mAngleAtPresent) >= mRotationTrigger){
                            sX = nsX;
                            sY = nsY;
                            fX = nfX;
                            fY = nfY;
                            mIsRotated = true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                ptrID1 = INVALID_POINTER_ID;
                ptrID2 = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                ptr_Index = event.getActionIndex();
                if(ptrID1 == event.getPointerId(ptr_Index) || ptrID2 == event.getPointerId(ptr_Index)){
                    ptrID1 = INVALID_POINTER_ID;
                    ptrID2 = INVALID_POINTER_ID;
                    if(mListener != null){
                        if(mIsRotated) {
                            mListener.StopRotate(this);
                            mIsRotated = false;
                        }
                    }
                }
//                if(event.getPointerId(ptr_Index) == ptrID1){
//                    ptrID1 = INVALID_POINTER_ID;
//                    if(mListener != null && mIsRotated){
//                        mListener.StopRotate(this);
//                        mIsRotated = false;
//                    }
//                }
//                else if(event.getPointerId(ptr_Index) == ptrID2){
//                    ptrID2 = INVALID_POINTER_ID;
//                    if(mListener != null && mIsRotated){
//                        mListener.StopRotate(this);
//                        mIsRotated = false;
//                    }
//                }
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

        Log.d("angleBetweenLines","" + angle);
        return angle;
    }

    private double lineDistance(float fX, float fY, float sX, float sY){
        return  Math.sqrt((fX - sX) * (fX - sX) + (fY - sY) * (fY - sY));
    }


    public static interface OnRotationGestureListener {
        public abstract boolean OnRotate(RotationGestureDetector rotationGestureDetector);
        public abstract boolean StopRotate(RotationGestureDetector rotationGestureDetector);
    }
}
