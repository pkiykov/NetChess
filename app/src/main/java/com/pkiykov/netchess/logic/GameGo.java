package com.pkiykov.netchess.logic;

import android.animation.Animator;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.fragments.Game;
import com.pkiykov.netchess.fragments.Profile;
import com.pkiykov.netchess.pojo.Player;
import com.pkiykov.netchess.pojo.RunningGame;

import static com.pkiykov.netchess.fragments.Game.DRAW;
import static com.pkiykov.netchess.fragments.Game.LAN_GAME;
import static com.pkiykov.netchess.fragments.Game.ONE_DEVICE_GAME;
import static com.pkiykov.netchess.fragments.Game.ONLINE_GAME;
import static com.pkiykov.netchess.fragments.Game.RESIGN;
import static com.pkiykov.netchess.logic.GameTime.NO_TIME_CONTROL;
import static com.pkiykov.netchess.logic.GameTime.stopTimers;
import static com.pkiykov.netchess.pojo.FinishedGame.REASON_RESIGN;
import static com.pkiykov.netchess.pojo.RunningGame.CHAT_LOGS;
import static com.pkiykov.netchess.pojo.RunningGame.DRAW_BY_AGREEMENT;
import static com.pkiykov.netchess.pojo.RunningGame.GAME_STATUS;
import static com.pkiykov.netchess.pojo.RunningGame.ON_PAUSED;

public class GameGo {
    private Game game;
    private boolean isFlipped, drawOfferWasSent;


    public GameGo(Game game) {
        this.game = game;
        isFlipped = false;
        drawOfferWasSent = false;
    }

    public void prepareMenu() {
       if (game.getRunningGame() != null) {
            if (game.getGameType() == ONE_DEVICE_GAME) {
                boolean p1cancel = game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves();
                boolean p2cancel = game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves();
                if ((((p1cancel || p2cancel) && (((p1cancel && game.getGameExtraParams().isColor())|| (p2cancel && !game.getGameExtraParams().isColor()))))
                        ||(p1cancel && p2cancel))
                        && game.getRunningGame().getMoveList().size() > 0) {
                    game.getCancelMove().setVisible(true);
                } else {
                    game.getCancelMove().setVisible(false);
                }
            } else {
                game.getOfferDraw().setVisible(false);
                game.getPause().setVisible(false);
                if (game.getRunningGame() != null) {
                    if (game.getRunningGame().getStatus() == RunningGame.RUNNING) {
                        if (game.getGameExtraParams().getCountMoves() > 19 && !drawOfferWasSent ) {
                            game.getOfferDraw().setVisible(true);
                        }
                        if (game.getGameType() == ONLINE_GAME) {
                            if (((game.getRunningGame().getPlayer1().getPlayerGameParams().getPauseCount() < 4
                                    && game.getRunningGame().getPlayer1().getId().equals(game.getGameDatabase().getmAuth().getCurrentUser().getUid())
                                    || game.getRunningGame().getPlayer2().getPlayerGameParams().getPauseCount() < 4
                                    && game.getRunningGame().getPlayer2().getId().equals(game.getGameDatabase().getmAuth().getCurrentUser().getUid()))) &&
                                    game.getRunningGame().getMoveList().size() > 0) {
                                game.getPause().setVisible(true);
                            }
                        } else {
                            if (game.getRunningGame().getMoveList().size() > 0) {
                                game.getPause().setVisible(true);
                            }
                        }
                    }
                }
            }
       }
    }


