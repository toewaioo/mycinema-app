package com.two.channelmyanmar.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseContentDbHelper extends SQLiteOpenHelper {

    /**
     * map of dao
     */
    private Map<String, ContentDbDao> mContentDbDaoMap = new HashMap<String, ContentDbDao>();

    public BaseContentDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        initContentDbDaoMap();
    }

    /**
     * init dao map
     */
    private void initContentDbDaoMap() {

        List<ContentDbDao> contentDbDaos = new ArrayList<ContentDbDao>();

        // config daos
        onConfigContentDbDaos(contentDbDaos);

        if (contentDbDaos.isEmpty()){
            return;
        }

        for (ContentDbDao contentDbDao : contentDbDaos) {
            if (contentDbDao == null) {
                continue;
            }
            String tableName = contentDbDao.getTableName();

            if (tableName.isEmpty()) {
                continue;
            }

            if (mContentDbDaoMap.containsKey(tableName)) {
                continue;
            }

            // put dao to map
            mContentDbDaoMap.put(tableName, contentDbDao);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Collection<ContentDbDao> contentDbDaos = mContentDbDaoMap.values();

        if (contentDbDaos.isEmpty()) {
            return;
        }

        for (ContentDbDao contentDbDao : contentDbDaos) {
            if (contentDbDao == null) {
                continue;
            }
            // call dao's onCreate
            contentDbDao.onCreate(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Collection<ContentDbDao> contentDbDaos = mContentDbDaoMap.values();

        if (contentDbDaos.isEmpty()) {
            return;
        }

        for (ContentDbDao contentDbDao : contentDbDaos) {
            if (contentDbDao == null) {
                continue;
            }
            // call dao's onUpgrade
            contentDbDao.onUpgrade(db, oldVersion, newVersion);
        }
    }

    /**
     * config daos
     *
     * @param contentDbDaos current daos
     */
    protected abstract void onConfigContentDbDaos(List<ContentDbDao> contentDbDaos);

    /**
     * get dao by table name
     *
     * @param tableName table name
     * @return the dao for the given table name
     */
    public ContentDbDao getContentDbDao(String tableName) {
        if (!mContentDbDaoMap.containsKey(tableName)) {
            throw new RuntimeException("unregistered database table:" + tableName + " in " + this.getClass()
                    .getSimpleName());
        }
        return mContentDbDaoMap.get(tableName);
    }

}
