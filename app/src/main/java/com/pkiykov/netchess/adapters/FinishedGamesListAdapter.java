package com.pkiykov.netchess.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pkiykov.netchess.R;
import com.pkiykov.netchess.pojo.FinishedGame;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FinishedGamesListAdapter extends RecyclerView.Adapter<FinishedGamesListAdapter.ViewHolder> {
    private FinishedGamesListAdapter.Listener listener;
    private ArrayList<FinishedGame> list;
    private LinearLayout layout;

    private Context context;
    private SimpleDateFormat sfd;

    public FinishedGamesListAdapter(ArrayList<FinishedGame> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public FinishedGamesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        sfd = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_finished_games_list, parent, false);
        return new FinishedGamesListAdapter.ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(final FinishedGamesListAdapter.ViewHolder holder, int position) {
        if (list.size() > 0) {
            String white = context.getString(R.string.white) + ": [" + list.get(position).getPlayer1().getRating()
                    + "] " + list.get(position).getPlayer1().getName();
            String black = context.getString(R.string.black) + ": [" + list.get(position).getPlayer2().getRating()
                    + "] " + list.get(position).getPlayer2().getName();
            String date = sfd.format(new Date(list.get(position).getTimestampCreatedLong()));
            String result = list.get(position).getResult();

            holder.whitePlayer.setText(white);
            holder.blackPlayer.setText(black);
            holder.gameDate.setText(date);
            holder.gameResult.setText(result);

            final int p = position;
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClick(p);
                    }
                }
            });
        }
    }

    public void setListener(FinishedGamesListAdapter.Listener listener) {
        this.listener = listener;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView whitePlayer;
        private TextView blackPlayer;
        private TextView gameDate;
        private TextView gameResult;

        ViewHolder(View itemView) {
            super(itemView);

            whitePlayer = (TextView) layout.findViewById(R.id.white_player_tv);
            blackPlayer = (TextView) layout.findViewById(R.id.black_player_tv);
            gameDate = (TextView) layout.findViewById(R.id.game_date_tv);
            gameResult = (TextView) layout.findViewById(R.id.game_result_tv);
        }
    }

    public interface Listener {
        void onClick(int position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
