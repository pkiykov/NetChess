package com.pkiykov.netchess.logic;

import android.content.Context;
import android.os.CountDownTimer;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.fragments.Game;
import com.pkiykov.netchess.pojo.PlayerGameParams;
import com.pkiykov.netchess.pojo.RunningGame;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.pkiykov.netchess.logic.GameEnd.getPlayerMovesCount;
import static com.pkiykov.netchess.pojo.Player.TIMESTAMP;

public class GameTime {
    public static final int NO_TIME_CONTROL = 0;
    private static final int MINUTES_PER_GAME = 1;
    private static final int SECONDS_PER_MOVE = 2;
    private static final int FISHER_TIMING = 3;
    private static final int MOVES_MINUTES = 4;
    static final int HOURGLASS = 5;
    private static final String FORMAT = "%02d:%02d:%02d";
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss", Locale.ENGLISH);
    private Game game;

    public GameTime(Game game) {
        this.game = game;
    }

    void setTimeFromTimer() {
        if (game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeControl() != NO_TIME_CONTROL) {
            long t = timeRest(game.getTimer1().getText().toString());
            game.getRunningGame().getPlayer1().getPlayerGameParams().setTimeRest(t);
        }
        if (game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeControl() != NO_TIME_CONTROL) {
            long t2 = timeRest(game.getTimer2().getText().toString());
            game.getRunningGame().getPlayer2().getPlayerGameParams().setTimeRest(t2);
        }
    }

    static String timeFormat(long millis) {
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;
        return String.format(FORMAT, hour, minute, second);
    }

