package com.miniand.brdgme;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GameContentFragment extends Fragment {
    public static final String ARG_ID = "id";

    private String id;
    private BoardGame boardGame;

    public GameContentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            id = savedInstanceState.getString(ARG_ID);
        } else {
            id = getArguments().getString(ARG_ID);
        }
        View rootView = inflater.inflate(R.layout.fragment_home_game_content, container, false);
        WebView webView = (WebView) rootView.findViewById(R.id.game_render);
        webView.getSettings().setBuiltInZoomControls(true);
        updateGame(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.
                LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void openGame(String id) {
        this.id = id;
        updateGame(getView());
    }

    public void updateGame(View rootView) {
        CodeStringRequest request = new CodeStringRequest(
                Request.Method.GET,
                String.format("https://api.beta.brdg.me/game/%s", id),
                new Response.Listener<CodeStringRequest.CodeString>() {
                    @Override
                    public void onResponse(CodeStringRequest.CodeString response) {
                        if (response.code == 401) {
                            Brdgme.logOut();
                            return;
                        }
                        try {
                            JSONObject json = new JSONObject(response.string);
                            boardGame = BoardGame.fromJSONObject(new JSONObject(response.string));
                            WebView render = (WebView) getView().findViewById(R.id.game_render);
                            Log.v("game", boardGame.game);
                            render.loadDataWithBaseURL("file:///android_asset/", String.format(
                                    getString(R.string.game_layout_html),
                                    boardGame.game).replaceAll("\n", "<br />"),
                                    "text/html", "utf-8", null);
                        } catch (JSONException e) {
                            Toast.makeText(
                                    getActivity(),
                                    "Unable to update game at the moment, please try again later.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(
                                getActivity(),
                                "Unable to update game at the moment, please try again later.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", String.format("token %s", Brdgme.getAuthToken()));
                return headers;
            }
        };
        Brdgme.getRequestQueue().add(request);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_ID, id);
    }
}
