package com.two.channelmyanmar.watchlater;

import android.content.Context;


import com.two.channelmyanmar.download.db.BaseContentDbHelper;
import com.two.channelmyanmar.download.db.ContentDbDao;

import java.util.List;

public class WatchLaterDbHelper extends BaseContentDbHelper {

    private static final String DB_NAME = "watchlaterdb.db";
    private static final int DB_VERSION = 3;

    public WatchLaterDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    protected void onConfigContentDbDaos(List<ContentDbDao> contentDbDaos) {
        WatchLaterContentDao downloadFileDao = new WatchLaterContentDao(this);
        // config DownloadFileDao dao
        contentDbDaos.add(downloadFileDao);
    }





}
