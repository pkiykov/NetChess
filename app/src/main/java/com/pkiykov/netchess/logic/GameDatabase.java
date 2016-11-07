package com.pkiykov.netchess.logic;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.fragments.Game;
import com.pkiykov.netchess.others.FirebaseHelper;
import com.pkiykov.netchess.pojo.Coordinates;
import com.pkiykov.netchess.pojo.FinishedGame;
import com.pkiykov.netchess.pojo.Player;
import com.pkiykov.netchess.pojo.PlayerGameParams;
import com.pkiykov.netchess.pojo.RunningGame;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.pkiykov.netchess.logic.GameEnd.getPlayerMovesCount;
import static com.pkiykov.netchess.pojo.FinishedGame.REASON_DISCONNECT;

public class GameDatabase {
    private boolean opponentIsConnected;
    private Game game;
    private DatabaseReference mDatabase;
    private DatabaseReference gameRef;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private FirebaseHelper opponentTimeRestDB;
    private FirebaseHelper player1PauseCountDB;
    private FirebaseHelper player2PauseCountDB;
    private FirebaseHelper currentGameDB;
    private FirebaseHelper currentGameStatusDB;
    private FirebaseHelper coordinatesDB;
    private FirebaseHelper chatLogsDB;
    private ReceivedFromOpponent receivedFromOpponent;

    GameDatabase(Game game) {
        mAuth = ((GameActivity) game.getActivity()).getmAuth();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference().child(Player.AVATARS);
        this.game = game;
        opponentIsConnected = false;
        receivedFromOpponent = new ReceivedFromOpponent(game);
    }

    void createGameInDatabase() {
        gameRef = mDatabase.child(RunningGame.RUNNING_GAME).push();
        gameRef.setValue(game.getRunningGame()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                game.getRunningGame().setId(gameRef.getKey());
                game.getGameStart().startService();
                gameRef.child(RunningGame.ID).setValue(game.getRunningGame().getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addGameListener();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        failToWriteToDatabase(e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                failToWriteToDatabase(e.getMessage());
            }
        });
    }

    private void failToWriteToDatabase(String message) {
        Toast.makeText(game.getActivity(), message, Toast.LENGTH_SHORT).show();
        game.getGameExtraParams().setReason(REASON_DISCONNECT);
        if (game.getRunningGame().isThisPlayerPlaysWhite()) {
            game.getRunningGame().setStatus(RunningGame.PLAYER_2_WIN);
        } else {
            game.getRunningGame().setStatus(RunningGame.PLAYER_1_WIN);
        }
        game.getGameEnd().recordResult();
    }

    void addGameListener() {
        currentGameDB = new FirebaseHelper(gameRef, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null && game.getRunningGame() != null) {
                    if (dataSnapshot.getValue(RunningGame.class).getPlayer1() != null &&
                            dataSnapshot.getValue(RunningGame.class).getPlayer2() != null) {

                        if ((game.getRunningGame().getPlayer1() == null ^ game.getRunningGame().getPlayer2() == null) && !opponentIsConnected) {
                            opponentIsConnected = true;
                            if (game.getRunningGame().isThisPlayerPlaysWhite()) {
                                game.getRunningGame().setPlayer2(dataSnapshot.getValue(RunningGame.class).getPlayer2());
                            } else {
                                game.getRunningGame().setPlayer1(dataSnapshot.getValue(RunningGame.class).getPlayer1());
                            }
                            game.getGameStart().createPlayerJoinedDialog();
                        } else if (dataSnapshot.getValue(RunningGame.class).getPlayer1().getPlayerGameParams() == null
                                && dataSnapshot.getValue(RunningGame.class).getPlayer2().getPlayerGameParams() == null
                                && game.getRunningGame().getStatus() == RunningGame.DRAW_BY_AGREEMENT) {
                            game.getGameEnd().setGameHasBeenEnded(true);
                            game.getGameEnd().finishService();
                            game.getGameEnd().unbindService();
                            removeDatabaseListenersForCurrentGame();
                            game.getGameEnd().recordResult();
                        }
                    } else if ((dataSnapshot.getValue(RunningGame.class).getPlayer1() == null ^
                            dataSnapshot.getValue(RunningGame.class).getPlayer2() == null) && (game.getRunningGame().getPlayer1() != null &&
                            game.getRunningGame().getPlayer2() != null)) {
                        if (game.getGameStart().getAcceptPlayerDialog() != null) {
                            if (game.getGameStart().getAcceptPlayerDialog().isShowing()) {
                                game.getGameStart().getAcceptPlayerDialog().dismiss();
                            }
                        }
                        game.getGameEnd().onOpponentDisconnect();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void loadOpponentPhoto() {
        StorageReference rr;
        if (game.getRunningGame().isThisPlayerPlaysWhite()) {
            rr = storageRef.child(game.getRunningGame().getPlayer2().getId());
        } else {
            rr = storageRef.child(game.getRunningGame().getPlayer1().getId());
        }
        rr.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                game.setJoinedAvaUri(uri);
                Picasso.with(game.getActivity()).load(uri).into(game.getPlayerJoinedAva());
                game.getGameStart().getAcceptPlayerDialog().show();
                game.getWaitingPlayers().dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                game.getPlayerJoinedAva().setImageResource(R.drawable.empty_avatar);
                game.getGameStart().getAcceptPlayerDialog().show();
                game.getWaitingPlayers().dismiss();
            }
        });
    }

