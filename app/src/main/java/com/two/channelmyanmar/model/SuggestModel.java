package com.two.channelmyanmar.model;

public class SuggestModel {
    @Override
    public String toString() {
        return "SuggestModel{" +
                "imgUrl='" + imgUrl + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
    String imgUrl;
    String link;
    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public SuggestModel(String imgUrl, String link) {
        this.imgUrl = imgUrl;
        this.link = link;
    }


}
