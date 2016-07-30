package com.ishinomakihackathon2016.bluespring.vr;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.google.vr.sdk.widgets.video.VrVideoView.Options;
import com.ishinomakihackathon2016.bluespring.skyway.SkyWay;
import com.ishinomakihackathon2016.bluespring.skyway.SkyWayPeerEventListener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.widget.SeekBar;
import android.widget.Toast;

import io.skyway.Peer.MediaConnection;


/**
 * Created by adam on 16/07/30.
 */
public class FullVrVideoActivity extends Activity {
    private static final String TAG = FullVrVideoActivity.class.getSimpleName();

    /**
     * Preserve the video's state when rotating the phone.
     */
    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_PROGRESS_TIME = "progressTime";
    /**
     * The video duration doesn't need to be preserved, but it is saved in this example. This allows
     * the seekBar to be configured during {@link #onRestoreInstanceState(Bundle)} rather than waiting
     * for the video to be reloaded and analyzed. This avoid UI jank.
     */
    private static final String STATE_VIDEO_DURATION = "videoDuration";

    /**
     * Arbitrary constants and variable to track load status. In this example, this variable should
     * only be accessed on the UI thread. In a real app, this variable would be code that performs
     * some UI actions when the video is fully loaded.
     */
    public static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
    public static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
    public static final int LOAD_VIDEO_STATUS_ERROR = 2;

    private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

    public int getLoadVideoStatus() {
        return loadVideoStatus;
    }

    /**
     * Tracks the file to be loaded across the lifetime of this app.
     **/
    private Uri fileUri;

    /**
     * Configuration information for the video.
     **/
    private Options videoOptions = new Options();

    private VideoLoaderTask backgroundVideoLoaderTask;

    /**
     * The video view and its custom UI elements.
     */
    protected VrVideoView videoWidgetView;

    /**
     * Seeking UI & progress indicator. The seekBar's progress value represents milliseconds in the
     * video.
     */

    /**
     * By default, the video will start playing as soon as it is loaded. This can be changed by using
     * {@link VrVideoView#pauseVideo()} after loading the video.
     */
    private boolean isPaused = false;

    private Handler mHandler;
    private SkyWay mSw;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vr_layout);

        Log.i(TAG, "================= FullVrVideoActivity.onCreate() =================");

        mHandler = new Handler();

        Intent intent = getIntent();
        // setupPeer(intent);

        // Bind input and output objects for the view.
        videoWidgetView = (VrVideoView) findViewById(R.id.video_view);
        videoWidgetView.setEventListener(new ActivityEventListener());
        loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

        // Initial launch of the app or an Activity recreation due to rotation.
        handleIntent();
    }

    private void setupPeer(Intent intent) {
        String myPeerId = intent.getStringExtra("my_peer_id");
        final String dstPeerid = intent.getStringExtra("dst_peer_id");
        mSw = new SkyWay(getApplicationContext(), mHandler, null);
        mSw.createPeer(myPeerId, new SkyWayPeerEventListener() {
            @Override
            public void OnOpen(String peerId) {
                mSw.createDataConnection(dstPeerid);
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

    /**
     * Called when the Activity is already running and it's given a new intent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, this.hashCode() + ".onNewIntent()");
        // Save the intent. This allows the getIntent() call in onCreate() to use this new Intent during
        // future invocations.
        setIntent(intent);
        // Load the new image.
        handleIntent();
    }

    /**
     * Load custom videos based on the Intent or load the default video. See the Javadoc for this
     * class for information on generating a custom intent via adb.
     */
    private void handleIntent() {
        if (backgroundVideoLoaderTask != null) {
            backgroundVideoLoaderTask.cancel(true);
        }
        backgroundVideoLoaderTask = new VideoLoaderTask();
        backgroundVideoLoaderTask.execute(Pair.create(fileUri, videoOptions));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(STATE_PROGRESS_TIME, videoWidgetView.getCurrentPosition());
        savedInstanceState.putLong(STATE_VIDEO_DURATION, videoWidgetView.getDuration());
        savedInstanceState.putBoolean(STATE_IS_PAUSED, isPaused);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
        videoWidgetView.seekTo(progressTime);

        isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
        if (isPaused) {
            videoWidgetView.pauseVideo();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Prevent the view from rendering continuously when in the background.
        videoWidgetView.pauseRendering();
        // If the video is playing when onPause() is called, the default behavior will be to pause
        // the video and keep it paused when onResume() is called.
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the 3D rendering.
        videoWidgetView.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        videoWidgetView.shutdown();
        super.onDestroy();
    }

    private void togglePause() {
        if (isPaused) {
            this.playVideo();
        } else {
            this.pauseVideo();
        }
        mSw.sendMessage("HELLO!!");
    }

    private void playVideo() {
        long seek = getPosition();
        playVideo(seek);
    }

    private void playVideo(long seektime) {
        // seektime: position in milliseconds
        if (!isPaused) return;

        videoWidgetView.seekTo(seektime);
        videoWidgetView.playVideo();
        isPaused = !isPaused;
    }

    private void pauseVideo() {
        if (isPaused) return;

        videoWidgetView.pauseVideo();
        isPaused = !isPaused;
    }

    private long getPosition() {
        // the current position in milliseconds.
        return videoWidgetView.getCurrentPosition();
    }

    /**
     * When the user manipulates the seek bar, update the video position.
     */
    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoWidgetView.seekTo(progress);
            } // else this was from the ActivityEventHandler.onNewFrame()'s seekBar.setProgress update.
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrVideoEventListener {
        /**
         * Called by video widget on the UI thread when it's done loading the video.
         */
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Sucessfully loaded video " + videoWidgetView.getDuration());
            loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
        }

        /**
         * Called by video widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            // An error here is normally due to being unable to decode the video format.
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            Toast.makeText(
                    FullVrVideoActivity.this, "Error loading video: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
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

    /**
     * Helper class to manage threading.
     */
    class VideoLoaderTask extends AsyncTask<Pair<Uri, Options>, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Pair<Uri, Options>... fileInformation) {
            if (fileInformation == null || fileInformation.length < 1
                    || fileInformation[0] == null || fileInformation[0].first == null) {
                Log.i(TAG, "loadVideoFromAssert(penguins.mp4)");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // No intent was specified, so we default to playing the local stereo-over-under video.
                        Options options = new Options();
                        options.inputType = Options.TYPE_STEREO_OVER_UNDER;

                        try {
                            //videoWidgetView.loadVideoFromAsset("congo.mp4", options);
                            videoWidgetView.loadVideoFromAsset("penguins.mp4", options);
                        } catch (Exception e) {
                            Log.e(TAG, "Could not open video: " + e);
                            e.printStackTrace();
                            videoWidgetView.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(FullVrVideoActivity.this, "Error opening file. ", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            } else {
                final Pair<Uri, Options> fileInfo = fileInformation[0];
                Log.i(TAG, "loadVideoFromAssert(" + fileInformation[0].first + ")");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            videoWidgetView.loadVideo(fileInfo.first, fileInfo.second);
                        } catch (Exception e) {
                            Log.e(TAG, "Could not open video: " + e);
                            e.printStackTrace();
                            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
                            videoWidgetView.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(FullVrVideoActivity.this, "Error opening file. ", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });

            }
            return true;
        }
    }
}