    static long timeRest(String timeLeft) {
        Date d = null;
        try {
            d = simpleDateFormat.parse(timeLeft);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d.getTime() + 10800000;
    }

    private static String countDownTimerFormat(long millisUntilFinished) {
        return String.format(FORMAT,
                TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
    }


    public static String getTimeInfo(Context context, int timeControl, int firstSpinner, int secondSpinner) {
        String time = "";
        switch (timeControl) {
            case MINUTES_PER_GAME:
                time = firstSpinner + " " + context.getString(R.string.minutes_per_game);
                break;
            case SECONDS_PER_MOVE:
                time = firstSpinner + " " + context.getString(R.string.seconds_per_move);
                break;
            case FISHER_TIMING:
                time = secondSpinner + "/" + firstSpinner + " " + context.getString(R.string.fisher_timing);
                break;
            case MOVES_MINUTES:
                time = secondSpinner + "/" + firstSpinner + " " + context.getString(R.string.moves_minutes);
                break;
            case HOURGLASS:
                time = firstSpinner + " " + context.getString(R.string.seconds) + context.getString(R.string.hourglass);
                break;
        }
        return time;
    }

    static void stopTimers(CountDownTimer countDownTimer1, CountDownTimer countDownTimer2) {
        if (countDownTimer1 != null) {
            countDownTimer1.cancel();
        }
        if (countDownTimer2 != null) {
            countDownTimer2.cancel();
        }
    }

    private void time1up() {
        final long startTime = game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeRest();
        final long tmp = System.currentTimeMillis();
        game.setCountDownTimer1(new CountDownTimer(999999999, 1000) {
            public void onTick(long millisUntilFinished) {
                long elapsedTime = System.currentTimeMillis() - tmp;
                game.getTimer1().setText(countDownTimerFormat(elapsedTime + startTime));
            }

            public void onFinish() {
                game.getTimer1().setText(game.getString(R.string.loser));
                game.getTimer2().setText(game.getString(R.string.winner));
                game.getRunningGame().setStatus(RunningGame.PLAYER_2_WIN);
                game.getGameEnd().recordResult();
            }
        }.start());
    }

    void setTimeRest() {
        if (game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeControl() != NO_TIME_CONTROL) {
            long t = timeRest(game.getTimer1().getText().toString());
            game.getRunningGame().getPlayer1().getPlayerGameParams().setTimeRest(t);
        }
        if (game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeControl() != NO_TIME_CONTROL) {
            long t = timeRest(game.getTimer2().getText().toString());
            game.getRunningGame().getPlayer2().getPlayerGameParams().setTimeRest(t);
        }
    }

    private void startCountDown1(long milliseconds) {
        game.setCountDownTimer1(new CountDownTimer(milliseconds, 1000) {
            public void onTick(long millisUntilFinished) {
                game.getTimer1().setText(countDownTimerFormat(millisUntilFinished));
            }

            public void onFinish() {
                game.getTimer1().setText(game.getString(R.string.loser));
                game.getTimer2().setText(game.getString(R.string.winner));
                game.getRunningGame().setStatus(RunningGame.PLAYER_2_WIN);
                game.getGameEnd().recordResult();
            }
        }.start());
    }

    private void startCountDown2(long milliseconds) {
        game.setCountDownTimer2(new CountDownTimer(milliseconds, 1000) {

            public void onTick(long millisUntilFinished) {
                game.getTimer2().setText(countDownTimerFormat(millisUntilFinished));
            }

            public void onFinish() {
                game.getTimer2().setText(game.getString(R.string.loser));
                game.getTimer1().setText(game.getString(R.string.winner));
                game.getRunningGame().setStatus(RunningGame.PLAYER_1_WIN);
                game.getGameEnd().recordResult();
            }
        }.start());
    }

    private void time2up() {
        final long startTime = game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeRest();
        final long tmp = System.currentTimeMillis();
        game.setCountDownTimer2(new CountDownTimer(999999999, 1000) {
            public void onTick(long millisUntilFinished) {
                long elapsedTime = System.currentTimeMillis() - tmp;
                game.getTimer2().setText(countDownTimerFormat(elapsedTime + startTime));
            }

            public void onFinish() {
                game.getTimer2().setText(game.getString(R.string.loser));
                game.getTimer1().setText(game.getString(R.string.winner));
                game.getRunningGame().setStatus(RunningGame.PLAYER_1_WIN);
                game.getGameEnd().recordResult();
            }
        }.start());
    }

    void changeTimers(boolean color) {
        if (color) {
            switch (game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeControl()) {
                case MINUTES_PER_GAME:
                    startCountDown1(game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeRest() + 999);
                    break;
                case SECONDS_PER_MOVE:
                    game.getTimer2().setText(timeFormat(game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1() * 1000));
                    startCountDown1(999 + game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1() * 1000);
                    break;
                case FISHER_TIMING:
                    startCountDown1(game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeRest() + 999 +
                            (game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1() * 1000));
                    break;
                case MOVES_MINUTES:
                    if (getPlayerMovesCount(game.getRunningGame()) % game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker2() == 0) {
                        game.getRunningGame().getPlayer1().getPlayerGameParams().setTimeRest(game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1() * 60000);
                        game.getTimer2().setText(timeFormat(game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeRest()));
                    }
                    startCountDown1(game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeRest() + 999);
                    break;
                case HOURGLASS:
                    if (game.getCountDownTimer1() != null) {
                        game.getCountDownTimer1().cancel();
                    }
                    startCountDown1(game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeRest() + 999);
                    break;
            }
            if (game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeControl() == HOURGLASS) {
                time2up();
            }
            game.getPlayer1Layout().setBackgroundResource(R.drawable.active_player_selection);
            game.getPlayer2Layout().setBackground(null);
        } else {
            switch (game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeControl()) {
                case MINUTES_PER_GAME:
                    startCountDown2(game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeRest() + 999);
                    break;
                case SECONDS_PER_MOVE:
                    game.getTimer1().setText(timeFormat(game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker1() * 1000));
                    startCountDown2(999 + game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker1() * 1000);
                    break;
                case FISHER_TIMING:
                    startCountDown2(game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeRest() + 999 +
                            (game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker1() * 1000));
                    break;
                case MOVES_MINUTES:
                    if (getPlayerMovesCount(game.getRunningGame()) % game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker2() == 0) {
                        game.getRunningGame().getPlayer2().getPlayerGameParams().setTimeRest(game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker1() * 60000);
                        game.getTimer1().setText(timeFormat(game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeRest()));
                    }
                    startCountDown2(game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeRest() + 999);
                    break;
                case HOURGLASS:
                    if (game.getCountDownTimer2() != null) {
                        game.getCountDownTimer2().cancel();
                    }
                    startCountDown2(game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeRest() + 999);
                    break;
            }
            if (game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeControl() == HOURGLASS) {
                time1up();
            }
            game.getPlayer2Layout().setBackgroundResource(R.drawable.active_player_selection);
            game.getPlayer1Layout().setBackground(null);
        }
    }

    void setPeer2PeerTimeRest() {
        if (game.getRunningGame().isThisPlayerPlaysWhite()) {
            game.getPeer2Peer().sendAction(TIMESTAMP, game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeRest());
        } else {
            game.getPeer2Peer().sendAction(TIMESTAMP, game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeRest());
        }
        if (game.getRunningGame().getStatus() != RunningGame.ON_PAUSED) {
            game.getGameTime().changeTimers(game.getGameExtraParams().isColor());
        }
    }

    void setOnlineTimeRest() {
        if (game.getRunningGame().isThisPlayerPlaysWhite()) {
            game.getGameDatabase().getGameRef().child(RunningGame.PLAYER_1).child(PlayerGameParams.PLAYER_GAME_PARAMS).child(PlayerGameParams.TIME_REST)
                    .setValue(game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeRest()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (game.getRunningGame() != null && game.getRunningGame().getStatus() != RunningGame.ON_PAUSED) {
                        game.getGameTime().changeTimers(game.getGameExtraParams().isColor());
                    }
                }
            });
        } else {
            game.getGameDatabase().getGameRef().child(RunningGame.PLAYER_2).child(PlayerGameParams.PLAYER_GAME_PARAMS).child(PlayerGameParams.TIME_REST)
                    .setValue(game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeRest()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (game.getRunningGame() != null && game.getRunningGame().getStatus() != RunningGame.ON_PAUSED) {
                        game.getGameTime().changeTimers(game.getGameExtraParams().isColor());
                    }
                }
            });
        }
    }

    static void resetTimeRest(boolean thisPlayerPlaysWhite, DatabaseReference gameRef) {
        if (thisPlayerPlaysWhite) {
            gameRef.child(RunningGame.PLAYER_1).child(PlayerGameParams.PLAYER_GAME_PARAMS).child(PlayerGameParams.TIME_REST)
                    .setValue(0L);
        } else {
            gameRef.child(RunningGame.PLAYER_2).child(PlayerGameParams.PLAYER_GAME_PARAMS).child(PlayerGameParams.TIME_REST)
                    .setValue(0L);
        }
    }

    public void timeControl() {
        long time = 0L;
        String timeText = "";
        if (game.getRunningGame().getPlayer1() != null) {
            switch (game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeControl()) {
                case MINUTES_PER_GAME:
                    time = game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1() * 60000;
                    timeText = game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1() + " "
                            + game.getString(R.string.minutes_per_game);
                    break;
                case SECONDS_PER_MOVE:
                    time = game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1() * 1000;
                    timeText = game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1() + " "
                            + game.getString(R.string.seconds_per_move);
                    break;
                case FISHER_TIMING:
                    time = game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker2() * 60000;
                    timeText = game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker2() + "/" +
                            game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1() + " "
                            + game.getString(R.string.fisher_timing);
                    break;
                case MOVES_MINUTES:
                    time = game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1() * 60000;
                    timeText = game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker2() + "/" +
                            game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1() + " "
                            + game.getString(R.string.moves_minutes);
                    break;
                case HOURGLASS:
                    time = game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1() * 1000;
                    timeText = game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1() + " "
                            + game.getString(R.string.seconds) + game.getString(R.string.hourglass);
                    break;
                default:
                    game.getTimer1().setText("∞");
                    break;
            }
            game.getPlayer1TimeControlDescription().setText(timeText);
            if (game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeControl() != NO_TIME_CONTROL) {
                game.getTimer1().setText(GameTime.timeFormat(time));
            }

        }
        if (game.getRunningGame().getPlayer2() != null) {
            switch (game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeControl()) {
                case MINUTES_PER_GAME:
                    time = game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker1() * 60000;
                    timeText = game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker1() + " "
                            + game.getString(R.string.minutes_per_game);
                    break;
                case SECONDS_PER_MOVE:
                    time = game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker1() * 1000;
                    timeText = game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker1() + " "
                            + game.getString(R.string.seconds_per_move);
                    break;
                case FISHER_TIMING:
                    time = game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker2() * 60000;
                    timeText = game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker2() + "/" +
                            game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker1() + " "
                            + game.getString(R.string.fisher_timing);
                    break;
                case MOVES_MINUTES:
                    time = game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker1() * 60000;
                    timeText = game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker2() + "/" +
                            game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker1() + " "
                            + game.getString(R.string.moves_minutes);
                    break;
                case HOURGLASS:
                    time = game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker1() * 1000;
                    timeText = game.getRunningGame().getPlayer2().getPlayerGameParams().getTimePicker1() + " "
                            + game.getString(R.string.seconds) + game.getString(R.string.hourglass);
                    break;
                default:
                    game.getTimer2().setText("∞");
                    break;
            }
            game.getPlayer2TimeControlDescription().setText(timeText);
            if (game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeControl() != NO_TIME_CONTROL) {
                game.getTimer2().setText(GameTime.timeFormat(time));
            }
        }

    }

}
