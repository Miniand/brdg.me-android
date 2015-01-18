package com.miniand.brdgme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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


public class HomeActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_left, new GameListFragment())
                    .add(R.id.container_right, new GameContentFragment())
                    .commit();
        }
        SlidingPaneLayout layout = (SlidingPaneLayout) findViewById(R.id.container);
        layout.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {}

            @Override
            public void onPanelOpened(View panel) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar == null) return;

                actionBar.setHomeButtonEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(false);
            }

            @Override
            public void onPanelClosed(View panel) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar == null) return;

                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        });
        layout.openPane();

        Intent webSocketIntent = new Intent(this, WebSocketService.class);
        startService(webSocketIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == android.R.id.home) {
            SlidingPaneLayout layout = (SlidingPaneLayout) findViewById(R.id.container);
            layout.openPane();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class GameListFragment extends Fragment {
        private GameListAdapter currentTurnAdapter;
        private GameListAdapter recentlyFinishedAdapter;

        public GameListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home_game_list, container, false);

            ListView currentTurnList = (ListView) rootView.findViewById(R.id.current_turn_list);
            currentTurnAdapter = new GameListAdapter(getActivity(), 0);
            currentTurnList.setAdapter(currentTurnAdapter);
            ListView recentlyFinishedList = (ListView) rootView.findViewById(R.id.recently_finished_list);
            recentlyFinishedAdapter = new GameListAdapter(getActivity(), 0);
            recentlyFinishedList.setAdapter(recentlyFinishedAdapter);

            update(rootView);
            return rootView;
        }

        public void update() {
            View rootView = getView();
            if (rootView == null) return;
            update(rootView);
        }

        public void update(final View rootView) {
            CodeStringRequest request = new CodeStringRequest(
                    Request.Method.GET,
                    "http://api.beta.brdg.me/game/summary",
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

    public static class GameContentFragment extends Fragment {

        public GameContentFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home_game_content, container, false);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().getWindow().setSoftInputMode(WindowManager.
                    LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);        }
    }
}
