package com.example.ximalaya.interfaces;

import com.example.ximalaya.base.IBasePresenter;

public interface IAlbumDetailPresenter extends IBasePresenter<IAlbumDetailViewCallBack> {
    /**
     * 下拉刷新内容
     */
    void pull2RefreshMore();
    /**
     * 上接加载更多
     */
    void loadMore();

    /**
     * 获取专辑详情
     * @param albunId
     * @param page
     */
    void getAlbumDetail(int albunId,int page);

//    /***
//     * 注册ui通知的接口
//     * @param detailPresenter
//     */
//    void registerViewCallBack(IAlbumDetailViewCallBack detailPresenter);
//
//    void unregisterViewCallBack(IAlbumDetailViewCallBack detailPresenter);
}
