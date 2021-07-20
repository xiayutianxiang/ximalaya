package com.example.ximalaya.data;

import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public interface ISubDaoCallBak {

    /**
     * 添加的结果回调方法
     * @param isSuccess
     */
    void onAddResult(boolean isSuccess);

    /**
     * 删除结果的回调方法
     * @param isSuccess
     */
    void onDelResult(boolean isSuccess);

    /**
     * 加载的结果
     * @param result
     */
    void onSubListLoaded(List<Album> result);
}