    void userProfileClick() {
        game.getPlayer1_ava().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(Player.PLAYER_ID, game.getPlayer1Id());
                Fragment profile = new Profile();
                profile.setArguments(bundle);
                ((GameActivity) game.getActivity()).fragmentTransaction(profile);
            }
        });
        game.getPlayer2_ava().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(Player.PLAYER_ID, game.getPlayer2Id());
                Fragment profile = new Profile();
                profile.setArguments(bundle);
                ((GameActivity) game.getActivity()).fragmentTransaction(profile);
            }
        });
    }

    void createDrawOfferedDialog() {
        game.setDrawOfferedDialog(((GameActivity) game.getActivity()).createAlertDialog(game.getActivity(), null, false, "",
                game.getString(R.string.your_opponent_offers_you_a_draw), game.getString(R.string.accept),
                game.getString(R.string.decline), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        game.getRunningGame().setStatus(RunningGame.DRAW_BY_AGREEMENT);
                        game.getGameExtraParams().setReason(DRAW_BY_AGREEMENT);
                        game.getGameEnd().recordResult();
                        dialogInterface.dismiss();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        game.getRunningGame().setStatus(RunningGame.RUNNING);
                        if (game.getGameType() == ONLINE_GAME) {
                            game.getGameDatabase().getGameRef().child(RunningGame.GAME_STATUS).setValue(game.getRunningGame().getStatus());
                        } else {
                            game.getPeer2Peer().sendAction(GAME_STATUS, game.getRunningGame().getStatus());
                        }
                        dialogInterface.dismiss();
                    }
                }));
    }


    void resumeGame() {
        showFigures();
        game.getGameTime().changeTimers(game.getGameExtraParams().isColor());
        game.getPauseDialog().dismiss();
    }

    void hideFigures() {
        for (ImageView[] aPiece : game.getPiece()) {
            for (ImageView anAPiece : aPiece) {
                anAPiece.setVisibility(View.INVISIBLE);
            }
        }
    }

    void showFigures() {
        for (ImageView[] aPiece : game.getPiece()) {
            for (ImageView anAPiece : aPiece) {
                anAPiece.setVisibility(View.VISIBLE);
            }
        }
    }
    void playerInactive() {
        if (game.getGameExtraParams().isColor() != game.getRunningGame().isThisPlayerPlaysWhite()) {
            gameActionsAvailable(game.getAllField(), false);
        } else {
            gameActionsAvailable(game.getAllField(), true);
        }
    }

    void openLogs() {
        game.getActivity().findViewById(game.getOpenLogs().getItemId()).clearAnimation();
        if (game.getPauseDialog().isShowing()) {
            game.getOpenLogsButton().clearAnimation();
        }
        View view = game.getActivity().getLayoutInflater().inflate(R.layout.dialog_chat_logs, null);
        Button closeButton = (Button) view.findViewById(R.id.close_logs_button);
        ImageButton sendMessage = (ImageButton) view.findViewById(R.id.send_message_button);
        ListView listView = (ListView) view.findViewById(R.id.list_view_logs);
        game.setChatLogsAdapter(new ArrayAdapter<>(game.getActivity()
                , android.R.layout.simple_list_item_1, android.R.id.text1, game.getRunningGame().getChatLogs()));
        listView.setAdapter(game.getChatLogsAdapter());
        final Dialog dialog = new Dialog(game.getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        dialog.show();
    }

    void sendMessage() {
        final LinearLayout linearLayout = (LinearLayout) game.getActivity().getLayoutInflater().inflate(R.layout.dialog_send_message, null);
        final EditText edt = (EditText) linearLayout.findViewById(R.id.message_field);
        AlertDialog changeNameDialog = ((GameActivity) game.getActivity()).createAlertDialog(game.getActivity(), linearLayout,
                true, game.getString(R.string.enter_message), ""
                , game.getString(R.string.button_text_send), game.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringBuilder sb = new StringBuilder(edt.getText().toString());
                        if (!sb.toString().isEmpty()) {
                            if (game.getRunningGame().isThisPlayerPlaysWhite()) {
                                if(game.getRunningGame().getPlayer1().getName()!= null) {
                                    sb.insert(0, game.getRunningGame().getPlayer1().getName() + "[white]: ");
                                }else{
                                    sb.insert(0, "[white]: ");
                                }
                            } else {
                                if(game.getRunningGame().getPlayer2().getName()!= null) {
                                    sb.insert(0, game.getRunningGame().getPlayer2().getName() + "[black]: ");
                                }else{
                                    sb.insert(0, "[black]: ");
                                }
                            }
                            game.getRunningGame().getChatLogs().add(sb.toString());
                            if (game.getGameType() == ONLINE_GAME) {
                                game.getGameDatabase().getGameRef().child(RunningGame.CHAT_LOGS).setValue(game.getRunningGame().getChatLogs());
                            } else {
                                game.getPeer2Peer().sendAction(CHAT_LOGS, game.getRunningGame().getChatLogs());
                            }
                            Toast.makeText(game.getActivity(), R.string.message_has_been_sent, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        changeNameDialog.show();
    }

    void changeKingIsCheckedTextView() {
        if (game.getGameExtraParams().isKingIsChecked()) {
            if (game.getGameExtraParams().isColor()) {
                game.getKingIsCheckedText1().setVisibility(View.VISIBLE);
            } else {
                game.getKingIsCheckedText2().setVisibility(View.VISIBLE);
            }
        }
    }

    void gameActionsAvailable(ViewGroup layout, final boolean b) {
        layout.setEnabled(b);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                gameActionsAvailable((ViewGroup) child, b);
            } else {
                child.setEnabled(b);
            }
        }
    }

    void pulsAnimationStart(View v) {
        Animation pulsation = AnimationUtils.loadAnimation(game.getActivity(), R.anim.pulse);
        pulsation.setRepeatCount(Animation.INFINITE);
        pulsation.setRepeatMode(Animation.RESTART);
        v.startAnimation(pulsation);
    }

    void flipBoard() {
        View v = game.getActivity().findViewById(game.getFlipBoard().getItemId());
        Animation rotation = AnimationUtils.loadAnimation(game.getActivity(), R.anim.rotation);
        v.startAnimation(rotation);
        for (ImageView[] aPiece : game.getPiece()) {
            for (ImageView anAPiece : aPiece) {
                anAPiece.animate().setDuration(0).rotationBy(180);
                if (anAPiece.getTag() != null) {
                    int tag = Integer.parseInt(anAPiece.getTag().toString());
                    if (tag == R.drawable.knight_black) {
                        anAPiece.setImageResource(R.drawable.knight_black_left);
                        anAPiece.setTag(R.drawable.knight_black_left);
                    } else if (tag == R.drawable.knight_black_left) {
                        anAPiece.setImageResource(R.drawable.knight_black);
                        anAPiece.setTag(R.drawable.knight_black);
                    } else if (tag == R.drawable.knight_white_left) {
                        anAPiece.setImageResource(R.drawable.knight_white);
                        anAPiece.setTag(R.drawable.knight_white);
                    } else if (tag == R.drawable.knight_white) {
                        anAPiece.setImageResource(R.drawable.knight_white_left);
                        anAPiece.setTag(R.drawable.knight_white_left);
                    }
                }
            }
        }
        if (game.getRunningGame() != null) {
            if (game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves() || game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()) {
                for (int i = 0; i < game.getGameExtraParams().getMovedFiguresImageList().size(); i++) {
                    if (game.getGameExtraParams().getMovedFiguresImageList().get(i) == R.drawable.knight_black_left) {
                        game.getGameExtraParams().getMovedFiguresImageList().set(i, R.drawable.knight_black);
                    } else if (game.getGameExtraParams().getMovedFiguresImageList().get(i) == R.drawable.knight_black) {
                        game.getGameExtraParams().getMovedFiguresImageList().set(i, R.drawable.knight_black_left);
                    } else if (game.getGameExtraParams().getMovedFiguresImageList().get(i) == R.drawable.knight_white) {
                        game.getGameExtraParams().getMovedFiguresImageList().set(i, R.drawable.knight_white_left);
                    } else if (game.getGameExtraParams().getMovedFiguresImageList().get(i) == R.drawable.knight_white_left) {
                        game.getGameExtraParams().getMovedFiguresImageList().set(i, R.drawable.knight_white);
                    }
                }

                for (int i = 0; i < game.getGameExtraParams().getRemovedFiguresImageList().size(); i++) {
                    if (game.getGameExtraParams().getRemovedFiguresImageList().get(i) == R.drawable.knight_black_left) {
                        game.getGameExtraParams().getRemovedFiguresImageList().set(i, R.drawable.knight_black);
                    } else if (game.getGameExtraParams().getRemovedFiguresImageList().get(i) == R.drawable.knight_black) {
                        game.getGameExtraParams().getRemovedFiguresImageList().set(i, R.drawable.knight_black_left);
                    } else if (game.getGameExtraParams().getRemovedFiguresImageList().get(i) == R.drawable.knight_white) {
                        game.getGameExtraParams().getRemovedFiguresImageList().set(i, R.drawable.knight_white_left);
                    } else if (game.getGameExtraParams().getRemovedFiguresImageList().get(i) == R.drawable.knight_white_left) {
                        game.getGameExtraParams().getRemovedFiguresImageList().set(i, R.drawable.knight_white);
                    }
                }
            }
        }
        game.getRelativeLayout().animate().setDuration(0).rotationXBy(180).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                game.getFlipBoard().setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                game.getFlipBoard().setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                game.getFlipBoard().setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
        if (isFlipped) {
            game.getPlayer2Layout().animate().rotationXBy(180).start();
            game.getPlayer1Layout().animate().rotationXBy(180).start();
            isFlipped = false;
        } else {
            game.getPlayer1Layout().animate().rotationX(180).start();
            game.getPlayer2Layout().animate().rotationX(180).start();
            isFlipped = true;
        }

    }

    void recordListsForCancelableMoves() {
        game.getGameExtraParams().getCountNoProgressMovesList().add(game.getGameExtraParams().getCountNoProgressMoves());
        if (game.getKingIsCheckedText1().getVisibility() == View.VISIBLE ^ game.getKingIsCheckedText2().getVisibility() == View.VISIBLE) {
            game.getGameExtraParams().getKingIsCheckedList().add(true);
        } else {
            game.getGameExtraParams().getKingIsCheckedList().add(false);
        }

    }

    void pause() {
        if (game.getGameType() == ONLINE_GAME) {
            game.getGameDatabase().getGameRef().child(RunningGame.GAME_STATUS).setValue(ON_PAUSED);
            if (game.getRunningGame().isThisPlayerPlaysWhite()) {
                game.getRunningGame().getPlayer1().getPlayerGameParams().setPauseCount(game.getRunningGame().getPlayer1().getPlayerGameParams().getPauseCount() + 1);
            } else {
                game.getRunningGame().getPlayer2().getPlayerGameParams().setPauseCount(game.getRunningGame().getPlayer2().getPlayerGameParams().getPauseCount() + 1);
            }
        } else if (game.getGameType() == ONE_DEVICE_GAME) {
            stopTimers(game.getCountDownTimer1(), game.getCountDownTimer2());
            game.getGameTime().setTimeFromTimer();
            hideFigures();
            game.getPauseDialog().show();
        } else {
            game.getPeer2Peer().sendAction(GAME_STATUS, ON_PAUSED);
            game.getPeer2Peer().getReceivedFromOpponent().onGameStatusUpdated(ON_PAUSED);
        }
    }

    void draw() {
        game.getRunningGame().setStatus(RunningGame.DRAW_BY_AGREEMENT);
        if (game.getGameType() == ONLINE_GAME) {
            Intent intent = new Intent();
            intent.putExtra(DRAW, DRAW);
            game.getOfferDraw().setIntent(intent);
            game.getGameDatabase().getGameRef().child(RunningGame.GAME_STATUS).setValue(game.getRunningGame().getStatus());
        } else {
            game.getPeer2Peer().sendAction(GAME_STATUS, game.getRunningGame().getStatus());
            Toast.makeText(game.getActivity(), R.string.draw_offer_has_been_sent, Toast.LENGTH_SHORT).show();
        }
        drawOfferWasSent = true;
    }

    public void resign() {
        Intent intent = new Intent();
        intent.putExtra(RESIGN, game.getRunningGame().isThisPlayerPlaysWhite());
        game.getResign().setIntent(intent);
        if (game.getRunningGame().isThisPlayerPlaysWhite()) {
            game.getRunningGame().setStatus(RunningGame.PLAYER_2_WIN);
        } else {
            game.getRunningGame().setStatus(RunningGame.PLAYER_1_WIN);
        }
        game.getGameExtraParams().setReason(REASON_RESIGN);
        if (game.getGameType() == ONLINE_GAME) {
            game.getGameDatabase().removeDatabaseListenersForCurrentGame();
        }else if(game.getGameType() == LAN_GAME){
            game.getPeer2Peer().sendAction(GAME_STATUS, game.getRunningGame().getStatus());
        }
        game.getGameEnd().recordResult();
    }

    boolean isFlipped() {
        return isFlipped;
    }

    void setDrawOfferWasSent(boolean drawOfferWasSent) {
        this.drawOfferWasSent = drawOfferWasSent;
    }
}
