package com.atguigu.ljt.mobileplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.atguigu.ljt.mobileplayer.R;
import com.atguigu.ljt.mobileplayer.bean.MediaItem;
import com.atguigu.ljt.mobileplayer.util.Utils;
import com.bumptech.glide.Glide;

import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;

import java.util.ArrayList;

/**
 * Created by 李金桐 on 2017/1/7.
 * QQ: 474297694
 * 功能: 本地视频的列表配适器
 */

public class NetVideoAdapter extends BaseAdapter {
    private final Context mContext;
    private final ArrayList<MediaItem> datas;
    private Utils timeUtil;
    private ImageOptions imageOptions;

    public NetVideoAdapter(Context mContext, ArrayList<MediaItem> mediaItems) {
        this.mContext = mContext;
        this.datas = mediaItems;
        timeUtil = new Utils();
        imageOptions = new ImageOptions.Builder()
                .setSize(DensityUtil.dip2px(120), DensityUtil.dip2px(120))
                // 如果ImageView的大小不是定义为wrap_content, 不要crop.
                .setCrop(true) // 很多时候设置了合适的scaleType也不需要它.
                // 加载中或错误图片的ScaleType
                //.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
                .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .setLoadingDrawableId(R.drawable.video_default)//加载过程中的默认图片
                .setFailureDrawableId(R.drawable.video_default)//就挨着出错的图片
                .build();
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
            convertView = View.inflate(mContext, R.layout.item_net_video, null);
            holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_duration = (TextView) convertView.findViewById(R.id.tv_duration);
            holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        MediaItem mediaItem = datas.get(position);
        holder.tv_name.setText(mediaItem.getName());
        holder.tv_duration.setText(mediaItem.getDesc());
        holder.tv_size.setText(mediaItem.getDuration() + "秒");
        //xUtils3 网络请求图片
//        x.image().bind(holder.iv_icon,mediaItem.getImageUrl(),imageOptions);
        //Picasso 网络请求图片
//        Picasso.with(mContext)
//                .load(mediaItem.getImageUrl())
//                .placeholder(R.drawable.video_default)
//                .error(R.drawable.video_default)
//                .into(holder.iv_icon);
        //Gilde 网络请求图片
        Glide.with(mContext)
                .load(mediaItem.getImageUrl())
                .placeholder(R.drawable.video_default)
                .error(R.drawable.video_default)
                .into(holder.iv_icon);
        return convertView;
    }

    class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_duration;
        TextView tv_size;
    }
}