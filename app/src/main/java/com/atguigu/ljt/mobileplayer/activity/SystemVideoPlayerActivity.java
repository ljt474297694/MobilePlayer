package com.atguigu.ljt.mobileplayer.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.atguigu.ljt.mobileplayer.R;
import com.atguigu.ljt.mobileplayer.bean.MediaItem;
import com.atguigu.ljt.mobileplayer.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 视频播放Activity
 */
public class SystemVideoPlayerActivity extends Activity implements View.OnClickListener {
    private VideoView videoview;
    private Uri uri;
    private LinearLayout llTop;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystetime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwichePlayer;
    private LinearLayout llBottom;
    private TextView tvCurrenttime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnPre;
    private Button btnStartPause;
    private Button btnNext;
    private Button btnSwitchScreen;
    private static final int PROGRESS = 0;
    private static final int PROGRESSTIME = 1;
    private Utils timeUtil;
    private MyBroadcastRecevier recevier;
    private ArrayList<MediaItem> mediaItems;
    private int position;

    private void findViews() {
        setContentView(R.layout.activity_system_video_player);
        videoview = (VideoView) findViewById(R.id.videoview);
        llTop = (LinearLayout) findViewById(R.id.ll_top);
        tvName = (TextView) findViewById(R.id.tv_name);
        ivBattery = (ImageView) findViewById(R.id.iv_battery);
        tvSystetime = (TextView) findViewById(R.id.tv_systetime);
        btnVoice = (Button) findViewById(R.id.btn_voice);
        seekbarVoice = (SeekBar) findViewById(R.id.seekbar_voice);
        btnSwichePlayer = (Button) findViewById(R.id.btn_swiche_player);
        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        tvCurrenttime = (TextView) findViewById(R.id.tv_currenttime);
        seekbarVideo = (SeekBar) findViewById(R.id.seekbar_video);
        tvDuration = (TextView) findViewById(R.id.tv_duration);
        btnExit = (Button) findViewById(R.id.btn_exit);
        btnPre = (Button) findViewById(R.id.btn_pre);
        btnStartPause = (Button) findViewById(R.id.btn_start_pause);
        btnNext = (Button) findViewById(R.id.btn_next);
        btnSwitchScreen = (Button) findViewById(R.id.btn_switch_screen);
        btnVoice.setOnClickListener(this);
        btnSwichePlayer.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnPre.setOnClickListener(this);
        btnStartPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnSwitchScreen.setOnClickListener(this);
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS:
                    int currentProgress = videoview.getCurrentPosition();
                    seekbarVideo.setProgress(currentProgress);
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS, 500);
                    break;
                case PROGRESSTIME:
                    int currentTime = videoview.getCurrentPosition();
                    tvCurrenttime.setText(timeUtil.stringForTime(currentTime));
                    tvSystetime.setText(getSystemTime());
                    removeMessages(PROGRESSTIME);
                    sendEmptyMessageDelayed(PROGRESSTIME, 500);
                    break;
            }
        }
    };

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2017-01-09 12:51:14 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            // Handle clicks for btnVoice
        } else if (v == btnSwichePlayer) {
            // Handle clicks for btnSwichePlayer
        } else if (v == btnExit) {
            // Handle clicks for btnExit
        } else if (v == btnPre) {
            setPreVideo();
            // Handle clicks for btnPre
        } else if (v == btnStartPause) {
            if (videoview.isPlaying()) {
                videoview.pause();
                btnStartPause.setBackgroundResource(R.drawable.btn_start_selector);
            } else {
                videoview.start();
                btnStartPause.setBackgroundResource(R.drawable.btn_pause_selector);
            }
            // Handle clicks for btnStartPause
        } else if (v == btnNext) {
            setNextVideo();
            // Handle clicks for btnNext
        } else if (v == btnSwitchScreen) {
            // Handle clicks for btnSwitchScreen
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        initData();
        setListener();
        getData();
        setData();
    }

    private void initData() {
        timeUtil = new Utils();
        recevier = new MyBroadcastRecevier();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(recevier, filter);
    }

    class MyBroadcastRecevier extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            setBattery(level);
        }
    }

    private void setBattery(int level) {
        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        } else {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    private String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    private void setData() {
        if (mediaItems != null && mediaItems.size() > 0) {
            MediaItem mediaItem = mediaItems.get(position);
            tvName.setText(mediaItem.getName());
            videoview.setVideoPath(mediaItem.getData());
        } else if (uri != null) {
            videoview.setVideoURI(uri);
        }
    }

    private void setNextVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            position++;
            if (position < mediaItems.size()) {
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                videoview.setVideoPath(mediaItem.getData());
                checkButtonStatus();
            } else {
                position = mediaItems.size() - 1;
                Toast.makeText(SystemVideoPlayerActivity.this, "已经是最后一个了", Toast.LENGTH_SHORT).show();
            }
        } else if (uri != null) {
            finish();
        }
    }

    private void setPreVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            position--;
            if (position >= 0) {
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                videoview.setVideoPath(mediaItem.getData());
                checkButtonStatus();
            } else {
                position = 0;
            }
        }
    }

    private void checkButtonStatus() {
        setButtonEnable(true);
        if (position == 0) {
            btnPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnPre.setEnabled(false);
        } else if (position == mediaItems.size() - 1) {
            btnNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnNext.setEnabled(false);
        }

    }

    private void setButtonEnable(boolean isEnable) {
        if (isEnable) {
            btnNext.setBackgroundResource(R.drawable.btn_next_selector);
            btnPre.setBackgroundResource(R.drawable.btn_pre_selector);
        } else {
            btnNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnPre.setBackgroundResource(R.drawable.btn_pre_gray);
        }
        btnNext.setEnabled(isEnable);
        btnPre.setEnabled(isEnable);
    }

    public void getData() {
        uri = getIntent().getData();
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);
        checkButtonStatus();
    }

    private void setListener() {
        //调用系统的媒体控制器
//        videoview.setMediaController(new MediaController(this));
        seekbarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             *
             * @param seekBar
             * @param progress
             * @param fromUser 用户进度产生改变时返回ture 否则返回false
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoview.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeMessages(PROGRESS);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.sendEmptyMessage(PROGRESS);
            }
        });
        /**
         * 当播放器准备完成时调用
         */
        videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                int duration = videoview.getDuration();
                seekbarVideo.setMax(duration);
                tvDuration.setText(timeUtil.stringForTime(duration));
                handler.sendEmptyMessage(PROGRESS);
                handler.sendEmptyMessage(PROGRESSTIME);
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
                        setNextVideo();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (recevier != null) {
            unregisterReceiver(recevier);
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
