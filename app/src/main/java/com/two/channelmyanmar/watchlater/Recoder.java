package com.two.channelmyanmar.watchlater;


import com.two.channelmyanmar.model.MovieModel;

public interface Recoder {

    public static final String TAG = "Recoder";

    void RecordWatch(MovieModel model);
    boolean RecordWatch(String url);
    boolean DeleteRecord(String url);
}
