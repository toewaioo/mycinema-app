package com.two.channelmyanmar.model;

/*
 * Created by Toewaioo on 4/27/25
 * Description: [Add class description here]
 */

public class MeganzModel {

    String url,type,size,quality;
    public MeganzModel(String url,String type,String quality,String size) {
        this.url = url;
        this.type = type;
        this.size = size;
        this.quality = quality;
    }
    public MeganzModel(String url,String type,String quality) {
        this.url = url;
        this.type = type;
        this.size = "";
        this.quality = quality;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getQuality() {
        return quality;
    }

}

