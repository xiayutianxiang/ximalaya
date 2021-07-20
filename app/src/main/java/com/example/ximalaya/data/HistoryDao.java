package com.example.ximalaya.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ximalaya.base.BaseApplication;
import com.example.ximalaya.utils.Constants;
import com.example.ximalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.album.Announcer;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.schedulers.Schedulers;

public class HistoryDao implements IHistoryDao {

    private static final String TAG = "HistoryDao";
    private final XimalayDBHelper mDBHelper;
    private IHistoryDaoCallback mCallback = null;
    private Object mLock = new Object();

    public HistoryDao() {
        mDBHelper = new XimalayDBHelper(BaseApplication.getAppContext());
    }

    @Override
    public void setCallback(IHistoryDaoCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void addHistory(Track track) {
        SQLiteDatabase db = null;
        boolean isSuccess = false;
        try {
            db = mDBHelper.getWritableDatabase();
            //先删除
            db.delete(Constants.HISTORY_TB_NAME, Constants.HISTORY_TRACK_ID + "=?", new String[]{track.getDataId() + ""});
            //删除后在进行添加

            db.beginTransaction();
            ContentValues values = new ContentValues();

            values.put(Constants.HISTORY_TRACK_ID, track.getDataId());
            values.put(Constants.HISTORY_TITLE, track.getTrackTitle());
            values.put(Constants.HISTORY_PLAY_COUNT, track.getPlayCount());
            values.put(Constants.HISTORY_DURATION, track.getDuration());
            values.put(Constants.HISTORY_UPDATE_TIME, track.getUpdatedAt());
            values.put(Constants.HISTORY_COVER, track.getCoverUrlLarge());
            values.put(Constants.HISTORY_AUTHOR,track.getAnnouncer().getNickname());

            db.insert(Constants.HISTORY_TB_NAME, null, values);
            db.setTransactionSuccessful();
            isSuccess = true;
        } catch (Exception e) {
            isSuccess = false;
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
            if (mCallback != null) {
                mCallback.onHistoryAdd(isSuccess);
            }
        }
    }

    @Override
    public void delHistory(Track track) {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            boolean isDelSuccess = false;
            try {
                db = mDBHelper.getWritableDatabase();
                db.beginTransaction();

                //删除数据
                int delete = db.delete(Constants.HISTORY_TB_NAME, Constants.HISTORY_TRACK_ID + "=?", new String[]{track.getDataId() + ""});
                LogUtil.d(TAG, "delete ----> " + delete);
                db.setTransactionSuccessful();
                isDelSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
                isDelSuccess = false;
            } finally {
                if (db != null) {
                    db.endTransaction();
                    db.close();
                }
                if (mCallback != null) {
                    mCallback.onHistoryDel(isDelSuccess);
                }
            }
        }
    }

    @Override
    public void clearHistory() {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            boolean isDelSuccess = false;
            try {
                db = mDBHelper.getWritableDatabase();
                db.beginTransaction();

                db.delete(Constants.HISTORY_TB_NAME, null, null);
                db.setTransactionSuccessful();
                isDelSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
                isDelSuccess = false;
            } finally {
                if (db != null) {
                    db.endTransaction();
                    db.close();
                }
                if (mCallback != null) {
                    mCallback.onHistoriesClean(isDelSuccess);
                }
            }
        }
    }

    @Override
    public void listHistories() {
        synchronized (mLock) {
            //从数据表中查出内容
            SQLiteDatabase db = null;
            List<Track> histories = new ArrayList<>();
            try {
                db = mDBHelper.getReadableDatabase();
                db.beginTransaction();
                Cursor query = db.query(Constants.HISTORY_TB_NAME, null, null, null, null, null, "_id desc");
                while (query.moveToNext()) {
                    Track track = new Track();
                    int trackId = query.getInt(query.getColumnIndex(Constants.HISTORY_TRACK_ID));
                    String title = query.getString(query.getColumnIndex(Constants.HISTORY_TITLE));
                    int playCount = query.getInt(query.getColumnIndex(Constants.HISTORY_PLAY_COUNT));
                    int duration = query.getInt(query.getColumnIndex(Constants.HISTORY_DURATION));
                    long updateTime = query.getLong(query.getColumnIndex(Constants.HISTORY_UPDATE_TIME));
                    String corver = query.getString(query.getColumnIndex(Constants.HISTORY_COVER));
                    String author = query.getString(query.getColumnIndex(Constants.HISTORY_AUTHOR));
                    Announcer announcer = new Announcer();
                    announcer.setNickname(author);
                    track.setDataId(trackId);
                    track.setTrackTitle(title);
                    track.setPlayCount(playCount);
                    track.setDuration(duration);
                    track.setUpdatedAt(updateTime);
                    track.setCoverUrlLarge(corver);
                    track.setCoverUrlSmall(corver);
                    track.setCoverUrlMiddle(corver);
                    track.setAnnouncer(announcer);
                    histories.add(track);
                }
                db.setTransactionSuccessful();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.endTransaction();
                    db.close();
                }
                //通知出去
                if (mCallback != null) {
                    mCallback.onHistoriesLoaded(histories);
                }
            }
        }
    }
}
