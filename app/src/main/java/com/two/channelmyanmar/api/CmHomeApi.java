package com.two.channelmyanmar.api;


import android.util.Log;

import com.two.channelmyanmar.model.Genres;
import com.two.channelmyanmar.model.MovieModel;
import com.two.channelmyanmar.preference.PreferenceHelper;
import com.two.channelmyanmar.viewmodel.ApiViewModel;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmHomeApi implements Runnable, ApiViewModel.BaseApi {
    @Override
    public void addListener(Callback callback) {
        this.callback= callback;

    }

    public interface CallBack {
        public void onSuccessNewRelease(ArrayList<MovieModel> newRelease);

        public void onMovies(ArrayList<MovieModel> movie);

        void onSeries(ArrayList<MovieModel> series);

        void onFail(String message);

    }

    PreferenceHelper helper;

    ForkJoinPool service;
    Callback callback;
    private static final CopyOnWriteArrayList<CallBack> listeners = new CopyOnWriteArrayList<>();

    public static void addListener(CallBack listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeListener(CallBack listener) {
        listeners.remove(listener);
    }

    // Notify all global listeners on success new release
    private void notifyGlobalSuccessNewRelease(ArrayList<MovieModel> result) {
        for (CallBack listener : listeners) {
            listener.onSuccessNewRelease(result);
        }
    }
    // Notify all global listeners on success movies
    private void notifyGlobalSuccessMovies(ArrayList<MovieModel> result) {
        for (CallBack listener : listeners) {
            listener.onMovies(result);
        }
    }
    // Notify all global listeners on success series
    private void notifyGlobalSuccessSeries(ArrayList<MovieModel> result) {
        for (CallBack listener : listeners) {
            listener.onSeries(result);
        }
    }
    // Notify all global listeners on fail
    private void notifyGlobalFail(String message) {
        for (CallBack listener : listeners) {
            listener.onFail(message);
        }
    }

    public CmHomeApi(PreferenceHelper helper) {
        this.helper = helper;
        this.service = ForkJoinPool.commonPool();

    }

    @Override
    public void run() {
        Log.d("CmHome","calling api..");
        try {
            if (helper.needApi()) {
                if (helper.useCrossPlatform()){
                    Document doc=Jsoup.connect("https://api.allorigins.win/get?charset=UTF-8&url=https://channelmyanmar.to").ignoreContentType(true).postDataCharset("UTF-8").get();
                    service.submit(() -> onMovieSeries(doc));
                }else {
                    String api = helper.getApiKey();
                    Document doc = Jsoup.connect("https://api.webscraping.ai/html?api_key=" + helper.getApiKey() + "&js=false&url=https://channelmyanmar.to/").get();
                    //Jsoup.connect(api + "/api?url=https://channelmyanmar.to/").get();
                    //Jsoup.connect("https://api.webscraping.ai/html?api_key=" + helper.getApiKey() + "&js=false&url=https://channelmyanmar.to/").get();
                    //service.submit(()->newRelease(doc));
                    service.submit(() -> onMovieSeries(doc));
                }
            } else {
                Document doc = Jsoup.connect("https://channelmyanmar.to/").get();
               // service.submit(() -> newRelease(doc));
                service.submit(() -> onMovieSeries(doc));
            }
        } catch (Exception ignored) {
            notifyGlobalFail(ignored.toString());
            if (callback != null) {
                callback.onError("Failed to load home data");
            }
        }
    }

    public void onMovieSeries(Document doc) {
        //System.out.println(doc.html());
        Elements elements = doc.getElementsByClass("item_1 items");

        Elements elements1 = elements.get(0).getElementsByClass("item");
        // System.out.println(elements1.html());
        ArrayList<MovieModel> list = new ArrayList<>();
        ArrayList<MovieModel> series = new ArrayList<>();
        ArrayList<MovieModel> newRelease = newRelease(doc);
        ArrayList<Genres> genres = getGenres(doc);
        for (Element ee : elements1) {

            String html = ee.html();
            String name = name(html);
            String baseurl = baseUrl(html);
            String imgUrl = imageUrl(html);
            String imdb = imdb(html);
            String description = description(html);
            String type = type(html);
            if (baseurl.contains("tvshows")) {
                series.add(new MovieModel(name, baseurl, imgUrl, imdb, description, type));

            } else {
                list.add(new MovieModel(name, baseurl, imgUrl, imdb, description, type));

            }
        }

        if (callback != null) {
            callback.onSuccess(new CombinedResult(newRelease, list, series, genres));
        }
        service.execute(() -> notifyGlobalSuccessMovies(list));
        service.execute(() -> notifyGlobalSuccessSeries(series));


    }
    private static ArrayList<Genres> parseGenrse(String jsonString) {
        ArrayList<Genres> itemList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray=jsonObject.getJSONArray("genres");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject itemObject = jsonArray.getJSONObject(i);
                String name = itemObject.getString("name");
                String href = itemObject.getString("href");
                String count = itemObject.getString("count");
                itemList.add(new Genres(name,href,count));
            }
        }catch (Exception e){

        }
        return itemList;
    }
    public ArrayList<Genres> getGenres(Document doc){

        Element element=doc.getElementById("moviehome");
        Elements elements=element.getElementsByClass("categorias");
        String html=elements.html();
        Matcher matcher=Pattern.compile("<li").matcher(html);
        ArrayList<Genres> list=new ArrayList<>();
        while (matcher.find()){
            String data=html.substring(matcher.start());
            String url=baseUrl(data);
            String name=getName(data);
            String count=getCount(data);
            list.add(new Genres(name,url,count));

        }
        return list;
    }
    public String getCount(String data){
        Matcher matcherr=Pattern.compile("<span>(.*?)</span>").matcher(data);
        if (matcherr.find()){
            return matcherr.group(1);
        }else {
            return "";
        }
    }
    public String getName(String data){
        Matcher matcher=Pattern.compile("/\">(.*?)</a>").matcher(data);
        if (matcher.find()){
            return matcher.group(1);
        }else {
            return "";
        }
    }
    public ArrayList<MovieModel> newRelease(Document doc) {
        try {
            Element e = doc.getElementById("slider1");
            ArrayList<MovieModel> list = new ArrayList<>();
            Elements es = e.getElementsByClass("item");
            for (Element ee : es) {
                // String html=ee.html();
                String html = ee.html();
                String name = name(html);
                String baseurl = baseUrl(html);
                String imgUrl = imageUrl(html);
                String imdb = imdb(html);
                String description = description(html);
                String type = type(html);
                list.add(new MovieModel(name, baseurl, imgUrl, imdb, description, type));

            }
            return list;
           // service.execute(() -> notifyGlobalSuccessNewRelease(list));

        } catch (Exception e) {
            return new ArrayList<>();
           // service.execute(() -> notifyGlobalSuccessNewRelease(new ArrayList<>()));
        }


    }

    public String getWeb(String url) {
        try {
            URL urrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            // connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");

            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }
            // JSONObject obj=new JSONObject(stringBuilder.toString());
            // System.out.println(obj.getString("contents"));
            return stringBuilder.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static ArrayList<MovieModel> parseRandom(String jsonString) {
        ArrayList<MovieModel> itemList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("random");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject itemObject = jsonArray.getJSONObject(i);
                String title = itemObject.getString("title");
                String href = itemObject.getString("href");
                String image = itemObject.getString("image");
                String content = "";
                String imdb = itemObject.getString("imdb");
                String year = itemObject.getString("year");
                itemList.add(new MovieModel(title, href, image, imdb, content, year));
            }
        } catch (Exception e) {

        }
        return itemList;
    }

    private static ArrayList<MovieModel> parseMovies(String jsonString) {
        ArrayList<MovieModel> itemList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("movies");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject itemObject = jsonArray.getJSONObject(i);
                String title = itemObject.getString("title");
                String href = itemObject.getString("href");
                String image = itemObject.getString("image");
                String content = itemObject.getString("content");
                String imdb = itemObject.getString("imdb");
                String year = itemObject.getString("year");
                itemList.add(new MovieModel(title, href, image, imdb, content, year));
            }
        } catch (Exception e) {

        }
        return itemList;
    }

    private static ArrayList<MovieModel> parseSeries(String jsonString) {
        ArrayList<MovieModel> itemList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("series");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject itemObject = jsonArray.getJSONObject(i);
                String title = itemObject.getString("title");
                String href = itemObject.getString("href");
                String image = itemObject.getString("image");
                String content = itemObject.getString("content");
                String imdb = itemObject.getString("imdb");
                String year = itemObject.getString("year");
                itemList.add(new MovieModel(title, href, image, imdb, content, year));
            }
        } catch (Exception e) {

        }
        return itemList;
    }

    //for doc
    public String name(String html) {
        Matcher m = Pattern.compile("alt=\"(.*?)\"").matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }
    }

    public String baseUrl(String html) {
        Matcher m = Pattern.compile("href=\"(.*?)\"").matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }


    }

    public String imageUrl(String html) {
        Matcher m = Pattern.compile("src=\"(.*?)\"").matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }


    }

    public String imdb(String html) {
        Matcher m = Pattern.compile("icon-star\"></b></b>(.*?)</span>").matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }


    }

    public String description(String html) {
        Matcher m = Pattern.compile("class=\"ttx\">(.*?)<div").matcher(html.replaceAll("[\\r\\n]", ""));
        if (m.find()) {
            return m.group(1);
        } else {
            return "Error occour";
        }


    }

    public String type(String html) {
        Matcher m = Pattern.compile("class=\"typepost\">(.*?)</div>").matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }



    }
    public static class CombinedResult {
        public final List<MovieModel> newReleases;
        public final List<MovieModel> movies;
        public final List<MovieModel> series;
        public final List<Genres> genres;

        public CombinedResult(List<MovieModel> newReleases,
                              List<MovieModel> movies,
                              List<MovieModel> series, List<Genres> genres) {
            this.newReleases = newReleases;
            this.movies = movies;
            this.series = series;
            this.genres = genres;
        }
    }




}

