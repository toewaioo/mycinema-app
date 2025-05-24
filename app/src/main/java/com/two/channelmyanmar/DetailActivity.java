package com.two.channelmyanmar;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.two.channelmyanmar.databinding.ActivityDetailBinding;
import com.two.channelmyanmar.fragment.detail.DefaultFragment;
import com.two.channelmyanmar.fragment.detail.MovieDetail;
import com.two.channelmyanmar.fragment.detail.SerieDetail;
import com.two.channelmyanmar.preference.PreferenceHelper;
import com.two.my_libs.ActivityTheme;
import com.two.my_libs.base.BaseThemeActivity;
import com.two.my_libs.views.taptarget.TapTarget;
import com.two.my_libs.views.taptarget.TapTargetSequence;

public class DetailActivity extends BaseThemeActivity {
    private ActivityDetailBinding binding;
    private SerieDetail seriesDetailFragment;
    private MovieDetail movieDetailFragment;
    private DefaultFragment defaultFragment;
    private PreferenceHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        helper = new PreferenceHelper(getApplicationContext());

        // Always hide teaching overlay by default; we’ll show it later if needed
        binding.teachContainer.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            return; // Let the system restore fragments/state
        }

        Intent intent = getIntent();
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

        if (baseUrl == null || baseUrl.isEmpty()) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            // 18+/21+ filter
            if (!helper.showAdult() && name != null
                    && (name.contains("18+") || name.contains("21+"))) {

                defaultFragment = new DefaultFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, defaultFragment)
                        .commitAllowingStateLoss();

            } else {
                // Decide which detail fragment to load
                if (baseUrl.contains("tvshows")) {
                    seriesDetailFragment = SerieDetail.newInstance(baseUrl);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detail_container, seriesDetailFragment)
                            .commitAllowingStateLoss();
                } else {
                    movieDetailFragment = MovieDetail.newInstance(baseUrl);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detail_container, movieDetailFragment)
                            .commitAllowingStateLoss();
                }

                // First-time tutorial overlay
                if (helper.isDetailFirstTime()) {
                    binding.teachContainer.setVisibility(View.VISIBLE);

                    new TapTargetSequence(this)
                            .targets(
                                    TapTarget.forView(findViewById(R.id.teach_inject),
                                                    "Click here if loading state is paused at 100%.",
                                                    "Enjoy.")
                                            .dimColor(android.R.color.black)
                                            .outerCircleColor(com.two.my_libs.R.color.red)
                                            .targetCircleColor(R.color.white)
                                            .cancelable(false)
                                            .textColor(android.R.color.black),

                                    TapTarget.forView(findViewById(R.id.teach_heart),
                                                    "Click here to add to favorites.")
                                            .dimColor(android.R.color.black)
                                            .outerCircleColor(com.two.my_libs.R.color.red)
                                            .targetCircleColor(R.color.white)
                                            .textColor(android.R.color.black)
                            )
                            .listener(new TapTargetSequence.Listener() {
                                @Override
                                public void onSequenceFinish() {
                                    binding.teachContainer.setVisibility(View.GONE);
                                    helper.setDetailFirstTime(false);
                                }

                                @Override
                                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                                    // no-op
                                }

                                @Override
                                public void onSequenceCanceled(TapTarget lastTarget) {
                                    binding.teachContainer.setVisibility(View.GONE);
                                    helper.setDetailFirstTime(false);
                                }
                            })
                            .start();
                }
            }
        } catch (Exception e) {
            // Catch any unexpected error and inform the user
            Log.e("DetailActivity", "Error loading detail", e);
            Toast.makeText(this, "Something went wrong. Please try again later.", Toast.LENGTH_LONG).show();
            // Optionally you could load a default error fragment here instead of finishing
            finish();
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if (movieDetailFragment != null) {
            getSupportFragmentManager().putFragment(outState, "movieDetail", movieDetailFragment);
        }
        if (seriesDetailFragment != null) {
            getSupportFragmentManager().putFragment(outState, "seriesDetail", seriesDetailFragment);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        movieDetailFragment = (MovieDetail) getSupportFragmentManager().getFragment(savedInstanceState, "movieDetail");
        seriesDetailFragment = (SerieDetail) getSupportFragmentManager().getFragment(savedInstanceState, "seriesDetail");
    }

    @Override
    protected void configTheme(ActivityTheme activityTheme) {
        activityTheme.setThemes(new int[]{R.style.AppTheme_Light, R.style.AppTheme_Black});
        activityTheme.setStatusBarColorAttrRes(R.attr.colorPrimary);
        activityTheme.setSupportMenuItemThemeEnable(true);
    }
}

