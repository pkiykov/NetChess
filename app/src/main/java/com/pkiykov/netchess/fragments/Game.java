package com.pkiykov.netchess.fragments;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.logic.GameDatabase;
import com.pkiykov.netchess.logic.GameEnd;
import com.pkiykov.netchess.logic.GameGo;
import com.pkiykov.netchess.logic.GameMove;
import com.pkiykov.netchess.logic.GameStart;
import com.pkiykov.netchess.logic.GameTime;
import com.pkiykov.netchess.logic.Peer2Peer;
import com.pkiykov.netchess.logic.ReceivedFromOpponent;
import com.pkiykov.netchess.pojo.GameExtraParams;
import com.pkiykov.netchess.pojo.RunningGame;
import com.pkiykov.netchess.receivers.OpponentDisconnectReceiver;
import com.pkiykov.netchess.services.OnlineCheckPlayerService;

public class Game extends Fragment {

    public static final String RESIGN = "resign";
    public static final String DRAW = "draw";
    public static final String GAME_TYPE = "selected_runningGame_type";

    public static final int ONLINE_GAME = 1;
    public static final int LAN_GAME = 2;
    public static final int ONE_DEVICE_GAME = 3;

    private Uri joinedAvaUri;
    private Dialog pauseDialog;
    private AlertDialog drawOfferedDialog;
    private ProgressDialog waitingPlayers;
    private LinearLayout allField, player1Layout, player2Layout, linearLayout1,
            linearLayout2, linearLayout3, linearLayout4, linearLayout5,
            linearLayout6, linearLayout7, linearLayout8;
    private RelativeLayout relativeLayout;
    private FrameLayout pressed;
    private ImageView[][] images, piece, select, highlight;
    private TextView kingIsCheckedText1, kingIsCheckedText2, timer1, timer2,
            player1_rating, player2_rating, player1_name, player2_name,
            player1TimeControlDescription, player2TimeControlDescription;
    private ImageView player1_ava, player2_ava, playerJoinedAva;
    private ImageButton openLogsButton;
    private CountDownTimer countDownTimer1, countDownTimer2;
    private MenuItem flipBoard;
    private MenuItem cancelMove;
    private MenuItem openLogs;
    private MenuItem resign;
    private MenuItem offerDraw;
    private MenuItem pause;
    private MenuItem sendMessage;
    private OnlineCheckPlayerService mService;
    private OpponentDisconnectReceiver receiver;
    private ServiceConnection mConnection;
    private int gameType;
    private ArrayAdapter<String> chatLogsAdapter;
    private GameExtraParams gameExtraParams;
    private RunningGame runningGame;
    private GameTime gameTime;
    private GameMove gameMove;
    private GameDatabase gameDatabase;
    private GameStart gameStart;
    private GameEnd gameEnd;
    private GameGo gameGo;
    private Peer2Peer peer2Peer;
    private String player1Id, player2Id;
    private boolean backstack = false;
    //private boolean highlightMoves;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if (runningGame == null && !backstack) {
            relativeLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_game, container, false);
            initialize();
            gameStart.createPauseDialog();
            gameStart.setUpGameSettings();
            gameStart.initField();
            gameStart.setActivePlayer();
            gameTime.timeControl();
        }
        gameStart.setUpMenuItems();
        return relativeLayout;
    }

    @Override
    public void onResume() {
        ((GameActivity) getActivity()).getDrawerList().post(new Runnable() {
            @Override
            public void run() {
                ((GameActivity) getActivity()).changeStateCurrentGameFragmentItem(false);
            }
        });
        backstack = false;
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((GameActivity) getActivity()).changeStateCurrentGameFragmentItem(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        gameStart.createMenu(menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        gameGo.prepareMenu();
        super.onPrepareOptionsMenu(menu);
    }

    private void initialize() {
        allField = (LinearLayout) relativeLayout.findViewById(R.id.all_field);
        player1Layout = (LinearLayout) relativeLayout.findViewById(R.id.player1_layout);
        player2Layout = (LinearLayout) relativeLayout.findViewById(R.id.player2_layout);
        linearLayout1 = (LinearLayout) relativeLayout.findViewById(R.id.field1);
        linearLayout2 = (LinearLayout) relativeLayout.findViewById(R.id.field2);
        linearLayout3 = (LinearLayout) relativeLayout.findViewById(R.id.field3);
        linearLayout4 = (LinearLayout) relativeLayout.findViewById(R.id.field4);
        linearLayout5 = (LinearLayout) relativeLayout.findViewById(R.id.field5);
        linearLayout6 = (LinearLayout) relativeLayout.findViewById(R.id.field6);
        linearLayout7 = (LinearLayout) relativeLayout.findViewById(R.id.field7);
        linearLayout8 = (LinearLayout) relativeLayout.findViewById(R.id.field8);
        kingIsCheckedText1 = (TextView) relativeLayout.findViewById(R.id.player1_kingIsChecked);
        kingIsCheckedText2 = (TextView) relativeLayout.findViewById(R.id.player2_kingIsChecked);
        timer1 = (TextView) relativeLayout.findViewById(R.id.player1_time);
        timer2 = (TextView) relativeLayout.findViewById(R.id.player2_time);
        player1_ava = (ImageView) relativeLayout.findViewById(R.id.player1_photo);
        player2_ava = (ImageView) relativeLayout.findViewById(R.id.player2_photo);
        player1_name = (TextView) relativeLayout.findViewById(R.id.player1_name);
        player2_name = (TextView) relativeLayout.findViewById(R.id.player2_name);
        player1_rating = (TextView) relativeLayout.findViewById(R.id.player1_rating);
        player2_rating = (TextView) relativeLayout.findViewById(R.id.player2_rating);
        player1TimeControlDescription = (TextView) relativeLayout.findViewById(R.id.player1_time_control_description);
        player2TimeControlDescription = (TextView) relativeLayout.findViewById(R.id.player2_time_control_description);
        runningGame = (RunningGame) getArguments().getSerializable(RunningGame.RUNNING_GAME);
        gameType = getArguments().getInt(GAME_TYPE);
        gameTime = new GameTime(this);
        gameMove = new GameMove(this);
        gameStart = new GameStart(this);
        gameGo = new GameGo(this);
        gameEnd = new GameEnd(this);
        if (((GameActivity) getActivity()).getPeer2Peer() != null) {
            peer2Peer = ((GameActivity) getActivity()).getPeer2Peer();
            peer2Peer.setGame(this);
            peer2Peer.setReceivedFromOpponent(new ReceivedFromOpponent(this));
        }
        player1Layout.setBackgroundResource(R.drawable.active_player_selection);
        // highlightMoves = true;
    }

    public int getGameType() {
        return gameType;
    }

    public RunningGame getRunningGame() {
        return runningGame;
    }

    public void setRunningGame(RunningGame runningGame) {
        this.runningGame = runningGame;
    }

    public void setGameExtraParams(GameExtraParams gameExtraParams) {
        this.gameExtraParams = gameExtraParams;
    }

    public TextView getTimer1() {
        return timer1;
    }


    public TextView getTimer2() {
        return timer2;
    }

    public CountDownTimer getCountDownTimer1() {
        return countDownTimer1;
    }

    public void setCountDownTimer1(CountDownTimer countDownTimer1) {
        this.countDownTimer1 = countDownTimer1;
    }

    public CountDownTimer getCountDownTimer2() {
        return countDownTimer2;
    }

    public void setCountDownTimer2(CountDownTimer countDownTimer2) {
        this.countDownTimer2 = countDownTimer2;
    }

    public GameExtraParams getGameExtraParams() {
        return gameExtraParams;
    }

    public ImageView[][] getPiece() {
        return piece;
    }

    public FrameLayout getPressed() {
        return pressed;
    }

    public void setPressed(FrameLayout pressed) {
        this.pressed = pressed;
    }

    public ImageView[][] getSelect() {
        return select;
    }

    public Uri getJoinedAvaUri() {
        return joinedAvaUri;
    }

    public void setJoinedAvaUri(Uri joinedAvaUri) {
        this.joinedAvaUri = joinedAvaUri;
    }


    public Dialog getPauseDialog() {
        return pauseDialog;
    }

    public void setPauseDialog(Dialog pauseDialog) {
        this.pauseDialog = pauseDialog;
    }

    public AlertDialog getDrawOfferedDialog() {
        return drawOfferedDialog;
    }

    public void setDrawOfferedDialog(AlertDialog drawOfferedDialog) {
        this.drawOfferedDialog = drawOfferedDialog;
    }

    public ProgressDialog getWaitingPlayers() {
        return waitingPlayers;
    }

    public void setWaitingPlayers(ProgressDialog waitingPlayers) {
        this.waitingPlayers = waitingPlayers;
    }

    public LinearLayout getAllField() {
        return allField;
    }

    public LinearLayout getPlayer1Layout() {
        return player1Layout;
    }

    public LinearLayout getPlayer2Layout() {
        return player2Layout;
    }

    public LinearLayout getLinearLayout1() {
        return linearLayout1;
    }

    public LinearLayout getLinearLayout2() {
        return linearLayout2;
    }

    public LinearLayout getLinearLayout3() {
        return linearLayout3;
    }

    public LinearLayout getLinearLayout4() {
        return linearLayout4;
    }

    public LinearLayout getLinearLayout5() {
        return linearLayout5;
    }

    public LinearLayout getLinearLayout6() {
        return linearLayout6;
    }

    public LinearLayout getLinearLayout7() {
        return linearLayout7;
    }

    public LinearLayout getLinearLayout8() {
        return linearLayout8;
    }

    public RelativeLayout getRelativeLayout() {
        return relativeLayout;
    }

    public ImageView[][] getImages() {
        return images;
    }

    public void setImages(ImageView[][] images) {
        this.images = images;
    }

    public void setPiece(ImageView[][] piece) {
        this.piece = piece;
    }

    public void setSelect(ImageView[][] select) {
        this.select = select;
    }

    public ImageView[][] getHighlight() {
        return highlight;
    }

    public void setHighlight(ImageView[][] highlight) {
        this.highlight = highlight;
    }

    public TextView getKingIsCheckedText1() {
        return kingIsCheckedText1;
    }

    public TextView getKingIsCheckedText2() {
        return kingIsCheckedText2;
    }

    public TextView getPlayer1_rating() {
        return player1_rating;
    }

    public TextView getPlayer2_rating() {
        return player2_rating;
    }

    public TextView getPlayer1_name() {
        return player1_name;
    }

    public TextView getPlayer2_name() {
        return player2_name;
    }

    public TextView getPlayer1TimeControlDescription() {
        return player1TimeControlDescription;
    }

    public TextView getPlayer2TimeControlDescription() {
        return player2TimeControlDescription;
    }

    public ImageView getPlayer1_ava() {
        return player1_ava;
    }

    public ImageView getPlayer2_ava() {
        return player2_ava;
    }

    public MenuItem getFlipBoard() {
        return flipBoard;
    }

    public void setFlipBoard(MenuItem flipBoard) {
        this.flipBoard = flipBoard;
    }

    public MenuItem getCancelMove() {
        return cancelMove;
    }

    public void setCancelMove(MenuItem cancelMove) {
        this.cancelMove = cancelMove;
    }

    public MenuItem getOpenLogs() {
        return openLogs;
    }

    public void setOpenLogs(MenuItem openLogs) {
        this.openLogs = openLogs;
    }

    public MenuItem getResign() {
        return resign;
    }

    public void setResign(MenuItem resign) {
        this.resign = resign;
    }

    public MenuItem getOfferDraw() {
        return offerDraw;
    }

    public void setOfferDraw(MenuItem offerDraw) {
        this.offerDraw = offerDraw;
    }

    public MenuItem getPause() {
        return pause;
    }

    public void setPause(MenuItem pause) {
        this.pause = pause;
    }

    public OnlineCheckPlayerService getmService() {
        return mService;
    }

    public void setmService(OnlineCheckPlayerService mService) {
        this.mService = mService;
    }

    public ImageButton getOpenLogsButton() {
        return openLogsButton;
    }

    public void setOpenLogsButton(ImageButton openLogsButton) {
        this.openLogsButton = openLogsButton;
    }

    public ArrayAdapter<String> getChatLogsAdapter() {
        return chatLogsAdapter;
    }

    public void setChatLogsAdapter(ArrayAdapter<String> chatLogsAdapter) {
        this.chatLogsAdapter = chatLogsAdapter;
    }

    public ServiceConnection getmConnection() {
        return mConnection;
    }

    public void setmConnection(ServiceConnection mConnection) {
        this.mConnection = mConnection;
    }

    public GameTime getGameTime() {
        return gameTime;
    }

    public GameMove getGameMove() {
        return gameMove;
    }

    public GameDatabase getGameDatabase() {
        return gameDatabase;
    }

    public void setGameDatabase(GameDatabase gameDatabase) {
        this.gameDatabase = gameDatabase;
    }

    public ImageView getPlayerJoinedAva() {
        return playerJoinedAva;
    }

    public void setPlayerJoinedAva(ImageView playerJoinedAva) {
        this.playerJoinedAva = playerJoinedAva;
    }

    public OpponentDisconnectReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(OpponentDisconnectReceiver receiver) {
        this.receiver = receiver;
    }

    public GameStart getGameStart() {
        return gameStart;
    }

    public GameEnd getGameEnd() {
        return gameEnd;
    }

    public GameGo getGameGo() {
        return gameGo;
    }

    public Peer2Peer getPeer2Peer() {
        return peer2Peer;
    }

    public MenuItem getSendMessage() {
        return sendMessage;
    }

    public void setSendMessage(MenuItem sendMessage) {
        this.sendMessage = sendMessage;
    }

    public String getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(String player1Id) {
        this.player1Id = player1Id;
    }

    public String getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(String player2Id) {
        this.player2Id = player2Id;
    }

    public void setBackstack(boolean backstack) {
        this.backstack = backstack;
    }
}