package com.pkiykov.netchess.receivers;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.fragments.Auth;
import com.pkiykov.netchess.fragments.Game;
import com.pkiykov.netchess.fragments.OnlineGamesList;
import com.pkiykov.netchess.fragments.PeerToPeerDevicesList;
import com.pkiykov.netchess.fragments.RankList;
import com.pkiykov.netchess.fragments.RankedGameSettings;
import com.pkiykov.netchess.fragments.Registration;
import com.pkiykov.netchess.async_tasks.OnDisconnect;
import com.pkiykov.netchess.fragments.UnrankedGameSettings;
import com.pkiykov.netchess.util.NetworkUtil;

import static com.pkiykov.netchess.util.NetworkUtil.TYPE_WIFI;
import static com.pkiykov.netchess.util.NetworkUtil.getNetworkStatusNotConnected;

public class ConnectionStateChangeReceiver extends BroadcastReceiver {
    private GameActivity activity;
    private OnDisconnect onDisconnectTask;

    public ConnectionStateChangeReceiver(GameActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Fragment currentFragment;
        int count = activity.getFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            FragmentManager.BackStackEntry backEntry = activity.getFragmentManager().getBackStackEntryAt(count - 1);

            if (NetworkUtil.getConnectivityStatusString(context) == getNetworkStatusNotConnected()) {
                if (GameActivity.isActivityVisible()) {
                    Toast.makeText(activity, activity.getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
                } else {
                    String message = context.getString(R.string.this_disconnected);
                    String title = context.getString(R.string.connection_state_changed);
                    activity.createNotification(context, message, title, 1);
                }
                if (backEntry.getName().equals(RankedGameSettings.class.getSimpleName())) {
                    currentFragment = activity.getFragmentManager().findFragmentByTag(RankedGameSettings.class.getSimpleName());
                    ((RankedGameSettings) currentFragment).disableStart();
                    return;
                }
                if(backEntry.getName().equals(UnrankedGameSettings.class.getSimpleName())){
                    currentFragment = activity.getFragmentManager().findFragmentByTag(UnrankedGameSettings.class.getSimpleName());
                    ((UnrankedGameSettings) currentFragment).disableStart();
                    return;
                }
                if (backEntry.getName().equals(PeerToPeerDevicesList.class.getSimpleName())) {
                    activity.fragmentTransaction(new Auth());
                    return;
                }
                if (backEntry.getName().equals(OnlineGamesList.class.getSimpleName())) {
                    activity.fragmentTransaction(new Auth());
                    return;
                }
                if (backEntry.getName().equals(Registration.class.getSimpleName())) {
                    activity.fragmentTransaction(new Auth());
                    return;
                }
                if (backEntry.getName().equals(RankList.class.getSimpleName())) {
                    activity.fragmentTransaction(new Auth());
                    return;
                }

                Game f = (Game) activity.getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
                if (f != null && f.getRunningGame() != null) {
                    if (f.getGameType() == Game.ONLINE_GAME ) {
                        if (onDisconnectTask == null) {
                            onDisconnectTask = new OnDisconnect(activity);
                            final int SECONDS_10 = 10;
                            onDisconnectTask.execute(SECONDS_10);
                            onDisconnectTask = null;
                        }
                    }else if (f.getGameType() == Game.LAN_GAME ){
                        f.getGameEnd().disconnectFromLanGame();
                    }
                }
            } else {
                if (onDisconnectTask != null) {
                    onDisconnectTask.cancel(true);
                }

                if (backEntry.getName().equals(RankedGameSettings.class.getSimpleName())) {
                    currentFragment = activity.getFragmentManager().findFragmentByTag(RankedGameSettings.class.getSimpleName());
                    ((RankedGameSettings) currentFragment).createPlayerWithDatabase();
                    return;
                }
                if(backEntry.getName().equals(UnrankedGameSettings.class.getSimpleName())
                        && NetworkUtil.getConnectivityStatusString(activity) == TYPE_WIFI){
                    currentFragment = activity.getFragmentManager().findFragmentByTag(UnrankedGameSettings.class.getSimpleName());
                    ((UnrankedGameSettings) currentFragment).enableStart();
                }
            }
        }
    }
}
