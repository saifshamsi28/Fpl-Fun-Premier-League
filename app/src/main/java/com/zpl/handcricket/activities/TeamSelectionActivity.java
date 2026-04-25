package com.zpl.handcricket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zpl.handcricket.R;
import com.zpl.handcricket.adapters.TeamAdapter;
import com.zpl.handcricket.api.ApiClient;
import com.zpl.handcricket.models.Team;
import com.zpl.handcricket.utils.AppState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamSelectionActivity extends AppCompatActivity {

    private TeamAdapter adapter;
    private final List<Team> teams = new ArrayList<>();
    private Integer picked = null;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_team_selection);

        ImageView back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setEnabled(false);
        btnConfirm.setAlpha(0.5f);
        btnConfirm.setOnClickListener(v -> confirm());

        RecyclerView rv = findViewById(R.id.teamGrid);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new TeamAdapter(teams, t -> {
            picked = t.id;
            btnConfirm.setEnabled(true);
            btnConfirm.setAlpha(1f);
        });
        rv.setAdapter(adapter);

        loadTeams();
    }

    private void loadTeams() {
        ApiClient.get().teams().enqueue(new Callback<List<Team>>() {
            @Override public void onResponse(Call<List<Team>> c, Response<List<Team>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    teams.clear();
                    teams.addAll(r.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(TeamSelectionActivity.this,
                            "Failed to load teams", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<Team>> c, Throwable t) {
                Toast.makeText(TeamSelectionActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirm() {
        if (picked == null) return;
        Map<String, Integer> body = new HashMap<>();
        body.put("teamId", picked);
        btnConfirm.setEnabled(false);
        ApiClient.get().pickTeam(body).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {
                btnConfirm.setEnabled(true);
                if (r.isSuccessful()) {
                    AppState.get().setTeamId(picked);
                    startActivity(new Intent(TeamSelectionActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(TeamSelectionActivity.this,
                            "Could not save team", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> c, Throwable t) {
                btnConfirm.setEnabled(true);
                Toast.makeText(TeamSelectionActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
