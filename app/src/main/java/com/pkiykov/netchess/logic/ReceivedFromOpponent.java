package com.pkiykov.netchess.logic;

import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.figures.Figure;
import com.pkiykov.netchess.fragments.Game;
import com.pkiykov.netchess.pojo.Coordinates;
import com.pkiykov.netchess.pojo.RunningGame;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.pkiykov.netchess.fragments.Game.LAN_GAME;
import static com.pkiykov.netchess.fragments.Game.ONLINE_GAME;
import static com.pkiykov.netchess.logic.GameTime.HOURGLASS;
import static com.pkiykov.netchess.logic.GameTime.stopTimers;
import static com.pkiykov.netchess.logic.Peer2Peer.USER_INFO;
import static com.pkiykov.netchess.pojo.Player.NAME;
import static com.pkiykov.netchess.pojo.Player.PLAYER_ID;
import static com.pkiykov.netchess.pojo.Player.PLAYER_RATING;
import static com.pkiykov.netchess.pojo.RunningGame.DRAW_BY_AGREEMENT;
import static com.pkiykov.netchess.pojo.RunningGame.ON_PAUSED;
import static com.pkiykov.netchess.pojo.RunningGame.PLAYER_2_WIN;

public class ReceivedFromOpponent {

    private long dif;
    private Game game;
    private boolean moveFromOpponent;

    public ReceivedFromOpponent(Game game) {
        moveFromOpponent = false;
        this.game = game;
    }

