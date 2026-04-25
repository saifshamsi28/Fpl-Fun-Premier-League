package com.zpl.handcricket.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zpl.handcricket.R;
import com.zpl.handcricket.models.MatchSummary;
import com.zpl.handcricket.utils.TimeAgo;

import java.util.List;

public class RecentMatchAdapter extends RecyclerView.Adapter<RecentMatchAdapter.VH> {

    public interface OnItemClick { void onClick(MatchSummary m); }

    private final List<MatchSummary> items;
    private final OnItemClick listener;

    public RecentMatchAdapter(List<MatchSummary> items, OnItemClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_match, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MatchSummary m = items.get(position);
        h.myName.setText(m.myName != null ? m.myName : "You");
        h.myTeam.setText(m.myTeam != null ? m.myTeam : "");
        h.opp.setText(m.opponentName != null ? m.opponentName : "Opponent");
        h.score.setText(m.myRuns + " - " + m.opponentRuns);
        h.when.setText(TimeAgo.format(m.playedAt));

        if (m.won) {
            h.badge.setText("WON");
            h.badge.setBackgroundResource(R.drawable.bg_badge_won);
            h.badge.setTextColor(0xFF1B5E20);
        } else {
            h.badge.setText("LOST");
            h.badge.setBackgroundResource(R.drawable.bg_badge_lost);
            h.badge.setTextColor(0xFFB71C1C);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(m);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView myName, myTeam, opp, score, when, badge;
        VH(View v) {
            super(v);
            myName = v.findViewById(R.id.txtMyName);
            myTeam = v.findViewById(R.id.txtMyTeam);
            opp    = v.findViewById(R.id.txtOpponentName);
            score  = v.findViewById(R.id.txtScore);
            when   = v.findViewById(R.id.txtWhen);
            badge  = v.findViewById(R.id.txtResultBadge);
        }
    }
}
