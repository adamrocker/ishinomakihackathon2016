package com.ishinomakihackathon2016.bluespring.util;

/**
 * Created by adam on 16/07/30.
 */
public interface ApiCallback {
    public void onFailure();
    public void onResponse(String result);
}
