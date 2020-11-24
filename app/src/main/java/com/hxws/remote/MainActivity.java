package com.hxws.remote;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hxws.remote.Common.KeyboardUtil;
import com.hxws.remote.Utilities.Splash;
import com.hxws.remote.Utilities.NetworkUtils;
import com.hxws.remote.Wifi.BasicProtocol;
import com.hxws.remote.Wifi.DeviceActivity;
import com.hxws.remote.Wifi.DevicesDiscover;
import com.hxws.remote.Wifi.EmptyProtocol;
import com.hxws.remote.Wifi.KeyProtocol;
import com.hxws.remote.Wifi.MotionActivity;
import com.hxws.remote.Wifi.MotionProtocol;
import com.hxws.remote.Utilities.Conversion;
import com.hxws.remote.Utilities.CustomProgressDialog;
import com.hxws.remote.Wifi.PingAckProtocol;
import com.hxws.remote.Wifi.StringProtocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnTouchListener {

    private final static String TAG = "MainActivity";

    //motion Send相关定义


    private EditText mSend_Edit;
    private Button mSend_btn_send;
    private Button mSend_btn_ret;


    //控制系统输入法
    InputMethodManager mInputMethodManager;

    EditText mRealText_input;

    //View 2 鼠标输入界面
    Button mMotion_btn_click;

    RelativeLayout mRLTouch;

    private static final int MOTION_SEND_INTETVAL = 45;

    int saveX = 0;
    int saveY = 0;
    int downX;
    int downY;

    int moveX;
    int moveY;
    int upX;
    int upY;

    /***************************************/

    private DrawerLayout mDrawerLayout;

    //滑页效果 窗口定义
    View view1;
    View view2;
    View view3;

    //滑页标题
    private TabLayout mTabLayout;

    Context mContext;

    Activity mActivity;

    private boolean D = false;

    @SuppressLint("StaticFieldLeak")
    public static MainActivity sInstance;

    //popupMenu
    private View mPopupMenu;

    View SwitchMode;


    //界面模式
    public int BgModeFlag;
    public int IntBgModeFlag;
    public static final int BgNightMode = 1;
    public static final int BgDayMode = 2;
    View SwitchBg;

    //有无网络连接的标志
    public boolean isState_connected = false;

    //设备发现界面
    private static final int REQUEST_WIFI_CONNECT_DEVICE = 1;
    //触屏界面
    private static final int REQUEST_START_MOTION = 2;

    //back
    //定义首次按下返回键的时间变量，初始化
    long time = 0;

    //开机提示框
    private View checkbox;
    private CheckBox cBox;

    //getDPI

    public TextView mDpiTextView;

    //声明震动
    public Vibrator mVibrator;

    //锁
    Object mLock;

    //传递MAC地址和IP ,Ble Mac
    Bundle mBundle;

    //全局保存的MAC地址和IP地址
    public String storeMac = null;
    public String storeAddr = "";

    //第一次开机提示未连接过设备对话框

    public Dialog mFirstDialog;

    //网络重连提示界面
    public Dialog mNetLoadDialog;

    Splash mSplash;

    private boolean mReceiverTag = false;   //广播接受者标识

    //广播标识
    private static final String NETWORK_CONNECTIVITY_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE";

    //最后保存的IP
    private String mLastIp;


    //获取本机IP
    private ServerSocket mServerSocket;

    //TCP
    //地址端口

    public String Tcp_Svr_MAC = "";
    public String Tcp_Svr_IP = "";
    private static final int DEFAULT_CONTROL_PORT = 1853;
    private Socket mTcpSocket;
    private OutputStream mOutputStream;

    //心跳进程
    private SocketReadThread mReadThread;
    private SocketSendThread mSendThread;

    //TCP接收线程获取的长度和数据报
    int len;
    byte[] data;

    /*************************************************/

    //UDP
    DatagramSocket mUdpSocket;
    public String Udp_Broadcast_IP;
    private static final int DEFAULT_DISCOVER_PORT = 1852;

    //接收
    public String rcvMsg;
    public String macAddr = "";
    public boolean udpRcvOpen = false;

    //接收线程
    Thread rcvThread;
    Thread sendThread;

    //接收线程终止标志位
    public boolean mRcvFlag = false;

    /*************************************************/

    //各协议声明
    BasicProtocol basicProtocol;
    KeyProtocol keyProtocol;
    DevicesDiscover devicesDiscover;
    MotionProtocol motionProtocol;
    PingAckProtocol pingAckProtocol;
    EmptyProtocol emptyProtocol;
    StringProtocol stringProtocol;


    //Toast标志
    private static final int WIFI_DISCONN = 1;
    private static final int LOST_CONN = 2;
    private static final int SUCCESS_CONN = 3;
    private static final int IN_CONN = 4;
    private static final int HALFWAY_LOST_CONN = 5;
    private static final int CHOOSE_DEVICES = 6;
    private static final int TEST_TIPS = 7;

    //Handler
    //提示
    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            //开机提示没有设备
            if (msg.what == WIFI_DISCONN) {
                SwitchMode.setBackgroundResource(R.drawable.wifi_disconn);
                //mNetLoadDialog.show();
                isState_connected = false;

                //Toast.makeText(getApplicationContext(), R.string.discoverDevices, Toast.LENGTH_LONG).show();

//                new Handler().postDelayed(new Runnable(){
//                    public void run() {
//
//                        if (mNetLoadDialog.isShowing())
//                        {
//                            mNetLoadDialog.hide();
//                            mNetLoadDialog.dismiss();
//                        }
//
//                    }
//                }, 1000);

            }
            //已断开连接
            if (msg.what == LOST_CONN) {
                SwitchMode.setBackgroundResource(R.drawable.wifi_disconn);

                isState_connected = false;

                mNetLoadDialog.show();

                disconnDialog().dismiss();
                disconnDialog().hide();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mNetLoadDialog.isShowing())
                        {
                            mNetLoadDialog.hide();

                            if (!disconnDialog().isShowing()) {
                                //disconnDialog().show();
                                disconnTips();
                            }

                            Toast.makeText(getApplicationContext(), R.string.lost_connection, Toast.LENGTH_LONG).show();
                        }
                    }
                }, 2000);

            }
            //设备连接
            if (msg.what == SUCCESS_CONN) {
                if (D) Log.d(TAG, "handleMessage: SUCCESS_CONN");
                SwitchMode.setBackgroundResource(R.drawable.wifi);
                isState_connected = true;

                if (disconnDialog().isShowing())
                {
                    disconnDialog().hide();
                }

                //mNetLoadDialog.show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {

                        if (mNetLoadDialog.isShowing())
                        {
                            mNetLoadDialog.hide();
                        }
                        //Toast.makeText(getApplicationContext(), R.string.connected, Toast.LENGTH_LONG).show();

                    }
                }, 1000);

            }
            //设备处于连接状态
            if (msg.what == IN_CONN) {
                isState_connected = true;

                if (disconnDialog().isShowing())
                {
                    disconnDialog().hide();
                }

                if (mNetLoadDialog.isShowing())
                {
                    mNetLoadDialog.hide();
                }

                SwitchMode.setBackgroundResource(R.drawable.wifi);
                //Toast.makeText(getApplicationContext(), R.string.device_in_connection, Toast.LENGTH_LONG).show();
            }
            //中途断线
            if (msg.what == HALFWAY_LOST_CONN)
            {
                SwitchMode.setBackgroundResource(R.drawable.wifi_disconn);
                //Toast.makeText(getApplicationContext(), R.string.lost_connection, Toast.LENGTH_LONG).show();
            }
            //搜索设备
            if (msg.what == CHOOSE_DEVICES) {

                Toast.makeText(getApplicationContext(), R.string.discovingDevices, Toast.LENGTH_SHORT).show();
            }
            //切换蓝牙提示
            if (msg.what == TEST_TIPS) {

                Toast.makeText(getApplicationContext(), R.string.BLE_TEST_TIPS, Toast.LENGTH_LONG).show();
            }
        }
    };

    int tipsCount = 0;

    public void addMsgHandler(int wt) {

        tipsCount++;

        if (tipsCount == 1)
        {
            tipsCount = 0;
            Message msg = new Message();

            msg.what = wt;

            myHandler.sendMessage(msg);
        }

    }

    //侧边菜单
    @SuppressLint("RtlHardcoded")
    private void showDrawerLayout() {
        if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        } else {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.custom_vp_activity_main);

        if (D) Log.d(TAG, "------onCreate------");

        mContext = this;

        mActivity = this;

        //侧边栏 待用
        //mDrawerLayout = new DrawerLayout(getApplicationContext());

        //实例对象
        sInstance = this;

        mLock = new Object();

        mBundle = new Bundle();

        //背景
        BgModeFlag = BgDayMode;


        //协议定义
        wifiProtocolDef();

        //初始化各页面信息
        initViewPager();
        initView();

        //getMetric();

        checkWifiState();

        //启动局域网
        start();

        //注册广播
        registerBroadcasts();

    }//end onCreate

    @Override
    public void onResume() {
        super.onResume();
        if (D) Log.d(TAG, "onResume");

        mNetLoadDialog = CustomProgressDialog.createLoadingDialog(this, R.string.connecting);
        mNetLoadDialog.setCancelable(true);//允许返回

        mLastIp = NetworkUtils.getIPAddress(true);

        getBgModeFlag();

        //加载背景
        loadBg();

        SharedPreferences sp = getSharedPreferences("data", 0);

        //保存网络连接IP地址
        storeAddr = sp.getString("showTcpIp", "");

        if (sp.getString("showTcpIp", "").isEmpty()) {
                if (D) Log.d(TAG, "strAddr is null");

                if (!mTcpSocket.isConnected()) {
                    addMsgHandler(1);
                    //首次启动时显示提示对话框
                    startTip();
                }
        }
        else
        {
            if (!mTcpSocket.isConnected() && mTcpSocket != null) {
                reStart();
                TcpReconnect();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (D) Log.d(TAG, "onPause");

        //SharedPreferences sp = getSharedPreferences("data", 0);
        //SharedPreferences.Editor editor = sp.edit();

        if (storeAddr.equals("")) {
            //editor.putString("showTcpIp", storeAddr);

        }

        //editor.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (D) Log.d(TAG, "onDestroy");

        unregisterReceiver();

        stop();

        if (D) Log.i(TAG, "wait for threads");
        try {
            if (rcvThread != null && sendThread != null)
            {
                rcvThread.join();
                sendThread.join();
                if (D) Log.i(TAG, "sendThread,rcvThread over");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mSendThread != null && mReadThread != null)
        {
            try {
                mSendThread.join();
                mReadThread.join();

                if (D) Log.i(TAG, "mSendThread,mReadThread over");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (D) Log.i(TAG, "threads end.");

    }

    void wifiProtocolDef() {
        //wifi要传输的协议
        basicProtocol = new BasicProtocol();
        devicesDiscover = new DevicesDiscover();
        keyProtocol = new KeyProtocol();
        motionProtocol = new MotionProtocol();
        pingAckProtocol = new PingAckProtocol();
        emptyProtocol = new EmptyProtocol();
        stringProtocol = new StringProtocol();

        //心跳协议
        mSendThread = new SocketSendThread();
        mReadThread = new SocketReadThread();
    }

    //设置状态栏透明
    private void setStatus() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }


    @SuppressLint("InflateParams")
    public void initView() {

        setStatus();

        //侧滑菜单
        //mDrawerLayout = findViewById(R.id.dl_left);

//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setItemIconTintList(null);
//        navigationView.setNavigationItemSelectedListener(this);

        //一些控件的定义
        mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        //popupMenu
        mPopupMenu = findViewById(R.id.img_popup_menu);

        //Net连接加载对话框
        mNetLoadDialog = CustomProgressDialog.createLoadingDialog(this, R.string.connecting);
        mNetLoadDialog.setCancelable(true);//允许返回

        //侧滑菜单唤出
        //View MenuDrawer = findViewById(R.id.menu_drawer);

        View DevDiscover = findViewById(R.id.WifiDevDiscover);

        SwitchMode = findViewById(R.id.chooseMode);
        SwitchBg = findViewById(R.id.chooseBg);
//
//        //绑定监听器
        //MenuDrawer.setOnClickListener(mcon);
        DevDiscover.setOnClickListener(mcon);

        SwitchMode.setOnClickListener(mcon);
        SwitchBg.setOnClickListener(mcon);
        mPopupMenu.setOnClickListener(mcon);

    }

    //load motion 控件
    @SuppressLint("ClickableViewAccessibility")
    void loadViews()
    {
        //View 文本输入界面
        mSend_Edit = view3.findViewById(R.id.EdText_input);

        mSend_btn_send = view3.findViewById(R.id.EdText_send);

        mSend_btn_ret = view3.findViewById(R.id.EdText_ret);

        mRealText_input = view3.findViewById(R.id.realText_input);

        //View 2 鼠标输入界面
        mMotion_btn_click = view2.findViewById(R.id.motion_button_click);

        mRLTouch = view2.findViewById(R.id.fullP_touch);

        //监听触屏坐标
        mRLTouch.setOnTouchListener(this);

        mMotion_btn_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                double_click();
                mVibrator.vibrate(40);
            }
        });

//        btn_realTime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });
        new KeyboardUtil(view3, mContext, mSend_Edit, mSend_btn_send,mSend_btn_ret,mRealText_input).showKeyboard(view3);


    }

    void double_click() {
        if (D) Log.d(TAG, "enter double_click()");
        sendWifiMotion((byte) 0x01, 0, 0);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendWifiMotion((byte) 0x02, 0, 0);
        mVibrator.vibrate(50);
    }


    //activity shift
    @SuppressLint("ClickableViewAccessibility")
    private void initViewPager(){

        mTabLayout = (TabLayout) findViewById(R.id.tablayout);

        final MyViewPager viewPager = findViewById(R.id.viewPager);
        view1 = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        view2 = LayoutInflater.from(this).inflate(R.layout.activity_motion,null);
        view3 = LayoutInflater.from(this).inflate(R.layout.activity_text_transfer,null);

        final ArrayList<View> views = new ArrayList<View>();
        views.add(view1);
        views.add(view2);
        views.add(view3);

        String[] titles = this.getResources().getStringArray(R.array.tab_string);

        MyPagerAdapter adapter = new MyPagerAdapter(titles);

        adapter.setViews(views);

        viewPager.setAdapter(adapter);

        mTabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(0);

        //viewPager.setSlide(true);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }
            @Override
            public void onPageSelected(int i) {
            }
            @Override
            public void onPageScrollStateChanged(int i) {

                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive())
                {
//                    imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
//                    imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(view3.getWindowToken(), 0);
                }

                //Log.d(TAG, ": CurrentItem: "+viewPager.getCurrentItem());

            }
        });

        loadViews();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViewPagerNight(){
        MyViewPager viewPager = findViewById(R.id.viewPager_night);
        view1 = LayoutInflater.from(this).inflate(R.layout.activity_main_night, null);
        view2 = LayoutInflater.from(this).inflate(R.layout.activity_motion_night, null);
        view3 = LayoutInflater.from(this).inflate(R.layout.activity_text_transfer_night, null);

        ArrayList<View> views = new ArrayList<View>();
        views.add(view1);
        views.add(view2);
        views.add(view3);

        String[] titles = this.getResources().getStringArray(R.array.tab_string);

        MyPagerAdapter adapter = new MyPagerAdapter(titles);

        adapter.setViews(views);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        //viewPager.setSlide(true);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
//                    imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
//                    imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(view3.getWindowToken(), 0);
                }
            }
        });

        loadViews();
    }

    public long lastMotionSentTime = 0;
    //motion
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.getParent().requestDisallowInterceptTouchEvent(true);

                downX = (int) event.getX();
                downY = (int) event.getY();

                if (D) Log.e(TAG, "downX: " + downX + " " + "downY: " + downY);

                saveX = downX;
                saveY = downY;

                //sendWifiMotion((byte) 0x01, saveX, saveY);

