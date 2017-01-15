package com.atguigu.ljt.mobileplayer.util;

import com.atguigu.ljt.mobileplayer.bean.LyricBean;

import java.io.BufferedInputStream;
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
            isExistsLyric = false;
            lyricBeens = null;
            return;
        } else {
            lyricBeens = new ArrayList<>();
            isExistsLyric = true;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), getCharset(file)));
                String line;
                while ((line = reader.readLine()) != null) {
                    analyzeLyrc(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Collections.sort(lyricBeens, new Comparator<LyricBean>() {
            @Override
            public int compare(LyricBean o1, LyricBean o2) {
                if(o1.getTimePoint()>o2.getTimePoint()) {
                    return 1;
                }else if(o1.getTimePoint()<o2.getTimePoint()) {
                    return -1;
                }else{
                    return 0;
                }
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


    private void analyzeLyrc(String line) {

        int pos1 = line.indexOf("[");
        int pos2 = line.indexOf("]");
        if (pos1 == 0 && pos2 != -1) {
        long[] timeLongs = new long[getCountTag(line)];
        String timeStr = line.substring(pos1+1,pos2);
        timeLongs[0] = strTime2Long(timeStr);
        if(timeLongs[0] == -1) {
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

            LyricBean lyricBean;
            for(int i1 = 0; i1 <timeLongs.length ; i1++) {
              if(timeLongs[i1]!=0) {
                  lyricBean = new LyricBean();
                  lyricBean.setContent(content);
                  lyricBean.setTimePoint(timeLongs[i1]);
                  lyricBeens.add(lyricBean);
              }
            }
        }

    }

    private long strTime2Long(String timeStr) {
        long time = -1;
        try {
            String[] s1 = timeStr.split(":");
            String[] s2 = s1[1].split("\\.");

            long m = Long.valueOf(s1[0]);

            long s = Long.valueOf(s2[0]);

            long ms = Long.valueOf(s2[1]);

            time = m * 60000 + s * 1000 + ms * 10;

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
    /**
     * 判断文件编码
     * @param file 文件
     * @return 编码：GBK,UTF-8,UTF-16LE
     */
    public String getCharset(File file) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF)
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF)
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }
}
