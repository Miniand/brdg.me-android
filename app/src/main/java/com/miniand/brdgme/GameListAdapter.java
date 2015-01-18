package com.miniand.brdgme;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by beefsack on 18/01/15.
 */
public class GameListAdapter extends ArrayAdapter<BoardGame> {
    public GameListAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.game_list_item, parent, false);
        }
        BoardGame bg = getItem(position);

        TextView name = (TextView) convertView.findViewById(R.id.name);
        name.setText(bg.name);

        TextView playerList = (TextView) convertView.findViewById(R.id.player_list);
        playerList.setText(Html.fromHtml(String.format("with %s", TextUtils.join(", ", bg.playerList))));

        return convertView;
    }
}
