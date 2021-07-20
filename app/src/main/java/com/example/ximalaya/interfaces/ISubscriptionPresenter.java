package com.example.ximalaya.interfaces;

import com.example.ximalaya.base.IBasePresenter;
import com.ximalaya.ting.android.opensdk.model.album.Album;



public interface ISubscriptionPresenter extends IBasePresenter<ISubscriptionCallBack> {

    /**
     * 添加订阅
     * @param album
     */
    void addSubscription(Album album);

    /**
     * 删除订阅
     * @param album
     */
    void deleteSubscription(Album album);

    /**
     * 获取订阅列表
     */
    void getSubscriptionList();

    /**
     * 判断是否收藏
     * @param album
     */
    boolean isSub(Album album);
}
