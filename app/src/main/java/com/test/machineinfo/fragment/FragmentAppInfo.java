package com.test.machineinfo.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.test.machineinfo.R;
import com.test.machineinfo.adapter.InstallAppListAdapter;
import com.test.machineinfo.utils.ShellUtils;
import com.test.machineinfo.view.DividerItemDecoration;
import com.test.machineinfo.view.RcvLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WTZ on 2018/4/4.
 */

public class FragmentAppInfo extends Fragment implements InstallAppListAdapter.OnItemClickListener {
    private static final String TAG = FragmentAppInfo.class.getSimpleName();

    private EditText etPkgWord;

    private RecyclerView mInstallAppRecyclerView;
    private InstallAppListAdapter mInstallAppListAdapter;
    private List<ApplicationInfo> mApplicationInfos = new ArrayList<ApplicationInfo>();

    private View mFocusView;

    public FragmentAppInfo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateAppListData("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView...");
        View root = inflater.inflate(R.layout.fragment_app_info, container, false);

        mInstallAppRecyclerView = (RecyclerView) root.findViewById(R.id.rcv_install_app_list);
        mInstallAppRecyclerView.setLayoutManager(new RcvLinearLayoutManager(getContext()));
        mInstallAppListAdapter = new InstallAppListAdapter(getContext(), mApplicationInfos);
        mInstallAppListAdapter.setOnItemClickListener(this);
        mInstallAppRecyclerView.setAdapter(mInstallAppListAdapter);
        mInstallAppRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST));

        etPkgWord = (EditText) root.findViewById(R.id.et_pkg_keyword);
        Button btnUpdate = (Button) root.findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keyword = "";
                if (etPkgWord.getText() == null || TextUtils.isEmpty(keyword = etPkgWord.getText().toString())) {
//                    Toast.makeText(getContext(), "过滤关键词不能为空", Toast.LENGTH_SHORT).show();
//                    return;
                    keyword = "";
                }

                updateAppListData(keyword);
                mInstallAppListAdapter.updateAll(mApplicationInfos);
            }
        });

        registerReceiver();
        return root;
    }

    private void updateAppListData(String keyword) {
        mApplicationInfos.clear();

        PackageManager pm = getActivity().getPackageManager();
        List<ApplicationInfo> listAppcations = pm
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo info : listAppcations) {
            String name = (String) info.loadLabel(pm);
            if (info.packageName.contains(keyword)
                    || (!TextUtils.isEmpty(name) && name.contains(keyword))) {
                mApplicationInfos.add(info);
            }
        }

        Log.d(TAG, "mApplicationInfos.size=" + mApplicationInfos.size());
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        if (mFocusView != null) {
            mFocusView.requestFocus();
        } else {
            etPkgWord.requestFocus();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        mFocusView = getActivity().getCurrentFocus();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        getContext().unregisterReceiver(mReceiver);
        mInstallAppListAdapter.destroy();
        mInstallAppListAdapter = null;
        mApplicationInfos.clear();
        mApplicationInfos = null;
        super.onDestroyView();
    }

    private void registerReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        mFilter.addDataScheme("package");
        getContext().registerReceiver(mReceiver, mFilter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "mReceiver: " + action);
            if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                String data = intent.getDataString();
                if (TextUtils.isEmpty(data) || !data.contains(":")) {
                    return;
                }
                int index = data.indexOf(":");
                String packageName = data.substring(index + 1);
                int size = (mApplicationInfos != null) ? mApplicationInfos.size() : 0;
                int position = -1;
                for (int i = 0; i < size; i++) {
                    if (mApplicationInfos.get(i).packageName.equals(packageName)) {
                        position = i;
                        break;
                    }
                }
                Log.i(TAG, "卸载了: " + packageName + ", position: " + position);
                if (position != -1) {
                    mApplicationInfos.remove(position);
                    mInstallAppListAdapter.remove(position);
                }
            }
        }
    };

    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG, "onItemClick: " + position);
        ApplicationInfo appInfo = (ApplicationInfo) mInstallAppListAdapter.getItem(position);
        Uri uri = Uri.fromParts("package", appInfo.packageName, null);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Log.d(TAG, "onItemLongClick: " + position);
    }
}
