package com.zpl.handcricket.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zpl.handcricket.R;
import com.zpl.handcricket.models.BallDetail;

import java.util.List;

public class BallTrackingAdapter extends RecyclerView.Adapter<BallTrackingAdapter.VH> {

    private final List<BallDetail> balls;

    public BallTrackingAdapter(List<BallDetail> balls) { this.balls = balls; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ball_tracking, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
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

    @Override public int getItemCount() { return balls.size(); }

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
}
