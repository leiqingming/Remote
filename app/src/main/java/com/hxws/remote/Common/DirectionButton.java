package com.hxws.remote.Common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.hxws.remote.MainActivity;
import com.hxws.remote.R;

public class DirectionButton extends RCButton {

    private final static String TAG = "DirectionButton";

    public boolean D = false;

    //close adv flag
    public static boolean flag_close_adv;

    private final static int MSG_REPEAT_KEY = 1000;

    private static long KEY_SEND_INTERVAL = 80;

    private static long KEY_FIRST_DELAY = 400;

    private final static long DEBOUNCE_INTERVAL = 300;

    private final static long FIRST_INTERVAL = 300;
    private final static long SECOND_INTERVAL = 100;

    private String[] mWifiKeyCodes;
    private String[] mBleKeyCodes;

    int keyId;

    private int mWidth = 0;
    private int mHeight = 0;


    private long lastSentTime = 0;

    //按键动作
    /********************** KeyEvent **********************/

    private static final byte KEY_ACTION_DOWN = 0x01;
    private static final byte KEY_ACTION_UP = 0x02;

    /********************************************************/

    private static final int KEY_ID_OK = 0;
    private static final int KEY_ID_LEFT = 1;
    private static final int KEY_ID_RIGHT = 2;
    private static final int KEY_ID_UP = 3;
    private static final int KEY_ID_DOWN = 4;

    private String mCurrentWifiKeyCode;
    private String mCurrentBleKeyCode;

    private byte mCurrentWifiAction;


    public DirectionButton(Context context) {
        super(context);

    }

