package com.two.channelmyanmar.api;


import android.util.Log;


import com.two.channelmyanmar.model.HeaderModel;
import com.two.channelmyanmar.model.MovieModel;
import com.two.channelmyanmar.model.SearchModel;
import com.two.channelmyanmar.preference.PreferenceHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmMovieApi implements Runnable {
    String html;
    String url;
    PreferenceHelper api;
    CallBack cb;

    public CmMovieApi(PreferenceHelper api, String url, CallBack cb) {
        this.url = url;
        this.cb = cb;
        this.api = api;
    }

    @Override
    public void run() {
        Log.d("CmMovie","calling api..");
        try {
            // String html=Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36").ignoreContentType(true).execute().body();
            Document doc = null;
            if (api.needApi()){
                if (api.useCrossPlatform()){
                    Log.d("CmMovieApi","load with cross platform");
                    doc=Jsoup.connect("https://api.allorigins.win/get?charset=UTF-8&url="+url).ignoreContentType(true).postDataCharset("UTF-8").get();
                }else {
                    Log.d("CmMovieApi","load with webscraping");
                    String apiKey = api.getApiKey();
                    doc=Jsoup.connect("https://api.webscraping.ai/html?api_key=" + api.getApiKey() + "&js=false&url=" + url).ignoreContentType(true).postDataCharset("UTF-8").get();
                    //Jsoup.connect(apiKey+"/api?url="+url).get();
                    //Jsoup.connect("https://api.webscraping.ai/html?api_key=" + api.getApiKey() + "&js=false&url=" + url).ignoreContentType(true).postDataCharset("UTF-8").get();
                }
            }else {
                doc = Jsoup.connect(url).ignoreContentType(true).postDataCharset("UTF-8").get();
            }
            ForkJoinPool pool = ForkJoinPool.commonPool();
            Document finalDoc = doc;
            pool.submit(()->loadTask(finalDoc));
        } catch (Exception e) {
            e.printStackTrace();
            cb.onFail(e.toString());
            api.useCrossPlatform(false);
        }
    }
    public  void loadTask(Document doc){
        try {
            Element elements = doc.getElementById("uwee");
            html = elements.html();
            String c1 = byclass(doc);
            System.out.println(c1);
            String c2 = byClass(doc);
            String body = getHtml(c1, c2);
            String name = name();
            String imgurl = imgurl();
            String contentRating = contentRating();
            String datePublished = datePublished();
            String releaseYear = releaseYear();
            String duration = duration();
            String categoryTag = categoryTag();
            String ratingValue = ratingValue();
            String megaphone = megaphone();
            String network = network();
            String eye = eye();
            // String name,imgUrl,datePublished,contentRatting,releaseYear,duaration,tag,ratingValue,megaPhone,network,eye;
            HeaderModel header = new HeaderModel(name, imgurl, datePublished, contentRating, releaseYear, duration, categoryTag, ratingValue, megaphone, network, eye);
            ArrayList<MovieModel> related = new ArrayList<>();
            Element r1 = doc.getElementById("slider1");
            Elements relate = r1.getElementsByClass("item");
            for (Element e : relate) {
                String name2 = name2(e.html());
                String url = baseUrl(e.html());
                String img = imageUrl(e.html());
                String rate = rRating(e.html());
                related.add(new MovieModel(name2, url, img, rate, "", "movie"));

            }
            Log.v("",body);
            cb.onSuccess(body, header, related);
            api.useCrossPlatform(false);
        }catch (Exception e){

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

    public String getHtml(String c1, String c2) {

        String s = "";
        //"<link rel=\"stylesheet\" type=\"text/css\" href=\"https://64e151dc12dded1655ff0762--fantastic-souffle-53e55e.netlify.app/styles.css\" media=\"all\"/>";

        String bodyFirstPart = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +

                "  <style>\n" +
                "    /* CSS Variables for Themes */\n" +
                "    :root {\n" +
                "      --bg-color: #1E222B;\n" +
                "      --text-color: #636363;\n" +
                "      --menu-bg: rgba(0, 0, 0, 0.06);\n" +
                "      --menu-color: #858EA5;\n" +
                "      --menu-active-bg: #262B36;\n" +
                "      --menu-active-color: #FFF;\n" +
                "      --tag-bg: #6457dd;\n" +
                "      --tag-color: #333;\n" +
                "      --btn-bg: #58626c;\n" +
                "      --btn-hover: #58626f;\n" +
                "    }\n" +
                "\n" +
                "    /* Light Theme */\n" +
                "    body.light {\n" +
                "      --bg-color: #f0f2f5;\n" +
                "      --text-color: #333;\n" +
                "      --menu-bg: rgba(0, 0, 0, 0.05);\n" +
                "      --menu-color: #666;\n" +
                "      --menu-active-bg: #e0e0e0;\n" +
                "      --menu-active-color: #000;\n" +
                "      --tag-color: #fff;\n" +
                "      --btn-bg: #4a5568;\n" +
                "      --btn-hover: #2d3748;\n" +
                "    }\n" +
                "\n" +
                "    /* Body Styles */\n" +
                "    body {\n" +
                "      margin: 0;\n" +
                "      padding: 0;\n" +
                "      font-family: 'Source Sans Pro', sans-serif;\n" +
                "      font-size: 13px;\n" +
                "      background: var(--bg-color);\n" +
                "      color: var(--text-color);\n" +
                "    }\n" +
                "\n" +
                "    /* Menu Styles */\n" +
                "    .itemmenu ul li a {\n" +
                "      background-color: var(--menu-bg);\n" +
                "      color: var(--menu-color);\n" +
                "    }\n" +
                "\n" +
                "    .itemmenu ul li a.selected,\n" +
                "    .itemmenu ul li a:hover {\n" +
                "      background: var(--menu-active-bg);\n" +
                "      color: var(--menu-active-color);\n" +
                "    }\n" +
                "\n" +
                "    /* Meta Tags */\n" +
                "    .metatags a {\n" +
                "      background-color: var(--tag-bg);\n" +
                "      color: var(--tag-color);\n" +
                "    }\n" +
                "\n" +
                "    /* Buttons */\n" +
                "    .enlaces li button {\n" +
                "      background-color: var(--btn-bg);\n" +
                "    }\n" +
                "\n" +
                "    .enlaces li button:hover {\n" +
                "      background-color: var(--btn-hover);\n" +
                "    }\n" +
                "\n" +
                "    /* Existing Styles */\n" +
                "    .metatags a {\n" +
                "      display: inline-block;\n" +
                "      margin: 5px;\n" +
                "      background-color: var(--tag-bg);\n" +
                "      padding: 4px 8px;\n" +
                "      border-radius: 4px;\n" +
                "      text-decoration: none;\n" +
                "      color: var(--tag-color);\n" +
                "      font-size: 16px;\n" +
                "    }\n" +
                "\n" +
                "    #info {\n" +
                "      margin: 0px 20px 20px 20px;\n" +
                "    }\n" +
                "\n" +
                "    .youtube_id {\n" +
                "      position: relative;\n" +
                "      padding-bottom: 56.25%;\n" +
                "      padding-top: 30px;\n" +
                "      height: 0;\n" +
                "      overflow: hidden;\n" +
                "      float: left;\n" +
                "      width: 100%;\n" +
                "      margin-top: 0;\n" +
                "      margin-bottom: 10px;\n" +
                "    }\n" +
                "\n" +
                "    .youtube_id iframe {\n" +
                "      position: absolute;\n" +
                "      top: 0;\n" +
                "      left: 0;\n" +
                "      width: 100%;\n" +
                "      height: 100%;\n" +
                "    }\n" +
                "\n" +
                "    .idTabs {\n" +
                "      display: flex;\n" +
                "      width: 100%;\n" +
                "      padding: 0;\n" +
                "margin: 0;"+
                "    }\n" +
                "\n" +
                "    ul {\n" +
                "      list-style: none;\n" +
                "    }\n" +
                "\n" +
                "    .itemmenu ul {\n" +
                "      list-style: none;\n" +
                "      align-items: center;\n" +
                "      justify-content: space-evenly;\n" +
                "      width: 100%;\n" +
                "    }\n" +
                "\n" +
                "  .entry-content{\n" +
                "    padding:10px;\n" +
                "    }\n"+
                "    .itemmenu ul li {\n" +
                "      float: left;\n" +
                "      width: 33%;\n" +
                "    }\n" +
                "\n" +
                "    .itemmenu ul li a {\n" +
                "      text-decoration: none;\n" +
                "      width: 96%;\n" +
                "      font-size: 15px;\n" +
                "      text-align: center;\n" +
                "      float: left;\n" +
                "      padding: 15px 0;\n" +
                "      background-color: var(--menu-bg);\n" +
                "      color: var(--menu-color);\n" +
                "    }\n" +
                "\n" +
                "    .itemmenu ul li a.selected,\n" +
                "    .itemmenu ul li a:hover {\n" +
                "      background: var(--menu-active-bg);\n" +
                "      font-weight: 600;\n" +
                "      color: var(--menu-active-color);\n" +
                "    }\n" +
                "\n" +
                "    .metatags {\n" +
                "      width: 100%;\n" +
                "      float: left;\n" +
                "      margin-bottom: 20px;\n" +
                "    }\n" +
                "\n" +
                "    .enlaces {\n" +
                "      padding: 0;\n" +
                "    }\n" +
                "\n" +
                "    .enlaces .elemento {\n" +
                "      margin-bottom: 10px;\n" +
                "    }\n" +
                "\n" +
                "    .enlaces li button {\n" +
                "      background-color: var(--btn-bg);\n" +
                "      color: #fff;\n" +
                "      margin-bottom: 5px;\n" +
                "      margin-left: 20px;\n" +
                "      padding: 10px 10px;\n" +
                "      border: none;\n" +
                "      border-radius: 5px;\n" +
                "      cursor: pointer;\n" +
                "    }\n" +
                "\n" +
                "    .enlaces li button span {\n" +
                "      margin-right: 10px;\n" +
                "    }\n" +
                "\n" +
                "    .enlaces li button:hover {\n" +
                "      background-color: var(--btn-hover);\n" +
                "    }\n" +
                "  </style>\n" +
                "</head>";
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
                "    var boxes = document.querySelectorAll('.sbox > div > div');" +
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
                "function myFun(x){xGetter.show(x);}</script>";

        String cast = "<script> function myCast(x){xGetter.showCast(x);}</script>";

        String list = "<div class=\"itemmenu\"><ul class=\"idTabs\"><li><a href=\"#cap1\" class=\"selected\">Synopsis</a></li><li><a href=\"#cap2\">Trailers</a></li><li><a href=\"#cap3\">Complete cast</a></li></ul></div>";
        return bodyFirstPart+"<body class=\".replaceMe\">" + list + c1 + c2 + script + xgetter + cast + "</body></html>";

    }

    public String byClass(Document doc) {
        Elements e = doc.getElementsByClass("enlaces_box");
        String html = e.html() + "</div>";
        String data2 = "<div class=\"enlaces_box>";
        return data2 + html.replaceAll("<a href=\"", "<button><a onClick=\"myFun(\'").replaceAll("\" target=\"_blank\">", "\')\">").replaceAll("</a>", "</a></button>");

    }

    public String byclass(Document doc) {
        Elements e = doc.getElementsByClass("entry-content");
        for (Element ee : e) {
            ee.removeClass("myResponsiveAd");
            //  System.out.println( ee.removeClass("code-block code-block-9"));
            //  ee.select("div.code-block code-block-9").first().remove();
            ee.getElementsByClass("code-block code-block-9").remove();
        }
        // System.out.println(e.);
        String data = "<div class=\"sbox\">";
        String data1 = "<div class=\"entry-content\">";
        return data + data1 + e.html().replaceAll("id=\"cap3\"", "id=\"cap3\" style=\"display: none;\"").replaceAll("id=\"cap2\"", "id=\"cap2\" style=\"display: none;\"").replaceAll("allowfullscreen", "allowfullscreen=\"\"").replaceAll("//www", "https://www").replaceAll("<a href=\"", "<a onClick=\"myCast(\'").replaceAll("" + "/\" rel=\"tag\">", "\')\">") + "</div></div>";
    }

    public String eye() {
        int index = html.indexOf("icon-eye");
        if (index > 1) {
            Matcher m = Pattern.compile("</b>(.*?)</p>").matcher(html.substring(index));
            if (m.find()) {
                return m.group(1);
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public String network() {
        int index = html.indexOf("network");
        if (index > 1) {
            Matcher m = Pattern.compile("</b>(.*?)</p>").matcher(html.substring(index));
            if (m.find()) {
                return m.group(1);
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public String megaphone() {
        int index = html.indexOf("megaphone");
        int end = html.indexOf("meta_dd limpiar");
        String result = "";
        if (index > 1 & end > 1) {
            Matcher m = Pattern.compile("rel=\"tag\">(.*?)</a>").matcher(html.substring(index, end));
            while (m.find()) {
                result = result + m.group(1) + ",";

            }
        }
        return result;
    }

    public String ratingValue() {
        Matcher m = Pattern.compile("ratingValue\">(.*?)</span>").matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }
    }

    public String categoryTag() {
        Matcher m = Pattern.compile("category tag\">(.*?)</a>").matcher(html);
        String result = "";
        while (m.find()) {
            result = result + m.group(1) + ",";
        }
        return result;

    }

    public String duration() {
        Matcher m = Pattern.compile("duration\">(.*?)</i>").matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }
    }

    public String releaseYear() {
        Matcher m = Pattern.compile("rel=\"tag\">(.*?)</a>").matcher(html);
        if (m.find()) {
            try {
                int i = Integer.parseInt(m.group(1).replace(" ", ""));
                return String.valueOf(i);
            } catch (Exception e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public String datePublished() {
        Matcher m = Pattern.compile("datePublished\">(.*?)</i>").matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }
    }

    public String name() {
        Matcher m = Pattern.compile("\"name\">(.*?)</h1>").matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }
    }

    public String imgurl() {
        Matcher m = Pattern.compile("src=\"(.*?)\"").matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }
    }

    public String contentRating() {
        int index = html.indexOf("contentRating");
        if (index > 1) {
            Matcher m = Pattern.compile(">(.*?)</span>").matcher(html.substring(index));
            m.find();
            return m.group(1);
        } else {
            return "";
        }
    }

    public interface CallBack {
        public void onSuccess(String body, HeaderModel headerm, ArrayList<MovieModel> related);

        public void onFail(String msg);
    }

}


