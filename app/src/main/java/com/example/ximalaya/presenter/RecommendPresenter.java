package com.example.ximalaya.presenter;

import com.example.ximalaya.data.XimalayApi;
import com.example.ximalaya.interfaces.IRecommendPresenter;
import com.example.ximalaya.interfaces.IRecommendViewCallBack;
import com.example.ximalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.ArrayList;
import java.util.List;

public class RecommendPresenter implements IRecommendPresenter {
    private static final String TAG = "RecommendPresenter";
    private List<IRecommendViewCallBack> mCallBacks = new ArrayList<>();
    private List<Album> mCurrentRecommend = null;
    private List<Album> mRecommendList;

    private RecommendPresenter() {

    }

    private static RecommendPresenter sInstance = null;

    /**
     * 获取单例对象
     *
     * @return
     */
    public static RecommendPresenter getInstance() {
        if (sInstance == null) {
            synchronized (RecommendPresenter.class) {
                if (sInstance == null) {
                    sInstance = new RecommendPresenter();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取当前的推荐专辑列表
     *
     * @return
     */
    public List<Album> getCurrentRecommend() {
        return mCurrentRecommend;
    }

    @Override
    public void getRecommendList() {

        //如果内容不为空，直接使用当前 内容
        if (mRecommendList != null && mRecommendList.size() > 0) {
            handlerRecommendResult(mRecommendList);
            LogUtil.d(TAG, "getRecommendList ----> from local");
            return;
        }
        //获取内容
        updateLoading();
        /**
         * 获取推荐内容 拿到 猜你喜欢 专辑
         */
        //封装数据
        XimalayApi ximalayApi = XimalayApi.getXimalayApi();
        ximalayApi.getRecommendList(new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(GussLikeAlbumList gussLikeAlbumList) {
                if (gussLikeAlbumList != null) {
                    LogUtil.d(TAG, "getRecommendList ----> from network");
                    mRecommendList = gussLikeAlbumList.getAlbumList();
                    //得到数据以后，更新ui
                    //upRecommendUI(albumList);
                    handlerRecommendResult(mRecommendList);
                }
            }

            @Override
            public void onError(int i, String s) {
                LogUtil.d(TAG, "error --->" + i);
                LogUtil.d(TAG, "errorMsg --->" + s);
                handlerError(); //发生错误
            }
        });

    }

    private void handlerError() {
        //更新ui
        if (mCallBacks != null) {
            for (IRecommendViewCallBack callBack : mCallBacks) {
                callBack.onNetworkError();
            }
        }
    }

    private void handlerRecommendResult(List<Album> albumList) {
        //更新ui
        if (albumList != null) {
            //测试，清空数据
            //albumList.clear();
            if (albumList.size() == 0) {
                for (IRecommendViewCallBack callBack : mCallBacks) {
                    callBack.onEmpty();
                }
            } else {
                for (IRecommendViewCallBack callBack : mCallBacks) {
                    callBack.onRecommendListLoaded(albumList);
                }
                this.mCurrentRecommend = albumList;
            }
        }
    }

    private void updateLoading() {
        for (IRecommendViewCallBack callBack : mCallBacks) {
            callBack.onLoading();
        }
    }

    @Override
    public void pull2RefreshMore() {

    }

    @Override
    public void loadMore() {

    }

    @Override
    public void registerViewCallBack(IRecommendViewCallBack callBack) {
        if (mCallBacks != null && !mCallBacks.contains(callBack)) {
            mCallBacks.add(callBack);
        }
    }

    @Override
    public void unRegisterViewCallBack(IRecommendViewCallBack callBack) {
        if (mCallBacks != null) {
            mCallBacks.remove(callBack);
        }
    }
}
