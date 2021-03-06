package com.atguigu.ljt.mobileplayer.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.atguigu.ljt.mobileplayer.IMusicPlayerService;
import com.atguigu.ljt.mobileplayer.R;
import com.atguigu.ljt.mobileplayer.bean.MediaItem;
import com.atguigu.ljt.mobileplayer.service.MusicPlayerService;
import com.atguigu.ljt.mobileplayer.util.LyricParaser;
import com.atguigu.ljt.mobileplayer.util.Utils;
import com.atguigu.ljt.mobileplayer.view.BaseVisualizerView;
import com.atguigu.ljt.mobileplayer.view.LyricShowView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

public class SystemAudioPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivIcon;
    private TextView tvArtist;
    private TextView tvName;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnAudioPlaymode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private Button btnSwichLyric;
    private LyricShowView lyric_show_view;
    private BaseVisualizerView basevisualizerview;
    private int position;
    private static final int PROGRESS = 0;
    private static final int AUDIOTIME = 1;
    private static final int SHOW_LYRIC = 2;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_LYRIC:
                    try {
                        int currentPosition = service.getCurrentPosition();
                        lyric_show_view.setNextShowLyric(currentPosition);
                        removeMessages(SHOW_LYRIC);
                        sendEmptyMessageDelayed(SHOW_LYRIC, 50);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case AUDIOTIME:
                    try {
                        int currentPosition = service.getCurrentPosition();
                        tvTime.setText(utils.stringForTime(currentPosition) + "/" + utils.stringForTime(service.getDuration()));

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    removeMessages(AUDIOTIME);
                    sendEmptyMessageDelayed(AUDIOTIME, 1000);
                    break;
                case PROGRESS:
                    try {
                        seekbarAudio.setProgress(service.getCurrentPosition());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
            }
        }
    };
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            service = IMusicPlayerService.Stub.asInterface(iBinder);
            if (service != null) {
                try {
                    if (notification) {
                        service.notifyChange();
                    } else {
                        service.openAudio(position);
                    }
                    showButtonState();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private IMusicPlayerService service;
    private Utils utils;
    private boolean notification;
    private Visualizer mVisualizer;

    private void findViews() {
        setContentView(R.layout.activity_system_audio_player);
        basevisualizerview = (BaseVisualizerView)findViewById(R.id.basevisualizerview);
        lyric_show_view = (LyricShowView) findViewById(R.id.lyric_show_view);
        ivIcon = (ImageView) findViewById(R.id.iv_icon);
        tvArtist = (TextView) findViewById(R.id.tv_artist);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvTime = (TextView) findViewById(R.id.tv_time);
        seekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        btnAudioPlaymode = (Button) findViewById(R.id.btn_audio_playmode);
        btnAudioPre = (Button) findViewById(R.id.btn_audio_pre);
        btnAudioStartPause = (Button) findViewById(R.id.btn_audio_start_pause);
        btnAudioNext = (Button) findViewById(R.id.btn_audio_next);
        btnSwichLyric = (Button) findViewById(R.id.btn_swich_lyric);

        btnAudioPlaymode.setOnClickListener(this);
        btnAudioPre.setOnClickListener(this);
        btnAudioStartPause.setOnClickListener(this);
        btnAudioNext.setOnClickListener(this);
        btnSwichLyric.setOnClickListener(this);
        ivIcon.setBackgroundResource(R.drawable.animation_list);
        AnimationDrawable drawable = (AnimationDrawable) ivIcon.getBackground();
        drawable.start();
    }

    @Override
    public void onClick(View v) {
        if (v == btnAudioPlaymode) {
            changePlayMode();
        } else if (v == btnAudioPre) {
            try {
                service.pre();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == btnAudioStartPause) {
            try {
                if (service.isPlaying()) {
                    service.pause();
                    btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                } else {
                    service.start();
                    btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == btnAudioNext) {
            try {
                service.next();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == btnSwichLyric) {
            // Handle clicks for btnSwichLyric
        }
    }

    private void changePlayMode() {
        try {
            int mode = service.getPlayMode();
            switch (mode) {
                case MusicPlayerService.NORMAL:
                    service.setPlayMode(MusicPlayerService.ALL);
                    break;
                case MusicPlayerService.ALL:
                    service.setPlayMode(MusicPlayerService.SINGLE);
                    break;
                case MusicPlayerService.SINGLE:
                    service.setPlayMode(MusicPlayerService.NORMAL);
                    break;
            }
            showButtonState();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据音乐服务当前的模式进行设置对应的图片
     */
    private void showButtonState() {
        try {
            int mode = service.getPlayMode();
            switch (mode) {
                case MusicPlayerService.NORMAL:
                    btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
                    break;
                case MusicPlayerService.ALL:
                    btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
                    break;
                case MusicPlayerService.SINGLE:
                    btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViews();
        getData();
        initData();
        startAndBindServide();
    }

    private void initData() {

        utils = new Utils();

        seekbarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeMessages(PROGRESS);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    service.seekTo(seekBar.getProgress());
                    handler.sendEmptyMessage(PROGRESS);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void getData() {
        notification = getIntent().getBooleanExtra("notification", false);
        if (!notification) {
            position = getIntent().getIntExtra("position", 0);
        }
    }

    /**
     * EventBus订阅者 可以轻松优雅的进行数据传输
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showViewData(MediaItem mediaItem) {
        setupVisualizerFxAndUi();
        if (mediaItem.getArtist().equals("<unknown>")) {
            tvArtist.setText("");
        } else {
            tvArtist.setText(mediaItem.getArtist());
        }
        tvName.setText(mediaItem.getName());
        seekbarAudio.setMax((int) mediaItem.getDuration());
        String path = mediaItem.getData();
        path = path.substring(0, path.lastIndexOf("."));
        File file = new File(path + ".lrc");
        if (!file.exists()) {
            file = new File(path + ".txt");
        }
        LyricParaser lyricParaser = new LyricParaser();
        lyricParaser.readFile(file);

        if (lyricParaser.isExistsLyric()) {
            lyric_show_view.setLyric(lyricParaser.getLyricBeens());
            handler.sendEmptyMessage(SHOW_LYRIC);
        }else{
            lyric_show_view.setLyric(lyricParaser.getLyricBeens());
        }


        handler.sendEmptyMessage(PROGRESS);
        handler.sendEmptyMessage(AUDIOTIME);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ResetStartButton(Integer action) {
        if (action == MusicPlayerService.NORMAL_STOP) {
            btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
        }
    }
    /**
     * 生成一个VisualizerView对象，使音频频谱的波段能够反映到 VisualizerView上
     */
    private void setupVisualizerFxAndUi() {

        int audioSessionid = 0;
        try {
            audioSessionid = service.getAudioSessionId();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("audioSessionid==" + audioSessionid);
        mVisualizer = new Visualizer(audioSessionid);
        // 参数内必须是2的位数
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        // 设置允许波形表示，并且捕获它
        basevisualizerview.setVisualizer(mVisualizer);
        mVisualizer.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            mVisualizer.release();
        }
    }
    private void startAndBindServide() {
        Intent intent = new Intent(this, MusicPlayerService.class);

        bindService(intent, conn, BIND_ABOVE_CLIENT);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        if (conn != null) {
            unbindService(conn);
        }

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


}
