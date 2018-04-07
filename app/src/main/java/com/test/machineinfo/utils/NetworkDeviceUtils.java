package com.test.machineinfo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.conn.routing.RouteInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkDeviceUtils {

    private static final String TAG = NetworkDeviceUtils.class.getName();

    public static boolean isNetConnected(Context cxt) {
        if (cxt == null) {
            Log.d(TAG, "isNetConnected: Context is null");
            return false;
        }

        ConnectivityManager connectivity = (ConnectivityManager) cxt
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        Log.d(TAG, "isNetConnected: true");
                        return true;
                    }
                }
            }
        }

        Log.d(TAG, "isNetConnected: false");
        return false;
    }

    public static String getWlanMac(Context context) {
        String mac = null;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            mac = wifiInfo.getMacAddress();
        }
        if (TextUtils.isEmpty(mac)) {
            mac = readDevMacFromWlan0();
        }
        return mac;
    }

    public static String getEthMac() {
        return readDevMacFromEth0();
    }

    private static String readDevMacFromEth0() {
        final String path = "/sys/class/net/eth0/address";
        return readDevMac(path);
    }

    /**
     * 从设备配置文件"/sys/class/net/wlan0/address"中读取设备mac地址。
     *
     * @return 设备的mac地址
     */
    private static String readDevMacFromWlan0() {
        final String path = "/sys/class/net/wlan0/address";
        return readDevMac(path);
    }

    private static String readDevMac(final String path) {

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        String devMac = "";
        try {
            fis = new FileInputStream(new File(path));
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            StringBuffer buffer = new StringBuffer();
            buffer.append(br.readLine());
            devMac = buffer.toString().trim();
            Log.d(TAG, "read mac from " + path + "-" + devMac);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                // just ignore
                e.printStackTrace();
            }
        }
        return devMac;
    }

    /*
    * 其实这个与 android.os.Build.HARDWARE 相同
    * */
    public static String readCpuHardware() {
        String path = "/proc/cpuinfo";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        String hardware = "";
        try {
            fis = new FileInputStream(new File(path));
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.indexOf("Hardware") > -1) {
                    //提取内容
                    hardware = line.substring(line.indexOf(":") + 1,
                            line.length());
                    //去空格
                    hardware = hardware.trim();
                    break;
                }
            }
            Log.d(TAG, "read cpu hardware from " + path + ":" + hardware);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                // just ignore
                e.printStackTrace();
            }
        }
        return hardware;
    }

    public static String getBSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getBSSID() != null
                && !wifiInfo.getBSSID().equals("00:00:00:00:00:00")) {
            return wifiInfo.getBSSID();
        } else {
            List<ScanResult> list = wifiManager.getScanResults();
            int len = list.size();
            int index = -1;
            int maxLevel = -10000;
            for (int i = 0; i < len; i++) {
                ScanResult result = list.get(i);
                if (result.level > maxLevel) {
                    index = i;
                    maxLevel = result.level;
                }
            }

            if (index >= 0) {
                return list.get(index).BSSID;
            } else {
                return null;
            }
        }
    }

    public static ArrayList<String> getBSSIDList(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Log.d(TAG, "wifiManager == null");
            return null;
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d(TAG, "wifiInfo = " + wifiInfo);
        if (wifiInfo != null && wifiInfo.getBSSID() != null
                && !wifiInfo.getBSSID().equals("00:00:00:00:00:00")) {
            Log.d(TAG, "current connected wifi bssid = " + wifiInfo.getBSSID());
        }

        List<ScanResult> list = wifiManager.getScanResults();
        if (list == null || list.size() == 0) {
            Log.d(TAG, "wifiManager getScanResults is null");
            return null;
        }

        ArrayList<String> bssidList = new ArrayList<String>();
        for (ScanResult sr : list) {
            bssidList.add(sr.BSSID);
            Log.d(TAG, "find ap bssid: " + sr.BSSID);
        }
        return bssidList;
    }

    public static String getSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getSSID() != null) {
            return wifiInfo.getSSID();
        }

        List<ScanResult> list = wifiManager.getScanResults();
        if (null == list) {
            return null;
        }
        int len = list.size();
        int index = -1;
        int maxLevel = -10000;
        for (int i = 0; i < len; i++) {
            ScanResult result = list.get(i);
            if (null != result && result.level > maxLevel) {
                index = i;
                maxLevel = result.level;
            }
        }

        if (index >= 0) {
            return list.get(index).SSID;
        }

        return null;
    }

    /**
     * @param context
     * @return ANDROID_ID
     */
    public static String getAndroidID(Context context) {
        try {
            return Settings.System.getString(context.getContentResolver(),
                    Settings.System.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getEth0GatewayByCmd() {
        String gateWay = null;
        String prefix = "default via ";
        String suffix = " dev eth0";
        String cmd = "ip route show | grep \"default via\" | grep \"dev eth0\"";
        ShellUtils.CommandResult ret = ShellUtils.execCommand(cmd, false);
        Log.d(TAG, ret.toString());
        if (!TextUtils.isEmpty(ret.successMsg)) {
            int start = ret.successMsg.indexOf(prefix);
            int end = ret.successMsg.indexOf(suffix);
            if (start != -1 && end != -1) {
                start = start + prefix.length();
                gateWay = ret.successMsg.substring(start, end);
            }
        }
        return gateWay;
    }

    public static Map<String, String> getNetworkInfo(Context context) {
        String ip = "";
        String net_type = "";
        String mask = "";
        String gateway = "";
        String dns1 = "";
        String dns2 = "";
        String ssid = "";
        String bssid = "";
        Map<String, String> results = new HashMap<String, String>();

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo connectedInfo = null;
        if (cm != null) {
            NetworkInfo[] infos = cm.getAllNetworkInfo();
            for (NetworkInfo ni : infos) {
                if (ni.getState() == NetworkInfo.State.CONNECTED) {
                    Log.d(TAG, "find connected info, type is " + ni.getTypeName());
                    connectedInfo = ni;
                    break;
                }
            }
        }

        if (connectedInfo != null) {
            // 已连接
            switch (connectedInfo.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    net_type = "wifi";
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        ip = int2ip(wifiInfo.getIpAddress());
                        ssid = wifiInfo.getSSID();
                        bssid = wifiInfo.getBSSID();
                        DhcpInfo di = wifiManager.getDhcpInfo();
                        if (di != null) {
                            gateway = int2ip(di.gateway);
                            mask = int2ip(di.netmask);
                            dns1 = int2ip(di.dns1);
                            dns2 = int2ip(di.dns2);
                        }
                    }
                    if (TextUtils.isEmpty(ip) || !Utils.isIpString(ip)) {
                        ip = Utils.androidGetProp("dhcp.wlan0.ipaddress", "");
                    }
                    if (TextUtils.isEmpty(mask)) {
                        mask = Utils.androidGetProp("dhcp.wlan0.mask", "");
                    }
                    if (TextUtils.isEmpty(gateway)) {
                        gateway = Utils.androidGetProp("dhcp.wlan0.gateway", "");
                    }
                    if (TextUtils.isEmpty(dns1)) {
                        dns1 = Utils.androidGetProp("dhcp.wlan0.dns1", "");
                    }
                    if (TextUtils.isEmpty(dns2)) {
                        dns2 = Utils.androidGetProp("dhcp.wlan0.dns2", "");
                    }
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    net_type = "ethernet";
                    try {
                        for (Enumeration<NetworkInterface> en = NetworkInterface
                                .getNetworkInterfaces(); en.hasMoreElements(); ) {
                            {
                                NetworkInterface netInterface = en.nextElement();
                                List<InterfaceAddress> mList = netInterface.getInterfaceAddresses();
                                for (InterfaceAddress interfaceAddress : mList) {
                                    InetAddress inetAddress = interfaceAddress.getAddress();
                                    if (!inetAddress.isLoopbackAddress()) {
                                        String hostAddress = inetAddress.getHostAddress();
                                        Log.d(TAG, "inetAddress.getHostAddress = " + hostAddress);
                                        if (!hostAddress.contains("::")) {
                                            ip = hostAddress;
                                            mask = calcMaskByPrefixLength(interfaceAddress
                                                    .getNetworkPrefixLength());
                                        }
                                    }
                                }
                            }
                        }

                        Field cmServiceField = Class.forName(ConnectivityManager.class.getName())
                                .getDeclaredField("mService");
                        cmServiceField.setAccessible(true);
                        // connectivitymanager.mService
                        Object cmService = cmServiceField.get(cm);
                        // get IConnectivityManager class
                        Class cmServiceClass = Class.forName(cmService.getClass().getName());
                        Method methodGetLinkp = cmServiceClass.getDeclaredMethod("getLinkProperties",
                                new Class[]{int.class});
                        methodGetLinkp.setAccessible(true);
                        Object linkProperties = methodGetLinkp.invoke(cmService, ConnectivityManager.TYPE_ETHERNET);

                        Class<?> classLinkp = Class.forName("android.net.LinkProperties");
                        Method methodGetRoutes = classLinkp.getDeclaredMethod("getRoutes");
                        Method methodGetDnses = classLinkp.getDeclaredMethod("getDnses");
                        Collection<RouteInfo> routeInfos = (Collection<RouteInfo>) methodGetRoutes.invoke(linkProperties);
                        Collection<InetAddress> inetAddresses = (Collection<InetAddress>) methodGetDnses.invoke(linkProperties);

                        String routeInfoString = routeInfos.toString();
                        if (routeInfoString.contains(">")) {
                            gateway = routeInfoString.substring(
                                    routeInfoString.lastIndexOf('>') + 2,
                                    routeInfoString.length() - 1);
                            Log.d(TAG, "get gateway form routeInfoString: " + gateway);
                        }

                        String inetAddressString = inetAddresses.toString();
                        if (inetAddressString.contains(",")) {
                            dns1 = inetAddressString.substring(2,
                                    inetAddressString.lastIndexOf(","));
                            dns2 = inetAddressString.substring(
                                    inetAddressString.lastIndexOf(",") + 3,
                                    inetAddressString.length() - 1);
                            Log.d(TAG, "get dns form inetAddressString: " + dns1 + ", " + dns2);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (TextUtils.isEmpty(ip) || !Utils.isIpString(ip)) {
                        ip = Utils.androidGetProp("dhcp.eth0.ipaddress", "");
                    }
                    if (TextUtils.isEmpty(mask)) {
                        mask = Utils.androidGetProp("dhcp.eth0.mask", "");
                    }
                    if (TextUtils.isEmpty(gateway)) {
                        gateway = Utils.androidGetProp("dhcp.eth0.gateway", "");
                    }
                    if (TextUtils.isEmpty(dns1)) {
                        dns1 = Utils.androidGetProp("dhcp.eth0.dns1", "");
                    }
                    if (TextUtils.isEmpty(dns2)) {
                        dns2 = Utils.androidGetProp("dhcp.eth0.dns2", "");
                    }

                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    net_type = "mobile";
                    // TODO: 2017/9/20
                    break;
                default:
                    net_type = "unknown";
                    break;
            }
        }

        results.put("net_type", net_type);
        results.put("ip", ip);
        results.put("mask", mask);
        results.put("gateway", gateway);
        results.put("dns1", dns1);
        results.put("dns2", dns2);
        results.put("ssid", ssid);
        results.put("bssid", bssid);
        return results;
    }

    private static String calcMaskByPrefixLength(int length) {
        int mask = -1 << (32 - length);
        int partsNum = 4;
        int bitsOfPart = 8;
        int maskParts[] = new int[partsNum];
        int selector = 0x000000ff;


        for (int i = 0; i < maskParts.length; i++) {
            int pos = maskParts.length - 1 - i;
            maskParts[pos] = (mask >> (i * bitsOfPart)) & selector;
        }


        String result = "";
        result = result + maskParts[0];
        for (int i = 1; i < maskParts.length; i++) {
            result = result + "." + maskParts[i];
        }
        Log.d(TAG, "calcMaskByPrefixLength: " + result);
        return result;
    }

    private static String int2ip(long ip) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf((int) (ip & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
        return sb.toString();
    }
}
