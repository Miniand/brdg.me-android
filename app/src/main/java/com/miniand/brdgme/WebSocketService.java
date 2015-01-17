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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

/**
 * Created by beefsack on 14/01/15.
 */
public class WebSocketService extends IntentService
        implements CompletedCallback,
        AsyncHttpClient.WebSocketConnectCallback, WebSocket.StringCallback {
    public static final Intent INTENT = new Intent(
            Brdgme.getGlobalApplicationContext(),
            WebSocketService.class
    );

    private final CountDownLatch latch = new CountDownLatch(1);

    private WebSocket webSocket;
    private Timer restartTimer;

    public static void stop() {
        Log.v("WebSocket", "stop");
        Brdgme.getGlobalApplicationContext().stopService(INTENT);
    }

    public static void start() {
        Log.v("WebSocket", "start");
        stop();
        Brdgme.getGlobalApplicationContext().startService(INTENT);
    }

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
        Log.v("WebSocket", "onHandleIntent");
        String token = Brdgme.getAuthToken();
        if (token.isEmpty()) {
            return;
        }
        AsyncHttpClient.getDefaultInstance().websocket("ws://api.beta.brdg.me/ws", "ws", this);
        try {
            latch.await();
        } catch (InterruptedException ignored) {}
    }

    private void cancelRestart() {
        Log.v("WebSocket", "cancelRestart");
        if (restartTimer != null) {
            restartTimer.cancel();
            restartTimer = null;
        }
    }

    public void restartLater() {
        Log.v("WebSocket", "restartLater");
        cancelRestart();
        restartTimer = new Timer();
        restartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                start();
            }
        }, 60*1000);
    }

    // Closed callback.
    @Override
    public void onCompleted(Exception ex) {
        Log.v("WebSocket", "onCompleted for closed");
        if (ex != null) {
            Log.v("WebSocket", "onCompleted error");
            ex.printStackTrace();
        }
        restartLater();
        latch.countDown();
    }

    // Connect callback.
    @Override
    public void onCompleted(Exception ex, WebSocket webSocket) {
        Log.v("WebSocket", "onCompleted for connection");
        this.webSocket = webSocket;
        String token = Brdgme.getAuthToken();
        if (token.isEmpty()) {
            latch.countDown();
            return;
        }
        if (ex != null) {
            ex.printStackTrace();
            latch.countDown();
            restartLater();
            return;
        }
        Log.v("WebSocket", "connected, authenticating");
        webSocket.setClosedCallback(this);
        webSocket.setStringCallback(this);
        webSocket.send(String.format("\"%s\"", token));
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

    @Override
    public void onDestroy() {
        Log.v("WebSocket", "onDestroy");
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.close();
            cancelRestart();
        }
        webSocket = null;
        super.onDestroy();
    }
}
