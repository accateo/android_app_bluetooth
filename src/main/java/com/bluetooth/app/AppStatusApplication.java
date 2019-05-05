package com.bluetooth.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * AppStatusApplication: with this you can manage constants and, in this, case
 * we manage the bluetooth connection when the screen is off or when the app is
 * in background
 *
 *
 *
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class AppStatusApplication extends Application implements
        ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private static String TAG = AppStatusApplication.class.getName();

    public static String stateOfLifeCycle = "";

    public static Activity activityPaused;


    public static boolean wasInBackground = false;

    public static boolean startTimer = false;

    public static int TIMEOUT_BT = 180000;




    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        registerBroadcastReceiver();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle arg1) {
        wasInBackground = false;
        stateOfLifeCycle = "Create";
    }

    @Override
    public void onActivityStarted(Activity activity) {
        stateOfLifeCycle = "Start";
    }

    @Override
    public void onActivityResumed(Activity activity) {
        stateOfLifeCycle = "Resume";
        wasInBackground = false;

    }

    @Override
    public void onActivityPaused(Activity activity) {
        stateOfLifeCycle = "Pause";


    }

    @Override
    public void onActivityStopped(Activity activity) {

        stateOfLifeCycle = "Stop";
        activityPaused = activity;

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle arg1) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        wasInBackground = false;
        stateOfLifeCycle = "Destroy";
    }

    @Override
    public void onTrimMemory(int level) {
        if (stateOfLifeCycle.equals("Stop") && (activityPaused instanceof MainActivity )) {
            wasInBackground = true;
            new CountDownTimer(TIMEOUT_BT,1000){


                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    if (BluetoothApplication.getBSocket() != null && wasInBackground) {
                        try {

                            BluetoothApplication.getBSocket().close();
                            activityPaused.finishAffinity();
                            Toast.makeText(getApplicationContext(),"Connection closed", Toast.LENGTH_LONG).show();


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();

        }
        super.onTrimMemory(level);
    }

    //screen off
    private void registerBroadcastReceiver() {
        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction(Intent.ACTION_SCREEN_OFF);

        BroadcastReceiver screenOnOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String strAction = intent.getAction();
                if (strAction.equals(Intent.ACTION_SCREEN_OFF)) {
                    onTrimMemory(TRIM_MEMORY_UI_HIDDEN);
                }
            }
        };
        getApplicationContext()
                .registerReceiver(screenOnOffReceiver, theFilter);
    }
}
