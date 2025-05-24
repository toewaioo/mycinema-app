package com.two.channelmyanmar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.two.channelmyanmar.R;
import com.two.channelmyanmar.model.MovieModel;
import com.two.my_libs.shimmer.ShimmerFrameLayout;

import java.util.List;

//public class MyLoadMoreAdapter extends GridLoadMoreAdapter<MovieModel> {
//
//    public MyLoadMoreAdapter(RecyclerView recyclerView) {
//        super(recyclerView);
//    }
//
//    @Override
//    protected ContentViewHolder onCreateContentViewHolder(ViewGroup parent) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.movie_item, parent, false);
//        return new MyViewHolder(view);
//    }
//
//    @Override
//    protected void onBindContentViewHolder(ContentViewHolder holder, int position) {
//        MyViewHolder viewHolder = (MyViewHolder) holder;
//        viewHolder.textView.setText(getItem(position).getName());
//    }
//
//    private static class MyViewHolder extends ContentViewHolder {
//        ImageView imageView;
//        TextView textView;
//
//        MyViewHolder(@NonNull View itemView) {
//            super(itemView);
//            imageView = itemView.findViewById(R.id.imageView);
//            textView = itemView.findViewById(R.id.titleTextView);
//        }
//    }
//}
//
