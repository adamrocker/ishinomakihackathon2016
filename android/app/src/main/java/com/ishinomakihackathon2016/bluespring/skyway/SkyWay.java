package com.ishinomakihackathon2016.bluespring.skyway;

import android.content.Context;
import android.os.Handler;
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
    private Context _context;
    private Peer _peer;
    private MediaConnection _media;
    private MediaStream _msLocal;
    private MediaStream _msRemote;
    private DataConnection _data;
    private String _id;


    private SkyWay(Context context) {
        this._context = context;
    }

    private void createPeer() {
        PeerOption options = new PeerOption();
        options.key = "6eac44de-4e2f-417a-9834-e3a892446a21";
        options.domain = "localhost.com";
        this._peer = new Peer(this._context, options);
        this.setPeerCallbacks();
    }

    private void createDataConnection() {
        // connect option
		ConnectOption option = new ConnectOption();
		option.metadata = "data connection";
		option.label = "chat";
		option.serialization = DataConnection.SerializationEnum.BINARY;

		// connect
		_data = _peer.connect(_id, option);

        setDataConnectionCallbacks();
    }

    private boolean hasConnection() {
        synchronized (this) {
            return this._peer != null;
        }
    }

    private void setPeerCallbacks() {
        final Peer peer = this._peer;

        peer.on(Peer.PeerEventEnum.OPEN, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d(TAG, "[On/Open]");
				if (o instanceof String) {
					_id = (String) o;
					Log.d(TAG, "ID:" + _id);
				}
            }
        });

        peer.on(Peer.PeerEventEnum.CALL, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                _media = (MediaConnection) o;
                setMediaCallbacks();
                receiveCall();
            }
        });

        peer.on(Peer.PeerEventEnum.CLOSE, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                disconnect();
            }
        });

        peer.on(Peer.PeerEventEnum.DISCONNECTED, new OnCallback() {
            @Override
            public void onCallback(Object o) {

            }
        });

        peer.on(Peer.PeerEventEnum.ERROR, new OnCallback() {
            @Override
            public void onCallback(Object o) {

            }
        });
    }

    private void setMediaCallbacks() {
        final MediaConnection media = this._media;
        media.on(MediaConnection.MediaEventEnum.STREAM, new OnCallback() {
			@Override
			public void onCallback(Object object) {
				_msRemote = (MediaStream) object;
                // import io.skyway.Peer.Browser.Canvas;
				// Canvas canvas = (Canvas) findViewById(R.id.svPrimary);
				// canvas.addSrc(_msRemote, 0);
			}
		});

		media.on(MediaConnection.MediaEventEnum.CLOSE, new OnCallback() {
			@Override
			public void onCallback(Object object) {
				if (_msRemote == null) {
					return;
				}

                // import io.skyway.Peer.Browser.Canvas;
				// Canvas canvas = (Canvas) findViewById(R.id.svPrimary);
				// canvas.removeSrc(_msRemote, 0);
                _msRemote.close();
				_msRemote = null;
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
        final DataConnection data = this._data;
        data.on(DataConnection.DataEventEnum.OPEN, new OnCallback() {
			@Override
			public void onCallback(Object object) {
			}
		});

		data.on(DataConnection.DataEventEnum.DATA, new OnCallback() {
			@Override
			public void onCallback(Object object) {
				String value = null;

				if (object instanceof String) {
					value = (String) object;
				}
			}
		});

		data.on(DataConnection.DataEventEnum.CLOSE, new OnCallback() {
			@Override
			public void onCallback(Object object) {
                // ToDo disconnect process
			}
		});

		data.on(DataConnection.DataEventEnum.ERROR, new OnCallback() {
			@Override
			public void onCallback(Object object) {
				// TODO: DataEvent/ERROR
				PeerError error = (PeerError) object;
				Log.d(TAG, "DataError: " + error);
				String strMessage = error.message;
			}
		});
    }


    private void startLocalStream() throws IllegalStateException {
        if (!this.hasConnection()) {
            throw new IllegalStateException("No peer to start any local stream");
        }
        MediaConstraints constraints = new MediaConstraints();
        _msLocal = this._peer.getLocalMediaStream(constraints);

        // import io.skyway.Peer.Browser.Canvas;
        // Canvas canvas = (Canvas) findViewById(R.id.svSecondary);
        // canvas.addSrc(_msLocal, 0);
    }

    private void receiveCall() {
        synchronized (this) {
            if (!hasConnection()) {
                return;
            }

            this.startLocalStream();
            this._media.answer(_msLocal);
        }
    }

    public void call(String peerId) throws IllegalStateException {
        synchronized (this) {
            if (!this.hasConnection()) {
                throw new IllegalStateException("Already had a connection");
            }

            if (this._peer == null) {
                this.createPeer();
            }


            if (this._media != null) {
                if (this._media.isOpen) {
                    this._media.close();
                    this._media = null;
                }
            }

            this.startLocalStream();
            CallOption option = new CallOption();
            this._media = this._peer.call(peerId, _msLocal, option);
            if (this._media != null) {
                this.setMediaCallbacks();
            }
        }
    }

    public void closeCall() {
        synchronized (this) {
            final MediaConnection media = this._media;
            if (media != null && media.isOpen) {
                media.close();
            }
            this._media = null;

            if (this._msRemote != null) {
                this._msRemote.close();
                this._msRemote = null;
            }

            if (this._msLocal != null) {
                this._msLocal.close();
                this._msLocal = null;
            }
        }
    }

    public void startDataConnection() throws IllegalStateException {
        if (!hasConnection()) {
            throw new IllegalStateException("No peer to start data connection");
        }

        if (this._data == null) {
            createDataConnection();
        }
    }

    private void closeData() {
        // Disconnect the data connection
    }

    public void disconnect() {
        this.closeCall();
        this.closeData();

        synchronized (this) {
            if (this._peer != null) {
                if (!this._peer.isDisconnected) {
                    this._peer.disconnect();
                }

                if (!this._peer.isDestroyed) {
                    this._peer.destroy();
                }
                this._peer = null;
            }
        }
    }

}
