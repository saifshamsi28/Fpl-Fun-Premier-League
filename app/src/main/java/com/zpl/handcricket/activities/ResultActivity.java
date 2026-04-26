package com.zpl.handcricket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zpl.handcricket.R;
import com.zpl.handcricket.adapters.BallTrackingAdapter;
import com.zpl.handcricket.api.ApiClient;
import com.zpl.handcricket.models.BallDetail;
import com.zpl.handcricket.models.MatchDetail;
import com.zpl.handcricket.utils.AppState;
import com.zpl.handcricket.utils.MatchContext;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_MATCH_ID = "match_id";

    private TextView tvP1Name, tvP1Score, tvP2Name, tvP2Score, tvResult;
    private TextView labelInnings1, labelInnings2;
    private RecyclerView recyclerInnings1, recyclerInnings2;
    private TextView btnContinue;
    private View resultContent, scrollResult, shimmerPage;

    private BallTrackingAdapter adapter1, adapter2;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_result);

        tvP1Name      = findViewById(R.id.tvP1Name);
        tvP1Score     = findViewById(R.id.tvP1Score);
        tvP2Name      = findViewById(R.id.tvP2Name);
        tvP2Score     = findViewById(R.id.tvP2Score);
        tvResult      = findViewById(R.id.tvResult);
        labelInnings1  = findViewById(R.id.labelInnings1);
        labelInnings2  = findViewById(R.id.labelInnings2);
        recyclerInnings1 = findViewById(R.id.recyclerInnings1);
        recyclerInnings2 = findViewById(R.id.recyclerInnings2);
        btnContinue    = findViewById(R.id.btnContinue);
        resultContent  = findViewById(R.id.resultContent);
        scrollResult   = findViewById(R.id.scrollResult);
        shimmerPage    = findViewById(R.id.shimmerPage);
        ImageView btnClose = findViewById(R.id.btnClose);
        ImageView btnShare = findViewById(R.id.btnShare);

        adapter1 = new BallTrackingAdapter(new ArrayList<>());
        adapter2 = new BallTrackingAdapter(new ArrayList<>());

        recyclerInnings1.setLayoutManager(new LinearLayoutManager(this));
        recyclerInnings1.setAdapter(adapter1);
        recyclerInnings2.setLayoutManager(new LinearLayoutManager(this));
        recyclerInnings2.setAdapter(adapter2);

        btnContinue.setOnClickListener(v -> goHome());
        btnClose.setOnClickListener(v -> goHome());
        btnShare.setOnClickListener(v -> shareScore());

        String matchId = getIntent().getStringExtra(EXTRA_MATCH_ID);
        if (matchId != null) {
            loadFromApi(matchId);
        } else {
            shimmerPage.setVisibility(View.GONE);
            scrollResult.setVisibility(View.VISIBLE);
            loadFromSocketPayload();
        }
    }

    private void loadFromSocketPayload() {
        JsonObject end = MatchContext.get().lastMatchEnd;
        JsonObject mf  = MatchContext.get().matchFound;

        String youUserId = AppState.get().getUserId();
        String youName = "You", oppName = "Opponent";

        if (mf != null) {
            if (mf.has("you")) youName = mf.getAsJsonObject("you").get("username").getAsString();
            if (mf.has("opponent")) oppName = mf.getAsJsonObject("opponent").get("username").getAsString();
        }

        int p1Runs = 0, p2Runs = 0;
        String winnerId = null;
        int youRuns = 0, oppRuns = 0;

        if (end != null) {
            if (end.has("p1Runs")) p1Runs = end.get("p1Runs").getAsInt();
            if (end.has("p2Runs")) p2Runs = end.get("p2Runs").getAsInt();
            if (end.has("winnerId") && !end.get("winnerId").isJsonNull())
                winnerId = end.get("winnerId").getAsString();

            if (youUserId != null && end.has("player1Id") && end.has("player2Id")
                    && !end.get("player1Id").isJsonNull() && !end.get("player2Id").isJsonNull()) {
                String p1Id = end.get("player1Id").getAsString();
                String p2Id = end.get("player2Id").getAsString();
                if (youUserId.equals(p1Id))      { youRuns = p1Runs; oppRuns = p2Runs; }
                else if (youUserId.equals(p2Id)) { youRuns = p2Runs; oppRuns = p1Runs; }
                else                             { youRuns = p1Runs; oppRuns = p2Runs; }
            } else {
                youRuns = p1Runs; oppRuns = p2Runs;
            }
        }

        boolean youWon = youUserId != null && youUserId.equals(winnerId);
        boolean tie    = winnerId == null;

        renderHeader(youName, youRuns, oppName, oppRuns, youWon, tie);
        hideBallSections();
    }

    private void loadFromApi(String matchId) {
        scrollResult.setVisibility(View.GONE);
        shimmerPage.setVisibility(View.VISIBLE);

        ApiClient.get().matchDetail(matchId).enqueue(new Callback<MatchDetail>() {
            @Override
            public void onResponse(Call<MatchDetail> c, Response<MatchDetail> r) {
                shimmerPage.setVisibility(View.GONE);
                scrollResult.setVisibility(View.VISIBLE);

                if (!r.isSuccessful() || r.body() == null) {
                    hideBallSections();
                    return;
                }
                MatchDetail d = r.body();
                renderHeader(
                        d.myName != null ? d.myName : "You",
                        d.myRuns,
                        d.opponentName != null ? d.opponentName : "Opponent",
                        d.opponentRuns,
                        d.won, false);

                List<BallDetail> inn1 = new ArrayList<>();
                List<BallDetail> inn2 = new ArrayList<>();
                if (d.balls != null) {
                    for (BallDetail b : d.balls) {
                        if (b.innings == 1) inn1.add(b); else inn2.add(b);
                    }
                }
                adapter1.setBalls(inn1);
                adapter2.setBalls(inn2);
                showBallSections();
            }

            @Override
            public void onFailure(Call<MatchDetail> c, Throwable t) {
                shimmerPage.setVisibility(View.GONE);
                scrollResult.setVisibility(View.VISIBLE);
                hideBallSections();
            }
        });
    }

    private void showBallSections() {
        labelInnings1.setVisibility(View.VISIBLE);
        recyclerInnings1.setVisibility(View.VISIBLE);
        labelInnings2.setVisibility(View.VISIBLE);
        recyclerInnings2.setVisibility(View.VISIBLE);
    }

    private void renderHeader(String p1, int p1Runs, String p2, int p2Runs,
                              boolean youWon, boolean tie) {
        tvP1Name.setText(p1.toUpperCase());
        tvP1Score.setText(String.valueOf(p1Runs));
        tvP2Name.setText(p2.toUpperCase());
        tvP2Score.setText(String.valueOf(p2Runs));

        if (tie) {
            tvResult.setText("MATCH TIED");
            tvResult.setBackgroundResource(R.drawable.bg_tie_banner);
            tvResult.setTextColor(0xFFEDE9FE);
        } else if (youWon) {
            tvResult.setText("YOU WON \uD83C\uDFC6");
            tvResult.setBackgroundResource(R.drawable.bg_winner_banner);
            tvResult.setTextColor(0xFF1A0A00);
        } else {
            tvResult.setText("YOU LOST");
            tvResult.setBackgroundResource(R.drawable.bg_lost_banner);
            tvResult.setTextColor(0xFFCBD5E1);
        }

        animateEntrance(youWon, tie);
    }

    private void animateEntrance(boolean won, boolean tie) {
        if (resultContent == null) return;

        resultContent.setAlpha(0f);
        resultContent.setTranslationY(60f);
        resultContent.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(450)
                .setInterpolator(new DecelerateInterpolator(1.6f))
                .start();

        tvResult.setScaleX(0.6f);
        tvResult.setScaleY(0.6f);
        tvResult.setAlpha(0f);
        tvResult.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(500)
                .setStartDelay(180)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .start();
    }

    private void hideBallSections() {
        labelInnings1.setVisibility(View.GONE);
        recyclerInnings1.setVisibility(View.GONE);
        labelInnings2.setVisibility(View.GONE);
        recyclerInnings2.setVisibility(View.GONE);
    }

    private void shareScore() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT,
                tvP1Name.getText() + " scored " + tvP1Score.getText() + " runs"
                        + " vs " + tvP2Name.getText() + " on ZPL Hand Cricket!");
        startActivity(Intent.createChooser(i, "Share"));
    }

    private void goHome() {
        MatchContext.get().clear();
        startActivity(new Intent(this, HomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    @Override public void onBackPressed() { goHome(); }
}
