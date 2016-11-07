package com.pkiykov.netchess.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.fragments.Auth;
import com.pkiykov.netchess.fragments.Game;
import com.pkiykov.netchess.fragments.PeerToPeerDevicesList;
import com.pkiykov.netchess.pojo.RunningGame;

import static com.pkiykov.netchess.pojo.FinishedGame.REASON_DISCONNECT;

public class WifiP2PReceiver extends BroadcastReceiver {

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private Activity activity;

    public WifiP2PReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Activity activity) {
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state != WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Toast.makeText(activity, activity.getString(R.string.wifi_problem), Toast.LENGTH_SHORT).show();
                    Game gameFragment = (Game) activity.getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
                    if (gameFragment != null) {
                        if ((gameFragment.getWaitingPlayers() != null && gameFragment.getWaitingPlayers().isShowing())
                                || (gameFragment.getRunningGame() != null && gameFragment.getRunningGame().getMoveList().size() == 0)) {
                            gameFragment.getWaitingPlayers().dismiss();
                            gameFragment.getPeer2Peer().disconnect(false);
                            ((GameActivity) activity).fragmentTransaction(new Auth());
                        } else if (gameFragment.getRunningGame() != null) {
                            gameFragment.getGameExtraParams().setReason(REASON_DISCONNECT);
                            gameFragment.getRunningGame().setStatus(gameFragment.getRunningGame().isThisPlayerPlaysWhite()
                                    ? RunningGame.PLAYER_1_DISCONNECTED : RunningGame.PLAYER_2_DISCONNECTED);
                            gameFragment.getGameEnd().cancelDialogs();
                            gameFragment.getGameEnd().recordResult();
                        }
                    }
                }
                break;
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                if (wifiP2pManager != null) {
                    wifiP2pManager.requestPeers(channel, ((GameActivity) activity).getPeer2Peer().getPeerListListener());
                }
                break;
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    wifiP2pManager.requestConnectionInfo(channel,
                            ((GameActivity) activity).getPeer2Peer().getConnectionInfoListener());
                } else {
                    Game gameFragment = (Game) activity.getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
                    if (((GameActivity) activity).getPeer2Peer().getFetchingGameDialog() != null
                            && ((GameActivity) activity).getPeer2Peer().getFetchingGameDialog().isShowing()) {
                        ((GameActivity) activity).getPeer2Peer().getFetchingGameDialog().dismiss();
                        Toast.makeText(activity, activity.getString(R.string.opponent_disconnected), Toast.LENGTH_SHORT).show();
                    } else if (gameFragment != null && gameFragment.getRunningGame() != null && gameFragment.getRunningGame().getStatus() != RunningGame.WAITING_OPPONENT) {
                        if (gameFragment.getRunningGame().getMoveList().size() == 0) {
                            gameFragment.getGameEnd().disconnectFromLanGame();
                            Toast.makeText(activity, activity.getString(R.string.opponent_disconnected), Toast.LENGTH_SHORT).show();
                        } else {
                            if (gameFragment.getRunningGame().isThisPlayerPlaysWhite()) {
                                gameFragment.getRunningGame().setStatus(RunningGame.PLAYER_2_DISCONNECTED);
                            } else {
                                gameFragment.getRunningGame().setStatus(RunningGame.PLAYER_1_DISCONNECTED);
                            }
                            gameFragment.getGameExtraParams().setReason(REASON_DISCONNECT);
                            gameFragment.getGameEnd().cancelDialogs();
                            gameFragment.getGameEnd().recordResult();
                        }
                    }
                }
                break;
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                PeerToPeerDevicesList fragment = (PeerToPeerDevicesList) activity.getFragmentManager()
                        .findFragmentByTag(PeerToPeerDevicesList.class.getSimpleName());
                if (fragment != null) {
                    fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
                }
                break;
        }

    }
}
