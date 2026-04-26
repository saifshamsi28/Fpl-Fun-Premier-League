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

import java.util.ArrayList;
import java.util.List;

public class RecentMatchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SHIMMER = 0;
    private static final int TYPE_DATA = 1;

    public interface OnItemClick { void onClick(MatchSummary m); }

    private List<MatchSummary> items = new ArrayList<>();
    private final OnItemClick listener;
    private boolean isLoading = true;

    public RecentMatchAdapter(List<MatchSummary> items, OnItemClick listener) {
        this.items = items != null ? items : new ArrayList<>();
        this.listener = listener;
    }

    public void setItems(List<MatchSummary> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        this.isLoading = false;
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isLoading ? TYPE_SHIMMER : TYPE_DATA;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SHIMMER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.shimmer_item_recent_match, parent, false);
            return new ShimmerVH(v);
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_match, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VH && !items.isEmpty()) {
            VH h = (VH) holder;
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
    }

    @Override 
    public int getItemCount() { 
        return isLoading ? 3 : items.size(); 
    }

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

    static class ShimmerVH extends RecyclerView.ViewHolder {
        ShimmerVH(View v) { super(v); }
    }
}
