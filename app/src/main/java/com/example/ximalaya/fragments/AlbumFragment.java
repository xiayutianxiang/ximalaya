package com.example.ximalaya.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ximalaya.DetailActivity;
import com.example.ximalaya.R;
import com.example.ximalaya.adapters.AlbumListAdapter;
import com.example.ximalaya.base.BaseFragment;
import com.example.ximalaya.interfaces.IRecommendViewCallBack;
import com.example.ximalaya.presenter.AlbumDetailPresenter;
import com.example.ximalaya.presenter.RecommendPresenter;
import com.example.ximalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;


public class AlbumFragment extends BaseFragment implements IRecommendViewCallBack, UILoader.OnRetryClickListener, AlbumListAdapter.OnAlbumItemClickListener {

    private static final String TAG = "AlbumFragment";
    private View mRootView;
    private RecyclerView mRecommendRv;
    private AlbumListAdapter mAlbumListAdapter;
    private RecommendPresenter mRecommendPresenter;
    private UILoader mUiLoader;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        mUiLoader = new UILoader(getContext()) {
            @Override
            protected View getSuccessView(ViewGroup container) {
                return createSuccessView(layoutInflater, container);
            }
        };
        //获取到逻辑层的对象
        mRecommendPresenter = RecommendPresenter.getInstance();
        //先要设置通知接口的注册
        mRecommendPresenter.registerViewCallBack(this);
        //获取推荐列表
        mRecommendPresenter.getRecommendList();

        if (mUiLoader.getParent() instanceof ViewGroup) {
            ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
        }
        mUiLoader.setOnRetryClickListener(this::onRetryClick);
        //返回界面
        return mUiLoader;
    }

    private View createSuccessView(LayoutInflater layoutInflater, ViewGroup container) {
        mRootView = layoutInflater.inflate(R.layout.fragment_recommend, container, false);
        /**
         * 使用Recycrlview
         */
        //1.找到控件+
        mRecommendRv = mRootView.findViewById(R.id.recommend_list);

        TwinklingRefreshLayout twinklingRefreshLayout = mRootView.findViewById(R.id.over_scroll);
        twinklingRefreshLayout.setPureScrollModeOn();
        //2.设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecommendRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                //UIUtil,工具类，将px转为dp，dp转px
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });
        mRecommendRv.setLayoutManager(linearLayoutManager);
        //3.设置适配器
        mAlbumListAdapter = new AlbumListAdapter();
        mRecommendRv.setAdapter(mAlbumListAdapter);
        mAlbumListAdapter.setAlbumItemClickListener(this);
        return mRootView;
    }

    @Override
    public void onRecommendListLoaded(List<Album> result) {
        //获取到推荐内容时，调用此方法
        mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
        //设置数据给适配器，更新ui
        mAlbumListAdapter.setData(result);
    }

    @Override
    public void onNetworkError() {
        mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
    }

    @Override
    public void onEmpty() {
        mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
    }

    @Override
    public void onLoading() {
        mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消接口的注册
        if (mRecommendPresenter != null) {
            mRecommendPresenter.unRegisterViewCallBack(this);
        }
    }

    @Override
    public void onRetryClick() {
        //表示网络不佳的时候，用户点击了重试
        //重回新获取数据即可
        if(mRecommendPresenter != null){
            mRecommendPresenter.getRecommendList();
        }
    }


    @Override
    public void onItemClick(int position, Album album) {
        //根据位置拿到数据
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        //每一行条目被点击了,跳转到详情界面
        Intent intent = new Intent(getContext(), DetailActivity.class);
        startActivity(intent);
    }
}
