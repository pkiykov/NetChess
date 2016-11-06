package com.pkiykov.netchess.logic;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.figures.Figure;
import com.pkiykov.netchess.fragments.Game;
import com.pkiykov.netchess.fragments.OnlineGamesList;
import com.pkiykov.netchess.fragments.RankedGameSettings;
import com.pkiykov.netchess.fragments.UnrankedGameSettings;
import com.pkiykov.netchess.pojo.RunningGame;

import java.util.concurrent.TimeUnit;

import static com.pkiykov.netchess.fragments.Game.GAME_TYPE;
import static com.pkiykov.netchess.fragments.Game.LAN_GAME;
import static com.pkiykov.netchess.fragments.Game.ONE_DEVICE_GAME;
import static com.pkiykov.netchess.fragments.Game.ONLINE_GAME;
import static com.pkiykov.netchess.logic.GameTime.stopTimers;
import static com.pkiykov.netchess.pojo.RunningGame.GAME_STATUS;
import static com.pkiykov.netchess.pojo.RunningGame.PLAYER_1_WIN;
import static com.pkiykov.netchess.pojo.RunningGame.PLAYER_2_WIN;

public class GameEnd {

    private Game game;
    private boolean win, gameHasBeenEnded;

    public GameEnd(Game game) {
        gameHasBeenEnded = false;
        win = false;
        this.game = game;
    }

    public void onOpponentDisconnect() {
        cancelDialogs();
        if (game.getRunningGame().getMoveList().size() > 0) {
            game.getGameExtraParams().setReason(3);
            if (game.getRunningGame().isThisPlayerPlaysWhite()) {
                game.getRunningGame().setStatus(RunningGame.PLAYER_2_DISCONNECTED);
            } else {
                game.getRunningGame().setStatus(RunningGame.PLAYER_1_DISCONNECTED);
            }
            recordResult();
        } else {
            if (game.getGameType() == ONLINE_GAME) {
                if (game.getGameStart().isFirstPlayer()) {
                    restartOnlineGame();
                } else {
                    game.getGameDatabase().removeDatabaseListenersForCurrentGame();
                    Fragment f = game.getFragmentManager().findFragmentByTag(OnlineGamesList.class.getSimpleName());
                    if (f != null) {
                        ((OnlineGamesList) f).unbindService();
                    }
                    outOnlineGame();
                }
            } else {
                ((GameActivity) game.getActivity()).getPeer2Peer().disconnect(false);
                waitAndStartFragment(new RankedGameSettings(), 3);
            }
            Toast.makeText(game.getActivity(), R.string.opponent_disconnected, Toast.LENGTH_SHORT).show();
        }
    }


    public void cancelDialogs() {
        if (game.getPauseDialog() != null) {
            if (game.getPauseDialog().isShowing()) {
                game.getGameGo().showFigures();
                game.getPauseDialog().dismiss();
                return;
            }
        }
        if (game.getDrawOfferedDialog() != null) {
            if (game.getDrawOfferedDialog().isShowing()) {
                game.getDrawOfferedDialog().dismiss();
                return;
            }
        }
        if (game.getWaitingPlayers() != null) {
            if (game.getWaitingPlayers().isShowing()) {
                game.getWaitingPlayers().dismiss();
                return;
            }
        }
        if (game.getGameMove().getPawnToQueenDialog() != null) {
            if (game.getGameMove().getPawnToQueenDialog().isShowing()) {
                game.getGameMove().getPawnToQueenDialog().dismiss();
            }
        }
    }

    private void disconnectWithoutMoves(){
        Fragment fragment = new UnrankedGameSettings();
        Bundle bundle = new Bundle();
        bundle.putInt(GAME_TYPE, LAN_GAME);
        fragment.setArguments(bundle);
        ((GameActivity) game.getActivity()).fragmentTransaction(fragment);
    }

