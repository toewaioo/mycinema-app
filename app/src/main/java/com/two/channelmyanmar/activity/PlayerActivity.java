package com.two.channelmyanmar.activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.ConcatenatingMediaSource;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.two.channelmyanmar.R;
import com.two.channelmyanmar.util.Utils;
import com.two.my_libs.ActivityTheme;
import com.two.my_libs.base.BaseThemeActivity;
import java.net.URLDecoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/*
 * Created by Toewaioo on 4/4/25
 * Description: [Add class description here]
 */
@UnstableApi
public class PlayerActivity extends BaseThemeActivity {
    private static final String TAG = "Player";
    private static final int SEEK_INTERVAL_MS = 15000;
    private static final long ANIMATION_FADE_DURATION = 550;
    private static final long ANIMATION_SLIDE_DURATION = 250;
    private static final long ANIMATION_VISIBLE_MS = 600;

    // Player instance should not be static to prevent memory leaks
    public static ExoPlayer exoPlayer;
    private PlayerView playerView;

    // Views
    private TextView tvVideoTitle;
    private ImageButton btnAspectRatio, btnFullscreen;
    private LinearLayout seekOverlayLeft, seekOverlayRight;
    private ImageView seekIconRight, seekIconLeft;
    private TextView seekTextLeft, seekTextRight;

    // Player state
    private long playbackPosition = 0;
    private int currentWindow = 0;
    private boolean playWhenReady = true;
    private String currentVideoUrl = "https://www.sample-videos.com/video321/mp4/360/big_buck_bunny_360p_2mb.mp4";

    // Aspect ratio
    private static final int[] RESIZE_MODES = {
            AspectRatioFrameLayout.RESIZE_MODE_FIT,
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
            AspectRatioFrameLayout.RESIZE_MODE_FILL
    };
    private static final int[] RESIZE_MODE_ICONS = {
            R.drawable.aspect_ratio,
            R.drawable.fullscreen,
            R.drawable.fit_screen
    };
    private static final String[] RESIZE_MODE_LABELS = {"Fit", "Zoom", "Fill"};
    private int currentResizeModeIndex = 0;

    // Gestures
    private GestureDetector gestureDetector;
    private Handler animationHandler = new Handler();
    private Runnable hideAnimationRunnable;
    RelativeLayout root;
    ConcatenatingMediaSource concatenatingMediaSource;
    ImageView nextButton, previousButton;
    //horizontal recyclerview variables
    boolean expand = false;
    View nightMode;
    boolean dark = false;
    boolean mute = false;
    PlaybackParameters parameters;
    float speed;

    Uri uriSubtitle;

    boolean isCrossChecked;
    FrameLayout eqContainer;
    //horizontal recyclerview variables
    //
    //swipe and zoom variables
    private int device_height, device_width, brightness, media_volume;
    boolean start = false;
    boolean left, right;
    private float baseX, baseY;
    boolean swipe_move = false;
    private long diffX, diffY;
    public static final int MINIMUM_DISTANCE = 100;
    boolean success = false;
    TextView vol_text, brt_text, total_duration;
    ProgressBar vol_progress, brt_progress;
    LinearLayout vol_progress_container, vol_text_container, brt_progress_container, brt_text_container;
    ImageView vol_icon, brt_icon;
    AudioManager audioManager;
    private ContentResolver contentResolver;
    private Window window;
    boolean singleTap = false;

    RelativeLayout zoomLayout;
    RelativeLayout zoomContainer;
    TextView zoom_perc;
    ScaleGestureDetector scaleGestureDetector;
    private float scale_factor = 1.0f;
    boolean double_tap = false;
    TextView title;
    RelativeLayout double_tap_playpause;
    private ControlsMode controlsMode;

    public enum ControlsMode {
        LOCK, FULLSCREEN
    }

    ImageView videoBack, lock, unlock, scaling, videoList, videoMore,play,pause;