    public DirectionButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.buttonStyle);
    }

    public DirectionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        if (attrs != null) {
            //WIFI
            @SuppressLint("CustomViewStyleable") TypedArray a = getContext()
                    .obtainStyledAttributes(attrs,
                            R.styleable.WifiDirectionButton,
                            0, 0);

            String strWifiCodes = a.getString(R.styleable.WifiDirectionButton_WifiDctBtnKeyCodes);

            assert strWifiCodes != null;
            mWifiKeyCodes = strWifiCodes.split(",");

            //BLE
            @SuppressLint({"CustomViewStyleable", "Recycle"}) TypedArray b = getContext()
                    .obtainStyledAttributes(attrs,
                            R.styleable.BleDirectionButton,
                            0, 0);

            String strBleCodes = b.getString(R.styleable.BleDirectionButton_BleDctBtnKeyCodes);
            assert strBleCodes != null;
            mBleKeyCodes = strBleCodes.split(",");

            a.recycle();

            setOnTouchListener(this);
        }
    }

    public int convertX(int x) {
        if (mWidth == 0) {
            mWidth = getWidth();
        }

        //return x - (mWidth / 2);
        return x - (mWidth / 2);
    }

    public int convertY(int y) {
        if (mHeight == 0) {
            mHeight = getHeight();
        }

        return y - (mHeight / 2);
    }

    int count_key_down = 0;

    boolean flag_continue;

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REPEAT_KEY:

                    removeMessages(MSG_REPEAT_KEY);

                    if (flag_continue) {

                        sendWifiKeyCodes(keyId, mCurrentWifiAction, (byte) 0x00, 0);

                        sendEmptyMessageDelayed(MSG_REPEAT_KEY, KEY_SEND_INTERVAL);
                    }

                    break;
                default:
                    break;
            }
            return;
        }
    };


    void delayTime(int delay_ms) {
        try {
            Thread.sleep(delay_ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //if (D) Log.i(TAG,"onTouchAction:" + event.getAction());

        //Log.i(TAG,"X:"+event.getX());
        //Log.i(TAG,"Y:"+event.getY());

        judgeDirectionKey(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            if (System.currentTimeMillis() - lastSentTime < 150) {
                flag_close_adv = false;
            } else {
                flag_close_adv = true;
            }
            if (D) Log.e(TAG, "onTouch: quickClick");

            sendWifiKeyCodes(keyId, KEY_ACTION_DOWN, (byte) 0x00, 0);

            flag_continue = true;
            MainActivity.sInstance.mVibrator.vibrate(30);
            mHandler.sendEmptyMessageDelayed(MSG_REPEAT_KEY, KEY_FIRST_DELAY);

        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            //judgeDirectionKey(event);
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            v.getParent().requestDisallowInterceptTouchEvent(true);

            //judgeDirectionKey(event);


            sendWifiKeyCodes(keyId, KEY_ACTION_UP, (byte) 0x00, 0);


            //lastSentTime = 0;
            lastSentTime = System.currentTimeMillis();

            //count_key_down = 0;

            mHandler.removeMessages(MSG_REPEAT_KEY);
            flag_continue = false;

            if (MainActivity.sInstance.BgModeFlag == 2) {
                setBackgroundResource(R.drawable.circle_ok_night);
            } else if (MainActivity.sInstance.BgModeFlag == 1) {
                setBackgroundResource(R.drawable.circle_ok);
            }

            MainActivity.sInstance.mVibrator.cancel();

        }
        return false;
    }

    //WIFI Send

    public void sendWifiKeyCodes(int keyId, byte action, byte flag, int state) {

        try {
            sendWifiKeyCode(mWifiKeyCodes[keyId], action, flag, state);
        } catch (Exception e1) {
            if (D) Log.e(TAG, "onTouch: sendWifiKeyCode Exception");
        }
    }

    public void sendWifiKeyCode(String strCode, byte act, byte flg, int keyState) {

        mCurrentWifiKeyCode = strCode;

        mCurrentWifiAction = act;

        int intCurrentCode = Integer.parseInt(mCurrentWifiKeyCode);

        //if (System.currentTimeMillis() - lastSentTime > DEBOUNCE_INTERVAL) {

        MainActivity.sInstance.sendWifiKey(mCurrentWifiAction, intCurrentCode, flg, keyState);
        //if (D) Log.d(TAG, "---DEBOUNCE_INTERVAL---");
        if (D)
            Log.d(TAG, "mWifiKeyCode , action: " + strCode + " , " + act + " flag: " + flg + " keyState: " + keyState);
        //lastSentTime = System.currentTimeMillis();
        //}

    }

    private boolean checkLargeInside(int x, int y) {
        int CR = mWidth / 2;

        int midX = mWidth / 2;
        int midY = mHeight / 2;

        int dx = abs(x - midX);
        //if (D) Log.i(TAG, "dx = " + dx);
        if (dx > CR) {
            return false;
        }

        int dy = abs(y - midY);
        //if (D) Log.i(TAG, "dy = " + dy);
        if (dy > CR) {
            return false;
        }

        //if ( dx + dy < CR){return true;}

        return (dx * dx + dy * dy < CR * CR);
    }

    private boolean checkSmallInside(int x, int y) {
        int CR = mWidth * 80 / 300 / 2;
        //int Rx = mWidth *84 / 174 / 2;
        //int Ry = mHeight *84 / 174 / 2;
        int midX = mWidth / 2;
        int midY = mHeight / 2;

        int dx = abs(x - midX);
        //if (D) Log.i(TAG, "dx = " + dx);
        //if ( dx > CR){return false;}

        int dy = abs(y - midY);
        //if (D) Log.i(TAG, "dy = " + dy);
        if (dy > CR) {
            return false;
        }

        //if ( dx + dy < CR){return true;}

        return (dx * dx + dy * dy < CR * CR);

    }

    public void judgeDirectionKey(MotionEvent event) {

        //OK R size
        //int Rw = mWidth / 2 ;
        //int Rh = mHeight / 2 ;

        int Dx = (int) event.getX();
        int Dy = (int) event.getY();

        int x = convertX((int) event.getX());
        int y = convertY((int) event.getY());

        //方向按键区域判断
        if (checkLargeInside(Dx, Dy)) {
            if (checkSmallInside(Dx, Dy)) {
                //OK
                if (MainActivity.sInstance.BgModeFlag == 2) {
                    setBackgroundResource(R.drawable.circle_ok_click_night);
                } else if (MainActivity.sInstance.BgModeFlag == 1) {
                    setBackgroundResource(R.drawable.circle_ok_click);
                }

                keyId = KEY_ID_OK;

                //Log.i(TAG,"ok");
            } else {

                //判断 右按键
                if (((x > 0) && (y > 0)) || ((x > 0) && (y < 0))) {
                    if (abs(x) > abs(y)) {
                        if (MainActivity.sInstance.BgModeFlag == 2) {
                            setBackgroundResource(R.drawable.circle_right_click_night);
                        } else if (MainActivity.sInstance.BgModeFlag == 1) {
                            setBackgroundResource(R.drawable.circle_right_click);
                        }

                        keyId = KEY_ID_RIGHT;

                        //Log.i(TAG,"right");
                    }
                }

                //判断 左按键
                if (((x < 0) && (y < 0)) || ((x < 0) && (y > 0))) {
                    if (abs(x) > abs(y)) {
                        if (MainActivity.sInstance.BgModeFlag == 2) {
                            setBackgroundResource(R.drawable.circle_left_click_night);
                        } else if (MainActivity.sInstance.BgModeFlag == 1) {
                            setBackgroundResource(R.drawable.circle_left_click);
                        }

                        keyId = KEY_ID_LEFT;

                        //Log.i(TAG,"left");
                    }
                }

                //判断 下按键
                if (((x > 0) && (y > 0)) || ((x < 0) && (y > 0))) {
                    if ((abs(y) > abs(x))) {
                        if (MainActivity.sInstance.BgModeFlag == 2) {
                            setBackgroundResource(R.drawable.circle_down_click_night);
                        } else if (MainActivity.sInstance.BgModeFlag == 1) {
                            setBackgroundResource(R.drawable.circle_down_click);
                        }

                        keyId = KEY_ID_DOWN;

                        //Log.i(TAG,"Down");
                    }
                }

                //判断 上按键
                if (((y < 0) && (x > 0)) || ((y < 0) && (x < 0))) {
                    if (abs(y) > abs(x)) {
                        if (MainActivity.sInstance.BgModeFlag == 2) {
                            setBackgroundResource(R.drawable.circle_up_click_night);
                        } else if (MainActivity.sInstance.BgModeFlag == 1) {
                            setBackgroundResource(R.drawable.circle_up_click);
                        }

                        keyId = KEY_ID_UP;

                        //Log.i(TAG,"up");
                    }
                }
            }
        } else {
            keyId = 5;
        }
    }

    public static int abs(int a) {
        return (a < 0) ? -a : a;
    }


}

