package com.two.channelmyanmar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.two.channelmyanmar.databinding.ActivitySearchBinding;
import com.two.channelmyanmar.fragment.search.FirstFragment;
import com.two.channelmyanmar.viewmodel.ApiViewModel;
import com.two.my_libs.ActivityTheme;
import com.two.my_libs.base.BaseThemeActivity;

/*
 * Created by Toewaioo on 4/8/25
 * Description: [Add class description here]
 */
public class SearchActivity extends BaseThemeActivity {
    private final String TAG = this.getClass().getSimpleName();
    ApiViewModel viewModel;
    ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(ApiViewModel.class);
        setUp();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
               FragmentManager manager = getSupportFragmentManager();
               if(manager.getBackStackEntryCount()>0){
                   manager.popBackStack();
               }else {
                   finish();
               }
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);

    }
//    public void onBackPressed() {
//        System.out.println(getSupportFragmentManager().getFragments().size());
//        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
//            if (fragment.isVisible() && hasBackStack(fragment)) {
//                if (popFragment(fragment))
//                    return;
//            }
//        }
//        overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
//        super.onBackPressed();
//    }
    public void setUp(){
        Fragment first=new FirstFragment();
        loadFrag(first);
    }
    public void  loadFrag(Fragment fragment){
        FragmentManager manager=getSupportFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.search_container,fragment).commit();
    }


    @Override
    protected void onStart() {
        super.onStart();
        viewModel.loadData(ApiViewModel.ApiType.SERIES);
    }

    private boolean hasBackStack(Fragment fragment) {
        FragmentManager childFragmentManager = fragment.getChildFragmentManager();
        return childFragmentManager.getBackStackEntryCount() > 0;
    }

    private boolean popFragment(Fragment fragment) {

        FragmentManager fragmentManager = fragment.getChildFragmentManager();
        for (Fragment childFragment : fragmentManager.getFragments()) {
            if (childFragment.isVisible()) {
                if (hasBackStack(childFragment)) {
                    return popFragment(childFragment);
                } else {

                    fragmentManager.popBackStack();
                    return true;



                }
            }
        }
        return false;
    }

    @Override
    protected void configTheme(ActivityTheme activityTheme) {
        activityTheme.setThemes(new int[]{R.style.AppTheme_Light, R.style.AppTheme_Black});
        activityTheme.setStatusBarColorAttrRes(R.attr.colorPrimary);
        activityTheme.setSupportMenuItemThemeEnable(true);
    }
}
