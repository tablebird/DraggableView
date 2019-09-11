package com.tablebird.drag.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tablebird.drag.DraggableView;

import java.util.ArrayList;
import java.util.List;

public class MainListActivity extends AppCompatActivity implements DraggableView.OnDragListener , View.OnClickListener {

    private DraggableView mDraggableView;
    private ListView mListView;
    private MyAdapter mMyAdapter;

    private static final String TIP_SUMMARY_MARK = "TIP_SUMMARY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        mDraggableView = findViewById(R.id.tip_summary);
        mDraggableView.setOnDragCompeteListener(this);
        mDraggableView.setMark(TIP_SUMMARY_MARK);
        mListView = findViewById(R.id.list_view);
        mMyAdapter = new MyAdapter(this);
        mListView.setAdapter(mMyAdapter);
        mMyAdapter.setDataList(initData());
        initTipSummary();
        findViewById(R.id.restore_button).setOnClickListener(this);
        findViewById(R.id.switch_button).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private List<Data> initData() {
        List<Data> dataList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dataList.add(new Data(String.valueOf(i), 1));
        }
        return dataList;
    }

    private void initTipSummary() {
        List<Data> dataList = mMyAdapter.getDataList();
        int unread = 0;
        for (Data data: dataList) {
            unread +=data.unread;
        }
        mDraggableView.setText(String.valueOf(unread));
        mDraggableView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDragComplete(DraggableView draggableView) {
    }

    @Override
    public void onDisappearComplete(DraggableView draggableView) {

        List<Data> dataList = mMyAdapter.getDataList();
        for (Data data: dataList) {
            data.unread = 0;
        }
        mMyAdapter.setDataList(dataList);
        mDraggableView.setText("0");
        mDraggableView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.restore_button) {
            mMyAdapter.setDataList(initData());
            initTipSummary();
        } else if (v.getId() == R.id.switch_button) {
            DraggableView.FollowMode followMode = mDraggableView.getFollowMode();
            mDraggableView.setFollowMode(followMode == DraggableView.FollowMode.SIMULTANEOUSLY ? DraggableView.FollowMode.TRAILING : DraggableView.FollowMode.SIMULTANEOUSLY);
        }
    }

    private class MyAdapter extends BaseAdapter {
        private Context mContext;

        private List<Data> mDataList;

        public MyAdapter(Context context) {
            mContext = context;
        }

        public void setDataList(List<Data> dataList) {
            mDataList = dataList;
            notifyDataSetChanged();
        }

        public List<Data> getDataList() {
            return mDataList;
        }

        @Override
        public int getCount() {
            return mDataList == null ? 0 : mDataList.size();
        }

        @Override
        public Data getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            final ViewHolder viewHolder;
            if (convertView == null) {
                view = View.inflate(mContext, R.layout.list_view_item, null);
                viewHolder = new ViewHolder();
                viewHolder.mTextView = view.findViewById(R.id.list_item_name);
                viewHolder.mDraggableView = view.findViewById(R.id.list_item_tip);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }
            final Data data = getItem(position);

            if (data.unread <= 0) {
                viewHolder.mDraggableView.setVisibility(View.GONE);
            } else {
                viewHolder.mDraggableView.setText(String.valueOf(data.unread));
                viewHolder.mDraggableView.setVisibility(View.VISIBLE);
            }
            viewHolder.mTextView.setText(data.name);
            viewHolder.mDraggableView.setLeaderMark(TIP_SUMMARY_MARK);
            viewHolder.mDraggableView.setSort(position);
            viewHolder.mDraggableView.setOnDragCompeteListener(new DraggableView.OnDragListener() {
                @Override
                public void onDragComplete(DraggableView draggableView) {
                }

                @Override
                public void onDisappearComplete(DraggableView draggableView) {
                    data.unread = 0;
                    viewHolder.mDraggableView.setVisibility(View.GONE);
                    initTipSummary();
                }
            });

            return view;
        }

        class ViewHolder {
            TextView mTextView;
            DraggableView mDraggableView;
        }
    }

    private static class Data {
        private String name;
        private int unread;

        Data(String name, int unread) {
            this.name = name;
            this.unread = unread;
        }
    }
}