    @Override
    protected void configTheme(ActivityTheme activityTheme) {
        activityTheme.setThemes(new int[]{R.style.AppTheme_Light, R.style.AppTheme_Black});
        activityTheme.setStatusBarColorAttrRes(R.attr.colorPrimary);
        activityTheme.setSupportMenuItemThemeEnable(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        Utils.setOrientation(this, Utils.Orientation.SENSOR);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        device_width = displayMetrics.widthPixels;
        device_height = displayMetrics.heightPixels;

        hideBottomBar();
        handleIntentData();
        setupGestureDetection();
        restoreInstanceState(savedInstanceState);
        //setupWindowInsetsController();
        //setupSeekAnimation();
        hideAnimationRunnable = () -> {
            if (vol_progress_container.getVisibility() == View.VISIBLE) {
                vol_progress_container.animate()
                        .alpha(0f) // Fade out
                        .setDuration(ANIMATION_FADE_DURATION)
                        .setInterpolator(new AccelerateInterpolator()) // Speed up fade out
                        .withEndAction(() -> vol_progress_container.setVisibility(View.GONE))
                        .start();
            }
            if (vol_text_container.getVisibility() == View.VISIBLE) {
                vol_text_container.animate()
                        .alpha(0f) // Fade out
                        .setDuration(ANIMATION_FADE_DURATION)
                        .setInterpolator(new AccelerateInterpolator()) // Speed up fade out
                        .withEndAction(() -> vol_text_container.setVisibility(View.GONE))
                        .start();
            }
            if (brt_progress_container.getVisibility() == View.VISIBLE) {
                brt_progress_container.animate()
                        .alpha(0f) // Fade out
                        .setDuration(ANIMATION_FADE_DURATION)
                        .setInterpolator(new AccelerateInterpolator()) // Speed up fade out
                        .withEndAction(() -> brt_progress_container.setVisibility(View.GONE))
                        .start();
            }
            if (brt_text_container.getVisibility() == View.VISIBLE) {
                brt_text_container.animate()
                        .alpha(0f) // Fade out
                        .setDuration(ANIMATION_FADE_DURATION)
                        .setInterpolator(new AccelerateInterpolator()) // Speed up fade out
                        .withEndAction(() -> brt_text_container.setVisibility(View.GONE))
                        .start();
            }
        };
    }


    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
        pause =findViewById(R.id.exo_pause);
        play = findViewById(R.id.exo_play);
        nextButton = findViewById(R.id.exo_next);
        previousButton = findViewById(R.id.exo_prev);
        total_duration = findViewById(R.id.exo_duration);
        title = findViewById(R.id.video_title);
        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
        scaling = findViewById(R.id.scaling);
        root = findViewById(R.id.root_layout);
        nightMode = findViewById(R.id.night_mode);
        eqContainer = findViewById(R.id.eqFrame);
        vol_text = findViewById(R.id.vol_text);
        brt_text = findViewById(R.id.brt_text);
        vol_progress = findViewById(R.id.vol_progress);
        brt_progress = findViewById(R.id.brt_progress);
        vol_progress_container = findViewById(R.id.vol_progress_container);
        brt_progress_container = findViewById(R.id.brt_progress_container);
        vol_text_container = findViewById(R.id.vol_text_container);
        brt_text_container = findViewById(R.id.brt_text_container);
        vol_icon = findViewById(R.id.vol_icon);
        brt_icon = findViewById(R.id.brt_icon);
        zoomLayout = findViewById(R.id.zoom_layout);
        zoom_perc = findViewById(R.id.zoom_percentage);
        zoomContainer = findViewById(R.id.zoom_container);
        double_tap_playpause = findViewById(R.id.double_tap_play_pause);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        scaling.setOnClickListener(firstListener);
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleDetector());
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play.setVisibility(View.INVISIBLE);
                pause.setVisibility(View.VISIBLE);
                exoPlayer.setPlayWhenReady(true);
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play.setVisibility(View.VISIBLE);
                pause.setVisibility(View.INVISIBLE);
                exoPlayer.setPlayWhenReady(false);
            }
        });
        title.setText(getFileNameFromUrl(currentVideoUrl));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupGestureDetection() {
        gestureDetector = new GestureDetector(this, new PlayerGestureListener());
        playerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void handleIntentData() {
        String url = getIntent().getStringExtra("url"); // Assuming you pass the URL with this key

        if (TextUtils.isEmpty(url)) {
            Uri videoUri = getIntent().getData();
            if (videoUri != null) {
                url = videoUri.toString();
            }
        }
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "No valid video URL provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentVideoUrl = url;
        initializeViews();
        initializePlayer();
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void hideBottomBar() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decodeView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decodeView.setSystemUiVisibility(uiOptions);
        }
    }

    private void setupWindowInsetsController() {
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(
                getWindow(),
                getWindow().getDecorView()
        );
        insetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
    }


    private void initializePlayer() {
        releasePlayer(); // Ensure previous player is cleaned up
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);
        //  setupControllerComponents();

        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(currentVideoUrl)
                .setMediaMetadata(new MediaMetadata.Builder().setTitle(extractTitleFromUrl(currentVideoUrl)).build())
                .build();

        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.setPlayWhenReady(playWhenReady);
        exoPlayer.seekTo(currentWindow, playbackPosition);
        exoPlayer.addListener(new PlayerEventListener());
        exoPlayer.prepare();
    }


    private void cycleAspectRatio() {
        currentResizeModeIndex = (currentResizeModeIndex + 1) % RESIZE_MODES.length;
        playerView.setResizeMode(RESIZE_MODES[currentResizeModeIndex]);
        updateAspectRatioButton();
        showAspectRatioToast();
    }

    private void updateAspectRatioButton() {
        btnAspectRatio.setImageResource(RESIZE_MODE_ICONS[currentResizeModeIndex]);
    }

    private void showAspectRatioToast() {
        Toast.makeText(this,
                "Aspect Ratio: " + RESIZE_MODE_LABELS[currentResizeModeIndex],
                Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void toggleFullscreen() {
        int orientation = getResources().getConfiguration().orientation;
        boolean isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE;

        setRequestedOrientation(isLandscape ?
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT :
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), isLandscape);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(
                getWindow(),
                getWindow().getDecorView()
        );

        if (isLandscape) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars());
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars());
        }
    }

    // Simplified lifecycle methods
    @Override
    protected void onStart() {
        super.onStart();
        initializePlayerIfNeeded();
        playerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePlayerState();
        playerView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    private void initializePlayerIfNeeded() {
        if (exoPlayer == null && !currentVideoUrl.isEmpty()) {
            initializePlayer();
        }
    }

    private void savePlayerState() {
        if (exoPlayer != null) {
            playbackPosition = exoPlayer.getCurrentPosition();
            currentWindow = exoPlayer.getCurrentMediaItemIndex();
            playWhenReady = exoPlayer.getPlayWhenReady();
        }
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            savePlayerState();
            exoPlayer.release();
            exoPlayer = null;
            playerView.setPlayer(null);
        }
    }
    View.OnClickListener firstListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fullscreen);

            Toast.makeText(PlayerActivity.this, "Full Screen", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(secondListener);
        }
    };
    View.OnClickListener secondListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.zoom);

            Toast.makeText(PlayerActivity.this, "Zoom", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(thirdListener);
        }
    };
    View.OnClickListener thirdListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fit);

            Toast.makeText(PlayerActivity.this, "Fit", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(firstListener);
        }
    };

    // Gesture handling
    private class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            if (singleTap) {
                playerView.showController();
                singleTap = false;
            } else {
                playerView.hideController();
                singleTap = true;
            }
            if (double_tap_playpause.getVisibility() == View.VISIBLE) {
                double_tap_playpause.setVisibility(View.GONE);
            }

            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            playerView.hideController();
            boolean isRightSide = e.getX() > playerView.getWidth() / 2f;
            if (isRightSide) seekForward();
            else seekBackward();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            playerView.showController();
            float screenWidth = playerView.getWidth();
            boolean isLeftSide = e1.getX() < screenWidth / 2f;
            if (Math.abs(distanceY) > Math.abs(distanceX)) {
                if (isLeftSide) {
                    adjustBrightness(distanceY);
                } else {
                    adjustVolume(distanceY);
                }
                return true;
            }
            return false;
        }
    }
    public static String getFileNameFromUrl(String urlString) {
        try {
            // Parse the URL
            URL url = new URL(urlString);
            // Get and decode the path
            String path = url.getPath();
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8.toString());

            // Extract the file name using basic string manipulation
            String fileName = null;
            int lastSlashIndex = decodedPath.lastIndexOf('/');
            if (lastSlashIndex >= 0 && lastSlashIndex < decodedPath.length() - 1) {
                fileName = decodedPath.substring(lastSlashIndex + 1);
            }

            // If the file name is not valid (e.g., empty or missing an extension), try the query parameters
            if (fileName == null || fileName.isEmpty() || !fileName.contains(".")) {
                String query = url.getQuery();
                if (query != null && !query.isEmpty()) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] pair = param.split("=");
                        if (pair.length == 2) {
                            // Look for parameter keys that might contain the file name
                            if (pair[0].equalsIgnoreCase("file") || pair[0].equalsIgnoreCase("filename")) {
                                fileName = URLDecoder.decode(pair[1], StandardCharsets.UTF_8.toString());
                                break;
                            }
                        }
                    }
                }
            }

            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "Error extracting file name from URL: " + urlString, e);
            return null;
        }
    }

    //
    private void adjustVolume(float deltaY) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) return;

        int stream = AudioManager.STREAM_MUSIC;
        int currentVolume = audioManager.getStreamVolume(stream);
        int maxVolume = audioManager.getStreamMaxVolume(stream);
        int minVolume = 0;

        // Calculate volume change with sensitivity adjustment
        float sensitivity = 20f;
        int change = (int) (deltaY / sensitivity);
        int newVolume = Math.max(minVolume, Math.min(currentVolume + change, maxVolume));

        if (newVolume != currentVolume) {
            audioManager.setStreamVolume(stream, newVolume, 0);
            int volumePercent = (int) ((newVolume / (float) maxVolume) * 100);
            vol_text_container.setVisibility(View.VISIBLE);
            vol_text.setText(" " + (String.valueOf(volumePercent)) + "%");
            if (volumePercent < 1) {
                vol_icon.setImageResource(R.drawable.ic_volume_off);
                vol_text.setVisibility(View.VISIBLE);
                vol_text.setText("Off");
            } else if (volumePercent >= 1) {
                vol_icon.setImageResource(R.drawable.ic_volume);
                vol_text.setVisibility(View.VISIBLE);
            }
            vol_progress_container.setVisibility(View.VISIBLE);
            vol_progress.setProgress((int) volumePercent);

            showVolumeAnimation(true, "Volume: " + volumePercent + "%");
        }
    }
    private void showVolumeAnimation(boolean isLeft, String text) {

        animateSeekOverlays(true, vol_progress_container);
        animateSeekOverlays(true,vol_text_container);
        animationHandler.removeCallbacks(hideAnimationRunnable);
        animationHandler.postDelayed(hideAnimationRunnable, ANIMATION_VISIBLE_MS);
    }

    private void adjustBrightness(float deltaY) {
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        float currentBrightness = layoutParams.screenBrightness;

        // Handle default system brightness case
        if (currentBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            currentBrightness = 1.0f;
        }

        // Calculate brightness change with sensitivity adjustment
        float sensitivity = 20f;
        float change = deltaY / (sensitivity * 100); // Convert to 0-1 range
        float newBrightness = Math.max(0.0f, Math.min(currentBrightness + change, 1.0f));


        if (newBrightness != currentBrightness) {
            layoutParams.screenBrightness = newBrightness;
            window.setAttributes(layoutParams);
            int brightnessPercent = (int) (newBrightness * 100);
            brt_progress_container.setVisibility(View.VISIBLE);
            brt_text_container.setVisibility(View.VISIBLE);
            brt_progress.setProgress((int) brightnessPercent);

            if (brightnessPercent < 30) {
                brt_icon.setImageResource(R.drawable.ic_brightness_low);
            } else if (brightnessPercent > 30 && brightnessPercent < 80) {
                brt_icon.setImageResource(R.drawable.ic_brightness_moderate);
            } else if (brightnessPercent > 80) {
                brt_icon.setImageResource(R.drawable.ic_brightness);
            }

            brt_text.setText(" " + (int) brightnessPercent + "%");
            showVolumeBrightnessAnimation(false, "Brightness: " + brightnessPercent + "%");
        }
    }

    private void showVolumeBrightnessAnimation(boolean isLeft, String text) {

        animateSeekOverlays(true, brt_progress_container);
        animateSeekOverlays(true,brt_text_container);
        animationHandler.removeCallbacks(hideAnimationRunnable);
        animationHandler.postDelayed(hideAnimationRunnable, ANIMATION_VISIBLE_MS);
    }

    //

    private void seekForward() {
        performSeek(SEEK_INTERVAL_MS);
        showSeekAnimation(true);
    }

    private void seekBackward() {
        performSeek(-SEEK_INTERVAL_MS);
        showSeekAnimation(false);
    }

    private void performSeek(long interval) {
        if (exoPlayer == null) return;

        long newPosition = exoPlayer.getCurrentPosition() + interval;
        newPosition = MathUtils.clamp(newPosition, 0, exoPlayer.getDuration());
        exoPlayer.seekTo(newPosition);
    }

    private void showSeekAnimation(boolean forward) {
//        LinearLayout overlay = forward ? seekOverlayRight : seekOverlayLeft;
//        animateSeekOverlays(true, overlay);
//        animationHandler.removeCallbacks(hideAnimationRunnable);
//        animationHandler.postDelayed(hideAnimationRunnable, ANIMATION_VISIBLE_MS);
    }

    private void animateSeekOverlays(boolean show, LinearLayout... overlays) {
        for (LinearLayout overlay : overlays) {
            overlay.animate().cancel();
            float alpha = show ? 1f : 0f;
            float translation = show ? 0f : (overlay == seekOverlayLeft ?
                    overlay.getWidth() / 3f :
                    -overlay.getWidth() / 3f);

            if (show) {
                overlay.setVisibility(View.VISIBLE);
                overlay.setTranslationX(translation);
            }

            overlay.animate()
                    .translationX(show ? 0f : translation)
                    .alpha(alpha)
                    .setDuration(show ? ANIMATION_SLIDE_DURATION : ANIMATION_FADE_DURATION)
                    .setInterpolator(show ?
                            new DecelerateInterpolator() :
                            new AccelerateInterpolator())
                    .withEndAction(() -> {
                        if (!show) overlay.setVisibility(View.GONE);
                    })
                    .start();
        }
    }

    // Helper methods
    private String extractTitleFromUrl(String url) {
        try {
            String[] parts = url.split("/");
            String lastPart = parts[parts.length - 1];
            // Remove extension if present
            int lastDot = lastPart.lastIndexOf('.');
            if (lastDot > 0) {
                return lastPart.substring(0, lastDot);
            }
            return lastPart; // Return filename without extension or full last part
        } catch (Exception e) {
            Log.e(TAG, "Error extracting title from URL", e);
            return "Video"; // Default title
        }
    }

    private void hideSystemUI() {
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(
                getWindow(),
                getWindow().getDecorView()
        );
        insetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("playbackPosition", playbackPosition);
        outState.putInt("currentWindow", currentWindow);
        outState.putBoolean("playWhenReady", playWhenReady);
        outState.putString("currentVideoUrl", currentVideoUrl);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        playbackPosition = savedInstanceState.getLong("playbackPosition");
        currentWindow = savedInstanceState.getInt("currentWindow");
        playWhenReady = savedInstanceState.getBoolean("playWhenReady");
        currentVideoUrl = savedInstanceState.getString("currentVideoUrl");
    }

    // Player event listener
    private class PlayerEventListener implements Player.Listener {
        @Override
        public void onPlaybackStateChanged(int state) {
            if (state == Player.STATE_ENDED) {
                exoPlayer.seekTo(0);
                exoPlayer.setPlayWhenReady(false);
            }
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            Toast.makeText(PlayerActivity.this,
                    "Playback Error: " + error.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
    private class ScaleDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale_factor *= detector.getScaleFactor();
            scale_factor = Math.max(0.5f, Math.min(scale_factor, 6.0f));

            zoomLayout.setScaleX(scale_factor);
            zoomLayout.setScaleY(scale_factor);
            int percentage = (int) (scale_factor * 100);
            zoom_perc.setText(" " + percentage + "%");
            zoomContainer.setVisibility(View.VISIBLE);

            brt_text_container.setVisibility(View.GONE);
            vol_text_container.setVisibility(View.GONE);
            brt_progress_container.setVisibility(View.GONE);
            vol_progress_container.setVisibility(View.GONE);

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            zoomContainer.setVisibility(View.GONE);
            super.onScaleEnd(detector);
        }
    }
}