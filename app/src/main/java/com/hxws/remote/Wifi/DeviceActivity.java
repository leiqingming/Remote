package com.hxws.remote.Wifi;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hxws.remote.MainActivity;
import com.hxws.remote.R;

import java.util.ArrayList;


public class DeviceActivity extends AppCompatActivity {
    // Debug
    private static final String TAG = "DeviceSearchActivity";
    private boolean D = false;

    //背景资源
    View search_fullScreen;
    View search_title;


    private static final int NO_HAVE_DEVICES = 1;

    @SuppressLint("StaticFieldLeak")
    public static DeviceActivity sInstance;

    Bundle bundle;

    //扫描标志位
    private boolean isopen = true;

    //View
    private Button mbtnReturn;
    private Button mbtnRefresh;
    private ProgressBar mprgBar;

    private ArrayList<Devices> mData;
    private Context mContext;
    private DevicesAdapter mAdapter = null;
    private ListView list_devices;

    //Handler
    //提示
    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);

            if (msg.what == NO_HAVE_DEVICES) {
                Toast.makeText(getApplicationContext(), R.string.keepSameWifi, Toast.LENGTH_SHORT).show();
            }
        }

    };

    public void addMsgHandler(int wt) {
        Message msg = new Message();

        msg.what = wt;

        myHandler.sendMessage(msg);
    }

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicesearch);

        bundle = new Bundle();

        //sInstance = this;

        mContext = this;

        mData = new ArrayList<Devices>();
        mAdapter = new DevicesAdapter(mData, mContext);

        initView();

        start();

    }

    @Override
    public void onBackPressed() {

        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (D) Log.d(TAG, "onDestroy");

        isopen = false;
        sndTrdStop = true;

        mAdapter.clear();
    }

    public void initView() {
        // refresh button initial
        mbtnRefresh = (Button) findViewById(R.id.refresh);

        // process bar initial
        mprgBar = (ProgressBar) findViewById(R.id.progress_bar);

        // return button initial
        mbtnReturn = (Button) findViewById(R.id.back);

        //bg res
        search_fullScreen = (LinearLayout) findViewById(R.id.devices_search_fullScreen);
        search_title = (RelativeLayout) findViewById(R.id.devices_search_title);

        if (MainActivity.sInstance.BgModeFlag == 2)
        {
            search_title.setBackgroundResource(R.color.colorTitle_night);
        }
        else
        {
            search_title.setBackgroundResource(R.color.colorTitle);
        }

        // device list initial
        list_devices = (ListView) findViewById(R.id.lvDevices);
        list_devices.setOnItemClickListener(new DeviceActivity.ItemClickEvent());

        //筛选 可不要
        list_devices.setTextFilterEnabled(true);

        //back
        mbtnReturn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (D) Log.d(TAG, "return to the main activity");

                Intent intent = new Intent();
                setResult(2, intent);
                finish();
                //BackWifiInterface();
            }
        });

        //refresh
        mbtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (D) Log.d(TAG, "refresh the data list");
                start();
            }
        });

