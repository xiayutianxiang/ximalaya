package com.example.ximalaya;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.example.ximalaya.adapters.PlayerTrackPagerAdapter;
import com.example.ximalaya.base.BaseActivity;
import com.example.ximalaya.interfaces.IPlayerCallBack;
import com.example.ximalaya.presenter.PlayerPresenter;
import com.example.ximalaya.utils.LogUtil;
import com.example.ximalaya.views.SobPopWindow;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

public class PlayerActivity extends BaseActivity implements IPlayerCallBack, ViewPager.OnPageChangeListener {

    private static final String TAG = "PlayerActivity";
    private ImageView mControlBtn;
    private PlayerPresenter mPlayerPresenter;

    private SimpleDateFormat mMinFormat = new SimpleDateFormat("mm:ss");
    private SimpleDateFormat mHourFormat = new SimpleDateFormat("hh:mm:ss");
    private TextView mTotalDuration;
    private TextView mCurrentPosition;
    private SeekBar mDurationBar;
    private int mCurrentProgress = 0; //???????????????
    private boolean mIsUserTouchProgress = false;
    private ImageView mPlayNextBtn;
    private ImageView mPlayPreBtn;
    private TextView mTrackTitleTv;
    private ViewPager mTrackPagerView;
    private PlayerTrackPagerAdapter mTrackPagerAdapter;
    private boolean mIsUserSlidePage = false;
    private ImageView mPlayModeSwitchBtn;

    public final int BG_ANIMATION_DURATION = 500;
    private XmPlayListControl.PlayMode mCurrentMode = PLAY_MODEL_LIST;

    private static Map<XmPlayListControl.PlayMode,XmPlayListControl.PlayMode> sPlayModeRule = new HashMap<>();
    //???????????????????????????
    //1.????????????????????? PLAY_MODEL_LIST
    //2.???????????? PLAY_MODEL_LIST_LOOP
    //3.???????????? PLAY_MODEL_RANDOM
    //4.???????????? PLAY_MODEL_SINGLE_LOOP

    static {
        sPlayModeRule.put(PLAY_MODEL_LIST,PLAY_MODEL_LIST_LOOP);
        sPlayModeRule.put(PLAY_MODEL_LIST_LOOP,PLAY_MODEL_RANDOM);
        sPlayModeRule.put(PLAY_MODEL_RANDOM,PLAY_MODEL_SINGLE_LOOP);
        sPlayModeRule.put(PLAY_MODEL_SINGLE_LOOP,PLAY_MODEL_LIST);
    }

    private ImageView mPlayListBtn;
    private SobPopWindow mSobPopWindow;
    private ValueAnimator mEnterBgAnimator;
    private ValueAnimator mExitBgAnimator;
    private String mTrackTitleText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        LogUtil.d(TAG, "PlayerActivity --- >");
        //????????????
        initView();
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.registerViewCallBack(this);

