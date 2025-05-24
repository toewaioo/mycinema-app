package com.two.channelmyanmar;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationBarView;
import com.two.channelmyanmar.databinding.ActivityMainBinding;
import com.two.channelmyanmar.preference.PreferenceHelper;
import com.two.channelmyanmar.ui.movie.MovieFragment;
import com.two.channelmyanmar.ui.home.HomeFragment;
import com.two.channelmyanmar.ui.profile.ProfileFragment;
import com.two.channelmyanmar.ui.series.SeriesFragment;
import com.two.channelmyanmar.viewmodel.ApiViewModel;
import com.two.my_libs.ActivityTheme;
import com.two.my_libs.base.BaseThemeActivity;
import com.two.my_libs.views.taptarget.TapTarget;
import com.two.my_libs.views.taptarget.TapTargetSequence;

import java.util.List;


public class MainActivity extends BaseThemeActivity implements NavigationBarView.OnItemSelectedListener {
    private ActivityMainBinding binding;
    private Fragment activeFragment;
    private HomeFragment homeFragment;
    private MovieFragment movieFragment;
    private SeriesFragment seriesFragment;
    private ProfileFragment profileFragment;

    private PreferenceHelper helper;
    private ApiViewModel viewModel;
    private static final int REQ_STORAGE = 1001;

    private static final String KEY_ACTIVE_FRAGMENT = "activeFragmentTag";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadSuggest();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STORAGE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Storage permission granted.", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        helper = new PreferenceHelper(this);
        viewModel = new ViewModelProvider(this).get(ApiViewModel.class);


        // Restore fragments if available, otherwise create new instances
        if (savedInstanceState != null) {
            FragmentManager fm = getSupportFragmentManager();
            homeFragment = (HomeFragment) fm.findFragmentByTag("HomeFragment");
            movieFragment = (MovieFragment) fm.findFragmentByTag("MovieFragment");
            seriesFragment = (SeriesFragment) fm.findFragmentByTag("SeriesFragment");
            profileFragment = (ProfileFragment) fm.findFragmentByTag("ProfileFragment");

            String activeFragmentTag = savedInstanceState.getString(KEY_ACTIVE_FRAGMENT);
            if (activeFragmentTag != null) {
                activeFragment = getSupportFragmentManager().getFragment(savedInstanceState, activeFragmentTag);
            }

        } else {

            homeFragment = new HomeFragment();
            movieFragment = new MovieFragment();
            seriesFragment = new SeriesFragment();
            profileFragment = new ProfileFragment();
            loadFrag(homeFragment, "HomeFragment");
            activeFragment = homeFragment;

        }


