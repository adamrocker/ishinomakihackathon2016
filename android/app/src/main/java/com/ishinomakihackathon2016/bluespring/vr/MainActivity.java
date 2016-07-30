package com.ishinomakihackathon2016.bluespring.vr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.ishinomakihackathon2016.bluespring.skyway.SkyWay;
import com.ishinomakihackathon2016.bluespring.skyway.SkyWayDataEventListener;
import com.ishinomakihackathon2016.bluespring.skyway.SkyWayPeerEventListener;
import com.ishinomakihackathon2016.bluespring.util.Api;
import com.ishinomakihackathon2016.bluespring.util.ApiCallback;

import org.json.JSONObject;

import java.io.IOException;

import io.skyway.Peer.MediaConnection;

public class MainActivity extends Activity implements
        View.OnClickListener,
        Toolbar.OnMenuItemClickListener{
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
    private static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
    private static final int LOAD_VIDEO_STATUS_ERROR = 2;

    private Handler mHandler;

    private Toolbar mToolbar;
    private Button mJoinBtn;

    private SkyWay mP2p;
    private String mPeerId;
    private String mDstPeerId;
    private String mMyPeerId; // 自分のID
    private long mRoomId = -1;
    private String mRoomUrl;

    // ===== MAIN Layout
    private LinearLayout mMainLayout;

    // ===== VR VIDEO
    private LinearLayout mVideoLayout;
    private VrVideoView videoWidgetView;
    private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mHandler = new Handler();

        mP2p = new SkyWay(getApplicationContext(), new SkyWayDataEventListener() {
            @Override
            public void OnOpen(Object o) {

            }

            @Override
            public void OnData(Object object) {
                String value = (String) object;
                String[] command = value.split(":");
                String cmd = command[0]; // cmd
                String exec = command[1]; // play or pause
                if (exec.equals("play")) {
                    final String msec = command[2];
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            playVideo(Long.parseLong(msec));
                        }
                    });
                } else if(exec.equals("pause")) {
                    final long msec = Long.parseLong(command[2]);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            pauseVideo(msec);
                        }
                    });
                } else if (exec.equals("open")) {
                    final int movieId = Integer.parseInt(command[2]);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            openVr();
                        }
                    });
                } else if (exec.equals("start")) {
                    long msec = Long.parseLong(command[2]);
                    long now = System.currentTimeMillis();
                    long diff = Math.max(msec - now, 0);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playVideo(0);
                        }
                    }, diff);
                }
            }

            @Override
            public void OnClose(Object o) {

            }

            @Override
            public void OnError(Object o) {

            }
        });

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getColor(R.color.toolbar_title));
        mToolbar.setTitle(getString(R.string.app_name));
        mToolbar.inflateMenu(R.menu.menu_main);
        mToolbar.setOnMenuItemClickListener(this);

        mJoinBtn = (Button)findViewById(R.id.join_button);
        mJoinBtn.setOnClickListener(this);

        mVideoLayout = (LinearLayout) findViewById(R.id.video_layout);
        videoWidgetView = (VrVideoView) findViewById(R.id.video_view);
        videoWidgetView.setEventListener(new ActivityEventListener());
        loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            Log.d(TAG, "Intent.data.path=" + uri.getPath());
            String path = uri.getPath();
            String roomId = path.split("/")[2];
            mRoomId = Long.parseLong(roomId);
            mDstPeerId = String.valueOf(mRoomId);
            Log.d(TAG, "  - roomId=" + mRoomId);
            //connectPeer();
            connectDestPeer();
        } else {
            createRoom();
        }
    }

    @Override
    public void onClick(View v) {
        //mP2p.sendMessage("sender-peer-id:" + mMyPeerId);
        int id = v.getId();
        if( id == R.id.join_button) {
            int movieId = 0;
            mP2p.sendCommandOpen(movieId);
            openVr();
        }
    }

    private void openVr() {
        // show VR layout
        mVideoLayout.setVisibility(View.VISIBLE);
        loadVideo();
        // close Main Layout
        mMainLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            // RoomのURLをシェアする
            if (mRoomUrl == null) {
                Log.e(TAG, "No share url");
            } else {
                // mRoomUrlをシェアする
                share();
            }
        }
        return true;
    }

    private void createRoom() {
        Api api = Api.getInstance(mHandler);
        api.register("DUMMY", new ApiCallback() {
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
                    mRoomId = json.getLong("room_id");
                    mMyPeerId = String.valueOf(mRoomId);
                    Log.d(TAG, "  - room_page=" + mRoomUrl);
                    Log.d(TAG, "  - my_peer_id=" + mMyPeerId);
                    mP2p.createPeer(mMyPeerId, new SkyWayPeerEventListener() {
                        @Override
                        public void OnOpen(String peerId) {

                        }

                        @Override
                        public void OnCall(MediaConnection o) {

                        }

                        @Override
                        public void OnConnection(Object o) {

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void connectDestPeer() {
        mP2p.createPeer(null, new SkyWayPeerEventListener() {
            @Override
            public void OnOpen(String peerId) {
                mMyPeerId = peerId;
                mP2p.createDataConnection(mDstPeerId);
            }

            @Override
            public void OnCall(MediaConnection o) {

            }

            @Override
            public void OnConnection(Object o) {

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

    private void share(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mRoomUrl);
        startActivity(intent);
    }

    // ================== VR VIDEO =====================
    private void loadVideo() {
        VrVideoView.Options options = new VrVideoView.Options();
        options.inputType = VrVideoView.Options.TYPE_STEREO_OVER_UNDER;
        try {
            videoWidgetView.loadVideoFromAsset("penguins.mp4", options);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "LOAD ERROR!");
        }
    }

    private void togglePause() {
        if (isPaused) {
            this.playVideo();
        } else {
            this.pauseVideo();
        }
    }

    private void playVideo() {
        long seek = getPosition();
        playVideo(seek);
    }

    private void playVideo(long seektime) {
        // seektime: position in milliseconds
        mP2p.sendCommandPlay(seektime);
        videoWidgetView.seekTo(seektime);
        videoWidgetView.playVideo();
        isPaused = false;
    }

    private void pauseVideo() {
        long seek = getPosition();
        pauseVideo(seek);
    }

    private void pauseVideo(long seektime) {
        mP2p.sendCommandPause(seektime);
        videoWidgetView.seekTo(seektime);
        videoWidgetView.pauseVideo();
        isPaused = true;
    }

    private long getPosition() {
        // the current position in milliseconds.
        return videoWidgetView.getCurrentPosition();
    }

    private class ActivityEventListener extends VrVideoEventListener {
        /**
         * Called by video widget on the UI thread when it's done loading the video.
         */
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Sucessfully loaded video " + videoWidgetView.getDuration());
            loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
            pauseVideo(13000);
        }

        /**
         * Called by video widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            // An error here is normally due to being unable to decode the video format.
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            Log.e(TAG, "Error loading video: " + errorMessage);
        }

        @Override
        public void onClick() {
            togglePause();
        }

        /**
         * Update the UI every frame.
         */
        @Override
        public void onNewFrame() {
        }

        /**
         * Make the video play in a loop. This method could also be used to move to the next video in
         * a playlist.
         */
        @Override
        public void onCompletion() {
            videoWidgetView.seekTo(0);
        }
    }
}
