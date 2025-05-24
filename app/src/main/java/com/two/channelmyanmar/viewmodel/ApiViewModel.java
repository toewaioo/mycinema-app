package com.two.channelmyanmar.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.two.channelmyanmar.api.CmHomeApi;
import com.two.channelmyanmar.api.CmSearchApi;
import com.two.channelmyanmar.api.SuggestApi;
import com.two.channelmyanmar.model.MovieModel;
import com.two.channelmyanmar.preference.PreferenceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiViewModel extends AndroidViewModel {
    private final Map<ApiType, MutableLiveData<?>> liveDataMap = new HashMap<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final ExecutorService service = Executors.newFixedThreadPool(4);

    private final Set<ApiGroup> loadedGroups = new HashSet<>();
    private final PreferenceHelper preferenceHelper;

    public ApiViewModel(@NonNull Application application) {
        super(application);
        this.preferenceHelper = new PreferenceHelper(application);
        initializeLiveData();
    }

    private void initializeLiveData() {
        liveDataMap.put(ApiType.SUGGESTIONS, new MutableLiveData<>());
        liveDataMap.put(ApiType.NEW_RELEASE, new MutableLiveData<>());
        liveDataMap.put(ApiType.MOVIES, new MutableLiveData<>());
        liveDataMap.put(ApiType.SERIES, new MutableLiveData<>());
        liveDataMap.put(ApiType.GENRES, new MutableLiveData<>());
    }

    public LiveData<?> getLiveData(ApiType type) {
        return liveDataMap.get(type);
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void refreshData(ApiType type) {

        switch (type) {
            case SUGGESTIONS:
                executeApiCall(new SuggestApi(), ApiType.SUGGESTIONS);
                break;
            case NEW_RELEASE:
            case MOVIES:
            case SERIES:
                executeApiCall(new CmHomeApi(preferenceHelper), type);
                break;
        }
    }
    public void loadMore(ApiType type,String baseUrl){
        if (Objects.requireNonNull(type) == ApiType.MORE_SERIES) {
            executeApiCall(new CmSearchApi(preferenceHelper, baseUrl), ApiType.MORE_SERIES);
        }else if( type == ApiType.MORE_MOVIES){
            executeApiCall(new CmSearchApi(preferenceHelper, baseUrl), ApiType.MORE_MOVIES);
        }
    }

    public void loadData(ApiType type) {
        if (loadedGroups.contains(type.getGroup())) {
            return; // Data already loaded
        }
        switch (type) {
            case SUGGESTIONS:
                executeApiCall(new SuggestApi(), ApiType.SUGGESTIONS);
                break;
            case NEW_RELEASE:
            case MOVIES:
            case SERIES:
                executeApiCall(new CmHomeApi(preferenceHelper), type);
                break;
        }
    }

    private void executeApiCall(BaseApi api, ApiType type) {
        service.execute(() -> {
            try {

                api.addListener(new BaseApi.Callback() {
                    @Override
                    public void onSuccess(Object result) {
                        Log.d("ApiViewModel",result.toString());
                        handleSuccess(type, result);
                    }

                    @Override
                    public void onError(String error) {
                        handleError(type.getGroup(), error);
                    }
                });
                api.run();
            } catch (Exception e) {
                handleError(type.getGroup(), e.getMessage());
            }
        });
    }

    private void handleSuccess(ApiType type, Object result) {
        ApiGroup group = type.getGroup();

        if (group == ApiGroup.CM_HOME && result instanceof CmHomeApi.CombinedResult) {
            CmHomeApi.CombinedResult combined = (CmHomeApi.CombinedResult) result;
            updateLiveData(ApiType.NEW_RELEASE, combined.newReleases);
            updateLiveData(ApiType.MOVIES, combined.movies);
            updateLiveData(ApiType.SERIES, combined.series);
            updateLiveData(ApiType.GENRES,combined.genres);
        }else if (group == ApiGroup.PAGINATION){
            appendLiveData(type,result);
        }else{
            updateLiveData(type, result);
        }

        loadedGroups.add(group);
    }

    private void appendLiveData(ApiType type, Object data) {
        MutableLiveData<List<?>> liveData = (MutableLiveData<List<?>>) liveDataMap.get(type == ApiType.MORE_SERIES ? ApiType.SERIES : ApiType.MOVIES);
        if (liveData != null && data instanceof List) {
            List<Object> currentData = new ArrayList<>();
            if (liveData.getValue() != null) {
                currentData.addAll(liveData.getValue());
            }
            currentData.addAll((List<?>) data);
            liveData.postValue(currentData);
        }
    }
//    private void addLiveData(ApiType type, Object data) {
//        MutableLiveData liveData = liveDataMap.get(type);
//        if (liveData != null) {
//             currentData= liveData.getValue();
//            if (currentData == null){
//                currentData = new ArrayList<>();
//            }
//            currentData.addAll(data);
//            liveData.postValue(data);
//        }
//    }

    private void updateLiveData(ApiType type, Object data) {
        MutableLiveData liveData = liveDataMap.get(type);
        if (liveData != null) {
            liveData.postValue(data);
        }
    }

    private void handleError(ApiGroup group, String error) {
        errorLiveData.postValue(error);
        loadedGroups.remove(group); // Allow retry on error
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        service.shutdown();
    }

    public interface BaseApi extends Runnable {
        interface Callback {
            void onSuccess(Object result);
            void onError(String error);
        }
        void addListener(Callback callback);
    }

    public enum ApiType {
        SUGGESTIONS(ApiGroup.SUGGESTIONS),
        NEW_RELEASE(ApiGroup.CM_HOME),
        MOVIES(ApiGroup.CM_HOME),
        SERIES(ApiGroup.CM_HOME),
        GENRES(ApiGroup.CM_HOME),
        MORE_SERIES(ApiGroup.PAGINATION),
        MORE_MOVIES(ApiGroup.PAGINATION);

        private final ApiGroup group;

        ApiType(ApiGroup group) {
            this.group = group;
        }

        public ApiGroup getGroup() {
            return group;
        }
    }

    private enum ApiGroup {
        SUGGESTIONS, CM_HOME, PAGINATION
    }

}


//public class ApiViewModel extends AndroidViewModel {
//    private final Map<ApiType, MutableLiveData<?>> liveDataMap = new HashMap<>();
//    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
//    private final ExecutorService service = Executors.newFixedThreadPool(4);
//    private final ApiCaller apiCaller;
//    private final PreferenceHelper preferenceHelper;
//
//    public ApiViewModel(@NonNull Application application) {
//        super(application);
//        this.preferenceHelper = new PreferenceHelper(application);
//        this.apiCaller = new ApiCaller();
//        initializeLiveData();
//    }
//
//    private void initializeLiveData() {
//        liveDataMap.put(ApiType.SUGGESTIONS, new MutableLiveData<>());
//        liveDataMap.put(ApiType.NEW_RELEASE, new MutableLiveData<>());
//        liveDataMap.put(ApiType.MOVIES, new MutableLiveData<>());
//        liveDataMap.put(ApiType.SERIES, new MutableLiveData<>());
//    }
//
//    public LiveData<?> getLiveData(ApiType type) {
//        return liveDataMap.get(type);
//    }
//
//    public LiveData<String> getErrorLiveData() {
//        return errorLiveData;
//    }
//
//    public void loadData(ApiType type) {
//        switch (type) {
//            case SUGGESTIONS:
//                apiCaller.executeApiCall(
//                        new SuggestApi(),
//                        ApiType.SUGGESTIONS,
//                        this::handleSuccess,
//                        this::handleError
//                );
//                break;
//            case NEW_RELEASE:
//            case MOVIES:
//            case SERIES:
//                apiCaller.executeApiCall(
//                        new CmHomeApi(preferenceHelper),  // Pass dependencies here
//                        type,
//                        this::handleSuccess,
//                        this::handleError
//                );
//                break;
//        }
//    }
//    private void handleSuccess(ApiType type, Object result) {
//        MutableLiveData liveData = liveDataMap.get(type);
//        if (liveData != null) {
//            liveData.postValue(result);
//        }
//    }
//
//    private void handleError(String errorMessage) {
//        errorLiveData.postValue(errorMessage);
//    }
//
//    @Override
//    protected void onCleared() {
//        super.onCleared();
//        service.shutdown();
//    }
//    // Helper class to handle API calls
//    private class ApiCaller {
//        <T> void executeApiCall(Runnable apiTask, ApiType type,
//                                BiConsumer<ApiType, Object> successCallback,
//                                Consumer<String> errorCallback) {
//            service.execute(() -> {
//                try {
//                    apiTask.run();
//
//                    // Add generic listener
//                    if (apiTask instanceof BaseApi) {
//                        ((BaseApi) apiTask).addListener(new BaseApi.Callback() {
//                            @Override
//                            public void onSuccess(Object result) {
//                                successCallback.accept(type, result);
//                            }
//
//                            @Override
//                            public void onError(String error) {
//                                errorCallback.accept(error);
//                            }
//                        });
//                    }
//                } catch (Exception e) {
//                    errorCallback.accept(e.getMessage());
//                }
//            });
//        }
//    }
//
//    // Base API interface
//    public interface BaseApi extends Runnable {
//        interface Callback {
//            void onSuccess(Object result);
//            void onError(String error);
//        }
//
//        void addListener(Callback callback);
//    }
//
//    // API type enumeration
//    public enum ApiType {
//        SUGGESTIONS,
//        NEW_RELEASE,
//        MOVIES,
//        SERIES
//        // Add more types here for new APIs
//    }
//    private MutableLiveData<List<SuggestModel>> myData1;
//    private MutableLiveData<String> error1;
//    private MutableLiveData<List<MovieModel>> myData2;
//    private MutableLiveData<List<MovieModel>> myData3;
//    private MutableLiveData<List<MovieModel>> myData4;
//
//    private MutableLiveData<String> error2;
//
//    private ExecutorService service;
//    private Context context;
//
//    public ApiViewModel(Context context) {
//        this.service = Executors.newFixedThreadPool(4);
//        this.context = context;
//    }
//
//    // for api 1
//    public LiveData<List<SuggestModel>> getMyData1() {
//        if (myData1 == null) {
//            myData1 = new MutableLiveData<>();
//            loadData1();
//        }
//        return myData1;
//    }
//    private void loadData1() {
//        service.execute(new SuggestApi());
//
//        SuggestApi.addListener(new SuggestApi.CallBack() {
//            @Override
//            public void onFail(String s) {
//                error1.setValue(s);
//            }
//
//            @Override
//            public void onSuccess(ArrayList<SuggestModel> result) {
//                myData1.setValue(result);
//
//            }
//        });
//    }
//
//    //for api 2
//
//    public LiveData<List<MovieModel>> getMyData2() {
//        if (myData2 == null || myData3 == null || myData4 == null) {
//            myData2 = new MutableLiveData<>();
//            loadData2();
//        }
//        return myData2;
//    }
//    public LiveData<List<MovieModel>> getMyData3() {
//        if (myData3 == null) {
//            myData3 = new MutableLiveData<>();
//            loadData2();
//        }
//        return myData3;
//    }
//    public LiveData<List<MovieModel>> getMyData4() {
//        if (myData4 == null) {
//            myData4 = new MutableLiveData<>();
//            loadData2();
//        }
//        return myData4;
//    }
//    private void loadData2() {
//        service.execute(new CmHomeApi(new PreferenceHelper(context)));
//        CmHomeApi.addListener(new CmHomeApi.CallBack() {
//            @Override
//            public void onSuccessNewRelease(ArrayList<MovieModel> newRelease) {
//                myData2.setValue(newRelease);
//            }
//            @Override
//            public void onMovies(ArrayList<MovieModel> movie) {
//                myData3.setValue(movie);
//
//            }
//
//            @Override
//            public void onSeries(ArrayList<MovieModel> series) {
//                myData4.setValue(series);
//
//            }
//
//            @Override
//            public void onFail(String message) {
//                error2.setValue(message);
//
//            }
//        });
//
//
//    }





