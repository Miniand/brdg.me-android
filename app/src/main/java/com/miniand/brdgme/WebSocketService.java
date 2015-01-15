package com.miniand.brdgme;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

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
        AsyncHttpClient.getDefaultInstance().websocket("ws://api.beta.brdg.me/ws", "ws", this);
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
        try {
            JSONObject msg = new JSONObject(s);
            JSONObject data = msg.getJSONObject("data");
            switch (msg.getString("type")) {
                case "gameUpdate":
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(android.R.drawable.stat_notify_chat)
                            .setContentTitle(data.getString("gameName"))
                            .setContentText(data.getString("gameId"));
                    NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    mgr.notify(data.getString("gameId"), 0, mBuilder.build());
            }
        } catch (JSONException e) {
            Log.w("WebSocket", "Non-JSON message: " + s);
        }
    }
}
