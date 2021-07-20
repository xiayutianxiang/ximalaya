package com.example.ximalaya.interfaces;

import com.example.ximalaya.base.IBasePresenter;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

public interface IPlayerPresenter extends IBasePresenter<IPlayerCallBack> {

    //播放
    void play();

    //暂停
    void pause();

    //停止
    void stop();

    //上一首
    void playPre();

    //下一首
    void playNext();

    //切换模式
    void switchPlayMode(XmPlayListControl.PlayMode mode);

    //获取播放列表
    void getPlayList();

    //根据节目的位置播放
    void playByIndex(int index);

    //切换播放进度
    void seekTo(int progress);

    /**
     * 判断播放器是否播放
     */
    boolean isPlaying();

    /**
     * 把播放器内容反转
     */
    void reversePlayList();

    /**
     * 播放专辑的第一个节目
     * @param id
     */
    void playByAlbumId(long id);
}
