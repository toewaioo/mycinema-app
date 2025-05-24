package com.two.channelmyanmar.services;

/*
 * Created by Toewaioo on 4/24/25
 * Description: [Add class description here]
 */
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

public class DownloadReceiver extends BroadcastReceiver {
    private static final String PREFS_NAME = "apk_prefs";
    private static final String KEY_COMPLETE = "download_complete";
    private static final String KEY_URI = "apk_uri";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor c = dm.query(new DownloadManager.Query().setFilterById(downloadId));
            if (c.moveToFirst()) {
                @SuppressLint("Range")
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    @SuppressLint("Range")
                    String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    // Save to SharedPreferences
                    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean(KEY_COMPLETE, true)
                            .putString(KEY_URI, uriString)
                            .apply();
                    installApk(context, Uri.parse(uriString));
                } else {
                    Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
                }
            }
            c.close();
        }
    }

    private void installApk(Context context, Uri apkUri) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(apkUri, "application/vnd.android.package-archive");
        install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(install);
    }
}
