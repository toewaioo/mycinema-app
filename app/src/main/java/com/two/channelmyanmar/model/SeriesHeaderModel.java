package com.two.channelmyanmar.model;

public class SeriesHeaderModel {
    String name,status,imgurl,poster,imdb,voted;
    public SeriesHeaderModel(String name,String status, String imgurl, String poster, String imdb, String voted) {
        this.name = name;
        this.status=status;
        this.imgurl = imgurl;
        this.poster = poster;
        this.imdb = imdb;
        this.voted = voted;
    }
    public String getStatus(){
        return status;
    }
    public void setStatus(String status){
        this.status=status;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public String getVoted() {
        return voted;
    }

    public void setVoted(String voted) {
        this.voted = voted;
    }

}
