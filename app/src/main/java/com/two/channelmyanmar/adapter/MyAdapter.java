package com.two.channelmyanmar.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.two.channelmyanmar.R;
import com.two.channelmyanmar.model.MovieModel;
import com.two.channelmyanmar.util.BlurTransform;
import com.two.my_libs.shimmer.ShimmerFrameLayout;
import com.two.my_libs.shimmer.ShimmerTextViewDrawable;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(MovieModel data);
    }

    private static final int TYPE_SHIMMER = 0;
    private boolean showShimmer = true;
    private static final int TYPE_DATA = 1;
    private static final int SCROLL_DELAY_MS = 3000;

    private ArrayList<MovieModel> dataItems;
    private OnItemClickListener listener;

    private final Handler handler;
    private int currentPosition = 0;
    private RecyclerView recyclerView;

    public MyAdapter(ArrayList<MovieModel> dataItems, OnItemClickListener listener) {
        this.dataItems = dataItems != null ? dataItems : new ArrayList<>();
        this.listener = listener;
        this.handler = new Handler();
    }

    @Override
    public int getItemViewType(int position) {
        return showShimmer ? TYPE_SHIMMER : TYPE_DATA;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SHIMMER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shimmer_item, parent, false);
            return new ShimmerViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, parent, false);
            return new DataViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ShimmerViewHolder) {
            ((ShimmerViewHolder) holder).shimmerLayout.startShimmer();
        } else if (holder instanceof DataViewHolder) {
            MovieModel item = dataItems.get(position);
            DataViewHolder dataHolder = (DataViewHolder) holder;

            dataHolder.textView.setText(item.getName());
            dataHolder.rattingText.setText(item.getImdb().trim().isEmpty() ? "N/A" : item.getImdb().trim());

            ShimmerTextViewDrawable drawable = new ShimmerTextViewDrawable(
                    dataHolder.imageView.getContext(),
                    "Loading.."
            );
            drawable.setTextSize(50);
            drawable.setTypeface(Typeface.DEFAULT_BOLD);
            drawable.setShimmerDuration(1200);

            if (item.getName().contains("18+") | item.getName().contains("21+")) {
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
            } else {
                Glide.with(dataHolder.imageView.getContext())
                        .load(item.getImgurl())
                        .placeholder(drawable)
                        .error(R.drawable.err_image)
                        .into(dataHolder.imageView);
            }

            dataHolder.imageView.setOnClickListener(v -> {
                int adapterPosition = dataHolder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < dataItems.size()) {
                    listener.onItemClick(dataItems.get(adapterPosition));
                }
            });
            dataHolder.imageView.setOnLongClickListener(v -> {
                int adapterPosition = dataHolder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION &&
                        adapterPosition < dataItems.size()) {

                    String description = dataItems.get(adapterPosition).getDescription();
                    if (description == null || description.isEmpty()) {
                        description = "No description available";
                    }

                    showCustomTooltip(v, description);
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public int getItemCount() {
        return showShimmer ? 6 : dataItems.size() >= 9 ? 9 : 5;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof ShimmerViewHolder) {
            ((ShimmerViewHolder) holder).shimmerLayout.stopShimmer();
        }
        super.onViewRecycled(holder);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void startAutoScroll() {
        if (recyclerView == null || dataItems.isEmpty()) return;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentPosition = (currentPosition + 1) % dataItems.size();
                recyclerView.smoothScrollToPosition(currentPosition);
                handler.postDelayed(this, SCROLL_DELAY_MS);
            }
        }, SCROLL_DELAY_MS);
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


    public void stopAutoScroll() {
        handler.removeCallbacksAndMessages(null);
    }

    public void setShowShimmer(boolean showShimmer) {
        if (this.showShimmer != showShimmer) {
            this.showShimmer = showShimmer;
            notifyDataSetChanged();
        }
    }

    public void updateData(ArrayList<MovieModel> items) {
        this.dataItems.clear();
        this.dataItems.addAll(items);
        setShowShimmer(false);
    }

    static class ShimmerViewHolder extends RecyclerView.ViewHolder {
        ShimmerFrameLayout shimmerLayout;

        ShimmerViewHolder(@NonNull View itemView) {
            super(itemView);
            shimmerLayout = itemView.findViewById(R.id.shimmerLayout);
        }
    }

    static class DataViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView, rattingText;
        View blur;

        DataViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.titleTextView);
            rattingText = itemView.findViewById(R.id.ratingText);
            blur = itemView.findViewById(R.id.blurOverlay);
        }
    }
}