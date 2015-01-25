package com.miniand.brdgme;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class GameActivity extends ActionBarActivity implements GameListFragment.OnGameClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);
        if (savedInstanceState == null) {
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.add(R.id.container, new GameListFragment());
            if ((getResources().getConfiguration().screenLayout &
                    Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
                trans.add(R.id.container, new GameContentFragment());
            }
            trans.commit();
        }

        /*Intent webSocketIntent = new Intent(this, WebSocketService.class);
        startService(webSocketIntent);*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGameClick(BoardGame boardGame) {
        GameContentFragment contentFrag = (GameContentFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_game_content);
        if (contentFrag != null) {
            contentFrag.openGame(boardGame.id);
        } else {
            contentFrag = new GameContentFragment();
            Bundle args = new Bundle();
            args.putString(GameContentFragment.ARG_ID, boardGame.id);
            contentFrag.setArguments(args);

            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.replace(R.id.container, contentFrag);
            trans.addToBackStack(null);
            trans.commit();
        }
    }
}
