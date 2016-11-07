package com.pkiykov.netchess;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.pkiykov.netchess.fragments.Auth;
import com.pkiykov.netchess.fragments.Game;
import com.pkiykov.netchess.fragments.OnlineGamesList;
import com.pkiykov.netchess.fragments.Profile;
import com.pkiykov.netchess.fragments.RankList;
import com.pkiykov.netchess.fragments.RankedGameSettings;
import com.pkiykov.netchess.fragments.UnrankedGameSettings;
import com.pkiykov.netchess.logic.Peer2Peer;
import com.pkiykov.netchess.receivers.ConnectionStateChangeReceiver;

import java.util.concurrent.TimeUnit;

import static com.pkiykov.netchess.fragments.Game.GAME_TYPE;
import static com.pkiykov.netchess.fragments.Game.LAN_GAME;
import static com.pkiykov.netchess.fragments.Game.ONE_DEVICE_GAME;

public class GameActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public static final int REFRESH_MENU_ITEM = 142321;
    public static final String THIS_PLAYER_DISCONNECTED_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private ConnectionStateChangeReceiver receiver;
    private Peer2Peer peer2Peer;

    private static boolean isActivityVisible;

    @Override
    protected void onStart() {
        super.onStart();
        isActivityVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityVisible = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        if (receiver == null) {
            initViews();
            mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() == null) {
                fragmentTransaction(new Auth());
            } else {
                fragmentTransaction(new Profile());
            }
        }
    }

    private void initViews() {
        drawerList = (ListView) findViewById(R.id.drawer);
        drawerList.getBackground().setAlpha(70);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundResource(R.drawable.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ArrayAdapter<String> drawerAdapter = new ArrayAdapter<>(this, R.layout.drawer_list, getResources().getStringArray(R.array.titles));
        drawerList.setAdapter(drawerAdapter);
        drawerList.setOnItemClickListener(this);
        drawerList.post(new Runnable() {
            @Override
            public void run() {
                changeStateCurrentGameFragmentItem(false);
            }
        });
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }


            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
    }

    private void gameTypeChoseDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_select_game_type, null);
        builder.setView(layout);
        ImageButton onlineGame = (ImageButton) layout.findViewById(R.id.online_game);
        ImageButton lanGame = (ImageButton) layout.findViewById(R.id.lan_game);
        ImageButton oneDeviceGame = (ImageButton) layout.findViewById(R.id.one_device_game);
        final AlertDialog chooseGameType = builder.create();
        onlineGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAuth.getCurrentUser() != null) {
                    if (!isNetworkAvailable()) {
                        Toast.makeText(GameActivity.this, getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
                    }
                    unfinishedGameDialog(new RankedGameSettings());
                } else {
                    Toast.makeText(GameActivity.this, getResources().getString(R.string.authentication_problem), Toast.LENGTH_SHORT).show();
                    fragmentTransaction(new Auth());
                }
                chooseGameType.dismiss();
                drawerLayout.closeDrawer(drawerList);
            }
        });
        lanGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (peer2Peer == null) {
                    peer2Peer = new Peer2Peer(GameActivity.this, null);
                }
                Bundle bundle = new Bundle();
                bundle.putInt(GAME_TYPE, LAN_GAME);
                Fragment unrankedGameSettings = new UnrankedGameSettings();
                unrankedGameSettings.setArguments(bundle);
                unfinishedGameDialog(unrankedGameSettings);
                chooseGameType.dismiss();
                drawerLayout.closeDrawer(drawerList);
            }
        });
        oneDeviceGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt(GAME_TYPE, ONE_DEVICE_GAME);
                Fragment unrankedGameSettings = new UnrankedGameSettings();
                unrankedGameSettings.setArguments(bundle);
                unfinishedGameDialog(unrankedGameSettings);
                chooseGameType.dismiss();
                drawerLayout.closeDrawer(drawerList);
            }
        });
        chooseGameType.show();
    }

    private void unfinishedGameDialog(final Fragment fragmentToOpen) {
        if (fragmentToOpen.getArguments() == null) {
            Bundle bundle = new Bundle();
            bundle.putInt(GAME_TYPE, ONE_DEVICE_GAME);
            fragmentToOpen.setArguments(bundle);
        }
        final Fragment f = getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
        if (f != null) {
            if (((Game) f).getRunningGame() != null) {
                AlertDialog gameIsNotFinishedDialog = createAlertDialog(GameActivity.this, null, true, getString(R.string.warning),
                        getString(R.string.game_is_not_finished), getString(R.string.yes), getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (((Game) f).getGameType() == Game.ONLINE_GAME) {
                                    ((Game) f).getGameDatabase().removeDatabaseListenersForCurrentGame();
                                    ((Game) f).getGameEnd().finishService();
                                    ((Game) f).getGameEnd().unbindService();
                                    Fragment gamesListFragment = getFragmentManager().findFragmentByTag(OnlineGamesList.class.getSimpleName());
                                    if (gamesListFragment != null) {
                                        ((OnlineGamesList) gamesListFragment).unbindService();
                                    }
                                    waitWithDialog(3);
                                } else if (((Game) f).getGameType() == Game.LAN_GAME) {
                                    getPeer2Peer().disconnect(false);
                                }
                                fragmentTransaction(fragmentToOpen);
                                dialogInterface.dismiss();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                gameIsNotFinishedDialog.show();
            } else {
                fragmentTransaction(fragmentToOpen);
            }
        } else {
            fragmentTransaction(fragmentToOpen);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        selectItem(position);
    }

    public boolean isNetworkAvailable() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void selectItem(int position) {
        switch (position) {
            case 0: {
                Fragment f = getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
                ((Game) f).setBackstack(true);
                fragmentTransaction(f);
                break;
            }
            case 1: {
                gameTypeChoseDialog();
                break;
            }
            case 2:
                userAuthCheckAndFragmentTransaction(new Profile());
                break;
            case 3:
                userAuthCheckAndFragmentTransaction(new RankList());
                break;
            case 4:
                exit();

        }
    }

    public void userAuthCheckAndFragmentTransaction(Fragment fragment) {
        if (mAuth.getCurrentUser() != null) {
            fragmentTransaction(fragment);
        } else {
            fragmentTransaction(new Auth());
            Toast.makeText(this, R.string.authentication_problem, Toast.LENGTH_SHORT).show();
        }
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count < 2) {
            exit();
            return;
        }
        if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof Game) {
            createExitDialog();
            return;
        }
        FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(count - 2);
        String tag = backEntry.getName();
        Fragment f = getFragmentManager().findFragmentByTag(tag);
        if (f != null && f instanceof Game && ((Game) f).getRunningGame() == null) {
            ((Game) f).setBackstack(true);
        }
        removeSameFragment(count, tag);
        super.onBackPressed();
    }

    private void removeSameFragment(int count, String tag) {
        if (getFragmentManager().getBackStackEntryAt(count - 1).getName().equals(tag)) {
            getFragmentManager().popBackStack();
            removeSameFragment(count, tag);
        }
    }

    private void createExitDialog() {
        AlertDialog exitDialog = createAlertDialog(this, null, true, getString(R.string.quit_request), ""
                , getString(R.string.yes), getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Fragment f = getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
                        if (((Game) f).getGameType() == Game.ONLINE_GAME && ((Game) f).getRunningGame() != null) {
                            if (((Game) f).getRunningGame().getMoveList().size() > 0) {
                                ((Game) f).getGameGo().resign();
                            } else {
                                ((Game) f).getGameEnd().leftOnlineGame();
                            }
                        } else if (((Game) f).getGameType() == Game.ONLINE_GAME && ((Game) f).getRunningGame() != null) {
                            if (((Game) f).getRunningGame().getMoveList().size() > 0) {
                                ((Game) f).getGameGo().resign();
                            }
                        }
                        exit();

                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }
        );
        exitDialog.show();
    }

    private void exit() {
        if (peer2Peer != null) {
            peer2Peer.exit();
        } else {
            finish();
            System.exit(1);
        }
    }

    public void waitWithDialog(int secondsToWait) {

        final ProgressDialog pd = ProgressDialog.show(this, "",
                getString(R.string.please_wait), true);
        pd.show();
        new AsyncTask<Integer, Integer, String>() {

            @Override
            protected String doInBackground(Integer[] objects) {
                try {
                    TimeUnit.SECONDS.sleep(objects[0]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String o) {
                super.onPostExecute(o);
                pd.dismiss();
            }
        }.execute(secondsToWait);
    }


    public void createNotification(Context context, String message, String title, int id) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.app_icon)
                .setContentText(message)
                .setContentTitle(title)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .setSound(alarmSound);
        Notification n = builder.build();
        nm.notify(id, n);
    }

    public void fragmentTransaction(Fragment fragment) {
        if ((fragment instanceof UnrankedGameSettings || fragment instanceof RankedGameSettings)) {
            drawerList.post(new Runnable() {
                @Override
                public void run() {
                    changeStateCurrentGameFragmentItem(false);
                }
            });
            Fragment f = getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
            if (f != null) {
                if (((Game) f).getRunningGame() != null) {
                    ((Game) f).setRunningGame(null);
                    changeStateCurrentGameFragmentItem(false);
                }
                if (peer2Peer != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            peer2Peer.disconnect(false);
                        }
                    }).start();
                }
            }
        }
        String fragmentName = fragment.getClass().getSimpleName();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment, fragmentName);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        ft.addToBackStack(fragmentName);
        ft.commit();
        drawerLayout.closeDrawer(drawerList);
        hideKeyboard();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public AlertDialog createAlertDialog(final Context context, View view, boolean cancelable, String title, String message, String positiveBtn, String negativeBtn, DialogInterface.OnClickListener positive, DialogInterface.OnClickListener negative) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(GameActivity.this);
        if (!title.isEmpty()) {
            dialogBuilder.setTitle(title);
        }
        if (!message.isEmpty()) {
            dialogBuilder.setMessage(message);
        }
        if (view != null) {
            dialogBuilder.setView(view);
        }
        dialogBuilder.setPositiveButton(positiveBtn, positive);
        dialogBuilder.setNegativeButton(negativeBtn, negative);
        AlertDialog b = dialogBuilder.create();
        b.setCanceledOnTouchOutside(false);
        b.setCancelable(cancelable);
        b.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Window view = ((AlertDialog) dialogInterface).getWindow();
                TypedValue outValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                view.setBackgroundDrawableResource(R.drawable.dialog_background);
                view.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                Button positiveButton = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setBackgroundResource(outValue.resourceId);
                Button negativeButton = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE);
                negativeButton.setBackgroundResource(outValue.resourceId);
                positiveButton.setTextColor(Color.parseColor("#FFFFFF"));
                negativeButton.setTextColor(Color.parseColor("#FFFFFF"));
                positiveButton.invalidate();
                negativeButton.invalidate();
            }
        });
        return b;
    }

    public ListView getDrawerList() {
        return drawerList;
    }

    public static boolean isActivityVisible() {
        return isActivityVisible;
    }

    public void changeStateCurrentGameFragmentItem(boolean visible) {
        if (visible) {
            getDrawerList().getChildAt(0).setAlpha(1);
        } else {
            getDrawerList().getChildAt(0).setAlpha(0.5F);
        }
        drawerList.getChildAt(0).setClickable(!visible);
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public Peer2Peer getPeer2Peer() {
        return peer2Peer;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (peer2Peer != null) {
            peer2Peer.registerP2PReceiver();
        }
        if (receiver == null) {
            receiver = new ConnectionStateChangeReceiver(this);
            registerReceiver(receiver, new IntentFilter(THIS_PLAYER_DISCONNECTED_ACTION));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (peer2Peer != null) {
            peer2Peer.unregisterP2PReceiver();
        }
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }


}

