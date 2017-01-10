package com.atguigu.ljt.mobileplayer.bean;

import java.io.Serializable;

/**
 * Created by 李金桐 on 2017/1/7.
 * QQ: 474297694
 * 功能: 视频音频信息对象
 */

public class MediaItem implements Serializable {
    String name;
    long duration;
    long size;
    String data;
    String artist;
    /**
     * 图片路径
     */
    String imageUrl;
    /**
     * +    * 描述
     */
    String desc;
    String heightUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getHeightUrl() {
        return heightUrl;
    }

    public void setHeightUrl(String heightUrl) {
        this.heightUrl = heightUrl;
    }

    public MediaItem(String name, long duration, long size, String data, String artist) {
        this.name = name;
        this.duration = duration;
        this.size = size;
        this.data = data;
        this.artist = artist;
    }
    public MediaItem() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String toString() {
        return "MediaItem{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", data='" + data + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }
}
