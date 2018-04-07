package com.test.machineinfo.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.test.machineinfo.R;
import com.test.machineinfo.data.StorageInfo;
import com.test.machineinfo.utils.HardwareInfoUtil;
import com.test.machineinfo.utils.NetworkDeviceUtils;
import com.test.machineinfo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by WTZ on 2018/4/4.
 */

public class FragmentTotalInfo  extends Fragment {
    private static final String TAG = FragmentTotalInfo.class.getSimpleName();

    private ScrollView mScrollView;

    public FragmentTotalInfo() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView...");
        View root = inflater.inflate(R.layout.fragment_total_info, container, false);

        mScrollView = (ScrollView) root.findViewById(R.id.scrollView);
        LinearLayout contentContainer = (LinearLayout) root.findViewById(R.id.ll_container);

        StringBuffer buffer = new StringBuffer();

        buffer.setLength(0);
        readBasicInfo(buffer);
        addModule(contentContainer, "基本信息", buffer.toString());

        buffer.setLength(0);
        readVersions(buffer);
        addModule(contentContainer, "版本信息", buffer.toString());

        buffer.setLength(0);
        readChipInfo(buffer);
        addModule(contentContainer, "芯片信息", buffer.toString());

        buffer.setLength(0);
        readStorage(buffer);
        addModule(contentContainer, "内存信息", buffer.toString());

        buffer.setLength(0);
        readNetwork(buffer, getActivity());
        addModule(contentContainer, "网络信息", buffer.toString());

