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

   /* public String getStringReasonByInt(int reason) {
        String s = "";
        switch (reason) {
            case 0:
                s = CHECKMATE;
                break;
            case 1:
                s = RESIGN;
                break;
            case 2:
                s = TIME_OVER;
                break;
            case 3:
                s = DISCONNECT;
                break;
            case 4:
                s = DRAW_BY_AGREEMENT;
                break;
            case 5:
                s = INSUFFICIENT_MATERIAL;
                break;
            case 6:
                s = STALEMATE;
                break;
            case 7:
                s = THREEFOLD_REPETITION;
                break;
            case 8:
                s = NO_PROGRESS;
                break;
        }
        return s;
    }
*/
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
        this.timestamp = new HashMap<String, Object>();
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


    /*    public static final String CHECKMATE = "Checkmate";
    public static final String RESIGN = "Player resigned";
    public static final String TIME_OVER = "Time over";
    public static final String DISCONNECT = "OnDisconnect";
    public static final String DRAW_BY_AGREEMENT = "Draw by agreement";
    public static final String INSUFFICIENT_MATERIAL = "Insufficient material";
    public static final String STALEMATE = "Stalemate";
    public static final String THREEFOLD_REPETITION = "Threefold repetition";
    public static final String NO_PROGRESS = "50 moves no progress";*/
}
