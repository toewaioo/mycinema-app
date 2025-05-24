package com.two.channelmyanmar.model;

/*
 * Created by Toewaioo on 4/8/25
 * Description: [Add class description here]
 */

public class Genres {
    public String name,url,count;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public Genres(String name, String url, String count) {
        this.name = name;
        this.url = url;
        this.count = count;
    }
}
