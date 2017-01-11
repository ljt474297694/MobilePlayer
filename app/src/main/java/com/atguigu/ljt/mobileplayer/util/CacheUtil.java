package com.atguigu.ljt.mobileplayer.util;

import android.content.Context;
import android.content.SharedPreferences;

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
}
