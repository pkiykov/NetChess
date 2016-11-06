package com.pkiykov.netchess.logic;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.fragments.Game;
import com.pkiykov.netchess.fragments.Profile;
import com.pkiykov.netchess.fragments.UnrankedGameSettings;
import com.pkiykov.netchess.pojo.GameExtraParams;
import com.pkiykov.netchess.pojo.Player;
import com.pkiykov.netchess.pojo.RunningGame;
import com.pkiykov.netchess.receivers.OpponentDisconnectReceiver;
import com.pkiykov.netchess.services.OnlineCheckPlayerService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.pkiykov.netchess.fragments.Game.GAME_TYPE;
import static com.pkiykov.netchess.fragments.Game.LAN_GAME;
import static com.pkiykov.netchess.fragments.Game.ONE_DEVICE_GAME;
import static com.pkiykov.netchess.fragments.Game.ONLINE_GAME;
import static com.pkiykov.netchess.logic.Peer2Peer.USER_INFO;
import static com.pkiykov.netchess.pojo.Player.AVATARS;
import static com.pkiykov.netchess.pojo.Player.PLAYER_ID;
import static com.pkiykov.netchess.pojo.RunningGame.GAME_STATUS;
import static com.pkiykov.netchess.pojo.RunningGame.RUNNING;

public class GameStart {

    private final int PAUSE_BUTTON_ID = 12325;
    private final int OFFER_DRAW_BUTTON_ID = 12324;
    private final int RESIGN_BUTTON_ID = 12323;
    private final int CANCEL_LAST_MOVE_BUTTON_ID = 12322;
    private final int FLIP_BOARD_BUTTON_ID = 12321;
    private final int SEND_MESSAGE_BUTTON_ID = 12320;
    private final int OPEN_LOGS_BUTTON_ID = 12319;
    private Game game;
    private AlertDialog acceptPlayerDialog;
    private boolean firstPlayer;
    private ProgressDialog pleaseWaitDialog;

    public GameStart(Game game) {
        firstPlayer = false;
        this.game = game;
    }