//                int Dx = downX - last_downX;
//                int Dy = downY - last_downY;
//
//                if (D) Log.e(TAG, "Dx: " + Dx + " , Dy: " + Dy);

                break;
            case MotionEvent.ACTION_MOVE:
                v.getParent().requestDisallowInterceptTouchEvent(true);

                moveX = (int) event.getX();
                moveY = (int) event.getY();


                if ( (moveX > 0 && moveY > 0) && (moveX < 1000 && moveY < 750) )
                {
                    if (D) Log.e(TAG, "moveX: " + moveX + " " + "moveY: " + moveY);

                    if (System.currentTimeMillis() - lastMotionSentTime > MOTION_SEND_INTETVAL) {

                        int Mx = moveX - saveX;
                        int My = moveY - saveY;

                        if (Mx != 0 && My != 0) {
                            if (D) Log.e("mouse", "MOVE-->Mx = " + Mx + "; My=" + My);


                            sendWifiMotion((byte) 0x03, Mx, My);

//                        if (flag_move_down) {
//
//                            sendWifiMotion((byte) 0x01, saveX, saveY);
//                        }

                            lastMotionSentTime = System.currentTimeMillis();
                            saveX = moveX;
                            saveY = moveY;
                        }
                    }
                }


                break;

            case MotionEvent.ACTION_UP:
                v.getParent().requestDisallowInterceptTouchEvent(true);

                upX = (int) event.getX();
                upY = (int) event.getY();

                if (D) Log.e(TAG, "upX: " + upX + " " + "upY: " + upY);

                saveX = 0;
                saveY = 0;


                sendWifiMotion((byte) 0x02, saveX, saveY);

                if ( (downX == upX) && (downY == upY))
                {
                    double_click();
                }

                break;
            default:
                break;

        }
        return true;
    }
    public static int abs(int a) {
        return (a < 0) ? -a : a;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.nav_action_1:

                WifiDevicesList();

                break;

//            case R.id.nav_action_2:
//
//                loadBg();
//                break;
//            case R.id.nav_action_3:
//                //item.setIcon(R.drawable.ble);
//                break;

            default:
                break;
        }

        return true;
    }

    //popupMenu
    private void showPopupMenu(){
        PopupMenu popupMenu = new PopupMenu(this,mPopupMenu);
        popupMenu.inflate(R.menu.menu_popup);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.action_1:
                        //Toast.makeText(mContext,"Option 1",Toast.LENGTH_SHORT).show();
                        StartMotion();
                        return true;
//                    case R.id.action_2:
//                        Toast.makeText(mContext,"Option 2",Toast.LENGTH_SHORT).show();
//                        return true;
//                    case R.id.action_3:
//                        Toast.makeText(mContext,"Option 3",Toast.LENGTH_SHORT).show();
//                        return true;
                    default:
                        break;

                }

                return false;
            }
        });
        popupMenu.show();
    }


    public void saveBgModeFlag(int f)
    {
        SharedPreferences sp = getSharedPreferences("data", 0);
        SharedPreferences.Editor editor = sp.edit();

        IntBgModeFlag = f;
        editor.putInt("PrefBgModeFlag",IntBgModeFlag);
        if (D) Log.d(TAG, "save: IntBgModeFlag: "+IntBgModeFlag);


        editor.commit();
    }

    public void getBgModeFlag()
    {
        SharedPreferences sp = getSharedPreferences("data", 0);

        IntBgModeFlag = sp.getInt("PrefBgModeFlag",0);

        if (BgModeFlag != 0 && IntBgModeFlag != 0)
        {
            //获取背景标志
            BgModeFlag = IntBgModeFlag;

            if (D) Log.d(TAG, "onResume: BgModeFlag: "+ BgModeFlag);
        }

        //return BgModeFlag;
    }

    public void loadBg()
    {

        if (BgModeFlag == BgNightMode) {
            BgModeFlag = BgDayMode;

            saveBgModeFlag(BgNightMode);

            setContentView(R.layout.custom_vp_activity_main_night);
            initViewPagerNight();
            initView();

            SwitchBg.setBackgroundResource(R.drawable.mode_day);

            if (isState_connected) {
                SwitchMode.setBackgroundResource(R.drawable.wifi);
            } else {
                SwitchMode.setBackgroundResource(R.drawable.wifi_disconn);
            }



        } else if (BgModeFlag == BgDayMode){
            BgModeFlag = BgNightMode;

            saveBgModeFlag(BgDayMode);

            setContentView(R.layout.custom_vp_activity_main);

            initViewPager();
            initView();

            SwitchBg.setBackgroundResource(R.drawable.mode_night);

            if (isState_connected) {
                SwitchMode.setBackgroundResource(R.drawable.wifi);
            } else {
                SwitchMode.setBackgroundResource(R.drawable.wifi_disconn);
            }
        }

        //if (D) Log.d(TAG, "BgModeFlag: " + BgModeFlag);
    }

    View.OnClickListener mcon = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

