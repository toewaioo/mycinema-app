package com.two.channelmyanmar.view;

/*
 * Created by Toewaioo on 4/5/25
 * Description: [Add class description here]
 */
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.two.channelmyanmar.R;

import java.util.Formatter;
import java.util.Locale;

@UnstableApi
public class CustomPlayerVieww extends PlayerView implements GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {

    private static final int DEFAULT_ANIMATION_DURATION = 300;

    private FrameLayout controllerLayout;
    private ImageView playPauseButton;
    private SeekBar progressBar;
    private TextView currentTimeTextView;
    private TextView durationTextView;
    private ImageView fullscreenButton;
    private LinearLayout bottomControls;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private boolean isControllerVisible = true;
    private ExoPlayer player;
    private final StringBuilder formatBuilder;
    private final Formatter formatter;

    public CustomPlayerVieww(@NonNull Context context) {
        this(context, null);
    }

    public CustomPlayerVieww(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomPlayerVieww(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.custom_player_view, this);
        initViews();
        gestureDetector = new GestureDetector(context, this);
        scaleGestureDetector = new ScaleGestureDetector(context, this);
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
    }

    private void initViews() {
        //FrameLayout exoContentView = findViewById(R.id.exo_content_frame);
        controllerLayout = findViewById(R.id.controller_layout);
        playPauseButton = controllerLayout.findViewById(R.id.play_pause_button);
        progressBar = controllerLayout.findViewById(R.id.progress_bar);
        currentTimeTextView = controllerLayout.findViewById(R.id.current_time);
        durationTextView = controllerLayout.findViewById(R.id.duration);
        fullscreenButton = controllerLayout.findViewById(R.id.fullscreen_button);
        bottomControls = controllerLayout.findViewById(R.id.bottom_controls); // Assuming you add an ID to the LinearLayout in the layout

        playPauseButton.setOnClickListener(v -> {
            if (player != null) {
                if (player.isPlaying()) {
                    player.pause();
                    animatePlayPauseButton(R.drawable.play);
                } else {
                    player.play();
                    animatePlayPauseButton(R.drawable.play);
                }
            }
        });

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (player != null && fromUser) {
                    long duration = player.getDuration();
                    if (duration != C.TIME_UNSET) {
                        long newPosition = (duration * progress) / 1000;
                        player.seekTo(newPosition);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        fullscreenButton.setOnClickListener(v -> {
            // Implement fullscreen toggle logic here
            // This will typically involve changing the activity's orientation or using a custom fullscreen implementation
        });
    }

    private void animatePlayPauseButton(int drawableId) {
        playPauseButton.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(200)
                .withEndAction(() -> {
                    playPauseButton.setImageResource(drawableId);
                    playPauseButton.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                })
                .start();
    }

    @Override
    public void setPlayer(@Nullable Player player) {
        super.setPlayer(player);
        this.player = (ExoPlayer) player;
        if (player != null) {
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    updatePlayPauseButton();
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    updatePlayPauseButton();
                }

                public void onPositionChanged(long position) {
                    updateProgress();
                }

                @Override
                public void onMediaItemTransition(@Nullable androidx.media3.common.MediaItem mediaItem, int reason) {
                    updateDuration();
                    updateProgress();
                }
            });
            updatePlayPauseButton();
            updateDuration();
            updateProgress();
        }
    }

    private void updatePlayPauseButton() {
        if (player != null) {
            boolean isPlaying = player.isPlaying();
            playPauseButton.setImageResource(isPlaying ? R.drawable.play : R.drawable.play);
        } else {
            playPauseButton.setImageResource(R.drawable.play);
        }
    }

    private void updateProgress() {
        if (player != null) {
            long currentPosition = player.getCurrentPosition();
            long duration = player.getDuration();
            if (duration != C.TIME_UNSET) {
                progressBar.setMax(1000);
                progressBar.setProgress((int) ((currentPosition * 1000) / duration));
                currentTimeTextView.setText(formatTime(currentPosition)); // Using custom formatTime
            } else {
                progressBar.setProgress(0);
                currentTimeTextView.setText(formatTime(0)); // Using custom formatTime
            }
        } else {
            progressBar.setProgress(0);
            currentTimeTextView.setText(formatTime(0)); // Using custom formatTime
        }
    }

    private void updateDuration() {
        if (player != null) {
            long duration = player.getDuration();
            durationTextView.setText(duration == C.TIME_UNSET ? "--:--" : formatTime(duration));
            progressBar.setVisibility(duration == C.TIME_UNSET ? View.INVISIBLE : View.VISIBLE);
        } else {
            durationTextView.setText("--:--");
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private String formatTime(long ms) {
        long totalSeconds = (ms + 999) / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (ms >= 3600000) {
            long hours = minutes / 60;
            minutes %= 60;
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    private void toggleControllerVisibility() {
        if (isControllerVisible) {
            hideController();
        } else {
            showController();
        }
        isControllerVisible = !isControllerVisible;
    }

    public void showController() {
        controllerLayout.setVisibility(View.VISIBLE);
        controllerLayout.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(DEFAULT_ANIMATION_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        controllerLayout.setVisibility(View.VISIBLE);
                    }
                })
                .start();
    }

    public void hideController() {
        controllerLayout.animate()
                .translationY(controllerLayout.getHeight())
                .alpha(0f)
                .setDuration(DEFAULT_ANIMATION_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        controllerLayout.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = scaleGestureDetector.onTouchEvent(event);
        if (!scaleGestureDetector.isInProgress()) {
            handled = gestureDetector.onTouchEvent(event) || handled;
        }
        return handled || super.onTouchEvent(event);
    }

    // GestureDetector.OnGestureListener implementation
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // Do nothing
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        toggleControllerVisibility();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // Do nothing
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    // ScaleGestureDetector.OnScaleGestureListener implementation
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        // Implement scaling logic if needed
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        // Do nothing
    }
}
