package com.test.machineinfo;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    private static final String LOG_TAG = MyApplication.class.getSimpleName();

    private static MyApplication mApp;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "App onServiceDisconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "App onServiceConnected");
        }
    };

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate...");
        super.onCreate();
        mApp = this;
//        Intent serviceIntent = new Intent(this, MyService.class);
//        startService(serviceIntent);
        if (isMainProcess()) {// just main app to bind local identify service
            Log.i(LOG_TAG, "Main process app to bind local service...");
            Intent serviceIntent = new Intent(this, MyService.class);
            bindService(serviceIntent, connection, Service.BIND_AUTO_CREATE);
        }
    }

    public static MyApplication getInstance() {
        return mApp;
    }

    public boolean isMainProcess() {
        boolean ret = true;
        ActivityManager am = (ActivityManager) this
                .getSystemService(ACTIVITY_SERVICE);
        if (am == null) {
            return ret;
        }
        List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
        int pid = android.os.Process.myPid();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).pid == pid) {
                    String name = list.get(i).processName;
                    if (name != null
                            && !(name.equalsIgnoreCase(this.getPackageName()))) {
                        ret = false;
                    }
                    break;
                }
            }
        }

        return ret;
    }
}