//                case R.id.menu_drawer:
//
//                    showDrawerLayout();
//
//                    break;

                case R.id.img_popup_menu:

                    //showPopupMenu();

                    break;


                case R.id.WifiDevDiscover:

                    WifiDevicesList();
                    break;
                case R.id.chooseMode:

                    break;
                case R.id.chooseBg:

                    loadBg();
                    break;

                default:
                    break;
            }
        }
    };


    //get dpi

    void getMetric() {
        DisplayMetrics metric = getResources().getDisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  // 屏幕宽度（像素）
        int height = metric.heightPixels;  // 屏幕高度（像素）
        float density = metric.density;  // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）

        Log.e(TAG,"DPI: "+densityDpi);

        //getDPI
//        mDpiTextView = new TextView(this);
//        mDpiTextView = (TextView) findViewById(R.id.TextDpi);
//
//        if (densityDpi != 0) {
//            String textString = Integer.toString(densityDpi);
//            mDpiTextView.setText(textString);
//        }
    }

    @Override
    public void onBackPressed() {
        //2秒之内再次按下返回键将退出
        if (System.currentTimeMillis() - time <= 2 * 1000) {
            finish();
            super.onBackPressed();
        } else {
            //弹出提示
            Toast.makeText(this, R.string.press_exit_again, Toast.LENGTH_SHORT).show();
            //记录首次按下返回键的时间
            time = System.currentTimeMillis();
        }
    }

    private void startTip()
    {
        //提示框初始化
        //加载checkbox.xml 文件
        LayoutInflater lauoutInflater = LayoutInflater.from(this);
        checkbox = lauoutInflater.inflate(R.layout.checkbox_layout, null);

        //获取.xml文件的按钮
        cBox = checkbox.findViewById(R.id.check);

        cBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences pre = getSharedPreferences("data", 0);
                SharedPreferences.Editor editor = pre.edit();
                if (b)
                {
                    editor.putString("startischecked", "1");

                    if (D) Log.d(TAG, "startTip: putString(\"startischecked\", \"1\")");
                }
                else
                {
                    editor.putString("startischecked", "0");
                    if (D) Log.d(TAG, "startTip: putString(\"startischecked\", \"0\")");
                }
                editor.commit();
            }
        });

        //通过sharedPreferences来保存信息
        SharedPreferences pre = getSharedPreferences("data", 0);
        String value = pre.getString("startischecked", "");

        //判断接受到的信息
        if (value.endsWith("1")) {
            //如果选择的是1，则对话框就不再弹出
            startDialog().dismiss();
            if (D) Log.d(TAG, "startischecked: startDialog().dismiss()");
        } else {
            //如果没有选择，则对话框继续弹出

            if (!startDialog().isShowing())
            {

                startDialog().show();
            }

            if (D) Log.d(TAG, "startischecked: startDialog().show()");
        }
    }


    private AlertDialog startDialog() {
        //将数据保存到sharedPerferences中：
        //判断cBox是否被选中
        //提交选择的check,并且保存在pre中
        AlertDialog di = new AlertDialog.Builder(this)
                //.setTitle(R.string.UseTips)
                .setMessage(R.string.dialogMsg)
                .setView(checkbox)
                .setPositiveButton(R.string.GotIt, new DialogInterface.OnClickListener() {

                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        return di;
    }

    private void disconnTips()
    {
        //提示框初始化
        //加载checkbox.xml 文件
        LayoutInflater lauoutInflater = LayoutInflater.from(this);
        checkbox = lauoutInflater.inflate(R.layout.checkbox_layout, null);

        //获取.xml文件的按钮
        cBox = checkbox.findViewById(R.id.check);

        cBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences pre = getSharedPreferences("data", 0);
                SharedPreferences.Editor editor = pre.edit();
                if (b)
                {
                    editor.putString("disconnischecked", "1").apply();
                    if (D) Log.d(TAG, "disconnTip: putString(\"disconnischecked\", \"1\")");
                }
                else
                {
                    editor.putString("disconnischecked", "0").apply();
                    if (D) Log.d(TAG, "disconnTip: putString(\"disconnischecked\", \"0\")");
                }
                editor.commit();
            }
        });

        //通过sharedPreferences来保存信息
        SharedPreferences pre = getSharedPreferences("data", 0);
        String value = pre.getString("disconnischecked", "");

        //判断接受到的信息
        if (value.endsWith("1")) {
            //如果选择的是1，则对话框就不再弹出
            disconnDialog().dismiss();
            if (D) Log.d(TAG, "disconnischecked: disconnDialog().dismiss()");
        } else {
            //如果没有选择，则对话框继续弹出

            if (!disconnDialog().isShowing())
            {

                disconnDialog().show();
            }

            if (D) Log.d(TAG, "disconnischecked: disconnDialog().show()");
        }
    }

    private AlertDialog disconnDialog() {

        AlertDialog di = new AlertDialog.Builder(this)
                //.setTitle(R.string.DisconnTips)
                .setMessage(R.string.dialog_lost_connection)
                .setView(checkbox)
                .setPositiveButton(R.string.GotIt, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();
        return di;
    }

    //启动
    public boolean start() {

        try {
            if (D) Log.d(TAG, "About to create socket");

            mTcpSocket = new Socket();
            mUdpSocket = new DatagramSocket();

            if (D) Log.d(TAG, "created socket success");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean stop() {
        if (D) Log.e(TAG, "stop()");

        //关闭socket

        mRcvFlag = true;

        if (mUdpSocket != null) {
            mUdpSocket.close();
        }

        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }
            if (!mTcpSocket.isClosed()) {
                mTcpSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void reStart() {
        if (D) Log.e(TAG, "reStart");

        if (stop()) {
            start();
        }
    }

    public void TcpReconnect() {
        TcpConnect(storeAddr, DEFAULT_CONTROL_PORT);
        if (D) Log.e(TAG, "TcpReconnect");
    }

    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (D) Log.d(TAG, "onReceive:action: " + action);

            if (action.equals(NETWORK_CONNECTIVITY_CHANGED)
                    || action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                String newIp = NetworkUtils.getIPAddress(true);
                boolean isConnected = NetworkUtils.isNetworkConnected(context);

                if (D) Log.d(TAG, "onReceive: isConnected :"+isConnected);

                if (isConnected && !newIp.equals(mLastIp)) {
                    if (D) Log.e(TAG, "Network changed from ["
                            + mLastIp
                            + "] to ["
                            + newIp
                            + "], restart mobile client!");
                    mLastIp = newIp;

                    checkWifiState();
                    reStart();
                    TcpReconnect();
                }
                else if ( isWifiConnect() && newIp.equals(mLastIp))
                {
                    TcpReconnect();
                }

            }

        }
    };

    private void registerBroadcasts() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(NETWORK_CONNECTIVITY_CHANGED);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        //filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        getApplicationContext().registerReceiver(mNetworkReceiver, filter);
        if (D) Log.d(TAG, "registerBroadcasts");
    }

    //注销广播
    private void unregisterReceiver() {
        if (mReceiverTag) {
            mReceiverTag = false;   //Tag值 赋值为false
            this.unregisterReceiver(mNetworkReceiver);   //注销广播
        }
    }

    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }

    public void checkWifiState() {
        Intent intent = new Intent();
        if (!isWifiConnect()) {
            //Toast.makeText(this, R.string.keepWifiConn, Toast.LENGTH_SHORT).show();

//            intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//
//            startActivity(intent);
        }

        //获取局域网WIFI IP
        InetAddress BroadcastAddress = null;
        try {
            BroadcastAddress = getBroadcastAddress();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String strBCAddr = BroadcastAddress.toString();
        strBCAddr = strBCAddr.replace("/", "");

        //传给全局广播地址
        Udp_Broadcast_IP = strBCAddr;

        //获取到的广播地址 传给 UDP 发送时用
        //if (D) Log.d(TAG, "strBCAddr: " + Udp_Broadcast_IP);

        //获取本机地址
        //String localIP = NetworkUtils.getIPAddress(true);

    }


    public void WifiDevicesList() {
        addMsgHandler(CHOOSE_DEVICES);

        Intent serverIntent = new Intent(this, DeviceActivity.class);

        serverIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(serverIntent, REQUEST_WIFI_CONNECT_DEVICE);//启动配对界面
    }

    public void StartMotion() {
        Intent i = new Intent(getApplicationContext(), MotionActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(i, REQUEST_START_MOTION);
    }

    public void sendWifiChars(final byte charCount, final byte[] chars)
    {
        id++;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    stringProtocol.setPacketCmdString(id,charCount,chars);
                    mOutputStream = mTcpSocket.getOutputStream();

                    byte[] data = stringProtocol.genContentData();

                    if (D) Log.e(TAG, "stringProtocol Data: " + Arrays.toString(data));

                    if (D) Log.e(TAG, "stringProtocol Data: " +
                            NetworkUtils.bytesToHex(data, 8 + 1 + 1 + charCount*2));

                    mOutputStream.write(data);
                    mOutputStream.flush();

                } catch (IOException e2) {
                    e2.printStackTrace();
                    if (D) Log.e(TAG, "sendKey: IOException2");
                    reStart();
                    TcpReconnect();
                }
            }
        };
        thread.start();
