package com.two.channelmyanmar.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment Below is an example using a RelativeLayout. In this layout, a custom bottom navigation is implemented as a FrameLayout anchored to the bottom, and the main content (another FrameLayout) is set to appear above it:Below is an example using a RelativeLayout. In this layout, a custom bottom navigation is implemented as a FrameLayout anchored to the bottom, and the main content (another FrameLayout) is set to appear above it:\nThis is home fragment Below is an example using a RelativeLayout. In this layout, a custom bottom navigation is implemented as a FrameLayout anchored to the bottom, and the main content (another FrameLayout) is set to appear above it:Below is an example using a RelativeLayout. In this layout, a custom bottom navigation is implemented as a FrameLayout anchored to the bottom, and the main content (another FrameLayout) is set to appear above it:\nThis is home fragment Below is an example using a RelativeLayout. In this layout, a custom bottom navigation is implemented as a FrameLayout anchored to the bottom, and the main content (another FrameLayout) is set to appear above it:Below is an example using a RelativeLayout. In this layout, a custom bottom navigation is implemented as a FrameLayout anchored to the bottom, and the main content (another FrameLayout) is set to appear above it:\n*");
    }

    public LiveData<String> getText() {
        return mText;
    }
}