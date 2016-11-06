package com.pkiykov.netchess.logic;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.pkiykov.netchess.async_tasks.Peer2PeerSockets;
import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.fragments.Game;
import com.pkiykov.netchess.fragments.PeerToPeerDevicesList;
import com.pkiykov.netchess.pojo.RunningGame;
import com.pkiykov.netchess.receivers.WifiP2PReceiver;
import com.pkiykov.netchess.services.FileTransferService;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import static com.pkiykov.netchess.services.FileTransferService.ACTION_SEND_FILE;
import static com.pkiykov.netchess.services.FileTransferService.EXTRAS_HOST_ADDRESS;

public class Peer2Peer {

    public static final String USER_INFO = "user_profile_info";
    private static final String GAME_CREATED = "_NetChess_game_created";
    private static final String GAME_SEARCHING = "_NetChess_game_searching";
    public static final int SOCKET_PORT_GROUP_OWNER = 8899;
    public static final int SOCKET_PORT_NOT_GROUP_OWNER = 9988;


    private ReceivedFromOpponent receivedFromOpponent;
    private ProgressDialog waitingResponseDialog;
    private final Activity activity;
    private Game game;
    private WifiP2pManager.PeerListListener peerListListener;
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    private WifiP2PReceiver p2pReceiver;
    private ProgressDialog fetchingGameDialog;
    private IntentFilter intentFilterP2P;
    private WifiP2pDevice device;
    private WifiP2pInfo wifiP2pInfo;
    private InetAddress hostIneptAddress;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Peer2PeerSockets peer2PeerSockets;

    public Peer2Peer(Activity activity, Game game) {
        this.game = game;
        this.activity = activity;
    }

