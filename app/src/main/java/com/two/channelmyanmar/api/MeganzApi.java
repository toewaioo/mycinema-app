package com.two.channelmyanmar.api;

/*
 * Created by Toewaioo on 4/27/25
 * Description: [Add class description here]
 */


import com.two.channelmyanmar.model.MeganzModel;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import java.io.IOException;
import org.json.JSONObject;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
public class MeganzApi implements Runnable
{
    StreamApi.Listener cb;
    String url;
    public MeganzApi(String url, StreamApi.Listener cb){
        this.url=url;
        this.cb=cb;
    }
    @Override
    public void run() {
        String id=id(url);
        String key=key(url);
        if(id!=null&&key!=null){
            try {
                cb.onFound(url(id, key));
            } catch (JSONException e) {
                cb.onFail(e.toString());
            } catch (IOException e) {
                cb.onFail(e.toString());
            }
        } else {
            cb.onFail("Url mist match");
        }
    }
    public String id(String url){

        Matcher m=Pattern.compile("/file/(.*?)#").matcher(url);
        if( m.find()){
            return m.group(1);
        }else{
            return null;
        }

    }
    public String key(String url){

        Matcher m=Pattern.compile("/file/(.*?)#(.+)").matcher(url);
        if( m.find()){
            return m.group(2);
        }else{
            return null;
        }

    }
    public ArrayList<String> getList(String data){
        Matcher m=Pattern.compile("\"(.*?)\"").matcher(data);
        ArrayList<String> result=new ArrayList<>();
        while(m.find()){
            result.add(m.group(1));
        }
        return result;
    }
    public String getG(String data){
        Matcher m=Pattern.compile("\"g\":\\[(.*?)\\]").matcher(data);
        Matcher m1=Pattern.compile("\"g\":\"(.*?)\"").matcher(data);
        if(m.find()){

            return m.group(1);
        }else{
            if(m1.find()){
                return m1.group();
            }else{
                return null;
            }
        }
    }
    public String getAt(String data){
        Matcher m=Pattern.compile("\"at\":\"(.*?)\"").matcher(data);
        if(m.find()){
            return m.group(1);
        }else{
            return "";
        }
    }
    public  ArrayList<MeganzModel> url(String id, String key) throws JSONException, IOException{

        System.out.println(id+key);
        ArrayList<MeganzModel> result=new ArrayList<>();

        byte[] file_key = MegaCrypt.base64_url_decode_byte(key);


        int[] intKey = MegaCrypt.aByte_to_aInt(file_key);

        int[] keyNOnce = new int[] { intKey[0] ^ intKey[4], intKey[1] ^ intKey[5], intKey[2] ^ intKey[6], intKey[3] ^ intKey[7], intKey[4], intKey[5] };
        byte[] key1 = MegaCrypt.aInt_to_aByte(keyNOnce[0], keyNOnce[1], keyNOnce[2], keyNOnce[3]);

        String response=apirequest(id);
        System.out.println("1"+response);
        String at=getAt(response);
        //    System.out.println("\n2"+at);
        String g=getG(response);
        //   System.out.println("\n3"+g);
        String attribute= new String(MegaCrypt.aes_cbc_decrypt(MegaCrypt.base64_url_decode_byte(at), key1));
        //System.out.println("\n4"+attribute);
        //     System.out.println("\n5"+getName(attribute));
        if(g!=null){
            ArrayList<String> data=getList(g);
            for(int i=0;i<data.size();i++){
                if (i==1) {
                    result.add(new MeganzModel(data.get(i) + "/" + getName(attribute), getType(getName(attribute)), "Server" + String.valueOf(i)));
                }
                System.out.println(data.get(i)+"/"+getName(attribute));
            }
        }
        //  JSONArray data=new JSONArray(apirequest(id));
        //  JSONObject file_data=new JSONObject( apirequest(id));
        //   String attribs = (file_data.getString("at"));

        //   attribs = new String(MegaCrypt.aes_cbc_decrypt(MegaCrypt.base64_url_decode_byte(attribs), key1));

        /*   JSONArray arr=  file_data.getJSONArray("g");
         for(int i=0;i<arr.length();i++){
         System.out.println(arr.get(i)+"/"+getName(attribs));
         }

         */
        // System.out.println( attribs);
        return result;
    }
    public String getType(String url){
        if(url.endsWith(".mp4")){
            return "video/mp4";
        }else if(url.endsWith(".mp3")){
            return "audio/mp3";
        }else if(url.endsWith(".apk")){
            return "file/apk";
        }else{
            return "video/mp4";
        }
    }
    public  String getName(String data){
        Matcher m=Pattern.compile("\"n\":\"(.*?)\"").matcher(data);
        if(m.find()){
            return m.group(1);
        }else{
            return "error";
        }
    }

    public String apirequest(String id) throws IOException{
        JSONObject json = new JSONObject();
        try {
            json.put("a", "g");
            json.put("g","1");
            json.put("ssl", "2");
            json.put("v","2");
            json.put("p",id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        URL url = new URL ("https://g.api.mega.co.nz/cs");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST"); //use post method
        con.setDoOutput(true); //we will send stuff
        con.setDoInput(true); //we want feedback


        con.setUseCaches(false); //no caches
        con.setAllowUserInteraction(false);
        con.setRequestProperty("Content-Type", "text/xml");

        con.setDoOutput(true);

        //String jsonInputString = "{\"a\": \"g\", \"g\": \"1\",\"ssl\":\"0\",\"v\":\"2\",\"p\":\"KF5DDJ4C\"}";
        OutputStream os = con.getOutputStream();
        try {
            String s="["+json.toString()+"]";
            byte[] input = s.getBytes("utf-8");
            os.write(input, 0, input.length);
        } finally { //in this case, we are ensured to close the output stream
            if (os != null)
                os.close();
        }

        //  System.out.println(con.getResponseCode());
        InputStream in = con.getInputStream();
        StringBuffer response = new StringBuffer();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close(); //close the reader
        } catch (IOException e) {
            e.printStackTrace();
            //  cb.onFail(e.toString());
        } finally {  //in this case, we are ensured to close the input stream
            if (in != null)
                in.close();
        }
        return response.toString().substring(1, response.toString().length() - 1);
    }

    public interface CallBack{
        public void onSuccess(ArrayList<MeganzModel> result);
        public void onFail(String error);
    }

}
