package com.atguigu.ljt.mobileplayer.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.atguigu.ljt.mobileplayer.service.MusicPlayerService;

/**
 * Created by 李金桐 on 2017/1/11.
 * QQ: 474297694
 * 功能: sp的文本缓存
 */

public class CacheUtil {
    public static String getString(Context context, String key,String spName) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    public static void putString(Context context, String result, String key,String spName) {
        context.getSharedPreferences(spName, Context.MODE_PRIVATE).edit().putString(key, result).commit();
    }

    public static void putPlayMode(Context context, String key, int mode) {
        context.getSharedPreferences("playmode", Context.MODE_PRIVATE).edit().putInt(key, mode).commit();
    }

    public static int getPlayMode(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("playmode", Context.MODE_PRIVATE);
        return sp.getInt(key, MusicPlayerService.NORMAL);
    }
}
