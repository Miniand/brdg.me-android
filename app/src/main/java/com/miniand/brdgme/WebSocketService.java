package com.miniand.brdgme;

import android.app.IntentService;
import android.content.Intent;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import java.util.concurrent.CountDownLatch;

/**
 * Created by beefsack on 14/01/15.
 */
public class WebSocketService extends IntentService
        implements CompletedCallback,
        AsyncHttpClient.WebSocketConnectCallback, WebSocket.StringCallback {
    final CountDownLatch latch = new CountDownLatch(1);

    public WebSocketService() {
        this("WebSocketService");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WebSocketService(String name) {
        super(name);
    }

    @Override
    protected synchronized void onHandleIntent(Intent intent) {
        AsyncHttpClient.getDefaultInstance().websocket("ws://brdg.me/ws", "ws", this);
        try {
            latch.await();
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void onCompleted(Exception ex) {
        if (ex != null) {
            ex.printStackTrace();
            latch.countDown();
        }
    }

    @Override
    public void onCompleted(Exception ex, WebSocket webSocket) {
        if (ex != null) {
            ex.printStackTrace();
            latch.countDown();
            return;
        }
        webSocket.setClosedCallback(this);
        webSocket.setEndCallback(this);
        webSocket.setStringCallback(this);
        webSocket.send("\"ZTVeMZfIJYZtWmjjBxYkxndDfPxvBBCb\"");
    }

    @Override
    public void onStringAvailable(String s) {
    }
}
