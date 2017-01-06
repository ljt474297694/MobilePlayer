package com.atguigu.ljt.mobileplayer.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;

import com.atguigu.ljt.mobileplayer.R;
import com.atguigu.ljt.mobileplayer.base.BaseFragment;
import com.atguigu.ljt.mobileplayer.fragment.LocalAudioFragment;
import com.atguigu.ljt.mobileplayer.fragment.LocalVideoFragment;
import com.atguigu.ljt.mobileplayer.fragment.NetAudioFragment;
import com.atguigu.ljt.mobileplayer.fragment.NetVideoFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RadioGroup rg_main;
    private ArrayList<BaseFragment> fragments;
    private int position;
    private Fragment tempFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rg_main = (RadioGroup) findViewById(R.id.rg_main);

        initFragment();

        initListener();

    }

    /**
     * 设置RadioGroup监听的方法 用于根据对应的button的选择状态 切换不同Fragment
     */
    private void initListener() {
        rg_main.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_local_video:
                        position = 0;
                        break;
                    case R.id.rb_local_audio:
                        position = 1;
                        break;
                    case R.id.rb_net_audio:
                        position = 2;
                        break;
                    case R.id.rb_net_video:
                        position = 3;
                        break;
                }
                Fragment currentFragment = fragments.get(position);
                switchFragment(currentFragment);
            }
        });
        rg_main.check(R.id.rb_local_video);
    }

    /**
     * 根据Fragmnent的显示状态进行判断  是否添加显示或者隐藏对应的Fragment
     *
     * @param currentFragment
     */
    private void switchFragment(Fragment currentFragment) {
        //当页面不同时才切换页面
        if (tempFragment != currentFragment) {
            //需要显示的Fragment不能为空
            if (currentFragment != null) {
                //开启Fragment事物
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                if (currentFragment.isAdded()) {
                    //如果当前Fragment添加过了那么隐藏之前的Fragment并显示当前Fragment
                    if (tempFragment != null) {
                        ft.hide(tempFragment);
                    }
                    ft.show(currentFragment);
                } else {
                    //如果当前Fragment没添加过了那么隐藏之前的Fragment并添加当前Fragment
                    if (tempFragment != null) {
                        ft.hide(tempFragment);
                    }
                    ft.add(R.id.fl_mainc_content, currentFragment);
                }
                tempFragment = currentFragment;
                //提交事物
                ft.commit();
            }

        }
    }

    /**
     * 把需要显示的Fragment初始化并增添到集合中
     */
    private void initFragment() {
        fragments = new ArrayList<>();
        fragments.add(new LocalVideoFragment());
        fragments.add(new LocalAudioFragment());
        fragments.add(new NetAudioFragment());
        fragments.add(new NetVideoFragment());
    }
}
