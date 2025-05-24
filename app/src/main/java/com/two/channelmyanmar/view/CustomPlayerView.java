package com.two.channelmyanmar.view;

/*
 * Created by Toewaioo on 4/5/25
 * Description: [Add class description here]
 */
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

import com.two.channelmyanmar.R;

public class CustomPlayerView extends PlayerView implements
        GestureDetector.OnGestureListener,
        ScaleGestureDetector.OnScaleGestureListener {

    private static final int CONTROLLER_TIMEOUT = 3000;
    private static final float SWIPE_THRESHOLD = 100;
    private static final float SWIPE_VELOCITY_THRESHOLD = 100;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private boolean controllerVisible = true;
    private float initialBrightness;
    private int initialVolume;
    private WindowManager.LayoutParams windowParams;
    private AudioManager audioManager;

    // Animation views
    private View controllerContainer;
    private ImageView playPauseIcon;
    private TextView seekTimeText;
    private FrameLayout brightnessContainer;
    private ProgressBar brightnessProgress;
    private FrameLayout volumeContainer;
    private ProgressBar volumeProgress;
    private Activity hostActivity;
    public CustomPlayerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CustomPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("Context must be an Activity");
        }
        hostActivity = (Activity) context;
        gestureDetector = new GestureDetector(context, this);
        scaleGestureDetector = new ScaleGestureDetector(context, this);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        windowParams = hostActivity.getWindow().getAttributes();

        // Inflate overlay views
        LayoutInflater.from(context).inflate(R.layout.player_overlays, this);
        controllerContainer = findViewById(R.id.controller_container);
        playPauseIcon = findViewById(R.id.play_pause_icon);
        seekTimeText = findViewById(R.id.seck_time_text);
        brightnessContainer = findViewById(R.id.brightness_container);
        brightnessProgress = findViewById(R.id.brightness_progress);
        volumeContainer = findViewById(R.id.volume_container);
        volumeProgress = findViewById(R.id.volume_progress);

        setupInitialStates();
        resetControllerTimeout();
    }

    private void setupInitialStates() {
        controllerContainer.setAlpha(1f);
        playPauseIcon.setVisibility(GONE);
        seekTimeText.setVisibility(GONE);
        brightnessContainer.setVisibility(GONE);
        volumeContainer.setVisibility(GONE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    // Gesture implementations
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        toggleController();
        return true;
    }



    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();

        if (Math.abs(diffX) > Math.abs(diffY)) {
            handleHorizontalSwipe(diffX, velocityX);
        } else {
            handleVerticalSwipe(diffY, velocityY, e1.getX());
        }
        return true;
    }

    private void handleHorizontalSwipe(float diffX, float velocityX) {
        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (diffX > 0) {
                seekForward();
            } else {
                seekBackward();
            }
        }
    }

    private void handleVerticalSwipe(float diffY, float velocityY, float xPosition) {
        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
            if (diffY > 0) {
                decreaseVolumeOrBrightness(xPosition);
            } else {
                increaseVolumeOrBrightness(xPosition);
            }
        }
    }

    // Scale gesture implementations
    @OptIn(markerClass = UnstableApi.class)
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        setResizeMode(scaleFactor > 1 ? RESIZE_MODE_ZOOM : RESIZE_MODE_FIT);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {}

    // Controller visibility animations
    private void toggleController() {
        if (controllerVisible) {
            animateHideController();
        } else {
            animateShowController();
        }
        controllerVisible = !controllerVisible;
        resetControllerTimeout();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void animateShowController() {
        controllerContainer.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(1000)

                .withStartAction(() -> controllerContainer.setVisibility(VISIBLE))
                .start();
        showController();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void animateHideController() {
        controllerContainer.animate()
                .alpha(0f)
                .translationY(controllerContainer.getHeight())
                .setDuration(300)
                .withEndAction(() -> controllerContainer.setVisibility(GONE))
                .start();
        hideController();
    }

    // Play/Pause animations
    private void togglePlayPause() {
        Player player = getPlayer();
        if (player != null) {
            if (player.getPlayWhenReady()) {
                player.pause();
            } else {
                player.play();
            }
        }
    }

    private void animatePlayPause() {
        playPauseIcon.setImageResource(
                getPlayer().getPlayWhenReady() ? R.drawable.play : R.drawable.play
        );

        playPauseIcon.setScaleX(0f);
        playPauseIcon.setScaleY(0f);
        playPauseIcon.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .withEndAction(() -> playPauseIcon.animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .setStartDelay(500)
                        .setDuration(200)
                        .start())
                .start();
    }

    // Seek animations
    private void seekForward() {
        Player player = getPlayer();
        if (player != null) {
            player.seekTo(player.getCurrentPosition() + 10000);
            animateSeekText("+10s");
        }
    }

    private void seekBackward() {
        Player player = getPlayer();
        if (player != null) {
            player.seekTo(player.getCurrentPosition() - 10000);
            animateSeekText("-10s");
        }
    }

    private void animateSeekText(String text) {
        seekTimeText.setText(text);
        seekTimeText.setAlpha(1f);
        seekTimeText.setVisibility(VISIBLE);
        seekTimeText.animate()
                .alpha(0f)
                .setStartDelay(1000)
                .setDuration(500)
                .withEndAction(() -> seekTimeText.setVisibility(GONE))
                .start();
    }

    // Volume/Brightness controls
    private void increaseVolumeOrBrightness(float x) {
        if (x < getWidth() / 2) {
            adjustBrightness(0.1f);
        } else {
            adjustVolume(1);
        }
    }

    private void decreaseVolumeOrBrightness(float x) {
        if (x < getWidth() / 2) {
            adjustBrightness(-0.1f);
        } else {
            adjustVolume(-1);
        }
    }

    private void adjustVolume(int direction) {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int newVolume = Math.max(0, Math.min(maxVolume, currentVolume + direction));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        animateVolumeControl((int) ((float) newVolume / maxVolume * 100));
    }

    private void adjustBrightness(float delta) {
        float newBrightness = windowParams.screenBrightness + delta;
        newBrightness = Math.max(0.01f, Math.min(1.0f, newBrightness));

        try {
            windowParams.screenBrightness = newBrightness;
            hostActivity.getWindow().setAttributes(windowParams);
            animateBrightnessControl((int) (newBrightness * 100));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateBrightnessControl(int progress) {
        brightnessProgress.setProgress(progress);
        if (brightnessContainer.getVisibility() != VISIBLE) {
            brightnessContainer.setAlpha(0f);
            brightnessContainer.setVisibility(VISIBLE);
            brightnessContainer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
        resetControlTimeout(brightnessContainer);
    }

    private void animateVolumeControl(int progress) {
        volumeProgress.setProgress(progress);
        if (volumeContainer.getVisibility() != VISIBLE) {
            volumeContainer.setAlpha(0f);
            volumeContainer.setVisibility(VISIBLE);
            volumeContainer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
        resetControlTimeout(volumeContainer);
    }

    private void resetControlTimeout(View view) {
        view.removeCallbacks(hideControlRunnable);
        view.postDelayed(hideControlRunnable, 2000);
    }

    private final Runnable hideControlRunnable = new Runnable() {
        @Override
        public void run() {
            hideViewWithAnimation(brightnessContainer);
            hideViewWithAnimation(volumeContainer);
        }
    };

    private void hideViewWithAnimation(View view) {
        if (view.getVisibility() == VISIBLE) {
            view.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> view.setVisibility(GONE))
                    .start();
        }
    }

    private void resetControllerTimeout() {
        removeCallbacks(hideControllerRunnable);
        postDelayed(hideControllerRunnable, CONTROLLER_TIMEOUT);
    }

    private final Runnable hideControllerRunnable = this::animateHideController;

    // Other required gesture methods
    @Override public boolean onDown(@NonNull MotionEvent e) { return true; }
    @Override public void onShowPress(@NonNull MotionEvent e) {}
    @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
    @Override public void onLongPress(@NonNull MotionEvent e) {}
}