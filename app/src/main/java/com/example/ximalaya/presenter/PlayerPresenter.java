package com.example.ximalaya.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.ximalaya.data.XimalayApi;
import com.example.ximalaya.base.BaseApplication;
import com.example.ximalaya.interfaces.IPlayerCallBack;
import com.example.ximalaya.interfaces.IPlayerPresenter;
import com.example.ximalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.constants.PlayerConstants;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

//继承IPlayerPresenter，实现其中的方法
public class PlayerPresenter implements IPlayerPresenter, IXmAdsStatusListener, IXmPlayerStatusListener {

    private static final int DEFALUT_PLAY_INDEX = 0;
    private List<IPlayerCallBack> mIPlayerCallBacks = new ArrayList<>();

    private static final String TAG = "PlayerPresenter";
    private final XmPlayerManager mPlayerManager;
    private Track mCurrentTrack;
    private int mCurrentIndex = DEFALUT_PLAY_INDEX;
    private final SharedPreferences mPlayModeSp;

    public static final int PLAY_MODEL_LIST_INT = 0;
    public static final int PLAY_MODEL_LIST_LOOP_INT = 1;
    public static final int PLAY_MODEL_RANDOM_INT = 2;
    public static final int PLAY_MODEL_SINGLE_LOOP_INT = 3;

    private XmPlayListControl.PlayMode mCurrentPlayMode = PLAY_MODEL_LIST;
    //sp's key and name
    public static final String PLAY_MODE_SP_NAME = "PlayMode";
    public static final String PLAY_MODE_SP_KEY = "currentPlayMode";
    private boolean mIsReverse = false;
    private int mProgressDuration = 0;
    private int mCurrentProgressPosition = 0;

    private PlayerPresenter() {
        mPlayerManager = XmPlayerManager.getInstance(BaseApplication.getAppContext());

        //添加广告物料的监听
        mPlayerManager.addAdsStatusListener(this);
        //注册播放器状态相关的接口
        mPlayerManager.addPlayerStatusListener(this);

        //需要记录当前的播放模式
        mPlayModeSp = BaseApplication.getAppContext().getSharedPreferences(PLAY_MODE_SP_NAME, Context.MODE_PRIVATE);
    }

    //设计单例模式
    private static PlayerPresenter sPlayerPresenter = null;

    public static PlayerPresenter getPlayerPresenter() {
        if (sPlayerPresenter == null) {
            synchronized (PlayerPresenter.class) {
                if (sPlayerPresenter == null) {
                    sPlayerPresenter = new PlayerPresenter();
                }
            }
        }
        return sPlayerPresenter;
    }

    private boolean isPlayListSet = false;

    public void setPlayList(List<Track> list, int playIndex) {
        if (mPlayerManager != null) {
            mPlayerManager.setPlayList(list, playIndex);
            isPlayListSet = true;
            mCurrentTrack = list.get(playIndex);
            mCurrentIndex = playIndex;
        } else {
            LogUtil.d(TAG, "mPlayerManager is not null");
        }
    }

    @Override
    public void play() {
        if (isPlayListSet) {
            mPlayerManager.play();
        }
    }

