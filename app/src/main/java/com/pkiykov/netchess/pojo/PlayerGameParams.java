package com.pkiykov.netchess.pojo;

import java.io.Serializable;

public class PlayerGameParams implements Serializable {
    public static final String PAUSE_COUNT = "pauseCount";
    public static final String TIME_REST = "timeRest";
    public static final String PLAYER_GAME_PARAMS = "playerGameParams";

    private long timeRest;
    private int timePicker1, timePicker2, timeControl, handicap, pauseCount;
    private boolean cancelableMoves;

    public PlayerGameParams() {
        handicap = -1;
        cancelableMoves = false;
    }

    public long getTimeRest() {
        return timeRest;
    }

    public void setTimeRest(long timeRest) {
        this.timeRest = timeRest;
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

    public int getTimeControl() {
        return timeControl;
    }

    public void setTimeControl(int timeControl) {
        this.timeControl = timeControl;
    }

    public int getHandicap() {
        return handicap;
    }

    public void setHandicap(int handicap) {
        this.handicap = handicap;
    }

    public int getPauseCount() {
        return pauseCount;
    }

    public void setPauseCount(int pauseCount) {
        this.pauseCount = pauseCount;
    }

    public boolean isCancelableMoves() {
        return cancelableMoves;
    }

    public void setCancelableMoves(boolean cancelableMoves) {
        this.cancelableMoves = cancelableMoves;
    }
}
