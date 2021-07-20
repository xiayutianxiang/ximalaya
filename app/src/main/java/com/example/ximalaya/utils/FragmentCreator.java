package com.example.ximalaya.utils;

import com.example.ximalaya.base.BaseFragment;
import com.example.ximalaya.fragments.HistoryFragment;
import com.example.ximalaya.fragments.AlbumFragment;
import com.example.ximalaya.fragments.SubscriptionFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * 用来创建或者保存fragment
 */
public class FragmentCreator {
    public final static int INDEX_RECOMMEND = 0;
    public final static int INDEX_SUBSCRIPTION = 1;
    public final static int INDEX_HISTORY = 2;
    public final static int PAGER_COUNT = 3;

    private static Map<Integer, BaseFragment> sCache = new HashMap<>(); //保存创建过的fragment

    public static BaseFragment getFragment(int index){
        BaseFragment baseFragment = sCache.get(index);
        if(baseFragment != null){
            return baseFragment;
        }
        switch (index){
            case INDEX_RECOMMEND:
                baseFragment = new AlbumFragment();
                break;
            case INDEX_SUBSCRIPTION:
                baseFragment = new SubscriptionFragment();
                break;
            case INDEX_HISTORY:
                baseFragment = new HistoryFragment();
                break;
        }
        sCache.put(index,baseFragment);
        return baseFragment;
    }
}