    void loadThisPlayerPhoto(final ImageView avatar) {
        storageRef.child(mAuth.getCurrentUser().getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(game.getActivity()).load(uri).networkPolicy(NetworkPolicy.OFFLINE).into(avatar, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        storageRef.child(mAuth.getCurrentUser().getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.with(game.getActivity()).load(uri).fit().into(avatar);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                avatar.setImageResource(R.drawable.empty_avatar);
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

    void addDatabaseListenersForCurrentGame() {
        DatabaseReference opponentTimeRestRef = getTimeRestReference();
        opponentTimeRestDB = new FirebaseHelper(opponentTimeRestRef, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    long l = dataSnapshot.getValue(Long.class);
                    receivedFromOpponent.onOpponentTimeUpdated(l);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        player1PauseCountDB = new FirebaseHelper(gameRef.child(RunningGame.PLAYER_1).child(PlayerGameParams.PAUSE_COUNT), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    game.getRunningGame().getPlayer1().getPlayerGameParams().setPauseCount(dataSnapshot.getValue(Integer.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        player2PauseCountDB = new FirebaseHelper(gameRef.child(RunningGame.PLAYER_2).child(PlayerGameParams.PAUSE_COUNT), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    game.getRunningGame().getPlayer2().getPlayerGameParams().setPauseCount(dataSnapshot.getValue(Integer.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        currentGameStatusDB = new FirebaseHelper(gameRef.child(RunningGame.GAME_STATUS), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    int status = dataSnapshot.getValue(Integer.class);
                    receivedFromOpponent.onGameStatusUpdated(status);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        coordinatesDB = new FirebaseHelper(gameRef.child(Coordinates.COORDINATES), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Coordinates c = dataSnapshot.getValue(Coordinates.class);
                    if (c.getA() != 0) {
                        if (!game.getRunningGame().getCoordinates().equals(c)) {
                            game.getGameMove().setMoveFromOpponent(true);
                            receivedFromOpponent.onCoordinatesUpdated(c);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        chatLogsDB = new FirebaseHelper(gameRef.child(RunningGame.CHAT_LOGS), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                    };
                    List<String> list = dataSnapshot.getValue(t);
                    String message = list.get(list.size() - 1);
                    receivedFromOpponent.onChatLogsUpdated(message);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    void recordGameResultToDB() {

        gameRef.child(RunningGame.GAME_STATUS).setValue(game.getRunningGame().getStatus());
        int rating1 = game.getRunningGame().getPlayer1().getRating();
        int rating2 = game.getRunningGame().getPlayer2().getRating();
        String result;
        if (game.getTimer1().getText().toString().equals(game.getString(R.string.loser))) {
            game.getRunningGame().getPlayer1().resultsAfterLose(rating2);
            game.getRunningGame().getPlayer2().resultsAfterWin(rating1);
            result = FinishedGame.RESULT_LOSE;
        } else if (game.getTimer1().getText().toString().equals(game.getString(R.string.winner))) {
            game.getRunningGame().getPlayer1().resultsAfterWin(rating2);
            game.getRunningGame().getPlayer2().resultsAfterLose(rating1);
            result = FinishedGame.RESULT_WIN;
        } else {
            game.getRunningGame().getPlayer1().resultsAfterDraw(rating2);
            game.getRunningGame().getPlayer2().resultsAfterDraw(rating1);
            result = FinishedGame.RESULT_DRAW;
        }
        FinishedGame finishedGame = new FinishedGame(game.getRunningGame().getPlayer1(), game.getRunningGame().getPlayer2(), game.getRunningGame().getMoveList(), result,
                game.getGameExtraParams().getReason(), getPlayerMovesCount(game.getRunningGame()), game.getRunningGame().getPlayer1().getPlayerGameParams().getTimeControl(),
                game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker1(), game.getRunningGame().getPlayer1().getPlayerGameParams().getTimePicker2());
        DatabaseReference finishedGameRef = mDatabase.child(FinishedGame.FINISHED_GAME).push();
        finishedGameRef.setValue(finishedGame);
        finishedGameRef.child(Player.TIMESTAMP).setValue(ServerValue.TIMESTAMP);
        game.getRunningGame().getPlayer1().setPlayerGameParams(null);
        game.getRunningGame().getPlayer2().setPlayerGameParams(null);
        gameRef.setValue(game.getRunningGame());
        mDatabase.child(Player.PLAYERS).child(game.getRunningGame().getPlayer1().getId()).setValue(game.getRunningGame().getPlayer1());
        mDatabase.child(Player.PLAYERS).child(game.getRunningGame().getPlayer2().getId()).setValue(game.getRunningGame().getPlayer2());
    }

    private DatabaseReference getTimeRestReference() {
        DatabaseReference ref;
        if (game.getRunningGame().isThisPlayerPlaysWhite()) {
            ref = gameRef.child(RunningGame.PLAYER_2).child(PlayerGameParams.PLAYER_GAME_PARAMS).child(PlayerGameParams.TIME_REST);
        } else {
            ref = gameRef.child(RunningGame.PLAYER_1).child(PlayerGameParams.PLAYER_GAME_PARAMS).child(PlayerGameParams.TIME_REST);
        }
        return ref;
    }

    public void removeDatabaseListenersForCurrentGame() {
        if (opponentTimeRestDB != null) {
            opponentTimeRestDB.getRef().removeEventListener(opponentTimeRestDB.getValueEventListener());
        }
        if (player1PauseCountDB != null) {
            player1PauseCountDB.getRef().removeEventListener(player1PauseCountDB.getValueEventListener());
        }
        if (player2PauseCountDB != null) {
            player2PauseCountDB.getRef().removeEventListener(player2PauseCountDB.getValueEventListener());
        }
        if (currentGameDB != null) {
            currentGameDB.getRef().removeEventListener(currentGameDB.getValueEventListener());
        }
        if (currentGameStatusDB != null) {
            currentGameStatusDB.getRef().removeEventListener(currentGameStatusDB.getValueEventListener());
        }
        if (coordinatesDB != null) {
            coordinatesDB.getRef().removeEventListener(coordinatesDB.getValueEventListener());
        }
        if (chatLogsDB != null) {
            chatLogsDB.getRef().removeEventListener(chatLogsDB.getValueEventListener());
        }
    }

    DatabaseReference getGameRef() {
        return gameRef;
    }

    FirebaseHelper getCurrentGameDB() {
        return currentGameDB;
    }

    void setGameRef(DatabaseReference gameRef) {
        this.gameRef = gameRef;
    }

    StorageReference getStorageRef() {
        return storageRef;
    }

    FirebaseAuth getmAuth() {
        return mAuth;
    }

    ReceivedFromOpponent getReceivedFromOpponent() {
        return receivedFromOpponent;
    }

}