        binding.bottomNavView.setOnItemSelectedListener(this);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        Drawable icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.search);
        if (helper.isHomeFirstTime()) {
            new TapTargetSequence(this)
                    .targets(
                            TapTarget.forView(binding.fab, "Click here to search movies and series.", "Enjoy.")
                                    .dimColor(android.R.color.white)
                                    .outerCircleColor(R.color.black_colorPrimary)
                                    .targetCircleColor(R.color.white)
                                    .icon(icon)
                                    .cancelable(false)
                                    .transparentTarget(true)
                                    .textColor(android.R.color.white),
                            TapTarget.forView(binding.teachFav, "Click here to search movies and series.", "Enjoy.")
                                    .dimColor(android.R.color.white)
                                    .outerCircleColor(R.color.black_colorPrimary)
                                    .targetCircleColor(R.color.white)
                                    .textColor(android.R.color.white))
                    .listener(new TapTargetSequence.Listener() {
                        // This listener will tell us when interesting(tm) events happen in regards
                        // to the sequence
                        @Override
                        public void onSequenceFinish() {
                            // Yay
                            binding.mainContainer.setVisibility(View.GONE);
                            helper.setHomeFirstTime(false);
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                            // Perform action for the current target
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                            // Boo
                            helper.setHomeFirstTime(false);
                        }
                    }).start();
        } else {
            binding.mainContainer.setVisibility(View.GONE);
        }
        handleDeepLink(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (!Intent.ACTION_VIEW.equals(intent.getAction())) {
            return;
        }

        Uri data = intent.getData();
        if (data == null) {
           // Toast.makeText(this, "No URI data", Toast.LENGTH_SHORT).show();
            return;
        }

        String scheme = data.getScheme();   // "https"
        String host   = data.getHost();     // "channelmyanmar.to"
        List<String> segments = data.getPathSegments();
        // e.g. for https://channelmyanmar.to/tvshows/1234
        // segments = ["tvshows", "1234"]

        if (segments.size() > 0 && "tvshows".equalsIgnoreCase(segments.get(0))) {
            String showId = segments.size() > 1 ? segments.get(1) : null;
            if (showId != null && !showId.isEmpty()) {
               String tvshows= "https://channelmyanmar.to/tvshows/" + showId;
               goDetail(tvshows);
            } else {
                Toast.makeText(this, "Invalid TV show URL", Toast.LENGTH_SHORT).show();
            }
        } else {
            // fallback: treat as a generic movie or default case
            goDetail(data.toString());
        }
    }
    private  void goDetail(String url){
        if(url==null)
            return;
        try{
            if(url.contains("channelmyanmar.to")){
                Intent i = new Intent(this,DetailActivity.class);
                i.putExtra("baseUrl",url);
                i.putExtra("name","");
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }else {
                Intent i = new Intent(this,DeepLink.class);
                i.putExtra("baseUrl",url);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }catch (Exception ignored){

        }

    }


    @Override
    protected void configTheme(ActivityTheme activityTheme) {
        activityTheme.setThemes(new int[]{R.style.AppTheme_Light, R.style.AppTheme_Black});
        activityTheme.setStatusBarColorAttrRes(R.attr.colorPrimary);
        activityTheme.setSupportMenuItemThemeEnable(true);
    }

    public void loadSuggest() {
        // To access context-dependent APIs
        viewModel.loadData(ApiViewModel.ApiType.SUGGESTIONS);
        viewModel.loadData(ApiViewModel.ApiType.SERIES);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (activeFragment != null) {
            outState.putString(KEY_ACTIVE_FRAGMENT, activeFragment.getTag());
            getSupportFragmentManager().putFragment(outState, activeFragment.getTag(), activeFragment);
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String activeFragmentTag = savedInstanceState.getString(KEY_ACTIVE_FRAGMENT);
        if (activeFragmentTag != null) {
            activeFragment = getSupportFragmentManager().getFragment(savedInstanceState, activeFragmentTag);

        }
    }

    public void loadFrag(Fragment fragment, String tag) {
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                R.anim.slide_in_left, R.anim.slide_out_right);

        if (activeFragment != null && activeFragment.getClass().equals(fragment.getClass())) {
            return;
        }

        Fragment existingFragment = fm.findFragmentByTag(tag);
        if (existingFragment == null) {
            // Add new fragment instance to container.
            ft.add(R.id.nav_host_fragment_activity_main, fragment, tag);
            existingFragment = fragment;
        }
        if (activeFragment != null) {
            ft.hide(activeFragment);
        }

        ft.show(existingFragment);
        ft.commit();

        activeFragment = existingFragment;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Fragment selectedFragment = null;

        if (itemId == R.id.nav_home) {
            if (homeFragment == null) {
                homeFragment = new HomeFragment();
            }
            selectedFragment = homeFragment;
        } else if (itemId == R.id.nav_movie) {
            if (movieFragment == null) {
                movieFragment = new MovieFragment();
            }
            selectedFragment = movieFragment;
        } else if (itemId == R.id.nav_series) {
            if (seriesFragment == null) {
                seriesFragment = new SeriesFragment();
            }
            selectedFragment = seriesFragment;
        } else if (itemId == R.id.nav_profile) {
            if (profileFragment == null) {
                profileFragment = new ProfileFragment();
            }
            selectedFragment = profileFragment;
        }

        if (selectedFragment != null) {
            loadFrag(selectedFragment, selectedFragment.getClass().getSimpleName());
            return true;
        }

        return false;
    }
}
