package com.hxws.remote.Common;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hxws.remote.MainActivity;
import com.hxws.remote.R;
import com.hxws.remote.Wifi.MotionActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hxws.remote.R.string.discovingDevices;
import static java.nio.charset.StandardCharsets.UTF_16BE;

public class KeyboardUtil {

    private static final String TAG = "KeyboardUtil";

    private boolean D = false;

    private boolean isShift = false;

    InputMethodManager mInputMethodManager;

    private final static int MSG_REPEAT_KEY = 1000;

    private int FLAG_STATE_ALT = 1;
    private int FLAG_STATE_CTRL = 2;
    private int FLAG_STATE_SHIFT = 3;

    private Context ctx;
    private Activity act;
    private KeyboardView keyboardView;
    private Keyboard k1;// 字母键盘
    private Keyboard k2;// 数字键盘
    public boolean isnun = false;// 是否数字键盘
    public boolean isupper = false;// 是否大写

    private Button mBtn1;
    private Button mBtn2;

    private EditText mLineText;
    private EditText mRealText;


    @SuppressLint("ResourceAsColor")
    public KeyboardUtil(final View view, Context ctx, EditText editText1, Button btn1, Button btn2,EditText editText2) {
        //this.act = act;
        this.ctx = ctx;
        this.mLineText = editText1;
        this.mBtn1 = btn1;
        this.mBtn2 = btn2;

        this.mRealText = editText2;

        k1 = new Keyboard(ctx, R.xml.letter);
        k2 = new Keyboard(ctx, R.xml.symbols);
        keyboardView = view.findViewById(R.id.keyboard_view);
        keyboardView.setKeyboard(k1);

        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(true);
        keyboardView.setOnKeyboardActionListener(listener);

        //输入框监听
        mLineText.setOnFocusChangeListener(mcon);

        //刚启动显示的模式


        mInputMethodManager =
                (InputMethodManager) mLineText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        mBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //发送中文
                String mString = mLineText.getText().toString();

                byte[] b = {};

                //str_UTF_16 = new String(mString.getBytes(StandardCharsets.UTF_16), StandardCharsets.UTF_16);

                if (!mString.equals("")) {
                    byte str_UTF_16_count = (byte) mString.length();

                    if (D) Log.d(TAG, "str_UTF_16 count: " + str_UTF_16_count);

                    //byte [] x = {0x55, 0x4A};


                    //byte[] b = new byte[str_UTF_16_count*2];

                    //String a = new String(x, UTF_16BE);


//                    byte tmp = 0x00;
//
//                    tmp = x[x.length - 1];
//
//                    x[x.length - 1] = x[x.length - 2];
//
//                    x[x.length - 2] = tmp;
//
//                    if (D) Log.d(TAG, "change bit"+ Arrays.toString(x));

//                    if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
//                        b = a.getBytes();
//                    } else if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN){
//                        b = a.getBytes();
//                    }

                    //byte[] c = subBytes(b, 2, b.length - 2);

                    b = mString.getBytes(UTF_16BE);

                    if (D) Log.d(TAG, "onClick: b " + Arrays.toString(b) + ", b.size " + b.length);

                    //发送字符
                    MainActivity.sInstance.sendWifiChars(str_UTF_16_count, b);

                    showKeyboard(view);
                }

            }
        });


        mBtn2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
