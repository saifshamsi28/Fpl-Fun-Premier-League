package com.zpl.handcricket.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.zpl.handcricket.R;
import com.zpl.handcricket.models.LeaderboardEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.VH> {

    private final List<LeaderboardEntry> items = new ArrayList<>();

    public void setItems(List<LeaderboardEntry> rows) {
        items.clear();
        if (rows != null) {
            items.addAll(rows);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        LeaderboardEntry e = items.get(position);

        h.txtRank.setText("#" + e.rank);
        h.txtName.setText(displayName(e));
        h.txtRate.setText(String.format(Locale.US, "%.1f%%", e.winRate));
        h.txtPlayed.setText(String.valueOf(e.matchesPlayed));
        h.txtWon.setText(String.valueOf(e.matchesWon));
        h.txtRuns.setText(String.valueOf(e.totalRuns));
        h.txtTeam.setText(notBlank(e.teamName) ? e.teamName : "-");
        h.txtCity.setText(notBlank(e.city) ? e.city : "-");

        if (e.isYou) {
            h.root.setBackgroundResource(R.drawable.bg_leaderboard_row_you);
            ViewCompat.setElevation(h.root, 9f);
            h.txtName.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            h.root.setBackgroundResource(R.drawable.bg_leaderboard_row);
            ViewCompat.setElevation(h.root, 4f);
            h.txtName.setTextColor(Color.parseColor("#F7FBFF"));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String displayName(LeaderboardEntry e) {
        if (notBlank(e.fullName)) {
            return e.fullName.trim();
        }
        if (notBlank(e.username)) {
            return e.username.trim();
        }
        return "-";
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    static class VH extends RecyclerView.ViewHolder {
        final View root;
        final TextView txtRank;
        final TextView txtName;
        final TextView txtRate;
        final TextView txtPlayed;
        final TextView txtWon;
        final TextView txtRuns;
        final TextView txtTeam;
        final TextView txtCity;

        VH(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            txtRank = itemView.findViewById(R.id.txtRank);
            txtName = itemView.findViewById(R.id.txtName);
            txtRate = itemView.findViewById(R.id.txtRate);
            txtPlayed = itemView.findViewById(R.id.txtPlayed);
            txtWon = itemView.findViewById(R.id.txtWon);
            txtRuns = itemView.findViewById(R.id.txtRuns);
            txtTeam = itemView.findViewById(R.id.txtTeam);
            txtCity = itemView.findViewById(R.id.txtCity);
        }
    }
}
