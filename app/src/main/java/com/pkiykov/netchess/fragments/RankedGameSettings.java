package com.pkiykov.netchess.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.others.FirebaseHelper;
import com.pkiykov.netchess.pojo.Player;
import com.pkiykov.netchess.pojo.PlayerGameParams;
import com.pkiykov.netchess.pojo.RunningGame;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RankedGameSettings extends Fragment {

    private RelativeLayout mainLayout;
    private Button joinGame, createGame;
    private RadioButton white;
    private Spinner timeControlSpinner;
    private NumberPicker picker1, picker2;
    private TextView rating, playerName, textPicker1, textPicker2;
    private Player player;
    private FirebaseHelper playerDbHelper;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_ranked_game_settings, container, false);
        initViews();
        disableStart();

        setPickersParams();
        createPlayerWithDatabase();
        ArrayAdapter<String> adapterTime = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.time_control));
        adapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeControlSpinner.setAdapter(adapterTime);
        timeControlSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                setTimeControlPlayer(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        joinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Player.PLAYERS, player);
                Fragment fragment = new OnlineGamesList();
                fragment.setArguments(bundle);
                ((GameActivity) getActivity()).fragmentTransaction(fragment);
            }
        });
        createGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = createGameBundle();
                Fragment fragment = new Game();
                fragment.setArguments(bundle);
                ((GameActivity) getActivity()).fragmentTransaction(fragment);
            }
        });

        return mainLayout;

    }

    public void disableStart() {
        createGame.setEnabled(false);
        createGame.setAlpha(0.5F);
        joinGame.setEnabled(false);
        joinGame.setAlpha(0.5F);
    }

    public void createPlayerWithDatabase() {
        playerDbHelper = new FirebaseHelper(FirebaseDatabase.getInstance().getReference().child(Player.PLAYERS)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                player = dataSnapshot.getValue(Player.class);
                String r = getString(R.string.rating) + String.valueOf(player.getRating());
                playerName.setText(player.getName());
                rating.setText(r);
                playerDbHelper.getRef().removeEventListener(this);
                enableStart();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void enableStart() {
        joinGame.setAlpha(1);
        createGame.setAlpha(1);
        createGame.setEnabled(true);
        joinGame.setEnabled(true);
    }

    private void setPickersParams() {
        textPicker2.setVisibility(View.INVISIBLE);
        picker2.setVisibility(View.INVISIBLE);
        picker1.setMaxValue(300);
        picker1.setMinValue(1);
        picker1.setValue(15);
        picker2.setMaxValue(99);
        picker2.setMinValue(1);
        picker2.setValue(40);
    }

    private Bundle createGameBundle() {
        Bundle bundle = new Bundle();
        PlayerGameParams playerGameParams = new PlayerGameParams();
        playerGameParams.setTimePicker1(picker1.getValue());
        playerGameParams.setTimePicker2(picker2.getValue());
        playerGameParams.setTimeControl(timeControlSpinner.getSelectedItemPosition() + 1);
        player.setPlayerGameParams(playerGameParams);
        RunningGame game;
        if (white.isChecked()) {
            game = new RunningGame(player, null);
        } else {
            game = new RunningGame(null, player);
        }
        bundle.putInt(Game.GAME_TYPE, Game.ONLINE_GAME);
        bundle.putSerializable(RunningGame.RUNNING_GAME, game);
        return bundle;
    }

    private void setTimeControlPlayer(int position) {
        if (position == 0) {
            textPicker1.setText(getResources().getString(R.string.minutes));
            textPicker2.setVisibility(View.INVISIBLE);
            picker2.setEnabled(false);
            picker2.setVisibility(View.INVISIBLE);
        } else if (position == 1) {
            textPicker1.setText(getResources().getString(R.string.seconds));
            textPicker2.setVisibility(View.INVISIBLE);
            picker2.setEnabled(false);
            picker2.setVisibility(View.INVISIBLE);
        } else if (position == 2) {
            textPicker1.setText(getResources().getString(R.string.extra_seconds));
            String t = "\n" + getResources().getString(R.string.minutes);
            textPicker2.setText(t);
            textPicker2.setVisibility(View.VISIBLE);
            picker2.setEnabled(true);
            picker2.setVisibility(View.VISIBLE);
        } else if (position == 3) {
            picker2.setEnabled(true);
            picker2.setVisibility(View.VISIBLE);
            textPicker2.setText(getResources().getString(R.string.moves));
            textPicker2.setVisibility(View.VISIBLE);
            textPicker1.setText(getResources().getString(R.string.minutes));
        } else {
            textPicker1.setText(getResources().getString(R.string.seconds));
            textPicker2.setVisibility(View.INVISIBLE);
            picker2.setEnabled(false);
            picker2.setVisibility(View.INVISIBLE);
        }
    }

    private void initViews() {
        joinGame = (Button) mainLayout.findViewById(R.id.join_game);
        createGame = (Button) mainLayout.findViewById(R.id.create_new_game);
        playerName = (TextView) mainLayout.findViewById(R.id.player_name);
        rating = (TextView) mainLayout.findViewById(R.id.player_rating);
        white = (RadioButton) mainLayout.findViewById(R.id.white);
        timeControlSpinner = (Spinner) mainLayout.findViewById(R.id.spinner_time_control);
        picker1 = (NumberPicker) mainLayout.findViewById(R.id.time_picker1);
        picker2 = (NumberPicker) mainLayout.findViewById(R.id.time_picker2);
        textPicker1 = (TextView) mainLayout.findViewById(R.id.text_picker_1);
        textPicker2 = (TextView) mainLayout.findViewById(R.id.text_picker_2);
    }

}
