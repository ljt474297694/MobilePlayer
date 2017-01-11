package com.atguigu.ljt.mobileplayer.fragment;

import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.atguigu.ljt.mobileplayer.R;
import com.atguigu.ljt.mobileplayer.adapter.NetVideoAdapter;
import com.atguigu.ljt.mobileplayer.base.BaseFragment;
import com.atguigu.ljt.mobileplayer.bean.MediaItem;
import com.atguigu.ljt.mobileplayer.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;

/**
 * Created by 李金桐 on 2017/1/6.
 * QQ: 474297694
 * 功能: 网络视频播放的Fragment
 */

public class NetVideoFragment extends BaseFragment {
    @ViewInject(R.id.pb_no_media)
    private ProgressBar pb_no_media;
    @ViewInject(R.id.listview)
    private ListView listView;
    private ArrayList<MediaItem> mediaItems;
    private NetVideoAdapter adapter;

    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.fragment_net_video, null);
        x.view().inject(this, view);

        return view;
    }

    @Override
    protected void initData() {
        super.initData();
        getDataFromNet();


    }

    private void getDataFromNet() {
        RequestParams params = new RequestParams(Constant.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void processData(String json) {
        mediaItems = parsedJson(json);
        if (mediaItems != null && mediaItems.size() > 0) {
            adapter = new NetVideoAdapter(mContext, mediaItems);
            listView.setAdapter(adapter);
            pb_no_media.setVisibility(View.GONE);
        } else {
            pb_no_media.setVisibility(View.VISIBLE);
        }
    }

    private ArrayList<MediaItem> parsedJson(String json) {

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("trailers");
            for (int i = 0; i < jsonArray.length(); i++) {
                MediaItem mediaItem = new MediaItem();

                JSONObject jsonObjectItem = (JSONObject) jsonArray.get(i);
                String name = jsonObjectItem.optString("movieName");
                mediaItem.setName(name);
                String desc = jsonObjectItem.optString("videoTitle");
                mediaItem.setDesc(desc);
                String url = jsonObjectItem.optString("url");
                mediaItem.setData(url);
                String hightUrl = jsonObjectItem.optString("hightUrl");
                mediaItem.setHeightUrl(hightUrl);
                String coverImg = jsonObjectItem.optString("coverImg");
                mediaItem.setImageUrl(coverImg);
                int videoLength = jsonObjectItem.optInt("videoLength");
                mediaItem.setDuration(videoLength);

                mediaItems.add(mediaItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mediaItems;
    }

    protected void onRequesData() {
    }

}
