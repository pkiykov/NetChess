package com.pkiykov.netchess.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;


public class FileTransferService extends IntentService {

    public static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_HOST_ADDRESS = "go_host";
    public static final String EXTRAS_SERVER_PORT = "go_port";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            int port = intent.getExtras().getInt(EXTRAS_SERVER_PORT);
            Bundle bundle = intent.getExtras();
            String host = bundle.getString(EXTRAS_HOST_ADDRESS);
            Map map = (Map) bundle.getSerializable(ACTION_SEND_FILE);
            DataOutputStream stream = null;
            Socket socket = new Socket();
            try {
                if (map != null) {
                    Log.d("MyTag", "trying to connect to "+host+", + "+" to port "+port);
                    socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                    OutputStream outputStream = socket.getOutputStream();
                    stream = new DataOutputStream(outputStream);
                    stream.write(prepareBytesToSend(map));
                    stream.close();

                }
            } catch (IOException e) {
                Log.d("MyTag", e.getMessage());
            } finally {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private byte[] prepareBytesToSend(Map game) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        byte[] result = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(game);
            out.flush();
            result = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ignored) {
            }
        }
        return result;
    }
}