        return root;
    }

    private void addModule(LinearLayout contentContainer, String title, String content) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(title);
        tvTitle.setBackgroundColor(Color.parseColor("#CAFF70"));
        contentContainer.addView(tvTitle, lp);

        TextView tvContent = new TextView(getContext());
        tvContent.setText(content);
        contentContainer.addView(tvContent, lp);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        mScrollView.requestFocus();
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        super.onDestroyView();
    }

    private void setReceiveInfo(List<String> list, TextView tv2) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("当前收到的广播有：\n");
        if (list != null) {
            for (String info : list) {
                buffer.append(info);
                buffer.append("\n");
            }
        }

        tv2.setText(buffer.toString());
    }

    private void readNetwork(StringBuffer buffer, Context context) {
        buffer.append("MAC(有线):");
        buffer.append(NetworkDeviceUtils.getEthMac());
        buffer.append("\r\n");
        buffer.append("MAC(无线):");
        buffer.append(NetworkDeviceUtils.getWlanMac(context));
        buffer.append("\r\n");

        Map<String, String> networkInfos = NetworkDeviceUtils.getNetworkInfo(getActivity());

        buffer.append("Net Type:");
        buffer.append(networkInfos.get("net_type"));
        buffer.append("\r\n");

        buffer.append("IP:");
        buffer.append(networkInfos.get("ip"));
        buffer.append("\r\n");

        buffer.append("Mask:");
        buffer.append(networkInfos.get("mask"));
        buffer.append("\r\n");

        buffer.append("Gateway:");
        buffer.append(networkInfos.get("gateway"));
        buffer.append("\r\n");

        buffer.append("DNS1:");
        buffer.append(networkInfos.get("dns1"));
        buffer.append("\r\n");

        buffer.append("DNS2:");
        buffer.append(networkInfos.get("dns2"));
        buffer.append("\r\n");

        if (!TextUtils.isEmpty(networkInfos.get("ssid"))) {
            buffer.append("SSID:");
            buffer.append(networkInfos.get("ssid"));
            buffer.append("\r\n");

            buffer.append("BSSID:");
            buffer.append(networkInfos.get("bssid"));
            buffer.append("\r\n");
        }

        ArrayList<String> bssidList = NetworkDeviceUtils.getBSSIDList(getActivity());
        if (bssidList != null && bssidList.size() > 0) {
            buffer.append("around ap bssids:");
            buffer.append("\r\n");
            for (String s : bssidList) {
                buffer.append(s);
                buffer.append("/");
            }
            buffer.append("\r\n");
        }
    }

    private void readVersions(StringBuffer buffer) {
        buffer.append("ROM版本增量:");
        buffer.append(Build.VERSION.INCREMENTAL);
        buffer.append("\r\n");

        buffer.append("ROM描述:");
        buffer.append(Utils.androidGetProp("ro.build.description", ""));
        buffer.append("\r\n");

        buffer.append("Android系统Platform:");
        buffer.append(Build.VERSION.RELEASE);
        buffer.append("\r\n");

        buffer.append("Android系统SDK API Level:");
        buffer.append(Build.VERSION.SDK_INT);
        buffer.append("\r\n");
    }

    private void readBasicInfo(StringBuffer buffer) {
        buffer.append("机型:");
        buffer.append(Build.MODEL);
        buffer.append("（此为参考，具体以厂商接口为准）\r\n");

        buffer.append("制造商:");
        buffer.append(Build.MANUFACTURER);
        buffer.append("\r\n");

        buffer.append("品牌:");
        buffer.append(Build.BRAND);
        buffer.append("\r\n");

        buffer.append("产品:");
        buffer.append(Build.PRODUCT);
        buffer.append("\r\n");

        buffer.append("硬件:");
        buffer.append(Build.HARDWARE);
        buffer.append("\r\n");

        buffer.append("芯片类型:");
        buffer.append(Utils.androidGetProp("persist.sys.chiptype", ""));
        buffer.append("\r\n");

        buffer.append("AndroidID:");
        buffer.append(NetworkDeviceUtils.getAndroidID(getActivity()));
        buffer.append("\r\n");

        Map<String, Integer> screenMap = Utils.getScreenResolution(getActivity());
        buffer.append("分辨率:");
        buffer.append(screenMap.get("width") + "x" + screenMap.get("height"));
        Log.d(TAG, "分辨率：" + screenMap.get("width") + "x" + screenMap.get("height"));
        buffer.append("\r\n");

        long bootPassTime = SystemClock.elapsedRealtime();
        buffer.append("已开机时间:");
        buffer.append(Utils.getRemainTime(bootPassTime));
        buffer.append("\r\n");
    }

    private void readChipInfo(StringBuffer buffer) {
        buffer.append("NumberOfCPUCores:");
        buffer.append(HardwareInfoUtil.getNumberOfCPUCores());
        buffer.append("\r\n");

        buffer.append("MaxCpuFreq:");
        buffer.append(HardwareInfoUtil.getMaxCpuFreq());
        buffer.append("\r\n");

        buffer.append("MinCpuFreq:");
        buffer.append(HardwareInfoUtil.getMinCpuFreq());
        buffer.append("\r\n");

        buffer.append("CPU_ABI:");
        buffer.append(HardwareInfoUtil.getCPU_ABI("/"));
        buffer.append("\r\n");

        buffer.append("CpuInfo:");
        buffer.append(HardwareInfoUtil.getCpuInfo("#"));
        buffer.append("\r\n");
    }

    private void readStorage(StringBuffer buffer) {
        String[] ramList = HardwareInfoUtil.readRAMFromFile();
        buffer.append("内存(RAM)：\r\n");
        buffer.append("MemTotal:");
        buffer.append(ramList[0]);
        buffer.append("\r\n");

        buffer.append("MemFree:");
        buffer.append(ramList[1]);
        buffer.append("\r\n");

        buffer.append("MemAvailable:");
        String memAvailable = "-1";
        if (TextUtils.isEmpty(ramList[2])) {
            memAvailable = HardwareInfoUtil.availableMemory(getActivity()) + "MB";
        }
        buffer.append(memAvailable);
        buffer.append("\r\n");

        buffer.append("\r\n");

        List<StorageInfo> storageInfos = new ArrayList<StorageInfo>();
        storageInfos.add(new StorageInfo(Environment.getDataDirectory()));
        storageInfos.add(new StorageInfo(Environment.getExternalStorageDirectory()));
        for (StorageInfo info : storageInfos) {
            buffer.append(info.path + "存储情况：\r\n");
            buffer.append("总大小:");
            buffer.append("" + (info.blockSize * info.blockCount / 1024 / 1024));
            buffer.append("MB");
            buffer.append("\r\n");

            buffer.append("可用大小:");
            buffer.append("" + (info.availCount * info.blockSize / 1024 / 1024));
            buffer.append("MB");
            buffer.append("\r\n");
            buffer.append("\r\n");
        }
    }

}
