package com.hxws.remote;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.hxws.remote.Wifi.MotionActivity;


public class MyViewPager extends ViewPager {

    private static final String TAG = "MyViewPager";

    private static final boolean D = false;


    public static final int STATE_DOWN = MotionEvent.ACTION_DOWN;
    public static final int STATE_UP = MotionEvent.ACTION_UP;
    public static final int STATE_MOVE = MotionEvent.ACTION_MOVE;

    int state = STATE_UP;


    //是否可以进行滑动

    private boolean isSlide = false;



    public void setSlide(boolean slide) {

        isSlide = slide;

    }

    public MyViewPager(Context context) {

        super(context);

    }



    public MyViewPager(Context context, AttributeSet attrs) {

        super(context, attrs);

    }

//    @Override
//
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//
//        switch (ev.getAction())
//        {
//            case MotionEvent.ACTION_DOWN:
//
//                if (D) Log.d(TAG, "onInterceptTouchEvent: ACTION_DOWN");
//
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//                if (D) Log.d(TAG, "onInterceptTouchEvent: ACTION_MOVE");
//                break;
//
//            case MotionEvent.ACTION_UP:
//                if (D) Log.d(TAG, "onInterceptTouchEvent: ACTION_UP");
//                break;
//        }
//
//
//        return isSlide;
//
//    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if(mOnTouchListener != null)
            mOnTouchListener.onTouch(ev.getAction());

        return super.dispatchTouchEvent(ev);
    }


    public void setMyOnTouchListener(OnTouchListener l) {
        mOnTouchListener = l;
    }

    OnTouchListener mOnTouchListener;

    interface OnTouchListener {
        void onTouch(int state);
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
    }

    @Override
    public void setOnScrollChangeListener(OnScrollChangeListener l) {

        super.setOnScrollChangeListener(l);
    }
}
