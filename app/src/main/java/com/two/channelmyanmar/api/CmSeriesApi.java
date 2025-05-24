package com.two.channelmyanmar.api;


import android.util.Log;

import com.two.channelmyanmar.model.MovieModel;
import com.two.channelmyanmar.model.SeriesHeaderModel;
import com.two.channelmyanmar.preference.PreferenceHelper;

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

public class CmSeriesApi implements Runnable {

    public String url;
    public PreferenceHelper api;
    public CallBack cb;

    public CmSeriesApi(PreferenceHelper api, String url, CallBack cb) {
        this.url = url;
        this.cb = cb;
        this.api = api;
    }

    @Override
    public void run() {
        Log.d("SeriesApi","calling api");
        try {
            Document doc = null;
            if (api.needApi()){
                if (api.useCrossPlatform()){
                   // Log.d("CmMovieApi","load with cross platform");
                    doc=Jsoup.connect("https://api.allorigins.win/get?charset=UTF-8&url="+url).ignoreContentType(true).postDataCharset("UTF-8").get();
                }else {
                   // Log.d("CmMovieApi","load with webscraping");
                    String apiKey = api.getApiKey();
                    //Jsoup.connect("https://api.allorigins.win/get?charset=UTF-8&url="+url).ignoreContentType(true).postDataCharset("UTF-8").get();
                    doc =Jsoup.connect("https://api.webscraping.ai/html?api_key=" + api.getApiKey() + "&js=false&url=" + url).ignoreContentType(true).postDataCharset("UTF-8").get();
                    //Jsoup.connect(apiKey+"/api?url="+url).get();
                            //Jsoup.connect("https://api.webscraping.ai/html?api_key=" + api.getApiKey() + "&js=false&url=" + url).ignoreContentType(true).postDataCharset("UTF-8").get();
                }
            }else {
                doc = Jsoup.connect(url).ignoreContentType(true).postDataCharset("UTF-8").get();
            }
            //Jsoup.connect(url).ignoreContentType(true).postDataCharset("UTF-8").get();
            SeriesHeaderModel headerModel = getHeader(doc);
            ForkJoinPool pool = ForkJoinPool.commonPool();
            Document finalDoc = doc;
            pool.submit(()->gethtml(finalDoc));
            pool.submit(()->getList(finalDoc));
            if (cb!=null)
                cb.onSuccessHeader(headerModel);



        } catch (Exception e) {
            e.printStackTrace();
            cb.onFail(e.toString());
        }
    }
    public ArrayList<MovieModel> getList(Document doc){
        ArrayList<MovieModel> list = new ArrayList<>();
        Elements sidebar = doc.getElementsByClass("sidebartv");
        // System.out.println(sidebar.html());
        //  System.out.println("-------------------------------------------------------------------------------");
        Elements iiem = sidebar.get(0).getElementsByClass("tvitemrel");
        //  System.out.println(iiem.html());
        // System.out.println("-------------------------------------------------------------------------------");
        for (Element e : iiem) {
            String data = e.html();
            String name = name2(data);
            String url = baseUrl(data);
            String imgurl = imageUrl(data);
            String imdb = rating(data);
            System.out.println(imgurl);
            list.add(new MovieModel(name, url, imgurl, imdb, "", "series"));
        }
        if (cb!=null)
            cb.onSuccessRelated(list);
        return list;
    }

