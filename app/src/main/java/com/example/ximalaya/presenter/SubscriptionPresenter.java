package com.example.ximalaya.presenter;

import com.example.ximalaya.base.BaseApplication;
import com.example.ximalaya.data.ISubDaoCallBak;
import com.example.ximalaya.data.SubscriptionDao;
import com.example.ximalaya.interfaces.ISubscriptionCallBack;
import com.example.ximalaya.interfaces.ISubscriptionPresenter;
import com.example.ximalaya.utils.Constants;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SubscriptionPresenter implements ISubscriptionPresenter, ISubDaoCallBak {

    private final SubscriptionDao mSubscriptionDao;
    private Map<Long,Album> mData = new HashMap<>();
    private List<ISubscriptionCallBack> mCallbacks = new ArrayList<>();


    private SubscriptionPresenter(){
        mSubscriptionDao = SubscriptionDao.getInstance();
        mSubscriptionDao.setCallBack(this);
    }

    private void listSubscriptions(){
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                //只调用，不处理结果
                if (mSubscriptionDao != null) {
                    mSubscriptionDao.listAlbums();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }
    private static SubscriptionPresenter sInstance = null;

    public static SubscriptionPresenter getInstance(){
        if (sInstance == null) {
            synchronized (SubscriptionPresenter.class){
                if (sInstance == null) {
                    sInstance = new SubscriptionPresenter();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void addSubscription(Album album) {
        //判断当前订阅数量，不能超过100个
        if (mData.size()>= Constants.MAX_SUB_COUNT) {
            for (ISubscriptionCallBack callback : mCallbacks) {
                callback.onSubFull();
            }
            return;
        }
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mSubscriptionDao != null) {
                    mSubscriptionDao.addAlbum(album);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void deleteSubscription(Album album) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mSubscriptionDao != null) {
                    mSubscriptionDao.delAlbum(album);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void getSubscriptionList() {
        listSubscriptions();
    }

    @Override
    public boolean isSub(Album album) {
        Album result = mData.get(album.getId());

        //不为空  表示已经订阅
        return result != null;
    }

    @Override
    public void registerViewCallBack(ISubscriptionCallBack iSubscriptionCallBack) {
        if (!mCallbacks.contains(iSubscriptionCallBack)) {
            mCallbacks.add(iSubscriptionCallBack);
        }
    }

    @Override
    public void unRegisterViewCallBack(ISubscriptionCallBack iSubscriptionCallBack) {
        mCallbacks.remove(iSubscriptionCallBack);
    }

    @Override
    public void onAddResult(final boolean isSuccess) {
        //添加结果的回调
        listSubscriptions();
        BaseApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (ISubscriptionCallBack callback : mCallbacks) {
                    callback.onAddResult(isSuccess);
                }
            }
        });
    }

    @Override
    public void onDelResult(final boolean isSuccess) {
        listSubscriptions();
        //删除订阅的回调
        BaseApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (ISubscriptionCallBack callback : mCallbacks) {
                    callback.onDeleteResult(isSuccess);
                }
            }
        });
    }

    @Override
    public void onSubListLoaded(final List<Album> result) {
        mData.clear();
        //加载数据的回调
        for (Album album : result) {
            mData.put(album.getId(),album);
        }
        //通知UI更新
        BaseApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (ISubscriptionCallBack callback : mCallbacks) {
                    callback.onSubscriptionsLoaded(result);
                }
            }
        });
    }
}
