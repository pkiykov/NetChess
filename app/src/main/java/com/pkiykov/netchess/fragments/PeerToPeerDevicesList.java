package com.pkiykov.netchess.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;

import java.util.ArrayList;
import java.util.List;

public class PeerToPeerDevicesList extends Fragment {

    private ArrayList<WifiP2pDevice> peers;
    private LinearLayout linearLayout;
    private ProgressDialog peerDiscoveringDialog;
    private AlertDialog peerDetailDialog;
    private WiFiPeerListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        peers = new ArrayList<>();
        linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_peers_list, container, false);
        ListView listView = (ListView) linearLayout.findViewById(R.id.peers_list_view);
        adapter = new WiFiPeerListAdapter(getActivity(), R.layout.item_devices, peers);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((GameActivity) getActivity()).getPeer2Peer().setDevice((WifiP2pDevice) adapterView.getItemAtPosition(i));
                if (((GameActivity) getActivity()).getPeer2Peer().getDevice().status == 3
                        || ((GameActivity) getActivity()).getPeer2Peer().getDevice().status == 0) {
                    connectToPeer(((GameActivity) getActivity()).getPeer2Peer().getDevice());
                } else {
                    Toast.makeText(getActivity(), R.string.you_can_join_only_to_available_or_already_connected_devices, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button startDiscovering = (Button) linearLayout.findViewById(R.id.discover_peers_button);
        startDiscovering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                peers.clear();
                ((GameActivity) getActivity()).getPeer2Peer().startRegistration(1234);
                peerDiscoveringDialog = new ProgressDialog(getActivity());
                peerDiscoveringDialog.setCanceledOnTouchOutside(false);
                peerDiscoveringDialog.setCancelable(true);
                peerDiscoveringDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {

                        ((GameActivity) getActivity()).getPeer2Peer().getManager().stopPeerDiscovery(
                                ((GameActivity) getActivity()).getPeer2Peer().getChannel(), new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        peers.clear();
                                        adapter.notifyDataSetChanged();

                                    }

                                    @Override
                                    public void onFailure(int i) {
                                    }
                                });
                        peerDiscoveringDialog.dismiss();
                        peerDiscoveringDialog = null;
                    }
                });
                peerDiscoveringDialog.setMessage(getActivity().getString(R.string.discovering_peers));
                peerDiscoveringDialog.show();
                Runnable progressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if(peerDiscoveringDialog!=null) {
                            peerDiscoveringDialog.cancel();
                            Toast.makeText(getActivity(), R.string.nothing_found, Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                Handler pdHandler = new Handler();
                pdHandler.postDelayed(progressRunnable,6000);
            }
        });
        ((GameActivity) getActivity()).getPeer2Peer().initializeWifiP2P();
        return linearLayout;
    }


    private void connectToPeer(WifiP2pDevice device) {
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_device_detail, null);

        TextView addressTv = (TextView) v.findViewById(R.id.device_adress);
        String address = getString(R.string.device_adress) + " " + device.deviceAddress;
        addressTv.setText(address);

        TextView nameTv = (TextView) v.findViewById(R.id.device_name);
        String name = getString(R.string.device_name) + " " + device.deviceName;
        nameTv.setText(name);

        TextView statusTv = (TextView) v.findViewById(R.id.device_status);
        String status = getString(R.string.status) + " " + getDeviceStatus(device.status);
        statusTv.setText(status);

        builder.setView(v);
        builder.setCancelable(true);

        Button connectbtn = (Button) v.findViewById(R.id.btn_connect);
        Button disconnectBtn = (Button) v.findViewById(R.id.btn_disconnect);
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                peerDetailDialog.dismiss();
            }
        });
        connectbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((GameActivity) getActivity()).getPeer2Peer().connect(config);
                peerDetailDialog.dismiss();
            }
        });
        peerDetailDialog = builder.create();
        peerDetailDialog.show();
    }

    public void updateThisDevice(WifiP2pDevice device) {
        TextView view = (TextView) linearLayout.findViewById(R.id.tv_my_name);
        String name = getString(R.string.device_name) + " " + device.deviceName;
        view.setText(name);
        view = (TextView) linearLayout.findViewById(R.id.tv_my_status);
        String status = getString(R.string.status) + " " + getDeviceStatus(device.status);
        view.setText(status);
    }

    private String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }


    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {
        private List<WifiP2pDevice> items;

        WiFiPeerListAdapter(Context context, int textViewResourceId,
                            List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.item_devices, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView name = (TextView) v.findViewById(R.id.device_name);
                TextView status = (TextView) v.findViewById(R.id.device_details);
                name.setText(device.deviceName);
                status.setText(getDeviceStatus(device.status));
            }
            return v;
        }
    }

    public WiFiPeerListAdapter getAdapter() {
        return adapter;
    }

    public ArrayList<WifiP2pDevice> getPeers() {
        return peers;
    }

    public ProgressDialog getPeerDiscoveringDialog() {
        return peerDiscoveringDialog;
    }

    public void setPeerDiscoveringDialog(ProgressDialog peerDiscoveringDialog) {
        this.peerDiscoveringDialog = peerDiscoveringDialog;
    }
}
