package com.miniand.brdgme;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by beefsack on 26/12/14.
 */
public class Brdgme extends Application {
    public static final String PREF_EMAIL = "email";
    public static final String PREF_TOKEN = "token";
    public static final String AUTH_PREFS = "com.miniand.brdgme.auth_preferences";

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

    public static SharedPreferences getAuthPrefs() {
        return getGlobalApplicationContext().getSharedPreferences(AUTH_PREFS, MODE_PRIVATE);
    }

    public static void clearAuth() {
        getAuthPrefs().edit().clear().apply();
    }

    public static void storeAuth(String email, String token) {
        getAuthPrefs().edit()
                .putString(PREF_EMAIL, email)
                .putString(PREF_TOKEN, token)
                .apply();
    }

    public static String getAuthEmail() {
        return getAuthPrefs().getString(PREF_EMAIL, "");
    }

    public static String getAuthToken() {
        return getAuthPrefs().getString(PREF_TOKEN, "");
    }

    public static void logOut() {
        clearAuth();
        Intent intent = new Intent(getGlobalApplicationContext(), AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getGlobalApplicationContext().startActivity(intent);
    }
}
