package com.test.machineinfo.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.test.machineinfo.MyService;
import com.test.machineinfo.R;
import com.test.machineinfo.adapter.DynamicMemListAdapter;
import com.test.machineinfo.data.DynamicMemory;
import com.test.machineinfo.utils.Utils;
import com.test.machineinfo.view.DividerItemDecoration;
import com.test.machineinfo.view.RcvLinearLayoutManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by WTZ on 2018/4/4.
 */

public class FragmentDynamicMemInfo extends Fragment implements DynamicMemListAdapter.OnItemClickListener {
    private static final String TAG = FragmentDynamicMemInfo.class.getSimpleName();

    private RecyclerView mDynamicMemRecyclerView;
    private DynamicMemListAdapter mDynamicMemListAdapter;
    private ArrayList<DynamicMemory> mDynamicMemoryList;

    private LineChartView mLineChartView;
    private LineChartData mLineChartData;
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();
    private List<PointValue> mPointValues = new ArrayList<PointValue>();

    private MyService mService;
    private boolean mBound = false;

    private Handler mHandler;

    public FragmentDynamicMemInfo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        Intent intent = new Intent(getActivity(), MyService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            updateMemoryListData();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView...");
        View root = inflater.inflate(R.layout.fragment_dynamic_memory_info, container, false);

        mLineChartView = (LineChartView) root.findViewById(R.id.line_chart);
        initLineChart();

        mDynamicMemRecyclerView = (RecyclerView) root.findViewById(R.id.rcv_dynamic_mem);
        mDynamicMemRecyclerView.setLayoutManager(new RcvLinearLayoutManager(getContext()));
        mDynamicMemListAdapter = new DynamicMemListAdapter(getContext(), mDynamicMemoryList);
        mDynamicMemListAdapter.setOnItemClickListener(this);
        mDynamicMemRecyclerView.setAdapter(mDynamicMemListAdapter);
        mDynamicMemRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST));

        return root;
    }

    private void initLineChart() {
        Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色（橙色）
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(false);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
        line.setHasLabels(true);//曲线的数据坐标是否加上备注
//      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        lines.add(line);
        mLineChartData = new LineChartData();
        mLineChartData.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.BLACK);  //设置字体颜色
        axisX.setLineColor(Color.BLUE);
        axisX.setName("采样时间");
        axisX.setTextSize(10);//设置字体大小
        axisX.setMaxLabelChars(8); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        mLineChartData.setAxisXBottom(axisX); //x 轴在底部
        //data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        Axis axisY = new Axis();  //Y轴
        axisY.setName("可用内存(MB)");//y轴标注
        axisY.setTextColor(Color.BLACK);
        axisY.setLineColor(Color.BLUE);
        axisY.setTextSize(10);//设置字体大小
        mLineChartData.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边

        //设置行为属性，支持缩放、滑动以及平移
        mLineChartView.setInteractive(true);
        mLineChartView.setZoomType(ZoomType.HORIZONTAL);
        mLineChartView.setMaxZoom((float) 2);//最大方法比例
        mLineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        mLineChartView.setLineChartData(mLineChartData);
        mLineChartView.setVisibility(View.VISIBLE);
    }

    private void updateMemoryListData() {
        if (mService != null) {
            mDynamicMemoryList = mService.getCurrentMemoryList();

            updateLineChartData();

            if (mDynamicMemListAdapter != null) {
                mDynamicMemListAdapter.updateAll(mDynamicMemoryList);
            }
        }
    }

    private void updateLineChartData() {
        int size = mDynamicMemoryList.size();
        mAxisXValues.clear();
        for (int x = 0, i = size -1; i >= 0; x++, i--) {
            String dateTime = Utils.getSpecifiedDateTime(new Date(mDynamicMemoryList.get(i).getTimeStamp()), "HH:mm:ss");
            mAxisXValues.add(new AxisValue(x).setLabel(dateTime));
        }
        mPointValues.clear();
        for (int x = 0, i = size -1; i >= 0; x++, i--) {
            mPointValues.add(new PointValue(x, mDynamicMemoryList.get(i).getSystemAvailableMemory()));
        }
        if (mLineChartView != null && mLineChartData != null) {
            mLineChartView.setLineChartData(mLineChartData);
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        updateMemoryListData();
        // 在TV/BOX上延迟获取焦点
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDynamicMemRecyclerView.requestFocus();
            }
        }, 300);
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
        mDynamicMemListAdapter.destroy();
        mDynamicMemListAdapter = null;
        if (mDynamicMemoryList != null) {
            mDynamicMemoryList.clear();
            mDynamicMemoryList = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mBound) {
            getActivity().unbindService(mConnection);
            mService = null;
            mBound = false;
        }
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        super.onDestroy();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG, "onItemClick: " + position);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Log.d(TAG, "onItemLongClick: " + position);
    }
}
