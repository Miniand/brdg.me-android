package com.miniand.brdgme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by beefsack on 18/01/15.
 */
public class BoardGame {
    public static final SimpleDateFormat ISO_8601_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public String id;
    public String identifier;
    public String name;

    public Boolean isFinished;
    public Date finishedAt;

    public ArrayList<String> playerList;
    public ArrayList<String> whoseTurn;
    public ArrayList<String> winners;

    public String game;
    public String commands;
    public String log;

    public static ArrayList<String> stringsFromJSONArray(JSONArray a) {
        ArrayList<String> al = new ArrayList<>();
        for (int i = 0; i < a.length(); i++) {
            try {
                al.add(a.getString(i));
            } catch (JSONException ignore) {}
        }
        return al;
    }

    public static BoardGame fromJSONObject(JSONObject json) {
        BoardGame bg = new BoardGame();
        try {
            bg.id = json.getString("id");
        } catch (JSONException ignore) {}
        try {
            bg.identifier = json.getString("identifier");
        } catch (JSONException ignore) {}
        try {
            bg.name = json.getString("name");
        } catch (JSONException ignore) {}
        try {
            bg.isFinished = json.getBoolean("isFinished");
        } catch (JSONException ignore) {}
        try {
            bg.finishedAt = ISO_8601_FORMAT.parse(json.getString("finishedAt"));
        } catch (JSONException | ParseException ignore) {}
        try {
            bg.playerList = stringsFromJSONArray(json.getJSONArray("playerList"));
        } catch (JSONException ignore) {}
        try {
            bg.whoseTurn = stringsFromJSONArray(json.getJSONArray("whoseTurn"));
        } catch (JSONException ignore) {}
        try {
            bg.winners = stringsFromJSONArray(json.getJSONArray("winners"));
        } catch (JSONException ignore) {}
        try {
            bg.game = json.getString("game");
        } catch (JSONException ignore) {}
        try {
            bg.commands = json.getString("commands");
        } catch (JSONException ignore) {}
        try {
            bg.log = json.getString("log");
        } catch (JSONException ignore) {}
        return bg;
    }
}
