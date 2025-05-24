package com.two.channelmyanmar.preference;

import android.content.Context;

public class PreferenceHelper {
    private PreferenceManager manager;

    public PreferenceHelper(Context context) {
        this.manager = new PreferenceManager(context);
    }

    /*
     Checking for calling api is need to use third party api
     */
    private static final String need_api = "need_api";

    public void needApi(boolean b) {
        manager.save(need_api, b);
    }

    public boolean needApi() {
        return manager.getBoolean(need_api);
    }

    /*
    Set api key and get api key for fetch data from third party api
     */
    private static final String api_key = "api_key";

    public void setApiKey(String key) {
        manager.save(api_key, key);
    }

    public String getApiKey() {
        return manager.getString(api_key,"https://php-on-vercel-ashen.vercel.app");
    }
    /*
    Set use cross platform
     */
    /*
     Checking for calling api is need to use third party api
     */
    private static final String use_cross_platform = "use_cross_platform";

    public void useCrossPlatform(boolean b) {
        manager.save(use_cross_platform, b);
    }

    public boolean useCrossPlatform() {
        return manager.getBoolean(use_cross_platform,false);
    }

//    Show injection js code
    public static final String show_injecting = "show_injecting";
    public void setInjection(boolean b){
        manager.save(show_injecting,b);

    }
    public boolean showInject(){
        return manager.getBoolean(show_injecting,false);

    }
    //    Show adult contents
    public static final String show_adult_contents = "show_adult_contents";
    public void setAdult(boolean b){
        manager.save(show_adult_contents,b);
    }
    public boolean showAdult() {
        return manager.getBoolean(show_adult_contents, false);
    }
    //
    private static final String KEY_LAST_CHECK = "key_last_check";
    private static final String KEY_API_KEY     = "key_api";


    public  long getLastUpdateCheckTime() {
        return manager.getLong(KEY_LAST_CHECK);
    }
   public void setLastUpdateCheckTime(long ts) {
        manager.save(KEY_LAST_CHECK, ts);
    }
    //    Show firstTime contents
    public static final String show_first_contents = "show_first_contents";
    public void setFirstTime(boolean b){
        manager.save(show_first_contents,b);
    }
    public boolean isFirstTime() {
        return manager.getBoolean(show_first_contents, true);
    }
    //    Show home firstTime contents
    public static final String show_home_first_contents = "show_home_first_contents";
    public void setHomeFirstTime(boolean b){
        manager.save(show_home_first_contents,b);
    }
    public boolean isHomeFirstTime() {
        return manager.getBoolean(show_home_first_contents, true);
    }
    //    Show home firstTime contents
    public static final String show_detail_first_contents = "show_detail_first_contents";
    public void setDetailFirstTime(boolean b){
        manager.save(show_detail_first_contents,b);
    }
    public boolean isDetailFirstTime() {
        return manager.getBoolean(show_detail_first_contents, true);
    }
    //    use system downloader
    public static final String use_system_downloader = "use_system_downloader";
    public void setSystemDownload(boolean b){
        manager.save(use_system_downloader,b);
    }
    public boolean useSystemDownload() {
        return manager.getBoolean(use_system_downloader, false);
    }
}
