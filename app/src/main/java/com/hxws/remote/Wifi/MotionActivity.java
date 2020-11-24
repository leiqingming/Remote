package com.hxws.remote.Wifi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hxws.remote.Common.KeyboardUtil;
import com.hxws.remote.MainActivity;
import com.hxws.remote.MyPagerAdapter;
import com.hxws.remote.R;

import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.*;

public class MotionActivity extends AppCompatActivity implements View.OnTouchListener {

    private final static String TAG = "MotionActivity";

    private Context ctx;
    private Activity act;
    private EditText edt;
    private Button btn_send;


    StringProtocol mStringProtocol;
    String str_UTF_16;

    View mMotionTextView;
    View mMotionFullScreen;

    private boolean D = false;

    private final static int MSG_REPEAT_KEY = 1000;

    private static long KEY_SEND_INTERVAL = 100;

    private static long KEY_FIRST_DELAY = 500;

    private static long RESPONSE_AREA = 30;

    public boolean flag_continue;

    public boolean flag_move_down;

    //呼出键盘标志
    public static boolean flag_show_keyboard;

    private long lastSentTime;

    //双击事件
    private long clickCount = 0;
    private long firstClick = 0;
    private long secondClick = 0;

    private long clickCount_down = 0;
    private long firstClick_down = 0;
    private long secondClick_down = 0;

    private static final long totalTime = 300;

    int saveX = -10000;
    int saveY = -10000;
    int downX;
    int downY;

    int last_downX;
    int last_downY;

    int moveX;
    int moveY;
    int upX;
    int upY;
    int offsetX;
    int offsetY;

    private ImageButton ImgKeyboard;

    private Button btn_right_click;

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REPEAT_KEY:

                    removeMessages(MSG_REPEAT_KEY);

                    if (flag_continue) {
                        judgeSlideDown();

                        sendEmptyMessageDelayed(MSG_REPEAT_KEY, KEY_SEND_INTERVAL);
                    }

                    break;
                default:
                    break;
            }
            return;
        }
    };

    void judgeSlideDown() {
//        if (downY - moveY < 0)//下滑
//        {
//            MainActivity.sInstance.sendWifiKey((byte) 0x01, 20);
//        }
//        else//上
//        {
//            MainActivity.sInstance.sendWifiKey((byte) 0x01, 19);
//        }

//        if (downX - moveX < 0) //左
//        {
//            MainActivity.sInstance.sendWifiKey((byte) 0x01, 22);
//        }
//        else//右
//        {
//            MainActivity.sInstance.sendWifiKey((byte) 0x01, 21);
//        }
    }

    void judgeSlideUp() {
//        if (downY - moveY < 0)//下滑
//        {
//            MainActivity.sInstance.sendWifiKey((byte) 0x02, 20);
//        }
//        else
//        {
//            MainActivity.sInstance.sendWifiKey((byte) 0x02, 19);
//        }

//        if (downX - moveX < 0)
//        {
//            MainActivity.sInstance.sendWifiKey((byte) 0x02, 22);
//        }
//        else
//        {
//            MainActivity.sInstance.sendWifiKey((byte) 0x02, 21);
//        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion);
        ctx = this;
        act = this;

        mStringProtocol = new StringProtocol();

        flag_move_down = false;
        flag_show_keyboard = false;

        initView();
        loadBg();
    }

    //load bg
    public void loadBg() {
        if (MainActivity.sInstance.BgModeFlag == 1) {
//            mMotionFullScreen.setBackgroundResource(R.drawable.theme_day);
//            ImgKeyboard.setBackgroundResource(R.drawable.keyboard_pop);
            setContentView(R.layout.activity_motion);
            initView();
        } else {
            setContentView(R.layout.activity_motion_night);
            initView();
//            mMotionFullScreen.setBackgroundResource(R.drawable.theme_night);
//            ImgKeyboard.setBackgroundResource(R.drawable.keyboard_pop_night);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    public void initView() {
        //编辑框
        edt = findViewById(R.id.EdText_input);
        btn_send = findViewById(R.id.EdText_send);

        mMotionFullScreen = findViewById(R.id.MotionFullScreen);

        //ImageView imageRet = (ImageView) findViewById(R.id.EdText_ret);

        Button btn_click = (Button) findViewById(R.id.EdText_ret);


        //按钮
        //ImgKeyboard = findViewById(R.id.keyboard);

        initKeyboard();

//        imageRet.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (D) Log.d(TAG, "finish");
//                finish();
//            }
//        });

        btn_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                double_click();
                MainActivity.sInstance.mVibrator.vibrate(40);
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //发送中文
                String mString = edt.getText().toString();

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

                    if (D) Log.d(TAG, "onClick: " + b.length);
                    if (D) Log.d(TAG, "onClick: c " + Arrays.toString(b) + "c.size " + b.length);

                    //发送字符
                    MainActivity.sInstance.sendWifiChars(str_UTF_16_count, b);
                }
            }
        });

        //获取触屏坐标
        RelativeLayout RLTouch = findViewById(R.id.fullP_touch);

        //监听触屏坐标
        RLTouch.setOnTouchListener(this);
    }

    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];

        System.arraycopy(src, begin, bs, 0, count);

        return bs;
    }

    void initKeyboard() {
        //先显示
        //new KeyboardUtil(View, ctx, edt, btn_send).showKeyboard();
//        ImgKeyboard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //唤出键盘
//                if (flag_show_keyboard) {
//                    new KeyboardUtil(act, ctx, edt, btn_send).showKeyboard();
//                } else {
//                    new KeyboardUtil(act, ctx, edt, btn_send).hideKeyboard();
//                }
//            }
//        });
    }

    void double_click() {
        if (D) Log.d(TAG, "enter double_click()");
        MainActivity.sInstance.sendWifiMotion((byte) 0x01, 0, 0);
        MainActivity.sInstance.sendWifiMotion((byte) 0x02, 0, 0);
    }

    void btn_ret()
    {
        if (D) Log.d(TAG, "enter btn_ret()");

        MainActivity.sInstance.sendWifiKey((byte) 0x01, 4, (byte) 0x00, 0);
        MainActivity.sInstance.sendWifiKey((byte) 0x02, 4, (byte) 0x00, 0);
    }

    int Count_X;
    int Count_Y;

    int Last_CountX;
    int Last_CountY;

    int All_Of_CountX;
    int All_Of_CountY;

    int lastAllOfCountX;
    int lastAllOfCountY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();

                if (D) Log.e(TAG, "downX: " + downX + " " + "downY: " + downY);

                lastAllOfCountX = 0;
                lastAllOfCountY = 0;

                saveX = downX;
                saveY = downY;

                int Dx = downX - last_downX;
                int Dy = downY - last_downY;

                if (D) Log.e(TAG, "Dx: " + Dx + " , Dy: " + Dy);

                if ((abs(Dx) < RESPONSE_AREA) && abs(Dy) < RESPONSE_AREA) {
                    clickCount++;
                    clickCount_down++;
                }
