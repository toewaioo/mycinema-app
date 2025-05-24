package com.two.channelmyanmar.model;


public class HeaderModel
{
    String name,imgUrl,datePublished,contentRatting,releaseYear,duaration,tag,ratingValue,megaPhone,network,eye;

    public HeaderModel(String name,String imgUrl, String datePublished, String contentRatting, String releaseYear, String duaration, String tag, String ratingValue, String megaPhone, String network, String eye) {
        this.name = name;
        this.imgUrl=imgUrl;
        this.datePublished = datePublished;
        this.contentRatting = contentRatting;
        this.releaseYear = releaseYear;
        this.duaration = duaration;
        this.tag = tag;
        this.ratingValue = ratingValue;
        this.megaPhone = megaPhone;
        this.network = network;
        this.eye = eye;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setDatePublished(String datePublished) {
        this.datePublished = datePublished;
    }

    public String getDatePublished() {
        return datePublished;
    }

    public void setContentRatting(String contentRatting) {
        this.contentRatting = contentRatting;
    }

    public String getContentRatting() {
        return contentRatting;
    }

    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public void setDuaration(String duaration) {
        this.duaration = duaration;
    }

    public String getDuaration() {
        return duaration;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setRatingValue(String ratingValue) {
        this.ratingValue = ratingValue;
    }

    public String getRatingValue() {
        return ratingValue;
    }

    public void setMegaPhone(String megaPhone) {
        this.megaPhone = megaPhone;
    }

    public String getMegaPhone() {
        return megaPhone;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getNetwork() {
        return network;
    }

    public void setEye(String eye) {
        this.eye = eye;
    }

    public String getEye() {
        return eye;
    }


}

