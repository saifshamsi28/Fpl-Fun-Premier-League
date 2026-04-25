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

public class MatchHistoryAdapter extends RecyclerView.Adapter<MatchHistoryAdapter.VH> {

    public interface OnItemClick { void onClick(MatchSummary m); }

    private final List<MatchSummary> items;
    private final OnItemClick listener;

    public MatchHistoryAdapter(List<MatchSummary> items, OnItemClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MatchSummary m = items.get(position);

        String myName = m.myName != null ? m.myName : "You";
        String oppName = m.opponentName != null ? m.opponentName : "Opponent";

        h.myName.setText(myName);
        h.oppName.setText(oppName);
        h.myRuns.setText(m.myRuns + " runs");
        h.oppRuns.setText(m.opponentRuns + " runs");
        h.avatarMe.setText(initial(myName));
        h.avatarOpp.setText(initial(oppName));
        h.when.setText(TimeAgo.format(m.playedAt));
        h.mode.setText(m.mode != null ? m.mode : "RANKED");

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

    private static String initial(String name) {
        if (name == null || name.isEmpty()) return "?";
        return name.substring(0, 1).toUpperCase();
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView avatarMe, avatarOpp, myName, oppName, myRuns, oppRuns, badge, mode, when;
        VH(View v) {
            super(v);
            avatarMe  = v.findViewById(R.id.avatarMe);
            avatarOpp = v.findViewById(R.id.avatarOpponent);
            myName    = v.findViewById(R.id.txtMyName);
            oppName   = v.findViewById(R.id.txtOpponentName);
            myRuns    = v.findViewById(R.id.txtMyRuns);
            oppRuns   = v.findViewById(R.id.txtOpponentRuns);
            badge     = v.findViewById(R.id.badgeResult);
            mode      = v.findViewById(R.id.txtMode);
            when      = v.findViewById(R.id.txtWhen);
        }
    }
}
