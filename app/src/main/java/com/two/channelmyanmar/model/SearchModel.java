package com.two.channelmyanmar.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;


import com.two.channelmyanmar.util.Utils;

import java.util.Date;

public class SearchModel {

    String name,baseUrl,imgurl,imdb,description,type,date;
    public SearchModel(String name, String baseUrl, String imgurl, String imdb, String description, String type) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.imgurl = imgurl;
        this.imdb = imdb;
        this.description = description;
        this.type = type;
        this.date= Utils.date_to_string(new Date());

    }
    public SearchModel(SearchModel model){
        this.name=model.getName();
        this.baseUrl=model.getBaseUrl();
        this.imgurl=model.getImgurl();
        this.imdb=model.getImdb();
        this.date=Utils.date_to_string(new Date());
    }
    public SearchModel(Cursor cursor){
        if (cursor != null && !cursor.isClosed()) {

            String titles="";
            String urls="";
            String imgurls="";
            String rattings="";
            int columnIndex = -1;
            String date1=null;

            columnIndex = cursor.getColumnIndex(Table.COLUMN_NAME_OF_FIELD_TITLE);
            if (columnIndex != -1) {
                titles=cursor.getString(columnIndex);
            }
            columnIndex = cursor.getColumnIndex(Table.COLUMN_NAME_OF_FIELD_URL);
            if (columnIndex != -1) {
                urls=cursor.getString(columnIndex);
            }
            columnIndex = cursor.getColumnIndex(Table.COLUMN_NAME_OF_FIELD_IMGURL);
            if (columnIndex != -1) {
                imgurls=cursor.getString(columnIndex);
            }
            columnIndex = cursor.getColumnIndex(Table.COLUMN_NAME_OF_FIELD_RATING);
            if (columnIndex != -1) {
                rattings=cursor.getString(columnIndex);
            }
            columnIndex=cursor.getColumnIndex(Table.COLUM_NAME_OF_DATE);
            if (columnIndex!=-1){
                date1=cursor.getString(columnIndex);
            }

            if (!TextUtils.isEmpty(urls)) {
                // init fields

                this.name=titles;
                this.baseUrl=urls;
                this.imgurl=imgurls;
                this.imdb=rattings;
                this.date=date1;


            } else {
                throw new IllegalArgumentException("id or url from cursor illegal!");
            }
        } else {
            throw new NullPointerException("cursor illegal!");


        }
    }
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(Table.COLUMN_NAME_OF_FIELD_TITLE,name);
        values.put(Table.COLUMN_NAME_OF_FIELD_URL, baseUrl);
        values.put(Table.COLUMN_NAME_OF_FIELD_IMGURL,imgurl);
        values.put(Table.COLUMN_NAME_OF_FIELD_RATING,imdb);
        values.put(Table.COLUM_NAME_OF_DATE,date);
        return values;
    }
    public static final class Table {
        public static final String COLUMN_NAME_OF_FIELD_NO = "no";
        public static final String TABLE_NAME_OF_DOWNLOAD_FILE = "tb_watchlater";

        /**
         * id field name
         */

        /**
         * url field name
         */
        public static final String COLUMN_NAME_OF_FIELD_TITLE = "title";
        public static final String COLUMN_NAME_OF_FIELD_URL = "url";
        public static final String COLUMN_NAME_OF_FIELD_IMGURL = "imgurl";
        public static final String COLUMN_NAME_OF_FIELD_RATING = "rating";
        public static final  String COLUM_NAME_OF_DATE="date";
        /**
         * downloadedSize field name
         */


        public static final String getCreateTableSql() {

            String createTableSql = "CREATE TABLE IF NOT EXISTS " //
                    + TABLE_NAME_OF_DOWNLOAD_FILE //
                    + "(" + COLUMN_NAME_OF_FIELD_NO + " INTEGER PRIMARY KEY AUTOINCREMENT,"//no
                    + COLUMN_NAME_OF_FIELD_TITLE + " TEXT,"//id
                    + COLUMN_NAME_OF_FIELD_URL + " TEXT UNIQUE,"//url
                    + COLUMN_NAME_OF_FIELD_IMGURL + " TEXT,"//folder
                    + COLUMN_NAME_OF_FIELD_RATING+ " TEXT,"
                    +COLUM_NAME_OF_DATE+" TEXT"+ ")";//date

            return createTableSql;
        }
        public static final String getUpdateTableVersion1To2Sql() {

            String updateSql = "ALTER TABLE " //
                    + TABLE_NAME_OF_DOWNLOAD_FILE //
                    + " ADD " //
                    + COLUMN_NAME_OF_FIELD_URL+ " TEXT"; //

            return updateSql;
        }

        /**
         * the sql to update table when db version is 2 to 3
         */
        public static final String getUpdateTableVersion2To3Sql() {

            String updateSql = "ALTER TABLE " //
                    + TABLE_NAME_OF_DOWNLOAD_FILE //
                    + " ADD " //
                    + COLUMN_NAME_OF_FIELD_URL + " TEXT"; //

            return updateSql;
        }

    }public String getDate(){
        return date;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

