package com.pkiykov.netchess.pojo;

import com.pkiykov.netchess.figures.Figure;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static com.pkiykov.netchess.pojo.Player.TIMESTAMP;

public class RunningGame implements Serializable {
    public static final String PLAYER_1 = "player1";
    public static final String PLAYER_2 = "player2";
    public static final String ID = "id";
    public static final String RUNNING_GAME = "running game";
    public static final String MOVE_LIST = "move list";
    public static final String GAME_STATUS = "status";
    public static final String CHAT_LOGS ="chatLogs" ;

    public static final int WAITING_OPPONENT = 0;
    public static final int RUNNING = 1;
    public static final int ON_PAUSED = 2;
    public static final int DRAW_BY_AGREEMENT = 3;
    public static final int INSUFFICIENT_MATERIAL = 4;
    public static final int THREEFOLD_REPETITION = 5;
    public static final int STALEMATE = 6;
    public static final int PLAYER_1_WIN = 7;
    public static final int PLAYER_2_WIN = 8;
    public static final int PLAYER_1_DISCONNECTED = 9;
    public static final int PLAYER_2_DISCONNECTED = 10;

    private  boolean isThisPlayerPlaysWhite;
    private String id;
    private Player player1;
    private Player player2;
    private Coordinates coordinates;
    private int status;
    private ArrayList<String> moveList, chatLogs;
    private HashMap<String, Object> timestamp;

    public RunningGame() {
        moveList = new ArrayList<>();
        chatLogs = new ArrayList<>();
    }

    public RunningGame(Player player1, Player player2) {
        this.player2 = player2;
        this.player1 = player1;
        coordinates = new Coordinates();
        moveList = new ArrayList<>();
        chatLogs = new ArrayList<>();
        timestamp = timestampCreated();
    }

    private HashMap<String, Object> timestampCreated() {
        HashMap<String, Object> timestampNow = new HashMap<>();
        timestampNow.put(TIMESTAMP, ServerValue.TIMESTAMP);
        return timestampNow;
    }

     @Exclude
     private static String castle(String move) {
         if (move.contains("K")) {
             if (Character.getNumericValue(move.charAt(1)) - Character.getNumericValue(move.charAt(3)) == 2) {
                 move = "0-0-0";
             } else if (Character.getNumericValue(move.charAt(3)) - Character.getNumericValue(move.charAt(1)) == 2) {
                 move = "0-0";
             }
         }
         return move;
     }

     @Exclude
     public static ArrayList<String> formatMoveList(ArrayList<String> moveList) {
         int count = 0;
         ArrayList<String> moveListFormatted = new ArrayList<>();
         while (moveList.size() > 0) {
             count++;
             String tmp2 = "";
             String tmp1 = castle(moveList.get(0));
             moveList.remove(0);
             if (moveList.size() > 0) {
                 tmp2 = castle(moveList.get(0));
                 moveList.remove(0);
             }
             moveListFormatted.add(count + ". " + tmp1 + " " + tmp2);
         }
         return moveListFormatted;
     }
    @Exclude
    public void writeMove(Figure f) {
        char figure = f.getClass().getSimpleName().charAt(0);
        if (f.getClass().getSimpleName().equals(Figure.KNIGHT)) {
            figure = 'N';
        }
        this.getMoveList().add(figure + Character.toString((char) (96 + this.getCoordinates().getA())) + String.valueOf(this.getCoordinates().getB())
                + Character.toString((char) (96 + this.getCoordinates().getA1())) + String.valueOf(this.getCoordinates().getB1()));
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ArrayList<String> getMoveList() {
        return moveList;
    }

    public void setMoveList(ArrayList<String> moveList) {
        this.moveList = moveList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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


    public ArrayList<String> getChatLogs() {
        return chatLogs;
    }

    public void setChatLogs(ArrayList<String> chatLogs) {
        this.chatLogs = chatLogs;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public HashMap<String, Object> getTimestamp() {
        return timestamp;
    }

    @Exclude
    public boolean isThisPlayerPlaysWhite() {
        return isThisPlayerPlaysWhite;
    }
    @Exclude
    public void setThisPlayerPlaysWhite(boolean thisPlayerPlaysWhite) {
        isThisPlayerPlaysWhite = thisPlayerPlaysWhite;
    }

    @Exclude
    public long getTimestampCreatedLong() {
        return (long) timestamp.get(TIMESTAMP);
    }
}
