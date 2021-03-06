package com.example.ximalaya;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ximalaya.adapters.TrackLsitAdapter;
import com.example.ximalaya.base.BaseActivity;
import com.example.ximalaya.interfaces.IAlbumDetailViewCallBack;
import com.example.ximalaya.interfaces.IPlayerCallBack;
import com.example.ximalaya.interfaces.ISubscriptionCallBack;
import com.example.ximalaya.presenter.AlbumDetailPresenter;
import com.example.ximalaya.presenter.PlayerPresenter;
import com.example.ximalaya.presenter.SubscriptionPresenter;
import com.example.ximalaya.utils.ImageBlur;
import com.example.ximalaya.utils.LogUtil;
import com.example.ximalaya.views.RoundRectImageView;
import com.example.ximalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.header.bezierlayout.BezierLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class DetailActivity extends BaseActivity implements IAlbumDetailViewCallBack, UILoader.OnRetryClickListener,
        TrackLsitAdapter.ItemClickListener, IPlayerCallBack, ISubscriptionCallBack {
    private static final String TAG = "DetailActivity";
    private ImageView mLargeCover;
    private RoundRectImageView mSmallCover;
    private TextView mAlbumTitle;
    private TextView mAlbumAuthor;
    private AlbumDetailPresenter mAlbumDetailPresenter;
    private int mCurrentPage = 1;
    private RecyclerView mDetailList;
    private TrackLsitAdapter mDetailListAdapter;
    private FrameLayout mDetailListContainer;
    private UILoader mUiLoader;
    private long mCurrentId = -1;
    private ImageView mPlayControlBtn;
    private TextView mPlayControlTips;
    private PlayerPresenter mPlayerPresenter;
    private List<Track> mCurrentTracks = null;

    private final static int DEFAULT_PLAY_INDEX = 0;
    private TwinklingRefreshLayout mRefreshLayout;
    private String mCurrentTrackTitle = null;
    private SubscriptionPresenter mSubscriptionPresenter;
    private Album mCurrentAlbum = null;
    private TextView mSubBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        initView();

        initPresenter();
        //???????????????????????????
        upDateSubState();
        updatePlayState(mPlayerPresenter.isPlaying());
        initListener();
    }

    private void upDateSubState() {
        if (mSubscriptionPresenter != null) {
            boolean isSub = mSubscriptionPresenter.isSub(mCurrentAlbum);
            mSubBtn.setText(isSub ? R.string.cancel_sub_tips_text : R.string.sub_tips_text);
        }
    }

    private void initPresenter() {
        //???????????????Presenter
        mAlbumDetailPresenter = AlbumDetailPresenter.getInstance();
        mAlbumDetailPresenter.registerViewCallBack(this);

        //????????????Presenter
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.registerViewCallBack(this);

        //???????????????Presenter
        mSubscriptionPresenter = SubscriptionPresenter.getInstance();
        mSubscriptionPresenter.getSubscriptionList();
        mSubscriptionPresenter.registerViewCallBack(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.unRegisterViewCallBack(this);
        }
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unRegisterViewCallBack(this);
        }
        if (mSubscriptionPresenter != null) {
            mSubscriptionPresenter.unRegisterViewCallBack(this);
        }
    }

    private void initListener() {
        mSubBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSubscriptionPresenter != null) {
                    boolean isSub = mSubscriptionPresenter.isSub(mCurrentAlbum);
                    //????????????????????????????????????????????????
                    if (isSub) {
                        mSubscriptionPresenter.deleteSubscription(mCurrentAlbum);
                    } else {
                        mSubscriptionPresenter.addSubscription(mCurrentAlbum);
                    }
                }
            }
        });

        mPlayControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mPlayerPresenter != null) {
                    //????????????????????????????????????
                    boolean has = mPlayerPresenter.hasPlayList();
                    if (has) {
                        handlerPlayControl();
                    } else {
                        handlerNoPlayList();
                    }

                }
            }
        });
    }

    /**
     * ?????????????????????????????????????????????
     */
    private void handlerNoPlayList() {
        mPlayerPresenter.setPlayList(mCurrentTracks, DEFAULT_PLAY_INDEX);
    }

    private void handlerPlayControl() {
        //?????????????????????
        if (mPlayerPresenter.isPlaying()) {
            //?????????????????????
            mPlayerPresenter.pause();
        } else {
            //?????????????????????
            mPlayerPresenter.play();
        }
    }

    private void initView() {
        //????????????
        mDetailListContainer = this.findViewById(R.id.detail_list_container);

        if (mUiLoader == null) {
            mUiLoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(container);
                }
            };
            mDetailListContainer.removeAllViews();
            mDetailListContainer.addView(mUiLoader);
            mUiLoader.setOnRetryClickListener(DetailActivity.this);
        }

        mLargeCover = this.findViewById(R.id.iv_large_bg);
        mSmallCover = this.findViewById(R.id.viv_small_cover);
        mAlbumTitle = this.findViewById(R.id.tv_album_title);
        mAlbumAuthor = this.findViewById(R.id.tv_album_author);

        //?????????????????????
        mPlayControlBtn = this.findViewById(R.id.detail_play_control);
        mPlayControlTips = this.findViewById(R.id.play_control_tv);
        mPlayControlTips.setSelected(true);

        //????????????
        mSubBtn = this.findViewById(R.id.detail_sub_btn);
    }

    private boolean mIsLoadedMore = false;

    private View createSuccessView(ViewGroup container) {
        View detailListView = LayoutInflater.from(this).inflate(R.layout.item_detail_list, container, false);
        mDetailList = detailListView.findViewById(R.id.album_detail_list);
        mRefreshLayout = detailListView.findViewById(R.id.refresh_layout);
        //?????????????????????????????????
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mDetailList.setLayoutManager(linearLayoutManager);
        //???????????????????????????
        mDetailListAdapter = new TrackLsitAdapter();
        mDetailList.setAdapter(mDetailListAdapter);
        //??????Item???????????????
        mDetailList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });
        mDetailListAdapter.setItemClickListener(this);
        BezierLayout headerView = new BezierLayout(this);
        mRefreshLayout.setHeaderView(headerView);
        mRefreshLayout.setMaxHeadHeight(140);
        mRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                super.onRefresh(refreshLayout);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.finishRefreshing();
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                super.onLoadMore(refreshLayout);
                //TODO:??????????????????
                if (mAlbumDetailPresenter != null) {
                    mAlbumDetailPresenter.loadMore();
                    mIsLoadedMore = true;
                }
            }
        });
        return detailListView;
    }

    @Override
    public void onDetailListLoaded(List<Track> tracks) {

        if (mIsLoadedMore && mRefreshLayout != null) {
            mIsLoadedMore = false;
            mRefreshLayout.finishLoadmore();
        }
        this.mCurrentTracks = tracks;
        //???????????????????????????????????????
        if (tracks == null || tracks.size() == 0) {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
            }
        }

        //?????????
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }

        //??????????????????
        mDetailListAdapter.setData(tracks);
    }

    @Override
    public void onNetworkError(int errorCode, String errorMsg) {
        //???????????????????????????????????????
        mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
    }

    @Override
    public void onAlbumLoaded(Album album) {
        this.mCurrentAlbum = album;
        //????????????????????????
        Long sid = album.getId();
        LogUtil.d(TAG, "album--->" + sid);
        int id = new Long(sid).intValue(); //??????intValue???long??????int??????
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail(id, mCurrentPage);
        }
        //??????????????????Loading??????
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        }
        //???????????????????????????
        if (mAlbumTitle != null) {
            mAlbumTitle.setText(album.getAlbumTitle());
        }
        if (mAlbumAuthor != null) {
            mAlbumAuthor.setText(album.getAnnouncer().getNickname());
        }
        //??????????????????
        if (mLargeCover != null && null != mLargeCover) {
            Picasso.with(this).load(album.getCoverUrlLarge()).into(mLargeCover, new Callback() {
                @Override
                public void onSuccess() {
                    Drawable drawable = mLargeCover.getDrawable();
                    if (drawable != null) {
                        //?????????????????????????????????
                        ImageBlur.makeBlur(mLargeCover, DetailActivity.this);
                    }
                }

                @Override
                public void onError() {
                    LogUtil.d(TAG, "onError");
                }
            });
        }
        if (mSmallCover != null) {
            Picasso.with(this).load(album.getCoverUrlLarge()).into(mSmallCover);
        }
    }

    @Override
    public void onLoaderMoreFinished(int size) {
        if (size > 0) {
            Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefreshFinished(int size) {

    }

    @Override
    public void onRetryClick() {
        //????????????????????????????????????????????????????????????
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail((int) mCurrentId, mCurrentPage);
        }
    }

    @Override
    public void onItemClick(List<Track> detailData, int position) {
        //????????????????????????
        PlayerPresenter playerPresenter = PlayerPresenter.getPlayerPresenter();
        playerPresenter.setPlayList(detailData, position);
        //TODO:?????????????????????
        Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPlayStart() {
        //?????????????????????????????????????????????????????????
        updatePlayState(true);
    }

    @Override
    public void onPlayPause() {
        //??????????????????????????????????????????????????????
        updatePlayState(false);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param playing
     */
    private void updatePlayState(boolean playing) {
        if (mPlayControlBtn != null && mPlayControlTips != null) {
            mPlayControlBtn.setImageResource(playing ? R.drawable.selector_play_control_play : R.drawable.selector_play_control_pause);
            if (!playing) {
                mPlayControlTips.setText(R.string.click_play_tips_text);
            } else {
                if (!TextUtils.isEmpty(mCurrentTrackTitle)) {
                    mPlayControlTips.setText(mCurrentTrackTitle);
                }
            }
        }
    }

    @Override
    public void onPlayStop() {
        updatePlayState(false);
    }

    @Override
    public void onPlayError() {

    }

    @Override
    public void nextPlay(Track track) {

    }

    @Override
    public void onPrePlay(Track track) {

    }

    @Override
    public void onListLoaded(List<Track> list) {

    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode playMode) {

    }

    @Override
    public void onProgressChange(int currentProgress, int total) {

    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinishing() {

    }

    @Override
    public void onTrackUpDate(Track track, int playIndex) {
        if (track != null) {
            mCurrentTrackTitle = track.getTrackTitle();
            if (!TextUtils.isEmpty(mCurrentTrackTitle) && mPlayControlTips != null) {
                mPlayControlTips.setText(mCurrentTrackTitle);
            }
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }

    @Override
    public void onAddResult(boolean isSuccess) {
        if (isSuccess) {
            //?????????????????????UI???????????????
            mSubBtn.setText(R.string.cancel_sub_tips_text);
        }
        String tipsText = isSuccess ? "????????????" : "????????????";
        Toast.makeText(DetailActivity.this, tipsText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteResult(boolean isSuccess) {
        if (isSuccess) {
            //?????????????????????UI??? + ??????
            mSubBtn.setText(R.string.sub_tips_text);
        }
        String tipsText = isSuccess ? "????????????" : "????????????";
        Toast.makeText(DetailActivity.this, tipsText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubscriptionsLoaded(List<Album> albums) {

    }

    @Override
    public void onSubFull() {
        //??????
        Toast.makeText(this,"???????????????!",Toast.LENGTH_SHORT).show();
    }
}
