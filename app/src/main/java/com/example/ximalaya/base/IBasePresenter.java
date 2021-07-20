package com.example.ximalaya.base;

public interface IBasePresenter<T> {

    /**
     * 注册ui的接口
     * @param t
     */

    void registerViewCallBack(T t);

    /**
     * 取消接口
     * @param m
     */
    void unRegisterViewCallBack(T t);

}