    public String getWeb() {
        try {
            URL urrl = new URL("https://api.allorigins.win/get?charset=UTF-8&url=" + url);
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
            JSONObject obj = new JSONObject(stringBuilder.toString());
            // System.out.println(obj.getString("contents"));
            return obj.getString("contents");

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String rating(String html) {
        Matcher m = Pattern.compile("rating\">(.*?)</span>").matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }
    }

    public String name2(String html) {
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

    public String gethtml(Document doc) {
        String body = "<div class=\"central\">"+getbody(doc);
        String metadata = getMetadatas(doc);

        //  System.out.println(body);
        String s = "";
        //"<link rel=\"stylesheet\" type=\"text/css\" href=\"https://64e151dc12dded1655ff0762--fantastic-souffle-53e55e.netlify.app/styles.css\" media=\"all\"/>";

        String style = "<style>\n" +
                "/* === Theme Variables === */\n" +
                ":root {\n" +
                "  /* Dark Theme (Default) */\n" +
                "  --bg-color: #1E222B;\n" +
                "  --text-color: #636363;\n" +
                "  --menu-bg: rgba(0, 0, 0, .06);\n" +
                "  --menu-color: #858EA5;\n" +
                "  --menu-active-bg: #262B36;\n" +
                "  --menu-active-color: #FFF;\n" +
                "  --tag-bg: rgb(139, 164, 197);\n" +
                "  --btn-bg: #58626c;\n" +
                "  --btn-hover: #58626f;\n" +
                "  --section-bg: rgba(0,0,0,0.1);\n" +
                "  --border-color: rgba(255,255,255,0.1);\n" +
                "}\n" +
                "\n" +
                "/* Light Theme */\n" +
                "body.light {\n" +
                "  --bg-color: #f5f5f5;\n" +
                "  --text-color: #333333;\n" +
                "  --menu-bg: rgba(0, 0, 0, 0.05);\n" +
                "  --menu-color: #666666;\n" +
                "  --menu-active-bg: #e0e0e0;\n" +
                "  --menu-active-color: #000000;\n" +
                "  --tag-bg: #4a90e2;\n" +
                "  --btn-bg: #6457dd;\n" +
                "  --btn-hover: #5143c5;\n" +
                "  --section-bg: rgba(0,0,0,0.05);\n" +
                "  --border-color: rgba(0,0,0,0.1);\n" +
                "}\n" +
                "\n" +
                "/* === Your Original Styles - Modified with Variables === */\n" +
                ".metatags a {\n" +
                "  background-color: var(--tag-bg);\n" +
                "  border-radius: 10px;\n" +
                "  padding: 3px 5px;\n" +
                "  color: var(--menu-active-color);\n" +
                "  transition: background-color 0.3s ease;\n" +
                "}\n" +
                "\n" +
                "#info {\n" +
                "  margin: 0px 20px 20px 20px;\n" +
                "}\n" +
                "\n" +
                "#body {\n" +
                "  margin: 0;\n" +
                "  padding: 0;\n" +
                "}\n" +
                "\n" +
                ".youtube_id_tv {\n" +
                "  position: relative;\n" +
                "  padding-bottom: 56.25%;\n" +
                "  padding-top: 30px;\n" +
                "  height: 0;\n" +
                "  overflow: hidden;\n" +
                "  float: left;\n" +
                "  width: 100%;\n" +
                "  margin-top: 0;\n" +
                "  margin-bottom: 10px;\n" +
                "  border: 1px solid var(--border-color);\n" +
                "}\n" +
                "\n" +
                ".youtube_id_tv iframe {\n" +
                "  position: absolute;\n" +
                "  top: 0;\n" +
                "  left: 0;\n" +
                "  width: 100%;\n" +
                "  height: 100%;\n" +
                "}\n" +
                "\n" +
                ".idTabs {\n" +
                "  display: flex;\n" +
                "  width: 100%;\n" +
                "  padding: 0;\n" +
                "  margin: 0;\n" +
                "  background: var(--menu-bg);\n" +
                "}\n" +
                "\n" +
                ".itemmenu ul {\n" +
                "  list-style: none;\n" +
                "  width: 100%;\n" +
                "}\n" +
                "\n" +
                ".itemmenu ul li {\n" +
                "  width: 33%;\n" +
                "}\n" +
                "\n" +
                ".itemmenu ul li a {\n" +
                "  text-decoration: none;\n" +
                "  width: 96%;\n" +
                "  font-size: 15px;\n" +
                "  text-align: center;\n" +
                "  float: left;\n" +
                "  padding: 15px 0;\n" +
                "  background-color: var(--menu-bg);\n" +
                "  color: var(--menu-color);\n" +
                "  transition: all 0.3s ease;\n" +
                "}\n" +
                "\n" +
                ".itemmenu ul li a.selected,\n" +
                ".itemmenu ul li a:hover {\n" +
                "  background: var(--menu-active-bg);\n" +
                "  font-weight: 600;\n" +
                "  color: var(--menu-active-color);\n" +
                "}\n" +
                "\n" +
                ".metatags {\n" +
                "  width: 100%;\n" +
                "  float: left;\n" +
                "  margin-bottom: 20px;\n" +
                "}\n" +
                "\n" +
                "body {\n" +
                "  margin: 0;\n" +
                "  padding: 0;\n" +
                "  font-family: 'Source Sans Pro', sans-serif;\n" +
                "  font-size: 13px;\n" +
                "  color: var(--text-color);\n" +
                "  background: var(--bg-color);\n" +
                "  transition: background-color 0.3s ease, color 0.3s ease;\n" +
                "}\n" +
                "\n" +
                ".enlaces li button {\n" +
                "  background-color: var(--btn-bg);\n" +
                "  color: #fff;\n" +
                "  margin-bottom: 5px;\n" +
                "  margin-left: 20px;\n" +
                "  padding: 10px 10px;\n" +
                "  border: none;\n" +
                "  border-radius: 5px;\n" +
                "  cursor: pointer;\n" +
                "  transition: background-color 0.3s ease;\n" +
                "}\n" +
                "\n" +
                ".enlaces li button span {\n" +
                "  margin-right: 10px;\n" +
                "}\n" +
                "\n" +
                ".enlaces li button:hover {\n" +
                "  background-color: var(--btn-hover);\n" +
                "}\n" +
                "\n" +
                ".backdropss {\n" +
                "  width: 100%;\n" +
                "  overflow: hidden;\n" +
                "  position: relative;\n" +
                "}\n" +
                "\n" +
                ".galeria {\n" +
                "  display: flex;\n" +
                "  animation: scroll 15s linear infinite;\n" +
                "  width: 100%;\n" +
                "}\n" +
                "\n" +
                ".galeria_img {\n" +
                "  flex-shrink: 0;\n" +
                "  width: 100%;\n" +
                "  height: auto;\n" +
                "  transition: transform 1s ease-in-out;\n" +
                "}\n" +
                "\n" +
                "img {\n" +
                "  width: 100%;\n" +
                "  margin: 6px;\n" +
                "  border-radius: 5px;\n" +
                "  border: 1px solid var(--border-color);\n" +
                "}\n" +
                "\n" +
                "@keyframes scroll {\n" +
                "  0%, 100% { transform: translateX(0); }\n" +
                "  12.5% { transform: translateX(0); }\n" +
                "  25% { transform: translateX(-100%); }\n" +
                "  37.5% { transform: translateX(-100%); }\n" +
                "  50% { transform: translateX(-200%); }\n" +
                "  62.5% { transform: translateX(-200%); }\n" +
                "  75% { transform: translateX(-300%); }\n" +
                "  87.5% { transform: translateX(-300%); }\n" +
                "}\n" +
                "\n" +
                "/* Theme Toggle Button */\n" +
                ".theme-toggle {\n" +
                "  position: fixed;\n" +
                "  bottom: 20px;\n" +
                "  right: 20px;\n" +
                "  padding: 12px;\n" +
                "  background: var(--tag-bg);\n" +
                "  color: white;\n" +
                "  border: none;\n" +
                "  border-radius: 50%;\n" +
                "  cursor: pointer;\n" +
                "  z-index: 1000;\n" +
                "  box-shadow: 0 2px 10px rgba(0,0,0,0.2);\n" +
                "  transition: all 0.3s ease;\n" +
                "}\n" +
                "\n" +
                ".theme-toggle:hover {\n" +
                "  transform: scale(1.1);\n" +
                "}\n" +
                "</style>";
        String script = "<script>" +
                "var tabs = document.querySelectorAll('.idTabs li a');" +
                "" +
                "for (var i = 0; i < tabs.length; i++) {" +
                "  tabs[i].addEventListener('click', function(e) {" +
                "    e.preventDefault();" +
                "" +
                "    for (var j = 0; j < tabs.length; j++) {" +
                "      tabs[j].classList.remove('selected');" +
                "    }" +
                "" +
                "    this.classList.add('selected');" +
                "" +
                "    var boxes = document.querySelectorAll('.central > div ');" +
                "    for (var k = 0; k < boxes.length; k++) {" +
                "      boxes[k].style.display = 'none';" +
                "    }" +
                "" +
                "    var id = this.getAttribute('href').replace('#', '');" +
                "    var box = document.getElementById(id);" +
                "    box.style.display = 'block';" +
                "  });" +
                "}" +
                "</script>";
        String xgetter = "<script>" +
                "function myFun(x){xGetter.show(x);}function myCast(x){xGetter.showCast(x);}</script>";

        String list = "<div class=\"itemmenu\"><ul class=\"idTabs\"><li><a href=\"#info\" class=\"selected\">Synopsis</a></li><li><a href=\"#trailer\">Trailers</a></li><li><a href=\"#season\">Complete cast</a></li></ul></div>";
        String html= "<html><head>" + style +"</head><body class=\".replaceMe\">" + list + body + metadata + script + xgetter + "</body></html>";
        if (cb!=null)
            cb.onSuccessBody(html);
        return html;
    }

    public SeriesHeaderModel getHeader(Document doc) {
        Elements lodoA = doc.getElementsByClass("ladoA");
        Elements lodoB = doc.getElementsByClass("ladoB");

        String imgurll = "";
        Matcher matcher = Pattern.compile("src=\"(.*?)\"").matcher(lodoA.html());
        if (matcher.find()) {
            imgurll = matcher.group(1);
        }
        String posteer = "";
        Matcher mposter = Pattern.compile("url\\((.*?)\\)").matcher(lodoB.html());
        if (mposter.find()) {
            posteer = mposter.group(1);
        }
        String status = "";
        Matcher mstatus = Pattern.compile("status\">(.*?)</span>").matcher(lodoB.html());
        if (mstatus.find()) {
            status = mstatus.group(1);
        }
        String name = "";
        Matcher mname = Pattern.compile("name\">(.*?)</h1>").matcher(lodoB.html());
        if (mname.find()) {
            name = mname.group(1);
        }
        String tmdb = "";
        Matcher mtmdb = Pattern.compile("ratingValue\">(.*?)</span>").matcher(lodoB.html());
        if (mtmdb.find()) {
            tmdb = mtmdb.group(1).replaceAll("<b>", "") + "/10 ";
        }
        String rcount = "";
        Matcher mrcount = Pattern.compile("ratingCount\">(.*?)</b>").matcher(lodoB.html());
        if (mrcount.find()) {
            rcount = mrcount.group(1);
        }
        //name,status,imgurl,poster,imdb,voted;
        return new SeriesHeaderModel(name, status, imgurll, posteer, tmdb, rcount);

    }

    public String getbody(Document doc) {
        Element element = doc.getElementById("info");
        Elements bod = element.getElementsByClass("contenidotv");
        Elements backdrop = element.getElementsByClass("backdropss");
        for (Element ee : bod) {
            ee.removeClass("myResponsiveAd");
            //  System.out.println( ee.removeClass("code-block code-block-9"));

            //  ee.select("div.code-block code-block-9").first().remove();
            System.out.println(ee.getElementsByClass("code-block code-block-9").remove());
        }

        Element trailer = doc.getElementById("trailer");
        Element season = doc.getElementById("seasons");
        String trailers = "<div id=\"trailer\" style=\"display: none;\">" + trailer.html() + "</div>";
        String seasons = "<div id=\"seasons\" style=\"display: none;\">" + season.html() + "</div>";

        return "<div id=\"info\" style=\"display: block;\"><h2 class=\"css3\">General information</h2>" + bod.outerHtml().replaceAll("href=\"", "onClick=\"myFun('").replaceAll("\">", "')\">").replaceAll("\" target=\"_blank\" rel=\"noopener noreferrer", "").replaceAll("'\\)\"'\\)\"", "')\"").replaceAll("</a>", "</a>") + "</div>" + trailers.replaceAll("allowfullscreen", "allowfullscreen=\"\"").replaceAll("//www", "https://www") + seasons + backdrop.outerHtml() + "</div></div>";

    }

    public String getMetadatas(Document doc) {
        String metadatac = "";
        //"<div id=\"info\" style=\"display: block;\"><h2 class=\"css3\">General information</h2><div id=\"info\" style=\"display: block;\"><h2 class=\"css3\">General information</h2>";
        Element element = doc.getElementById("info");
        Elements elements = element.getElementsByClass("metadatac");
        for (Element ee : elements) {
            metadatac = metadatac + ee.outerHtml();
        }
        return metadatac.replaceAll("<a href=\"", "<a onClick=\"myCast('").replaceAll("" + "/\" rel=\"tag\">", "')\">");
    }

    public interface CallBack {
        void onSuccessHeader(SeriesHeaderModel header);
        void onSuccessBody(String html);
        void onSuccessRelated(ArrayList<MovieModel> related);
        void onFail(String msg);
    }
}

