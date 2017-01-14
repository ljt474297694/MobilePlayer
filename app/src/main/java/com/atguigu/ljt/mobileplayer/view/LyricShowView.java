package com.atguigu.ljt.mobileplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.atguigu.ljt.mobileplayer.bean.LyricBean;
import com.atguigu.ljt.mobileplayer.util.DensityUtil;

import java.util.ArrayList;

/**
 * Created by 李金桐 on 2017/1/13.
 * QQ: 474297694
 * 功能: 自定义TextView 滚动的显示音乐的歌词
 */

public class LyricShowView extends TextView {
    private Context mContext;
    private int width;
    private int height;
    private ArrayList<LyricBean> lyricBeens;
    private Paint paint;
    private Paint noPaint;

    private float textHeight;
    /**
     * 默认需要高亮的歌词的下标
     */
    private int index = 0;
    private int mCurrentPosition;
    private float sleepTime;
    private float timePoint;

    public LyricShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        textHeight = DensityUtil.dip2px(context, 15);
        initView();
    }

    private void initView() {
        paint = new Paint();
        paint.setTextSize(textHeight);
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        paint.setTextAlign(Paint.Align.CENTER);

        noPaint = new Paint();
        noPaint.setTextSize(textHeight);
        noPaint.setAntiAlias(true);
        noPaint.setColor(Color.WHITE);
        noPaint.setTextAlign(Paint.Align.CENTER);

    }

    /**
     * @param canvas 根据需要高亮的歌词下标 不断的进行绘制
     *               实现歌词滚动的效果
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (lyricBeens != null && lyricBeens.size() > 0) {

            if(index != lyricBeens.size()-1){
                float plush = 0;

                if(sleepTime==0){
                    plush = 0;
                }else{
                    // 这一句花的时间： 这一句休眠时间  =  这一句要移动的距离：总距离(行高)
                    //这一句要移动的距离 = （这一句花的时间/这一句休眠时间） * 总距离(行高)
                    plush = ((mCurrentPosition-timePoint)/sleepTime)*textHeight;
                }


                canvas.translate(0,-plush);

            }
            canvas.drawText(lyricBeens.get(index).getContent(), width / 2, height / 2, paint);
            float tempY = height / 2;
            for (int i = index - 1; i >= 0; i--) {

                tempY -= textHeight;
                if (tempY < 0) {
                    break;
                }

                canvas.drawText(lyricBeens.get(i).getContent(), width / 2, tempY, noPaint);
            }

            tempY = height / 2;
            for (int i = index + 1; i < lyricBeens.size(); i++) {

                tempY += textHeight;
                if (tempY > height) {
                    break;
                }
                canvas.drawText(lyricBeens.get(i).getContent(), width / 2, tempY, noPaint);
            }
        } else {

            canvas.drawText("没有找到歌词...", width / 2, height / 2, paint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    /**
     * @param currentPosition 根据歌曲对应的进度 判断需要高亮的歌词的下标
     *                        实现歌词同步的效果
     */
    public void setNextShowLyric(int currentPosition) {
        this.mCurrentPosition = currentPosition;

        if (lyricBeens == null || lyricBeens.size() == 0) return;

        for (int i = 1; i < lyricBeens.size(); i++) {
            /**
             * 当音乐的进度时间 小于某句歌词的时间 又大于等于他前一句歌词的时间
             * 那么i-1 就是当前需要高亮的歌词 前提是 歌词数据按照时间从小到大排序
             */
            if (mCurrentPosition < lyricBeens.get(i).getTimePoint()) {
                int indexTemp = i - 1;

                if (mCurrentPosition >= lyricBeens.get(indexTemp).getTimePoint()) {
                    index = indexTemp;
                    sleepTime = lyricBeens.get(indexTemp).getSleepTime();
                    timePoint = lyricBeens.get(indexTemp).getTimePoint();

                }
            } else {
                index = i;
            }
        }

        invalidate();
    }

    public void setLyric(ArrayList<LyricBean> lyricBeens) {
        this.lyricBeens = lyricBeens;
    }
}