    @Override
    public void pause() {
        if (mPlayerManager != null) {
            mPlayerManager.pause();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void playPre() {
        //播放前一个
        if (mPlayerManager != null) {
            mPlayerManager.playPre();
        }

    }

    @Override
    public void playNext() {
        //播放下一个
        if (mPlayerManager != null) {
            mPlayerManager.playNext();
        }
    }

    /**
     * 判断是否有播放的节目列表
     *
     * @return
     */
    public boolean hasPlayList() {
        return isPlayListSet;
    }

    @Override
    public void switchPlayMode(XmPlayListControl.PlayMode mode) {
        if (mPlayerManager != null) {
            mCurrentPlayMode = mode;
            mPlayerManager.setPlayMode(mode);
            //通知UI更新播放模式
            for (IPlayerCallBack iPlayerCallBack : mIPlayerCallBacks) {
                iPlayerCallBack.onPlayModeChange(mode);
            }
            SharedPreferences.Editor edit = mPlayModeSp.edit();
            edit.putInt(PLAY_MODE_SP_KEY, getIntPlayMode(mode));
            edit.commit();
        }
    }

    private int getIntPlayMode(XmPlayListControl.PlayMode mode) {
        switch (mode) {
            case PLAY_MODEL_LIST_LOOP:
                return PLAY_MODEL_LIST_LOOP_INT;
            case PLAY_MODEL_SINGLE_LOOP:
                return PLAY_MODEL_SINGLE_LOOP_INT;
            case PLAY_MODEL_LIST:
                return PLAY_MODEL_LIST_INT;
            case PLAY_MODEL_RANDOM:
                return PLAY_MODEL_RANDOM_INT;
        }
        return PLAY_MODEL_LIST_INT;
    }

    private XmPlayListControl.PlayMode getModeByInt(int index) {
        switch (index) {
            case PLAY_MODEL_LIST_LOOP_INT:
                return PLAY_MODEL_LIST_LOOP;
            case PLAY_MODEL_SINGLE_LOOP_INT:
                return PLAY_MODEL_SINGLE_LOOP;
            case PLAY_MODEL_LIST_INT:
                return PLAY_MODEL_LIST;
            case PLAY_MODEL_RANDOM_INT:
                return PLAY_MODEL_RANDOM;
        }
        return PLAY_MODEL_LIST;
    }

    @Override
    public void getPlayList() {
        if (mPlayerManager != null) {
            List<Track> playList = mPlayerManager.getPlayList();
            for (IPlayerCallBack iPlayerCallBack : mIPlayerCallBacks) {
                iPlayerCallBack.onListLoaded(playList);
            }
        }
    }

    @Override
    public void playByIndex(int index) {
        //切换播放器到第Index的位置进行播放
        if (mPlayerManager != null) {
            mPlayerManager.play(index);
        }
    }

    @Override
    public void seekTo(int progress) {
        //更新播放器进度
        mPlayerManager.seekTo(progress);
    }

    @Override
    public boolean isPlaying() {
        //返回到当前是否播放
        return mPlayerManager.isPlaying();
    }

    @Override
    public void reversePlayList() {
        //把播放列表反转
        List<Track> playList = mPlayerManager.getPlayList();
        Collections.reverse(playList);
        mIsReverse = !mIsReverse;
        //第一个参数时播放列表，第二个参数是开始播放的下标
        mCurrentIndex = playList.size() - 1 - mCurrentIndex;
        mPlayerManager.setPlayList(playList, mCurrentIndex);

        //更新UI
        mCurrentTrack = (Track) mPlayerManager.getCurrSound();
        for (IPlayerCallBack iPlayerCallBack : mIPlayerCallBacks) {
            iPlayerCallBack.onListLoaded(playList);
            iPlayerCallBack.onTrackUpDate(mCurrentTrack, mCurrentIndex);
            iPlayerCallBack.updateListOrder(mIsReverse);
        }
    }

    @Override
    public void playByAlbumId(long id) {
        int mId = new Long(id).intValue();
        //1.要获取的专辑的内容
        XimalayApi ximalayApi = XimalayApi.getXimalayApi();
        ximalayApi.getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                //2.把专辑内容设置给播放器
                List<Track> tracks = trackList.getTracks();
                if (trackList != null && tracks.size() > 0) {
                    mPlayerManager.setPlayList(tracks, DEFALUT_PLAY_INDEX);
                    isPlayListSet = true;
                    mCurrentTrack = tracks.get(DEFALUT_PLAY_INDEX);
                    mCurrentIndex = DEFALUT_PLAY_INDEX;
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.d(TAG,"errorCoed ---> "+ errorCode);
                LogUtil.d(TAG,"errorMsg ---> "+ errorMsg);
                Toast.makeText(BaseApplication.getAppContext(),"请求数据错误...",Toast.LENGTH_SHORT).show();
            }
        }, mId, 1);

        //3.开始播放
    }

    @Override
    public void registerViewCallBack(IPlayerCallBack iPlayerCallBack) {
        if (!mIPlayerCallBacks.contains(iPlayerCallBack)) {
            mIPlayerCallBacks.add(iPlayerCallBack);
        }
        //更新之前，让UI的 pager有数据
        getPlayList();
        iPlayerCallBack.onTrackUpDate(mCurrentTrack, mCurrentIndex);
        iPlayerCallBack.onProgressChange(mCurrentProgressPosition,mProgressDuration);
        //更新状态
        handPlayState(iPlayerCallBack);
        //从sp里头拿
        int modeIndex = mPlayModeSp.getInt(PLAY_MODE_SP_KEY, PLAY_MODEL_LIST_INT);
        mCurrentPlayMode = getModeByInt(modeIndex);
        iPlayerCallBack.onPlayModeChange(mCurrentPlayMode);
    }

    private void handPlayState(IPlayerCallBack iPlayerCallBack) {
        int playerStatus = mPlayerManager.getPlayerStatus();
        //根据状态调用接口方法
        if (PlayerConstants.STATE_STARTED == playerStatus) {
            iPlayerCallBack.onPlayStart();
        }else {
            iPlayerCallBack.onPlayPause();
        }
    }

    @Override
    public void unRegisterViewCallBack(IPlayerCallBack iPlayerCallBack) {
        mIPlayerCallBacks.remove(iPlayerCallBack);
    }

    /*广告相关的回调方法 start*/

    @Override
    public void onStartGetAdsInfo() {
        LogUtil.d(TAG, "onStartGetAdsInfo");
    }

    @Override
    public void onGetAdsInfo(AdvertisList advertisList) {
        LogUtil.d(TAG, "onGetAdsInfo");
    }

