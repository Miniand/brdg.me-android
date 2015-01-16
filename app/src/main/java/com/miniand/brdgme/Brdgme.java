package com.miniand.brdgme;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by beefsack on 26/12/14.
 */
public class Brdgme extends Application {
    private static Context applicationContext;
    private static RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
    }

    public static Context getGlobalApplicationContext() {
        return applicationContext;
    }

    public static RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getGlobalApplicationContext());
        }
        return requestQueue;
    }
}
