package com.example.ximalaya.data;

import com.ximalaya.ting.android.opensdk.model.album.Album;

public interface ISubDao {

    void setCallBack(ISubDaoCallBak callBack);

    /**
     * 添加专辑订阅
     */
    void addAlbum(Album album);

    /**
     * 删除订阅
     * @param album
     */
    void delAlbum(Album album);

    /**
     * 获取订阅内容
     */
    void listAlbums();
}