    void createPlayerJoinedDialog() {
        LinearLayout layout = (LinearLayout) game.getActivity().getLayoutInflater().inflate(R.layout.dialog_accept_player_join, null);
        acceptPlayerDialog = new AlertDialog.Builder(game.getActivity()).create();
        acceptPlayerDialog.setCancelable(true);
        acceptPlayerDialog.setCanceledOnTouchOutside(false);
        TextView age = (TextView) layout.findViewById(R.id.player_age);
        TextView wins = (TextView) layout.findViewById(R.id.player_wins);
        TextView losses = (TextView) layout.findViewById(R.id.player_losses);
        TextView draws = (TextView) layout.findViewById(R.id.player_draws);
        TextView playerName = (TextView) layout.findViewById(R.id.player_name);
        TextView playerRating = (TextView) layout.findViewById(R.id.player_rating);
        String a;
        String w;
        String l;
        String d;
        String r;
        String n;
        Button acceptBtn = (Button) layout.findViewById(R.id.accept_btn);
        Button declineBtn = (Button) layout.findViewById(R.id.decline_btn);
        game.setPlayerJoinedAva((ImageView) layout.findViewById(R.id.player_photo));
        if (game.getRunningGame().isThisPlayerPlaysWhite()) {
            a = game.getString(R.string.age) + game.getRunningGame().getPlayer2().getAge();
            w = game.getString(R.string.wins) + game.getRunningGame().getPlayer2().getWins();
            l = game.getString(R.string.losses) + game.getRunningGame().getPlayer2().getLosses();
            d = game.getString(R.string.draws) + game.getRunningGame().getPlayer2().getDraws();
            r = game.getString(R.string.rating) + game.getRunningGame().getPlayer2().getRating();
            n = game.getRunningGame().getPlayer2().getName();
        } else {
            a = game.getString(R.string.age) + game.getRunningGame().getPlayer1().getAge();
            w = game.getString(R.string.wins) + game.getRunningGame().getPlayer1().getWins();
            l = game.getString(R.string.losses) + game.getRunningGame().getPlayer1().getLosses();
            d = game.getString(R.string.draws) + game.getRunningGame().getPlayer1().getDraws();
            r = game.getString(R.string.rating) + game.getRunningGame().getPlayer1().getRating();
            n = game.getRunningGame().getPlayer1().getName();
        }
        age.setText(a);
        wins.setText(w);
        losses.setText(l);
        draws.setText(d);
        playerName.setText(n);
        playerRating.setText(r);

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String rating;
                if (game.getRunningGame().isThisPlayerPlaysWhite()) {
                    if (game.getJoinedAvaUri() != null) {
                        Picasso.with(game.getActivity()).load(game.getJoinedAvaUri()).error(R.drawable.empty_avatar).into(game.getPlayer2_ava());
                    }
                    game.getPlayer2_name().setText(game.getRunningGame().getPlayer2().getName());
                    rating = game.getString(R.string.rating) + String.valueOf(game.getRunningGame().getPlayer2().getRating());
                    game.getPlayer2_rating().setText(rating);
                    game.getPlayer2TimeControlDescription().setText(game.getPlayer1TimeControlDescription().getText().toString());
                    game.getTimer2().setText(game.getTimer1().getText().toString());
                } else {
                    if (game.getJoinedAvaUri() != null) {
                        Picasso.with(game.getActivity()).load(game.getJoinedAvaUri()).error(R.drawable.empty_avatar).into(game.getPlayer1_ava());
                    }
                    game.getPlayer1_name().setText(game.getRunningGame().getPlayer1().getName());
                    rating = game.getString(R.string.rating) + String.valueOf(game.getRunningGame().getPlayer1().getRating());
                    game.getPlayer1_rating().setText(rating);
                    game.getPlayer1TimeControlDescription().setText(game.getPlayer2TimeControlDescription().getText().toString());
                    game.getTimer1().setText(game.getTimer2().getText().toString());
                }
                game.getGameGo().playerInactive();
                game.getRunningGame().setStatus(RUNNING);
                game.getGameDatabase().getGameRef().setValue(game.getRunningGame());
                makePlayerLayoutClickable();
                game.getGameGo().playerInactive();
                acceptPlayerDialog.dismiss();
            }
        });
        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                game.getGameEnd().restartOnlineGame();
                acceptPlayerDialog.dismiss();
            }
        });
        acceptPlayerDialog.setView(layout);
        game.getGameDatabase().loadOpponentPhoto();
        game.getGameDatabase().addDatabaseListenersForCurrentGame();

    }

    public void createPauseDialog() {
        game.setPauseDialog(new Dialog(game.getActivity()));
        View view = game.getActivity().getLayoutInflater().inflate(R.layout.dialog_pause,null);
        game.setOpenLogsButton((ImageButton) view.findViewById(R.id.open_logs_button));
        ImageButton sendMessage = (ImageButton) view.findViewById(R.id.send_message_button);
        if (game.getGameType() != ONE_DEVICE_GAME) {
            sendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    game.getGameGo().sendMessage();
                }
            });
            game.getOpenLogsButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    game.getGameGo().openLogs();
                }
            });
        } else {
            game.getOpenLogsButton().setVisibility(View.GONE);
            sendMessage.setVisibility(View.GONE);
        }
        Button resumeGame = (Button) view.findViewById(R.id.resume_game_button);
        resumeGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (game.getGameType() == ONLINE_GAME) {
                    game.getGameDatabase().getGameRef().child(GAME_STATUS).setValue(RUNNING);
                } else if (game.getGameType() == ONE_DEVICE_GAME) {
                    game.getGameGo().resumeGame();
                } else {
                    game.getPeer2Peer().sendAction(GAME_STATUS, RUNNING);
                    game.getPeer2Peer().getReceivedFromOpponent().onGameStatusUpdated(RUNNING);
                }
            }
        });
        game.getPauseDialog().setCancelable(false);
        game.getPauseDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        game.getPauseDialog().setContentView(view);
    }


    void startService() {
        Intent intent = new Intent(game.getActivity(), OnlineCheckPlayerService.class);
        intent.putExtra(OnlineCheckPlayerService.GAME_ID, game.getRunningGame().getId());
        intent.putExtra(OnlineCheckPlayerService.COLOR, game.getRunningGame().isThisPlayerPlaysWhite());
        game.getActivity().startService(intent);
    }

    private void waitingPlayersDialog() {
        game.setWaitingPlayers(new ProgressDialog(game.getActivity()));
        game.getWaitingPlayers().setCancelable(true);
        game.getWaitingPlayers().setCanceledOnTouchOutside(false);
        game.getWaitingPlayers().setMessage(game.getString(R.string.waiting_for_player));
        game.getWaitingPlayers().setProgressStyle(ProgressDialog.STYLE_SPINNER);
        game.getWaitingPlayers().setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                pleaseWaitDialog = ProgressDialog.show(game.getActivity(), "",
                        game.getString(R.string.please_wait), false);

                if (game.getGameType() == ONLINE_GAME) {
                    game.getGameDatabase().removeDatabaseListenersForCurrentGame();
                    if (game.getGameDatabase().getCurrentGameDB() != null) {
                        game.getGameDatabase().getCurrentGameDB().getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                game.getGameEnd().outOnlineGame();
                            }
                        });
                    } else {
                        game.getGameEnd().outOnlineGame();
                    }
                } else {
                    game.getPeer2Peer().disconnect(false);
                    game.getPeer2Peer().setGame(null);
                    game.getFragmentManager().popBackStack();
                    Fragment fragment = new UnrankedGameSettings();
                    Bundle bundle = new Bundle();
                    bundle.putInt(GAME_TYPE, LAN_GAME);
                    fragment.setArguments(bundle);
                    ((GameActivity) game.getActivity()).fragmentTransaction(fragment);
                    pleaseWaitDialog.dismiss();
                }

            }
        });
        game.getWaitingPlayers().show();
    }

    public void setUpMenuItems() {
        Toolbar toolbar = ((GameActivity) game.getActivity()).getToolbar();
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case FLIP_BOARD_BUTTON_ID:
                        game.getGameGo().flipBoard();
                        break;
                    case CANCEL_LAST_MOVE_BUTTON_ID:
                        if ((game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves()
                                ^ game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()) || game.getGameExtraParams().getFigureMoved().size() < 2) {
                            game.getGameMove().cancelLastMove();
                        } else {
                            game.getGameMove().cancelLastMove();
                            game.getGameMove().cancelLastMove();
                        }
                        break;
                    case RESIGN_BUTTON_ID:
                        game.getGameGo().resign();
                        break;
                    case OFFER_DRAW_BUTTON_ID:
                        game.getGameGo().draw();
                        break;
                    case PAUSE_BUTTON_ID:
                        game.getGameGo().pause();
                        break;
                    case SEND_MESSAGE_BUTTON_ID:
                        game.getGameGo().sendMessage();
                        break;
                    case OPEN_LOGS_BUTTON_ID:
                        game.getGameGo().openLogs();
                        break;
                }
                return false;
            }
        });
    }


    public void initField() {
        game.getGameExtraParams().createFigures(game.getGameExtraParams(), game.getRunningGame().getMoveList());
        game.setImages(new ImageView[8][8]);
        game.setPiece(new ImageView[8][8]);
        game.setSelect(new ImageView[8][8]);
        game.setHighlight(new ImageView[8][8]);
        int countSquares = 0;
        int c = 0;
        for (int i = 0; i < game.getImages().length; i++) {
            for (int j = 0; j < game.getImages().length; j++) {
                View view = game.getActivity().getLayoutInflater().inflate(R.layout.item, null);
                FrameLayout square = (FrameLayout) view.findViewById(R.id.square);
                game.getImages()[i][j] = ((ImageView) view.findViewById(R.id.background));
                game.getPiece()[i][j] = (ImageView) view.findViewById(R.id.piece);
                game.getSelect()[i][j] = (ImageView) view.findViewById(R.id.select);
                game.getHighlight()[i][j] = (ImageView) view.findViewById(R.id.highlight);

                if (j == 0) {
                    if (i == 0 || i == 2 || i == 4 || i == 6) {
                        game.getImages()[i][j].setImageResource(R.drawable.w1);
                    } else {
                        game.getImages()[i][j].setImageResource(R.drawable.b1);
                    }
                } else if (j == 1) {
                    if (i == 0 || i == 2 || i == 4 || i == 6) {
                        game.getImages()[i][j].setImageResource(R.drawable.b2);
                    } else {
                        game.getImages()[i][j].setImageResource(R.drawable.w2);
                    }
                } else if (j == 2) {
                    if (i == 0 || i == 2 || i == 4 || i == 6) {
                        game.getImages()[i][j].setImageResource(R.drawable.w3);
                    } else {
                        game.getImages()[i][j].setImageResource(R.drawable.b3);
                    }
                } else if (j == 3) {
                    if (i == 0 || i == 2 || i == 4 || i == 6) {
                        game.getImages()[i][j].setImageResource(R.drawable.b4);
                    } else {
                        game.getImages()[i][j].setImageResource(R.drawable.w4);
                    }
                } else if (j == 4) {
                    if (i == 0 || i == 2 || i == 4 || i == 6) {
                        game.getImages()[i][j].setImageResource(R.drawable.w5);
                    } else {
                        game.getImages()[i][j].setImageResource(R.drawable.b5);
                    }
                } else if (j == 5) {
                    if (i == 0 || i == 2 || i == 4 || i == 6) {
                        game.getImages()[i][j].setImageResource(R.drawable.b6);
                    } else {
                        game.getImages()[i][j].setImageResource(R.drawable.w6);
                    }
                } else if (j == 6) {
                    if (i == 0 || i == 2 || i == 4 || i == 6) {
                        game.getImages()[i][j].setImageResource(R.drawable.w7);
                    } else {
                        game.getImages()[i][j].setImageResource(R.drawable.b7);
                    }
                } else if (j == 7) {
                    if (i == 0 || i == 2 || i == 4 || i == 6) {
                        game.getImages()[i][j].setImageResource(R.drawable.b8);
                    } else {
                        game.getImages()[i][j].setImageResource(R.drawable.w8);
                    }
                }
                if (i == 1) {
                    game.getPiece()[i][j].setImageResource(R.drawable.pawn_black);
                    game.getPiece()[i][j].setTag(R.drawable.pawn_black);
                } else if (i == 6) {
                    game.getPiece()[i][j].setImageResource(R.drawable.pawn_white);
                    game.getPiece()[i][j].setTag(R.drawable.pawn_white);
                } else if (i == 0) {
                    if (j == 0 || j == 7) {
                        game.getPiece()[i][j].setImageResource(R.drawable.rook_black);
                        game.getPiece()[i][j].setTag(R.drawable.rook_black);
                    } else if (j == 6) {
                        game.getPiece()[i][j].setImageResource(R.drawable.knight_black);
                        game.getPiece()[i][j].setTag(R.drawable.knight_black);
                    } else if (j == 1) {
                        game.getPiece()[i][j].setImageResource(R.drawable.knight_black_left);
                        game.getPiece()[i][j].setTag(R.drawable.knight_black_left);
                    } else if (j == 2 || j == 5) {
                        game.getPiece()[i][j].setImageResource(R.drawable.bishop_black);
                        game.getPiece()[i][j].setTag(R.drawable.bishop_black);

                    } else if (j == 3) {
                        game.getPiece()[i][j].setImageResource(R.drawable.queen_black);
                        game.getPiece()[i][j].setTag(R.drawable.queen_black);
                    } else {
                        game.getPiece()[i][j].setImageResource(R.drawable.king_black);
                        game.getPiece()[i][j].setTag(R.drawable.king_black);
                    }
                } else if (i == 7) {
                    if (j == 0 || j == 7) {
                        game.getPiece()[i][j].setImageResource(R.drawable.rook_white);
                        game.getPiece()[i][j].setTag(R.drawable.rook_white);
                    } else if (j == 6) {
                        game.getPiece()[i][j].setImageResource(R.drawable.knight_white);
                        game.getPiece()[i][j].setTag(R.drawable.knight_white);
                    } else if (j == 1) {
                        game.getPiece()[i][j].setImageResource(R.drawable.knight_white_left);
                        game.getPiece()[i][j].setTag(R.drawable.knight_white_left);
                    } else if (j == 2 || j == 5) {
                        game.getPiece()[i][j].setImageResource(R.drawable.bishop_white);
                        game.getPiece()[i][j].setTag(R.drawable.bishop_white);
                    } else if (j == 3) {
                        game.getPiece()[i][j].setImageResource(R.drawable.queen_white);
                        game.getPiece()[i][j].setTag(R.drawable.queen_white);
                    } else {
                        game.getPiece()[i][j].setImageResource(R.drawable.king_white);
                        game.getPiece()[i][j].setTag(R.drawable.king_white);
                    }
                }
                view.setTag(c);
                if (game.getGameType() != ONLINE_GAME) {
                    setHandicap(c, i, j);
                }
                c++;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                if (countSquares < 8) {
                    game.getLinearLayout1().addView(square, params);
                } else if (countSquares < 16) {
                    game.getLinearLayout2().addView(square, params);
                } else if (countSquares < 24) {
                    game.getLinearLayout3().addView(square, params);
                } else if (countSquares < 32) {
                    game.getLinearLayout4().addView(square, params);
                } else if (countSquares < 40) {
                    game.getLinearLayout5().addView(square, params);
                } else if (countSquares < 48) {
                    game.getLinearLayout6().addView(square, params);
                } else if (countSquares < 56) {
                    game.getLinearLayout7().addView(square, params);
                } else if (countSquares < 64) {
                    game.getLinearLayout8().addView(square, params);
                }
                countSquares++;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (game.getRunningGame() != null) {
                            game.getGameMove().tryToMove(v);
                        }
                    }
                });
            }
        }
    }

    private void setHandicap(int c, int i, int j) {
        if (game.getRunningGame().getPlayer1().getPlayerGameParams().getHandicap() == c
                || game.getRunningGame().getPlayer2().getPlayerGameParams().getHandicap() == c) {
            game.getPiece()[i][j].setImageDrawable(null);
            game.getPiece()[i][j].setTag(null);
            for (int n = 0; n < game.getGameExtraParams().getFigures().size(); n++) {
                if (game.getGameExtraParams().getFigures().get(n).getA() - 1 == j && 8 - game.getGameExtraParams().getFigures().get(n).getB() == i) {
                    game.getGameExtraParams().getFigures().remove(n);
                }
            }
        }
    }

    private void setPlayersToView(boolean color) {
        String rating;
        if (color) {
            game.getRunningGame().setThisPlayerPlaysWhite(true);
            game.getGameDatabase().loadThisPlayerPhoto(game.getPlayer1_ava());
            game.getPlayer1_name().setText(game.getRunningGame().getPlayer1().getName());
            rating = game.getString(R.string.rating) + game.getRunningGame().getPlayer1().getRating();
            game.getPlayer1_rating().setText(rating);
            if (game.getRunningGame().getPlayer2() != null) {
                game.getGameDatabase().getStorageRef().child(game.getRunningGame().getPlayer2().getId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(game.getActivity()).load(uri).into(game.getPlayer2_ava());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        game.getPlayer2_ava().setImageDrawable(ContextCompat.getDrawable(game.getActivity(), R.drawable.empty_avatar));
                    }
                });
                game.getPlayer2_name().setText(game.getRunningGame().getPlayer2().getName());
                rating = game.getString(R.string.rating) + game.getRunningGame().getPlayer2().getRating();
                game.getPlayer2_rating().setText(rating);
            }
        } else {
            game.getRunningGame().setThisPlayerPlaysWhite(false);
            game.getGameDatabase().loadThisPlayerPhoto(game.getPlayer2_ava());
            game.getPlayer2_name().setText(game.getRunningGame().getPlayer2().getName());
            rating = game.getString(R.string.rating) + game.getRunningGame().getPlayer2().getRating();
            game.getPlayer2_rating().setText(rating);
            if (game.getRunningGame().getPlayer1() != null) {
                game.getGameDatabase().getStorageRef().child(game.getRunningGame().getPlayer1().getId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(game.getActivity()).load(uri).into(game.getPlayer1_ava());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        game.getPlayer2_ava().setImageDrawable(ContextCompat.getDrawable(game.getActivity(), R.drawable.empty_avatar));
                    }
                });

                game.getPlayer1_name().setText(game.getRunningGame().getPlayer1().getName());
                rating = game.getString(R.string.rating) + game.getRunningGame().getPlayer1().getRating();
                game.getPlayer1_rating().setText(rating);
            }
        }
    }

    public void createMenu(Menu menu) {
        game.setFlipBoard(menu.add(1, FLIP_BOARD_BUTTON_ID, 3, game.getActivity().getString(R.string.flip_board))
                .setIcon(R.drawable.flip_board));
        game.getFlipBoard().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        if (game.getRunningGame() != null) {
            game.setPause(menu.add(0, PAUSE_BUTTON_ID, 0, game.getActivity().getString(R.string.pause)));
            game.getPause().setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            game.setResign(menu.add(0, RESIGN_BUTTON_ID, 0, game.getActivity().getString(R.string.resign)));
            game.getResign().setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            if (game.getGameType() != ONE_DEVICE_GAME) {
                game.setOfferDraw(menu.add(0, OFFER_DRAW_BUTTON_ID, 0, game.getActivity().getString(R.string.offer_draw)));
                game.getOfferDraw().setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                game.setSendMessage(menu.add(1, SEND_MESSAGE_BUTTON_ID, 1, game.getActivity().getString(R.string.send_message)).setIcon(R.drawable.send_message));
                game.getSendMessage().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                game.setOpenLogs(menu.add(1, OPEN_LOGS_BUTTON_ID, 2, game.getActivity().getString(R.string.open_logs)).setIcon(R.drawable.open_logs));
                game.getOpenLogs().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            } else {
                game.setCancelMove(menu.add(0, CANCEL_LAST_MOVE_BUTTON_ID, 0, game.getActivity().getString(R.string.undo_move)));
                game.getCancelMove().setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
        }
    }

    AlertDialog getAcceptPlayerDialog() {
        return acceptPlayerDialog;
    }

    public void setUpGameSettings() {
        switch (game.getGameType()) {
            case ONLINE_GAME:
                game.getGameGo().userProfileClick();
                game.setGameDatabase(new GameDatabase(game));
                game.getRunningGame().setChatLogs(new ArrayList<String>());
                game.setGameExtraParams(new GameExtraParams(false));
                if (game.getRunningGame().getPlayer1() != null) {
                    if (game.getGameDatabase().getmAuth().getCurrentUser().getUid().equals(game.getRunningGame().getPlayer1().getId())) {
                        game.getGameStart().setPlayersToView(true);
                    } else {
                        game.getGameStart().setPlayersToView(false);
                    }
                } else {
                    game.getGameStart().setPlayersToView(false);
                }
                if (game.getRunningGame().getPlayer1() == null ^ game.getRunningGame().getPlayer2() == null) {
                    firstPlayer = true;
                    game.getGameStart().waitingPlayersDialog();
                    game.getGameDatabase().createGameInDatabase();
                } else {
                    firstPlayer = false;
                    game.getGameDatabase().setGameRef(FirebaseDatabase.getInstance().getReference()
                            .child(RunningGame.RUNNING_GAME).child(game.getRunningGame().getId()));
                    game.getGameDatabase().addGameListener();
                    game.getGameDatabase().addDatabaseListenersForCurrentGame();
                    makePlayerLayoutClickable();
                }
                game.setReceiver(new OpponentDisconnectReceiver(game));
                game.setmConnection(new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                        OnlineCheckPlayerService.LocalBinder binder = (OnlineCheckPlayerService.LocalBinder) iBinder;
                        game.setmService(binder.getService());
                        game.getmService().registerOpponentDisconnectReceiver(game.getReceiver());
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName) {

                    }
                });
                game.getActivity().bindService(new Intent(game.getActivity(), OnlineCheckPlayerService.class)
                        , game.getmConnection(), Context.BIND_AUTO_CREATE);

                break;
            case LAN_GAME:
                game.getRunningGame().setChatLogs(new ArrayList<String>());
                game.setGameExtraParams(new GameExtraParams(false));
                setThisPlayerToView();
                if (game.getRunningGame().getStatus() != RUNNING) {
                    ((GameActivity) game.getActivity()).getPeer2Peer().initializeWifiP2P();
                    ((GameActivity) game.getActivity()).getPeer2Peer().startRegistration(1234);
                    game.getGameStart().waitingPlayersDialog();
                } else {
                    game.getRunningGame().setThisPlayerPlaysWhite(!game.getRunningGame().isThisPlayerPlaysWhite());
                    game.getPeer2Peer().sendAction(USER_INFO, packUserProfileInfo());
                }
                break;
            case ONE_DEVICE_GAME:
                game.getPlayer1_name().setText(RunningGame.PLAYER_1);
                game.getPlayer2_name().setText(RunningGame.PLAYER_2);
                game.getPlayer1_ava().setImageResource(R.drawable.empty_avatar);
                game.getPlayer2_ava().setImageResource(R.drawable.empty_avatar);
                game.getRunningGame().setStatus(RUNNING);
                game.setGameExtraParams(new GameExtraParams(game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves()
                        || game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()));
                if (game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves() || game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()) {
                    game.getGameGo().recordListsForCancelableMoves();
                }
                break;
        }
    }

    void makePlayerLayoutClickable() {
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (game.getRunningGame().getPlayer1().getId() != null) {
                game.setPlayer1Id(game.getRunningGame().getPlayer1().getId());
                game.getPlayer1Layout().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString(PLAYER_ID, game.getPlayer1Id());
                        Fragment profile = new Profile();
                        profile.setArguments(bundle);
                        ((GameActivity) game.getActivity()).fragmentTransaction(profile);
                    }
                });
                game.getPlayer1Layout().setClickable(true);
            }

            if (game.getRunningGame().getPlayer2().getId() != null) {
                game.setPlayer2Id(game.getRunningGame().getPlayer2().getId());
                game.getPlayer2Layout().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString(PLAYER_ID, game.getPlayer2Id());
                        Fragment profile = new Profile();
                        profile.setArguments(bundle);
                        ((GameActivity) game.getActivity()).fragmentTransaction(profile);
                    }
                });
                game.getPlayer2Layout().setClickable(true);
            }
        }
    }

    private void setThisPlayerToView() {
        final FirebaseAuth mAuth = ((GameActivity) game.getActivity()).getmAuth();
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            FirebaseDatabase.getInstance().getReference().child(Player.PLAYERS).child(mAuth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String rating;
                            Player player = dataSnapshot.getValue(Player.class);
                            if (game.getRunningGame().isThisPlayerPlaysWhite()) {
                                game.getRunningGame().getPlayer1().setId(player.getId());
                                loadPlayerPhoto(game.getPlayer1_ava(), player.getId());
                                game.getPlayer1_name().setText(player.getName());
                                rating = game.getString(R.string.rating) + player.getRating();
                                game.getPlayer1_rating().setText(rating);
                            } else {
                                game.getRunningGame().getPlayer2().setId(player.getId());
                                loadPlayerPhoto(game.getPlayer2_ava(), player.getId());
                                game.getPlayer2_name().setText(player.getName());
                                rating = game.getString(R.string.rating) + player.getRating();
                                game.getPlayer2_rating().setText(rating);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    Map<String, String> packUserProfileInfo() {
        Map<String, String> map = new HashMap<>();
        String name;
        String rating;
        if (game.getRunningGame().isThisPlayerPlaysWhite()) {
            name = game.getPlayer1_name().getText().toString();
            rating = game.getPlayer1_rating().getText().toString();
        } else {
            name = game.getPlayer2_name().getText().toString();
            rating = game.getPlayer2_rating().getText().toString();
        }
        map.put(Player.NAME, name);
        map.put(Player.PLAYER_RATING, rating);
        String playerId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            //noinspection ConstantConditions
            playerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        map.put(PLAYER_ID, playerId);
        return map;
    }

    public void setActivePlayer() {
        if ((game.getGameType() != ONE_DEVICE_GAME)
                && game.getRunningGame().getPlayer1() != null && game.getRunningGame().getPlayer2() != null) {
            if (!game.getRunningGame().isThisPlayerPlaysWhite()) {
                game.getGameGo().gameActionsAvailable(game.getAllField(), false);
            }
            if (game.getPeer2Peer() != null) {
                game.getPeer2Peer().dismissFetchingDialog();
            }
        }else if (game.getGameType() == ONLINE_GAME && (game.getRunningGame().getPlayer1() == null ^ game.getRunningGame().getPlayer2() == null)){
            game.getGameGo().gameActionsAvailable(game.getAllField(), false);
        }
    }
    void loadPlayerPhoto(final ImageView player_ava, final String playerId) {
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(AVATARS).child(playerId);
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(game.getActivity()).load(uri).networkPolicy(NetworkPolicy.OFFLINE).into(player_ava, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.with(game.getActivity()).load(uri).fit().into(player_ava);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                player_ava.setImageResource(R.drawable.empty_avatar);
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                            }
                        });
                    }
                });
            }
        });
    }
    ProgressDialog getPleaseWaitDialog() {
        return pleaseWaitDialog;
    }

    boolean isFirstPlayer() {
        return firstPlayer;
    }


}
