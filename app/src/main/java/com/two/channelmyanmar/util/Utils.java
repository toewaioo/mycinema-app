package com.two.channelmyanmar.util;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.media3.common.Format;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

import com.two.channelmyanmar.R;
import com.two.channelmyanmar.activity.PlayerActivity;
import com.two.channelmyanmar.preference.PreferenceHelper;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Utils {
    public static void startDownload(Context context, String url) {
        PreferenceHelper helper = new PreferenceHelper(context);

        if (helper.useSystemDownload()) {
            startSystemDownload(context, url, true);
            Log.d("Utils", "useSystem");
        } else {
            handleAlternativeDownload(context, url);
            Log.d("Utils", "notuseSystem");
        }
    }

    /**
     * Uses DownloadManager with optional fallback to alternative handler.
     */
    private static void startSystemDownload(Context context, String url, boolean allowFallback) {
        try {
            DownloadManager.Request request = createDownloadRequest(context, url);
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
            } else {
                throw new IllegalStateException("DownloadManager not available");
            }
        } catch (Exception e) {
            Log.e("Utils", "system download failed", e);
            if (allowFallback) {
                handleAlternativeDownload(context, url);
            }
        }
    }

    /**
     * If no system download or chooser-handling app, falls back to systemDownload once.
     */
    private static void handleAlternativeDownload(Context context, String url) {
        // 1) Build your VIEW intent
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("idm.internet.download.manager");
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }

    }

    /**
     * Builds a DownloadManager.Request that writes to MediaStore on Q+ or public Downloads on older.
     */
    private static DownloadManager.Request createDownloadRequest(Context context, String url) {
        Uri uri = Uri.parse(url);
        DownloadManager.Request req = new DownloadManager.Request(uri)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedNetworkTypes(
                        DownloadManager.Request.NETWORK_WIFI |
                                DownloadManager.Request.NETWORK_MOBILE
                )
                .setRequiresCharging(false)
                .setAllowedOverMetered(true);

        String fileName = URLUtil.guessFileName(url, null, null);
        String mime = getMimeTypeFromUrl(url);

        if (!TextUtils.isEmpty(fileName)) {
            req.setTitle(fileName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: insert into MediaStore.Downloads

                // fallback to app-specific folder
                req.setDestinationInExternalFilesDir(
                        context,
                        Environment.DIRECTORY_DOWNLOADS,
                        fileName
                );

            } else {
                // Android 7–9: legacy public Downloads folder
                req.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        fileName
                );
            }
        }

        if (!TextUtils.isEmpty(mime)) {
            req.setMimeType(mime);
        }
        return req;
    }

    /**
     * Utility: extract MIME type from URL extension.
     */
    private static String getMimeTypeFromUrl(String url) {
        String ext = android.webkit.MimeTypeMap.getFileExtensionFromUrl(url);
        if (TextUtils.isEmpty(ext)) return null;
        return android.webkit.MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(ext.toLowerCase(Locale.US));
    }

    public static void showPopupMenu(View view, String title) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.option, popupMenu.getMenu());

        // Optional: Set a custom title for the PopupMenu
        popupMenu.setGravity(Gravity.END);

        // Optional: Customize the style of the PopupMenu
        // Context context = new ContextThemeWrapper(this, );
        //popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_copy) {
                    ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Title", title);
                    clipboard.setPrimaryClip(clip);
                }
                if (item.getItemId() == R.id.menu_share) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, title);
                    // Start the share activity
                    view.getContext().startActivity(Intent.createChooser(shareIntent, "Share URL"));

                }

                return false;
            }
        });
        // Show the PopupMenu
        popupMenu.show();
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static float pxToDp(float px) {
        return px / Resources.getSystem().getDisplayMetrics().density;
    }

    public static boolean fileExists(final Context context, final Uri uri) {
        if ("file".equals(uri.getScheme())) {
            final File file = new File(uri.getPath());
            return file.exists();
        } else {
            try {
                final InputStream inputStream = context.getContentResolver().openInputStream(uri);
                inputStream.close();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public static void hideSystemUi(final PlayerView playerView) {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        // demo
//        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public static void showSystemUi(final PlayerView playerView) {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        try {
            if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                try (Cursor cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        final int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (columnIndex > -1)
                            result = cursor.getString(columnIndex);
                    }
                }
            }
            if (result == null) {
                result = uri.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
            if (result.indexOf(".") > 0)
                result = result.substring(0, result.lastIndexOf("."));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isVolumeMax(final AudioManager audioManager) {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public static boolean isVolumeMin(final AudioManager audioManager) {
        int min = Build.VERSION.SDK_INT >= 28 ? audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC) : 0;
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == min;
    }

//        public static void adjustVolume(final AudioManager audioManager, final CustomStyledPlayerView playerView, final boolean raise, boolean canBoost) {
//            final int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//            final int volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//            boolean volumeActive = volume != 0;
//
//            // Handle volume changes outside the app (lose boost if volume is not maxed out)
//            if (volume != volumeMax) {
//                PlayerActivity.boostLevel = 0;
//            }
//
//            if (PlayerActivity.loudnessEnhancer == null)
//                canBoost = false;
//
//            if (volume != volumeMax || (PlayerActivity.boostLevel == 0 && !raise)) {
//                if (PlayerActivity.loudnessEnhancer != null)
//                    PlayerActivity.loudnessEnhancer.setEnabled(false);
//                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, raise ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
//                final int volumeNew = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                if (raise && volume == volumeNew && !isVolumeMin(audioManager)) {
//                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI);
//                } else {
//                    volumeActive = volumeNew != 0;
//                    playerView.setCustomErrorMessage(volumeActive ? " " + volumeNew : "");
//                }
//            } else {
//                if (canBoost && raise && PlayerActivity.boostLevel < 10)
//                    PlayerActivity.boostLevel++;
//                else if (!raise && PlayerActivity.boostLevel > 0)
//                    PlayerActivity.boostLevel--;
//
//                if (PlayerActivity.loudnessEnhancer != null) {
//                    try {
//                        PlayerActivity.loudnessEnhancer.setTargetGain(PlayerActivity.boostLevel * 200);
//                    } catch (RuntimeException e) {
//                        e.printStackTrace();
//                    }
//                }
//                playerView.setCustomErrorMessage(" " + (volumeMax + PlayerActivity.boostLevel));
//            }
//
//            playerView.setIconVolume(volumeActive);
//            if (PlayerActivity.loudnessEnhancer != null)
//                PlayerActivity.loudnessEnhancer.setEnabled(PlayerActivity.boostLevel > 0);
//            playerView.setHighlight(PlayerActivity.boostLevel > 0);
//        }

//        public static void setButtonEnabled(final Context context, final ImageButton button, final boolean enabled) {
//            button.setEnabled(enabled);
//            button.setAlpha(enabled ?
//                    (float) context.getResources().getInteger(R.integer.exo_media_button_opacity_percentage_enabled) / 100 :
//                    (float) context.getResources().getInteger(R.integer.exo_media_button_opacity_percentage_disabled) / 100
//            );
//        }

//        public static void showText(final CustomStyledPlayerView playerView, final String text, final long timeout) {
//            playerView.removeCallbacks(playerView.textClearRunnable);
//            playerView.clearIcon();
//            playerView.setCustomErrorMessage(text);
//            playerView.postDelayed(playerView.textClearRunnable, timeout);
//        }
//
//        public static void showText(final CustomStyledPlayerView playerView, final String text) {
//            showText(playerView, text, 1200);
//        }

    public enum Orientation {
        VIDEO(0, R.string.video_orientation_video),
        SENSOR(1, R.string.video_orientation_sensor);

        public final int value;
        public final int description;

        Orientation(int type, int description) {
            this.value = type;
            this.description = description;
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public static void setOrientation(Activity activity, Orientation orientation) {
        switch (orientation) {
            case VIDEO:
                if (PlayerActivity.exoPlayer != null) {
                    final Format format = PlayerActivity.exoPlayer.getVideoFormat();
                    if (format != null && isPortrait(format))
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    else
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                } else {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }

                break;
            case SENSOR:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                break;
            /*case SYSTEM:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;*/
        }
    }

//        public static Orientation getNextOrientation(Orientation orientation) {
//            switch (orientation) {
//                case VIDEO:
//                    return Orientation.SENSOR;
//                case SENSOR:
//                default:
//                    return Orientation.VIDEO;
//            }
//        }

    @OptIn(markerClass = UnstableApi.class)
    public static boolean isRotated(final Format format) {
        return format.rotationDegrees == 90 || format.rotationDegrees == 270;
    }

    public static boolean isPortrait(final Format format) {
        if (isRotated(format)) {
            return format.width > format.height;
        } else {
            return format.height > format.width;
        }
    }

    public static String formatMilis(long time) {
        final int totalSeconds = Math.abs((int) time / 1000);
        final int seconds = totalSeconds % 60;
        final int minutes = totalSeconds % 3600 / 60;
        final int hours = totalSeconds / 3600;

        return (hours > 0 ? String.format("%d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds));
    }

    public static String formatMilisSign(long time) {
        if (time > -1000 && time < 1000)
            return formatMilis(time);
        else
            return (time < 0 ? "−" : "+") + formatMilis(time);
    }

//        public static void log(final String text) {
//            if (BuildConfig.DEBUG) {
//                Log.d("JustPlayer", text);
//            }
//        }

    public static void setViewParams(final View view, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom, int marginLeft, int marginTop, int marginRight, int marginBottom) {
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

        final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
        view.setLayoutParams(layoutParams);
    }

    public static boolean isDeletable(final Context context, final Uri uri) {
        try {
            if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                try (Cursor cursor = context.getContentResolver().query(uri, new String[]{DocumentsContract.Document.COLUMN_FLAGS}, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        final int columnIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_FLAGS);
                        if (columnIndex > -1) {
                            int flags = cursor.getInt(columnIndex);
                            return (flags & DocumentsContract.Document.FLAG_SUPPORTS_DELETE) == DocumentsContract.Document.FLAG_SUPPORTS_DELETE;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isSupportedNetworkUri(final Uri uri) {
        final String scheme = uri.getScheme();
        return scheme.startsWith("http") || scheme.equals("rtsp");
    }

//        public static boolean isTvBox(Activity activity) {
//            return activity.getResources().getBoolean(R.bool.tv_box);
//        }

    public static int normRate(float rate) {
        return (int) (rate * 100f);
    }

//        public static boolean switchFrameRate(final Activity activity, final float frameRateExo, final Uri uri) {
//            if (!Utils.isTvBox(activity))
//                return false;
//
//            float frameRate = Format.NO_VALUE;
//
//            // preferredDisplayModeId only available on SDK 23+
//            // ExoPlayer already uses Surface.setFrameRate() on Android 11+ but may not detect actual video frame rate
//            if (Build.VERSION.SDK_INT >= 23 && (Build.VERSION.SDK_INT < 30 || (frameRateExo == Format.NO_VALUE))) {
//                String path;
//                if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
//                    path = FFmpegKitConfig.getSafParameterForRead(activity, uri);
//                } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
//                    // TODO: FFprobeKit doesn't accept encoded uri (like %20) (?!)
//                    path = uri.getSchemeSpecificPart();
//                } else {
//                    path = uri.toString();
//                }
//                // Use ffprobe as ExoPlayer doesn't detect video frame rate for lots of videos
//                // and has different precision than ffprobe (so do not mix that)
//                MediaInformationSession mediaInformationSession = FFprobeKit.getMediaInformation(path);
//                MediaInformation mediaInformation = mediaInformationSession.getMediaInformation();
//                if (mediaInformation == null)
//                    return false;
//                List<StreamInformation> streamInformations = mediaInformation.getStreams();
//                for (StreamInformation streamInformation : streamInformations) {
//                    if (streamInformation.getType().equals("video")) {
//                        String averageFrameRate = streamInformation.getAverageFrameRate();
//                        if (averageFrameRate.contains("/")) {
//                            String[] vals = averageFrameRate.split("/");
//                            frameRate = Float.parseFloat(vals[0]) / Float.parseFloat(vals[1]);
//                            break;
//                        }
//                    }
//                }
//
//                if (BuildConfig.DEBUG)
//                    Toast.makeText(activity, "Video frameRate: " + frameRate, Toast.LENGTH_LONG).show();
//
//                if (frameRate != Format.NO_VALUE) {
//                    Display display = activity.getWindow().getDecorView().getDisplay();
//                    Display.Mode[] supportedModes = display.getSupportedModes();
//                    Display.Mode activeMode = display.getMode();
//
//                    if (supportedModes.length > 1) {
//                        // Refresh rate >= video FPS
//                        List<Display.Mode> modesHigh = new ArrayList<>();
//                        // Max refresh rate
//                        Display.Mode modeTop = activeMode;
//                        int modesResolutionCount = 0;
//
//                        // Filter only resolutions same as current
//                        for (Display.Mode mode : supportedModes) {
//                            if (mode.getPhysicalWidth() == activeMode.getPhysicalWidth() &&
//                                    mode.getPhysicalHeight() == activeMode.getPhysicalHeight()) {
//                                modesResolutionCount++;
//
//                                if (normRate(mode.getRefreshRate()) >= normRate(frameRate))
//                                    modesHigh.add(mode);
//
//                                if (normRate(mode.getRefreshRate()) > normRate(modeTop.getRefreshRate()))
//                                    modeTop = mode;
//                            }
//                        }
//
//                        if (modesResolutionCount > 1) {
//                            Display.Mode modeBest = null;
//
//                            for (Display.Mode mode : modesHigh) {
//                                if (normRate(mode.getRefreshRate()) % normRate(frameRate) <= 0.0001f) {
//                                    if (modeBest == null || normRate(mode.getRefreshRate()) > normRate(modeBest.getRefreshRate())) {
//                                        modeBest = mode;
//                                    }
//                                }
//                            }
//
//                            Window window = activity.getWindow();
//                            WindowManager.LayoutParams layoutParams = window.getAttributes();
//
//                            if (modeBest == null)
//                                modeBest = modeTop;
//
//                            final boolean switchingModes = !(modeBest.getModeId() == activeMode.getModeId());
//                            if (switchingModes) {
//                                layoutParams.preferredDisplayModeId = modeBest.getModeId();
//                                window.setAttributes(layoutParams);
//                            }
//                            if (BuildConfig.DEBUG)
//                                Toast.makeText(activity, "Video frameRate: " + frameRate + "\nDisplay refreshRate: " + modeBest.getRefreshRate(), Toast.LENGTH_LONG).show();
//                            return switchingModes;
//                        }
//                    }
//                }
//            }
//            return false;
//        }

    //        public static boolean alternativeChooser(PlayerActivity activity, Uri initialUri, boolean video) {
//            String startPath;
//            if (initialUri != null && (new File(initialUri.getSchemeSpecificPart())).exists()) {
//                startPath = initialUri.getSchemeSpecificPart();
//            } else {
//                startPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
//            }
//
//            final String[] suffixes = (video ? new String[] { "3gp", "m4v", "mkv", "mov", "mp4", "webm" } :
//                    new String[] { "srt", "ssa", "ass", "vtt", "ttml", "dfxp", "xml" });
//
//            ChooserDialog chooserDialog = new ChooserDialog(activity)
//                    .withStartFile(startPath)
//                    .withFilter(false, false, suffixes)
//                    .withChosenListener(new ChooserDialog.Result() {
//                        @Override
//                        public void onChoosePath(String path, File pathFile) {
//                            activity.releasePlayer();
//                            Uri uri = Uri.parse(pathFile.toURI().toString());
//                            if (video) {
//                                activity.mPrefs.updateMedia(activity, uri, null);
//                                activity.searchSubtitles();
//                            } else {
//                                // Convert subtitles to UTF-8 if necessary
//                                SubtitleUtils.clearCache(activity);
//                                uri = SubtitleUtils.convertToUTF(activity, uri);
//
//                                activity.mPrefs.updateSubtitle(uri);
//                            }
//                            PlayerActivity.focusPlay = true;
//                            activity.initializePlayer();
//                        }
//                    })
//                    // to handle the back key pressed or clicked outside the dialog:
//                    .withOnCancelListener(new DialogInterface.OnCancelListener() {
//                        public void onCancel(DialogInterface dialog) {
//                            dialog.cancel(); // MUST have
//                        }
//                    });
//            chooserDialog
//                    .withOnBackPressedListener(dialog -> chooserDialog.goBack())
//                    .withOnLastBackPressedListener(dialog -> dialog.cancel());
//            chooserDialog.build().show();
//
//            return true;
//        }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            // Get the active network
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) {
                return false;
            }
            // Get the capabilities of the active network
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    private static final Map<String, ThreadLocal<SimpleDateFormat>> Date_format = new ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>>();

    private static SimpleDateFormat getSimpleDateFormat(String format) {
        ThreadLocal<SimpleDateFormat> threadLocal = Date_format.get(format);
        if (threadLocal == null) {
            threadLocal = new ThreadLocal<>();
            Date_format.put(format, threadLocal);

        }
        SimpleDateFormat sdf = threadLocal.get();
        if (sdf == null) {
            sdf = new SimpleDateFormat(format, Locale.getDefault());
            threadLocal.set(sdf);
        }
        return sdf;

    }

    public static String date_to_string(Date date) {
        return getSimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
