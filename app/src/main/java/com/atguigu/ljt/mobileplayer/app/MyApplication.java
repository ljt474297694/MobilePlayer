package com.atguigu.ljt.mobileplayer.app;

import android.app.Application;

import org.xutils.x;


/**
 * Created by 李金桐 on 2017/1/10.
 * QQ: 474297694
 * 功能: Application xUtil初始化
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(true);
    }

}
