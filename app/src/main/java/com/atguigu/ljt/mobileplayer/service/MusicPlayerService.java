package com.atguigu.ljt.mobileplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.atguigu.ljt.mobileplayer.IMusicPlayerService;
import com.atguigu.ljt.mobileplayer.R;
import com.atguigu.ljt.mobileplayer.activity.SystemAudioPlayerActivity;
import com.atguigu.ljt.mobileplayer.bean.MediaItem;
import com.atguigu.ljt.mobileplayer.util.CacheUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by 李金桐 on 2017/1/11.
 * QQ: 474297694
 * 功能: 音乐播放器服务
 */

public class MusicPlayerService extends Service {
    private MediaPlayer mediaPlayer;
    private boolean isLoaded;
    public static final Integer NORMAL_STOP = -1;
    /**
     * 三种播放模式
     */
    public static final int NORMAL = 0;
    public static final int ALL = 1;
    public static final int SINGLE = 2;
    private ArrayList<MediaItem> mediaItems;
    private int mPosition;
    private MediaItem mediaItem;
    /**
     * 默认播放模式
     */
    private int mode = NORMAL;
    IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {
        MusicPlayerService service = MusicPlayerService.this;

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public String getAudioName() throws RemoteException {
            return service.getAudioName();
        }

        @Override
        public String getArtistName() throws RemoteException {
            return service.getArtistName();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public void setPlayMode(int mode) throws RemoteException {
            service.setPlayMode(mode);
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            if (mediaPlayer != null) {
                return mediaPlayer.isPlaying();
            }
            return false;
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(position);
            }
        }

        @Override
        public void notifyChange() throws RemoteException {
            service.notifyChange();
        }



    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mode = CacheUtil.getPlayMode(this,"playmode");
        getDataFromLocal();
    }

    /**
     * 根据位置打开一个音频并且播放
     *
     * @param position
     */
    void openAudio(int position) {
        if (mediaItems != null && mediaItems.size() > 0) {
            mediaItem = mediaItems.get(position);
            this.mPosition = position;
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer = null;
            }
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(mediaItem.getData());
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    start();
                    notifyChange();
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return true;
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    ModePlay();

                }
            });


        } else if (!isLoaded) {
            Toast.makeText(this, "没有加载完成", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 当每一首播放完成的时候 根据模式判断如何播放下一首
     */
    private void ModePlay() {
        switch (mode) {
            case NORMAL:
                if (mPosition >= mediaItems.size() - 1) {
                    EventBus.getDefault().post(NORMAL_STOP);
                } else {
                    openAudio(mPosition + 1);
                }
                break;
            case ALL:
                next();
                break;
            case SINGLE:
                openAudio(mPosition);
                break;
        }
    }

    public void notifyChange() {
        EventBus.getDefault().post(mediaItem);
    }

    private NotificationManager nm;

    /**
     * 开始播放音频
     */
    void start() {
        mediaPlayer.start();


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
        Notification notification = null;
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Intent intent = new Intent(this, SystemAudioPlayerActivity.class);
            PendingIntent pendingintent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.notification_music_playing)
                    .setContentText("正在播放:"+getAudioName())
                    .setContentTitle("321音乐")
                    .setContentIntent(pendingintent)
                    .build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            nm.notify(1, notification);
        }

    }

    /**
     * 暂停
     */
    void pause() {
        mediaPlayer.pause();
        nm.cancel(1);
    }

    /**
     * 得到歌曲的名称
     */
    String getAudioName() {
        return mediaItems.get(mPosition).getName();
    }

    /**
     * 得到歌曲演唱者的名字
     */
    String getArtistName() {
        return mediaItems.get(mPosition).getArtist();
    }

    /**
     * 得到歌曲的当前播放进度
     */
    int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 得到歌曲的当前总进度
     */
    int getDuration() {
        return mediaPlayer.getDuration();
    }

    /**
     * 播放下一首歌曲
     */
    void next() {
        mPosition++;
        if (mPosition >= mediaItems.size()) {
            mPosition = 0;
        }
        openAudio(mPosition);
    }

    /**
     * 播放上一首歌曲
     */
    void pre() {
        mPosition--;
        if (mPosition < 0) {
            mPosition = mediaItems.size() - 1;
        }
        openAudio(mPosition);
    }

    /**
     * 得到播放模式
     */
    int getPlayMode() {
        return mode;
    }

    /**
     * 设置播放模式
     */
    void setPlayMode(int mode) {
        this.mode = mode;
        CacheUtil.putPlayMode(this,"playmode",mode);
    }


    private void getDataFromLocal() {
        new Thread() {
            public void run() {
                mediaItems = new ArrayList<MediaItem>();
                ContentResolver resolver = getContentResolver();
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
                        String name = cursor.getString(0).substring(0, cursor.getString(0).lastIndexOf("."));
                        long duration = cursor.getLong(1);
                        long size = cursor.getLong(2);
                        String data = cursor.getString(3);
                        String artist = cursor.getString(4);
                        MediaItem mediaItem = new MediaItem(name, duration, size, data, artist);
                        mediaItems.add(mediaItem);
                    }
                    cursor.close();
                }
                isLoaded = true;
            }
        }.start();
    }


}
