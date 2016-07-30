package com.ishinomakihackathon2016.bluespring.vr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.ishinomakihackathon2016.bluespring.skyway.SkyWay;
import com.ishinomakihackathon2016.bluespring.skyway.SkyWayPeerEventListener;
import com.ishinomakihackathon2016.bluespring.util.Api;
import com.ishinomakihackathon2016.bluespring.util.ApiCallback;

import org.json.JSONObject;

import io.skyway.Peer.MediaConnection;

public class MainActivity extends Activity implements View.OnClickListener{
    private static final String TAG = MainActivity.class.getSimpleName();

    private Handler mHandler;

    private Toolbar mToolbar;
    private ImageButton mShareBtn;
    private Button mJoinBtn;

    private SkyWay mP2p;
    private String mPeerId;
    private long mRoomId = -1;
    private String mRoomUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();

        mP2p = new SkyWay(getApplicationContext());

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getColor(R.color.toolbar_title));
        mToolbar.setTitle(getString(R.string.app_name));

        mShareBtn = (ImageButton)findViewById(R.id.share_button);
        mShareBtn.setOnClickListener(this);

        mJoinBtn = (Button)findViewById(R.id.join_button);
        mJoinBtn.setOnClickListener(this);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            Log.d(TAG, "Intent.data.path=" + uri.getPath());
            String path = uri.getPath();
            String roomId = path.split("/")[2];
            mRoomId = Long.parseLong(roomId);
            Log.d(TAG, "  - roomId=" + mRoomId);
            connectPeer();
        } else {
            makeRoom();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.share_button) {
            // RoomのURLをシェアする
            if (mRoomUrl == null) {
                Log.e(TAG, "No share url");
            } else {
                // mRoomUrlをシェアする
            }
        } else if( id == R.id.join_button) {
            if (0 <= mRoomId) {
                // Roomに入る
            } else {

            }
        }
    }

    private void makeRoom() {
        mP2p.createPeer(new SkyWayPeerEventListener() {
            @Override
            public void OnOpen(String peerId) {
                mPeerId = peerId;
                createRoom();
            }

            @Override
            public void OnCall(MediaConnection o) {
            }

            @Override
            public void OnClose(Object o) {
            }

            @Override
            public void OnDisconnected(Object o) {
            }

            @Override
            public void OnError(Object o) {
            }
        });
    }

    private void createRoom() {
        Api api = Api.getInstance(mHandler);
        api.register(mPeerId, new ApiCallback() {
            @Override
            public void onFailure() {
                Log.e(TAG, "FAILURE/API Register");
            }

            @Override
            public void onResponse(String result) {
                Log.d(TAG, "Response/API Register: " + result);
                try {
                    JSONObject json = new JSONObject(result);
                    mRoomUrl = json.getString("room_page");
                    Log.d(TAG, "  - room_page=" + mRoomUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void connectPeer() {
        Api api = Api.getInstance(mHandler);
        api.getPeer(mRoomId, new ApiCallback() {
            @Override
            public void onFailure() {
                Log.e(TAG, "api.getPeer");
            }

            @Override
            public void onResponse(String result) {
                Log.i(TAG, "api.getPeer");
                Log.d(TAG, "Response/API GetPeer: " + result);
                try {
                    JSONObject json = new JSONObject(result);
                    mPeerId = json.getString("peer_id");
                    Log.d(TAG, " peer_id - =" + mPeerId);

                    // P2Pで接続開始
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
