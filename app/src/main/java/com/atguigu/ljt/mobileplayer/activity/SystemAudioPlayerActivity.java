package com.atguigu.ljt.mobileplayer.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.atguigu.ljt.mobileplayer.service.MusicPlayerService;
import com.atguigu.ljt.mobileplayer.util.Utils;

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
                    service.openAudio(position);
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
    private MyReceiver receiver;
    private Utils utils;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2017-01-11 18:10:23 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        initData();
        startAndBindServide();
    }

    private void initData() {
        receiver = new MyReceiver();
        position = getIntent().getIntExtra("position", 0);
        IntentFilter intentFilter = new IntentFilter(MusicPlayerService.OPEN_COMPLETE);
        registerReceiver(receiver, intentFilter);
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

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            showViewData();
        }
    }

    private void showViewData() {
        try {
            if (service.getArtistName().equals("<unknown>")) {
                tvArtist.setText("");
            } else {
                tvArtist.setText(service.getArtistName());
            }
            tvName.setText(service.getAudioName());
            seekbarAudio.setMax(service.getDuration());
            handler.sendEmptyMessage(PROGRESS);
            handler.sendEmptyMessage(AUDIOTIME);
        } catch (RemoteException e) {
            e.printStackTrace();
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
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }
}
