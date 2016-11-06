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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.others.NetworkUtil;
import com.pkiykov.netchess.pojo.Player;
import com.pkiykov.netchess.pojo.PlayerGameParams;
import com.pkiykov.netchess.pojo.RunningGame;

import static com.pkiykov.netchess.fragments.Game.GAME_TYPE;
import static com.pkiykov.netchess.fragments.Game.LAN_GAME;
import static com.pkiykov.netchess.fragments.Game.ONE_DEVICE_GAME;
import static com.pkiykov.netchess.others.NetworkUtil.TYPE_WIFI;

public class UnrankedGameSettings extends Fragment {

    private RelativeLayout mainLayout;
    private CheckBox conditions, player1_handicapCheckBox, player1_timeControlCheckBox, player1_cancelableMovesCheckBox, player2_handicapCheckBox, player2_timeControlCheckBox, player2_cancelableMovesCheckBox;
    private TabHost tabHost;
    private TabHost.TabSpec tab1;
    private Spinner player1_handicapSpinner, player1_timeControlSpinner, player2_handicapSpinner, player2_timeControlSpinner;
    private Button startGame, joinGame;
    private NumberPicker player1_picker1, player1_picker2, player2_picker1, player2_picker2;
    private TextView player1_textPicker1, player1_textPicker2, player2_textPicker1, player2_textPicker2;
    private int pos1, pos2, gameType, wtf1, wtf2;
    private RadioButton white;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        mainLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_unranked_game_settings, container, false);
        gameType = getArguments().getInt(GAME_TYPE);

        initViews();
        if ((NetworkUtil.getConnectivityStatusString(getActivity()) != TYPE_WIFI)) {
            disableStart();
        }
        conditions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (conditions.isChecked()) {
                    tabHost.getTabWidget().setEnabled(false);
                    if (tabHost.getCurrentTabTag().equals(tab1.getTag())) {
                        tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.INVISIBLE);
                    } else {
                        tabHost.getTabWidget().getChildTabViewAt(0).setVisibility(View.INVISIBLE);
                    }
                } else {
                    tabHost.getTabWidget().setEnabled(true);
                    tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.VISIBLE);
                    tabHost.getTabWidget().getChildTabViewAt(0).setVisibility(View.VISIBLE);
                }
            }
        });
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = createBundle();
                if (gameType == LAN_GAME) {
                    bundle.putInt(GAME_TYPE, LAN_GAME);
                } else {
                    bundle.putInt(GAME_TYPE, ONE_DEVICE_GAME);
                }

                Fragment fragment = new Game();
                fragment.setArguments(bundle);
                ((GameActivity) getActivity()).fragmentTransaction(fragment);
            }
        });
        if (gameType == LAN_GAME) {
            joinGame.setVisibility(View.VISIBLE);
            joinGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((GameActivity) getActivity()).fragmentTransaction(new PeerToPeerDevicesList());
                }
            });
        }
        setUpPickers();
        setUpCheckBoxes();
        setUpSpinners();
        return mainLayout;
    }


    public void disableStart() {
        if (gameType != ONE_DEVICE_GAME) {
            startGame.setEnabled(false);
            startGame.setAlpha(0.5F);
            joinGame.setEnabled(false);
            joinGame.setAlpha(0.5F);
            Toast.makeText(getActivity(), R.string.wifi_problem, Toast.LENGTH_SHORT).show();
        }
    }

    public void enableStart() {
        if (gameType != ONE_DEVICE_GAME) {
            joinGame.setAlpha(1);
            startGame.setAlpha(1);
            startGame.setEnabled(true);
            joinGame.setEnabled(true);
        }
    }

    private Bundle createBundle() {
        Bundle bundle = new Bundle();
        Player player1 = new Player();
        Player player2 = new Player();
        PlayerGameParams player1GameParams = new PlayerGameParams();
        PlayerGameParams player2GameParams = new PlayerGameParams();
        if (conditions.isChecked()) {
            if (tabHost.getTabWidget().getChildTabViewAt(0).getVisibility() == View.VISIBLE) {
                if (gameType == ONE_DEVICE_GAME) {
                    player1GameParams.setCancelableMoves(player1_cancelableMovesCheckBox.isChecked());
                    player2GameParams.setCancelableMoves(player1_cancelableMovesCheckBox.isChecked());
                }
                if (player1_handicapCheckBox.isChecked()) {
                    player1GameParams.setHandicap(convertHandicap(player1_handicapSpinner.getSelectedItemPosition(), player1_handicapSpinner));
                    player2GameParams.setHandicap(convertHandicap(player1_handicapSpinner.getSelectedItemPosition(), player2_handicapSpinner));
                }
                if (player1_timeControlCheckBox.isChecked()) {
                    player1GameParams.setTimeControl(player1_timeControlSpinner.getSelectedItemPosition() + 1);
                    player2GameParams.setTimeControl(player1_timeControlSpinner.getSelectedItemPosition() + 1);
                    player1GameParams.setTimePicker1(player1_picker1.getValue());
                    player1GameParams.setTimePicker2(player1_picker2.getValue());
                    player2GameParams.setTimePicker1(player1_picker1.getValue());
                    player2GameParams.setTimePicker2(player1_picker2.getValue());
                }
            } else {
                if (gameType == ONE_DEVICE_GAME) {
                    player1GameParams.setCancelableMoves(player2_cancelableMovesCheckBox.isChecked());
                    player2GameParams.setCancelableMoves(player2_cancelableMovesCheckBox.isChecked());
                }
                if (player2_handicapCheckBox.isChecked()) {
                    player2GameParams.setHandicap(convertHandicap(player2_handicapSpinner.getSelectedItemPosition(), player2_handicapSpinner));
                    player1GameParams.setHandicap(convertHandicap(player2_handicapSpinner.getSelectedItemPosition(), player1_handicapSpinner));
                }
                if (player2_timeControlCheckBox.isChecked()) {
                    player2GameParams.setTimeControl(player2_timeControlSpinner.getSelectedItemPosition() + 1);
                    player1GameParams.setTimeControl(player2_timeControlSpinner.getSelectedItemPosition() + 1);
                    player2GameParams.setTimePicker1(player2_picker1.getValue());
                    player2GameParams.setTimePicker2(player2_picker2.getValue());
                    player1GameParams.setTimePicker1(player2_picker1.getValue());
                    player1GameParams.setTimePicker2(player2_picker2.getValue());
                }
            }
        } else {
            if (gameType == ONE_DEVICE_GAME) {
                player1GameParams.setCancelableMoves(player1_cancelableMovesCheckBox.isChecked());
                player2GameParams.setCancelableMoves(player2_cancelableMovesCheckBox.isChecked());
            }
            if (player1_handicapCheckBox.isChecked()) {
                player1GameParams.setHandicap(convertHandicap(player1_handicapSpinner.getSelectedItemPosition(), player1_handicapSpinner));
            }
            if (player1_timeControlCheckBox.isChecked()) {
                player1GameParams.setTimeControl(player1_timeControlSpinner.getSelectedItemPosition() + 1);
                player1GameParams.setTimePicker1(player1_picker1.getValue());
                player1GameParams.setTimePicker2(player1_picker2.getValue());
            }
            if (player2_handicapCheckBox.isChecked()) {
                player2GameParams.setHandicap(convertHandicap(player2_handicapSpinner.getSelectedItemPosition(), player2_handicapSpinner));
            }
            if (player2_timeControlCheckBox.isChecked()) {
                player2GameParams.setTimeControl(player2_timeControlSpinner.getSelectedItemPosition() + 1);
                player2GameParams.setTimePicker1(player2_picker1.getValue());
                player2GameParams.setTimePicker2(player2_picker2.getValue());
            }
        }

        player1.setPlayerGameParams(player1GameParams);
        player2.setPlayerGameParams(player2GameParams);
        RunningGame game = new RunningGame(player1, player2);
        if(gameType  == LAN_GAME) {
            game.setThisPlayerPlaysWhite(white.isChecked());
        }
        bundle.putSerializable(RunningGame.RUNNING_GAME, game);

        return bundle;
    }

    private void setUpSpinners() {
        ArrayAdapter<String> adapterHandicap = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.handicap));
        adapterHandicap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        player1_handicapSpinner.setAdapter(adapterHandicap);
        player1_handicapSpinner.setVisibility(View.INVISIBLE);
        player1_handicapSpinner.setEnabled(false);
        player1_handicapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                wtf1++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        ArrayAdapter<String> adapterTime = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.time_control));
        adapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        player1_timeControlSpinner.setAdapter(adapterTime);
        if (gameType == ONE_DEVICE_GAME) {
            player1_timeControlSpinner.setVisibility(View.INVISIBLE);
            player1_timeControlSpinner.setEnabled(false);
        }
        player1_timeControlSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                if (wtf1 > 1) {
                    pos1 = position;
                    setTimeControlPlayer1(pos1);
                }
                wtf1++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        ArrayAdapter<String> adapterHandicap2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.handicap));
        adapterHandicap2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        player2_handicapSpinner.setAdapter(adapterHandicap2);
        player2_handicapSpinner.setVisibility(View.INVISIBLE);
        player2_handicapSpinner.setEnabled(false);
        player2_handicapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                wtf2++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        ArrayAdapter<String> adapterTime2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.time_control));
        adapterTime2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        player2_timeControlSpinner.setAdapter(adapterTime2);
        if (gameType == ONE_DEVICE_GAME) {
            player2_timeControlSpinner.setVisibility(View.INVISIBLE);
            player2_timeControlSpinner.setEnabled(false);
        }
        player2_timeControlSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                if (wtf2 > 1) {
                    pos2 = position;
                    setTimeControlPlayer2(pos2);
                }
                wtf2++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    private void setUpCheckBoxes() {
        player1_handicapCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (player1_handicapCheckBox.isChecked()) {
                    player1_handicapSpinner.setVisibility(View.VISIBLE);
                    player1_handicapSpinner.setEnabled(true);
                } else {
                    player1_handicapSpinner.setVisibility(View.INVISIBLE);
                    player1_handicapSpinner.setEnabled(false);
                }
            }
        });

        player2_handicapCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (player2_handicapCheckBox.isChecked()) {
                    player2_handicapSpinner.setVisibility(View.VISIBLE);
                    player2_handicapSpinner.setEnabled(true);
                } else {
                    player2_handicapSpinner.setVisibility(View.INVISIBLE);
                    player2_handicapSpinner.setEnabled(false);
                }
            }
        });
        if (gameType == ONE_DEVICE_GAME) {
            player1_timeControlCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    setUpTimeControlPlayer1(b);
                }
            });


            player2_timeControlCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    setUpTimeControlPlayer2(b);
                }
            });
        } else {
            setUpTimeControlPlayer1(true);
            setUpTimeControlPlayer2(true);
        }
    }

    private void setUpTimeControlPlayer2(boolean b) {
        if (b) {
            player2_timeControlSpinner.setVisibility(View.VISIBLE);
            player2_timeControlSpinner.setEnabled(true);
            setTimeControlPlayer2(pos2);
        } else {
            player2_timeControlSpinner.setVisibility(View.INVISIBLE);
            player2_timeControlSpinner.setEnabled(false);
            player2_textPicker1.setVisibility(View.INVISIBLE);
            player2_textPicker2.setVisibility(View.INVISIBLE);
            player2_picker1.setEnabled(false);
            player2_picker2.setEnabled(false);
            player2_picker2.setVisibility(View.INVISIBLE);
            player2_picker1.setVisibility(View.INVISIBLE);
        }
    }

    private void setUpTimeControlPlayer1(boolean b) {
        if (b) {
            player1_timeControlSpinner.setVisibility(View.VISIBLE);
            player1_timeControlSpinner.setEnabled(true);
            setTimeControlPlayer1(pos1);
        } else {
            player1_timeControlSpinner.setVisibility(View.INVISIBLE);
            player1_timeControlSpinner.setEnabled(false);
            player1_textPicker1.setVisibility(View.INVISIBLE);
            player1_textPicker2.setVisibility(View.INVISIBLE);
            player1_picker1.setEnabled(false);
            player1_picker2.setEnabled(false);
            player1_picker2.setVisibility(View.INVISIBLE);
            player1_picker1.setVisibility(View.INVISIBLE);
        }
    }

    private void setUpPickers() {
        player1_picker1 = (NumberPicker) mainLayout.findViewById(R.id.player1_timePicker);
        player1_picker1.setEnabled(true);
        player1_picker1.computeScroll();
        player1_picker1.setMaxValue(300);
        player1_picker1.setMinValue(1);
        player1_picker1.setValue(15);
        player1_picker1.setWrapSelectorWheel(true);
        player1_picker1.setVisibility(View.INVISIBLE);
        player1_picker1.setEnabled(false);

        player1_picker2.setEnabled(true);
        player1_picker2.computeScroll();
        player1_picker2.setMaxValue(99);
        player1_picker2.setMinValue(1);
        player1_picker2.setValue(40);
        player1_picker2.setWrapSelectorWheel(true);
        player1_picker2.setVisibility(View.INVISIBLE);
        player1_picker2.setEnabled(false);

        player2_picker1 = (NumberPicker) mainLayout.findViewById(R.id.player2_timePicker);
        player2_picker1.setEnabled(true);
        player2_picker1.computeScroll();
        player2_picker1.setMaxValue(300);
        player2_picker1.setMinValue(1);
        player2_picker1.setWrapSelectorWheel(true);
        player2_picker1.setValue(15);
        player2_picker1.setVisibility(View.INVISIBLE);
        player2_picker1.setEnabled(false);

        player2_picker2.setEnabled(true);
        player2_picker2.computeScroll();
        player2_picker2.setMaxValue(99);
        player2_picker2.setMinValue(1);
        player2_picker2.setWrapSelectorWheel(true);
        player2_picker2.setValue(40);
        player2_picker2.setVisibility(View.INVISIBLE);
        player2_picker2.setEnabled(false);
    }

    private int convertHandicap(int position, Spinner spinner) {
        int handicap = -1;

        if (spinner.equals(player1_handicapSpinner)) {
            if (position < 8)
                handicap = position + 48;
            switch (position) {
                case 8:
                    handicap = 57;
                    break;
                case 9:
                    handicap = 62;
                    break;
                case 10:
                    handicap = 58;
                    break;
                case 11:
                    handicap = 61;
                    break;
                case 12:
                    handicap = 56;
                    break;
                case 13:
                    handicap = 63;
                    break;
                case 14:
                    handicap = 59;
                    break;
            }
        } else {
            if (position < 8)
                handicap = position + 8;
            switch (position) {
                case 8:
                    handicap = 1;
                    break;
                case 9:
                    handicap = 6;
                    break;
                case 10:
                    handicap = 2;
                    break;
                case 11:
                    handicap = 5;
                    break;
                case 12:
                    handicap = 0;
                    break;
                case 13:
                    handicap = 7;
                    break;
                case 14:
                    handicap = 3;
                    break;
            }
        }
        return handicap;
    }

    private void initViews() {

        tabHost = (TabHost) mainLayout.findViewById(R.id.tab_host);
        tab1 = tabHost.newTabSpec(getActivity().getResources().getString(R.string.player) + " 1");
        TabHost.TabSpec tab2 = tabHost.newTabSpec(getActivity().getResources().getString(R.string.player) + " 2");
        tabHost.setup();
        tab1.setIndicator(getActivity().getResources().getString(R.string.player) + " 1");
        tab1.setContent(R.id.tab1);

        tab2.setIndicator(getActivity().getResources().getString(R.string.player) + " 2");
        tab2.setContent(R.id.tab2);

        tabHost.getTabWidget().setShowDividers(TabWidget.SHOW_DIVIDER_MIDDLE);

        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.getTabWidget().getChildTabViewAt(0).setTag("tab_1");
        tabHost.getTabWidget().getChildTabViewAt(1).setTag("tab_2");
        tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.INVISIBLE);

        conditions = (CheckBox) mainLayout.findViewById(R.id.conditions);
        startGame = (Button) mainLayout.findViewById(R.id.start_new_game);


        player1_handicapCheckBox = (CheckBox) mainLayout.findViewById(R.id.player1_handicapCheckbox);
        player1_timeControlCheckBox = (CheckBox) mainLayout.findViewById(R.id.player1_timeControlCheckbox);
        player1_handicapSpinner = (Spinner) mainLayout.findViewById(R.id.player1_spinner_handicap);
        player1_timeControlSpinner = (Spinner) mainLayout.findViewById(R.id.player1_spinner_time_control);
        player1_picker1 = (NumberPicker) mainLayout.findViewById(R.id.player1_timePicker);
        player1_picker2 = (NumberPicker) mainLayout.findViewById(R.id.player1_movePicker);
        player1_textPicker2 = (TextView) mainLayout.findViewById(R.id.player1_text_picker_2);
        player1_textPicker1 = (TextView) mainLayout.findViewById(R.id.player1_text_picker_1);
        player1_textPicker1.setVisibility(View.INVISIBLE);
        player1_textPicker2.setVisibility(View.INVISIBLE);

        player2_timeControlCheckBox = (CheckBox) mainLayout.findViewById(R.id.player2_timeControlCheckbox);
        player2_handicapCheckBox = (CheckBox) mainLayout.findViewById(R.id.player2_handicapCheckbox);
        player2_handicapSpinner = (Spinner) mainLayout.findViewById(R.id.player2_spinner_handicap);
        player2_timeControlSpinner = (Spinner) mainLayout.findViewById(R.id.player2_spinner_time_control);
        player2_picker1 = (NumberPicker) mainLayout.findViewById(R.id.player2_timePicker);
        player2_picker2 = (NumberPicker) mainLayout.findViewById(R.id.player2_movePicker);
        player2_textPicker2 = (TextView) mainLayout.findViewById(R.id.player2_text_picker_2);
        player2_textPicker1 = (TextView) mainLayout.findViewById(R.id.player2_text_picker_1);
        player2_textPicker1.setVisibility(View.INVISIBLE);
        player2_textPicker2.setVisibility(View.INVISIBLE);

        if (gameType == ONE_DEVICE_GAME) {
            player1_cancelableMovesCheckBox = (CheckBox) mainLayout.findViewById(R.id.player1_cancel_move_checkbox);
            player2_cancelableMovesCheckBox = (CheckBox) mainLayout.findViewById(R.id.player2_cancel_move_checkbox);
            LinearLayout pl1linLayout = (LinearLayout) mainLayout.findViewById(R.id.cancelable_moves_layout_player_1);
            pl1linLayout.setVisibility(View.VISIBLE);
            LinearLayout pl2linLayout = (LinearLayout) mainLayout.findViewById(R.id.cancelable_moves_layout_player_2);
            pl2linLayout.setVisibility(View.VISIBLE);
        } else {
            joinGame = (Button) mainLayout.findViewById(R.id.join_game);
            LinearLayout linearLayout = (LinearLayout) mainLayout.findViewById(R.id.color_picker_layout);
            linearLayout.setVisibility(View.VISIBLE);
            white = (RadioButton) mainLayout.findViewById(R.id.white);
            player1_timeControlCheckBox.setChecked(true);
            player2_timeControlCheckBox.setChecked(true);
            player1_timeControlCheckBox.setEnabled(false);
            player2_timeControlCheckBox.setEnabled(false);
        }

    }

    private void setTimeControlPlayer1(int position) {
        if (position == 0) {
            player1_picker1.setEnabled(true);
            player1_picker1.setVisibility(View.VISIBLE);
            player1_textPicker1.setText(getResources().getString(R.string.minutes));
            player1_textPicker1.setVisibility(View.VISIBLE);
            player1_textPicker2.setVisibility(View.INVISIBLE);
            player1_picker2.setEnabled(false);
            player1_picker2.setVisibility(View.INVISIBLE);
        } else if (position == 1) {
            player1_picker1.setEnabled(true);
            player1_picker1.setVisibility(View.VISIBLE);
            player1_textPicker1.setText(getResources().getString(R.string.seconds));
            player1_textPicker1.setVisibility(View.VISIBLE);
            player1_textPicker2.setVisibility(View.INVISIBLE);
            player1_picker2.setEnabled(false);
            player1_picker2.setVisibility(View.INVISIBLE);
        } else if (position == 2) {
            player1_textPicker1.setText(getResources().getString(R.string.extra_seconds));
            String t = "\n" + getResources().getString(R.string.minutes);
            player1_textPicker2.setText(t);
            player1_textPicker1.setVisibility(View.VISIBLE);
            player1_textPicker2.setVisibility(View.VISIBLE);
            player1_picker1.setEnabled(true);
            player1_picker1.setVisibility(View.VISIBLE);
            player1_picker2.setEnabled(true);
            player1_picker2.setVisibility(View.VISIBLE);
        } else if (position == 3) {
            player1_picker1.setEnabled(true);
            player1_picker1.setVisibility(View.VISIBLE);
            player1_picker2.setEnabled(true);
            player1_picker2.setVisibility(View.VISIBLE);
            player1_textPicker2.setText(getResources().getString(R.string.moves));
            player1_textPicker2.setVisibility(View.VISIBLE);
            player1_textPicker1.setText(getResources().getString(R.string.minutes));
            player1_textPicker1.setVisibility(View.VISIBLE);
        } else {
            player1_picker1.setEnabled(true);
            player1_picker1.setVisibility(View.VISIBLE);
            player1_textPicker1.setText(getResources().getString(R.string.seconds));
            player1_textPicker1.setVisibility(View.VISIBLE);
            player1_textPicker2.setVisibility(View.INVISIBLE);
            player1_picker2.setEnabled(false);
            player1_picker2.setVisibility(View.INVISIBLE);
        }
    }

    private void setTimeControlPlayer2(int position) {
        if (position == 0) {
            player2_picker1.setEnabled(true);
            player2_picker1.setVisibility(View.VISIBLE);
            player2_textPicker1.setText(getResources().getString(R.string.minutes));
            player2_textPicker1.setVisibility(View.VISIBLE);
            player2_textPicker2.setVisibility(View.INVISIBLE);
            player2_picker2.setEnabled(false);
            player2_picker2.setVisibility(View.INVISIBLE);
        } else if (position == 1) {
            player2_picker1.setEnabled(true);
            player2_picker1.setVisibility(View.VISIBLE);
            player2_textPicker1.setText(getResources().getString(R.string.seconds));
            player2_textPicker1.setVisibility(View.VISIBLE);
            player2_textPicker2.setVisibility(View.INVISIBLE);
            player2_picker2.setEnabled(false);
            player2_picker2.setVisibility(View.INVISIBLE);
        } else if (position == 2) {
            player2_textPicker1.setText(getResources().getString(R.string.extra_seconds));
            String t = "\n" + getResources().getString(R.string.minutes);
            player2_textPicker2.setText(t);
            player2_textPicker1.setVisibility(View.VISIBLE);
            player2_textPicker2.setVisibility(View.VISIBLE);
            player2_picker1.setEnabled(true);
            player2_picker1.setVisibility(View.VISIBLE);
            player2_picker2.setEnabled(true);
            player2_picker2.setVisibility(View.VISIBLE);
        } else if (position == 3) {
            player2_picker1.setEnabled(true);
            player2_picker1.setVisibility(View.VISIBLE);
            player2_picker2.setEnabled(true);
            player2_picker2.setVisibility(View.VISIBLE);
            player2_textPicker2.setText(getResources().getString(R.string.moves));
            player2_textPicker2.setVisibility(View.VISIBLE);
            player2_textPicker1.setText(getResources().getString(R.string.minutes));
            player2_textPicker1.setVisibility(View.VISIBLE);
        } else {
            player2_picker1.setEnabled(true);
            player2_picker1.setVisibility(View.VISIBLE);
            player2_textPicker1.setText(getResources().getString(R.string.seconds));
            player2_textPicker1.setVisibility(View.VISIBLE);
            player2_textPicker2.setVisibility(View.INVISIBLE);
            player2_picker2.setEnabled(false);
            player2_picker2.setVisibility(View.INVISIBLE);
        }
    }

}
