package com.atguigu.ljt.mobileplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
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
    private LinearLayout ll_loading;
    private LinearLayout ll_buffer;
    private TextView tv_loading;
    private TextView tv_buffer;


    private static final int PROGRESS = 0;
    private static final int PROGRESSTIME = 1;
    private static final int HIDE_MEDIA_CONTROLLER = 2;
    private static final int HIDE_VOLUME_TEXTVIEW = 3;
    private static final int SHOW_NET_SPEED = 4;


    private Utils timeUtil;
    private MyBroadcastRecevier recevier;
    private ArrayList<MediaItem> mediaItems;
    private int position;
    private GestureDetector detector;
    private boolean isShowMediaController;
    private boolean isFullScreen = true;
    private int screenWidth;
    private int screenHeight;
    private int videoWidth;
    private int videoHeight;
    private long currentTime = 0;
    private AudioManager am;
    private int currentVolume;
    private int maxVolume;
    private boolean isMute;
    private boolean isNetUrl;
    private int prePosition;
    private float startY;
    private float startX;
    private float touchScreenHeight;
    private int startVolume;
    private Vibrator vibrator;
    /**
     * 用来保存 视频进度条开始拖动的进度
     */
    public int startSeekBerProgress;
    private int fromUserTemp;


    private void findViews() {
        setContentView(R.layout.activity_system_video_player);
        videoview = (VideoView) findViewById(R.id.videoview);
        tv_loading = (TextView) findViewById(R.id.tv_loading);
        tv_buffer = (TextView) findViewById(R.id.tv_buffer);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);
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
        isShowMediaController(true);
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
                case SHOW_NET_SPEED:
                    if (isNetUrl) {
                        String netSpeed = timeUtil.showNetSpeed(SystemVideoPlayerActivity.this);
                        tv_buffer.setText("缓冲中..." + netSpeed);
                        tv_loading.setText("正在加载中..." + netSpeed);
                        removeMessages(SHOW_NET_SPEED);
                        sendEmptyMessageDelayed(SHOW_NET_SPEED, 1000);
                    }
                    break;
                case PROGRESS:
                    int currentProgress = videoview.getCurrentPosition();
                    seekbarVideo.setProgress(currentProgress);
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS, 1000);


                    break;
                case PROGRESSTIME:
                    int currentTime = videoview.getCurrentPosition();
                    tvCurrenttime.setText(timeUtil.stringForTime(currentTime));
                    tvSystetime.setText(getSystemTime());

                    if (isNetUrl) {
                        int buffer = videoview.getBufferPercentage();
                        int bufferProgress = buffer * seekbarVideo.getMax() / 100;
                        seekbarVideo.setSecondaryProgress(bufferProgress);
                    }
                    if (isNetUrl && videoview.isPlaying()) {
                        int buffer = currentTime - prePosition;
                        if (buffer < 500) {
                            ll_buffer.setVisibility(View.VISIBLE);
                        } else {
                            ll_buffer.setVisibility(View.GONE);

                        }
                    }
                    prePosition = currentTime;
                    removeMessages(PROGRESSTIME);
                    sendEmptyMessageDelayed(PROGRESSTIME, 1000);
                    break;
                case HIDE_MEDIA_CONTROLLER:
                    isShowMediaController(false);
                    break;
                case HIDE_VOLUME_TEXTVIEW:
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
            showSwitchPlayerDialog();
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

    private void showSwitchPlayerDialog() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("当前播放使用系统播放器播放，当播放出现有声音没有画面的时候，请切换万能播放器")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startVitamioVideoPlayer();
                    }
                })
                .show();
    }

    /**
     * 切换视频播放和暂停的状态同时切换按钮图片
     */
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
//        Log.e("TAG", "SystemVideoPlayerActivity onCreate()");
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
        handler.sendEmptyMessage(SHOW_NET_SPEED);

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
            isNetUrl = timeUtil.isNetUrl(mediaItem.getData());
        } else if (uri != null) {
            videoview.setVideoURI(uri);
            setButtonEnable(false);
            tvName.setText(uri.toString().substring(uri.toString().lastIndexOf("/") + 1, uri.toString().lastIndexOf(".")));
            isNetUrl = timeUtil.isNetUrl(uri.toString());
        }
        ll_loading.setVisibility(View.VISIBLE);
    }

    private void setNextVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            position++;
            if (position < mediaItems.size()) {
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                videoview.setVideoPath(mediaItem.getData());
                isNetUrl = timeUtil.isNetUrl(mediaItem.getData());
                checkButtonStatus();
            } else {
                position = -1;
                setNextVideo();
            }
            ll_loading.setVisibility(View.VISIBLE);
        } else if (uri != null) {
            isNetUrl = timeUtil.isNetUrl(uri.toString());
            startAndPause();
        }
    }

    private void setPreVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            position--;
            if (position >= 0) {
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                videoview.setVideoPath(mediaItem.getData());
                isNetUrl = timeUtil.isNetUrl(mediaItem.getData());
                checkButtonStatus();
            } else {
                position = 0;
            }
            ll_loading.setVisibility(View.VISIBLE);
        } else if (uri != null) {
            isNetUrl = timeUtil.isNetUrl(uri.toString());
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            videoview.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//                @Override
//                public boolean onInfo(MediaPlayer mp, int what, int extra) {
//                    switch (what) {
//                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
//                            ll_buffer.setVisibility(View.VISIBLE);
//                            break;
//                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
//                            ll_buffer.setVisibility(View.GONE);
//                            break;
//                    }
//                    return true;
//                }
//            });
        }
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
                    isShowMediaController(!isShowMediaController);
                } else {
                    isShowMediaController(!isShowMediaController);
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
                if (fromUser && Math.abs(progress - startSeekBerProgress - fromUserTemp * 1000)/1000 > 1) {
                    fromUserTemp++;
                    if (fromUserTemp > 2) {
                        if (progress  > startSeekBerProgress) {
                            tv_volume.setText("快进--> " + Math.abs(progress  - startSeekBerProgress)/ 1000 + "秒");
                            handler.removeMessages(HIDE_VOLUME_TEXTVIEW);
                            tv_volume.setVisibility(View.VISIBLE);
                        } else {
                            tv_volume.setText("后退<-- " + Math.abs(progress  - startSeekBerProgress)/ 1000 + "秒");
                            handler.removeMessages(HIDE_VOLUME_TEXTVIEW);
                            tv_volume.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                startSeekBerProgress = seekBar.getProgress();
                handler.removeMessages(PROGRESS);
                handler.removeMessages(HIDE_MEDIA_CONTROLLER);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                fromUserTemp = 0;
                videoview.seekTo(seekBar.getProgress());
                handler.sendEmptyMessage(PROGRESS);
                handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
                handler.sendEmptyMessage(HIDE_VOLUME_TEXTVIEW);
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
                ll_loading.setVisibility(View.GONE);
//                mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
//                    @Override
//                    public void onSeekComplete(MediaPlayer mp) {
//                        Toast.makeText(SystemVideoPlayerActivity.this, "拖动完成了", Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
        });
        /**
         * 播放出错时调用
         */
        videoview.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (isError) {
                    isError = false;
                    startVitamioVideoPlayer();
                }
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

    private boolean isError = true;

    private void startVitamioVideoPlayer() {
        if (videoview != null) {
            videoview.stopPlayback();
        }
        Intent intent = new Intent(SystemVideoPlayerActivity.this, VitamioVideoPlayerActivity.class);
        if (mediaItems != null && mediaItems.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);
        } else if (uri != null) {
            intent.setDataAndType(uri, "video/*");
        }
        startActivity(intent);
        finish();
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
                tv_volume.setText("音量: " + progress * 100 / maxVolume + "%");
            } else {
                isMute = !isMute;
                am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                seekbarVoice.setProgress(0);
                tv_volume.setText("静音模式");
            }
        } else {
            if (progress <= 0) {
                isMute = true;
            } else {
                isMute = false;
            }
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVoice.setProgress(progress);
            tv_volume.setText("音量: " + progress * 100 / maxVolume + "%");
        }
        handler.removeMessages(HIDE_VOLUME_TEXTVIEW);
        handler.sendEmptyMessageDelayed(HIDE_VOLUME_TEXTVIEW, 1000);
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


    /**
     * @param isShow 是否显示媒体控制面板
     */
    private void isShowMediaController(boolean isShow) {
        if (isShow) {
            isShowMediaController = true;
            llBottom.setVisibility(View.VISIBLE);
            llTop.setVisibility(View.VISIBLE);
            handler.removeMessages(HIDE_MEDIA_CONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
        } else {
            isShowMediaController = false;
            llBottom.setVisibility(View.GONE);
            llTop.setVisibility(View.GONE);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                startX = event.getX();
                touchScreenHeight = Math.min(screenHeight, screenWidth);
                startVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                break;
            case MotionEvent.ACTION_MOVE:
                float endY = event.getY();
                float distanceY = startY - endY;
                if (startX > screenWidth / 2) {
                    float tempVolume = ((distanceY / touchScreenHeight) * maxVolume);
                    int volume = (int) Math.min(Math.max(startVolume + tempVolume, 0), maxVolume);
                    if (Math.abs(tempVolume) > 1) {
                        updateVoice(volume, false);
                    }
                } else {
                    //左边屏幕--改变亮度
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (startY - endY > FLING_MIN_DISTANCE
                            && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(20);
                    }
                    if (startY - endY < FLING_MIN_DISTANCE
                            && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(-20);
                    }

                }

                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return true;
    }

    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        // if (lp.screenBrightness <= 0.1) {
        // return;
        // }
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200}; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200}; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        }
        getWindow().setAttributes(lp);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVolume--;
            if (currentVolume < 0) {
                currentVolume = 0;
            }
            updateVoice(currentVolume, false);
            handler.removeMessages(HIDE_MEDIA_CONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVolume++;
            if (currentTime > maxVolume) {
                currentTime = maxVolume;
            }
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
