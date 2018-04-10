package com.test.machineinfo.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.machineinfo.R;
import com.test.machineinfo.data.DynamicMemory;
import com.test.machineinfo.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by WTZ on 2018/4/5.
 */

public class DynamicMemListAdapter extends RecyclerView.Adapter<DynamicMemListAdapter.MyViewHolder> {
    private static final String TAG = DynamicMemListAdapter.class.getSimpleName();
    private Context context;
    private List<DynamicMemory> dynamicMemoryList = new ArrayList<DynamicMemory>();
    private PackageManager pm;

    public DynamicMemListAdapter(Context context, List<DynamicMemory> dynamicMemoryList) {
        this.context = context;
        if (dynamicMemoryList != null) {
            this.dynamicMemoryList.addAll(dynamicMemoryList);
        }
        pm = context.getPackageManager();
    }

    public void destroy() {
        context = null;
        dynamicMemoryList.clear();
        dynamicMemoryList = null;
        onItemClickListener = null;
        pm = null;
    }

    public void updateAll(List<DynamicMemory> dynamicMemoryList) {
        if (dynamicMemoryList == null) {
            return;
        }
        this.dynamicMemoryList.clear();
        this.dynamicMemoryList.addAll(dynamicMemoryList);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (position < 0 || position >= dynamicMemoryList.size()) {
            return;
        }
        dynamicMemoryList.remove(position);
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
                        R.layout.item_dynamic_mem_layout, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder position: " + position);

        setInternalOnItemClickListener(holder);

        DynamicMemory dm = dynamicMemoryList.get(position);

        ApplicationInfo appInfo = dm.getTopApp();
        holder.id = appInfo.packageName;

        holder.ivIcon.setImageDrawable(appInfo.loadIcon(pm));
        holder.tvAppName.setText(appInfo.loadLabel(pm));

        String format = context.getString(R.string.format_rss);
        holder.tvRss.setText(String.format(format, dm.getTopAppPssMemory()));

        format = context.getString(R.string.format_top_page);
        holder.tvTopPage.setText(String.format(format, dm.getTopActivity()));

        format = context.getString(R.string.format_sample_time);
        String dateTime = Utils.getSpecifiedDateTime(new Date(dm.getTimeStamp()), "yyyy年MM月dd日 HH:mm:ss");
        holder.tvSampleTime.setText(String.format(format, dateTime));

        format = context.getString(R.string.format_availabe_mem);
        holder.tvAvailableMem.setText(String.format(format, dm.getSystemAvailableMemory() + "MB"));
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
        int count = (dynamicMemoryList != null) ? dynamicMemoryList.size() : 0;
        return count;
    }

    public Object getItem(int position) {
        return (dynamicMemoryList != null) ? dynamicMemoryList.get(position) : null;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        String id;
        ImageView ivIcon;
        TextView tvAppName;
        TextView tvRss;
        TextView tvTopPage;
        TextView tvSampleTime;
        TextView tvAvailableMem;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tvAppName = (TextView) itemView.findViewById(R.id.tv_app_name);
            tvRss = (TextView) itemView.findViewById(R.id.tv_app_rss);
            tvTopPage = (TextView) itemView.findViewById(R.id.tv_top_page);
            tvSampleTime = (TextView) itemView.findViewById(R.id.tv_sample_time);
            tvAvailableMem = (TextView) itemView.findViewById(R.id.tv_available_mem);
        }
    }
}
