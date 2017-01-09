package com.atguigu.ljt.mobileplayer.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.atguigu.ljt.mobileplayer.R;
import com.atguigu.ljt.mobileplayer.bean.MediaItem;
import com.atguigu.ljt.mobileplayer.util.Utils;

import java.util.ArrayList;

/**
 * Created by 李金桐 on 2017/1/7.
 * QQ: 474297694
 * 功能: 本地视频的列表配适器
 */

public class LocalVideoAdapter extends BaseAdapter {
    private final Context mContext;
    private final ArrayList<MediaItem> datas;
    private Utils timeUtil;

    public LocalVideoAdapter(Context mContext, ArrayList<MediaItem> mediaItems) {
        this.mContext = mContext;
        this.datas = mediaItems;
        timeUtil = new Utils();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_local_video, null);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_duration = (TextView) convertView.findViewById(R.id.tv_duration);
            holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        MediaItem mediaItem = datas.get(position);
        holder.tv_name.setText(mediaItem.getName());
        holder.tv_duration.setText(timeUtil.stringForTime((int) mediaItem.getDuration()));
        holder.tv_size.setText(Formatter.formatFileSize(mContext,mediaItem.getSize()));
        return convertView;
    }

    class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_duration;
        TextView tv_size;
    }
}