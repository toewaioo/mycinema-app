package com.two.channelmyanmar.api;

/*
 * Created by Toewaioo on 4/8/25
 * Description: [Add class description here]
 */


import android.util.Log;

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
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchCm implements Runnable {
    String url;
    PreferenceHelper helper;
    CallBack callBack;

    public interface CallBack {
        void onSuccess(ArrayList<MovieModel> data);

        void onFail(String msg);
    }

    public SearchCm(PreferenceHelper helper, String url, CallBack cb) {
        this.url = url;
        this.helper = helper;
        this.callBack = cb;
    }

    @Override
    public void run() {
        Log.d("CmSearch", "calling api..");
        try {
            Log.d("SearchApi:", url);
            Document doc = null;
            if (helper.needApi()) {
                if (helper.useCrossPlatform()) {
                    doc = Jsoup.connect("https://api.allorigins.win/get?charset=UTF-8&url=" + url).ignoreContentType(true).postDataCharset("UTF-8").get();
                    ForkJoinPool pool = new ForkJoinPool();
                    Document finalDoc = doc;
                    pool.submit(() -> parseMovies(finalDoc));
                } else {
                    String api = helper.getApiKey();
                    doc = Jsoup.connect("https://api.webscraping.ai/html?api_key=" + helper.getApiKey() + "&js=false&url="+url).ignoreContentType(true).postDataCharset("UTF-8").get();
                    //Jsoup.connect(api + "/api?url=" + url).get();
                    //soup.connect("https://api.webscraping.ai/html?api_key=" + helper.getApiKey() + "&js=false&url="+url).ignoreContentType(true).postDataCharset("UTF-8").get();
                    ForkJoinPool pool = new ForkJoinPool();
                    Document finalDoc = doc;
                    pool.submit(() -> parseMovies(finalDoc));
                }


            } else {
                doc = Jsoup.connect(url).ignoreContentType(true).postDataCharset("UTF-8").get();
                ForkJoinPool pool = new ForkJoinPool();
                Document finalDoc = doc;
                pool.submit(() -> parseMovies(finalDoc));
            }
//            if (!api.isMyanmar()){
//                doc=Jsoup.connect(url).ignoreContentType(true).postDataCharset("UTF-8").get();
//                callBack.onSuccess(parseMovies(doc));
//            }else {
//                if (Util.useVPN()){
//                    doc=Jsoup.connect(url).ignoreContentType(true).postDataCharset("UTF-8").get();
//                    callBack.onSuccess(parseMovies(doc));
//                }else {
//                    if (api.isPxxl()){
//                        callBack.onSuccess(parseMovies(getWeb("https://m.pxxl.space/api.php?url=")));
//
//                    }else {
//                        if (api.getApi().isEmpty()){
//                            //use cross orgin
//                            doc=Jsoup.connect("https://api.allorigins.win/get?charset=UTF-8&url="+url).ignoreContentType(true).postDataCharset("UTF-8").get();
//                            callBack.onSuccess(parseMovies(doc));
//                        }else{
//                            doc=Jsoup.connect("https://api.webscraping.ai/html?api_key=" + api.getApi() + "&js=false&url="+url).ignoreContentType(true).postDataCharset("UTF-8").get();
//                            callBack.onSuccess(parseMovies(doc));
//                        }
//                    }
//                }
//            }

        } catch (Exception e) {
            callBack.onFail(e.toString());
        }

    }

    private ArrayList<MovieModel> parseMovies(Document doc) {
        Elements elements = doc.getElementsByClass("item_1 items");
        Elements elements1 = elements.get(0).getElementsByClass("item");
        ArrayList<MovieModel> list = new ArrayList<>();
        for (Element ee : elements1) {

            String html = ee.html();
            String name = name(html);
            String baseurl = baseUrl(html);
            String imgUrl = imageUrl(html);
            String imdb = imdb(html);
            String description = description(html);
            String type = type(html);
            list.add(new MovieModel(name, baseurl, imgUrl, imdb, description, type));
        }
        callBack.onSuccess(list);
        return list;
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

    public String getWeb(String baseUrl) {
        try {
            URL urrl = new URL("https://m.pxxl.space/api.php?url=" + url);
            HttpURLConnection connection = (HttpURLConnection) urrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            //connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");

            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }
            return stringBuilder.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String rRating(String html) {
        Matcher m = Pattern.compile("icon-star\"></b></b>(.*?)</span>").matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }
    }

    public String rName(String html) {
        Matcher m = Pattern.compile("ttps\">(.*?)</span>").matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }
    }

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

}

