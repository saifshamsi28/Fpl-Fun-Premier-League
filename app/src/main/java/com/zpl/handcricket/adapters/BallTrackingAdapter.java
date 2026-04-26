package com.zpl.handcricket.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zpl.handcricket.R;
import com.zpl.handcricket.models.BallDetail;

import java.util.ArrayList;
import java.util.List;

public class BallTrackingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SHIMMER = 0;
    private static final int TYPE_DATA = 1;

    private List<BallDetail> balls = new ArrayList<>();
    private boolean isLoading = true;

    public BallTrackingAdapter(List<BallDetail> balls) {
        this.balls = balls != null ? balls : new ArrayList<>();
    }

    public void setBalls(List<BallDetail> newBalls) {
        this.balls = newBalls != null ? newBalls : new ArrayList<>();
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
                    .inflate(R.layout.shimmer_item_ball_tracking, parent, false);
            return new ShimmerVH(v);
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ball_tracking, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VH && !balls.isEmpty()) {
            VH h = (VH) holder;
            BallDetail b = balls.get(position);
            h.ballNo.setText("B" + b.ballNo);
            h.pickBat.setText(String.valueOf(b.batterPick));
            h.pickBowl.setText(String.valueOf(b.bowlerPick));
            h.batName.setText(b.batterName != null ? b.batterName : "—");
            h.bowlName.setText(b.bowlerName != null ? b.bowlerName : "—");

            if (b.wicket) {
                h.outcome.setText("OUT");
                h.outcome.setBackgroundResource(R.drawable.bg_wicket_badge);
            } else {
                h.outcome.setText("+" + b.runs);
                h.outcome.setBackgroundResource(R.drawable.bg_pick_blue);
            }
        }
    }

    @Override 
    public int getItemCount() { 
        return isLoading ? 10 : balls.size(); 
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView ballNo, pickBat, pickBowl, batName, bowlName, outcome;
        VH(View v) {
            super(v);
            ballNo   = v.findViewById(R.id.txtBallNo);
            pickBat  = v.findViewById(R.id.pickBatter);
            pickBowl = v.findViewById(R.id.pickBowler);
            batName  = v.findViewById(R.id.txtBatterName);
            bowlName = v.findViewById(R.id.txtBowlerName);
            outcome  = v.findViewById(R.id.txtOutcome);
        }
    }

    static class ShimmerVH extends RecyclerView.ViewHolder {
        ShimmerVH(View v) { super(v); }
    }
}