//                else{
//                    clickCount = 0;
//                    clickCount_down = 0;
//                }

                last_downX = downX;
                last_downY = downY;


//                if (1 == clickCount_down) {
//                    firstClick_down = System.currentTimeMillis();
//                } else if (2 == clickCount_down) {
//                    secondClick_down = System.currentTimeMillis();
//                    if (secondClick_down - firstClick_down < totalTime) {   //超过一定时间相应双击事件
//                        flag_move_down = true;
//                        flag_continue = true;
//                        mHandler.sendEmptyMessageDelayed(MSG_REPEAT_KEY, KEY_FIRST_DELAY);
//                        clickCount_down = 0;
//                        firstClick_down = 0;
//                    } else {
//                        firstClick_down = secondClick_down;
//                        clickCount_down = 1;
//                    }
//                    secondClick_down = 0;
//                } else {
//                    clickCount_down = 1;
//                }
//                if (D) Log.e(TAG, "clickCount_down: " + clickCount_down);
                break;
            case MotionEvent.ACTION_MOVE:

                moveX = (int) event.getX();
                moveY = (int) event.getY();
                if (D) Log.e(TAG, "moveX: " + moveX + " " + "moveY: " + moveY);

                if (System.currentTimeMillis() - lastSentTime > 8) {
                    int Mx = moveX - saveX;
                    int My = moveY - saveY;
                    if (Mx != 0 && My != 0) {
                        if (D) Log.e("mouse", "MOVE-->Mx = " + Mx + "; My=" + My);
//                        All_Of_CountX = All_Of_CountX + Mx;
//                        All_Of_CountY = All_Of_CountY + My;
                        MainActivity.sInstance.sendWifiMotion((byte) 0x03, Mx, My);

                        if (flag_move_down) {
                            MainActivity.sInstance.sendWifiMotion((byte) 0x01, saveX, saveY);
                        }

                        lastSentTime = System.currentTimeMillis();
                        saveX = moveX;
                        saveY = moveY;
                    }
                }

                break;
            case MotionEvent.ACTION_UP:

                upX = (int) event.getX();
                upY = (int) event.getY();

                saveX = -10000;
                saveY = -10000;

                int mX = abs(upX - downX);
                int mY = abs(upY - downY);
                offsetX = upX - downX;
                offsetY = upY - downY;

                if (flag_move_down) {
                    flag_move_down = false;
                    MainActivity.sInstance.sendWifiMotion((byte) 0x02, saveX, saveY);
                }

                if (1 == clickCount) {
                    firstClick = System.currentTimeMillis();
                } else if (2 == clickCount) {
                    secondClick = System.currentTimeMillis();
                    if (secondClick - firstClick < totalTime) {
                        //双击事件
                        //double_click();
                        clickCount = 0;
                        firstClick = 0;
                    } else {
                        firstClick = secondClick;
                        clickCount = 1;
                    }
                    secondClick = 0;
                } else {
                    clickCount = 1;
                }
                if (D) Log.e(TAG, "clickCount: " + clickCount);

                lastSentTime = 0;

//                Log.d(TAG, "onTouch: All_Of_CountX: "+All_Of_CountX);
//                Log.d(TAG, "onTouch: All_Of_CountY: "+All_Of_CountY);

//                All_Of_CountX = 0;
//                All_Of_CountY = 0;

//                Log.d(TAG, "onTouch: lastAllOfCountX: "+lastAllOfCountX);
//                Log.d(TAG, "onTouch: lastAllOfCountY: "+lastAllOfCountY);

                //Log.e(TAG,"offsetX: "+ offsetX +" "+"offsetY: "+offsetY);

                //Log.d(TAG, "onTouch: downX: "+downX+" , downY: "+downY);
//                Log.d(TAG, "onTouch: upX: "+upX+" , upY: "+upY);
//
//                Log.d(TAG, "onTouch: offsetX: "+ (offsetX));
//                Log.d(TAG, "onTouch: offsetY: "+ (offsetY));
                if (flag_continue) {
                    judgeSlideUp();
                }
                mHandler.removeMessages(MSG_REPEAT_KEY);
                flag_continue = false;
                break;
            default:
                break;

        }
        return true;
    }




    public static int abs(int a) {
        return (a < 0) ? -a : a;
    }


}
