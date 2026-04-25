package com.zpl.handcricket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

/**
 * Post-match summary. Can be launched either:
 *  (a) Right after a live match via MatchContext.lastMatchEnd (default flow), or
 *  (b) From history with EXTRA_MATCH_ID -> fetches full MatchDetail with ball-by-ball.
 */
public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_MATCH_ID = "match_id";

    private TextView tvP1Name, tvP1Score, tvP2Name, tvP2Score, tvResult;
    private TextView tvFinalP1Name, tvFinalP1Runs, tvFinalP2Name, tvFinalP2Runs;
    private TextView labelInnings1, labelInnings2;
    private RecyclerView recyclerInnings1, recyclerInnings2;
    private TextView btnContinue;

    private final List<BallDetail> inn1 = new ArrayList<>();
    private final List<BallDetail> inn2 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_result);

        tvP1Name = findViewById(R.id.tvP1Name);
        tvP1Score= findViewById(R.id.tvP1Score);
        tvP2Name = findViewById(R.id.tvP2Name);
        tvP2Score= findViewById(R.id.tvP2Score);
        tvResult = findViewById(R.id.tvResult);
        tvFinalP1Name = findViewById(R.id.tvFinalP1Name);
        tvFinalP1Runs = findViewById(R.id.tvFinalP1Runs);
        tvFinalP2Name = findViewById(R.id.tvFinalP2Name);
        tvFinalP2Runs = findViewById(R.id.tvFinalP2Runs);
        labelInnings1 = findViewById(R.id.labelInnings1);
        labelInnings2 = findViewById(R.id.labelInnings2);
        recyclerInnings1 = findViewById(R.id.recyclerInnings1);
        recyclerInnings2 = findViewById(R.id.recyclerInnings2);
        btnContinue = findViewById(R.id.btnContinue);
        ImageView btnClose = findViewById(R.id.btnClose);
        ImageView btnShare = findViewById(R.id.btnShare);

        recyclerInnings1.setLayoutManager(new LinearLayoutManager(this));
        recyclerInnings1.setAdapter(new BallTrackingAdapter(inn1));
        recyclerInnings2.setLayoutManager(new LinearLayoutManager(this));
        recyclerInnings2.setAdapter(new BallTrackingAdapter(inn2));

        btnContinue.setOnClickListener(v -> goHome());
        btnClose.setOnClickListener(v -> goHome());
        btnShare.setOnClickListener(v -> shareScore());

        String matchId = getIntent().getStringExtra(EXTRA_MATCH_ID);
        if (matchId != null) {
            loadFromApi(matchId);
        } else {
            loadFromSocketPayload();
        }
    }

    private void loadFromSocketPayload() {
        JsonObject end = MatchContext.get().lastMatchEnd;
        JsonObject mf  = MatchContext.get().matchFound;

        String youUserId = AppState.get().getUserId();
        String youName = "You", oppName = "Opponent";

        if (mf != null) {
            youName = mf.getAsJsonObject("you").get("username").getAsString();
            oppName = mf.getAsJsonObject("opponent").get("username").getAsString();
        }
        int p1Runs = 0, p2Runs = 0;
        String winnerId = null;
        int youRuns = 0, oppRuns = 0;
        if (end != null) {
            if (end.has("p1Runs")) p1Runs = end.get("p1Runs").getAsInt();
            if (end.has("p2Runs")) p2Runs = end.get("p2Runs").getAsInt();
            if (end.has("winnerId") && !end.get("winnerId").isJsonNull())
                winnerId = end.get("winnerId").getAsString();

            // Map p1/p2 scores to "you/opponent" deterministically when ids are provided.
            if (youUserId != null && end.has("player1Id") && end.has("player2Id")
                    && !end.get("player1Id").isJsonNull() && !end.get("player2Id").isJsonNull()) {
                String player1Id = end.get("player1Id").getAsString();
                String player2Id = end.get("player2Id").getAsString();
                if (youUserId.equals(player1Id)) {
                    youRuns = p1Runs;
                    oppRuns = p2Runs;
                } else if (youUserId.equals(player2Id)) {
                    youRuns = p2Runs;
                    oppRuns = p1Runs;
                } else {
                    // Unexpected id mismatch; fall back to legacy ordering.
                    youRuns = p1Runs;
                    oppRuns = p2Runs;
                }
            } else {
                // Legacy payload fallback (older backend): assumes p1 is local user.
                youRuns = p1Runs;
                oppRuns = p2Runs;
            }
        }

        boolean youWon = youUserId != null && youUserId.equals(winnerId);
        boolean tie = winnerId == null;

        renderHeader(youName, youRuns, oppName, oppRuns, youWon, tie);

        // Ball-by-ball if server included it
        if (end != null && end.has("balls") && end.get("balls").isJsonArray()) {
            parseBalls(end.getAsJsonArray("balls"), youName, oppName);
        } else {
            hideBallSections();
        }
    }

    private void loadFromApi(String matchId) {
        ApiClient.get().matchDetail(matchId).enqueue(new Callback<MatchDetail>() {
            @Override
            public void onResponse(Call<MatchDetail> c, Response<MatchDetail> r) {
                if (!r.isSuccessful() || r.body() == null) { hideBallSections(); return; }
                MatchDetail d = r.body();
                renderHeader(
                        d.myName != null ? d.myName : "You",
                        d.myRuns,
                        d.opponentName != null ? d.opponentName : "Opponent",
                        d.opponentRuns,
                        d.won,
                        false);

                inn1.clear(); inn2.clear();
                if (d.balls != null) {
                    for (BallDetail b : d.balls) {
                        if (b.innings == 1) inn1.add(b); else inn2.add(b);
                    }
                }
                labelInnings1.setVisibility(inn1.isEmpty() ? View.GONE : View.VISIBLE);
                recyclerInnings1.setVisibility(inn1.isEmpty() ? View.GONE : View.VISIBLE);
                labelInnings2.setVisibility(inn2.isEmpty() ? View.GONE : View.VISIBLE);
                recyclerInnings2.setVisibility(inn2.isEmpty() ? View.GONE : View.VISIBLE);
                recyclerInnings1.getAdapter().notifyDataSetChanged();
                recyclerInnings2.getAdapter().notifyDataSetChanged();
            }
            @Override public void onFailure(Call<MatchDetail> c, Throwable t) { hideBallSections(); }
        });
    }

    private void renderHeader(String p1, int p1Runs, String p2, int p2Runs,
                              boolean youWon, boolean tie) {
        tvP1Name.setText(p1);
        tvP1Score.setText(p1Runs + " runs");
        tvP2Name.setText(p2);
        tvP2Score.setText(p2Runs + " runs");
        tvFinalP1Name.setText(p1.toUpperCase());
        tvFinalP1Runs.setText(p1Runs + " runs");
        tvFinalP2Name.setText(p2.toUpperCase());
        tvFinalP2Runs.setText(p2Runs + " runs");

        if (tie)         tvResult.setText("MATCH TIED");
        else if (youWon) tvResult.setText("YOU WON");
        else             tvResult.setText("YOU LOST");
    }

    private void parseBalls(JsonArray arr, String youName, String oppName) {
        inn1.clear(); inn2.clear();
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            BallDetail b = new BallDetail();
            b.innings    = optInt(o, "innings", 1);
            b.ballNo     = optInt(o, "ballNo", 0);
            b.batterPick = optInt(o, "batterPick", 0);
            b.bowlerPick = optInt(o, "bowlerPick", 0);
            b.runs       = optInt(o, "runs", 0);
            b.wicket     = o.has("wicket") && o.get("wicket").getAsBoolean();
            b.batterName = optStr(o, "batterName",
                    b.innings == 1 ? youName : oppName);
            b.bowlerName = optStr(o, "bowlerName",
                    b.innings == 1 ? oppName : youName);
            if (b.innings == 1) inn1.add(b); else inn2.add(b);
        }
        labelInnings1.setVisibility(inn1.isEmpty() ? View.GONE : View.VISIBLE);
        recyclerInnings1.setVisibility(inn1.isEmpty() ? View.GONE : View.VISIBLE);
        labelInnings2.setVisibility(inn2.isEmpty() ? View.GONE : View.VISIBLE);
        recyclerInnings2.setVisibility(inn2.isEmpty() ? View.GONE : View.VISIBLE);
        recyclerInnings1.getAdapter().notifyDataSetChanged();
        recyclerInnings2.getAdapter().notifyDataSetChanged();
    }

    private void hideBallSections() {
        labelInnings1.setVisibility(View.GONE);
        recyclerInnings1.setVisibility(View.GONE);
        labelInnings2.setVisibility(View.GONE);
        recyclerInnings2.setVisibility(View.GONE);
    }

    private int optInt(JsonObject o, String k, int d) {
        try { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : d; }
        catch (Exception e) { return d; }
    }
    private String optStr(JsonObject o, String k, String d) {
        try { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : d; }
        catch (Exception e) { return d; }
    }

    private void shareScore() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT,
                tvP1Name.getText() + " scored " + tvP1Score.getText()
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
