package com.test.machineinfo.data;

import android.content.pm.ApplicationInfo;

/**
 * Created by WTZ on 2018/4/6.
 */

public class DynamicMemory {

    long timeStamp;
    int systemAvailableMemory;
    ApplicationInfo topApp;
    String topActivity;
    String topAppPssMemory;

    public DynamicMemory copyData(DynamicMemory dm) {
        timeStamp = dm.timeStamp;
        systemAvailableMemory = dm.systemAvailableMemory;
        topApp = dm.topApp;
        topAppPssMemory = dm.topAppPssMemory;
        topActivity = dm.topActivity;
        return this;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * unit:MB
     * @return
     */
    public int getSystemAvailableMemory() {
        return systemAvailableMemory;
    }

    public void setSystemAvailableMemory(int systemAvailableMemory) {
        this.systemAvailableMemory = systemAvailableMemory;
    }

    public ApplicationInfo getTopApp() {
        return topApp;
    }

    public void setTopApp(ApplicationInfo topApp) {
        this.topApp = topApp;
    }

    public String getTopAppPssMemory() {
        return topAppPssMemory;
    }

    public void setTopAppPssMemory(String topAppRssMemory) {
        this.topAppPssMemory = topAppRssMemory;
    }

    public String getTopActivity() {
        return topActivity;
    }

    public void setTopActivity(String topActivity) {
        this.topActivity = topActivity;
    }
}
