package com.two.channelmyanmar.fragment.detail;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.AppCompatButton;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.two.channelmyanmar.R;
import com.two.channelmyanmar.activity.PlayerActivity;
import com.two.channelmyanmar.adapter.MyAdapter;
import com.two.channelmyanmar.api.CmMovieApi;
import com.two.channelmyanmar.api.CmSeriesApi;
import com.two.channelmyanmar.api.ExtractorListener;
import com.two.channelmyanmar.api.FileSizeTask;
import com.two.channelmyanmar.bottomsheet.LoadingSheet;
import com.two.channelmyanmar.bottomsheet.WatchSheet;
import com.two.channelmyanmar.databinding.MovieChildFragmentBinding;
import com.two.channelmyanmar.databinding.SeriesChildFragmentBinding;
import com.two.channelmyanmar.model.MovieModel;
import com.two.channelmyanmar.model.SeriesHeaderModel;
import com.two.channelmyanmar.preference.PreferenceHelper;
import com.two.channelmyanmar.util.BlurTransform;
import com.two.channelmyanmar.util.Utils;
import com.two.channelmyanmar.watchlater.WatchLaterCacher;
import com.two.channelmyanmar.watchlater.WatchLaterRecoder;
import com.two.my_libs.MultiTheme;
import com.two.my_libs.base.BaseThemeFragment;
import com.two.my_libs.views.ptr.PtrFrameLayout;
import com.two.my_libs.views.ptr.PtrHandler;
import com.two.my_libs.views.ptr.header.StoreHouseHeader;
import com.two.my_libs.views.ptr.util.PtrLocalDisplay;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class SeriesChildFragment extends BaseThemeFragment implements CmSeriesApi.CallBack, ExtractorListener {
    private static final String ARG_TEXT = "series_data";
    private SeriesChildFragmentBinding binding;
    private PreferenceHelper helper;
    private String baseUrl;
    private MyAdapter adapter;
    private ExecutorService service = Executors.newFixedThreadPool(3);
    private WatchLaterRecoder recoder;

    public SeriesChildFragment() {
        // Required empty public constructor.
    }

    public static SeriesChildFragment newInstance(String text) {
        SeriesChildFragment fragment = new SeriesChildFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            baseUrl = getArguments().getString(ARG_TEXT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SeriesChildFragmentBinding.inflate(inflater, container, false);
        helper = new PreferenceHelper(getContext());
        recoder = new WatchLaterCacher(getContext());
        loadData();
        setupUI();
        return binding.getRoot();
    }

    private void loadData() {
        service.execute(new CmSeriesApi(helper, baseUrl, this));

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupUI() {
        WebView webView = binding.seriesMain.swebView;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.addJavascriptInterface(new MyInterFace(), "xGetter");

        binding.movieDetailShimmer.startShimmer();
        binding.seriesChildPtrFrame.setVisibility(View.INVISIBLE);
        binding.movieDetailToolbar.setVisibility(View.INVISIBLE);
        binding.movieDetailToolbar.setNavigationIcon(R.drawable.back);

        binding.movieChildAppbar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            boolean isCollapsed = verticalOffset < -270;
            binding.movieDetailToolbar.setVisibility(isCollapsed ? View.VISIBLE : View.INVISIBLE);
            binding.toolbarTitle.setVisibility(isCollapsed ? View.VISIBLE : View.INVISIBLE);
            binding.headerImage.setVisibility(isCollapsed ? View.INVISIBLE : View.VISIBLE);
            if (verticalOffset == 0) {
                binding.seriesChildPtrFrame.enableScrolling();
            } else {
                binding.seriesChildPtrFrame.disableScrolling();
            }
        });

        setupPullToRefresh();
    }

    private void setupPullToRefresh() {
        StoreHouseHeader header = new StoreHouseHeader(getContext());
        header.setPadding(0, PtrLocalDisplay.dp2px(5), 0, 0);
        header.initWithString("Loading");

        binding.seriesChildPtrFrame.setDurationToCloseHeader(1500);
        binding.seriesChildPtrFrame.setHeaderView(header);
        binding.seriesChildPtrFrame.addPtrUIHandler(header);
        binding.seriesChildPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return true;
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                loadData();
                binding.movieDetailShimmer.setVisibility(View.VISIBLE);
                binding.seriesChildPtrFrame.setVisibility(View.INVISIBLE);
                binding.movieDetailShimmer.startShimmer();
            }
        });
    }


    @Override
    public void onSuccessHeader(SeriesHeaderModel header) {
        if (!isAdded())
            return;
        requireActivity().runOnUiThread(() -> {
            binding.seriesChildPtrFrame.refreshComplete();
            binding.movieDetailShimmer.stopShimmer();
            binding.seriesChildPtrFrame.setVisibility(View.VISIBLE);
            binding.movieDetailShimmer.setVisibility(View.INVISIBLE);
            binding.seriesMain.SmovieTitle.setText(header.getName());
            binding.seriesMain.statustext.setText(header.getStatus());
            binding.toolbarTitle.setText(header.getName());
            Glide.with(requireActivity()).load(header.getPoster())
                    .apply(RequestOptions.bitmapTransform(new BlurTransform(getContext(), 10)))
                    .error(R.drawable.err_image)
                    .into(binding.headerImage);

            Glide.with(requireActivity()).load(header.getImgurl())
                    .placeholder(R.drawable.err_image)
                    .error(R.drawable.err_image)
                    .into(binding.seriesMain.SmovieImage);
            //
            binding.movieDetailOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.showPopupMenu(v, baseUrl);
                }
            });
            binding.addFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recoder.RecordWatch(baseUrl)) {
                        if (recoder.DeleteRecord(baseUrl)) {
                            binding.addFav.setSelected(false);
                        } else {
                            binding.addFav.setSelected(true);
                        }
                    } else {
                        recoder.RecordWatch(new MovieModel(header.getName(), baseUrl, header.getImgurl(), header.getImdb(), "", ""));
                        binding.addFav.setSelected(true);
                        binding.addFav.likeAnimation(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                Toast.makeText(getContext(), "Added to Fav:)", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            if (recoder.RecordWatch(baseUrl)) {
                binding.addFav.setSelected(true);
            } else {
                binding.addFav.setSelected(false);
            }
        });

    }

    @Override
    public void onSuccessBody(String html) {
        if (!isAdded())
            return;
        requireActivity().runOnUiThread(() -> {
            binding.seriesMain.swebView.loadDataWithBaseURL(null, injectDarkLight(html), "text/html", "UTF-8", null);
        });
    }

    private String injectDarkLight(String html) {
        return html.replace(".replaceMe", MultiTheme.getAppTheme() == 0 ? "light" : "dark");
    }

    @Override
    public void onSuccessRelated(ArrayList<MovieModel> related) {
        if (!isAdded())
            return;
        requireActivity().runOnUiThread(() -> {
            adapter = new MyAdapter(related, new MyAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(MovieModel data) {
                    ((SerieDetail) getParentFragment()).replaceChildFragment(data.getBaseUrl());
                }
            });

            binding.seriesMain.relatedseries.setLayoutManager(new GridLayoutManager(getContext(), 3));
            binding.seriesMain.relatedseries.setAdapter(adapter);
            adapter.setShowShimmer(false);
        });
    }

    @Override
    public void onFail(String msg) {
        if (!isAdded())
            return;
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            binding.seriesChildPtrFrame.refreshComplete();
            binding.movieDetailShimmer.stopShimmer();
            binding.seriesChildPtrFrame.setVisibility(View.VISIBLE);
            binding.movieDetailShimmer.setVisibility(View.INVISIBLE);
        });

    }

    public void showDialog(String directLink) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.play_download_dialog, null);
        AppCompatButton play = dialogView.findViewById(R.id.play_bt);
        AppCompatButton download = dialogView.findViewById(R.id.download_bt);
        TextView directLinkText = dialogView.findViewById(R.id.direct_link);
        TextView fileSize  = dialogView.findViewById(R.id.file_size);
        directLinkText.setText(directLink);
        Thread t=new Thread(new FileSizeTask(directLink, new FileSizeTask.CallBack() {
            @Override
            public void onSuccess(String length) {
                try{
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            fileSize.setVisibility(View.VISIBLE);
                            // pb.setVisibility(View.INVISIBLE);
                            fileSize.setText(length);
                        }
                    });
                }catch (Exception e){

                }


            }

            @Override
            public void onFail(String msg) {

            }
        }));
        t.start();
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startDownload(requireActivity(),directLink);

            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                Intent i = new Intent(requireActivity(), PlayerActivity.class);
                i.putExtra("url", directLink);
                startActivity(i);
            }
        });

        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        }
        alertDialog.show();

    }

    @Override
    public void onSuccessMegaUp(String directLink) {
        if (!isAdded())
            return;
        showDialog(directLink);


    }

    @Override
    public void onSuccessYoteShin(String directLink, boolean isDirect) {
        if (!isAdded())
            return;
        showDialog(directLink);

    }

    @Override
    public void onFailed(String message) {
        if (!isAdded())
            return;

    }

    @Override
    public void loading(int progress) {
        if (!isAdded())
            return;

    }

    public void showCustomDialog(String linkText) {
        // Inflate your custom layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_latout, null);

        // Initialize views from the custom layout
        final TextView tvTitle = dialogView.findViewById(R.id.dialogTitle);
        final TextView tvLink = dialogView.findViewById(R.id.tvLink);
        final ImageView ivCopy = dialogView.findViewById(R.id.ivCopy);
        final ImageView ivShare = dialogView.findViewById(R.id.ivShare);
        final EditText etLink = dialogView.findViewById(R.id.etLink);
        final TextView cancel = dialogView.findViewById(R.id.btnNeutral);
        final TextView direct = dialogView.findViewById(R.id.btnDirect);
        final TextView leetdev = dialogView.findViewById(R.id.btnLeetdev);

        // Optionally, you can set initial values
        etLink.setText(linkText);


        // Set listeners for the icons
        ivCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Copy text to clipboard
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("link", etLink.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Share the link using an intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, etLink.getText().toString());
                startActivity(Intent.createChooser(shareIntent, "Share link via"));
            }
        });

        // Build the AlertDialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity());
        builder.setView(dialogView);

        // Set up three action buttons: Positive, Negative, and Neutral.

        // Create and show the dialog
        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        //alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        direct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSheet(linkText, true);
            }
        });
        leetdev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSheet(linkText, false);
            }
        });

    }

    public void loadSheet(String link, boolean isDirect) {
        LoadingSheet sheet = new LoadingSheet(link, this, isDirect);
        sheet.show(getChildFragmentManager(), sheet.getTag());
    }

    public class MyInterFace {
        @JavascriptInterface
        public void show(String data) {
            if (data.contains("https://t.me/")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
                intent.setPackage("org.telegram.messenger");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(data)));
                }
            } else {
                if (data.contains("usersdrive") || data.contains("gdtot") || data.contains("cmapp")||data.contains("megaup")||data.contains("yoteshin")) {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showCustomDialog(data);
                        }
                    });
                }else {
                    if (data.contains("mega.nz")){
                        WatchSheet ws = new WatchSheet(data,true);

                        ws.show(getChildFragmentManager(),ws.getTag());

                    }else {
                        WatchSheet watchSheet = new WatchSheet(data,false);
                        watchSheet.show(getChildFragmentManager(),watchSheet.getTag());
                    }
                }

            }
            // Toast.makeText(getContext(), data, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void showCast(String data) {
            // Toast.makeText(getContext(), data, Toast.LENGTH_SHORT).show();
        }

        public String getString(String url) {
            return url.substring(url.lastIndexOf("/") + 1).toUpperCase();
        }
    }
}
