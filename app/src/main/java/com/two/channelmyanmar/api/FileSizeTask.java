package com.two.channelmyanmar.api;

/*
 * Created by Toewaioo on 4/27/25
 * Description: [Add class description here]
 */

import java.util.Map;
import java.util.List;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.text.DecimalFormat;
public class FileSizeTask implements Runnable
{
    public interface CallBack{
        void onSuccess(String length);
        void onFail(String msg);
    }
    CallBack cb;
    String url;
    public FileSizeTask(String url,CallBack cb){
        this.url=url;
        this.cb=cb;
    }

    @Override
    public void run() {
        try{
            long headers = getHeaders(url);
            cb.onSuccess(getSizeStr(headers));
        }catch(Exception e){
            e.printStackTrace();
            cb.onFail(e.toString());
        }
    }
    private static long getHeaders(String segmentUrl) {
        Map<String, List<String>> headers = null;
        try {
            URL url = new URL(segmentUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            long size=conn.getContentLength();
            conn.disconnect();
            return size;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0l;
    }
    public static String getSizeStr(long size) {
        StringBuilder sb = new StringBuilder();
        DecimalFormat format = new DecimalFormat("###.00");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            sb.append(format.format(i)).append("GB");
        } else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            sb.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            sb.append(format.format(i)).append("KB");
        } else if (size < 1024) {
            if (size <= 0) {
                sb.append("0B");
            } else {
                sb.append((int) size).append("B");
            }
        }
        return sb.toString();
    }
}
