package com.pkiykov.netchess.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.pkiykov.netchess.R;
import com.pkiykov.netchess.pojo.FinishedGame;
import com.pkiykov.netchess.pojo.RunningGame;

import java.util.ArrayList;

public class FinishedGameDescription extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_finished_game_description, container, false);
        ListView listView = (ListView) linearLayout.findViewById(R.id.list_view_moves);
        ArrayList<String> myList = RunningGame.formatMoveList(getArguments().getStringArrayList(FinishedGame.MOVE_LIST));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, myList);
        listView.setAdapter(adapter);
        return linearLayout;
    }
}
