package com.two.channelmyanmar.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.two.channelmyanmar.R;
import com.two.channelmyanmar.model.MovieModel;
import com.two.my_libs.shimmer.ShimmerTextViewDrawable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LoadMoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE = 0;
    private static final int LOADING_VIEW_TYPE = 1;
    private static final int SHIMMER_VIEW_TYPE = 2;
    private static final int EMPTY_VIEW_TYPE = 3;
    private static final int SPAN_COUNT = 3;
    private static final int SHIMMER_ITEM_COUNT = 15;
    private static final int VISIBLE_THRESHOLD = 3;
    private static final int LOADING_VIEW_POSITION = -1;

    private final List<MovieModel> data = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Object dataLock = new Object();

    private boolean isLoading = false;
    private boolean showEmptyView = false;
    private LoadMoreCallback callback;

    public interface LoadMoreCallback {
        void onReachEnd();
        void onRetry();
    }

    public void setLoadMoreCallback(LoadMoreCallback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM_VIEW_TYPE:
                return new DataViewHolder(inflater.inflate(R.layout.movie_item, parent, false));
            case LOADING_VIEW_TYPE:
                return new LoadingViewHolder(inflater.inflate(R.layout.item_loading, parent, false));
            case SHIMMER_VIEW_TYPE:
                return new ShimmerViewHolder(inflater.inflate(R.layout.shimmer_item, parent, false));
            case EMPTY_VIEW_TYPE:
            default:
                return new EmptyViewHolder(inflater.inflate(R.layout.empty_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            bindDataViewHolder((DataViewHolder) holder, position);
        } else if (holder instanceof ShimmerViewHolder) {
            ((ShimmerViewHolder) holder).startShimmer();
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).retryButton.setOnClickListener(v -> {
                if (callback != null) callback.onRetry();
            });
        }
    }

    private void bindDataViewHolder(DataViewHolder holder, int position) {
        try {
            MovieModel item;
            synchronized (dataLock) {
                item = data.get(position);
            }

            holder.textView.setText(item.getName());
            ShimmerTextViewDrawable drawable = new ShimmerTextViewDrawable(
                    holder.imageView.getContext(),
                    "Loading.."
            );
            drawable.setTextSize(50);
            drawable.setTypeface(Typeface.DEFAULT_BOLD);
            drawable.setShimmerDuration(1200);

            Glide.with(holder.imageView.getContext())
                    .load(item.getImgurl())
                    .placeholder(drawable)
                    .error(R.drawable.err_image)
                    .into(holder.imageView);

            holder.imageView.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (isValidPosition(adapterPosition)) {
                    // Handle click
                }
            });

            holder.imageView.setOnLongClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (isValidPosition(adapterPosition)) {
                   // showCustomTooltip(v, data.get(adapterPosition).getDescription());
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            Log.e("BindingError", "Position: " + position + ", Error: " + e.getMessage());
        }
    }

    private boolean isValidPosition(int position) {
        return position != RecyclerView.NO_POSITION && position >= 0 && position < data.size();
    }

    @Override
    public int getItemCount() {
        if (showEmptyView) return 1;
        if (isLoading && data.isEmpty()) return SHIMMER_ITEM_COUNT;
        return data.size() + (isLoading ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (showEmptyView) return EMPTY_VIEW_TYPE;
        if (position == LOADING_VIEW_POSITION) return LOADING_VIEW_TYPE;
        if (position < data.size()) return ITEM_VIEW_TYPE;
        if (isLoading && data.isEmpty()) return SHIMMER_VIEW_TYPE;
        return LOADING_VIEW_TYPE;
    }

    public GridLayoutManager createGridLayoutManager(Context context) {
        GridLayoutManager glm = new GridLayoutManager(context, SPAN_COUNT);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (getItemViewType(position)) {
                    case LOADING_VIEW_TYPE:
                    case EMPTY_VIEW_TYPE:
                        return SPAN_COUNT;
                    case SHIMMER_VIEW_TYPE:
                        return 1;
                    default:
                        return 1;
                }
            }
        });
        return glm;
    }

    public void checkScrollChanged(int lastVisiblePosition) {
        synchronized (dataLock) {
            int totalItems = getItemCount();
            if (lastVisiblePosition < 0 || lastVisiblePosition >= totalItems) return;

            boolean shouldLoad = !isLoading &&
                    !showEmptyView &&
                    (totalItems - lastVisiblePosition) <= VISIBLE_THRESHOLD &&
                    callback != null;

            if (shouldLoad) {
                isLoading = true;
                mainHandler.post(() -> notifyItemInserted(data.size()));
                callback.onReachEnd();
            }
        }
    }

    public void addData(List<MovieModel> newItems) {
        new Thread(() -> {
            List<MovieModel> filtered = filterNewItems(newItems);
            if (filtered.isEmpty()) return;

            synchronized (dataLock) {
                int insertPosition = data.size();
                data.addAll(filtered);
                mainHandler.post(() -> {
                    showEmptyView = false;
                    notifyItemRangeInserted(insertPosition, filtered.size());
                    isLoading = false;
                    if (data.size() == filtered.size()) { // First page load
                        notifyItemRangeRemoved(0, SHIMMER_ITEM_COUNT);
                    }
                });
            }
        }).start();
    }

    private List<MovieModel> filterNewItems(List<MovieModel> newItems) {
        Set<MovieModel> existing = new HashSet<>(data);
        return newItems.stream()
                .filter(item -> !existing.contains(item))
                .collect(Collectors.toList());
    }

    public void setLoading(boolean loading) {
        if (isLoading == loading) return;
        isLoading = loading;

        mainHandler.post(() -> {
            if (loading) {
                notifyItemInserted(data.size());
            } else {
                notifyItemRemoved(data.size());
            }
        });
    }

    public void showEmptyState() {
        synchronized (dataLock) {
            showEmptyView = true;
            mainHandler.post(() -> {
                notifyDataSetChanged();
                isLoading = false;
            });
        }
    }

    public void refreshData() {
        synchronized (dataLock) {
            data.clear();
            showEmptyView = false;
            mainHandler.post(() -> {
                notifyDataSetChanged();
                isLoading = false;
            });
        }
    }

    // ViewHolder classes remain unchanged
    static class DataViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        DataViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.titleTextView);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    static class ShimmerViewHolder extends RecyclerView.ViewHolder {
        View shimmerView;
        ShimmerViewHolder(View itemView) {
            super(itemView);
            shimmerView = itemView.findViewById(R.id.shimmer_layout);
        }
        void startShimmer() {
            // Implement your shimmer animation here
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        View retryButton;
        EmptyViewHolder(View itemView) {
            super(itemView);
            retryButton = itemView.findViewById(R.id.retry_button);
        }
    }
}