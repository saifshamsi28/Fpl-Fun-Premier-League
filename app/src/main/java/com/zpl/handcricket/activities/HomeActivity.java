package com.zpl.handcricket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zpl.handcricket.R;
import com.zpl.handcricket.adapters.RecentMatchAdapter;
import com.zpl.handcricket.api.ApiClient;
import com.zpl.handcricket.models.MatchSummary;
import com.zpl.handcricket.models.User;
import com.zpl.handcricket.utils.AppState;

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
    private View emptyRecent, layoutHero, shimmerHero;

    private RecentMatchAdapter adapter;

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
        layoutHero      = findViewById(R.id.layoutHero);
        shimmerHero     = findViewById(R.id.shimmerHero);

        adapter = new RecentMatchAdapter(new ArrayList<>(), m -> openResult(m.id));
        recyclerRecent.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecent.setAdapter(adapter);

        btnPlayRanked.setOnClickListener(v ->
                startActivity(new Intent(this, MatchmakingActivity.class)));
        btnPlayFriendly.setOnClickListener(v ->
                startActivity(new Intent(this, FriendMatchActivity.class)));
        btnViewAll.setOnClickListener(v ->
                startActivity(new Intent(this, MatchHistoryActivity.class)));

        txtRank.setOnClickListener(v ->
            startActivity(new Intent(this, LeaderboardActivity.class)));

        txtAvatar.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfile();
        loadRecent();
    }

    private void refreshProfile() {
        layoutHero.setVisibility(View.GONE);
        shimmerHero.setVisibility(View.VISIBLE);

        ApiClient.get().me().enqueue(new Callback<User>() {
            @Override public void onResponse(Call<User> c, Response<User> r) {
                shimmerHero.setVisibility(View.GONE);
                layoutHero.setVisibility(View.VISIBLE);
                if (!r.isSuccessful() || r.body() == null) return;
                User u = r.body();
                AppState.get().setCachedUser(u);
                bind(u);
            }
            @Override public void onFailure(Call<User> c, Throwable t) {
                shimmerHero.setVisibility(View.GONE);
                layoutHero.setVisibility(View.VISIBLE);
            }
        });
    }

    private void bind(User u) {
        String name = u.username != null ? u.username : "Player";
        txtUserName.setText(name);
        txtAvatar.setText(name.substring(0, 1).toUpperCase());
        
        setTeamName(u.teamId);

        if (u.rank != null && u.rank > 0) {
            txtRank.setText("#" + u.rank);
        } else {
            txtRank.setText("--");
        }

        txtStatPlayed.setText(String.valueOf(u.matchesPlayed));
        txtStatWon.setText(String.valueOf(u.matchesWon));
        int rate = u.matchesPlayed > 0
                ? (int) Math.round(u.matchesWon * 100.0 / u.matchesPlayed) : 0;
        txtStatWinRate.setText(rate + "%");

        btnPlayRanked.setAlpha(1f);
        btnPlayRanked.setClickable(true);
        btnPlayRanked.setText("PLAY RANKED MATCH");
    }

    private void setTeamName(Integer teamId) {
        if (teamId == null) {
            txtTeam.setText("NO TEAM");
            return;
        }
        String code = switch (teamId) {
            case 1 -> "MI";
            case 2 -> "SRH";
            case 3 -> "DC";
            case 4 -> "KKR";
            case 5 -> "RCB";
            case 6 -> "CSK";
            case 7 -> "LSG";
            case 8 -> "GT";
            case 9 -> "PBKS";
            case 10 -> "RR";
            default -> "T" + teamId;
        };
        txtTeam.setText("TEAM : " + code);
    }

    private void loadRecent() {
        adapter.setLoading(true);
        emptyRecent.setVisibility(View.GONE);
        recyclerRecent.setVisibility(View.VISIBLE);

        ApiClient.get().recentMatches(3).enqueue(new Callback<List<MatchSummary>>() {
            @Override
            public void onResponse(Call<List<MatchSummary>> c, Response<List<MatchSummary>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    List<MatchSummary> body = r.body();
                    adapter.setItems(body);
                    boolean empty = body.isEmpty();
                    emptyRecent.setVisibility(empty ? View.VISIBLE : View.GONE);
                    recyclerRecent.setVisibility(empty ? View.GONE : View.VISIBLE);
                } else {
                    adapter.setLoading(false);
                    emptyRecent.setVisibility(View.VISIBLE);
                    recyclerRecent.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Call<List<MatchSummary>> c, Throwable t) {
                adapter.setLoading(false);
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
