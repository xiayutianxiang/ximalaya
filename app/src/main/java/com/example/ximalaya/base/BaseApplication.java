package com.example.ximalaya.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.example.ximalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.DeviceInfoProviderDefault;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDeviceInfoProvider;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;


public class BaseApplication extends Application {
    private static Handler sHandler = null;
    private static Context sContext = null;
    @Override
    public void onCreate() {
        super.onCreate();
        CommonRequest mXimalaya = CommonRequest.getInstanse();
        if(DTransferConstants.isRelease) {
            String mAppSecret = "8646d66d6abe2efd14f2891f9fd1c8af";
            mXimalaya.setAppkey("9f9ef8f10bebeaa83e71e62f935bede8");
            mXimalaya.setPackid("com.app.test.android");
            mXimalaya.init(this ,mAppSecret, getDeviceInfoProvider(this));
        } else {
            String mAppSecret = "0a09d7093bff3d4947a5c4da0125972e";
            mXimalaya.setAppkey("f4d8f65918d9878e1702d49a8cdf0183");
            mXimalaya.setPackid("com.ximalaya.qunfeng");
            mXimalaya.init(this ,mAppSecret, getDeviceInfoProvider(this));
        }

        /**
         * 初始化播放器
         */
        XmPlayerManager.getInstance(this).init();

        //初始化log
        LogUtil.init(this.getPackageName(),false);
        sHandler = new Handler();

        sContext = getBaseContext();
    }

    public static Context getAppContext(){
        return sContext;
    }

    public static Handler getHandler(){
        return sHandler;
    }

    public IDeviceInfoProvider getDeviceInfoProvider(Context context) {
        return new DeviceInfoProviderDefault(context) {
            @Override
            public String oaid() {
                return "!!!这里要传入真正的oaid oaid 接入请访问 http://www.msa-alliance.cn/col.jsp?id=120";
            }
        };
    }
}
