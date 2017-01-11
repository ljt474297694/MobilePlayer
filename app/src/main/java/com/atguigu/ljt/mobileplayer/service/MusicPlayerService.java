package com.atguigu.ljt.mobileplayer.service;

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
import com.atguigu.ljt.mobileplayer.bean.MediaItem;

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
    public static final String OPEN_COMPLETE = "open_complete";
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
    };
    private ArrayList<MediaItem> mediaItems;
    private int position;
    private MediaItem mediaItem;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
            this.position = position;
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer = null;
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    start();
                    notifyChange(OPEN_COMPLETE);
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    next();
                    return true;
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    next();
                }
            });
            try {
                mediaPlayer.setDataSource(mediaItem.getData());
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (!isLoaded) {
            Toast.makeText(this, "没有加载完成", Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyChange(String action) {
        Intent intent = new Intent(action);
        //发广播
        sendBroadcast(intent);
    }

    /**
     * 开始播放音频
     */
    void start() {
        mediaPlayer.start();
    }

    /**
     * 暂停
     */
    void pause() {
        mediaPlayer.pause();
    }

    /**
     * 得到歌曲的名称
     */
    String getAudioName() {
        return mediaItems.get(position).getName();
    }

    /**
     * 得到歌曲演唱者的名字
     */
    String getArtistName() {
        return mediaItems.get(position).getArtist();
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
        position++;
        if (position >= mediaItems.size()) {
            position = 0;
        }
        openAudio(position);
    }

    /**
     * 播放上一首歌曲
     */
    void pre() {
        position--;
        if (position <= 0) {
            position = mediaItems.size() - 1;
        }
        openAudio(position);
    }

    /**
     * 得到播放模式
     */
    int getPlayMode() {
        return 0;
    }

    /**
     * 设置播放模式
     */
    void setPlayMode(int mode) {
    }

    boolean isPlaying() {
        return false;
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
