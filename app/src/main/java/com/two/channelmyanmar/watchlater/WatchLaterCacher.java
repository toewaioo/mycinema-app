package com.two.channelmyanmar.watchlater;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;


import com.two.channelmyanmar.download.db.ContentDbDao;
import com.two.channelmyanmar.model.MovieModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WatchLaterCacher implements WatchLaterRecoder {






    private WatchLaterDbHelper mDownloadFileDbHelper;

    // download files memory cache
    private Map<String, MovieModel> mDownloadFileInfoMap = new HashMap<String, MovieModel>();

    private Object mModifyLock = new Object();// modify lock


    public WatchLaterCacher(Context c){

        mDownloadFileDbHelper=new WatchLaterDbHelper(c);
        initDownloadFileInfoMapFromDb();
    }
    private void initDownloadFileInfoMapFromDb() {
        // read from database
        ContentDbDao dao = mDownloadFileDbHelper.getContentDbDao(MovieModel.Table.TABLE_NAME_OF_DOWNLOAD_FILE);
        if (dao == null) {
            return;
        }

        // query all
        Cursor cursor = dao.query(null, null, null, null);
        List<MovieModel> downloadFileInfos = getDownloadFilesFromCursor(cursor);
        // close the cursor
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        if (downloadFileInfos.isEmpty()) {
            return;
        }

        // cache in memory
        for (MovieModel downloadFileInfo : downloadFileInfos) {

            synchronized (mModifyLock) {// lock
                mDownloadFileInfoMap.put(downloadFileInfo.getBaseUrl(), downloadFileInfo);
            }
        }
    }
    private List<MovieModel> getDownloadFilesFromCursor(Cursor cursor) {
        List<MovieModel> downloadFileInfos = new ArrayList<MovieModel>();
        while (cursor != null && cursor.moveToNext()) {
            MovieModel downloadFileInfo = new MovieModel(cursor);
            if (downloadFileInfo == null) {
                continue;
            }
            downloadFileInfos.add(downloadFileInfo);
        }
        return downloadFileInfos;
    }
    @Override
    public MovieModel createWatchLaterInfo(MovieModel model) {
        MovieModel downloadFileInfo = new MovieModel(model);
        // add to cache
        System.out.println(downloadFileInfo.getBaseUrl());
        boolean isSucceed = addDownloadFile(downloadFileInfo);
        if (isSucceed) {
            System.out.println("Succed added");
            return downloadFileInfo;
        }
        System.out.println("Add fail");
        return null;
    }

    @Override
    public MovieModel getWatchLater(String url) {
        MovieModel downloadFileInfo = getDownloadFileInternal(url);

        if (downloadFileInfo == null && !url.isEmpty()) {
            downloadFileInfo = getDownloadFileInternal(url.trim());
        }

        return downloadFileInfo;
    }

    @Override
    public List<MovieModel> getWatchLaterList() {
        mDownloadFileInfoMap.clear();
        if (mDownloadFileInfoMap.isEmpty()) {
            initDownloadFileInfoMapFromDb();
        }

        // if this time the memory cache is not empty,return the cache
        if (!mDownloadFileInfoMap.isEmpty()) {
            List<MovieModel> downloadFileInfos = new ArrayList<MovieModel>(mDownloadFileInfoMap.values());

            // check status
            if (!downloadFileInfos.isEmpty()) {
                for (MovieModel downloadFileInfo : downloadFileInfos) {
                    //checkDownloadFileStatus(downloadFileInfo);
                }
            }
            return downloadFileInfos;
        }

        // otherwise return empty list
        return new ArrayList<MovieModel>();
    }
    @Override
    public boolean DeleteRecord(String url) {
        ContentDbDao dao = mDownloadFileDbHelper.getContentDbDao(MovieModel.Table.TABLE_NAME_OF_DOWNLOAD_FILE);
        if (dao == null) {
            return false;
        }
        synchronized (mModifyLock) {// lock

            long id = dao.delete("url = ?",new String[]{url});
            if (id > 0) {
                // succeed, update memory cache
                // downloadFileInfo.setId(new Integer((int) id));
                System.out.println("add successed");
                mDownloadFileInfoMap.remove(url);
                // notify caller
                //notifyDownloadFileCreated(downloadFileInfo);
                return true;
            }
        }
        return false;
    }
    @Override
    public void RecordWatch(MovieModel model) {
        MovieModel downloadFileInfo = new MovieModel(model);
        // add to cache
        System.out.println(downloadFileInfo.getBaseUrl());
        boolean isSucceed = addDownloadFile(downloadFileInfo);
        if (isSucceed) {
            System.out.println("Succed added");

        }
        System.out.println("Add fail");

    }
    @Override
    public boolean RecordWatch(String url) {
        if(getWatchLater(url)==null){
            return false;
        }else{
            return true;
        }
    }
    private MovieModel getDownloadFileInternal(String url) {

        MovieModel downloadFileInfo = null;

        if (mDownloadFileInfoMap.get(url) != null) {
            // return memory cache
            downloadFileInfo = mDownloadFileInfoMap.get(url);
        } else {
            // find in database
            ContentDbDao dao = mDownloadFileDbHelper.getContentDbDao(MovieModel.Table
                    .TABLE_NAME_OF_DOWNLOAD_FILE);
            if (dao == null) {
                return null;
            }

            Cursor cursor = dao.query(null, MovieModel.Table.COLUMN_NAME_OF_FIELD_URL + "= ?", new
                    String[]{url}, null);
            if (cursor != null && cursor.moveToFirst()) {
                downloadFileInfo = new MovieModel(cursor);
            }

            // close the cursor
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            if (downloadFileInfo == null) {
                return null;
            }

            String downloadUrl = downloadFileInfo.getBaseUrl();
            if (downloadUrl.isEmpty()) {
                synchronized (mModifyLock) {// lock
                    // cache in memory
                    mDownloadFileInfoMap.put(downloadUrl, downloadFileInfo);
                    downloadFileInfo = mDownloadFileInfoMap.get(url);
                }
            }
        }

        // checkDownloadFileStatus(downloadFileInfo);

        return downloadFileInfo;
    }
    public static boolean isEmpty(ContentValues values) {
        if (values == null || values.size() == 0) {
            return true;
        } else {
            return false;
        }
    }
    private boolean addDownloadFile(MovieModel downloadFileInfo) {



        ContentDbDao dao = mDownloadFileDbHelper.getContentDbDao(MovieModel.Table.TABLE_NAME_OF_DOWNLOAD_FILE);
        if (dao == null) {
            return false;
        }

        ContentValues values = downloadFileInfo.getContentValues();
        if (isEmpty(values)) {
            return false;
        }

        String url = downloadFileInfo.getBaseUrl();

        MovieModel downloadFileInfoExist = getWatchLater(url);
        // exist the download file, update
        if (downloadFileInfoExist != downloadFileInfo) {



            // insert new one in db
            synchronized (mModifyLock) {// lock
                long id = dao.insert(values);
                if (id != -1) {
                    // succeed, update memory cache
                    // downloadFileInfo.setId(new Integer((int) id));
                    System.out.println("add successed");
                    mDownloadFileInfoMap.put(url, downloadFileInfo);
                    // notify caller
                    //notifyDownloadFileCreated(downloadFileInfo);
                    return true;
                }
            }
            return false;
        }
        return false;
    }


}
