package com.pkiykov.netchess.pojo;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class FinishedGame implements Serializable {



    public static final String RESULT_DRAW = "1/2:1/2";
    public static final String RESULT_WIN = "1:0";
    public static final String RESULT_LOSE = "0:1";
    public static final String MOVE_LIST = "MoveList";
    public static final String FINISHED_GAME = "finished game";
    public static final int WON_GAMES = 2;
    public static final int LOST_GAMES = 3;
    public static final int DRAWN_GAMES = 4;
    public static final int ALL_GAMES = 5;

    public static final int END_GAME_NO_PROGRESS = 100;
    public static final int REASON_CHECKMATE = 0;
    public static final int REASON_RESIGN = 1;
    public static final int REASON_TIME_OVER = 2;
    public static final int REASON_DISCONNECT = 3;
    public static final int REASON_DRAW_BY_AGREEMENT = 4;
    public static final int REASON_INSUFFICIENT_MATERIAL = 5;
    public static final int REASON_STALEMATE = 6;
    public static final int REASON_THREEFOLD_REPETITION = 7;
    public static final int REASON_NO_PROGRESS = 8;

    private Player player1, player2;
    private ArrayList<String> moveList;
    private String result;
    private int size;
    private int reason;
    private int timeControl;
    private int timePicker1;
    private int timePicker2;
    private HashMap<String, Object> timestamp;

    public FinishedGame() {
    }

    public FinishedGame(Player player1, Player player2, ArrayList<String> moveList, String result, int reason, int size, int timeControl, int timePicker1, int timePicker2) {
        this.player1 = player1;
        this.player2 = player2;
        this.moveList = moveList;
        this.result = result;
        this.size = size;
        this.reason = reason;
        this.timeControl = timeControl;
        this.timePicker1 = timePicker1;
        this.timePicker2 = timePicker2;
    }

    private HashMap<String, Object> timestampCreated() {
        HashMap<String, Object> timestampNow = new HashMap<>();
        timestampNow.put("timestamp", ServerValue.TIMESTAMP);
        return timestampNow;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public ArrayList<String> getMoveList() {
        return moveList;
    }

    public void setMoveList(ArrayList<String> moveList) {
        this.moveList = moveList;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = new HashMap<>();
        this.timestamp.put("timestamp", timestamp);
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    public int getTimeControl() {
        return timeControl;
    }

    public void setTimeControl(int timeControl) {
        this.timeControl = timeControl;
    }

    public int getTimePicker1() {
        return timePicker1;
    }

    public void setTimePicker1(int timePicker1) {
        this.timePicker1 = timePicker1;
    }

    public int getTimePicker2() {
        return timePicker2;
    }

    public void setTimePicker2(int timePicker2) {
        this.timePicker2 = timePicker2;
    }

    public HashMap<String, Object> getTimestamp() {
        return timestamp;
    }

    @Exclude
    public long getTimestampCreatedLong() {
        return (long) timestamp.get("timestamp");
    }

}
