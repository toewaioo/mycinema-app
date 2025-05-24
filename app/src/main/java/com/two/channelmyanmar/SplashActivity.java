package com.two.channelmyanmar;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.two.channelmyanmar.activity.BrowserActivity;
import com.two.channelmyanmar.bottomsheet.UpdateSheet;
import com.two.channelmyanmar.preference.PreferenceHelper;
import com.two.channelmyanmar.services.DownloadService;
import com.two.channelmyanmar.services.NetworkService;
import com.two.channelmyanmar.updater.UpdateChecker;
import com.two.channelmyanmar.updater.UpdateInfo;
import com.two.channelmyanmar.updater.UpdateListener;
import com.two.channelmyanmar.util.BlurTransform;
import com.two.my_libs.ActivityTheme;
import com.two.my_libs.base.BaseThemeActivity;

import android.Manifest;

import java.io.File;

/*
 * Created by Toewaioo on 4/20/25
 * Description: [Add class description here]
 */
public class SplashActivity extends BaseThemeActivity  implements UpdateSheet.CallBack {

    @Override
    public void onClick(String url) {
        try {
            pendingUrl = url;
            checkPermissionsAndDownload(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private final String TAG = this.getClass().getSimpleName();
    private static final int REQ_STORAGE = 1001;
    private static final int REQ_INSTALL = 1002;

    private DownloadManager downloadManager;
    private long downloadId;
    private String pendingUrl;
    private Uri pendingInstallUri;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "apk_prefs";
    private static final String PREF_DOWNLOAD_ID = "downloadId";
    private static final String PREF_FILE_URI = "fileUri";
    private static final String PREF_IS_COMPLETED = "isCompleted";
    private Uri downloadedApkUri;
    private static final String KEY_SHOW_UPDATE_SHEET = "show_update_sheet";
    private boolean shouldShowUpdateSheet = false;
    private UpdateInfo pendingUpdateInfo;
    private final BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long receivedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (receivedId == downloadId) {
                checkDownloadStatus();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent service=new Intent(this, NetworkService.class);
        startService(service);
        // Listen for completed downloads

    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        if (savedInstanceState != null) {
            shouldShowUpdateSheet = savedInstanceState.getBoolean(KEY_SHOW_UPDATE_SHEET, false);
            pendingUpdateInfo = savedInstanceState.getParcelable("pending_update_info");
        }
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        registerReceiver(downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED);
        ImageView bg= findViewById(R.id.bg_image);
        //
        Glide.with(this).load("https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEi8PQVyu6-c3eQIersLEzxJ3M3lD16EA1YqOd6z2cBZoj2ym3XIhxOxuxkXETG4P33X-a4gNBSuRejnAOD4xPrD7bonIKq1MBzoPrHmIxGJ7CAx5sjbZMCIXMrRlcfGwQ6YtgbmUbcBZE4urV3MZrC2RkSoJilvThWuGZIF6ugUEYtVeL1zVnKd6Y0ZUK4/s320/IMG_20230420_125734.png").apply(RequestOptions.bitmapTransform(new BlurTransform(this, 15))).into(bg);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Start animations
        findViewById(R.id.logo).startAnimation(fadeIn);
        TextView appName = findViewById(R.id.appName);
        appName.startAnimation(slideUp);

        // Setup terms checkbox
        CheckBox termsCheckbox = findViewById(R.id.termsCheckbox);
        String termsText = "I accept the Terms and Privacy Policy";
        SpannableString spannableString = new SpannableString(termsText);

        // Make "Terms" clickable
        termsCheckbox.setChecked(true);
        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent i =new Intent(getApplicationContext(), BrowserActivity.class);
                i.putExtra("baseUrl","https://mycinema-sage.vercel.app/privacy.html");
                startActivity(i);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                // Handle terms click
               // startActivity(new Intent(SplashActivity.this, TermsActivity.class));
            }
        };

        // Make "Privacy Policy" clickable
        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent i =new Intent(getApplicationContext(), BrowserActivity.class);
                i.putExtra("baseUrl","https://mycinema-sage.vercel.app/privacy.html");
                startActivity(i);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                // Handle privacy policy click
              //  startActivity(new Intent(SplashActivity.this, PrivacyPolicyActivity.class));
            }
        };

