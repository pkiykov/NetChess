package com.pkiykov.netchess.pojo;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Player implements Serializable {
    public static final String NAME = "name";
    public static final String AVATARS = "avatars";
    public static final String PLAYERS = "players";
    public static final String TIMESTAMP = "timestamp";
    public static final String PLAYER_ID = "playerId";
    public static final String PLAYER_RATING = "rating";
    public static final String BIRTHDATE ="birthdate" ;

    private String id, birthdate, name;
    private int rating, wins, losses, draws, rank;
    private long timestamp;
    private boolean color;
    private PlayerGameParams playerGameParams;

    public Player() {
        rating = 1400;
    }

    public int getAge() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date birth = null;
        try {
                birth = dateFormat.parse(this.birthdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date current = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(current.getTime() - birth.getTime());
        return calendar.get(Calendar.YEAR) - 1970;
    }

    @Exclude
    private int getK() {
        int rating = this.getRating();
        int age = this.getAge();
        int gamesCount = this.getDraws() + this.getWins() + this.getLosses();
        int k;
        if (rating >= 2400) {
            k = 10;
        } else {
            k = 20;
        }
        if ((rating < 2300 && age < 18) || (gamesCount <= 30)) {
            k = 40;
        }
        return k;
    }

    @Exclude
    private double getExpValue(int rating) {
        return 1.0 / (1.0 + (Math.pow(10.0, ((double) (rating - this.getRating()) / 400.0))));
    }

    @Exclude
    public void resultsAfterWin(int rating) {
        setRating((int) ((double) (getRating()) + (double) (getK()) * (1.0 - getExpValue(rating))));
        setWins(getWins() + 1);
    }

    @Exclude
    public void resultsAfterLose(int rating) {
        setRating((int) ((double) (getRating()) + (double) (getK()) * (0.0 - getExpValue(rating))));
        setLosses(getLosses() + 1);
    }

    @Exclude
    public void resultsAfterDraw(int rating) {
        setRating((int) ((double) (getRating()) + (double) (getK()) * (0.5 - getExpValue(rating))));
        setDraws(getDraws() + 1);
    }
    @Exclude
    public int getRank() {
        return rank;
    }
    @Exclude
    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getWins() {
        return wins;
    }

    private void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    private void setLosses(int losses) {
        this.losses = losses;
    }

    public int getDraws() {
        return draws;
    }

    private void setDraws(int draws) {
        this.draws = draws;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public PlayerGameParams getPlayerGameParams() {
        return playerGameParams;
    }

    public void setPlayerGameParams(PlayerGameParams playerGameParams) {
        this.playerGameParams = playerGameParams;
    }
@Exclude
    public boolean isColor() {
        return color;
    }
    @Exclude
    public void setColor(boolean color) {
        this.color = color;
    }
}