package com.test.machineinfo;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.test.machineinfo.data.DynamicMemory;
import com.test.machineinfo.utils.HardwareInfoUtil;
import com.test.machineinfo.utils.Utils;

import java.util.ArrayList;

public class MyService extends Service {
    private final static String TAG = MyService.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();

    private static final int MAX_STAT_NUM = 10;
    private static final int STAT_TIME_INTERVAL = 3000;// milliscond
    private int mLatestMemoryIndex;
    private ArrayList<DynamicMemory> mDynamicMemoryList = new ArrayList<DynamicMemory>();
    private ArrayList<DynamicMemory> mDynamicMemoryListCopy = new ArrayList<DynamicMemory>();
    private DynamicMemory mDynamicMemoryTemp = new DynamicMemory();
    private ActivityManager.MemoryInfo mMemoryInfoTemp = new ActivityManager.MemoryInfo();

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private PackageManager pm;
    private final static int GC_INTERVAL_COUNT = 10;
    private int mGcCount;

    public class LocalBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind...");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate...");
        Notification notification = new Notification();
        startForeground(1, notification);

        pm = getPackageManager();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                readMemory();
                if (++mGcCount >= GC_INTERVAL_COUNT) {
                    mGcCount = 0;
                    System.gc();
                }

                mHandler.removeCallbacks(this);
                mHandler.postDelayed(this, STAT_TIME_INTERVAL);
            }
        });
    }

    private void readMemory() {
        mDynamicMemoryTemp.setTimeStamp(System.currentTimeMillis());
        mDynamicMemoryTemp.setSystemAvailableMemory(HardwareInfoUtil.availableMemory(this, mMemoryInfoTemp));
        ActivityManager.RunningTaskInfo rti = Utils.getTopRunningAppInfo(this);
        String pkgName = null;
        if (rti != null) {
            mDynamicMemoryTemp.setTopActivity(rti.topActivity.getClassName());
            pkgName = rti.topActivity.getPackageName();
        } else {
            mDynamicMemoryTemp.setTopActivity(null);
        }
        try {
            mDynamicMemoryTemp.setTopApp(pm.getApplicationInfo(pkgName, 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            mDynamicMemoryTemp.setTopApp(null);
        }
        try {
            mDynamicMemoryTemp.setTopAppPssMemory(Utils.getAppPssMemory(this, pkgName));
        } catch (Exception e) {
            e.printStackTrace();
            mDynamicMemoryTemp.setTopAppPssMemory("");
        }

        updateLatestMemory(mDynamicMemoryTemp);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "onStart...");
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand...");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy...");
        super.onDestroy();
    }

    /**
     * Reuse a list of fixed sizes.
     * @param dm
     */
    private synchronized void updateLatestMemory(DynamicMemory dm) {
        DynamicMemory dynamicMemory;
        if (mDynamicMemoryList.size() < MAX_STAT_NUM) {
            dynamicMemory = new DynamicMemory();
            dynamicMemory.copyData(dm);
            mDynamicMemoryList.add(dynamicMemory);
            mLatestMemoryIndex = mDynamicMemoryList.size() - 1;
            return;
        }

        // reuse old object
        mLatestMemoryIndex++;
        if (mLatestMemoryIndex == MAX_STAT_NUM) {
            mLatestMemoryIndex = 0;
        }
        dynamicMemory = mDynamicMemoryList.get(mLatestMemoryIndex);
        dynamicMemory.copyData(dm);
    }

    /**
     * Sort by the latest time
     */
    public synchronized ArrayList<DynamicMemory> getCurrentMemoryList() {
        int size1 = mDynamicMemoryList.size();
        int size2 = mDynamicMemoryListCopy.size();

        for (int num = 0, index = mLatestMemoryIndex; num < size1; num++) {
            DynamicMemory dm;
            if (num < size2) {
                //has already object
                dm = mDynamicMemoryListCopy.get(num);
            } else {
                dm = new DynamicMemory();
                mDynamicMemoryListCopy.add(dm);
            }
            dm.copyData(mDynamicMemoryList.get(index));
            index--;
            if (index == -1) {
                index = size1 - 1;
            }
        }
        return mDynamicMemoryListCopy;
    }
}