    public void onThisPlayerDisconnect() {
        if(game.getGameType()== ONLINE_GAME) {
            game.getGameDatabase().removeDatabaseListenersForCurrentGame();
            finishService();
            if (!game.getGameStart().isFirstPlayer()) {
                Fragment f = game.getActivity().getFragmentManager().findFragmentByTag(OnlineGamesList.class.getSimpleName());
                ((OnlineGamesList) f).unbindService();
            }
            unbindService();
            if (game.getRunningGame().getStatus() == RunningGame.WAITING_OPPONENT || game.getRunningGame().getMoveList().size() == 0) {
                waitAndStartFragment(new RankedGameSettings(), 3);
            } else {
                gameHasBeenEnded = true;
            }
        }

        stopTimers(game.getCountDownTimer1(), game.getCountDownTimer2());
        cancelDialogs();
        disableMenu();
        game.getPlayer1Layout().setBackground(null);
        game.getPlayer2Layout().setBackground(null);
        game.setRunningGame(null);
        endGameDialogShow(game.getString(R.string.this_disconnected));
    }

    public void unbindService() {
        try {
            game.getActivity().unbindService(game.getmConnection());
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void leftOnlineGame() {
        if (game.getRunningGame().isThisPlayerPlaysWhite()) {
            game.getGameDatabase().getGameRef().child(RunningGame.PLAYER_1).setValue(null);
        } else {
            game.getGameDatabase().getGameRef().child(RunningGame.PLAYER_2).setValue(null);
        }
    }

    void restartOnlineGame() {
        game.getGameDatabase().removeDatabaseListenersForCurrentGame();
        finishService();
        unbindService();
        game.getGameDatabase().getGameRef().removeValue();
      if(game.getRunningGame().isThisPlayerPlaysWhite()){
          game.getRunningGame().setPlayer2(null);
      }else{
          game.getRunningGame().setPlayer1(null);
      }
        final Fragment f = new Game();
        f.setArguments(game.getArguments());
        waitAndStartFragment(f, 3);
    }

    private void waitAndStartFragment(final Fragment f, int secondsToWait) {
        new AsyncTask<Integer, Integer, String>() {

            @Override
            protected String doInBackground(Integer[] objects) {
                try {
                    TimeUnit.SECONDS.sleep(objects[0]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String o) {
                super.onPostExecute(o);
                game.setRunningGame(null);
                FragmentTransaction ft = game.getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, f).commit();
                if(game.getGameStart().getPleaseWaitDialog()!= null && game.getGameStart().getPleaseWaitDialog().isShowing()) {
                    game.getGameStart().getPleaseWaitDialog().dismiss();
                }
            }
        }.execute(secondsToWait);
    }

    void outOnlineGame() {
        finishService();
        unbindService();
        waitAndStartFragment(new RankedGameSettings(), 3);
    }

    private boolean win(boolean sideToMove) {
        game.getGameExtraParams().setKingIsChecked(false);

        int a = 0, b = 0;
        for (Figure f : game.getGameExtraParams().getFigures()) {
            if (f.getClass().getSimpleName().equals(Figure.KING) && f.isColor() == sideToMove) {
                a = f.getA();
                b = f.getB();
                break;
            }
        }
        win = false;
        for (Figure f : game.getGameExtraParams().getFigures()) {
            if (f.isColor() != sideToMove) {
                if (f.move(a, b, true)) {
                    win = true;
                    game.getGameExtraParams().setKingIsChecked(true);
                    String move = game.getRunningGame().getMoveList().get(game.getRunningGame().getMoveList().size() - 1) + "+";
                    game.getRunningGame().getMoveList().set(game.getRunningGame().getMoveList().size() - 1, move);
                    break;
                }
            }
        }
        for (int n = 0; n < game.getGameExtraParams().getFigures().size(); n++) {
            Figure f = game.getGameExtraParams().getFigures().get(n);
            if (f.isColor() == sideToMove) {
                for (int i = 1; i < 9; i++) {
                    for (int j = 1; j < 9; j++) {
                        if (f.move(i, j, true)) {
                            win = false;
                            return false;
                        }
                    }
                }
            }
        }
        String move = game.getRunningGame().getMoveList().get(game.getRunningGame().getMoveList().size() - 1).replace('+', '#');
        game.getRunningGame().getMoveList().set(game.getRunningGame().getMoveList().size() - 1, move);
        return true;
    }

    boolean checkEndGame() {
        if (win(game.getGameExtraParams().isColor())) {
            if (game.getGameExtraParams().isColor() && win) {
                game.getRunningGame().setStatus(PLAYER_1_WIN);
            } else if (!game.getGameExtraParams().isColor() && win) {
                game.getRunningGame().setStatus(PLAYER_2_WIN);
            } else {
                game.getRunningGame().setStatus(RunningGame.STALEMATE);
                game.getGameExtraParams().setReason(6);
            }
            return true;
        } else {
            if (game.getGameExtraParams().getCountNoProgressMoves() == 100) {
                game.getGameExtraParams().setReason(8);
                return true;
            }
            int countBlackFigures = 0;
            int countWhiteFigures = 0;
            boolean isWhiteLightFigure = false;
            boolean isBlackLightFigure = false;
            for (Figure f : game.getGameExtraParams().getFigures()) {
                if (f.getClass().getSimpleName().equals(Figure.BISHOP) || f.getClass().getSimpleName().equals(Figure.KNIGHT)) {
                    if (f.isColor()) {
                        isWhiteLightFigure = true;
                    } else {
                        isBlackLightFigure = true;
                    }
                }
                if (f.isColor()) {
                    countWhiteFigures++;
                } else {
                    countBlackFigures++;
                }
            }
            if (((countBlackFigures == 2 && isBlackLightFigure) ^ (countBlackFigures == 1))
                    && ((countWhiteFigures == 2 && isWhiteLightFigure) ^ (countWhiteFigures == 1))) {
                game.getRunningGame().setStatus(RunningGame.INSUFFICIENT_MATERIAL);
                game.getGameExtraParams().setReason(5);
                return true;
            }
            if (game.getRunningGame().getMoveList().size() > 5) {
                if (threeFoldRepetition()) {
                    game.getGameExtraParams().setReason(7);
                    return true;
                }
            }
        }
        return false;
    }

    public void recordResult() {
        stopTimers(game.getCountDownTimer1(), game.getCountDownTimer2());
        String message = "";
        if (game.getGameExtraParams().getReason() == 3) {
            if (game.getRunningGame().getStatus() == RunningGame.PLAYER_2_DISCONNECTED) {
                message = game.getString(R.string.opponent_disconnected) + " " + game.getString(R.string.white_wins);
                game.getTimer2().setText(game.getString(R.string.loser));
                game.getTimer1().setText(game.getString(R.string.winner));
                game.getRunningGame().setStatus(PLAYER_1_WIN);
            } else if (game.getRunningGame().getStatus() == RunningGame.PLAYER_1_DISCONNECTED) {
                message = game.getString(R.string.opponent_disconnected) + " " + game.getString(R.string.black_wins);
                game.getTimer1().setText(game.getString(R.string.loser));
                game.getTimer2().setText(game.getString(R.string.winner));
                game.getRunningGame().setStatus(PLAYER_2_WIN);
            }
        } else if (game.getRunningGame().getStatus() == PLAYER_1_WIN ^ game.getRunningGame().getStatus()
                == PLAYER_2_WIN) {
            if (game.getGameExtraParams().getReason() != 1) {
                if (game.getGameExtraParams().isColor()) {
                    message = game.getString(R.string.black_wins);
                    game.getTimer1().setText(game.getString(R.string.loser));
                    game.getTimer2().setText(game.getString(R.string.winner));
                    game.getRunningGame().setStatus(PLAYER_2_WIN);
                } else {
                    message = game.getString(R.string.white_wins);
                    game.getTimer2().setText(game.getString(R.string.loser));
                    game.getTimer1().setText(game.getString(R.string.winner));
                    game.getRunningGame().setStatus(PLAYER_1_WIN);
                }
            } else {
                if (game.getGameExtraParams().getReason() == PLAYER_2_WIN) {
                    message = game.getString(R.string.white_resigned) + " " + game.getString(R.string.black_wins);
                    game.getTimer1().setText(game.getString(R.string.loser));
                    game.getTimer2().setText(game.getString(R.string.winner));
                    game.getRunningGame().setStatus(PLAYER_2_WIN);
                } else {
                    message = game.getString(R.string.black_resigned) + " " + game.getString(R.string.white_wins);
                    game.getTimer2().setText(game.getString(R.string.loser));
                    game.getTimer1().setText(game.getString(R.string.winner));
                    game.getRunningGame().setStatus(PLAYER_1_WIN);
                }
            }
        } else {
            game.getTimer1().setText(game.getString(R.string.draw));
            game.getTimer2().setText(game.getString(R.string.draw));
            if (game.getGameExtraParams().getCountNoProgressMoves() == 100) {
                message = game.getString(R.string.no_progress_draw);
            } else if (game.getRunningGame().getStatus() == RunningGame.THREEFOLD_REPETITION) {
                message = game.getString(R.string.threefold_draw);
            } else if (game.getRunningGame().getStatus() == RunningGame.DRAW_BY_AGREEMENT) {
                message = game.getString(R.string.draw_by_agreement);
            } else if (game.getRunningGame().getStatus() == RunningGame.INSUFFICIENT_MATERIAL) {
                message = game.getString(R.string.material_draw);
            } else if (game.getRunningGame().getStatus() == RunningGame.STALEMATE) {
                message = game.getString(R.string.stalemate_draw);
            }
        }
        if (game.getGameType() == ONLINE_GAME) {
            game.getGameDatabase().removeDatabaseListenersForCurrentGame();
            game.getGameEnd().finishService();
            game.getGameEnd().unbindService();
            if (!gameHasBeenEnded) {
                game.getGameDatabase().recordGameResultToDB();
            }
        } else if (game.getGameType() == LAN_GAME) {
            if (game.getRunningGame().getStatus() == RunningGame.PLAYER_1_WIN
                    ^ game.getRunningGame().getStatus() == RunningGame.PLAYER_2_WIN
                    ^ game.getRunningGame().getStatus() == RunningGame.DRAW_BY_AGREEMENT) {
              game.getPeer2Peer().sendAction(GAME_STATUS, game.getRunningGame().getStatus());
            }
            if (!GameActivity.isActivityVisible()) {
                ((GameActivity) game.getActivity()).createNotification(game.getActivity(), game.getString(R.string.draw_offer_has_been_accepted), game.getString(R.string.game_over), 1234);
            }

        }
        disableMenu();
        game.getPlayer1Layout().setBackground(null);
        game.getPlayer2Layout().setBackground(null);
        game.setRunningGame(null);
        endGameDialogShow(message);
    }

    private void disableMenu() {
        if(game.getGameType() != ONE_DEVICE_GAME) {
            game.getOfferDraw().setVisible(false);
            game.getSendMessage().setVisible(false);
            game.getOpenLogs().setVisible(false);
        }else{
            game.getCancelMove().setVisible(false);
        }
        game.getPause().setVisible(false);
        game.getResign().setVisible(false);

    }

    static int getPlayerMovesCount(RunningGame game) {
        return game.getMoveList().size() / 2 + game.getMoveList().size() % 2;
    }

    public void finishService() {
        if(game.getmService()!= null) {
            game.getmService().setFlag(false);
        }
    }

    private void endGameDialogShow(String message) {
        AlertDialog.Builder b = new AlertDialog.Builder(game.getActivity());
        b.setMessage(message);
        b.setCancelable(false);
        b.setNeutralButton(game.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        b.setTitle(game.getString(R.string.game_over));
        AlertDialog ad = b.create();
        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Window view = ((AlertDialog) dialogInterface).getWindow();
                view.setBackgroundDrawableResource(R.drawable.dialog_background);
                view.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
        });
        ad.show();
    }

    private boolean threeFoldRepetition() {
        int countRepetition = 0;
        String position = game.getGameExtraParams().getPosition().getLast();
        for (int i = 0; i < game.getGameExtraParams().getPosition().size() - 1; i++) {
            if (game.getGameExtraParams().getPosition().get(i).equals(position)) {
                countRepetition++;
            }
        }
        if (countRepetition > 2) {
            game.getRunningGame().setStatus(RunningGame.THREEFOLD_REPETITION);
            return true;

        }
        return false;
    }

    void setGameHasBeenEnded(boolean gameHasBeenEnded) {
        this.gameHasBeenEnded = gameHasBeenEnded;
    }

    public void disconnectFromLanGame() {
        game.getPeer2Peer().disconnect(false);
        if(game.getRunningGame().getMoveList().size()>0) {
            game.getGameEnd().onThisPlayerDisconnect();
        }else{
            game.getGameEnd().disconnectWithoutMoves();
        }

    }
}
