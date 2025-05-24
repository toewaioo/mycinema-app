package com.two.channelmyanmar.api;

public interface ExtractorListener {
    void onSuccessMegaUp(String directLink);
    void onSuccessYoteShin(String directLink,boolean isDirect);
    void onFailed(String message);
    void loading(int progress);


}
