
package com.pkiykov.netchess.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.pkiykov.netchess.others.FirebaseHelper;
import com.pkiykov.netchess.pojo.Player;
import com.pkiykov.netchess.pojo.RunningGame;
import com.pkiykov.netchess.receivers.OpponentDisconnectReceiver;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.pkiykov.netchess.receivers.OpponentDisconnectReceiver.OPPONENT_DISCONNECT_ACTION;


public class OnlineCheckPlayerService extends Service {
    public static final String COLOR = "player's color";
    public static final String GAME_ID = "gameId";

    private final IBinder mBinder = new LocalBinder();

    private boolean thisPlayerPlaysWhite, flag;
    private long player1timestamp;
    private long player2timestamp;
    private ExecutorService executorService;
    private OpponentDisconnectReceiver receiver;

    private DatabaseReference gameRef;
    private FirebaseHelper player1timestampDB;
    private FirebaseHelper player2timestampDB;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player1timestamp = 0L;
        player2timestamp = 0L;
        executorService = Executors.newFixedThreadPool(1);
    }


    public class LocalBinder extends Binder {
        public OnlineCheckPlayerService getService() {
            return OnlineCheckPlayerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("MyTag","service started!!!");
        thisPlayerPlaysWhite = intent.getBooleanExtra(COLOR, true);
        String gameId = intent.getStringExtra(GAME_ID);

        gameRef = FirebaseDatabase.getInstance().getReference().child(RunningGame.RUNNING_GAME).child(gameId);

        player1timestampDB = new FirebaseHelper(gameRef.child(RunningGame.PLAYER_1).child(Player.TIMESTAMP), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    player1timestamp = dataSnapshot.getValue(Long.class);
                } else {
                    player1timestamp = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        player2timestampDB = new FirebaseHelper(gameRef.child(RunningGame.PLAYER_2).child(Player.TIMESTAMP), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    player2timestamp = dataSnapshot.getValue(Long.class);
                } else {
                    player2timestamp = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        RunChecking r = new RunChecking();
        executorService.execute(r);
        return START_NOT_STICKY;
    }

    public void registerOpponentDisconnectReceiver(OpponentDisconnectReceiver receiver) {
        this.receiver = receiver;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OPPONENT_DISCONNECT_ACTION);
        registerReceiver(this.receiver, intentFilter);
    }

    public void unregisterMyReceiver() {
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    class RunChecking implements Runnable {

        private int countLags;

        @Override
        public void run() {
            flag = true;
            countLags = 0;
            while (flag) {
                if (thisPlayerPlaysWhite) {
                    gameRef.child(RunningGame.PLAYER_1).child(Player.TIMESTAMP).setValue(ServerValue.TIMESTAMP);
                } else {
                    gameRef.child(RunningGame.PLAYER_2).child(Player.TIMESTAMP).setValue(ServerValue.TIMESTAMP);
                }
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (player1timestamp > 0 && player2timestamp > 0 && flag) {
                    stopAndIntent();
                }
            }
            unregisterMyReceiver();
            player1timestampDB.getRef().removeEventListener(player1timestampDB.getValueEventListener());
            player2timestampDB.getRef().removeEventListener(player2timestampDB.getValueEventListener());
            stopSelf();
        }

        private void stopAndIntent() {
            if (compareTimestamps()) {
                if (countLags < 3) {
                    countLags++;
                } else {
                    Intent intent = new Intent();
                    intent.setAction(OpponentDisconnectReceiver.OPPONENT_DISCONNECT_ACTION);
                    sendBroadcast(intent);
                }
            } else {
                countLags = 0;
            }
        }

        private boolean compareTimestamps() {
            if (thisPlayerPlaysWhite) {
                return player1timestamp - player2timestamp > 3300;
            } else {
                return player2timestamp - player1timestamp > 3300;
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.v("MyTag", "service destroyed !!! ");
        super.onDestroy();
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