        spannableString.setSpan(termsSpan,
                termsText.indexOf("Terms"),
                termsText.indexOf("Terms") + "Terms".length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(privacySpan,
                termsText.indexOf("Privacy Policy"),
                termsText.indexOf("Privacy Policy") + "Privacy Policy".length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsCheckbox.setText(spannableString);
        termsCheckbox.setMovementMethod(LinkMovementMethod.getInstance());

        // Delay transition to MainActivity

        String currentVersion;
        try {
            currentVersion = getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // Fallback if something really goes wrong:
            currentVersion = "1.0";
        }

        PreferenceHelper prefs = new PreferenceHelper(this);
        UpdateChecker checker = new UpdateChecker(
                "https://mycinema-sage.vercel.app/update.json",
                currentVersion,
                prefs
        );//change your update app info json file
        checker.checkForUpdate(new UpdateListener() {
            @Override
            public void onNewFound(UpdateInfo info) {
                pendingUpdateInfo = info;
                shouldShowUpdateSheet = true;
                showUpdateSheet(info);

                    // Show dialog with info.whatNew, info.downloadLink, etc.


            }
            @Override
            public void onNotFound() {
                // Optional: silently ignore
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                      overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                     finish();
                }, 3000);
            }
            @Override
            public void onError(Exception e) {
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    finish();
                }, 1000);
                e.printStackTrace();
            }
        });

    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SHOW_UPDATE_SHEET, shouldShowUpdateSheet);
        outState.putParcelable("pending_update_info", pendingUpdateInfo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldShowUpdateSheet && pendingUpdateInfo != null) {
            showUpdateSheet(pendingUpdateInfo);
            shouldShowUpdateSheet = false;
            pendingUpdateInfo = null;
        }
    }
    private void showUpdateSheet(UpdateInfo info) {
        if (isFinishing() || isDestroyed()) return;

        UpdateSheet sheet = UpdateSheet.newInstance(
                info.downloadLink,
                info.whatNew,
                info.version
        );
        sheet.setCallBack(this);

        sheet.show(getSupportFragmentManager(), "update_sheet");
    }

    private void checkExistingDownload() {
        boolean isCompleted = sharedPreferences.getBoolean(PREF_IS_COMPLETED, false);
        if (isCompleted) {
            String uriString = sharedPreferences.getString(PREF_FILE_URI, null);
            if (uriString != null) {
                downloadedApkUri = Uri.parse(uriString);
                if (fileExists(downloadedApkUri)) {
                    updateUIForInstall();
                    return;
                }
            }
        }
        clearDownloadPrefs();
    }

    private boolean fileExists(Uri uri) {
        try {
            getContentResolver().openFileDescriptor(uri, "r").close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateUIForInstall() {
//        Button btn = findViewById(R.id.downloadButton);
//        btn.setText("Install APK");
//        btn.setOnClickListener(v -> installApk());
    }

    private void checkPermissionsAndDownload(String url) {
        if (sharedPreferences.getBoolean(PREF_IS_COMPLETED, false)) {
            installApk();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQ_STORAGE);
            return ;
        }
        startDownload(url);
    }

    private void startDownload(String url) {
        clearDownloadPrefs();
        pendingUrl = url;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle("App Update")
                .setDescription("Downloading...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app.apk");

        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadId = dm.enqueue(request);

        // Save download ID
        sharedPreferences.edit()
                .putLong(PREF_DOWNLOAD_ID, downloadId)
                .putBoolean(PREF_IS_COMPLETED, false)
                .apply();
    }

    private void checkDownloadStatus() {
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);

        try (Cursor cursor = dm.query(query)) {
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    @SuppressLint("Range") String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    downloadedApkUri = Uri.parse(uriString);

                    // Save completed download info
                    sharedPreferences.edit()
                            .putString(PREF_FILE_URI, uriString)
                            .putBoolean(PREF_IS_COMPLETED, true)
                            .apply();

                    updateUIForInstall();
                    installApk();
                }
            }
        }
    }
    private void showInstallPermissionDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "Allow install from unknown sources", Toast.LENGTH_LONG).show();
                return;
            }
        }
//        new AlertDialog.Builder(this)
//                .setTitle("Install Permission Needed")
//                .setMessage("Allow installation from unknown sources?")
//                .setPositiveButton("Settings", (d, w) -> openInstallPermissionSettings())
//                .setNegativeButton("Cancel", null)
//                .show();
    }

    private void openInstallPermissionSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQ_INSTALL);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_INSTALL) {
            installApk(); // Retry installation after returning from settings
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STORAGE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDownload(pendingUrl);
        } else {
            Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
        }
    }


    private void installApk() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !getPackageManager().canRequestPackageInstalls()) {
            showInstallPermissionDialog();
            return;
        }

        String downloadedApkUria= sharedPreferences.getString(PREF_FILE_URI, null).substring(6);
        Intent installIntent = new Intent(Intent.ACTION_VIEW)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri apkUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", new File(downloadedApkUria));
        installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(installIntent);
    }

    private void clearDownloadPrefs() {
        sharedPreferences.edit()
                .remove(PREF_DOWNLOAD_ID)
                .remove(PREF_FILE_URI)
                .remove(PREF_IS_COMPLETED)
                .apply();
    }

    // Rest of the methods remain same as previous implementation
    // (showInstallPermissionDialog, openInstallPermissionSettings, etc.)

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadCompleteReceiver);
    }


    @Override
    protected void configTheme(ActivityTheme activityTheme) {
        activityTheme.setThemes(new int[]{R.style.AppTheme_Light, R.style.AppTheme_Black});
        activityTheme.setStatusBarColorAttrRes(R.attr.colorPrimary);
        activityTheme.setSupportMenuItemThemeEnable(true);
    }
}