//        if (MainActivity.sInstance.BgModeFlag == 2) {
//            search_fullScreen.setBackgroundResource(R.drawable.theme_day);
//            search_title.setBackgroundResource(R.color.colorTitle);
//        } else {
//            search_fullScreen.setBackgroundResource(R.drawable.theme_night);
//            search_title.setBackgroundResource(R.color.colorTitle_night);
//
//        }


    }

    private boolean sndTrdStop = false;
    private long lastSendTick = 0;

    public void start() {
        mprgBar.setVisibility(View.VISIBLE);
        mbtnRefresh.setVisibility(View.GONE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mprgBar.setVisibility(View.GONE);
                mbtnRefresh.setVisibility(View.VISIBLE);

                if (MainActivity.sInstance.macAddr.equals("")
                        && MainActivity.sInstance.Tcp_Svr_IP.equals("")) {
                    Toast.makeText(mContext, R.string.keepSameWifi, Toast.LENGTH_LONG).show();
                }
            }
        }, 12000);//.秒后执行Runnable中的run方法

        // 清一下界面
        mAdapter.clear();

        Thread thread = new Thread() {
            public void run() {
                MainActivity.sInstance.UdpDiscover();
                while (!sndTrdStop) {
                    if (System.currentTimeMillis() - lastSendTick > 3000) {
                        MainActivity.sInstance.UdpDiscover();
                        lastSendTick = System.currentTimeMillis();
                    }
                }
            }
        };
        thread.start();

        autoFlash();

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void autoFlash() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isopen) {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!MainActivity.sInstance.Tcp_Svr_MAC.equals("")
                            && !MainActivity.sInstance.Tcp_Svr_IP.equals("")) {
                        addDevices(MainActivity.sInstance.Tcp_Svr_MAC, MainActivity.sInstance.Tcp_Svr_IP);

                        if (D) Log.e(TAG, "autoFlash:add");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                list_devices.setAdapter(mAdapter);
                                mAdapter.notifyDataSetChanged();
                                if (D) Log.e(TAG, "adding msg");
                            }
                        });
                    } else {
                        //addMsgHandler(NO_HAVE_DEVICES);
                    }
                }
            }
        });
        thread.start();
    }



    public void BackWifiInterface() {
        Intent serverIntent = new Intent(getApplicationContext(), MainActivity.class);

        serverIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivityForResult(serverIntent, 1);//启动Wifi界面
    }

    public int indexOf(String sMac, String sIP) {
        int index = -1;
        for (int i = 0; i < mData.size(); i++) {
            if ((sMac.equals(mData.get(i).getDevicesMac()))
                    && (sIP.equals(mData.get(i).getDevicesAddr()))
            ) {
                index = i;
            }
        }
        return index;
    }

    private boolean addDevices(String sMac, String sIP) {

        if (indexOf(sMac, sIP) != -1) {
            return false;   //有相同的
        }

        Devices devices = new Devices(sMac, sIP);

        if (D) Log.d(TAG, "addDevices: sMac: "+ sMac +"sIP"+sIP);

        if (!mData.contains(devices))
        {
            //mData.add(devices);
        }

        mData.add(devices);

        return true;
    }

    public class Devices {
        private String devicesMac;
        private String devicesAddr;
        private int aIcon;


        Devices(String devicesMac, String devicesAddr) {
            this.devicesMac = devicesMac;
            this.devicesAddr = devicesAddr;
            //this.aIcon = aIcon;
        }


        String getDevicesMac() {
            return devicesMac;
        }

        String getDevicesAddr() {
            return devicesAddr;
        }

        public int getaIcon() {
            return aIcon;
        }

        public void setDevicesMac(String macAddr) {
            this.devicesMac = macAddr;
        }

        public void setDevicesAddr(String addr) {
            this.devicesAddr = addr;
        }

        public void setaIcon(int aIcon) {
            this.aIcon = aIcon;
        }


    }

    public class DevicesAdapter extends BaseAdapter {
        private static final String TAG = "DevicesAdapter";

        private boolean D = true;

        private ArrayList<Devices> mData;
        private Context mContext;

        DevicesAdapter(ArrayList<Devices> mData, Context mContext) {
            super();
            this.mData = mData;
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_device, parent, false);
                holder = new ViewHolder();
                //ImageView img_icon = (ImageView) convertView.findViewById(R.id.img_icon);
                holder.devicesMacAddr = (TextView) convertView.findViewById(R.id.device_name);
                holder.devicesAddress = (TextView) convertView.findViewById(R.id.device_address);
                //img_icon.setBackgroundResource(mData.get(position).getaIcon());
                convertView.setTag(holder);   //将Holder存储到convertView中
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.devicesMacAddr.setText(mData.get(position).getDevicesMac());
            holder.devicesAddress.setText(mData.get(position).getDevicesAddr());
            //if (D)Log.e(TAG,"DevicesAdapter");

            return convertView;
        }

        void clear() {
            if (mData != null) {
                mData.clear();
            }
            notifyDataSetChanged();
        }

        class ViewHolder {
            ImageView img_icon;
            TextView devicesMacAddr;
            TextView devicesAddress;
        }

    }

    public class ItemClickEvent implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> l, View v, int position,
                                long id) {
            // TODO Auto-generated method stub
            //扫描设备标志位
            isopen = false;

            //发现线程
            sndTrdStop = true;

            //MainActivity.sInstance.mRcvFlag = true;

            // store the information for result
            //Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            Intent intent = new Intent();

            if (D) Log.d(TAG, "onItemClick: Mac: " + mData.get(position).getDevicesMac());
            intent.putExtra("Mac", mData.get(position).getDevicesMac());

            if (D) Log.d(TAG, "onItemClick: Addr: " + mData.get(position).getDevicesAddr());
            intent.putExtra("Addr", mData.get(position).getDevicesAddr());

            setResult(1, intent);
            if (D) Log.d(TAG, "store information ok");
            finish();

            //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            //startActivityForResult(intent, 1);//启动Wifi界面

        }
    }

}
