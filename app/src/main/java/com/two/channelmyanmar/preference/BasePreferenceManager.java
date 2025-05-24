package com.two.channelmyanmar.preference;


import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.sax.SAXResult;

// 1. Serializable Interface

// 2. Abstract Preference Manager
public abstract class BasePreferenceManager {
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    protected BasePreferenceManager(Context context, String prefName) {
        preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    // Primitive types
    protected void save(String key, String value) {
        editor.putString(key, value).apply();
    }

    protected String getString(String key) {
        return preferences.getString(key, "");
    }
    protected String getString(String key,String defValue){
        return preferences.getString(key,defValue);
    }
    protected void save(String key, long value) {
        editor.putLong(key, value).apply();
    }

    protected long getLong(String key) {
        return preferences.getLong(key, 0);
    }

    protected void save(String key, int value) {
        editor.putInt(key, value).apply();
    }

    protected int getInt(String key) {
        return preferences.getInt(key, 0);
    }
    protected void save(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    protected boolean getBoolean(String key) {
        return preferences.getBoolean(key, true);
    }
    protected boolean getBoolean(String key,boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    // Add other primitive types as needed...

    // Custom objects
    protected <T extends JsonSerializable> void save(String key, T object) {
        try {
            save(key, object.toJson().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected <T extends JsonSerializable> T get(String key, Class<T> type) {
        try {
            String jsonString = getString(key);
            if (!jsonString.isEmpty()) {
                JSONObject jsonObject = new JSONObject(jsonString);
                T object = type.newInstance();
                object.fromJson(jsonObject);
                return object;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ArrayList of custom objects
    protected <T extends JsonSerializable> void saveList(String key, List<T> list) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (T item : list) {
                jsonArray.put(item.toJson());
            }
            save(key, jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected <T extends JsonSerializable> List<T> getList(String key, Class<T> type) {
        List<T> list = new ArrayList<>();
        try {
            String jsonString = getString(key);
            if (!jsonString.isEmpty()) {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    T object = type.newInstance();
                    object.fromJson(jsonObject);
                    list.add(object);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void clear() {
        editor.clear().apply();
    }
}
