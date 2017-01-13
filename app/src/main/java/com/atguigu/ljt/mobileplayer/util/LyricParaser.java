package com.atguigu.ljt.mobileplayer.util;

import com.atguigu.ljt.mobileplayer.bean.LyricBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by 李金桐 on 2017/1/13.
 * QQ: 474297694
 * 功能: 歌词解析工具类
 */

public class LyricParaser {
    private ArrayList<LyricBean> lyricBeens;
    private boolean isExistsLyric;

    public ArrayList<LyricBean> getLyricBeens() {
        return lyricBeens;
    }

    public boolean isExistsLyric() {
        return isExistsLyric;
    }

    public void readFile(File file) {
        if (file == null || !file.exists()) {
            lyricBeens = null;
            isExistsLyric = false;
        } else {
            lyricBeens = new ArrayList<>();
            isExistsLyric = true;
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
                String line;
                while ((line = br.readLine()) != null) {
                    analyzeLyrc(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            Collections.sort(lyricBeens, new Comparator<LyricBean>() {
                @Override
                public int compare(LyricBean o1, LyricBean o2) {

                    return (int) (o1.getTimePoint() - o2.getTimePoint());
                }
            });

            for (int i = 0; i < lyricBeens.size(); i++) {
                LyricBean one = lyricBeens.get(i);

                if (i + 1 < lyricBeens.size()) {
                    LyricBean two = lyricBeens.get(i + 1);
                    one.setSleepTime(two.getTimePoint() - one.getTimePoint());
                }

            }
        }

    }

    private void analyzeLyrc(String line) {
        int pos1 = line.indexOf("[");
        int pos2 = line.indexOf("]");
        if (pos1 == 0 && pos2 != -1) {
            long[] timeLongs = new long[getCountTag(line)];
            String timeStr = line.substring(pos1 + 1, pos2);
            timeLongs[0] = strTime2Long(timeStr);

            if (timeLongs[0] == -1) {
                return;
            }
            int i = 1;
            String content = line;
            while (pos1 == 0 && pos2 != -1) {
                content = content.substring(pos2 + 1);
                pos1 = content.indexOf("[");
                pos2 = content.indexOf("]");

                if (pos2 != -1) {
                    timeStr = content.substring(pos1 + 1, pos2);
                    timeLongs[i] = strTime2Long(timeStr);
                    if (timeLongs[i] == -1) {
                        return;
                    }
                    i++;
                }
            }

            LyricBean lyricBean =new LyricBean();
            for (int i1 = 0; i1 < timeLongs.length; i1++) {
                if(timeLongs[i1] != 0) {
                    lyricBean.setContent(content);
                    lyricBean.setTimePoint(timeLongs[i1]);
                    lyricBeens.add(lyricBean);
                    lyricBean = new LyricBean();
                }
            }
        }

    }

    private long strTime2Long(String timeStr) {
        long time = -1;
        try {
            //1.根据":"切成02和04.12
            String[] s1 = timeStr.split(":");
            //2.根据“.”把04.12切成04和12
            String[] s2 = s1[1].split("\\.");
            //3.转换成long类型的毫秒时间
            //分
            long min = Long.valueOf(s1[0]);//02

            //秒
            long second = Long.valueOf(s2[0]);//04

            //毫秒
            long mil = Long.valueOf(s2[1]);//12

            time = min * 60 * 1000 + second * 1000 + mil * 10;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return time;
    }

    /**
     * @param line
     * @return 根据split方法分割字符串取最大的值用来创建数组
     */
    private int getCountTag(String line) {
        String[] left = line.split("\\[");
        String[] right = line.split("\\[");
        if (left.length == 0 && right.length == 0) {
            return 1;
        } else {
            return left.length > right.length ? left.length : right.length;
        }
    }
}
