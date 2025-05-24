package com.two.channelmyanmar.bottomsheet;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.UnstableApi;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.two.channelmyanmar.R;
import com.two.channelmyanmar.activity.PlayerActivity;
import com.two.channelmyanmar.adapter.MeganzAdapter;
import com.two.channelmyanmar.api.MeganzApi;
import com.two.channelmyanmar.api.StreamApi;
import com.two.channelmyanmar.databinding.WatchSheetBinding;
import com.two.channelmyanmar.model.MeganzModel;
import com.two.channelmyanmar.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Created by Toewaioo on 4/27/25
 * Description: [Add class description here]
 */
public class WatchSheet extends BottomSheetDialogFragment implements StreamApi.Listener {
    private final String TAG = this.getClass().getSimpleName();
    private String url;
    private WatchSheetBinding binding;
    private ExecutorService service= Executors.newFixedThreadPool(2);
    private boolean isMega;
    private MeganzAdapter adapter;
    ArrayList<MeganzModel> list=new ArrayList<>();
    public WatchSheet(String url,boolean isMega) {
        // constructor code
        this.isMega = isMega;
        this.url= url;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        View view = (View) getView().getParent();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            view.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = WatchSheetBinding.inflate(inflater,container,false);


        if (isMega)
            service.execute(new MeganzApi(url,this));
        else
            service.execute(new StreamApi(url,this));
        return binding.getRoot();
    }

    @Override
    public void onFail(String msg) {
        if (!isAdded())
            return;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    @Override
    public void onFound(ArrayList<MeganzModel> result) {
        if (!isAdded())
            return;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (result.isEmpty()) {
                    Toast.makeText(getContext(),"File not found!",Toast.LENGTH_SHORT).show();
                    dismiss();
                    return;
                }
               // Toast.makeText(getContext(),result.get(0).getUrl(),Toast.LENGTH_SHORT).show();
                binding.watchFirst.setVisibility(View.GONE);
                binding.watchSecond.setVisibility(View.VISIBLE);
                adapter = new MeganzAdapter(getContext(), R.layout.watch_sheet_adapter_layout, result);
                binding.watchSheetListView.setAdapter(adapter);
                binding.watchSheetListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                binding.watchSheetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == adapter.getSelectIndex()) {
                            adapter.setSelectedIndex(-1);
                            adapter.notifyDataSetChanged();
                        } else {
                            adapter.setSelectedIndex(position);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                binding.watchSheetWatch.setOnClickListener(new View.OnClickListener() {
                    @OptIn(markerClass = UnstableApi.class)
                    @Override
                    public void onClick(View v) {
                        if (adapter.getSelectIndex() != -1) {

                            MeganzModel data = adapter.getdata(adapter.getSelectIndex());
                            if (data.getUrl().endsWith(".m3u8")|data.getUrl().contains(".m3u8")){

                                    Intent i = new Intent(getActivity(), PlayerActivity.class);
                                    i.putExtra("url", data.getUrl());
                                    getActivity().startActivity(i);
                                    getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

                            }else {
                                Utils.startDownload(requireActivity(),data.getUrl());
                                Toast.makeText(getContext(), data.getUrl(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                if(!result.isEmpty()) {
                    if (result.get(0).getUrl().endsWith(".m3u8")|result.get(0).getUrl().contains(".m3u8")) {
                        binding.watchSheetWatch.setText("Watch");
                    } else {
                        binding.watchSheetWatch.setText("Download");
                    }
                }
            }
        });
    }

}
