package com.test.machineinfo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


import com.test.machineinfo.R;
import com.test.machineinfo.adapter.ListAdapter;
import com.test.machineinfo.data.ListItem;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentListClickListener} interface
 * to handle interaction events.
 * Use the {@link FragmentList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentList extends Fragment {
    private static final String TAG = FragmentList.class.getSimpleName();

    private static final String ARG_PARAM1 = "param_list";
    private ArrayList<ListItem> mList;

    private OnFragmentListClickListener mListener;

    private ListView mListView;

    public FragmentList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mList Parameter 1.
     * @return A new instance of fragment FragmentList.
     */
    public static FragmentList newInstance(ArrayList<ListItem> mList) {
        FragmentList fragment = new FragmentList();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1, mList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        if (context instanceof OnFragmentListClickListener) {
            mListener = (OnFragmentListClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentListClickListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mList = getArguments().getParcelableArrayList(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View root = inflater.inflate(R.layout.fragment_list, container, false);
        mListView = (ListView) root.findViewById(R.id.lv_list);
        mListView.setAdapter(new ListAdapter(getActivity(), mList));
        mListView.setOnItemClickListener(mOnItemClickListener);
        return root;
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mListener != null && mList != null) {
                mListener.onFragmentListClick(mList.get(position));
            }
        }
    };

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        mListView.requestFocus();
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

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        super.onDetach();
        mListener = null;
        mList = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentListClickListener {
        void onFragmentListClick(ListItem item);
    }
}
