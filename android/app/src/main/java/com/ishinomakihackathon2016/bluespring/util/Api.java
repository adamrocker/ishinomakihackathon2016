package com.ishinomakihackathon2016.bluespring.util;

import android.os.Handler;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by adam on 16/07/30.
 */

public class Api {
    private static final String TAG = Api.class.getSimpleName();
    private static final String API_URL = "http://ishinomaki-hackathon2016.appspot.com";
    volatile private static Api _instance;
    private OkHttpClient mClient;
    private Handler mHandler;
    private Api(Handler handler) {
        mClient = new OkHttpClient();
        mHandler = handler;
    }

    public static Api getInstance(Handler handler) {
        if (Api._instance == null) {
            Api._instance = new Api(handler);
        }
        return Api._instance;
    }

    private void post(String path, RequestBody body, final ApiCallback callback) {
        String url = Api.API_URL + "/api/v1" + path;
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String result = response.body().string();
                Log.d(TAG, result);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(result);
                    }
                });
            }
        });
    }

    public void register(String peerId, ApiCallback callback) {
        // リクエストボディを作る
        RequestBody body = new FormEncodingBuilder()
            .add("peer_id", peerId)
            .build();

        post("/peer/register", body, callback);
    }

    public void getPeer(long roomId, ApiCallback callback) {
        // リクエストボディを作る
        RequestBody body = new FormEncodingBuilder().build();

        String path = "/peer/get/" + roomId;
        post(path, body, callback);
    }
}

