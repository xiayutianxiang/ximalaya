package com.example.ximalaya.presenter;

import com.example.ximalaya.data.XimalayApi;
import com.example.ximalaya.interfaces.IAlbumDetailPresenter;
import com.example.ximalaya.interfaces.IAlbumDetailViewCallBack;
import com.example.ximalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;

import java.util.ArrayList;
import java.util.List;

public class AlbumDetailPresenter implements IAlbumDetailPresenter {

    private static final String TAG = "AlbumDetailPresenter";
    private List<IAlbumDetailViewCallBack> mCallbacks =  new ArrayList<>();
    private Album mTargetAlbum = null;
    private List<Track> mTracks = new ArrayList<>();
    //当前的专辑id
    private int mCurrentAlbumId = -1;
    //当前页
    private int mCurrentPageIndex = 0;

    private AlbumDetailPresenter(){

    }
    private static AlbumDetailPresenter sInstance = null;

    public static AlbumDetailPresenter getInstance(){
        if(sInstance == null){
            synchronized (AlbumDetailPresenter.class){
                if(sInstance == null){
                    sInstance = new AlbumDetailPresenter();
                }
            }
        }
        return sInstance;
    }
    @Override
    public void pull2RefreshMore() {

    }

    @Override
    public void loadMore() {
        //加载更多
        mCurrentPageIndex++;
        //传入true，表示结果追加到列表
        doLoaded(true);
    }

    private void doLoaded(final boolean isLoadedMore){

        XimalayApi ximalayApi = XimalayApi.getXimalayApi();
        ximalayApi.getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                if(trackList!=null){
                    List<Track> tracks = trackList.getTracks();
                    LogUtil.d(TAG,"tracks Size --->" + tracks);
                    if (isLoadedMore) {
                        //如果是上拉加载更多，从列表末尾增加数据
                        //mTracks.addAll(tracks);   作用相同
                        mTracks.addAll(mTracks.size() - 1 ,tracks);
                        int size = tracks.size();
                        handlerLoaderMoreResult(size);
                        //加载出来了更多数据

                    }else {
                        //如果是下拉刷新，则从列表索引为0处直接添加数据
                        mTracks.addAll(0,tracks);
                    }
                    handlerAlbumDetailResult(mTracks);
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if(isLoadedMore){
                    mCurrentPageIndex--;
                }
                LogUtil.d(TAG,"errorCode--->" + errorCode);
                LogUtil.d(TAG,"errorMsg--->" + errorMsg);
                handlerError(errorCode,errorMsg);
            }
        },mCurrentAlbumId,mCurrentPageIndex);
    }

    /**
     * 处理加载更多的结果
     */
    private void handlerLoaderMoreResult(int size) {
        for (IAlbumDetailViewCallBack callback : mCallbacks) {
            callback.onLoaderMoreFinished(size);
        }
    }

    @Override
    public void getAlbumDetail(int albumId, int page) {
        mTracks.clear();
        this.mCurrentAlbumId = albumId;
        this.mCurrentPageIndex = page;
        doLoaded(false);
    }

    /**
     * 如果发生错误，则通知UI
     * @param errorCode
     * @param errorMsg
     */
    private void handlerError(int errorCode, String errorMsg) {
        for (IAlbumDetailViewCallBack callback : mCallbacks) {
            callback.onNetworkError(errorCode,errorMsg);
        }
    }

    private void handlerAlbumDetailResult(List<Track> tracks) {
        for(IAlbumDetailViewCallBack mCallBack : mCallbacks){
            mCallBack.onDetailListLoaded(tracks);
        }
    }

    @Override
    public void registerViewCallBack(IAlbumDetailViewCallBack detailViewCallBack) {
        //先判断
        if(!mCallbacks.contains(detailViewCallBack)){
            mCallbacks.add(detailViewCallBack);
            if(mTargetAlbum != null){
                detailViewCallBack.onAlbumLoaded(mTargetAlbum); 
            }
        }
    }

    @Override
    public void unRegisterViewCallBack(IAlbumDetailViewCallBack detailViewCallBack) {
        if ((mCallbacks.contains(detailViewCallBack))){
            mCallbacks.remove(detailViewCallBack);
        }
    }

    public void setTargetAlbum(Album targetAlbum){
        this.mTargetAlbum = targetAlbum;
    }
}
