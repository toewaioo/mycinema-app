package com.two.channelmyanmar.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.two.channelmyanmar.DetailActivity;
import com.two.channelmyanmar.R;
import com.two.channelmyanmar.model.MovieModel;
import com.two.channelmyanmar.util.BlurTransform;
import com.two.my_libs.MultiTheme;
import com.two.my_libs.shimmer.ShimmerFrameLayout;
import com.two.my_libs.shimmer.ShimmerTextViewDrawable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GridLoadMoreAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;
    private static final int VIEW_TYPE_ERROR = 2;
    private static final int VIEW_TYPE_SHIMMER = 3;

    private List<T> items = new ArrayList<>();
    private boolean isLoading = false;
    private boolean isError = false;
    private boolean showShimmer = false;

    private LoadMoreListener loadMoreListener;
    private OnItemClickListener listener;

    public interface LoadMoreListener {
        void onLoadMore();
    }
    public interface OnItemClickListener {
        void onItemClick(MovieModel data);
    }
    public void setItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                View itemView = inflater.inflate(R.layout.movie_item, parent, false);
                return new ItemViewHolder(itemView);
            case VIEW_TYPE_LOADING:
                return new LoadingViewHolder(inflater.inflate(R.layout.item_loading, parent, false));
            case VIEW_TYPE_ERROR:
                return new ErrorViewHolder(inflater.inflate(R.layout.item_load_failed, parent, false));
            case VIEW_TYPE_SHIMMER:
                return new ShimmerViewHolder(inflater.inflate(R.layout.shimmer_item, parent, false));
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {

            MovieModel item =(MovieModel) items.get(position);

            ItemViewHolder dataHolder = (ItemViewHolder) holder;
            dataHolder.textView.setText(item.getName());
            dataHolder.rattingText.setText(item.getImdb().trim().isEmpty() ? "N/A" : item.getImdb().trim());

            ShimmerTextViewDrawable drawable = new ShimmerTextViewDrawable(
                    dataHolder.imageView.getContext(),
                    "Loading.."
            );
            drawable.setTextSize(50);
            drawable.setTypeface(Typeface.DEFAULT_BOLD);
            drawable.setShimmerDuration(1200);
            if (item.getName().contains("18+")|item.getName().contains("21+")){
                dataHolder.blur.setVisibility(View.VISIBLE);
                dataHolder.blur.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dataHolder.blur.setVisibility(View.GONE);
                        Glide.with(dataHolder.imageView.getContext())
                                .load(item.getImgurl())
                                .placeholder(drawable)
                                .error(R.drawable.err_image)
                                .into(dataHolder.imageView);
                    }
                });
                Glide.with(dataHolder.imageView.getContext()).load(item.getImgurl()).apply(RequestOptions.bitmapTransform(new BlurTransform(dataHolder.imageView.getContext(), 25))).into(dataHolder.imageView);
            }else {
                Glide.with(dataHolder.imageView.getContext())
                        .load(item.getImgurl())
                        .placeholder(drawable)
                        .error(R.drawable.err_image)
                        .into(dataHolder.imageView);
            }
            dataHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener!=null)
                        listener.onItemClick(item);
                    //(dataHolder.itemView.getContext()).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });
            dataHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener!=null) {
                        listener.onItemClick(item);
                    }
                    //(dataHolder.itemView.getContext()).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });
            dataHolder.imageView.setOnLongClickListener(v -> {
                int adapterPosition = dataHolder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION &&
                        adapterPosition < items.size()) {

                    String description = item.getDescription();
                    if (description == null || description.isEmpty()) {
                        description = "No description available";
                    }

                    showCustomTooltip(v, description);
                    return true;
                }
                return false;
            });



        } else if (holder instanceof ShimmerViewHolder) {
            ((ShimmerViewHolder) holder).shimmerLayout.startShimmer();
        }else if (holder instanceof LoadingViewHolder){
            ((LoadingViewHolder) holder).loading.setTextColor(MultiTheme.getAppTheme()==0 ? Color.BLACK : Color.WHITE);
        }
    }
    private void showCustomTooltip(View anchor, String text) {
        if (anchor == null || text == null) return;

        LayoutInflater inflater = LayoutInflater.from(anchor.getContext());
        View tooltipView = inflater.inflate(R.layout.tooltip_layout, null);

        TextView tooltipText = tooltipView.findViewById(R.id.tooltip_text);
        tooltipText.setText(text);

        PopupWindow popupWindow = new PopupWindow(
                tooltipView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // Configure popup window
        popupWindow.setElevation(16f);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Calculate position
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int x = location[0] + (anchor.getWidth() / 2);
        int y = location[1] - anchor.getHeight();

        // Show popup
        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);

        // Auto-dismiss after 2 seconds
        // new Handler().postDelayed(popupWindow::dismiss, 2000);
    }

    @Override
    public int getItemCount() {
        if (showShimmer && items.isEmpty()) return 15; // Show 5 shimmer items
        return items.size() + (isLoading || isError ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (showShimmer && items.isEmpty()) return VIEW_TYPE_SHIMMER;
        if (position >= items.size()) {
            return isError ? VIEW_TYPE_ERROR : VIEW_TYPE_LOADING;
        }
        return VIEW_TYPE_ITEM;
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (items.isEmpty())
                    return;
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                    if (loadMoreListener != null) {
                        if (isError){
                            setError(false);
                        }

                        setLoading(true);
                        loadMoreListener.onLoadMore();
                    }
                }
            }
        });
        // Set span size for different view types
        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = getItemViewType(position);
                if (viewType == VIEW_TYPE_ITEM || viewType == VIEW_TYPE_SHIMMER) return 1;
                return layoutManager.getSpanCount(); // Full width for other types
            }
        });
    }
    public void setItems(List<T> items) {
        this.items = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public void addItems(List<T> newItems) {
        int oldSize = items.size();

        // Create a Set to track existing items for quick lookup
        Set<T> existingItems = new HashSet<>(items);

        List<T> filteredItems = new ArrayList<>();
        for (T newItem : newItems) {
            if (!existingItems.contains(newItem)) {
                filteredItems.add(newItem);
                existingItems.add(newItem); // Add to set to prevent further duplicates
            }
        }

        if (!filteredItems.isEmpty()) {
            items.addAll(filteredItems);
            notifyItemRangeInserted(oldSize, filteredItems.size());
        }
    }


    public void setLoading(boolean loading) {
        if (isLoading != loading) {
            isLoading = loading;
            if (loading) {
                notifyItemInserted(items.size());
            } else {
                notifyItemRemoved(items.size());
            }
        }
    }

    public void setError(boolean error) {
        if (isError != error) {
            isError = error;
            notifyItemChanged(items.size());
        }
    }

    public void showShimmer(boolean show) {
        showShimmer = show;
        notifyDataSetChanged();
    }

    public void setLoadMoreListener(LoadMoreListener listener) {
        this.loadMoreListener = listener;
    }


    // Concrete ItemViewHolder
    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        View blur;
        TextView textView,rattingText;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.titleTextView);
            rattingText = itemView.findViewById(R.id.ratingText);
            blur = itemView.findViewById(R.id.blurOverlay);
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        TextView loading;
        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            loading = itemView.findViewById(R.id.loading_text);
        }
    }

    private static class ErrorViewHolder extends RecyclerView.ViewHolder {
        TextView errorText;

        ErrorViewHolder(@NonNull View itemView) {
            super(itemView);
            errorText = itemView.findViewById(R.id.retry_button);
        }
    }




        private static class ShimmerViewHolder extends RecyclerView.ViewHolder {
        ShimmerFrameLayout shimmerLayout;

        ShimmerViewHolder(@NonNull View itemView) {
            super(itemView);
            shimmerLayout = itemView.findViewById(R.id.shimmerLayout);
        }
    }
}
