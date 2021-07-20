package com.example.ximalaya.presenter;

import com.example.ximalaya.data.XimalayApi;
import com.example.ximalaya.interfaces.ISearchCallBack;
import com.example.ximalaya.interfaces.ISearchPresenter;
import com.example.ximalaya.utils.Constants;
import com.example.ximalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.ArrayList;
import java.util.List;

public class SearchPresenter implements ISearchPresenter {

    private static final String TAG = "SearchPresenter";
    private List<ISearchCallBack> mCallBack = new ArrayList<>();
    private String mCurrentKeyWord = null;  //当前搜索关键字
    private final XimalayApi mXimalayApi;
    private static final int DEFAULT_PAGE = 1;
    private static int mCurrentPage = DEFAULT_PAGE;

    private List<Album> mSearchResult = new ArrayList<>();

    //单例
    private SearchPresenter() {
        mXimalayApi = XimalayApi.getXimalayApi();
    }

    private static SearchPresenter sSearchPresenter = null;

    public static SearchPresenter getSearchPresenter() {
        if (sSearchPresenter == null) {
            synchronized (SearchPresenter.class) {
                if (sSearchPresenter == null) {
                    sSearchPresenter = new SearchPresenter();
                }
            }
        }
        return sSearchPresenter;
    }

    @Override
    public void doSearch(String keyWord) {
        mCurrentPage = DEFAULT_PAGE;
        mSearchResult.clear();
        //用于新搜索
        //当网络不好时，用户点击重新搜索
        this.mCurrentKeyWord = keyWord;
        search(keyWord);
    }

    private boolean mIsloadMore = false;

    private void search(String keyWord) {
        mXimalayApi.searchByKeyWord(keyWord, mCurrentPage, new IDataCallBack<SearchAlbumList>() {
            @Override
            public void onSuccess(SearchAlbumList searchAlbumList) {
                List<Album> albums = searchAlbumList.getAlbums();
                mSearchResult.addAll(albums);
                if (albums != null) {
                    LogUtil.d(TAG, "albums size ---- > " + albums.size());
                    if(mIsloadMore){
                        for (ISearchCallBack iSearchCallBack : mCallBack) {
                            if(albums.size() == 0){
                                iSearchCallBack.onLoadedMoreResult(mSearchResult,false);
                            }else {
                                iSearchCallBack.onLoadedMoreResult(mSearchResult,true);
                            }
                        }
                        mIsloadMore = false;
                    }else {
                        for (ISearchCallBack iSearchCallBack : mCallBack) {
                            iSearchCallBack.onSearchResultLoaded(mSearchResult);
                        }
                    }
                } else {
                    LogUtil.d(TAG, "albums is null");
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.d(TAG, "errorCode ---- > " + errorCode);
                LogUtil.d(TAG, "errorMsg ---- > " + errorMsg);

                for (ISearchCallBack iSearchCallBack : mCallBack) {
                    if (mIsloadMore) {
                        iSearchCallBack.onLoadedMoreResult(mSearchResult, false);
                        mCurrentPage--;
                        mIsloadMore = false;
                    } else {
                        iSearchCallBack.onError(errorCode, errorMsg);
                    }
                }
            }
        });
    }

    @Override
    public void reSearch() {
        search(mCurrentKeyWord);
    }

    @Override
    public void loadMore() {
        if (mSearchResult.size() < Constants.COUNT_DEFAULT) {
            for (ISearchCallBack iSearchCallBack : mCallBack) {
                iSearchCallBack.onLoadedMoreResult(mSearchResult, false);
            }
        } else {
            mIsloadMore = true;
            mCurrentPage++;
            search(mCurrentKeyWord);
        }
    }

    @Override
    public void getHotWord() {
        //做热词缓存
        mXimalayApi.getHotWords(new IDataCallBack<HotWordList>() {
            @Override
            public void onSuccess(HotWordList hotWordList) {
                if (hotWordList != null) {
                    List<HotWord> hotWords = hotWordList.getHotWordList();
                    LogUtil.d(TAG, "hotWords size --- > " + hotWords.size());
                    for (ISearchCallBack iSearchCallBack : mCallBack) {
                        iSearchCallBack.onHotWordLoaded(hotWords);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.d(TAG, "errorCode --- > " + errorCode);
                LogUtil.d(TAG, "errorMsg --- > " + errorMsg);
                for (ISearchCallBack iSearchCallBack : mCallBack) {
                    iSearchCallBack.onError(errorCode, errorMsg);
                }
            }
        });
    }

    @Override
    public void getRecommendWord(String keyWord) {
        mXimalayApi.getSuggestWord(keyWord, new IDataCallBack<SuggestWords>() {
            @Override
            public void onSuccess(SuggestWords suggestWords) {
                if (suggestWords != null) {
                    List<QueryResult> keyWordList = suggestWords.getKeyWordList();
                    LogUtil.d(TAG, "keyWordList --- >" + keyWordList.size());
                    for (ISearchCallBack iSearchCallBack : mCallBack) {
                        iSearchCallBack.onRecommendWordLoaded(keyWordList);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.d(TAG, "getRecommendWord errorCode --- > " + errorCode);
                LogUtil.d(TAG, "getRecommendWord errorMsg --- > " + errorMsg);
                for (ISearchCallBack iSearchCallBack : mCallBack) {
                    iSearchCallBack.onError(errorCode, errorMsg);
                }
            }
        });
    }

    @Override
    public void registerViewCallBack(ISearchCallBack iSearchCallBack) {
        if (!mCallBack.contains(iSearchCallBack)) {
            mCallBack.add(iSearchCallBack);
        }
    }

    @Override
    public void unRegisterViewCallBack(ISearchCallBack iSearchCallBack) {
        mCallBack.remove(iSearchCallBack);
    }
}
