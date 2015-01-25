package com.miniand.brdgme;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameListFragment extends Fragment {
    private GameListAdapter currentTurnAdapter;
    private GameListAdapter recentlyFinishedAdapter;

    public static interface OnGameClickListener {
        public void onGameClick(BoardGame boardGame);
    }

    private OnGameClickListener onGameClickListener;

    public GameListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_game_list, container, false);

        final ListView currentTurnList = (ListView) rootView.findViewById(R.id.current_turn_list);
        currentTurnAdapter = new GameListAdapter(getActivity(), 0);
        currentTurnList.setAdapter(currentTurnAdapter);
        currentTurnList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (onGameClickListener != null) {
                    onGameClickListener.onGameClick(currentTurnAdapter.getItem(position));
                }
            }
        });

        ListView recentlyFinishedList = (ListView) rootView.findViewById(R.id.recently_finished_list);
        recentlyFinishedAdapter = new GameListAdapter(getActivity(), 0);
        recentlyFinishedList.setAdapter(recentlyFinishedAdapter);
        recentlyFinishedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (onGameClickListener != null) {
                    onGameClickListener.onGameClick(recentlyFinishedAdapter.getItem(position));
                }
            }
        });

        update(rootView);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        onGameClickListener = (OnGameClickListener) activity;
    }

    public void update() {
        View rootView = getView();
        if (rootView == null) return;
        update(rootView);
    }

    public void update(final View rootView) {
        CodeStringRequest request = new CodeStringRequest(
                Request.Method.GET,
                "https://api.beta.brdg.me/game/summary",
                new Response.Listener<CodeStringRequest.CodeString>() {
                    @Override
                    public void onResponse(CodeStringRequest.CodeString response) {
                        if (response.code == 401) {
                            Brdgme.logOut();
                            return;
                        }
                        try {
                            JSONObject json = new JSONObject(response.string);
                            ArrayList<BoardGame> currentTurn = BoardGame.fromJSONArray(
                                    json.getJSONArray("currentTurn")
                            );
                            currentTurnAdapter.clear();
                            currentTurnAdapter.addAll(currentTurn);
                            ArrayList<BoardGame> recentlyFinished = BoardGame.fromJSONArray(
                                    json.getJSONArray("recentlyFinished")
                            );
                            recentlyFinishedAdapter.clear();
                            recentlyFinishedAdapter.addAll(recentlyFinished);
                        } catch (JSONException e) {
                            Toast.makeText(
                                    getActivity(),
                                    "Unable to update the game list at the moment, please try again later.",
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
                                "Unable to update the game list at the moment, please try again later.",
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
}
