package com.atguigu.ljt.mobileplayer.bean;

/**
 * Created by 李金桐 on 2017/1/13.
 * QQ: 474297694
 * 功能: 每一条歌词的bean
 */

public class LyricBean {
    /**
     * 歌词内容
     */
    private String content;

    /**
     * 时间戳
     */
    private long timePoint;
    /**
     * 高亮时间
     */
    private long sleepTime;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(long timePoint) {
        this.timePoint = timePoint;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public String toString() {
        return "LyricBean{" +
                "content='" + content + '\'' +
                ", timePoint=" + timePoint +
                ", sleepTime=" + sleepTime +
                '}';
    }
}

