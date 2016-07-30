package com.ishinomakihackathon2016.bluespring.skyway;

import io.skyway.Peer.MediaConnection;

/**
 * Created by adam on 16/07/30.
 */
public interface SkyWayPeerEventListener {
    public void OnOpen(String peerId);
    public void OnCall(MediaConnection o);
    public void OnConnection(Object o);
    public void OnClose(Object o);
    public void OnDisconnected(Object o);
    public void OnError(Object o);
}
