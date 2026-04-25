package com.zpl.handcricket.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zpl.handcricket.R;
import com.zpl.handcricket.models.Team;

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
        h.tvName.setText(t.name);
        h.tvLandmark.setText(t.landmark);
        int color;
        try { color = Color.parseColor(t.primaryColor); }
        catch (Exception e) { color = Color.parseColor("#1D4ED8"); }
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(color);
        if (t.id == selectedId) {
            gd.setStroke(6, Color.parseColor("#F59E0B"));
        }
        h.colorDot.setBackground(gd);
        h.itemView.setOnClickListener(v -> {
            selectedId = t.id;
            notifyDataSetChanged();
            cb.onPick(t);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvLandmark;
        View colorDot;
        VH(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLandmark = itemView.findViewById(R.id.tvLandmark);
            colorDot = itemView.findViewById(R.id.colorDot);
        }
    }
}
