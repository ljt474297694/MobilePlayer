package com.atguigu.ljt.mobileplayer.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by 李金桐 on 2017/1/6.
 * QQ: 474297694
 * 功能: Fragment基类  子类必须重写initView() 方法来用于返回子类需要显示的试图
 * 如果子类需要加载数据 如果网络请求 等初始化数据的代码 就可以重写initData()这个方法
 * 在方法中初始化 这个方法会在 onActivityCreated()方法中调用
 */

public abstract class BaseFragment extends Fragment {
    /**
     * 通过getActivity()方法获得Context
     * 用于需要Context时的参数保存在基类中
     * 更方便的让子类使用
     */
    protected Context mContext;

    /**
     * 当Fragment创建时调用 获得Context并保存起来
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    /**
     * Fragment创建试图的方法
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initView();
    }

    /**
     * 此方法让子类必须重写 用于返回子类需要显示的试图对象 就是onCreateView()的返回值
     *
     * @return
     */
    protected abstract View initView();

    /**
     * 当Activity绑定fragment成功的时候 onCreateView()方法后调用
     * 初始化数据:
     * 联网请求数据
     * 绑定数据
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    /**
     * 当子类需要加载数据的时候重写的方法
     * 如 联网请求
     */
    protected void initData() {

    }

    /**
     * 当Fragment的隐藏状态发生变化的时候回调的方法
     *
     * @param hidden 是否隐藏 true 表示隐藏  flase 表示显示
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            //当显示的时候可以执行刷新数据
            onRequesData();
        }else{
            //当隐藏的时候可以执行的操作

        }

    }

    protected void onRequesData() {

    }

    ;
}