    @Override
    public void onAdsStartBuffering() {
        LogUtil.d(TAG, "onAdsStopBuffering");
    }

    @Override
    public void onAdsStopBuffering() {
        LogUtil.d(TAG, "onAdsStopBuffering");
    }

    @Override
    public void onStartPlayAds(Advertis advertis, int i) {
        LogUtil.d(TAG, "onStartPlayAds");
    }

    @Override
    public void onCompletePlayAds() {
        LogUtil.d(TAG, "onCompletePlayAds");
    }

    /**
     * what:错误类型
     * extra：错误的额外信息
     *
     * @param what
     * @param extra
     */
    @Override
    public void onError(int what, int extra) {
        LogUtil.d(TAG, "onError what ---> " + what + "onError extra ---> " + extra);
    }

    /*------------广告相关的回调方法 end---------*/


    /*-----------------播放器相关的回调方法 start---------*/
    @Override
    public void onPlayStart() {
        LogUtil.d(TAG, "onPlayStart 开始播放....");
        for (IPlayerCallBack iPlayerCallBack : mIPlayerCallBacks) {
            iPlayerCallBack.onPlayStart();
        }
    }

    @Override
    public void onPlayPause() {
        LogUtil.d(TAG, "onPlayPause 开始播放....");
        for (IPlayerCallBack iPlayerCallBack : mIPlayerCallBacks) {
            iPlayerCallBack.onPlayPause();
        }
    }

    @Override
    public void onPlayStop() {
        LogUtil.d(TAG, "onPlayStop");
        for (IPlayerCallBack iPlayerCallBack : mIPlayerCallBacks) {
            iPlayerCallBack.onPlayStop();
        }
    }

    @Override
    public void onSoundPlayComplete() {
        LogUtil.d(TAG, "onSoundPlayComplete 播放完成....");
    }

    @Override
    public void onSoundPrepared() {
        LogUtil.d(TAG, "onSoundPrepared");
        mPlayerManager.setPlayMode(mCurrentPlayMode);
        if (mPlayerManager.getPlayerStatus() == PlayerConstants.STATE_PREPARED) {
            //播放器准备完了，可以播放
            mPlayerManager.play();
        }
    }

    /**
     * lastModel：上一首model,可能为空
     * curModel：下一首model
     *
     * @param lastModel
     * @param curModel
     */
    @Override
    public void onSoundSwitch(PlayableModel lastModel, PlayableModel curModel) {
        LogUtil.d(TAG, "onSoundSwitch");
        if (lastModel != null) {
            LogUtil.d(TAG, "lastModel ---> " + lastModel.getKind());
        }
        if (curModel != null) {
            LogUtil.d(TAG, "curModel ----> " + curModel.getKind());
        }
        //curModel代表当前播放内容
        //通过getKind方法获取它是什么类型的
        //track表示track类型
        //第一种：这种不推荐
//        if("track".equals(curModel.getKind())){
//            Track currentTrack = (Track) curModel;
//            LogUtil.d(TAG,"currentTrack --- >" + currentTrack.getTrackTitle());
//        }

        //推荐第二种写法（第一种中字段可能会被修改）
        mCurrentIndex = mPlayerManager.getCurrentIndex();
        if (curModel instanceof Track) {
            Track currentTrack = (Track) curModel;
            mCurrentTrack = currentTrack;
            //保存播放记录
            HistoryPresenter historyPresenter = HistoryPresenter.getInstance();
            historyPresenter.addHistory(currentTrack);
            //LogUtil.d(TAG, "title --- >" + currentTrack.getTrackTitle());
            //更新ui
            for (IPlayerCallBack iPlayerCallBack : mIPlayerCallBacks) {
                iPlayerCallBack.onTrackUpDate(mCurrentTrack, mCurrentIndex);
            }
        }

    }

    @Override
    public void onBufferingStart() {
        LogUtil.d(TAG, "onBufferingStart...");
    }

    @Override
    public void onBufferingStop() {
        LogUtil.d(TAG, "onBufferingStop 缓冲完成....");
    }

    @Override
    public void onBufferProgress(int progress) {
        LogUtil.d(TAG, "onBufferProgress 缓冲进度.....");
    }

    /**
     * 播放进度回调
     *
     * @param currPos
     * @param duration
     */
    @Override
    public void onPlayProgress(int currPos, int duration) {
        this.mCurrentProgressPosition = currPos;
        this.mProgressDuration = duration;
        //单位是毫秒
        for (IPlayerCallBack iPlayerCallBack : mIPlayerCallBacks) {
            iPlayerCallBack.onProgressChange(currPos, duration);
        }
        LogUtil.d(TAG, "onPlayProgress currPos ----> " + currPos + "duration ----> " + duration);
    }

    @Override
    public boolean onError(XmPlayerException e) {
        return false;
    }
    /*------------播放器相关的回调方法 end-----------*/
}