    public void sendAction(String action, Object object) {

        Intent serviceIntent = new Intent(activity, FileTransferService.class);

        Map<String, Object> map = new HashMap<>();
        map.put(action, object);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRAS_HOST_ADDRESS, hostIneptAddress.getHostAddress());
        bundle.putSerializable(ACTION_SEND_FILE, (Serializable) map);
        serviceIntent.putExtras(bundle);
        serviceIntent.setAction(ACTION_SEND_FILE);
        if (wifiP2pInfo.isGroupOwner) {
            serviceIntent.putExtra(FileTransferService.EXTRAS_SERVER_PORT, SOCKET_PORT_NOT_GROUP_OWNER);
        } else {
            serviceIntent.putExtra(FileTransferService.EXTRAS_SERVER_PORT, SOCKET_PORT_GROUP_OWNER);
        }
        activity.startService(serviceIntent);
    }

    public void initializeWifiP2P() {
        intentFilterP2P = new IntentFilter();
        intentFilterP2P.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilterP2P.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilterP2P.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilterP2P.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(activity, activity.getMainLooper(), null);
        registerP2PReceiver();
    }

    public void sendGame() {
        Game gameFragment = (Game) activity.getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
        RunningGame game = gameFragment.getRunningGame();
        ((GameActivity) activity).getPeer2Peer().sendAction(RunningGame.RUNNING_GAME, game);
        gameFragment.getWaitingPlayers().dismiss();
    }


    public void startRegistration(int portNumber) {
        Game gameFragment = (Game) activity.getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
        String gameCreated = GAME_SEARCHING;
        if (gameFragment != null && gameFragment.getGameType() == Game.LAN_GAME && gameFragment.getRunningGame() != null
                && gameFragment.getRunningGame().getStatus() == RunningGame.WAITING_OPPONENT) {
            gameCreated = GAME_CREATED;
        }
        Map<String, String> record = new HashMap<>();
        record.put("listenport", String.valueOf(portNumber));
        record.put("available", "visible");
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance(gameCreated, "_presence._tcp", record);

        manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                discoverService();
            }

            @Override
            public void onFailure(int arg0) {
            }
        });

        setUpConnectionInfoListener();

    }

    private void setUpConnectionInfoListener() {
        connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                if (Peer2Peer.this.wifiP2pInfo == null) {
                    Peer2Peer.this.wifiP2pInfo = wifiP2pInfo;
                    if (wifiP2pInfo.groupFormed) {
                        peer2PeerSockets = (Peer2PeerSockets) new Peer2PeerSockets(activity).execute();
                    }

                    Game gameFragment = (Game) activity.getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
                    if (gameFragment == null || gameFragment.getGameType() != Game.LAN_GAME || gameFragment.getRunningGame() == null
                            || gameFragment.getRunningGame().getStatus() != RunningGame.WAITING_OPPONENT) {
                        fetchingGameDialog = new ProgressDialog(activity);
                        fetchingGameDialog.setMessage(activity.getString(R.string.fetching_game_info));
                        fetchingGameDialog.setCanceledOnTouchOutside(false);
                        fetchingGameDialog.setCancelable(true);
                        fetchingGameDialog.show();
                        waitingResponseDialog.dismiss();
                        waitingResponseDialog = null;
                    }

                }
            }
        };
    }

    private void discoverService() {
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {

            }
        };
        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                if (GAME_CREATED.equals(instanceName)) {
                    Game gameFragment = (Game) activity.getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
                    if (gameFragment == null || gameFragment.getGameType() != Game.LAN_GAME || gameFragment.getRunningGame() == null
                            || gameFragment.getRunningGame().getStatus() != RunningGame.WAITING_OPPONENT) {
                        PeerToPeerDevicesList fragment = (PeerToPeerDevicesList) activity.getFragmentManager()
                                .findFragmentByTag(PeerToPeerDevicesList.class.getSimpleName());
                        if (fragment.getPeers().size() > 0) {
                            for (int i = 0; i < fragment.getPeers().size(); i++) {
                                if (fragment.getPeers().get(i).deviceAddress.equals(resourceType.deviceAddress)) {
                                    fragment.getPeers().set(i, resourceType);
                                    ((ArrayAdapter) fragment.getAdapter()).notifyDataSetChanged();
                                    return;
                                }
                            }
                        }
                        fragment.getPeers().add(resourceType);
                        if (fragment.getPeerDiscoveringDialog() != null && fragment.getPeerDiscoveringDialog().isShowing()) {
                            fragment.getPeerDiscoveringDialog().dismiss();
                            fragment.setPeerDiscoveringDialog(null);
                        }
                        ((ArrayAdapter) fragment.getAdapter()).notifyDataSetChanged();
                    }
                }
            }
        };

        manager.setDnsSdResponseListeners(channel, servListener, txtListener);

        manager.addServiceRequest(channel,
                WifiP2pDnsSdServiceRequest.newInstance(),
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(int code) {
                    }
                });
        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Game gameFragment = (Game) activity.getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
                if (gameFragment == null || gameFragment.getGameType() != Game.LAN_GAME || gameFragment.getRunningGame() == null
                        || gameFragment.getRunningGame().getStatus() != RunningGame.WAITING_OPPONENT) {
                    peerListListener = new WifiP2pManager.PeerListListener() {
                        @Override
                        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                        }
                    };
                }
            }

            @Override
            public void onFailure(int code) {
                if (code == WifiP2pManager.P2P_UNSUPPORTED) {
                    Toast.makeText(activity, R.string.p2p_not_supported, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d("MyTag", "Service connected!");
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(activity, R.string.connection_failed, Toast.LENGTH_SHORT).show();
                if (waitingResponseDialog != null && waitingResponseDialog.isShowing()) {
                    waitingResponseDialog.dismiss();
                }
            }
        });
        waitingResponseDialog = new ProgressDialog(activity);
        waitingResponseDialog.setCancelable(true);
        waitingResponseDialog.setCanceledOnTouchOutside(false);
        waitingResponseDialog.setMessage(activity.getString(R.string.waiting_for_invite_response));
        waitingResponseDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (waitingResponseDialog != null) {
                    cancelConnect(false);
                    Toast.makeText(activity, activity.getString(R.string.connection_canceled), Toast.LENGTH_SHORT).show();
                }
            }
        });
        waitingResponseDialog.show();
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (waitingResponseDialog != null) {
                    waitingResponseDialog.cancel();
                }
            }
        };
        Handler pdHandler = new Handler();
        pdHandler.postDelayed(progressRunnable, 30000);
    }

    private void cancelConnect(final boolean exit) {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (exit) {
                    activity.finish();
                    System.exit(1);
                }
            }

            @Override
            public void onFailure(int i) {
                if (exit) {
                    activity.finish();
                    System.exit(1);
                }
            }
        });
    }

    public void disconnect(final boolean exit) {
        unregisterP2PReceiver();
        if (peer2PeerSockets != null) {
            peer2PeerSockets.closeSockets();
        }
        if (manager != null && channel != null) {
            cancelConnect(exit);
        }
        wifiP2pInfo = null;
        manager = null;
        channel = null;


    }

    public void registerP2PReceiver() {
        if (manager != null && channel != null) {
            p2pReceiver = new WifiP2PReceiver(manager, channel, activity);
            activity.registerReceiver(p2pReceiver, intentFilterP2P);
        }
    }

    public void unregisterP2PReceiver() {
        if (p2pReceiver != null) {
            activity.unregisterReceiver(p2pReceiver);
            p2pReceiver = null;
        }
    }

    public void exit() {
        if (manager != null) {
            disconnect(true);
        } else {
            activity.finish();
            System.exit(1);
        }
    }

    public WifiP2pManager.PeerListListener getPeerListListener() {
        return peerListListener;
    }

    public WifiP2pManager.ConnectionInfoListener getConnectionInfoListener() {
        return connectionInfoListener;
    }

    public WifiP2pInfo getWifiP2pInfo() {
        return wifiP2pInfo;
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    public void setDevice(WifiP2pDevice device) {
        this.device = device;
    }

    public ProgressDialog getFetchingGameDialog() {
        return fetchingGameDialog;
    }

    public WifiP2pManager.Channel getChannel() {
        return channel;
    }

    public WifiP2pManager getManager() {
        return manager;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setReceivedFromOpponent(ReceivedFromOpponent receivedFromOpponent) {
        this.receivedFromOpponent = receivedFromOpponent;
    }

    public ReceivedFromOpponent getReceivedFromOpponent() {
        return receivedFromOpponent;
    }

    public InetAddress getHostIneptAddress() {
        return hostIneptAddress;
    }

    public void setHostIneptAddress(InetAddress hostIneptAddress) {
        this.hostIneptAddress = hostIneptAddress;
    }

    void dismissFetchingDialog() {
        if (fetchingGameDialog != null) {
            if (fetchingGameDialog.isShowing()) {
                fetchingGameDialog.dismiss();
            }
        }
    }


}
