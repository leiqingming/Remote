package com.hxws.remote.Utilities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationUtils;

import com.hxws.remote.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Splash extends Dialog {
    private static final String TAG = "Splash";

    private static final int PROGRESS_TIMEOUT = 3000;
    private static final int AUTO_DISMISS = 1;


    private Context mContext;


    private final Runnable mProgressTimeoutRunnable  = new Runnable() {
        @Override
        public void run() {
            View v = getLayoutInflater().inflate(
                    R.layout.splash, null /* root */);
            setContentView(v);
            v.setAlpha(0f);
            v.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setInterpolator(AnimationUtils.loadInterpolator(
                            mContext, android.R.interpolator.fast_out_slow_in))
                    .start();
            getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    };



    public Splash(Context context) {
        super(context, R.style.SplashDialog);
        mContext = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Set ourselves totally black before the device is provisioned so that
        // we don't flash the wallpaper before SUW
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        mProgressTimeoutRunnable.run();
        mHandler.sendEmptyMessageDelayed(AUTO_DISMISS, PROGRESS_TIMEOUT);

    }

    @Override
    public void dismiss() {
        mHandler.removeMessages(AUTO_DISMISS);
        super.dismiss();
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == AUTO_DISMISS) {
                Log.i(TAG, "Auto dismiss");
                dismiss();
            }
        }
    };
}
