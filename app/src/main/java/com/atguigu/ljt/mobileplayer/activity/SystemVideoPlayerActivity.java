package com.atguigu.ljt.mobileplayer.activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.atguigu.ljt.mobileplayer.R;

public class SystemVideoPlayerActivity extends Activity {
    private VideoView videoview;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_video_player);
        videoview = (VideoView) findViewById(R.id.videoview);
        setListener();
        getData();
        setData();
    }

    private void setData() {
        videoview.setVideoURI(uri);
    }

    public void getData() {
        uri = getIntent().getData();
    }

    private void setListener() {
        videoview.setMediaController(new MediaController(this));
        /**
         * 当播放器准备完成时调用
         */
        videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        /**
         * 播放出错时调用
         */
        videoview.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(SystemVideoPlayerActivity.this, "播放出错了", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        /**
         * 播放完成时调用
         */
        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(SystemVideoPlayerActivity.this, "播放完成", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


}
