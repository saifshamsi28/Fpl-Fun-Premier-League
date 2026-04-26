package com.zpl.handcricket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zpl.handcricket.R;
import com.zpl.handcricket.adapters.RecentMatchAdapter;
import com.zpl.handcricket.adapters.SkeletonRecentMatchAdapter;
import com.zpl.handcricket.api.ApiClient;
import com.zpl.handcricket.models.MatchSummary;
import com.zpl.handcricket.models.User;
import com.zpl.handcricket.utils.AppState;
import com.zpl.handcricket.utils.SkeletonManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView txtUserName, txtTeam, txtAvatar, txtRank;
    private TextView txtStatPlayed, txtStatWon, txtStatWinRate;
    private TextView btnPlayRanked, btnPlayFriendly, btnViewAll;
    private RecyclerView recyclerRecent;
    private View emptyRecent;
    private ViewGroup rootContainer;

    private final List<MatchSummary> recent = new ArrayList<>();
    private RecentMatchAdapter adapter;
    private SkeletonRecentMatchAdapter skeletonAdapter;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_home);

        txtUserName     = findViewById(R.id.txtUserName);
        txtTeam         = findViewById(R.id.txtTeam);
        txtAvatar       = findViewById(R.id.txtAvatar);
        txtRank         = findViewById(R.id.txtRank);
        txtStatPlayed   = findViewById(R.id.txtStatPlayed);
        txtStatWon      = findViewById(R.id.txtStatWon);
        txtStatWinRate  = findViewById(R.id.txtStatWinRate);
        btnPlayRanked   = findViewById(R.id.btnPlayRanked);
        btnPlayFriendly = findViewById(R.id.btnPlayFriendly);
        btnViewAll      = findViewById(R.id.btnViewAll);
        recyclerRecent  = findViewById(R.id.recyclerRecent);
        emptyRecent     = findViewById(R.id.emptyRecent);

        adapter = new RecentMatchAdapter(recent, m -> openResult(m.id));
        skeletonAdapter = new SkeletonRecentMatchAdapter(3);
        recyclerRecent.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecent.setAdapter(skeletonAdapter);

        btnPlayRanked.setOnClickListener(v ->
                startActivity(new Intent(this, MatchmakingActivity.class)));
        btnPlayFriendly.setOnClickListener(v ->
                startActivity(new Intent(this, FriendMatchActivity.class)));
        btnViewAll.setOnClickListener(v ->
                startActivity(new Intent(this, MatchHistoryActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfile();
        loadRecent();
    }

    private void refreshProfile() {
        ApiClient.get().me().enqueue(new Callback<User>() {
            @Override public void onResponse(Call<User> c, Response<User> r) {
                if (!r.isSuccessful() || r.body() == null) return;
                User u = r.body();
                AppState.get().setCachedUser(u);
                bind(u);
            }
            @Override public void onFailure(Call<User> c, Throwable t) { }
        });
    }

    private void bind(User u) {
        String name = u.username != null ? u.username : "Player";
        txtUserName.setText(name);
        txtAvatar.setText(name.substring(0, 1).toUpperCase());
        txtTeam.setText(u.teamId != null ? ("TEAM #" + u.teamId) : "NO TEAM");
        txtRank.setText("#" + Math.max(1, 2000 - u.matchesWon * 15));

        txtStatPlayed.setText(String.valueOf(u.matchesPlayed));
        txtStatWon.setText(String.valueOf(u.matchesWon));
        int rate = u.matchesPlayed > 0
                ? (int) Math.round(u.matchesWon * 100.0 / u.matchesPlayed) : 0;
        txtStatWinRate.setText(rate + "%");

        // Ranked is always available (same as friendly mode).
        btnPlayRanked.setAlpha(1f);
        btnPlayRanked.setClickable(true);
        btnPlayRanked.setText("PLAY RANKED MATCH");
    }

    private void loadRecent() {
        ApiClient.get().recentMatches(3).enqueue(new Callback<List<MatchSummary>>() {
            @Override
            public void onResponse(Call<List<MatchSummary>> c, Response<List<MatchSummary>> r) {
                recent.clear();
                if (r.isSuccessful() && r.body() != null) recent.addAll(r.body());
                
                // Switch from skeleton to real data
                if (recent.isEmpty()) {
                    emptyRecent.setVisibility(View.VISIBLE);
                    recyclerRecent.setVisibility(View.GONE);
                } else {
                    emptyRecent.setVisibility(View.GONE);
                    recyclerRecent.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<MatchSummary>> c, Throwable t) {
                emptyRecent.setVisibility(View.VISIBLE);
                recyclerRecent.setVisibility(View.GONE);
            }
        });
    }

    private void openResult(String matchId) {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra(ResultActivity.EXTRA_MATCH_ID, matchId);
        startActivity(i);
    }
}
