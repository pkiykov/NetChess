package com.pkiykov.netchess.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.fragments.Game;
import com.pkiykov.netchess.R;

public class OpponentDisconnectReceiver extends BroadcastReceiver {
    public static final String OPPONENT_DISCONNECT_ACTION = "com.pkiykov.netchess.playerDisconnect";
    private Game game;

    public OpponentDisconnectReceiver(Game game) {
        this.game = game;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (game.getRunningGame() != null) {
            if (!GameActivity.isActivityVisible()) {
                String message = context.getString(R.string.opponent_disconnected);
                String title = context.getString(R.string.game_over);
                ((GameActivity) game.getActivity()).createNotification(context, message, title, 1234);
            }
            game.getGameEnd().onOpponentDisconnect();
        }
    }
}


