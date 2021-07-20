package com.example.ximalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IAlbumDetailViewCallBack {

    /**
     * 专辑详情内容
     * @param tracks
     */
    void onDetailListLoaded(List<Track> tracks);

    /**
     *网络错误
     */
    void onNetworkError(int errorCode,String errorMsg);

    /**
     * 把Album传给UI
     * @param album
     */
    void onAlbumLoaded(Album album);

    /**
     * 加载更多的结果
     * isOkay 为true表示加载成功
     * @param size
     */
    void onLoaderMoreFinished(int size);

    /**
     * 下拉加载更多 true表示成功
     * @param size
     */
    void onRefreshFinished(int size);
}
