package com.two.channelmyanmar.watchlater;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.two.channelmyanmar.download.db.BaseContentDbDao;
import com.two.channelmyanmar.model.MovieModel;


public class WatchLaterContentDao extends BaseContentDbDao {

    private static final String TAG = WatchLaterContentDao.class.getSimpleName();

    public WatchLaterContentDao(SQLiteOpenHelper dbHelper) {
        super(dbHelper, MovieModel.Table.TABLE_NAME_OF_DOWNLOAD_FILE, MovieModel.Table
                .COLUMN_NAME_OF_FIELD_TITLE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table of DownloadFile
        db.execSQL(MovieModel.Table.getCreateTableSql());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.i(TAG, TAG + ".onUpgrade，oldVersion：" + oldVersion + "，oldVersion：" + newVersion);

        // upgrade to version 2
        if (newVersion == 2) {
            switch (oldVersion) {
                case 1:
                    // version 1 to 2
                    updateVersion1To2(db);
                    break;
            }
        }
        // upgrade to version 3
        else if (newVersion == 3) {
            switch (oldVersion) {
                case 1:
                    // version 1 to 3
                    updateVersion1To3(db);
                    break;
                case 2:
                    // version 2 to 3
                    updateVersion2To3(db);
                    break;
            }
        }
        // upgrade to version 4

    }

    // version 1 to 2
    private void updateVersion1To2(SQLiteDatabase db) {
        db.execSQL(MovieModel.Table.getUpdateTableVersion1To2Sql());
    }

    // version 2 to 3
    private void updateVersion2To3(SQLiteDatabase db) {
        db.execSQL(MovieModel.Table.getUpdateTableVersion2To3Sql());
    }

    // version 1 to 3
    private void updateVersion1To3(SQLiteDatabase db) {
        db.execSQL(MovieModel.Table.getUpdateTableVersion1To2Sql());
        db.execSQL(MovieModel.Table.getUpdateTableVersion2To3Sql());
    }





}
