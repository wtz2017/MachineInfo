package com.test.machineinfo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.test.machineinfo.data.ListItem;
import com.test.machineinfo.fragment.FragmentAppInfo;
import com.test.machineinfo.fragment.FragmentDynamicMemInfo;
import com.test.machineinfo.fragment.FragmentList;
import com.test.machineinfo.fragment.FragmentTotalInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements FragmentList.OnFragmentListClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ArrayList<ListItem> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        initList();

        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentByTag(FragmentList.class.getSimpleName()) == null) {
            Log.d(TAG, "findFragmentByTag == null, to add " + FragmentList.class.getSimpleName());
            // 防止屏幕旋转时重复加载fragment
            FragmentList frag = FragmentList.newInstance(mList);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.add(R.id.fl_container, frag, FragmentList.class.getSimpleName());
            transaction.commit();
        }
    }

    private void initList() {
        mList = new ArrayList<ListItem>();
        mList.add(new ListItem("系统信息", FragmentTotalInfo.class.getName()));
        mList.add(new ListItem("应用列表", FragmentAppInfo.class.getName()));
        mList.add(new ListItem("动态内存", FragmentDynamicMemInfo.class.getName()));
    }

    @Override
    public void onFragmentListClick(ListItem item) {
        if (item != null) {
            startFragment(item.getClassName());
        }
    }

    private void startFragment(String className) {
        Log.d(TAG, "startFragment...className=" + className);
        Class<?> cls = null;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (cls == null) {
            toast("未找到" + className);
        } else {
            Fragment frag = null;
            try {
                Constructor c1 = cls.getDeclaredConstructor();
                c1.setAccessible(true);
                frag = (Fragment) c1.newInstance();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (frag == null) {
                toast("无法创建" + className);
            } else {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fl_container, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
    }

    private void toast(String msg) {
        Toast toast = Toast.makeText(MainActivity.this, "" + msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 200);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}