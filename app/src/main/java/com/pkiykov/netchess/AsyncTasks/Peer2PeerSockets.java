package com.pkiykov.netchess.AsyncTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.others.ConnectionPeer2Peer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static com.pkiykov.netchess.logic.Peer2Peer.SOCKET_PORT_GROUP_OWNER;
import static com.pkiykov.netchess.logic.Peer2Peer.SOCKET_PORT_NOT_GROUP_OWNER;

public class Peer2PeerSockets extends AsyncTask<Object, Object, Object> {
    public static final String INET_ADRESS = "inetAdress";
    private Activity activity;
    private ServerSocket serverSocket;
    private Socket connectedClientSocket;

    public Peer2PeerSockets(Activity activity) {
        this.activity = activity;
    }

    public void closeSockets(){

        if(connectedClientSocket!= null){
            try {
                connectedClientSocket.close();
                Log.d("MyTag", "Socket client closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(serverSocket!= null){
            try {
                serverSocket.close();
                Log.d("MyTag", "Socket server closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Object doInBackground(Object... objects) {
        boolean owner = ((GameActivity) activity).getPeer2Peer().getWifiP2pInfo().isGroupOwner;
        try {

            if (owner) {
               serverSocket = new ServerSocket(SOCKET_PORT_GROUP_OWNER);
                Log.d("MyTag", "Server: Socket opened");
                //noinspection InfiniteLoopStatement
                while (true) {
                   connectedClientSocket = serverSocket.accept();
                    if (((GameActivity) activity).getPeer2Peer().getHostIneptAddress() == null) {
                        InetAddress inetAddress = connectedClientSocket.getInetAddress();
                        ((GameActivity) activity).getPeer2Peer().setHostIneptAddress(inetAddress);
                        if (((GameActivity) activity).getPeer2Peer().getFetchingGameDialog() == null) {
                            ((GameActivity) activity).getPeer2Peer().sendGame();
                        }
                    }
                    new Thread(new ConnectionPeer2Peer(activity, connectedClientSocket)).start();
                    Log.d("MyTag", "Server: ConnectionPeer2Peer done");
                }
            } else {
                InetAddress inetAdress = ((GameActivity) activity).getPeer2Peer().getWifiP2pInfo().groupOwnerAddress;
                ((GameActivity) activity).getPeer2Peer().setHostIneptAddress(inetAdress);
               serverSocket = new ServerSocket(SOCKET_PORT_NOT_GROUP_OWNER);
                Log.d("MyTag", "Server: Socket opened");
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (((GameActivity) activity).getPeer2Peer().getFetchingGameDialog() == null) {
                    ((GameActivity) activity).getPeer2Peer().sendGame();
                } else {
                    ((GameActivity) activity).getPeer2Peer().sendAction(INET_ADRESS, InetAddress.getLocalHost().getHostAddress());
                }
                //noinspection InfiniteLoopStatement
                while (true) {
                   connectedClientSocket = serverSocket.accept();
                    Log.d("MyTag", "Server: ConnectionPeer2Peer done");
                    new Thread(new ConnectionPeer2Peer(activity, connectedClientSocket)).start();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
