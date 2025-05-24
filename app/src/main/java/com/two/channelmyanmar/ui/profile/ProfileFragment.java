package com.two.channelmyanmar.ui.profile;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.color.MaterialColors;
import com.two.channelmyanmar.DeepLink;
import com.two.channelmyanmar.R;
import com.two.channelmyanmar.activity.BrowserActivity;
import com.two.channelmyanmar.databinding.FragmentProfileBinding;
import com.two.channelmyanmar.preference.PreferenceHelper;
import com.two.my_libs.DarkMode;
import com.two.my_libs.MultiTheme;
import com.two.my_libs.base.BaseThemeFragment;

public class ProfileFragment extends BaseThemeFragment {
    FragmentProfileBinding binding;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView profileImage;
    private Toolbar toolbar;
    private PreferenceHelper helper;
    private static final int REQ_STORAGE = 1001;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater,container,false);
        helper = new PreferenceHelper(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        profileImage = view.findViewById(R.id.profile_image);
        toolbar = view.findViewById(R.id.toolbar);

       // setupToolbar();
        setupProfileImage();
        binding.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // Log.d("Appbar", String.valueOf(verticalOffset));
                if (verticalOffset == 0) {
                    binding.toolbar.setVisibility(View.INVISIBLE);
                } else {
                    binding.toolbar.setVisibility(View.VISIBLE);
                }
            }
        });
        binding.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isToolbarVisible = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // Determine the total scroll range once.
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                // When fully collapsed, show the toolbar.
                if (scrollRange + verticalOffset == 0) {
                    if (!isToolbarVisible) {

                        isToolbarVisible = true;

                    }
                } else {
                    // When expanded, hide the toolbar.
                    if (isToolbarVisible) {
                        binding.toolbar.setVisibility(View.INVISIBLE);
                        isToolbarVisible = false;


                    }
                }
            }
        });
        try {
            assert binding.directLink != null;
            binding.directLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(requireActivity(), DeepLink.class);
                    i.putExtra("baseUrl", "");
                    startActivity(i);

                }
            });
        }catch (Exception ignored){

        }
        try {
            int appTheme = MultiTheme.getAppTheme();
            boolean isDarkmode = appTheme != 0;
            assert binding.switchWidgetDarkMode != null;
            binding.switchWidgetDarkMode.setChecked(isDarkmode);
            binding.switchWidgetDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    toggleDarkMode(isChecked);
                }
            });
        }catch (Exception e){

        }

        boolean isInjectmode = helper.showInject();
        assert binding.switchWidgetInject != null;
        binding.switchWidgetInject.setChecked(isInjectmode);
        binding.switchWidgetInject.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                helper.setInjection(isChecked);
            }
        });
        boolean isShowAdult = helper.showAdult();
        assert binding.switchWidgetAdult != null;
        binding.switchWidgetAdult.setChecked(isShowAdult);
        binding.switchWidgetAdult.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                helper.setAdult(isChecked);
            }
        });
        boolean useSystem = helper.useSystemDownload();
        assert binding.switchWidgetDownload != null;
        binding.switchWidgetDownload.setChecked(useSystem);
        binding.switchWidgetDownload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(requireActivity(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.e("TAG", "WRITE_EXTERNAL_STORAGE permission required");
                        // TODO: request permission from UI and retry on grant
                        ActivityCompat.requestPermissions(requireActivity(),
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQ_STORAGE);
                        return;
                    }else {
                        helper.setSystemDownload(isChecked);
                    }
                }else {
                    helper.setSystemDownload(isChecked);
                }

            }
        });
        binding.helpCenterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/mycinema_app_chat"));
                intent.setPackage("org.telegram.messenger");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/mycinema_app_chat")));
                }
            }
        });
        binding.helpPrivacyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(getContext(), BrowserActivity.class);
                i.putExtra("baseUrl","https://mycinema-sage.vercel.app/privacy.html");
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });
        binding.mfooterYoutube.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View p1) {
                Intent i=new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("vnd.youtube://channel/UCKsgXd054rmAs60qmUhE27w"));
                startActivity(i);
            }


        });
        binding.mfooterTelegram.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View p1) {
                Intent i=getTelegramInt(getContext());
                startActivity(i);
            }


        });
        binding.mfooterFacebook.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View p1) {
                Intent i=new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getFacebookPageURL(getContext())));
                startActivity(i);
            }


        });
    }



    Intent getTelegramInt(Context context) {
        Intent intent;
        try {
            try { // check for telegram app
                context.getPackageManager().getPackageInfo("org.telegram.messenger", 0);
            } catch (PackageManager.NameNotFoundException e) {
                // check for telegram X app
                context.getPackageManager().getPackageInfo("org.thunderdog.challegram", 0);
            }
            // set app Uri
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=mycinema_app"));
        } catch (PackageManager.NameNotFoundException e) {
            // set browser URI
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://t.me/mycinema_app"));
        }
        return intent;
    }
    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + "https://www.facebook.com/liarkod/";
            } else { //older versions of fb app
                return "fb://page/" +"100069516109237";
            }
        } catch (PackageManager.NameNotFoundException e) {
            return "https://www.facebook.com/liarkod/"; //normal web url
        }
    }


    private void setupToolbar() {
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        collapsingToolbar.setTitle("John Doe");
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupProfileImage() {
        // Load profile image using Glide or your preferred image loading library
        Glide.with(this)
                .load("https://iili.io/31DutYN.th.png")
                .circleCrop()
                .into(profileImage);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            setupProfileImage();

           // requireActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commitAllowingStateLoss();
        }
    }

    public void updaeImage(){
        int iconColor = MaterialColors.getColor(requireContext(),R.attr.title_text_color, Color.BLACK);
        profileImage.setColorFilter(iconColor);
    }


    private void setupItem(int itemId, int iconRes, String title, String subtitle) {
        View itemView = requireView().findViewById(itemId);
        ImageView icon = itemView.findViewById(R.id.icon);
        TextView titleView = itemView.findViewById(R.id.title);

        icon.setImageResource(iconRes);
        titleView.setText(title);

        if (subtitle != null) {
            titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            titleView.setText(String.format("%s\n%s", title, subtitle));
        }
    }


    private void toggleDarkMode(boolean enabled) {
        // Implement dark mode toggle logic
        if(enabled)
            MultiTheme.setAppTheme(1);
        else
            MultiTheme.setAppTheme(0);

    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    // Handle logout
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
