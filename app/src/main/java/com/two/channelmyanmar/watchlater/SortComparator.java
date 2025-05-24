package com.two.channelmyanmar.watchlater;

import com.two.channelmyanmar.model.MovieModel;

import java.util.Comparator;

/*
 * Created by Toewaioo on 4/20/25
 * Description: [Add class description here]
 */
public class SortComparator implements Comparator<MovieModel> {
    @Override
    public int compare(MovieModel o1, MovieModel o2) {
        return o2.getDate().compareTo(o1.getDate());
    }
}
