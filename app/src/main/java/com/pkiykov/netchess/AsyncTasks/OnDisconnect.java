package com.pkiykov.netchess.AsyncTasks;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.os.AsyncTask;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.fragments.Game;

import java.util.concurrent.TimeUnit;

public class OnDisconnect extends AsyncTask<Integer, Integer, String> {

    private Fragment gameFragment;
    private Dialog disconnectDialog;
    private ProgressBar disconnectProgressBar;
    private TextView disconnectSecondsRemain;
    private GameActivity activity;

    public OnDisconnect(GameActivity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        gameFragment = activity.getFragmentManager().findFragmentByTag(Game.class.getSimpleName());
        @SuppressLint("InflateParams")
        RelativeLayout relativeLayout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.dialog_disconnect_10sec,  null);
        disconnectDialog = new Dialog(activity);
        disconnectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        disconnectDialog.setContentView(relativeLayout);
        disconnectDialog.setCancelable(false);
        disconnectSecondsRemain = (TextView) relativeLayout.findViewById(R.id.disconnect_countdown);
        disconnectProgressBar = (ProgressBar) relativeLayout.findViewById(R.id.disconnect_progress);
        disconnectProgressBar.setVisibility(View.VISIBLE);
        disconnectProgressBar.setProgress(0);
        Button discNow = (Button) relativeLayout.findViewById(R.id.disconnect_now_button);
        discNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnectDialog.dismiss();
                cancel(true);
                ((Game) gameFragment).getGameEnd().onThisPlayerDisconnect();
            }
        });
        disconnectDialog.show();
    }

    @Override
    protected String doInBackground(Integer... values) {
        int count = 10;
        while (count > 0) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count--;
            publishProgress(count);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(100 - values[0] * 10);
        disconnectProgressBar.setProgress(100 - values[0] * 10);
        disconnectSecondsRemain.setText(activity.getString(R.string.you_will_be_disconnected) + " " + values[0] + " "
                + activity.getString(R.string.seconds_simple));
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        disconnectDialog.dismiss();
        ((Game) gameFragment).getGameEnd().onThisPlayerDisconnect();
    }

}
