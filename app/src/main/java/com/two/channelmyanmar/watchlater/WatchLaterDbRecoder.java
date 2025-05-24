package com.two.channelmyanmar.watchlater;

import com.two.channelmyanmar.model.MovieModel;

import java.util.List;

public interface WatchLaterDbRecoder extends Recoder {

    MovieModel getWatchLater(String url);
    List<MovieModel> getWatchLaterList();

}
