package com.ishinomakihackathon2016.bluespring.skyway;

/**
 * Created by adam on 16/07/31.
 */
public interface SkyWayDataEventListener {
    public void OnOpen(Object o);
    public void OnData(Object o);
    public void OnClose(Object o);
    public void OnError(Object o);
}