//                Editable editable2 = mLineText.getText();
//
//                int start = mLineText.getSelectionStart();
//
//                if (editable2 != null && editable2.length() > 0) {
//                    if (start > 0) {
//                        editable2.delete(start - 1, start);
//
//
//                        MainActivity.sInstance.sendWifiKey((byte) 0x01, 67, (byte) 0x00, 0);
//
//                        MainActivity.sInstance.sendWifiKey((byte) 0x02, 67, (byte) 0x00, 0);
//                    }
//                }

                showKeyboard(view);

                mLineText.clearFocus();
            }
        });

    }

    //Handler
    //提示
    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);

            if (msg.what == 1) {
                Toast.makeText(ctx, R.string.Swtich_Chinese_tips, Toast.LENGTH_LONG).show();

            }
            if (msg.what == 2)
            {
                Toast.makeText(ctx, R.string.Swtich_English_tips, Toast.LENGTH_LONG).show();
            }
        }
    };


    public void addMsgHandler(int wt) {

        Message msg = new Message();

        msg.what = wt;

        myHandler.sendMessage(msg);

    }

    View.OnFocusChangeListener mcon = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {

            if (b)
            {
                hideKeyboard();
                isShift = false;
                if (D) Log.d(TAG, "onFocusChange: hideKeyboard");
            }
            else
            {
                isShift = true;
                showKeyboard(view);
                if (D) Log.d(TAG, "onFocusChange: showKeyboard");
            }
        }
    };


    private KeyboardView.OnKeyboardActionListener listener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void swipeUp() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeDown() {
            //hideKeyboard();

        }

        @Override
        public void onText(CharSequence text) {
            Log.d(TAG, "onText: " + text);
        }

        @Override
        public void onRelease(int primaryCode) {
            if (D) Log.d(TAG, "onRelease: " + primaryCode);

            if (primaryCode == -2) {
                //MainActivity.sInstance.sendWifiKey((byte) 0x02, 0, (byte) 0x00, 0);
            } else if (primaryCode == -3) {

            } else if (primaryCode == 57422) {

            } else if (primaryCode == 57423) {

            } else if (primaryCode == 57424) {

            } else if (primaryCode == 57425) {

            } else {
                if (isupper) {
                    MainActivity.sInstance.sendWifiKey((byte) 0x02, primaryCode, (byte) 0x00, FLAG_STATE_SHIFT);

                } else {
                    MainActivity.sInstance.sendWifiKey((byte) 0x02, primaryCode, (byte) 0x00, 0);
                }
            }
        }

        private void sleepNoThrow(long ms) {
            try {
                Thread.sleep(ms);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendSleep() {
            sleepNoThrow(20);
        }

        @Override
        public void onPress(int primaryCode) {
            if (D) Log.d(TAG, "onPress: " + primaryCode);


            //过滤
            if (primaryCode == -2) {
                // MainActivity.sInstance.sendWifiKey((byte) 0x01, 0, (byte) 0x00, 0);
            } else if (primaryCode == -3) {

            } else if (primaryCode == 57422) {
                MainActivity.sInstance.sendWifiKey((byte) 0x01, 51, (byte) 0x00, 0);
                sendSleep();
                MainActivity.sInstance.sendWifiKey((byte) 0x01, 51, (byte) 0x00, 0);
                sendSleep();
                MainActivity.sInstance.sendWifiKey((byte) 0x01, 51, (byte) 0x00, 0);
                sendSleep();
                MainActivity.sInstance.sendWifiKey((byte) 0x01, 56, (byte) 0x00, 0);

            } else if (primaryCode == 57423) {
                MainActivity.sInstance.sendWifiKey((byte) 0x01, 56, (byte) 0x00, 0);
                sendSleep();
                MainActivity.sInstance.sendWifiKey((byte) 0x01, 31, (byte) 0x00, 0);
                sendSleep();
                MainActivity.sInstance.sendWifiKey((byte) 0x01, 43, (byte) 0x00, 0);
                sendSleep();
                MainActivity.sInstance.sendWifiKey((byte) 0x01, 41, (byte) 0x00, 0);
            } else if (primaryCode == 57424) {
                MainActivity.sInstance.sendWifiKey((byte) 0x01, 56, (byte) 0x00, 0);
                sendSleep();
                MainActivity.sInstance.sendWifiKey((byte) 0x01, 31, (byte) 0x00, 0);
                sendSleep();
                MainActivity.sInstance.sendWifiKey((byte) 0x01, 31, (byte) 0x00, 0);
            } else if (primaryCode == 57425) {
                //hideKeyboard();
            } else {
                if (isupper) {
                    MainActivity.sInstance.sendWifiKey((byte) 0x01, primaryCode, (byte) 0x00, FLAG_STATE_SHIFT);
                } else {
                    MainActivity.sInstance.sendWifiKey((byte) 0x01, primaryCode, (byte) 0x00, 0);
                }

            }

        }



        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            if (D) Log.d(TAG, "onKey: " + primaryCode);

            Editable editable1 = mRealText.getText();
            int start = mRealText.getSelectionStart();

            if (primaryCode == KeyEvent.KEYCODE_ENTER) {// 完成
                //hideKeyboard();
                editable1.clear();
            } else if (primaryCode == KeyEvent.KEYCODE_DEL) {//Keyboard.KEYCODE_DELETE) {// 回退
                if (editable1 != null && editable1.length() > 0) {
                    if (start > 0) {
                        editable1.delete(start - 1, start);
                    }
                }
            } else if (primaryCode == KeyEvent.KEYCODE_SHIFT_LEFT) {// 大小写切换

                changeKey();
                keyboardView.setKeyboard(k1);

            } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {// 数字键盘切换
                if (isnun) {
                    editable1.clear();
                    isnun = false;
                    keyboardView.setKeyboard(k1);
                } else {
                    editable1.clear();
                    isnun = true;
                    keyboardView.setKeyboard(k2);
                }

            } else if (primaryCode == -3) {
                //hideKeyboard();
                editable1.clear();
            } else if (primaryCode == 57425) {
                hideKeyboard();
            } else {
                String label = null;
                List<Keyboard.Key> keyList = keyboardView.getKeyboard().getKeys();
                for (Keyboard.Key key : keyList) {
                    for (int code : key.codes) {
                        if (code == primaryCode) {
                            //if (key.label != null && isword(key.label.toString())) {
                            if (key.label != null) {
                                label = key.label.toString();
                            }

                            //}
                        }
                    }
                }

                if (label == null) {
                    return;
                }

                if (isupper) {
                    label = label.toUpperCase();

                    if (label.equals("0")){
                        label = ")";
                    }
                    else if (label.equals("1")){
                        label = "!";
                    }
                    else if (label.equals("2")){
                        label = "@";
                    }
                    else if (label.equals("3")){
                        label = "#";
                    }
                    else if (label.equals("4")){
                        label = "$";
                    }
                    else if (label.equals("5")){
                        label = "%";
                    }
                    else if (label.equals("6")){
                        label = "^";
                    }
                    else if (label.equals("7")){
                        label = "&";
                    }
                    else if (label.equals("8")){
                        label = "*";
                    }
                    else if (label.equals("9")){
                        label = "(";
                    }
                    else if (label.equals(".")){
                        label = ">";
                    }
                    else if (label.equals(",")){
                        label = "<";
                    }

                }
                editable1.insert(start, label);
            }
        }
    };

    //映射

    /**
     * 键盘大小写切换
     */
    private void changeKey() {

        List<Keyboard.Key> keylist = k1.getKeys();
        if (isupper) {//大写切换小写
            //MainActivity.sInstance.sendWifiKey((byte) 0x01, 59, (byte) 0x00, 0);
            isupper = false;
            for (Keyboard.Key key : keylist) {
                if (key.label != null && isword(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    //key.codes[0] = key.codes[0] + 32;
                }
            }
        } else {//小写切换大写
            //MainActivity.sInstance.sendWifiKey((byte) 0x01, 59, (byte) 0x00, FLAG_STATE_SHIFT);
            isupper = true;
            for (Keyboard.Key key : keylist) {
                if (key.label != null && isword(key.label.toString())) {
                    key.label = key.label.toString().toUpperCase();
                    //key.codes[0] = key.codes[0] - 32;
                }
            }
        }
    }

    public void showKeyboard(View view) {
        int visibility = keyboardView.getVisibility();
        if (visibility == View.GONE || visibility == View.INVISIBLE) {

            MotionActivity.flag_show_keyboard = false;

            keyboardView.setVisibility(View.VISIBLE);

            //实时输入框
            mRealText.setVisibility(View.VISIBLE);

            mLineText.setVisibility(View.INVISIBLE);
            Editable EdiitLine = mLineText.getText();

            EdiitLine.clear();

            mBtn1.setVisibility(View.INVISIBLE);
            mBtn2.setVisibility(View.INVISIBLE);

            //系统输入法
            if (mInputMethodManager.isActive())
            {
                mInputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(),0);
            }

        }
    }

    public void hideKeyboard() {
        int visibility = keyboardView.getVisibility();
        if (visibility == View.VISIBLE) {
            MotionActivity.flag_show_keyboard = true;
            keyboardView.setVisibility(View.INVISIBLE);

            //实时输入框
            mRealText.setVisibility(View.INVISIBLE);

            mLineText.setVisibility(View.VISIBLE);
            mBtn1.setVisibility(View.VISIBLE);
            mBtn2.setVisibility(View.VISIBLE);

            mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

            mLineText.setFocusable(true);
            mLineText.requestFocus();

        }
    }

    private boolean isword(String str) {
        String wordstr = "abcdefghijklmnopqrstuvwxyz.,' '";
        if (wordstr.indexOf(str.toLowerCase()) > -1) {
            return true;
        }
        return false;
    }

}
