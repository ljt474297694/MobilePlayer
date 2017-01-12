package com.atguigu.ljt.mobileplayer.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
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
import com.atguigu.ljt.mobileplayer.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private int position;
    private static final int PROGRESS = 0;
    private static final int AUDIOTIME = 1;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
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

    private void findViews() {
        setContentView(R.layout.activity_system_audio_player);
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
     *
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showViewData(MediaItem mediaItem) {
            if (mediaItem.getArtist().equals("<unknown>")) {
                tvArtist.setText("");
            } else {
                tvArtist.setText(mediaItem.getArtist());
            }
            tvName.setText(mediaItem.getName());
            seekbarAudio.setMax((int) mediaItem.getDuration());
            handler.sendEmptyMessage(PROGRESS);
            handler.sendEmptyMessage(AUDIOTIME);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ResetStartButton(Integer action){
        if(action==MusicPlayerService.NORMAL_STOP) {
            btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
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
