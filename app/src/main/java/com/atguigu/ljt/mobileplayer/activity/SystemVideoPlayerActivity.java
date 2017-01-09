package com.atguigu.ljt.mobileplayer.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.atguigu.ljt.mobileplayer.R;
import com.atguigu.ljt.mobileplayer.bean.MediaItem;
import com.atguigu.ljt.mobileplayer.util.Utils;
import com.atguigu.ljt.mobileplayer.view.VideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 李金桐 on 2017/1/7.
 * QQ: 474297694
 * 功能: 简单的自定义播放器 使用VideoView
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
    private TextView tv_volume;

    private static final int PROGRESS = 0;
    private static final int PROGRESSTIME = 1;
    private static final int HIDE_MEDIA_CONTROLLER = 2;
    private static final int HiDE_VOLUME_TEXTVIEW = 3;

    private Utils timeUtil;
    private MyBroadcastRecevier recevier;
    private ArrayList<MediaItem> mediaItems;
    private int position;
    private GestureDetector detector;
    private boolean isShowMediaController = false;
    private boolean isFullScreen = true;
    private int screenWidth;
    private int screenHeight;
    private int videoWidth;
    private int videoHeight;
    private long currentTime = 0;
    private AudioManager am;
    private int currentVolume;
    private int maxVolume;
    private boolean isMute = false;


    private void findViews() {
        setContentView(R.layout.activity_system_video_player);
        videoview = (VideoView) findViewById(R.id.videoview);
        tv_volume = (TextView) findViewById(R.id.tv_volume);
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
        hideMediaController();
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        seekbarVoice.setMax(maxVolume);
        seekbarVoice.setProgress(currentVolume);
        tv_volume.setVisibility(View.GONE);
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
                case HIDE_MEDIA_CONTROLLER:
                    hideMediaController();
                    break;
                case HiDE_VOLUME_TEXTVIEW:
                    tv_volume.setVisibility(View.GONE);
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
            updateVoice(currentVolume, true);
        } else if (v == btnSwichePlayer) {
            // Handle clicks for btnSwichePlayer
        } else if (v == btnExit) {
            twoSecondFinish();
        } else if (v == btnPre) {
            setPreVideo();
            // Handle clicks for btnPre
        } else if (v == btnStartPause) {
            startAndPause();
            // Handle clicks for btnStartPause
        } else if (v == btnNext) {
            setNextVideo();
            // Handle clicks for btnNext
        } else if (v == btnSwitchScreen) {
            setVideoType();
        }
        handler.removeMessages(HIDE_MEDIA_CONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
    }

    private void startAndPause() {
        if (videoview.isPlaying()) {
            videoview.pause();
            btnStartPause.setBackgroundResource(R.drawable.btn_start_selector);
        } else {
            videoview.start();
            btnStartPause.setBackgroundResource(R.drawable.btn_pause_selector);
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
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;

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
                position = -1;
                setNextVideo();
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
        seekbarVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateVoice(progress, false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeMessages(HIDE_MEDIA_CONTROLLER);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
            }
        });
        /**
         * 创建手势识别器对象并设置对应需要操作的监听
         * 此处为 单击 双击 长按
         */
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                startAndPause();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                setVideoType();
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isShowMediaController) {
                    hideMediaController();
                } else {
                    showMediaController();
                }
                return super.onSingleTapConfirmed(e);
            }


        });
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
                handler.removeMessages(HIDE_MEDIA_CONTROLLER);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.sendEmptyMessage(PROGRESS);
                handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
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
                videoWidth = mp.getVideoWidth();
                videoHeight = mp.getVideoHeight();
                setVideoType();
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

    /**
     * @param progress       progress的进度和音量同步 可以完美控制音量
     * @param isConsiderMute 是否考虑静音
     */
    private void updateVoice(int progress, boolean isConsiderMute) {
        if (isConsiderMute) {
            if (isMute) {
                isMute = !isMute;
                am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                seekbarVoice.setProgress(progress);
                tv_volume.setText(progress * 100 / maxVolume+"%");
            } else {
                isMute = !isMute;
                am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                seekbarVoice.setProgress(0);
                tv_volume.setText(0+"%");
            }
        } else {
            if (progress <= 0) {
                isMute = true;
            } else {
                isMute = false;
            }
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVoice.setProgress(progress);
            tv_volume.setText(progress * 100 / maxVolume+"%");
        }
        handler.removeMessages(HiDE_VOLUME_TEXTVIEW);
        handler.sendEmptyMessageDelayed(HiDE_VOLUME_TEXTVIEW,4000);
        tv_volume.setVisibility(View.VISIBLE);
        currentVolume = progress;
    }

    /**
     * 将屏幕状态改变 如果是全屏就变回默认 默认就变回全屏
     */
    private void setVideoType() {
        if (isFullScreen) {
            isFullScreen = !isFullScreen;
            btnSwitchScreen.setBackgroundResource(R.drawable.btn_full_screen_selector);
            int mVideoWidth = videoWidth;
            int mVideoHeight = videoHeight;
            int height = screenHeight;
            int width = screenWidth;
            if (mVideoWidth * height < width * mVideoHeight) {
                width = height * mVideoWidth / mVideoHeight;
            } else if (mVideoWidth * height > width * mVideoHeight) {
                height = width * mVideoHeight / mVideoWidth;
            }
            videoview.setVideoSize(width, height);
        } else {
            isFullScreen = !isFullScreen;
            btnSwitchScreen.setBackgroundResource(R.drawable.btn_default_screen_selector);
            videoview.setVideoSize(screenWidth, screenHeight);
        }
    }


    private void showMediaController() {
        isShowMediaController = true;
        llBottom.setVisibility(View.VISIBLE);
        llTop.setVisibility(View.VISIBLE);
        handler.removeMessages(HIDE_MEDIA_CONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
    }

    private void hideMediaController() {
        isShowMediaController = false;
        llBottom.setVisibility(View.GONE);
        llTop.setVisibility(View.GONE);
    }

    private float startY;
    private float touchScreenHeight;
    private int startVolume;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                touchScreenHeight = Math.min(screenHeight, screenWidth);
                startVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                break;
            case MotionEvent.ACTION_MOVE:
                float endY = event.getY();
                float distanceY = startY - endY;
                float tempVolume = ((distanceY / touchScreenHeight) * maxVolume);
                int volume = (int) Math.min(Math.max(startVolume + tempVolume, 0), maxVolume);
                if (tempVolume != 0) {
                    updateVoice(volume, false);
                }
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return true;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVolume--;
            updateVoice(currentVolume, false);
            handler.removeMessages(HIDE_MEDIA_CONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVolume++;
            updateVoice(currentVolume, false);
            handler.removeMessages(HIDE_MEDIA_CONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        twoSecondFinish();
    }

    private void twoSecondFinish() {
        if (System.currentTimeMillis() - currentTime > 2000) {
            Toast.makeText(SystemVideoPlayerActivity.this, "在按一次退出", Toast.LENGTH_SHORT).show();
            currentTime = System.currentTimeMillis();
        } else {
            finish();
        }
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
