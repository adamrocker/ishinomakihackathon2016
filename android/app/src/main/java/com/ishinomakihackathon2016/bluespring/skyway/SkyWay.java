package com.ishinomakihackathon2016.bluespring.skyway;

import android.content.Context;
import android.util.Log;

import io.skyway.Peer.Browser.MediaConstraints;
import io.skyway.Peer.Browser.MediaStream;
import io.skyway.Peer.CallOption;
import io.skyway.Peer.ConnectOption;
import io.skyway.Peer.DataConnection;
import io.skyway.Peer.MediaConnection;
import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerError;
import io.skyway.Peer.PeerOption;

public class SkyWay {
    public static final String TAG = SkyWay.class.getSimpleName();
    private Context mContext;
    private Peer mPeer;
    private MediaConnection _mMedia;
    private MediaStream _mMediaLocal;
    private MediaStream _mMediaRemote;
    private DataConnection mDataConnection;
    private String mPeerId;
    private String mTargetPeerId;

    private SkyWayDataEventListener mDataEventListener;

    public SkyWay(Context context, SkyWayDataEventListener listener) {
        this.mContext = context;
        this.mDataEventListener = listener;
    }

    public void createPeer(String myPeerId, final SkyWayPeerEventListener listener) {
        PeerOption options = new PeerOption();
        options.key = "6eac44de-4e2f-417a-9834-e3a892446a21";
        options.domain = "localhost.com";
        Peer peer;
        if (myPeerId == null) {
            Log.i(TAG, "Create Peer without myPeerId");
            peer = new Peer(this.mContext, options);
        } else {
            Log.i(TAG, "Create Peer with myPeerId(=Room Owner)");
            peer = new Peer(this.mContext, myPeerId, options);
        }

        this.mPeer = peer;

        peer.on(Peer.PeerEventEnum.OPEN, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d(TAG, "Peer/OPEN");
				if (o instanceof String) {
					mPeerId = (String) o;
					Log.d(TAG, "My Peer ID:" + mPeerId);
                    if(listener != null) listener.OnOpen(mPeerId);
				}

            }
        });

        peer.on(Peer.PeerEventEnum.CALL, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d(TAG, "Peer/CALL");
                _mMedia = (MediaConnection) o;
                setMediaCallbacks();
                receiveCall();
                if(listener != null) listener.OnCall(_mMedia);
            }
        });

        peer.on(Peer.PeerEventEnum.CONNECTION, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d(TAG, "Peer/CONNECTION");
                Log.i(TAG, "  - " + o);
                if (o instanceof DataConnection) {
                    mDataConnection = (DataConnection) o;
                    mTargetPeerId = mDataConnection.peer;
                    Log.i(TAG, " - target_peer_id=" + mTargetPeerId);
                    setDataConnectionCallbacks();
                }
                if(listener != null) listener.OnConnection(o);
            }
        });

        peer.on(Peer.PeerEventEnum.CLOSE, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d(TAG, "Peer/CLOSE");
                disconnect();
                if(listener != null) listener.OnClose(o);
            }
        });

        peer.on(Peer.PeerEventEnum.DISCONNECTED, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d(TAG, "Peer/DISCONNECTED");
                if(listener != null) listener.OnDisconnected(o);
            }
        });

        peer.on(Peer.PeerEventEnum.ERROR, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d(TAG, "Peer/ERROR");
                if(listener != null) listener.OnError(o);
            }
        });
    }

    public void createDataConnection(String destPeerId) {
        Log.d(TAG, "createDataConnection: " + destPeerId);
        // connect option
		ConnectOption option = new ConnectOption();
		option.metadata = "data connection";
		option.label = "chat";
		option.serialization = DataConnection.SerializationEnum.BINARY;

		// connect
		mDataConnection = mPeer.connect(destPeerId, option);

        setDataConnectionCallbacks();
    }

    private boolean hasConnection() {
        synchronized (this) {
            return this.mPeer != null;
        }
    }

    private void setMediaCallbacks() {
        final MediaConnection media = this._mMedia;
        media.on(MediaConnection.MediaEventEnum.STREAM, new OnCallback() {
			@Override
			public void onCallback(Object object) {
				_mMediaRemote = (MediaStream) object;
                // import io.skyway.Peer.Browser.Canvas;
				// Canvas canvas = (Canvas) findViewById(R.id.svPrimary);
				// canvas.addSrc(_mMediaRemote, 0);
			}
		});

		media.on(MediaConnection.MediaEventEnum.CLOSE, new OnCallback() {
			@Override
			public void onCallback(Object object) {
				if (_mMediaRemote == null) {
					return;
				}

                // import io.skyway.Peer.Browser.Canvas;
				// Canvas canvas = (Canvas) findViewById(R.id.svPrimary);
				// canvas.removeSrc(_mMediaRemote, 0);
                _mMediaRemote.close();
				_mMediaRemote = null;
			}
		});

		media.on(MediaConnection.MediaEventEnum.ERROR, new OnCallback() {
			@Override
			public void onCallback(Object object) {
				PeerError error = (PeerError) object;
				Log.d(TAG, "MediaError@MediaConnection: " + error);
			}
		});
    }

    private void setDataConnectionCallbacks() {
        final DataConnection data = this.mDataConnection;
        data.on(DataConnection.DataEventEnum.OPEN, new OnCallback() {
			@Override
			public void onCallback(Object object) {
                Log.d(TAG, "Peer-Dataconnection/OPEN");
                Log.i(TAG, "  - " + object);
                mDataEventListener.OnOpen(object);
			}
		});

		data.on(DataConnection.DataEventEnum.DATA, new OnCallback() {
			@Override
			public void onCallback(Object object) {
                Log.d(TAG, "Peer-Dataconnection/DATA");
                Log.i(TAG, "  - " + object);
				String value = null;

				if (object instanceof String) {
                    mDataEventListener.OnData(object);
				}
			}
		});

		data.on(DataConnection.DataEventEnum.CLOSE, new OnCallback() {
			@Override
			public void onCallback(Object object) {
                Log.d(TAG, "Peer-Dataconnection/CLOSE");
                Log.i(TAG, "  - " + object);
                // ToDo disconnect process
                mDataEventListener.OnClose(object);
			}
		});

		data.on(DataConnection.DataEventEnum.ERROR, new OnCallback() {
			@Override
			public void onCallback(Object object) {
                Log.d(TAG, "Peer-Dataconnection/ERROR");
                Log.i(TAG, "  - " + object);
				// TODO: DataEvent/ERROR
				PeerError error = (PeerError) object;
				Log.d(TAG, "DataError: " + error);
				String strMessage = error.message;
                mDataEventListener.OnError(object);
			}
		});
    }

    public void sendMessage(String msg) {
        Log.d(TAG, "sendMessage: " + msg);
        if (mDataConnection == null) {
            Log.d(TAG, "  - error: no data connection");
            return;
        }
        Log.i(TAG, " - peer(target_peer_id):" + mDataConnection.peer);
        Log.i(TAG, " - isOpen:" + mDataConnection.isOpen);
        boolean result = mDataConnection.send(msg);
        Log.d(TAG, "  - result: " + result);
    }

    public void sendCommandStartVideo(long delay) {
        if (delay < 0) {
            delay = 1500;
        }
        long utc = System.currentTimeMillis() + delay;  // start after 1.5 second
        sendMessage("cmd:start:" + utc);
    }

    public void sendCommandOpen(int movieId) {
        sendMessage("cmd:open:" + movieId);
    }

    public void sendCommandPlay(long seektime) {
        sendMessage("cmd:play:" + seektime);
    }

    public void sendCommandPause(long seektime) {
        sendMessage("cmd:pause:" + seektime);
    }

    private void startLocalStream() throws IllegalStateException {
        if (!this.hasConnection()) {
            throw new IllegalStateException("No peer to start any local stream");
        }
        MediaConstraints constraints = new MediaConstraints();
        _mMediaLocal = this.mPeer.getLocalMediaStream(constraints);
    }

    private void receiveCall() {
        synchronized (this) {
            if (!hasConnection()) {
                return;
            }

            this.startLocalStream();
            this._mMedia.answer(_mMediaLocal);
        }
    }

    public void call(String peerId) throws IllegalStateException {
        synchronized (this) {
            if (!this.hasConnection()) {
                throw new IllegalStateException("Already had a connection");
            }

            if (this._mMedia != null) {
                if (this._mMedia.isOpen) {
                    this._mMedia.close();
                    this._mMedia = null;
                }
            }

            this.startLocalStream();
            CallOption option = new CallOption();
            this._mMedia = this.mPeer.call(peerId, _mMediaLocal, option);
            if (this._mMedia != null) {
                this.setMediaCallbacks();
            }
        }
    }

    public void closeCall() {
        synchronized (this) {
            final MediaConnection media = this._mMedia;
            if (media != null && media.isOpen) {
                media.close();
            }
            this._mMedia = null;

            if (this._mMediaRemote != null) {
                this._mMediaRemote.close();
                this._mMediaRemote = null;
            }

            if (this._mMediaLocal != null) {
                this._mMediaLocal.close();
                this._mMediaLocal = null;
            }
        }
    }

    private void closeData() {
        // Disconnect the data connection
    }

    public void disconnect() {
        this.closeCall();
        this.closeData();

        synchronized (this) {
            if (this.mPeer != null) {
                if (!this.mPeer.isDisconnected) {
                    this.mPeer.disconnect();
                }

                if (!this.mPeer.isDestroyed) {
                    this.mPeer.destroy();
                }
                this.mPeer = null;
            }
        }
    }

}