//        try {
//            thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private int countSend;
    int id = 0;

    public void sendWifiKey(final byte action, final int code, final byte flag, final int keyState) {
        id++;
//        if (id > 9) {
//            id = 0;
//        }
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    keyProtocol.setCmdKeyEvent(id, action, code, flag, keyState);
                    mOutputStream = mTcpSocket.getOutputStream();

                    byte[] data = keyProtocol.genContentData();

                    mOutputStream.write(data);
                    mOutputStream.flush();

                    if (D) Log.e(TAG, "keyProtocol Data: " + NetworkUtils.bytesToHex(data, 14));
                    //mVibrator.vibrate(40);
                } catch (IOException e2) {
                    e2.printStackTrace();
                    if (D) Log.e(TAG, "sendKey: IOException2");
                    reStart();
                    TcpReconnect();
                }
            }
        };
        thread.start();
//        try {
//            thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public void sendWifiMotion(final byte action, final int x, final int y) {
        id++;
//        if (id > 9) {
//            id = 0;
//        }
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    motionProtocol.setPacketCmdMotionEvent(id, action, x, y);
                    mOutputStream = mTcpSocket.getOutputStream();

                    byte[] data = motionProtocol.genContentData();

                    mOutputStream.write(data);
                    mOutputStream.flush();

                    if (D) Log.e(TAG, "motionProtocol Data: " + NetworkUtils.bytesToHex(data, 13));

                } catch (IOException e2) {
                    e2.printStackTrace();
                    reStart();
                    TcpReconnect();
                    if (D) Log.e(TAG, "sendMotion:IOException2");
                }
            }
        };
        thread.start();