    public void onGameStatusUpdated(int status) {
        if(game.getRunningGame()!=null) {
            String message = "";
            String title = "";
            if (game.getRunningGame().getStatus() == RunningGame.DRAW_BY_AGREEMENT && status == RunningGame.RUNNING) {
                game.getOfferDraw().setIntent(null);
                message = game.getString(R.string.draw_offer_has_been_declined);
                Toast.makeText(game.getActivity(), message, Toast.LENGTH_SHORT).show();
            } else if (game.getGameType() == LAN_GAME) {
                if (game.getRunningGame().getStatus() == RunningGame.WAITING_OPPONENT
                        && status == RunningGame.RUNNING) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                TimeUnit.SECONDS.sleep(2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            game.getPeer2Peer().sendAction(USER_INFO, game.getGameStart().packUserProfileInfo());
                        }
                    }).start();
                } else if (game.getRunningGame().getStatus() == RunningGame.DRAW_BY_AGREEMENT && status == DRAW_BY_AGREEMENT) {
                    game.getGameExtraParams().setReason(3);
                    game.getGameEnd().recordResult();
                    return;
                }
            }
            game.getRunningGame().setStatus(status);
            if (game.getRunningGame().getStatus() == ON_PAUSED) {
                stopTimers(game.getCountDownTimer1(), game.getCountDownTimer2());
                game.getGameTime().setTimeFromTimer();
                game.getGameGo().hideFigures();
                if (game.getGameType() == ONLINE_GAME) {
                    game.getGameTime().setOnlineTimeRest();
                } else {
                    game.getGameTime().setPeer2PeerTimeRest();
                }
                game.getPauseDialog().show();
                title = game.getString(R.string.game_is_paused);
            } else {
                if (game.getPauseDialog().isShowing()) {
                    game.getGameGo().resumeGame();
                }
                if (game.getRunningGame().getStatus() == RunningGame.DRAW_BY_AGREEMENT) {
                    if (game.getGameType() == ONLINE_GAME) {
                        if (game.getOfferDraw().getIntent() == null) {
                            if (game.getDrawOfferedDialog() == null) {
                                game.getGameGo().createDrawOfferedDialog();
                            }
                            game.getDrawOfferedDialog().show();
                            message = game.getString(R.string.your_opponent_offers_you_a_draw);
                        } else {
                            Toast.makeText(game.getActivity(), R.string.draw_offer_has_been_sent, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        game.getGameGo().createDrawOfferedDialog();
                        game.getDrawOfferedDialog().show();
                        message = game.getString(R.string.your_opponent_offers_you_a_draw);
                    }
                } else if (game.getRunningGame().getStatus() == RunningGame.PLAYER_1_WIN ^ game.getRunningGame().getStatus() == PLAYER_2_WIN) {
                    if (game.getGameType() == ONLINE_GAME) {
                        game.getGameEnd().finishService();
                        game.getGameEnd().unbindService();
                        game.getGameDatabase().removeDatabaseListenersForCurrentGame();
                        game.getGameEnd().setGameHasBeenEnded(true);
                    }
                    title = game.getString(R.string.game_over);
                    if (game.getRunningGame().isThisPlayerPlaysWhite()) {
                        message = game.getString(R.string.black_resigned);
                    } else {
                        message = game.getString(R.string.white_resigned);
                    }
                    game.getGameExtraParams().setReason(1);
                    game.getGameEnd().recordResult();
                }
            }
            if (!GameActivity.isActivityVisible()) {
                ((GameActivity) game.getActivity()).createNotification(game.getActivity(), message, title, 1234);
            }
        }
    }

    public void onCoordinatesUpdated(Coordinates coordinates) {
        game.getRunningGame().setCoordinates(coordinates);
        int n = game.getGameMove().findFigureByCoordinates();
        Figure f = game.getGameExtraParams().getFigures().get(n);
        game.getGameMove().coordinatesUpdated(f);
        if (!GameActivity.isActivityVisible()) {
            String message = game.getRunningGame().getMoveList().get(game.getRunningGame().getMoveList().size() - 1);
            String title = game.getString(R.string.move_has_been_done);
            ((GameActivity) game.getActivity()).createNotification(game.getActivity(), message, title, 1234);
        }
    }

    public void onChatLogsUpdated(String message) {
        boolean flag = true;
        if (game.getRunningGame().isThisPlayerPlaysWhite()) {
            if (message.startsWith(game.getRunningGame().getPlayer1().getName() + "[white]: ")) {
                flag = false;
            }
        } else {
            if (message.startsWith(game.getRunningGame().getPlayer2().getName() + "[black]: ")) {
                flag = false;
            }
        }
        if (flag) {
            game.getRunningGame().getChatLogs().add(message);
            Toast toast = Toast.makeText(game.getActivity(), message, Toast.LENGTH_LONG);
            if (game.getRunningGame().isThisPlayerPlaysWhite()) {
                if (!game.getGameGo().isFlipped()) {
                    toast.setGravity(Gravity.TOP, 0, 100);
                } else {
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                }
            } else {
                if (!game.getGameGo().isFlipped()) {
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                } else {
                    toast.setGravity(Gravity.TOP, 0, 100);
                }
            }

            View v = game.getActivity().findViewById(game.getOpenLogs().getItemId());
            game.getGameGo().pulsAnimationStart(v);
            if (game.getPauseDialog().isShowing()) {
                game.getGameGo().pulsAnimationStart(game.getOpenLogsButton());
                game.getChatLogsAdapter().notifyDataSetChanged();
            }
            toast.show();
        }
    }

    public void onOpponentTimeUpdated(long l) {
        if(game.getRunningGame()!= null) {
            if (l != 0L) {
                if (!game.getRunningGame().isThisPlayerPlaysWhite()) {
                    if (game.getRunningGame().getMoveList().size() < 2) {
                        game.getRunningGame().getPlayer1().getPlayerGameParams().setTimeRest(GameTime.timeRest(game.getTimer1().getText().toString()));
                        game.getRunningGame().getPlayer2().getPlayerGameParams().setTimeRest(GameTime.timeRest(game.getTimer2().getText().toString()));
                    }
                    if (game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeControl() == HOURGLASS) {
                        dif = game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeRest() - l;
                    }
                    game.getRunningGame().getPlayer1().getPlayerGameParams().setTimeRest(l);
                    game.getTimer1().setText(GameTime.timeFormat(l));
                    if (game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeControl() == HOURGLASS) {
                        game.getRunningGame().getPlayer2().getPlayerGameParams()
                                .setTimeRest(game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeRest() + dif);
                        game.getTimer2().setText(GameTime.timeFormat(game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeRest()));
                    }
                } else {
                    if (game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeControl() == HOURGLASS) {
                        dif = game.getRunningGame().getPlayer2().getPlayerGameParams().getTimeRest() - l;
                    }
                    game.getRunningGame().getPlayer2().getPlayerGameParams().setTimeRest(l);
                    game.getTimer2().setText(GameTime.timeFormat(l));
                    if (game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeControl() == HOURGLASS) {
                        game.getRunningGame().getPlayer1().getPlayerGameParams()
                                .setTimeRest(game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeRest() + dif);
                        game.getTimer1().setText(GameTime.timeFormat(game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeRest()));
                    }
                }
                if (game.getRunningGame().getStatus() != ON_PAUSED) {
                    stopTimers(game.getCountDownTimer1(), game.getCountDownTimer2());
                    game.getGameTime().changeTimers(game.getRunningGame().isThisPlayerPlaysWhite());
                }
            }
        }
    }

    boolean isMoveFromOpponent() {
        return moveFromOpponent;
    }

    public void setMoveFromOpponent(boolean moveFromOpponent) {
        this.moveFromOpponent = moveFromOpponent;
    }

    public void setOpponentProfileInfo(Map<String, String> userInfoMap) {
        String playerId = userInfoMap.get(PLAYER_ID);
        if (!playerId.isEmpty()) {
            String name = userInfoMap.get(NAME);
            String rating = userInfoMap.get(PLAYER_RATING);
            if (game.getRunningGame().isThisPlayerPlaysWhite()) {
                game.getRunningGame().getPlayer2().setName(name);
                game.getPlayer2_name().setText(name);
                game.getPlayer2_rating().setText(rating);
                game.getGameStart().loadPlayerPhoto(game.getPlayer2_ava(), playerId);
            } else {
                game.getRunningGame().getPlayer1().setName(name);
                game.getPlayer1_name().setText(name);
                game.getPlayer1_rating().setText(rating);
                game.getGameStart().loadPlayerPhoto(game.getPlayer1_ava(), playerId);
            }
        }
            game.getGameStart().makePlayerLayoutClickable();
    }
}
