package com.two.channelmyanmar;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.widget.AppCompatButton;
import androidx.media3.common.util.UnstableApi;

import com.two.channelmyanmar.activity.PlayerActivity;
import com.two.channelmyanmar.api.ExtractorListener;
import com.two.channelmyanmar.api.FileSizeTask;
import com.two.channelmyanmar.bottomsheet.LoadingSheet;
import com.two.channelmyanmar.databinding.DeepLinkActivityBinding;
import com.two.channelmyanmar.util.Utils;
import com.two.my_libs.ActivityTheme;
import com.two.my_libs.base.BaseThemeActivity;

/*
 * Created by Toewaioo on 5/22/25
 * Description: [Add class description here]
 */
public class DeepLink extends BaseThemeActivity implements ExtractorListener {
    private final String TAG = this.getClass().getSimpleName();
    DeepLinkActivityBinding binding;


    public DeepLink() {
        // constructor code
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DeepLinkActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        handelIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handelIntent(intent);
    }

    private void handelIntent(Intent intent){
        if (intent == null) {
            Toast.makeText(this, "No data provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Bundle extras = intent.getExtras();
        if (extras == null) {
            Toast.makeText(this, "Missing parameters", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String baseUrl = extras.getString("baseUrl");
        String name    = extras.getString("name");

        if (baseUrl == null) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        binding.editText.setText(baseUrl);
        binding.goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSheet(binding.editText.getText().toString(),false);
            }
        });

    }
    public void loadSheet(String link, boolean isDirect) {
        LoadingSheet sheet = new LoadingSheet(link, this, isDirect);
        sheet.show(getSupportFragmentManager(), sheet.getTag());
    }

    @Override
    protected void configTheme(ActivityTheme activityTheme) {
        activityTheme.setThemes(new int[]{R.style.AppTheme_Light, R.style.AppTheme_Black});
        activityTheme.setStatusBarColorAttrRes(R.attr.colorPrimary);
        activityTheme.setSupportMenuItemThemeEnable(true);
    }
    public void showDialog(String directLink) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeepLink.this);
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
                Utils.startDownload(DeepLink.this,directLink);
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(directLink));
//                intent.setPackage("idm.internet.download.manager");
//                try {
//                    startActivity(intent);
//                } catch (ActivityNotFoundException e) {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(directLink)));
//                }

            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PlayerActivity.class);
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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
               showDialog(directLink);
            }
        });

    }

    @Override
    public void onSuccessYoteShin(String directLink, boolean isDirect) {
        if(isDirect)
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    showDialog(directLink);
                }
            });
    }

    @Override
    public void onFailed(String message) {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();

    }

    @Override
    public void loading(int progress) {

    }
}
