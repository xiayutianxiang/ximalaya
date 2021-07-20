package com.example.ximalaya.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ximalaya.R;
import com.example.ximalaya.adapters.PlayListAdapter;
import com.example.ximalaya.base.BaseApplication;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

public class SobPopWindow extends PopupWindow {

    private final View mPopView;
    private TextView mCloseBtn;
    private RecyclerView mTrackList;
    private PlayListAdapter mPlayListAdapter;
    private TextView mPlayModeTv;
    private ImageView mPlayModeIv;
    private View mPlayModeContainer;
    private PlayListActionListener mPlayModeClickListener = null;
    private View mPlayOrderContainer;
    private ImageView mOrderIcon;
    private TextView mOrderText;

    public SobPopWindow() {
        //设置宽高
        super(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        setOutsideTouchable(true); //点击外部关闭弹窗
        //加载view
        mPopView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.pop_play_list, null);

        //设置内容
        setContentView(mPopView);

        //设置弹窗进入和推出的动画
        setAnimationStyle(R.style.pop_animation);

        //找到控件
        initView();
        initEvent();
    }

    private void initEvent() {
        //点击关闭弹窗
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SobPopWindow.this.dismiss();
            }
        });

        mPlayModeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:切换播放模式
                if (mPlayModeClickListener != null) {
                    mPlayModeClickListener.onPlayModeClick();
                }
            }
        });

        //点击排序，正序和逆序
        mPlayOrderContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayModeClickListener.onOrderClick();

            }
        });
    }

    private void initView() {
        mCloseBtn = mPopView.findViewById(R.id.play_list_close_btn);

        //RecyclerView
        //找到控件
        mTrackList = mPopView.findViewById(R.id.play_list_rv);
        //播放模式相关
        mPlayModeTv = mPopView.findViewById(R.id.play_list_play_mode_tv);
        mPlayModeIv = mPopView.findViewById(R.id.play_list_play_mode_iv);
        mPlayModeContainer = mPopView.findViewById(R.id.play_list_play_mode_container);
        //设置布局管理器
        mTrackList.setLayoutManager(new LinearLayoutManager(BaseApplication.getAppContext()));
        //设置适配器
        mPlayListAdapter = new PlayListAdapter();
        mTrackList.setAdapter(mPlayListAdapter);
        mPlayOrderContainer = mPopView.findViewById(R.id.play_list_order_container);
        mOrderIcon = mPopView.findViewById(R.id.play_list_oder_iv);
        mOrderText = mPopView.findViewById(R.id.play_list_order_tv);

    }

    /**
     * 给设配器设置数据
     * @param data
     */
    public void setListData(List<Track> data){
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setData(data);
        }
    }

    public void setCurrentPlayPosition(int position){
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setCurrentPlayPosition(position);
            mTrackList.scrollToPosition(position);
        }
    }

    public void setPlayListItemClickListener(PlayListItemClickListener listener){
        mPlayListAdapter.setOniItemClickListener(listener);
    }

    /**
     * 更新切换列表时的文字和图标
     * @param isOrder
     */
    public void updateOrderIcon(boolean isOrder){
        mOrderIcon.setImageResource(isOrder ? R.drawable.selector_play_mode_list_order : R.drawable.selector_play_mode_list_revers);
        mOrderText.setText(isOrder ? R.string.order_text : R.string.revers_text);
    }

    /**
     * 更新播放列表的播放模式
     * @param playMode
     */
    public void upDatePlayMode(XmPlayListControl.PlayMode playMode) {
        updatePlayModeBtnImg(playMode);
    }

    /**
     * 根据当前状态，更新图标播放模式
     */
    private void updatePlayModeBtnImg(XmPlayListControl.PlayMode playMode) {
        int resId = R.drawable.selector_play_mode_list_order;
        int textId = R.string.play_mode_order_text;
        switch (playMode){
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_play_mode_list_order;
                textId = R.string.play_mode_order_text;
                break;
            case PLAY_MODEL_RANDOM:
                resId = R.drawable.selector_play_mode_random;
                textId = R.string.play_mode_random_text;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId = R.drawable.selector_play_mode_list_order_looper;
                textId = R.string.play_mode_list_play_text;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId = R.drawable.selector_play_mode_single_loop;
                textId = R.string.play_mode_single_play_text;
                break;
        }
        mPlayModeIv.setImageResource(resId);
        mPlayModeTv.setText(textId);
    }

    public interface PlayListItemClickListener{
        void omItemClick(int position);
    }

    public void setPlayListActionListener(PlayListActionListener playModeClickListener){
        mPlayModeClickListener = playModeClickListener;
    }

    public interface PlayListActionListener {
        //播放模式被点击
        void onPlayModeClick();
        //播放逆序和正序
        void onOrderClick();
    }
}