        initEvent();
        initBgAnimation(); //????????????????????????
    }

    private void initBgAnimation() {
        //???????????????
        mEnterBgAnimator = ValueAnimator.ofFloat(1.0f,0.6f);
        mEnterBgAnimator.setDuration(BG_ANIMATION_DURATION);
        mEnterBgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value =(float) animation.getAnimatedValue();
                //LogUtil.d(TAG,"value --- > " + animation.getAnimatedValue());
                updateBgAlpha(value);
            }
        });
        //???????????????
        mExitBgAnimator = ValueAnimator.ofFloat(0.6f, 1.0f);
        mExitBgAnimator.setDuration(BG_ANIMATION_DURATION);
        mExitBgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                updateBgAlpha(value);
            }
        });
    }

    /**
     * ??????????????????
     */
    private void initView() {
        mControlBtn = this.findViewById(R.id.play_or_pause_btn);
        mTotalDuration = this.findViewById(R.id.track_duration);
        mCurrentPosition = this.findViewById(R.id.current_position);
        mDurationBar = this.findViewById(R.id.track_seek_bar);
        mPlayNextBtn = this.findViewById(R.id.play_next);
        mPlayPreBtn = this.findViewById(R.id.play_pre);
        mTrackTitleTv = this.findViewById(R.id.track_title);

        mTrackPagerView = this.findViewById(R.id.track_pager_view);
        //???????????????
        mTrackPagerAdapter = new PlayerTrackPagerAdapter();

        //???????????????
        mTrackPagerView.setAdapter(mTrackPagerAdapter);

        //???????????????????????????
        mPlayModeSwitchBtn = this.findViewById(R.id.player_mode_switch_btn);

        //????????????
        mPlayListBtn = this.findViewById(R.id.play_list);
        mSobPopWindow = new SobPopWindow();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPlayerPresenter != null) {
            mPlayerPresenter.unRegisterViewCallBack(this);
            mPlayerPresenter = null;
        }
    }

    /**
     * ??????????????????????????????
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        mControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //??????????????????????????????????????????
                if (mPlayerPresenter.isPlaying()) {
                    mPlayerPresenter.pause();
                } else {
                    //?????????????????????????????????
                    mPlayerPresenter.play();

                }

            }
        });

        mDurationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mCurrentProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsUserTouchProgress = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //?????????????????????????????????????????????
                mIsUserTouchProgress = false;
                mPlayerPresenter.seekTo(mCurrentProgress);
            }
        });

        mPlayPreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //???????????????
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.playPre();
                }
            }
        });

        mPlayNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //???????????????
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.playNext();
                }
            }
        });

        mTrackPagerView.addOnPageChangeListener(this);
        mTrackPagerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mIsUserSlidePage = true;
                        break;
                }
                return false;
            }
        });

        mPlayModeSwitchBtn.setOnClickListener(v -> {
            switchPlayMode();
        });

        mPlayListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:??????????????????
                mSobPopWindow.showAtLocation(v, Gravity.BOTTOM,0,0);
                //??????????????????????????????

                //????????????????????????????????????
                mEnterBgAnimator.start();
            }
        });
        mSobPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //pop?????????????????????????????????
                mExitBgAnimator.start();
            }
        });
        mSobPopWindow.setPlayListItemClickListener(new SobPopWindow.PlayListItemClickListener() {
            @Override
            public void omItemClick(int position) {
                //???????????????item????????????
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.playByIndex(position);
                }
            }
        });

        mSobPopWindow.setPlayListActionListener(new SobPopWindow.PlayListActionListener() {
            @Override
            public void onPlayModeClick() {
                switchPlayMode();
            }

            @Override
            public void onOrderClick() {
                //??????????????????????????????
                // Toast.makeText(PlayerActivity.this,"??????????????????",Toast.LENGTH_SHORT).show();
                if(mPlayerPresenter != null){
                    mPlayerPresenter.reversePlayList();
                }

            }
        });
    }

    private void switchPlayMode() {
        //???????????????MODE??????????????????Mode
        XmPlayListControl.PlayMode playMode = sPlayModeRule.get(mCurrentMode);
        //??????????????????
        if (mPlayerPresenter != null) {
            mPlayerPresenter.switchPlayMode(playMode);
        }
    }

    public void updateBgAlpha(float alpha){
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.alpha = alpha;
        window.setAttributes(attributes);
    }

    /**
     * ?????????????????????????????????????????????
     */
    private void updatePlayModeBtnImg() {
        int resId = R.drawable.selector_play_mode_list_order;
        switch (mCurrentMode){
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_play_mode_list_order;
                break;
            case PLAY_MODEL_RANDOM:
                resId = R.drawable.selector_play_mode_random;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId = R.drawable.selector_play_mode_list_order_looper;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId = R.drawable.selector_play_mode_single_loop;
                break;
        }
        mPlayModeSwitchBtn.setImageResource(resId);
    }

    @Override
    public void onPlayStart() {
        //?????????????????????UI??????????????????
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_pause);
        }
    }

    @Override
    public void onPlayPause() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_play);
        }
    }

    @Override
    public void onPlayStop() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_play);
        }
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
        //LogUtil.d(TAG,"list ---- >" + list);
        //??????????????????????????? (????????????)
        if (mTrackPagerAdapter != null) {
            mTrackPagerAdapter.setData(list);
        }
        //????????????????????????
        if (mSobPopWindow != null) {
            mSobPopWindow.setListData(list);
        }
    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode playMode) {
        //??????????????????????????????UI
        mCurrentMode = playMode;

        //??????pop??????????????????
        mSobPopWindow.upDatePlayMode(playMode);

        updatePlayModeBtnImg();
    }

    @Override
    public void onProgressChange(int currentProgress, int total) {
        mDurationBar.setMax(total);
        //????????????????????????????????????
        String totalDuration;
        String currentPosition;
        if (total > 1000 * 60 * 60) {
            totalDuration = mHourFormat.format(total);
            currentPosition = mHourFormat.format(currentProgress);
        } else {
            totalDuration = mMinFormat.format(total);
            currentPosition = mMinFormat.format(currentProgress);
        }
        if (mTotalDuration != null) {
            mTotalDuration.setText(totalDuration);
        }

        //?????????????????????
        if (mCurrentPosition != null) {
            mCurrentPosition.setText(currentPosition);
        }
        //????????????
        //??????????????????
        if (!mIsUserTouchProgress) {
            mDurationBar.setProgress(currentProgress);
        }
    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinishing() {

    }

    @Override
    public void onTrackUpDate(Track track, int playIndex) {
        if(track == null){
            LogUtil.d(TAG,"onTrackUpDate --- > track null");
        }
        this.mTrackTitleText = track.getTrackTitle();
        if (mTrackTitleTv != null) {
            //???????????????????????????
            mTrackTitleTv.setText(mTrackTitleText);
        }
        //????????????????????????????????????????????????????????????????????????
        //?????????????????????????????????????????????????????????
        if (mTrackPagerView != null) {
            mTrackPagerView.setCurrentItem(playIndex, true);
        }
        //????????????????????????????????????
        if (mSobPopWindow != null) {
            mSobPopWindow.setCurrentPlayPosition(playIndex);
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {
        mSobPopWindow.updateOrderIcon(isReverse);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        LogUtil.d(TAG, "position --- >" + position);
        //???????????????????????? ???????????????????????????
        if (mPlayerPresenter != null && mIsUserSlidePage) {
            mPlayerPresenter.playByIndex(position);
        }
        mIsUserSlidePage = false;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
