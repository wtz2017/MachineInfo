package com.test.machineinfo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.test.machineinfo.R;
import com.test.machineinfo.data.ListItem;

import java.util.List;

public class ListAdapter extends BaseAdapter {
    private final static String TAG = ListAdapter.class.getName();

    private Context mContext;

    private List<ListItem> mList;

    public ListAdapter(Context context, List<ListItem> list) {
        mContext = context;
        mList = list;
    }

    public void update(List<ListItem> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public List<ListItem> getList() {
        return mList;
    }

    @Override
    public int getCount() {
        return (mList == null) ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return (mList == null) ? null : mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == mContext) {
            Log.d(TAG, "getView...null == mContext");
            return null;
        }

        if (null == mList || mList.isEmpty()) {
            Log.d(TAG, "getView...list isEmpty");
            return null;
        }

        ViewHolder itemLayout = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_home_list_layout, null);
            itemLayout = new ViewHolder();
            itemLayout.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(itemLayout);
        } else {
            itemLayout = (ViewHolder) convertView.getTag();
        }

        ListItem item = null;
        if ((item = mList.get(position)) != null) {
            itemLayout.tvName.setText(item.getName());
        }

        return convertView;
    }

    class ViewHolder {
        TextView tvName;
    }

}
