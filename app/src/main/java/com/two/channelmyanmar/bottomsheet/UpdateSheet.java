package com.two.channelmyanmar.bottomsheet;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.two.channelmyanmar.databinding.UpdateSheetBinding;

/*
 * Created by Toewaioo on 4/20/25
 * Description: [Add class description here]
 */
public class UpdateSheet extends BottomSheetDialogFragment {
    UpdateSheetBinding binding;
    private long downloadId;
    private static final String ARG_DOWNLOAD_URL = "download_url";
    private static final String ARG_WHAT_NEW = "what_new";
    private static final String ARG_VERSION = "version";

    public interface CallBack{
        void onClick(String url);
    }

    CallBack cb;
    public UpdateSheet() {}

    public static UpdateSheet newInstance(String downloadUrl, String whatNew, String version) {
        UpdateSheet fragment = new UpdateSheet();
        Bundle args = new Bundle();
        args.putString(ARG_DOWNLOAD_URL, downloadUrl);
        args.putString(ARG_WHAT_NEW, whatNew);
        args.putString(ARG_VERSION, version);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.url = getArguments().getString(ARG_DOWNLOAD_URL);
            this.whatNew = getArguments().getString(ARG_WHAT_NEW);
            this.verSize = getArguments().getString(ARG_VERSION);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CallBack) {
            cb = (CallBack) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cb = null;
    }
    public  void  setCallBack(CallBack cb){
        this.cb= cb;
    }
    public UpdateSheet( String url, String whatNew, String verSize,CallBack cb) {

        this.cb=cb;
        this.url = url;
        this.whatNew = whatNew;
        this.verSize = verSize;
    }

    String url,whatNew,verSize;

    @Override
    public void onStart() {
        super.onStart();
        this.getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ((View) getView().getParent()).setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=UpdateSheetBinding.inflate(inflater,container,false);
        setCancelable(false);
        binding.versionSize.setText(verSize);
        binding.whatNew.setText(whatNew);
        if (url.isEmpty()){
            binding.startDown.setText("Exit");
            binding.versionSize.setText("****Attention****");
        }
        binding.startDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (url.isEmpty()){
                    System.exit(0);
                }else {
                    if (binding.startDown.getText().toString().contains("Don't exit")) {
                        Toast.makeText(getContext(), "Download in progress.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cb.onClick(url);
                    binding.startDown.setText("Don't exit app while downloading app!");
                }
                // dismiss();
                // Util.startDownload(getContext(),url);
            }
        });
        return binding.getRoot();

    }

    @Override
    public void onResume() {
        super.onResume();
        // getActivity().registerReceiver(downloadCompleteListener,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    @Override
    public void onPause() {
        super.onPause();
    }



}

