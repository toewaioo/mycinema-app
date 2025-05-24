package com.two.channelmyanmar.api;

import com.two.channelmyanmar.model.MeganzModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

/*
 * Created by Toewaioo on 4/27/25
 * Description: [Add class description here]
 */
public class StreamApi implements Runnable{
    public interface Listener{
        void onFail(String msg);
        void onFound(ArrayList<MeganzModel> result);
    }
    private final String TAG = this.getClass().getSimpleName();

    private Listener listener;
    private String url;
    public StreamApi(String url,Listener listener) {
        // constructor code
        this.url = url;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            String html =fetchHtml(url);
            String script = extractScript(html);
            List<String> fl = firstList(script);
            List<String> sl=secondList(script);
            String finalUrl = firstL(sl)+secondL(fl)+lastL(script);
            if (isURLValid(finalUrl)){
                ArrayList<MeganzModel> result = new ArrayList<>();
                result.add(new MeganzModel(finalUrl,"m3u8","Normal",""));
                if (listener!=null)
                    listener.onFound(result);
            }else {
                if (listener!=null)
                    listener.onFail("Invalid link.");
            }

        }catch (Exception e){
            if (listener!=null)
                listener.onFail(e.toString());
        }


    }
    public  boolean isURLValid(String urlString) {
        System.out.println(urlString);
        try {
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD"); // Faster: we don't need full response body
            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.setReadTimeout(5000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 400); // 2xx and 3xx are considered valid
        } catch (IOException e) {
            return false; // If any error happens, the URL is not valid
        }
    }
    public  String lastL(String script){
        StringBuilder sb=new StringBuilder();

        try {
            String s = script.substring(script.indexOf("adb|"));
            sb.append(s.substring(s.indexOf("adb|") + 4, s.indexOf("||")) + "&f=");

            String ss = script.substring(script.indexOf("adb|"));
            String sss = ss.substring(ss.indexOf("||") + 2);
            //  System.out.println(s.indexOf("||"));
            sb.append(sss.substring(0, sss.indexOf("|")) + "&srv=");
            String vvplay = script.substring(script.indexOf("vvplay"));
            String vvplays = vvplay.substring(vvplay.indexOf("100|") + 4);
            // System.out.println(s.indexOf("||"));
            String vvplayss = vvplays.substring(0, vvplays.indexOf("|"));
            sb.append(vvplayss + "&p1=" + vvplayss + "&p2=" + vvplayss + "&i=0.4&sp=500");
        }catch (Exception e){
            sb.append(lastL2(script));
        }
        return sb.toString();
    }
    public  String lastL2(String script){
        StringBuilder sb=new StringBuilder();

        try {
            String s = script.substring(script.indexOf("adb||")+5);
            sb.append(s.substring(0, s.indexOf("|")) + "&f=");

            String ss = script.substring(script.indexOf("adb|"));
            String sss = ss.substring(ss.indexOf("||") + 2);
            sb.append(sss.substring(0, sss.indexOf("|")) + "&srv=");
            String vvplay = script.substring(script.indexOf("vvplay"));
            String vvplays = vvplay.substring(vvplay.indexOf("100|") + 4);
            String vvplayss = vvplays.substring(0, vvplays.indexOf("|"));
            sb.append(vvplayss + "&p1=" + vvplayss + "&p2=" + vvplayss + "&i=0.4&sp=500");
        }catch (Exception e){

        }
        return sb.toString();
    }
    public  String secondL(List<String> list){
        StringBuilder sb=new StringBuilder();
        try {
            if(list.size()==12){
                int i=0;
                for(String s:list){
                    if(i==0){

                    }else if(i==1){
                        sb.append(s+"&e=");
                    }else if(i==2){
                        sb.append(s+"&asn=");
                    }else if(i==9){
                        sb.append(s+"&s=");
                    }
                    i++;
                }
            }else if(list.size()==13){
                int i=0;
                for(String s:list){
                    if(i==0){

                    }else if(i==1){
                        sb.append(s+"-");
                    }else if(i==2){
                        sb.append(s+"&e=");
                    }else if(i==3){
                        sb.append(s+"&asn=");
                    }else if(i==10){
                        sb.append(s+"&s=");
                    }
                    i++;
                }
            }else{
                int i=0;
                for(String s:list){
                    if(i==0){

                    }else if(i==1){
                        sb.append(s+"-");
                    }else if(i==2){
                        sb.append(s+"-");
                    }
                    else if(i==3){
                        sb.append(s+"&e=");
                    }else if(i==4){
                        sb.append(s+"&asn=");
                    }else if(i==11){
                        sb.append(s+"&s=");
                    }
                    i++;
                }
            }
        }catch (Exception e){

        }

        return sb.toString();
    }
    public  String firstL(List<String> list){
        StringBuilder sb=new StringBuilder("https://");
        try {
            if(list.size()==5){
                int i=0;
                for(String s:list){
                    if(i==0){
                        sb.append(s+".");
                    }else if(i==1){
                        sb.append(s+".com/hls2/");
                    }else if(i==2){
                        sb.append(s+"/");
                    }else if(i==3){
                        sb.append(s+"/");
                    }else if(i==4){
                        sb.append(s+"/index-v1-a1.m3u8?t=");
                    }
                    i++;
                }
            }else{
                int i=0;
                for(String s:list){
                    if(i==0){
                        sb.append(s+".");
                    }else if(i==1){
                        sb.append(s+"-");
                    }
                    else if(i==2){
                        sb.append(s+".com/hls2/");
                    }else if(i==3){
                        sb.append(s+"/");
                    }else if(i==4){
                        sb.append(s+"/");
                    }else if(i==5){
                        sb.append(s+"/index-v1-a1.m3u8?t=");
                    }
                    i++;
                }
            }
        }catch (Exception e){

        }

        return sb.toString();
    }
    public List<String> firstList(String script){
        try {
            String html=script.substring(script.indexOf("setup|"),script.lastIndexOf("'."));
            String first=html.substring(0,html.indexOf("|master"));
            String[] f=first.split("\\|");
            List<String> fl= Arrays.asList(f);
            Collections.reverse(fl);
            return fl;
        }catch (Exception e){

            return new ArrayList<>();
        }

    }
    public List<String> secondList(String script){
        try {
            String html=script.substring(script.indexOf("setup|"),script.lastIndexOf("'."));
            String second=html.substring(html.indexOf("master|")+7);
            String[] s=second.split("\\|");
            List<String> sl=Arrays.asList(s);
            Collections.reverse(sl);
            return sl;
        }catch (Exception e){
            return new ArrayList<>();
        }

    }
    public  String extractScript(String input)
    {
        // Pattern to match <script ... </script> including the content inside
        Pattern pattern = Pattern.compile("<script(.*?)</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        StringBuilder sb=new StringBuilder();
        while (matcher.find())
        {
            String script= matcher.group(0);
            if (script.contains("eval"))
            {
                sb.append(matcher.group(1));
            }// Return the full matched <script>...</script> block
        }
        return sb.toString();

        // return null; // Return null if no match found
    }
    public  String fetchHtml(String urlString)
    {
        StringBuilder htmlContent = new StringBuilder();

        try
        {
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            // Set request method
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK)
            {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );
                String line;
                while ((line = reader.readLine()) != null)
                {
                    htmlContent.append(line).append("\n");
                }
                reader.close();
            }
            else
            {
                System.out.println("Failed to fetch HTML. Response code: " + responseCode);
                if(listener != null )
                    listener.onFail("Failed to fetch HTML. Response code: " + responseCode);
            }

            connection.disconnect();
        }
        catch (IOException e)
        {
        }

        return htmlContent.toString();
    }
}