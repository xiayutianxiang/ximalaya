package com.example.ximalaya;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ximalaya.adapters.AlbumListAdapter;
import com.example.ximalaya.adapters.SearchRecommendAdapter;
import com.example.ximalaya.base.BaseActivity;
import com.example.ximalaya.interfaces.ISearchCallBack;
import com.example.ximalaya.presenter.AlbumDetailPresenter;
import com.example.ximalaya.presenter.SearchPresenter;
import com.example.ximalaya.utils.Constants;
import com.example.ximalaya.utils.LogUtil;
import com.example.ximalaya.views.FlowTextLayout;
import com.example.ximalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;

import net.lucode.hackware.magicindicator.buildins.UIUtil;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends BaseActivity implements ISearchCallBack, AlbumListAdapter.OnAlbumItemClickListener {

    private static final String TAG = "SearchActivity";
    private ImageView mBackBtn;
    private EditText mInputBox;
    private TextView mSearchBtn;
    private FrameLayout mResultContainer;
    private SearchPresenter mSearchPresenter;

    private UILoader mUILoader;
    private RecyclerView mResultListView;
    private AlbumListAdapter mAlbumListAdapter;
    private FlowTextLayout mFlowTextLayout;
    private InputMethodManager mManager;
    private ImageView mDelBtn;
    public static final int TIME_SHOW_IMM = 300;
    private RecyclerView mSearchRecommendList;
    private SearchRecommendAdapter mSearchRecommendAdapter;
    private TwinklingRefreshLayout mRefreshLayout;
    private boolean mSuggesstWord = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initView();
        initPresenter();
        initEvent();
    }

    private void initPresenter() {
        mManager = ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE));
        mSearchPresenter = SearchPresenter.getSearchPresenter();
        //??????UI???????????????
        mSearchPresenter.registerViewCallBack(this);
        //????????????
        mSearchPresenter.getHotWord();
    }

    private void initView() {
        mBackBtn = findViewById(R.id.search_back);
        mInputBox = findViewById(R.id.search_input);
        mDelBtn = findViewById(R.id.search_input_delete);
        mDelBtn.setVisibility(View.GONE);
        //???????????????????????????????????????
        mInputBox.postDelayed(new Runnable() {
            @Override
            public void run() {
                mInputBox.requestFocus();
                mManager.showSoftInput(mInputBox,InputMethodManager.SHOW_IMPLICIT);
            }
        },TIME_SHOW_IMM);

        mSearchBtn = findViewById(R.id.search_btn);
        mResultContainer = findViewById(R.id.search_container);

        if (mUILoader == null) {
            mUILoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView();
                }
                @Override
                protected View getEmptyView() {
                    //????????????
                    View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                    TextView tipsView = emptyView.findViewById(R.id.empty_view_tips_tv);
                    tipsView.setText(R.string.search_no_content_tips_text);
                    return emptyView;
                }
            };
            if (mUILoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUILoader.getParent()).removeView(mUILoader);
            }
            mResultContainer.addView(mUILoader);
        }
    }
    /**
     * ???????????????????????????View
     *
     * @return
     */
    private View createSuccessView() {
        View resultView = LayoutInflater.from(this).inflate(R.layout.search_result_layout, null);

        //???????????????text
        mFlowTextLayout = resultView.findViewById(R.id.recommend_hot_word_view);

        mResultListView = resultView.findViewById(R.id.result_list_view);

        //????????????
        mRefreshLayout = resultView.findViewById(R.id.search_result_refresh);
        mRefreshLayout.setEnableRefresh(false); //??????????????????
        //?????????????????????
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mResultListView.setLayoutManager(linearLayoutManager);
        //???????????????
        mAlbumListAdapter = new AlbumListAdapter();
        mResultListView.setAdapter(mAlbumListAdapter);
        mResultListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                //UIUtil,???????????????px??????dp???dp???px
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });

        //????????????
        mSearchRecommendList = resultView.findViewById(R.id.search_recommend_list);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        mSearchRecommendList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                //UIUtil,???????????????px??????dp???dp???px
                outRect.top = UIUtil.dip2px(view.getContext(), 2);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 2);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });
        mSearchRecommendList.setLayoutManager(linearLayoutManager1);
        mSearchRecommendAdapter = new SearchRecommendAdapter();
        mSearchRecommendList.setAdapter(mSearchRecommendAdapter);
        return resultView;
    }

    private void initEvent() {

        mAlbumListAdapter.setAlbumItemClickListener(this);
        mRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                LogUtil.d(TAG,"????????????");
                //?????????????????????
                int dataSize = mAlbumListAdapter.getDataSize();
                if( dataSize<Constants.COUNT_DEFAULT){
                    //????????????
                    Toast.makeText(SearchActivity.this,"??????????????????",Toast.LENGTH_SHORT).show();
                    mRefreshLayout.finishLoadmore();
                }else {
                    if (mSearchPresenter != null) {
                        mSearchPresenter.loadMore();
                    }
                }
            }
        });

        if (mSearchRecommendAdapter != null) {
            mSearchRecommendAdapter.setItemClickListener(new SearchRecommendAdapter.ItemClickListener() {
                @Override
                public void onItemClick(String keyword) {
                    //LogUtil.d(TAG,"mSearchRecommendAdapter keyword --- > " + keyword);
                    //???????????????
                    mSuggesstWord = false;
                    //?????????????????????
                    switch2Search(keyword);
                }
            });
        }

        mDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputBox.setText("");
            }
        });

        mFlowTextLayout.setClickListener(new FlowTextLayout.ItemClickListener() {
            @Override
            public void onItemClick(String text) {
                mSuggesstWord = false;
                switch2Search(text);
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //????????????????????????
                String keyWord = mInputBox.getText().toString().trim();
                if(TextUtils.isEmpty(keyWord)){
                    Toast.makeText(SearchActivity.this,"???????????????????????????!" ,Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mSearchPresenter != null) {
                    mSearchPresenter.doSearch(keyWord);
                    mUILoader.updateStatus(UILoader.UIStatus.LOADING);
                }
            }
        });

        mInputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    mSearchPresenter.getHotWord();
                    mDelBtn.setVisibility(View.GONE);
                }else {
                    mDelBtn.setVisibility(View.VISIBLE);
                    //??????????????????
                    if(mSuggesstWord){
                        getSuggestWord(s.toString());
                    }else {
                        mSuggesstWord = true;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mUILoader.setOnRetryClickListener(new UILoader.OnRetryClickListener() {
            @Override
            public void onRetryClick() {
                if (mSearchPresenter != null) {
                    mSearchPresenter.reSearch();
                    mUILoader.updateStatus(UILoader.UIStatus.LOADING);
                }
            }
        });
    }

    private void switch2Search(String text) {
        if(TextUtils.isEmpty(text)){
            Toast.makeText(this,"???????????????????????????!" ,Toast.LENGTH_SHORT).show();
            return;
        }
        mInputBox.setText(text);
        if (mSearchPresenter != null) {
            mSearchPresenter.doSearch(text);
        }
        //??????UI??????
        if (mUILoader != null) {
            mUILoader.updateStatus(UILoader.UIStatus.LOADING);
        }
    }

    /**
     * ????????????????????????
     * @param keyword
     */
    private void getSuggestWord(String keyword) {
        LogUtil.d(TAG,"getSuggestWord -- > " + keyword);
        if (mSearchPresenter != null) {
            mSearchPresenter.getRecommendWord(keyword);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSearchPresenter != null) {
            mSearchPresenter.unRegisterViewCallBack(this);
            mSearchPresenter = null;
        }
    }

    @Override
    public void onSearchResultLoaded(List<Album> result) {
        handlerSearchResult(result);
        //??????????????????

        if (mManager != null) {
            mManager.hideSoftInputFromWindow(mInputBox.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

    }

    private void handlerSearchResult(List<Album> result) {
        hideSuccessView();
        mRefreshLayout.setVisibility(View.VISIBLE);
        if (result != null) {
            if (result.size() == 0) {
                //????????????
                if (mUILoader != null) {
                    mUILoader.updateStatus(UILoader.UIStatus.EMPTY);
                }
            } else {
                //????????????????????????????????????
                mAlbumListAdapter.setData(result);
                mUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
            }
        }
    }

    @Override
    public void onHotWordLoaded(List<HotWord> hotWordList) {
        hideSuccessView();
        mFlowTextLayout.setVisibility(View.VISIBLE);
        if(mUILoader != null){
            mUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
        LogUtil.d(TAG, "onHotWordLoaded --- >" + hotWordList.size());
        List<String> hotWords = new ArrayList<>();
        hotWords.clear();
        for (HotWord hotWord : hotWordList) {
            String searchWord = hotWord.getSearchword();
            hotWords.add(searchWord);
        }
        Collections.sort(hotWords);
        //??????UI
        LogUtil.d(TAG,"hotWords --- >"+ hotWords);
        mFlowTextLayout.setTextContents(hotWords);
    }

    @Override
    public void onLoadedMoreResult(List<Album> result, boolean isOkay) {
        //???????????????????????????
        if (mRefreshLayout != null) {
            mRefreshLayout.finishLoadmore();
        }
        if(isOkay){
            handlerSearchResult(result);
        }else {
            Toast.makeText(SearchActivity.this,"??????????????????",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRecommendWordLoaded(List<QueryResult> keyWordList) {
            //???????????????
        LogUtil.d(TAG,"keyWordList size--- > " + keyWordList.size());
        if (mSearchRecommendAdapter != null) {
            mSearchRecommendAdapter.setData(keyWordList);
        }

        //??????UI????????????????????????
        if (mUILoader != null) {
            mUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
        //?????????????????????
        hideSuccessView();
        mSearchRecommendList.setVisibility(View.VISIBLE);
    }


    private void hideSuccessView(){
        mSearchRecommendList.setVisibility(View.GONE);
        mFlowTextLayout.setVisibility(View.GONE);
        mRefreshLayout.setVisibility(View.GONE);
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        if (mUILoader != null) {
            mUILoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
        }
    }

    @Override
    public void onItemClick(int position, Album album) {
        //????????????????????????
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        //???????????????????????????,?????????????????????
        Intent intent = new Intent(this, DetailActivity.class);
        startActivity(intent);
    }
}