package com.hxws.remote.Common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.hxws.remote.MainActivity;
import com.hxws.remote.R;


public class RCButton extends AppCompatButton implements View.OnTouchListener {

    private final static String TAG = "RCButton";

    private boolean D = true;

    private Context mContext;

    private int mCurrentWifiKeyCode;

    private byte mCurrentWifiAction;

    //按键动作
    /********************** KeyEvent **********************/

    private static final byte KEY_ACTION_DOWN = 0x01;
    private static final byte KEY_ACTION_UP = 0x02;

    /********************************************************/


    private int mWifiKeyCode;

    private final static int MSG_REPEAT_KEY = 1000;

    private static long KEY_SEND_INTERVAL = 100;

    private static long KEY_FIRST_DELAY = 300;

    private long lastSentTime = 0;

    public RCButton(Context context) {
        super(context);
        mContext = context;
    }

    public RCButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.buttonStyle);
    }

    public RCButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        if (attrs != null) {
            @SuppressLint("CustomViewStyleable") TypedArray a = getContext()
                    .obtainStyledAttributes(attrs,
                            R.styleable.WifiRCButton,
                            0, 0);

            mWifiKeyCode = a.getInteger(R.styleable.WifiRCButton_WifiBtnkeyCode, 0);

            @SuppressLint({"CustomViewStyleable", "Recycle"}) TypedArray b = getContext()
                    .obtainStyledAttributes(attrs,
                            R.styleable.BleRCButton,
                            0, 0);

            a.recycle();

            setOnTouchListener(this);
        }
    }

    boolean flag_continue;

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REPEAT_KEY:
                    removeMessages(MSG_REPEAT_KEY);

                    if (flag_continue) {

                        sendWifiKeyCodes(mCurrentWifiKeyCode, mCurrentWifiAction, (byte) 0x00, 0);

                        sendEmptyMessageDelayed(MSG_REPEAT_KEY, KEY_SEND_INTERVAL);
                    }

                    break;
                case -1:
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        //if (D) Log.i(TAG,"onTouchAction:"+event.getAction());

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            if (D) Log.d(TAG, "onTouch: ACTION_DOWN");

            sendWifiKeyCodes(mWifiKeyCode, KEY_ACTION_DOWN, (byte) 0x00, 0);

            flag_continue = true;
            MainActivity.sInstance.mVibrator.vibrate(30);
            mHandler.sendEmptyMessageDelayed(MSG_REPEAT_KEY, KEY_FIRST_DELAY);

        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            if (D) Log.d(TAG, "onTouch: ACTION_MOVE");
            //flag_continue = false;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            if (D) Log.d(TAG, "onTouch: ACTION_UP");

            sendWifiKeyCodes(mWifiKeyCode, KEY_ACTION_UP, (byte) 0x00, 0);

            flag_continue = false;

            mHandler.removeMessages(MSG_REPEAT_KEY);
            MainActivity.sInstance.mVibrator.cancel();
        }

        return false;
    }


    public void sendWifiKeyCodes(int intCode, byte act, byte flg, int state) {

        mCurrentWifiKeyCode = intCode;

        mCurrentWifiAction = act;

        MainActivity.sInstance.sendWifiKey(mCurrentWifiAction, mCurrentWifiKeyCode, flg, state);

        if (D) Log.e(TAG, "mBleKeyCode , action: " + intCode + " , " + act);

    }


}
