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

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.adapters.PlayersListAdapter;
import com.pkiykov.netchess.pojo.Player;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RankList extends Fragment {
    private SearchView sv;
    private ArrayList<Player> playersList;
    private RecyclerView recycler;
    private MenuItem refresh;
    private PlayersListAdapter adapter;
    private DatabaseReference playersRef;
    private Query query;
    private ChildEventListener listener;
    private LinearLayoutManager linearLayoutManager;
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
        playersList = new ArrayList<>();
        playersRef = FirebaseDatabase.getInstance().getReference().child(Player.PLAYERS);
        pleaseWaitDialog = ProgressDialog.show(getActivity(), "", getString(R.string.please_wait), false);
        retreiveData();
        ((GameActivity) getActivity()).getToolbar().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == GameActivity.REFRESH_MENU_ITEM) {
                    View v = getActivity().findViewById(refresh.getItemId());
                    Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotation);
                    v.startAnimation(rotation);
                    playersList.clear();
                    createAdapter(playersList);
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
                        Player player = dataSnapshot.getValue(Player.class);
                        playersList.add(0, player);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                if(pleaseWaitDialog.isShowing()) {
                                    pleaseWaitDialog.dismiss();
                                }
                            }
                        });
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
        query = playersRef.orderByChild(Player.PLAYER_RATING);
        query.addChildEventListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        createAdapter(playersList);
        adapter.setListener(new PlayersListAdapter.Listener() {
            @Override
            public void onClick(int position) {
                Bundle bundle = new Bundle();
                bundle.putString(Player.PLAYER_ID, playersList.get(position).getId());
                Fragment profileFragment = new Profile();
                profileFragment.setArguments(bundle);
                ((GameActivity) getActivity()).fragmentTransaction(profileFragment);
            }
        });
        if (getArguments() != null) {
            int pos = 1;
            String playerId = getArguments().getString(Player.PLAYER_ID);
            for (int i = 0; i < playersList.size(); i++) {
                if (playersList.get(i).getId().equals(playerId)) {
                    pos = i;
                }
            }
            linearLayoutManager.scrollToPosition(pos);
        }
    }

    @Override
    public void onStop() {
        if (query != null) {
            query.removeEventListener(listener);
        }
        super.onStop();
    }

    private void createAdapter(final ArrayList<Player> playersList) {
        adapter = new PlayersListAdapter(playersList, getActivity(), true);
        recycler.setAdapter(adapter);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(linearLayoutManager);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(showSearchView) {
            inflater.inflate(R.menu.menu, menu);
            final MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
            addSearchView(myActionMenuItem);
        }
        refresh = menu.add(0, GameActivity.REFRESH_MENU_ITEM, 0, getActivity().getString(R.string.refresh)).setIcon(R.drawable.flip_board);
        refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    private void addSearchView(final MenuItem myActionMenuItem) {
        sv = (SearchView) myActionMenuItem.getActionView();
        sv.setQueryHint(getString(R.string.player_name));
        ((GameActivity) getActivity()).hideKeyboard();
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final ArrayList<Player> resultList = searchInPlayersList(query);
                if (resultList.size() == 1) {
                    linearLayoutManager.scrollToPosition(resultList.get(0).getRank());
                } else if (resultList.size() > 1) {
                    showSearchView = false;
                    createAdapter(resultList);
                    adapter.setListener(new PlayersListAdapter.Listener() {
                        @Override
                        public void onClick(int position) {
                            Bundle bundle = new Bundle();
                            bundle.putString(Player.PLAYER_ID, resultList.get(position).getId());
                            Fragment rankListFragment = new RankList();
                            rankListFragment.setArguments(bundle);
                            ((GameActivity) getActivity()).fragmentTransaction(rankListFragment);
                        }
                    });
                    getActivity().invalidateOptionsMenu();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.nothing_found), Toast.LENGTH_SHORT).show();
                }
                myActionMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private ArrayList<Player> searchInPlayersList(String query) {
        ArrayList<Player> resultList = new ArrayList<>();
        for (int i = 0; i < playersList.size(); i++) {
            if (playersList.get(i).getName().contains(query)) {
                playersList.get(i).setRank(i+1);
                resultList.add(playersList.get(i));
            }
        }
        return resultList;
    }
}
