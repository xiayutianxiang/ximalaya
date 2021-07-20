package com.example.ximalaya.data;

import android.util.ArrayMap;

import com.example.ximalaya.utils.Constants;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.HashMap;
import java.util.Map;

import static com.example.ximalaya.utils.Constants.COUNT_DEFAULT;
import static com.example.ximalaya.utils.Constants.COUNT_HOT_WORD;


public class XimalayApi {

    private XimalayApi(){

    }

    private static XimalayApi sXimalayApi;

    public static XimalayApi getXimalayApi(){
        if (sXimalayApi == null) {
            synchronized (XimalayApi.class){
                if(sXimalayApi == null){
                    sXimalayApi = new XimalayApi();
                }
            }
        }
        return sXimalayApi;
    }

    /**
     * 获取推荐的数据
     * @param callBack  请求结果的接口
     */

    public void getRecommendList(IDataCallBack<GussLikeAlbumList> callBack){
        Map<String, String> map = new HashMap<>();
        //这个参数表示一页数据返回多少条
        map.put(DTransferConstants.LIKE_COUNT, Constants.COUNT_RECOMMEND+"");
        CommonRequest.getGuessLikeAlbum(map,callBack);
    }

    /**
     * 根据专辑id获取到专辑内容
     * @param callBack  获取专辑详情数据回调
     * @param albumId   专辑Id
     * @param pageIndex  页码 ，第几页
     */
    public void getAlbumDetail(IDataCallBack<TrackList> callBack,long albumId,int pageIndex){
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.ALBUM_ID, albumId+"");
        map.put(DTransferConstants.SORT, "asc"); //正序
        map.put(DTransferConstants.PAGE, pageIndex +"");
        map.put(DTransferConstants.PAGE_SIZE,COUNT_DEFAULT+"");
        CommonRequest.getTracks(map,callBack);
    }

    /**
     *根据关键字搜索
     * @param keyWord
     * @param page
     */
    public void searchByKeyWord(String keyWord,int page,IDataCallBack<SearchAlbumList> callback) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.SEARCH_KEY, keyWord);
        map.put(DTransferConstants.PAGE, page+"");
        map.put(DTransferConstants.PAGE_SIZE, COUNT_DEFAULT+"");
        CommonRequest.getSearchedAlbums(map,callback);
    }

    /**
     * 获取推荐的热词
     * @param callback
     */
    public void getHotWords(IDataCallBack<HotWordList> callback){
        Map<String,String> map = new ArrayMap<>();
        map.put(DTransferConstants.TOP,COUNT_HOT_WORD + "");
        CommonRequest.getHotWords(map,callback);
    }

    /**
     * 获取关键字的热词
     * @param keyWord
     * @param callback
     */
    public void getSuggestWord(String keyWord,IDataCallBack<SuggestWords> callback){
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.SEARCH_KEY, keyWord);
        CommonRequest.getSuggestWord(map,callback);
    }
}
