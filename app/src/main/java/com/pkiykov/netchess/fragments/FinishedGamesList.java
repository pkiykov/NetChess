package com.pkiykov.netchess.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.adapters.FinishedGamesListAdapter;
import com.pkiykov.netchess.pojo.FinishedGame;
import com.pkiykov.netchess.pojo.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FinishedGamesList extends Fragment {
    public static final String LIST_POSITION = "list position";
    private SearchView sv;
    private ArrayList<FinishedGame> finishedGamesList;
    private RecyclerView recycler;
    private MenuItem refresh;
    private FinishedGamesListAdapter adapter;
    private DatabaseReference finishedGamesRef;
    private Query query;
    private ChildEventListener listener;
    private String playerId;
    private int showedGamesType;
    private boolean playerColor;
    private boolean showSearchView;
    private ProgressDialog pleaseWaitDialog;
    private ExecutorService pool;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        showSearchView = true;
        recycler = (RecyclerView) inflater.inflate(R.layout.fragment_recycler_list, container, false);
        sv = new SearchView(getActivity());
        playerId = getArguments().getString(Player.PLAYER_ID);
        showedGamesType = getArguments().getInt(FinishedGamesList.LIST_POSITION);
        finishedGamesList = new ArrayList<>();
        finishedGamesRef = FirebaseDatabase.getInstance().getReference().child(FinishedGame.FINISHED_GAME);
        pleaseWaitDialog = ProgressDialog.show(getActivity(), "", getString(R.string.please_wait), false);
        retreiveData();
        ((GameActivity) getActivity()).getToolbar().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == GameActivity.REFRESH_MENU_ITEM) {
                    View v = getActivity().findViewById(refresh.getItemId());
                    Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotation);
                    v.startAnimation(rotation);
                    finishedGamesList.clear();
                    createAdapter(finishedGamesList);
                    retreiveData();
                    adapter.notifyDataSetChanged();
                }
                return false;
            }
        });
        return recycler;
    }

    private void retreiveData() {
        pool = Executors.newFixedThreadPool(5);
        listener = new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                pool.execute(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FinishedGame finishedGame = dataSnapshot.getValue(FinishedGame.class);
                        if (finishedGame.getPlayer1().getId().equals(playerId)) {
                            playerColor = true;
                        } else if (finishedGame.getPlayer2().getId().equals(playerId)) {
                            playerColor = false;
                        } else {
                            return;
                        }
                        String result = finishedGame.getResult();
                        switch (showedGamesType) {
                            case FinishedGame.WON_GAMES:
                                if ((playerColor && FinishedGame.RESULT_WIN.equals(result))
                                        ^ (!playerColor && FinishedGame.RESULT_LOSE.equals(result))) {
                                    addGameToList(finishedGame);
                                }
                                break;
                            case FinishedGame.LOST_GAMES:
                                if ((playerColor && FinishedGame.RESULT_LOSE.equals(result))
                                        ^ (!playerColor && FinishedGame.RESULT_WIN.equals(result))) {
                                    addGameToList(finishedGame);
                                }
                                break;
                            case FinishedGame.DRAWN_GAMES:
                                if (FinishedGame.RESULT_DRAW.equals(result)) {
                                    addGameToList(finishedGame);
                                }
                                break;
                            case FinishedGame.ALL_GAMES:
                                addGameToList(finishedGame);
                                break;
                        }
                    }
                }));

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query = finishedGamesRef.orderByChild(Player.TIMESTAMP);
        query.addChildEventListener(listener);
    }

    private void addGameToList(FinishedGame finishedGame) {
        finishedGamesList.add(0, finishedGame);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                if(pleaseWaitDialog.isShowing()){
                    pleaseWaitDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        createAdapter(finishedGamesList);
    }

    @Override
    public void onStop() {
        if (query != null) {
            query.removeEventListener(listener);
        }
        super.onStop();
    }

    private void createAdapter(final ArrayList<FinishedGame> finishedGamesList) {
        adapter = new FinishedGamesListAdapter(finishedGamesList, getActivity());
        recycler.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(linearLayoutManager);
        adapter.setListener(new FinishedGamesListAdapter.Listener() {
            @Override
            public void onClick(int position) {
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(FinishedGame.MOVE_LIST, finishedGamesList.get(position).getMoveList());
                Fragment finishedGameDescriptionFragment = new FinishedGameDescription();
                finishedGameDescriptionFragment.setArguments(bundle);
                ((GameActivity) getActivity()).fragmentTransaction(finishedGameDescriptionFragment);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (showSearchView) {
            inflater.inflate(R.menu.menu, menu);
            final MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
            sv = (SearchView) myActionMenuItem.getActionView();
            sv.setQueryHint(getString(R.string.player_name) + " "+getString(R.string.or_date));
            ((GameActivity) getActivity()).hideKeyboard();
            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    final ArrayList<FinishedGame> resultList = searchInFinishedGamesList(query);
                    if (resultList.size() > 0) {
                        showSearchView = false;
                        createAdapter(resultList);
                        getActivity().invalidateOptionsMenu();
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.nothing_found), Toast.LENGTH_SHORT).show();
                    }
                    myActionMenuItem.collapseActionView();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
        refresh = menu.add(0, GameActivity.REFRESH_MENU_ITEM, 0, getActivity().getString(R.string.refresh)).setIcon(R.drawable.flip_board);
        refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    private ArrayList<FinishedGame> searchInFinishedGamesList(String query) {
        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        ArrayList<FinishedGame> resultList = new ArrayList<>();
        for (int i = 0; i < finishedGamesList.size(); i++) {
            String date = sfd.format(new Date(finishedGamesList.get(i).getTimestampCreatedLong()));
            if (date.contains(query) || finishedGamesList.get(i).getPlayer1().getName().contains(query)
                    || finishedGamesList.get(i).getPlayer2().getName().contains(query)) {
                resultList.add(finishedGamesList.get(i));
            }
        }
        return resultList;
    }
}
