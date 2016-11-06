package com.pkiykov.netchess.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.adapters.PlayersListAdapter;
import com.pkiykov.netchess.others.FirebaseHelper;
import com.pkiykov.netchess.pojo.Player;
import com.pkiykov.netchess.pojo.PlayerGameParams;
import com.pkiykov.netchess.pojo.RunningGame;
import com.pkiykov.netchess.services.OnlineCheckPlayerService;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class OnlineGamesList extends Fragment {

    private PlayersListAdapter adapter;
    private RecyclerView recycler;
    private ArrayList<RunningGame> runningGamesList;
    private ArrayList<Player> playersList;
    private ProgressDialog pdWaiting;
    private String gameId;
    private Player player;
    private long timestamp;
    private DatabaseReference gamesRef;
    private FirebaseHelper gamesListDBHelper, timestampDBHeler;
    private MenuItem refresh;
    private OnlineCheckPlayerService mService;
    private ServiceConnection mConnection;

    @Override
    public void onResume() {
        super.onResume();
        createAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                OnlineCheckPlayerService.LocalBinder binder = (OnlineCheckPlayerService.LocalBinder) iBinder;
                mService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        recycler = (RecyclerView) inflater.inflate(R.layout.fragment_recycler_list, container, false);
        runningGamesList = new ArrayList<>();
        playersList = new ArrayList<>();
        player = (Player) this.getArguments().getSerializable(Player.PLAYERS);
        gamesRef = FirebaseDatabase.getInstance().getReference().child(RunningGame.RUNNING_GAME);
        startTimestampUpdate();
        retreiveData();
        ((GameActivity) getActivity()).getToolbar().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                refresh();
                return false;
            }
        });
        return recycler;
    }

    private void refresh() {
        View v = getActivity().findViewById(refresh.getItemId());
        Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotation);
        v.startAnimation(rotation);
        runningGamesList.clear();
        playersList.clear();
        adapter.notifyDataSetChanged();
        startTimestampUpdate();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        refresh = menu.add(0, GameActivity.REFRESH_MENU_ITEM, 0, getActivity().getString(R.string.refresh)).setIcon(R.drawable.flip_board);
        refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    private void startTimestampUpdate() {
        timestampDBHeler = new FirebaseHelper(FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset"), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double offset = dataSnapshot.getValue(Double.class);
                timestamp = (long) (System.currentTimeMillis() + offset);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void unbindService() {
        try {
            getActivity().unbindService(mConnection);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public void onStop() {
        if (timestampDBHeler != null) {
            timestampDBHeler.getRef().removeEventListener(timestampDBHeler.getValueEventListener());
        }
        if (gamesListDBHelper != null) {
            gamesListDBHelper.getRef().removeEventListener(gamesListDBHelper.getChildEventListener());
        }
        super.onStop();
    }

    private void retreiveData() {
        gamesListDBHelper = new FirebaseHelper(gamesRef, new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (gameIsInvalid(dataSnapshot.getValue(RunningGame.class))) {
                    gamesRef.child(dataSnapshot.getKey()).removeValue();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                RunningGame dbGame = dataSnapshot.getValue(RunningGame.class);
                if (dbGame.getId() != null) {
                    if (dbGame.getId().equals(gameId)) {
                        if (dbGame.getPlayer1() != null && dbGame.getPlayer2() != null) {
                            if (playersIdEquals(dbGame) && dbGame.getStatus() == RunningGame.RUNNING) {
                                timestampDBHeler.getRef().removeEventListener(timestampDBHeler.getValueEventListener());
                                gamesListDBHelper.getRef().removeEventListener(gamesListDBHelper.getChildEventListener());
                                Fragment fragment = new Game();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(RunningGame.RUNNING_GAME, dbGame);
                                bundle.putInt(Game.GAME_TYPE, Game.ONLINE_GAME);
                                fragment.setArguments(bundle);
                                ((GameActivity) getActivity()).fragmentTransaction(fragment);
                                pdWaiting.dismiss();
                            } else if (!playersIdEquals(dbGame)) {
                                mService.setFlag(false);
                                unbindService();
                                gameId = "";
                                pdWaiting.dismiss();
                                removeFromList(dbGame);
                            }
                        }
                    } else if (dbGame.getPlayer1() != null && dbGame.getPlayer2() != null) {
                        removeFromList(dbGame);
                    } else {
                        removeDuplicateGame(dbGame);
                        addGameToList(dbGame);
                    }
                } else if (dbGame.getPlayer1() == null && dbGame.getPlayer2() == null) {
                    gamesRef.child(dataSnapshot.getKey()).removeValue();
                    removeFromList(dbGame);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getKey().equals(gameId)) {
                    disconnectFromGame(false);
                } else if (dataSnapshot.getValue(RunningGame.class) != null) {
                    for (int i = 0; i < runningGamesList.size(); i++) {
                        if (dataSnapshot.getKey().equals(runningGamesList.get(i).getId())) {
                            runningGamesList.remove(i);
                            playersList.remove(i);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void disconnectFromGame(final boolean autoCancel) {
        gameId = "";
        getActivity().stopService(new Intent(getActivity(), OnlineCheckPlayerService.class));
        if (mService != null) {
            mService.setFlag(false);
            unbindService();
        }
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
                pdWaiting.dismiss();
                if(!autoCancel) {
                    Toast.makeText(getActivity(), getString(R.string.participation_declined), Toast.LENGTH_SHORT).show();
                }
                refresh();
            }
        }.execute(3);
    }

    private void removeDuplicateGame(RunningGame dbGame) {
        if (runningGamesList.size() > 0) {
            for (int i = 0; i < runningGamesList.size(); i++) {
                if (dbGame.getPlayer1() != null && runningGamesList.get(i).getPlayer1() != null) {
                    if (runningGamesList.get(i).getPlayer1().getId().equals(dbGame.getPlayer1().getId())) {
                        runningGamesList.remove(i);
                        playersList.remove(i);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
                if (dbGame.getPlayer2() != null && runningGamesList.get(i).getPlayer2() != null) {
                    if (runningGamesList.get(i).getPlayer2().getId().equals(dbGame.getPlayer2().getId())) {
                        runningGamesList.remove(i);
                        playersList.remove(i);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    }

    private boolean playersIdEquals(RunningGame dbGame) {
        return dbGame.getPlayer1().getId().equals(player.getId()) ^ dbGame.getPlayer2().getId().equals(player.getId());
    }

    private boolean gameIsInvalid(RunningGame gameFromList) {
        long res;
        if (gameFromList.getPlayer1() != null) {
            res = timestamp - gameFromList.getPlayer1().getTimestamp();
        } else {
            if (gameFromList.getPlayer2() != null) {
                res = timestamp - gameFromList.getPlayer2().getTimestamp();
            } else {
                return true;
            }
        }
        if (res > 10000) {
            if (gameFromList.getTimestamp() != null) {
                if (timestamp - gameFromList.getTimestampCreatedLong() > 10000) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private void removeFromList(RunningGame dbGame) {
        if (dbGame.getId() != null) {
            if (runningGamesList.size() > 0) {
                for (int i = 0; i < runningGamesList.size(); i++) {
                    if (runningGamesList.get(i).getId().equals(dbGame.getId())) {
                        runningGamesList.remove(i);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    }

    private void addGameToList(RunningGame gameFromList) {
        if ((gameFromList.getPlayer1() == null ^ gameFromList.getPlayer2() == null)) {
            if (runningGamesList.size() > 0) {
                for (int i = 0; i < runningGamesList.size(); i++) {
                    if (runningGamesList.get(i).getId().equals(gameFromList.getId())) {
                        return;
                    }
                }
            }
            int number = 0;
            if (runningGamesList.size() < 1) {
                runningGamesList.add(gameFromList);
            } else {
                int ratingThisPlayer = player.getRating();
                int ratingGameToAdd;
                if (gameFromList.getPlayer1() != null) {
                    ratingGameToAdd = gameFromList.getPlayer1().getRating();
                } else {
                    ratingGameToAdd = gameFromList.getPlayer2().getRating();
                }

                int rating;
                if (runningGamesList.get(0).getPlayer1() != null) {
                    rating = runningGamesList.get(0).getPlayer1().getRating();
                } else {
                    rating = runningGamesList.get(0).getPlayer2().getRating();
                }
                int difference = Math.abs(ratingGameToAdd - ratingThisPlayer);
                int minDeviation = Math.abs(rating - ratingThisPlayer);
                int resStart = Math.abs(difference - minDeviation);
                for (int i = 0; i < runningGamesList.size() - 1; i++) {
                    if (runningGamesList.get(i).getPlayer1() != null) {
                        rating = runningGamesList.get(i).getPlayer1().getRating();
                    } else {
                        rating = runningGamesList.get(i).getPlayer2().getRating();
                    }
                    minDeviation = Math.abs(rating - ratingThisPlayer);
                    int resNext = Math.abs(difference - minDeviation);
                    if (resNext < resStart) {
                        number = i;
                    }
                }
                runningGamesList.add(number, gameFromList);
            }
            Player player;
            if (runningGamesList.get(number).getPlayer1() != null) {
                player = runningGamesList.get(number).getPlayer1();
                player.setColor(true);

            } else {
                player = runningGamesList.get(number).getPlayer2();
                player.setColor(false);
            }
            playersList.add(player);
            adapter.notifyDataSetChanged();
        }
    }

    private void createAdapter() {
        adapter = new PlayersListAdapter(playersList, getActivity(), false);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        adapter.setListener(new PlayersListAdapter.Listener() {

            @Override
            public void onClick(final int position) {
                gameId = runningGamesList.get(position).getId();
                if (gameIsInvalid(runningGamesList.get(position))) {
                    gamesRef.child(runningGamesList.get(position).getId()).removeValue();
                    removeFromList(runningGamesList.get(position));
                    Toast.makeText(getActivity(), getString(R.string.selected_game_is_not_available), Toast.LENGTH_SHORT).show();
                    gameId = "";
                    return;
                }
                Intent intent = new Intent(getActivity(), OnlineCheckPlayerService.class);
                intent.putExtra(OnlineCheckPlayerService.GAME_ID, gameId);
                final DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference().child(RunningGame.RUNNING_GAME).child(gameId);
                pdWaiting = new ProgressDialog(getActivity());

                final boolean flag;
                PlayerGameParams playerGameParams;
                if (runningGamesList.get(position).getPlayer1() == null) {
                    intent.putExtra(OnlineCheckPlayerService.COLOR, true);
                    flag = true;
                    playerGameParams = runningGamesList.get(position).getPlayer2().getPlayerGameParams();
                    player.setPlayerGameParams(playerGameParams);
                    gameRef.child(RunningGame.PLAYER_1).setValue(player);
                } else {
                    intent.putExtra(OnlineCheckPlayerService.COLOR, false);
                    flag = false;
                    playerGameParams = runningGamesList.get(position).getPlayer1().getPlayerGameParams();
                    player.setPlayerGameParams(playerGameParams);
                    gameRef.child(RunningGame.PLAYER_2).setValue(player);
                }
                getActivity().bindService(new Intent(getActivity(), OnlineCheckPlayerService.class), mConnection, Context.BIND_AUTO_CREATE);
                getActivity().startService(intent);
                pdWaiting.setMessage(getString(R.string.waiting_for_invite_response));
                pdWaiting.setCancelable(true);
                pdWaiting.setCanceledOnTouchOutside(false);
                pdWaiting.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        disconnectFromGame(true);
                        if (flag) {
                            gameRef.child(RunningGame.PLAYER_1).removeValue();
                        } else {
                            gameRef.child(RunningGame.PLAYER_2).removeValue();
                        }
                    }
                });
                pdWaiting.show();
            }
        });
    }
}

