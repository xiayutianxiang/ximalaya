package com.example.ximalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

/**
 * 播放器的UI回调接口
 */
public interface IPlayerCallBack {

    //开始播放
    void onPlayStart();

    //播放暂停
    void onPlayPause();

    //播放停止
    void onPlayStop();

    //播放错误
    void  onPlayError();

    //下一首
    void nextPlay(Track track);

    //上一首
    void onPrePlay(Track track);

    //播放列表加载数据完成
    void onListLoaded(List<Track> list);

    //播放器模式改变
    void onPlayModeChange(XmPlayListControl.PlayMode playMode);

    //进度条的改变
    void onProgressChange(int currentProgress,int total);

    //广告加载
    void onAdLoading();

    //广告结束
    void onAdFinishing();

    //更新当前节目
    void onTrackUpDate(Track track,int playIndex);

    /**
     * 通知ui更新播放列表的顺序文字和图标
     * @param isReverse
     */
    void updateListOrder(boolean isReverse);
}
