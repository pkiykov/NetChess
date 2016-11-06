
package com.pkiykov.netchess.others;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.fragments.Game;
import com.pkiykov.netchess.logic.ReceivedFromOpponent;
import com.pkiykov.netchess.pojo.Coordinates;
import com.pkiykov.netchess.pojo.RunningGame;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

import static com.pkiykov.netchess.async_tasks.Peer2PeerSockets.INET_ADRESS;
import static com.pkiykov.netchess.fragments.Game.GAME_TYPE;
import static com.pkiykov.netchess.fragments.Game.LAN_GAME;
import static com.pkiykov.netchess.logic.Peer2Peer.USER_INFO;
import static com.pkiykov.netchess.pojo.Coordinates.COORDINATES;
import static com.pkiykov.netchess.pojo.Player.TIMESTAMP;
import static com.pkiykov.netchess.pojo.RunningGame.CHAT_LOGS;
import static com.pkiykov.netchess.pojo.RunningGame.GAME_STATUS;
import static com.pkiykov.netchess.pojo.RunningGame.RUNNING;
import static com.pkiykov.netchess.pojo.RunningGame.RUNNING_GAME;

public class ConnectionPeer2Peer implements Runnable {

    private Activity activity;
    private Socket socket;

    public ConnectionPeer2Peer(Activity activity, Socket socket) {
        this.activity = activity;
        this.socket = socket;
    }

    public void run() {
        try {
            final Object o = new ObjectInputStream(new DataInputStream(socket.getInputStream())).readObject();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    useReceivedObject(o);
                }
            });
        } catch (IOException e) {
            Log.d("MyTag", e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void useReceivedObject(Object o) {
        Map map = (Map) o;
        Game gameFragment = (Game) activity.getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
        ReceivedFromOpponent receivedFromOpponent;
        if (gameFragment == null) {
            receivedFromOpponent = ((GameActivity) activity).getPeer2Peer().getReceivedFromOpponent();
        } else {
            receivedFromOpponent = gameFragment.getPeer2Peer().getReceivedFromOpponent();
        }
        if (map.get(CHAT_LOGS) != null) {
            Object object = map.get(CHAT_LOGS);
            @SuppressWarnings("unchecked")
            ArrayList<String> list = (ArrayList<String>) object;
            String message = list.get(list.size() - 1);
            receivedFromOpponent.onChatLogsUpdated(message);
        } else if (map.get(COORDINATES) != null) {
            Coordinates coordinates = (Coordinates) map.get(COORDINATES);
            receivedFromOpponent.setMoveFromOpponent(true);
            receivedFromOpponent.onCoordinatesUpdated(coordinates);
        } else if (map.get(GAME_STATUS) != null) {
            int status = (int) map.get(GAME_STATUS);
            receivedFromOpponent.onGameStatusUpdated(status);
        } else if (map.get(TIMESTAMP) != null) {
            long opponentTimeRest = (long) map.get(TIMESTAMP);
            receivedFromOpponent.onOpponentTimeUpdated(opponentTimeRest);
        } else if (map.get(INET_ADRESS) != null) {
            String inetAddressString = (String) map.get(INET_ADRESS);
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getByName(inetAddressString);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if (((GameActivity) activity).getPeer2Peer().getHostIneptAddress() == null) {
                ((GameActivity) activity).getPeer2Peer().setHostIneptAddress(inetAddress);
                ((GameActivity) activity).getPeer2Peer().sendGame();
            }
        } else if (map.get(USER_INFO) != null) {
            Object object = map.get(USER_INFO);
            @SuppressWarnings("unchecked")
            Map<String, String> userInfoMap = (Map<String, String>) object;
            receivedFromOpponent.setOpponentProfileInfo(userInfoMap);
        } else {
            RunningGame game = (RunningGame) map.get(RUNNING_GAME);
            game.setStatus(RUNNING);
            Bundle bundle = new Bundle();
            bundle.putInt(GAME_TYPE, LAN_GAME);
            bundle.putSerializable(RUNNING_GAME, game);
            Fragment fragment = new Game();
            fragment.setArguments(bundle);
            ((GameActivity) activity).fragmentTransaction(fragment);
            ((GameActivity) activity).getPeer2Peer().getFetchingGameDialog().dismiss();
            ((GameActivity) activity).getPeer2Peer().sendAction(GAME_STATUS, game.getStatus());
        }
    }
}

