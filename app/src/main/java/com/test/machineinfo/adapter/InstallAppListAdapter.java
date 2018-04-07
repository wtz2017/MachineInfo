package com.test.machineinfo.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.machineinfo.R;
import com.test.machineinfo.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WTZ on 2018/4/5.
 */

public class InstallAppListAdapter extends RecyclerView.Adapter<InstallAppListAdapter.MyViewHolder> {
    private static final String TAG = InstallAppListAdapter.class.getSimpleName();
    private Context context;
    private List<ApplicationInfo> applicationInfos = new ArrayList<ApplicationInfo>();
    private PackageManager pm;
    private Handler handler = new Handler(Looper.getMainLooper());

    public InstallAppListAdapter(Context context, List<ApplicationInfo> applicationInfos) {
        this.context = context;
        this.applicationInfos.addAll(applicationInfos);
        pm = context.getPackageManager();
    }

    public void updateAll(List<ApplicationInfo> applicationInfos) {
        this.applicationInfos.clear();
        this.applicationInfos.addAll(applicationInfos);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (position < 0 || position >= applicationInfos.size()) {
            return;
        }
        applicationInfos.remove(position);
        notifyItemRemoved(position);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        MyViewHolder holder = new MyViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.item_install_app_layout, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder position: " + position);

        setInternalOnItemClickListener(holder);

        ApplicationInfo appInfo = applicationInfos.get(position);
        holder.id = appInfo.packageName;

        holder.ivIcon.setImageDrawable(appInfo.loadIcon(pm));
        holder.tvAppName.setText(appInfo.loadLabel(pm));

        String format = context.getString(R.string.format_app_version);
        try {
            PackageInfo packageInfo = pm.getPackageInfo(appInfo.packageName, 0);
            holder.tvVersion.setText(
                    String.format(format, packageInfo.versionName, packageInfo.versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        format = context.getString(R.string.format_app_pkg_name);
        holder.tvPkgName.setText(String.format(format, appInfo.packageName));

        format = context.getString(R.string.format_app_data_dir);
        holder.tvDataDir.setText(String.format(format, appInfo.dataDir));

        format = context.getString(R.string.format_app_apk_dir);
        holder.tvApkDir.setText(String.format(format, appInfo.sourceDir));

        format = context.getString(R.string.format_app_storage);
        holder.tvStorage.setText(String.format(format, "--", "--", "--", "--"));
        try {
            Utils.getAppSize(pm, appInfo.packageName, new IPackageStatsObserver.Stub() {
                @Override
                public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                    // 回调是在子线程中，需要到主线程更新ui
                    Log.d(TAG, "onGetStatsCompleted succeeded: " + succeeded);
                    updateStorageOnUiThread(holder, pStats);
                }
            });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void setInternalOnItemClickListener(final MyViewHolder holder) {
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    onItemClickListener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int count = (applicationInfos != null) ? applicationInfos.size() : 0;
        return count;
    }

    public Object getItem(int position) {
        return (applicationInfos != null) ? applicationInfos.get(position) : null;
    }

    private void updateStorageOnUiThread(final MyViewHolder holder, final PackageStats pStats) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (pStats == null) {
                    return;
                }
                if (!holder.id.equals(pStats.packageName)) {
                    return;
                }
                long codeSize = pStats.codeSize + pStats.externalCodeSize;
                long dataSize = pStats.dataSize + pStats.externalDataSize;
                long cacheSize = pStats.cacheSize + pStats.externalCacheSize;
                long totalSize = codeSize + dataSize + cacheSize;
                String format = context.getString(R.string.format_app_storage);
                holder.tvStorage.setText(String.format(format, Formatter.formatFileSize(context, totalSize),
                        Formatter.formatFileSize(context, codeSize),
                        Formatter.formatFileSize(context, dataSize),
                        Formatter.formatFileSize(context, cacheSize)));
            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        String id;
        ImageView ivIcon;
        TextView tvAppName;
        TextView tvVersion;
        TextView tvPkgName;
        TextView tvDataDir;
        TextView tvApkDir;
        TextView tvStorage;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tvAppName = (TextView) itemView.findViewById(R.id.tv_app_name);
            tvVersion = (TextView) itemView.findViewById(R.id.tv_version);
            tvPkgName = (TextView) itemView.findViewById(R.id.tv_pkg_name);
            tvDataDir = (TextView) itemView.findViewById(R.id.tv_data_dir);
            tvApkDir = (TextView) itemView.findViewById(R.id.tv_apk_dir);
            tvStorage = (TextView) itemView.findViewById(R.id.tv_app_storage);
        }
    }
}
