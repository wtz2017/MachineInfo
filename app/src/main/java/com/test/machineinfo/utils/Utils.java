package com.test.machineinfo.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by WTZ on 2017/9/19.
 */

public class Utils {
    private final static String TAG = Utils.class.getSimpleName();

    public static String androidGetProp(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "get property, " + key + " = " + value);
        return value;
    }

    public static boolean isIpString(String target) {
        String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        return isMatch(regex, target);
    }

    private static boolean isMatch(String regex, String target) {
        if (target == null || target.trim().equals("")) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(target);
        return matcher.matches();
    }

    public static String getRemainTime(long time) {
        time = time / 1000;
        int second = (int) (time % 60);
        int minute = (int) (time / 60 % 60);
        int hour = (int) (time / 60 / 60);
        String hourString = formatTime(String.valueOf(hour));
        String minuteString = formatTime(String.valueOf(minute));
        String secondString = formatTime(String.valueOf(second));
        String total = hourString + ":" + minuteString + ":" + secondString;
        return total;
    }

    private static String formatTime(String original) {
        if (original.length() < 2) {
            original = "0" + original;
        }
        return original;
    }

    public static Map<String, Integer> getScreenResolution(Activity activity) {
        int widthPixels;
        int heightPixels;
        Map<String, Integer> map = new HashMap<String, Integer>();

        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthPixels = dm.widthPixels;
        heightPixels = dm.heightPixels;

        Display display = activity.getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                Method method = display.getClass().getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, dm);
            } catch (Exception e) {
                e.printStackTrace();
            }
            heightPixels = dm.heightPixels;
        } else {
            try {
                Method method = display.getClass().getMethod("getRawHeight");
                heightPixels = (Integer) method.invoke(display);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        map.put("width", widthPixels);
        map.put("height", heightPixels);
        return map;
    }

    /**
     * @param format e.g. "yy-MM-dd_HH-mm-ss"
     * @return DateTime
     */
    public static String getCurrentDateTime(String format) {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(format);
        String nowTime = df.format(date);
        return nowTime;
    }

    /**
     * @param format
     *            e.g. "yy-MM-dd_HH-mm-ss"
     * @return DateTime
     */
    public static String getSpecifiedDateTime(Date date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        String nowTime = df.format(date);
        return nowTime;
    }

    /**
     * 获取Android App的缓存大小、数据大小、应用程序大小
     *
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void getAppSize(PackageManager pm, String pkgName, IPackageStatsObserver observer) throws NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException {
        // getPackageSizeInfo是PackageManager中的一个private方法，所以需要通过反射的机制来调用
        Method method = PackageManager.class.getMethod("getPackageSizeInfo",
                new Class[]{String.class, IPackageStatsObserver.class});
        // 调用 getPackageSizeInfo 方法，需要两个参数：1、需要检测的应用包名；2、回调
        method.invoke(pm, new Object[]{pkgName, observer});
    }

    public static ActivityManager.RunningTaskInfo getTopRunningAppInfo(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        if (runningTaskInfos != null && runningTaskInfos.size() > 0) {
            return runningTaskInfos.get(0);
        }
        return null;
    }

    public static String getAppRssMemory(Context context, String packageName) throws Exception {
        String cmd = "ps | grep " + packageName;
        ShellUtils.CommandResult ret = ShellUtils.execCommand(cmd, false);
        ArrayList<String> list = splitStringBySpace(ret.successMsg);
        String rss = list.get(4);
        long rssl = Long.parseLong(rss);//KB
        rssl = rssl * 1024;//Byte
        return Formatter.formatFileSize(context, rssl);
    }

    public static ArrayList<String> splitStringBySpace(String original) {
        String[] array = original.split(" ");
        ArrayList<String> list = new ArrayList<String>();
        for (String s : array) {
            if (s != null && !s.equals("") && !s.equals(" ")) {
                list.add(s);
            }
        }
        return list;
    }
}
