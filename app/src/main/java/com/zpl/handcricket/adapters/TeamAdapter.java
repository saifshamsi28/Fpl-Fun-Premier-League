package com.zpl.handcricket.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zpl.handcricket.R;
import com.zpl.handcricket.models.Team;
import com.zpl.handcricket.utils.TeamAssets;

import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.VH> {

    public interface OnSelect { void onPick(Team t); }

    private final List<Team> data;
    private final OnSelect cb;
    private int selectedId = -1;

    public TeamAdapter(List<Team> data, OnSelect cb) {
        this.data = data;
        this.cb = cb;
    }

    public void setSelected(int teamId) {
        this.selectedId = teamId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Team t = data.get(position);
        h.imgTeam.setImageResource(TeamAssets.imageFor(t.code, t.name));

        boolean selected = t.id == selectedId;
        h.selectionOverlay.setVisibility(selected ? View.VISIBLE : View.GONE);
        float targetScale = selected ? 1.05f : 1f;
        h.card.setScaleX(targetScale);
        h.card.setScaleY(targetScale);
        h.card.setAlpha(selected ? 1f : 0.96f);

        h.itemView.setOnClickListener(v -> {
            if (selectedId == t.id) {
                cb.onPick(t);
                return;
            }
            selectedId = t.id;
            notifyDataSetChanged();
            playSelectAnimation(h.card);
            cb.onPick(t);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    private void playSelectAnimation(View card) {
        ObjectAnimator sx = ObjectAnimator.ofFloat(card, View.SCALE_X, 1f, 1.08f, 1.05f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(card, View.SCALE_Y, 1f, 1.08f, 1.05f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(sx, sy);
        set.setDuration(220);
        set.setInterpolator(new OvershootInterpolator(1.1f));
        set.start();
    }

    static class VH extends RecyclerView.ViewHolder {
        View card;
        ImageView imgTeam;
        View selectionOverlay;

        VH(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.teamCard);
            imgTeam = itemView.findViewById(R.id.imgTeam);
            selectionOverlay = itemView.findViewById(R.id.selectionOverlay);
        }
    }
}
