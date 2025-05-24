package com.two.channelmyanmar.preference;


import org.json.JSONException;
import org.json.JSONObject;

public interface JsonSerializable {
    JSONObject toJson() throws JSONException;
    void fromJson(JSONObject jsonObject) throws JSONException;
}