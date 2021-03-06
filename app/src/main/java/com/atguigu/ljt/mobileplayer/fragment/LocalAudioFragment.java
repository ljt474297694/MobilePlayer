package com.atguigu.ljt.mobileplayer.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.atguigu.ljt.mobileplayer.R;
import com.atguigu.ljt.mobileplayer.activity.SystemAudioPlayerActivity;
import com.atguigu.ljt.mobileplayer.adapter.LocalVideoAdapter;
import com.atguigu.ljt.mobileplayer.base.BaseFragment;
import com.atguigu.ljt.mobileplayer.bean.MediaItem;

import java.util.ArrayList;

/**
 * Created by 李金桐 on 2017/1/6.
 * QQ: 474297694
 * 功能: 本地音频播放的Fragment
 */

public class LocalAudioFragment extends BaseFragment {
    private ListView mListView;
    private TextView mTextView;
    private ArrayList<MediaItem> mediaItems;
    private LocalVideoAdapter adapter;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (mediaItems != null && mediaItems.size() > 0) {
                mTextView.setVisibility(View.GONE);
                adapter = new LocalVideoAdapter(mContext, mediaItems,false);
                mListView.setAdapter(adapter);
            } else {
                mTextView.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.fragment_local_video, null);
        mListView = (ListView) view.findViewById(R.id.listview);
        mTextView = (TextView) view.findViewById(R.id.tv_no_media);
        /**
         * 设置ListVeiw的点击监听 跳转到播放器页面 播放本地视频
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(mContext, SystemVideoPlayerActivity.class);
//                intent.setDataAndType(Uri.parse(mediaItems.get(position).getData()), "video/*");
//                startActivity(intent);
                Intent intent = new Intent(mContext, SystemAudioPlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("videolist",mediaItems);
                intent.putExtras(bundle);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    protected void initData() {
        super.initData();
        getDataFromLocal();
    }

    private void getDataFromLocal() {
        new Thread() {
            public void run() {
                mediaItems = new ArrayList<MediaItem>();
                ContentResolver resolver = mContext.getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ARTIST
                };
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(0).substring(0,cursor.getString(0).lastIndexOf("."));
                        long duration = cursor.getLong(1);
                        long size = cursor.getLong(2);
                        String data = cursor.getString(3);
                        String artist = cursor.getString(4);
                        MediaItem mediaItem = new MediaItem(name, duration, size, data, artist);
                        mediaItems.add(mediaItem);
                    }
                    cursor.close();
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    protected void onRequesData() {
    }


}
