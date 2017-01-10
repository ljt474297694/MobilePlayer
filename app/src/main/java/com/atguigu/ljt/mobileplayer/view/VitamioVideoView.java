package com.atguigu.ljt.mobileplayer.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by 李金桐 on 2017/1/9.
 * QQ: 474297694
 * 功能: xxxx
 */

public class VitamioVideoView extends io.vov.vitamio.widget.VideoView {
    public VitamioVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }

    public void setVideoSize(int screenWidth, int screenHeight) {
       ViewGroup.LayoutParams l =  getLayoutParams();
        l.height = screenHeight;
        l.width = screenWidth;
        setLayoutParams(l);
    }
}
