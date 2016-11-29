package com.pkiykov.netchess.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.logic.GameTime;
import com.pkiykov.netchess.pojo.Player;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PlayersListAdapter extends RecyclerView.Adapter<PlayersListAdapter.ViewHolder> {
    private Listener listener;
    private ArrayList<Player> list;
    private RelativeLayout layout;
    private Context context;
    private boolean isRankList;

    public PlayersListAdapter(ArrayList<Player> list, Context context, boolean isRankList) {
        this.list = list;
        this.context = context;
        this.isRankList = isRankList;
    }

    @Override
    public PlayersListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        layout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_current_games, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(final PlayersListAdapter.ViewHolder holder, int position) {
        if (list.size() > 0) {
            String id = String.valueOf(list.get(position).getId());
            String name = list.get(position).getName();
            String rating = context.getString(R.string.rating) + String.valueOf(list.get(position).getRating());
            String extraTextView1;
            String extraTextView2;
            if (isRankList) {
                extraTextView1 = context.getString(R.string.age) + list.get(position).getAge();
                int total = list.get(position).getWins() + list.get(position).getLosses() + list.get(position).getDraws();
                extraTextView2 = context.getString(R.string.total) + total;
            } else {
                extraTextView1 = context.getString(R.string.plays)
                        + (list.get(position).isColor() ? context.getString(R.string.white) : context.getString(R.string.black));
                extraTextView2 = GameTime.getTimeInfo(context, list.get(position).getPlayerGameParams().getTimeControl(),
                        list.get(position).getPlayerGameParams().getTimePicker1(),
                        list.get(position).getPlayerGameParams().getTimePicker2());
            }
            loadPhoto(id, holder.avatar);
            holder.playerName.setText(name);
            holder.playerRating.setText(rating);
            holder.extraTextView1.setText(extraTextView1);
            holder.extraTextView2.setText(extraTextView2);

            if (list.get(position).getRank() == 0) {
                holder.position.setText(String.valueOf(position + 1));
            } else {
                holder.position.setText(String.valueOf(list.get(position).getRank()));
            }

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

    private void loadPhoto(String id, final ImageView avatar) {
        StorageReference storage = FirebaseStorage.getInstance().getReference().child(Player.AVATARS).child(id);
        storage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(avatar.getContext())
                        .load(uri.toString())
                        .fit()
                        .into(avatar);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                avatar.setImageResource(R.drawable.empty_avatar);
            }
        });
    }


    public void setListener(Listener listener) {
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView playerName;
        private TextView playerRating;
        private TextView extraTextView1;
        private TextView extraTextView2;
        private ImageView avatar;
        private TextView position;

        ViewHolder(View itemView) {
            super(itemView);

            playerName = (TextView) layout.findViewById(R.id.player_name);
            playerRating = (TextView) layout.findViewById(R.id.player_rating);

            extraTextView1 = (TextView) layout.findViewById(R.id.extra1_tv);
            extraTextView2 = (TextView) layout.findViewById(R.id.extra2_tv);

            position = (TextView) layout.findViewById(R.id.number_tv);
            avatar = (ImageView) layout.findViewById(R.id.avatar);
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
