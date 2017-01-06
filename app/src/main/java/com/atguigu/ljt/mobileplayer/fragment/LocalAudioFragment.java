package com.atguigu.ljt.mobileplayer.fragment;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.atguigu.ljt.mobileplayer.base.BaseFragment;

/**
 * Created by 李金桐 on 2017/1/6.
 * QQ: 474297694
 * 功能: 本地音频播放的Fragment
 */

public class LocalAudioFragment extends BaseFragment {

    private TextView textView;

    @Override
    public View initView() {
        textView = new TextView(mContext);
        textView.setTextColor(Color.BLUE);
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    @Override
    protected void initData() {
        super.initData();
        textView.setText("本地音频");

    }
}
