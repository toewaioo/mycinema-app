package com.two.channelmyanmar.watchlater;

import com.two.channelmyanmar.model.MovieModel;

public interface WatchLaterRecoder extends WatchLaterDbRecoder {

    MovieModel createWatchLaterInfo(MovieModel model);

}
