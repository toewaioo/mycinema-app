package com.two.channelmyanmar.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.two.channelmyanmar.R;
import com.two.channelmyanmar.model.Genres;

import java.util.ArrayList;

/*
 * Created by Toewaioo on 4/8/25
 * Description: [Add class description here]
 */
public class GenresAdapter extends BaseAdapter {
    public interface CallBack {
        void onClickG(String data);
    }

    private static final int MAX_COLLAPSED_ITEMS = 3;
    private final ArrayList<Genres> list;
    private final CallBack cb;
    private final Context context;
    private final LayoutInflater inflater;
    private boolean isExpanded;

    public GenresAdapter(ArrayList<Genres> list, CallBack cb, Context context) {
        this.context = context;
        this.list = list;
        this.cb = cb;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (isExpanded) {
            return list.size() + 1; // All items + collapse button
        } else {
            if (list.size() > MAX_COLLAPSED_ITEMS) {
                return MAX_COLLAPSED_ITEMS + 1; // Collapsed items + expand button
            }
            return list.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (isExpanded && position < list.size()) {
            return list.get(position);
        } else if (!isExpanded && position < MAX_COLLAPSED_ITEMS) {
            return list.get(position);
        }
        return null; // Button positions have no data item
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.search_genres_item, parent, false);
            holder = new ViewHolder();
            holder.header = convertView.findViewById(R.id.gHeader);
            holder.count = convertView.findViewById(R.id.gCount);
            holder.content = convertView.findViewById(R.id.content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (isButtonPosition(position)) {
            setupButton(holder, position);
        } else {
            setupDataItem(holder, position);
        }

        return convertView;
    }

    private boolean isButtonPosition(int position) {
        if (isExpanded) {
            return position == list.size();
        } else {
            return list.size() > MAX_COLLAPSED_ITEMS && position == MAX_COLLAPSED_ITEMS;
        }
    }

    private void setupButton(ViewHolder holder, int position) {
        holder.count.setVisibility(View.GONE);
        holder.header.setGravity(Gravity.CENTER);
        holder.header.setText(isExpanded ? "Click to Collapse" : "Click to Expand");

        holder.header.setOnClickListener(v -> {
            isExpanded = !isExpanded;
            notifyDataSetChanged();
        });
    }

    private void setupDataItem(ViewHolder holder, int position) {
        Genres genre = list.get(getDataPosition(position));

        holder.count.setVisibility(View.VISIBLE);
        holder.header.setGravity(Gravity.LEFT);
        holder.header.setText(genre.getName());
        holder.count.setText(genre.getCount());

        holder.header.setOnClickListener(v -> cb.onClickG(genre.getUrl()));
        holder.content.setOnClickListener(v -> cb.onClickG(genre.getUrl()));

    }

    private int getDataPosition(int adapterPosition) {
        if (isExpanded) {
            return adapterPosition;
        } else {
            return Math.min(adapterPosition, list.size() - 1);
        }
    }

    static class ViewHolder {
        TextView header, count;
        RelativeLayout content;
    }
}