//        try {
//            thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    //获取WiFi广播地址
    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();

        if (dhcp == null) {
            if (D) Log.e(TAG, "dhcp == null");
        } else {
            //if (D) Log.d(TAG,"dhcp: "+ dhcp);
        }

        assert dhcp != null;
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) (broadcast >> (k * 8));

        return InetAddress.getByAddress(quads);
    }

    public boolean TcpConnect(final String strTcpSerIP, final int intTcpSerPort) {
        new Thread() {
            @Override
            public void run() {

                if (!mTcpSocket.isConnected()) {
                    try {
                        //连接目标地址
                        SocketAddress socketAddress = new InetSocketAddress(strTcpSerIP, intTcpSerPort);
                        if (D) Log.e(TAG, "socketAddress: " + socketAddress);

                        if (strTcpSerIP != null && intTcpSerPort != 0) {
                            mTcpSocket.connect(socketAddress, 1000);
                            mTcpSocket.setSoTimeout(6000);

                            addMsgHandler(SUCCESS_CONN);
                            if (D) Log.e(TAG, "Connect Success");


//                            if (!mSendThread.isAlive())
//                            {
                                mSendThread = new SocketSendThread();
                            //}
                            mSendThread.start();

//                            if (!mReadThread.isAlive())
//                            {
                                mReadThread = new SocketReadThread();
                            //}
                            mReadThread.start();

                        }

                    } catch (SocketTimeoutException ignored) {

                        addMsgHandler(LOST_CONN);

                        if (D) Log.e(TAG, "DisConnect: Connect timeout");

                    } catch (SocketException e) {
                        e.printStackTrace();
                        //addMsgHandler(LOST_CONN);
                        if (D) Log.e(TAG, "TcpConn: SocketException");

                    } catch (IOException e) {
                        e.printStackTrace();

                        if (D) Log.e(TAG, "TcpConn: IOException");
                    }

                } else {
                    if (D) Log.d(TAG, "already connect");
                    addMsgHandler(IN_CONN);
                }
            }
        }.start();

        return false;
    }

    private long lastThreadSentTime = 0;

    public class SocketSendThread extends Thread {
        private static final String TAG = "SocketSendThread";

        private volatile boolean mStopThread = false;

        void release() {
            mStopThread = true;
            //releaseLastSocket();
        }

        public void run() {
            while (!mStopThread) {
                try {

                    if (System.currentTimeMillis() - lastThreadSentTime > 3000) {
                        mOutputStream = mTcpSocket.getOutputStream();

                        byte[] emptyData = emptyProtocol.genContentData();

                        mOutputStream.write(emptyData);
                        mOutputStream.flush();
                        if (D) Log.e(TAG, "emptyProtocol Data: " + NetworkUtils.bytesToHex(emptyData, 8));
                        lastThreadSentTime = System.currentTimeMillis();
                    }

                } catch (IOException e) {
                    e.printStackTrace();

                    if (D) Log.e(TAG, "Server Is Close");

                    //addMsgHandler(HALFWAY_LOST_CONN);

                    mStopThread = true;
                }
            }
        }

    }

    public class SocketReadThread extends Thread {
        private static final String TAG = "SocketReadThread";
        private volatile boolean mStopThread = false;

        void release() {
            mStopThread = true;
            //releaseLastSocket();
        }

        @Override
        public void run() {
            DataInputStream mInputStream = null;
            try {
                mInputStream = new DataInputStream(mTcpSocket.getInputStream());
                while (!mStopThread) {
                    InputStream in = mTcpSocket.getInputStream();
                    data = new byte[12];

                    if (D) Log.d(TAG, "SocketThread read start!");
                    len = in.read(data);
                    if (D) Log.d(TAG, "SocketThread read finish!");

                    if (len > 0) {
                        //if (D) Log.e(TAG, "len: " + len + " , data: " + Arrays.toString(data));
                    } else {
                        if (D) Log.e(TAG, "mStopThread: len < 0");
                        mStopThread = true;
                    }

                }

            } catch (SocketTimeoutException e) {
                if (D) Log.e(TAG, "SocketReadThread:SocketTimeoutException");



                if (!mTcpSocket.isClosed()) {
                    try {
                        mTcpSocket.close();
                        mStopThread = true;
                        if (D) Log.e(TAG, "SocketReadThread:mTcpSocket.close()");

                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                if (D) Log.e(TAG, "SocketReadThread:IOException");
            } finally {
                if (D) Log.e(TAG, "SocketReadThread:finally");
                if (mInputStream != null) {
                    try {
                        mInputStream.close();
                        mInputStream = null;
                        addMsgHandler(HALFWAY_LOST_CONN);
                        if (D) Log.e(TAG, "mInputStream.close()");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void UdpDiscover() {
        udpRcvOpen = false;

        RcvThread();
        sendThread();
    }

    //发送搜索包
    public void sendThread() {
        //发送线程
        sendThread = new Thread(){
            @Override
            public void run() {
                try {
                    if (!udpRcvOpen && !rcvThread.isAlive()) {
                        udpRcvOpen = true;
                        rcvThread.start();
                        if (D) Log.e(TAG, "rcvThread.start()");
                    }

                    //根据广播地址发送给本网段所有地址
                    InetAddress serverAddress = InetAddress.getByName(Udp_Broadcast_IP);//Udp_Broadcast_IP
                    if (D) Log.d(TAG, "Udp_Ser_IP: " + Udp_Broadcast_IP);
                    byte[] data = devicesDiscover.genContentData();
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverAddress, DEFAULT_DISCOVER_PORT);

                    if (D) Log.e(TAG, "start sendData");
                    mUdpSocket.send(sendPacket);//把数据发送到服务端。
                    if (D) Log.e(TAG, "doing sendData");

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        sendThread.start();
    }

    private void sleepNoThrow(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exceptionSleep() {
        sleepNoThrow(50);
    }

    //接收设备信息
    public void RcvThread() {
        mRcvFlag = false;
        //接收线程
        rcvThread = new Thread() {
            @Override
            public void run() {
                while (!mRcvFlag) {
                    try {
                        byte[] data = new byte[50];
                        DatagramPacket rcvPacket = new DatagramPacket(data, data.length);

                        if (!mUdpSocket.isConnected() && mUdpSocket.isClosed()) {
                            continue;
                        }

                        if (D) Log.e(TAG, "start rcvData");
                        mUdpSocket.receive(rcvPacket);
                        if (D) Log.e(TAG, "doing rcvData");

                        //rcvMsg = new String(rcvPacket.getData(), rcvPacket.getOffset(), rcvPacket.getLength());
                        //if (D) Log.d(TAG,"rcvMsg: "+rcvMsg);

                        if (rcvPacket.getLength() > 0) {
                            byte[] s = rcvPacket.getData();
                            if (D) Log.e(TAG, "rcvData: " + Arrays.toString(s));
                            byte[] sbs = Conversion.subBytes(s, 8, 12);

                            if (sbs != null) {
                                //macAddr = NetworkUtils.bytesToHex(sbs,16);
                                char divisionChar = ':';
                                macAddr = new String(sbs);
                                macAddr = macAddr.replaceAll("(.{2})", "$1" + divisionChar).substring(0, 17);
                                if (D) Log.e(TAG, "macAddr: " + macAddr);

                                Tcp_Svr_MAC = macAddr;
                            }

                            //获取远程地址
                            InetAddress ip = rcvPacket.getAddress();

                            String strIP = ip.toString();
                            strIP = strIP.replace("/", "");
                            Tcp_Svr_IP = strIP;        //转给全局的IP 保存

                            //获取远程端口
                            int Port = rcvPacket.getPort();

                            //if (D) Log.d(TAG, "Tcp Port: " + Port);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        exceptionSleep();
                        continue;
                    }
                }
            }
        };
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        SharedPreferences sp = getSharedPreferences("data", 0);
        SharedPreferences.Editor editor = sp.edit();
        switch (resultCode) {

            case 1:

                if (D) Log.d(TAG, "case 1 start");
                if (data == null) {
                    return;
                }

                mBundle = data.getExtras();
                if (mBundle != null) {

                    storeMac = mBundle.getString("Mac");
                    if (D) Log.e(TAG, "strMac: " + new String(storeMac));
                    storeAddr = mBundle.getString("Addr");
                    isState_connected = true;

                    //重连
                    reStart();
                    TcpReconnect();
                }

                if (!storeAddr.equals("")) {

                    editor.putString("showTcpIp", storeAddr);
                    if (D) Log.e(TAG, "bundle:getTcpSerIP = " + storeAddr);

                    editor.commit();
                    //addMsgHandler(SUCCESS_CONN);

                }

                break;
            case 2:
                if (D) Log.d(TAG, "case 2");
                break;
            case 3:
                if (D) Log.d(TAG, "case 3");

                mBundle = data.getExtras();

                if (mBundle != null) {
                    //mMatchedAddr = mBundle.getString("MatchedAddr");

                    //if (D) Log.e(TAG, "mMatchedAddr: " + mMatchedAddr);
                }

                //editor.putString("MatchedAddr", mMatchedAddr);

                editor.commit();

                break;
            default:
                break;
        }
    }

}//end MainActivity





