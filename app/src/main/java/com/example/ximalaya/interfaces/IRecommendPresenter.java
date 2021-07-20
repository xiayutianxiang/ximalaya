package com.example.ximalaya.interfaces;

import com.example.ximalaya.base.IBasePresenter;

public interface IRecommendPresenter extends IBasePresenter <IRecommendViewCallBack>{
    /**
     * 获取推荐内容
     */
    void getRecommendList();
    /**
     * 下拉刷新内容
     */
    void pull2RefreshMore();
    /**
     * 上接加载更多
     */
    void loadMore();

//    /**
//     * 用于注册ui的回调
//     * @param callBack
//     */
//    void registerViewCallBack(IRecommendViewCallBack callBack);
//
//    /**
//     * 用于取消ui的注册回调
//     * @param callBack
//     */
//    void unRegisterViewCallBack(IRecommendViewCallBack callBack);
}
