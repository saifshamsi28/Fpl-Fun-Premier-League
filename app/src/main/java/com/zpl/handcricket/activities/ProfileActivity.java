package com.zpl.handcricket.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zpl.handcricket.R;
import com.zpl.handcricket.api.ApiClient;
import com.zpl.handcricket.models.User;
import com.zpl.handcricket.utils.AppState;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtAvatar, txtName, txtUsername,
            txtDetailTeam, txtRank, txtPlayed, txtWon, txtRuns, txtWinRate;
    private EditText txtFullName, txtEmail, txtCity,
            txtFavoritePlayer;
    private ImageView imgTeamBg, btnBack;
    private View btnLogout;
    private Button btnUpdateProfile;
    private boolean savingProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txtAvatar = findViewById(R.id.txtProfileAvatar);
        txtName = findViewById(R.id.txtProfileName);
        txtUsername = findViewById(R.id.txtDetailUsername);
        txtFullName = findViewById(R.id.txtDetailFullName);
        txtEmail = findViewById(R.id.txtDetailEmail);
        txtCity = findViewById(R.id.txtDetailCity);
        txtFavoritePlayer = findViewById(R.id.txtDetailFavoritePlayer);
        txtDetailTeam = findViewById(R.id.txtDetailTeam);
        txtRank = findViewById(R.id.txtDetailRank);
        txtPlayed = findViewById(R.id.txtDetailPlayed);
        txtWon = findViewById(R.id.txtDetailWon);
        txtRuns = findViewById(R.id.txtDetailRuns);
        txtWinRate = findViewById(R.id.txtDetailWinRate);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        imgTeamBg = findViewById(R.id.imgTeamBg);
        btnBack = findViewById(R.id.btnProfileBack);
        btnLogout = findViewById(R.id.btnLogout);

        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        btnUpdateProfile.setOnClickListener(v -> updateProfile());

        loadUser();
    }

    private void loadUser() {
        User u = AppState.get().getCachedUser();
        if (u == null) {
            finish();
            return;
        }

        String username = u.username != null ? u.username : "Player";
        String fullName = (u.fullName != null && !u.fullName.trim().isEmpty()) ? u.fullName : username;

        String avatarSource = fullName.isEmpty() ? username : fullName;
        txtAvatar.setText(avatarSource.substring(0, 1).toUpperCase());
        txtName.setText(fullName);
        txtUsername.setText(username);
        txtFullName.setText(fullName);
        txtEmail.setText(valueOrEmpty(u.email));
        txtCity.setText(valueOrEmpty(u.city));
        txtFavoritePlayer.setText(valueOrEmpty(u.favoritePlayer));
        
        String teamName = u.teamId != null ? getFullTeamName(u.teamId) : "NO TEAM";
        txtDetailTeam.setText(teamName);

        txtRank.setText((u.rank != null && u.rank > 0) ? ("#" + u.rank) : "--");
        txtPlayed.setText(String.valueOf(u.matchesPlayed));
        txtWon.setText(String.valueOf(u.matchesWon));
        txtRuns.setText(String.valueOf(u.totalRuns));
        int winRate = u.matchesPlayed > 0
                ? (int) Math.round((u.matchesWon * 100.0) / u.matchesPlayed)
                : 0;
        txtWinRate.setText(winRate + "%");

        if (u.teamId != null) {
            setTeamSpecifics(u.teamId);
        }
    }

    private String valueOrEmpty(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        return value;
    }

    private void updateProfile() {
        if (savingProfile) return;

        String fullName = txtFullName.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String city = txtCity.getText().toString().trim();
        String favoritePlayer = txtFavoritePlayer.getText().toString().trim();

        if (fullName.length() < 2) {
            Toast.makeText(this, "Full name must be at least 2 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (city.length() < 2) {
            Toast.makeText(this, "City must be at least 2 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("fullName", fullName);
        body.put("email", email);
        body.put("city", city);
        body.put("favoritePlayer", favoritePlayer);

        setSavingProfile(true);
        ApiClient.get().updateProfile(body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                setSavingProfile(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ProfileActivity.this, parseApiError(response), Toast.LENGTH_SHORT).show();
                    return;
                }

                User updated = response.body();
                AppState.get().setCachedUser(updated);
                loadUser();
                Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                setSavingProfile(false);
                Toast.makeText(ProfileActivity.this, "Unable to update profile. Check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String parseApiError(Response<?> response) {
        try {
            if (response.errorBody() == null) return "Unable to update profile";
            String raw = response.errorBody().string();
            if (raw == null || raw.trim().isEmpty()) return "Unable to update profile";
            JSONObject json = new JSONObject(raw);
            String error = json.optString("error");
            return (error == null || error.trim().isEmpty()) ? "Unable to update profile" : error;
        } catch (Exception ignored) {
            return "Unable to update profile";
        }
    }

    private void setSavingProfile(boolean saving) {
        savingProfile = saving;
        btnUpdateProfile.setEnabled(!saving);
        btnUpdateProfile.setText(saving ? "Updating..." : "Update Profile");
        btnUpdateProfile.setAlpha(saving ? 0.6f : 1f);
    }

    private String getFullTeamName(int teamId) {
        switch (teamId) {
            case 1: return "Mumbai Indians";
            case 2: return "Sunrisers Hyderabad";
            case 3: return "Delhi Capitals";
            case 4: return "Kolkata Knight Riders";
            case 5: return "Royal Challengers Bangalore";
            case 6: return "Chennai Super Kings";
            case 7: return "Lucknow Super Giants";
            case 8: return "Gujarat Titans";
            case 9: return "Punjab Kings";
            case 10: return "Rajasthan Royals";
            default: return "Team #" + teamId;
        }
    }

    private void setTeamSpecifics(int teamId) {
        int resId = R.drawable.bg_stadium_gradient;
        switch (teamId) {
            case 1: resId = R.drawable.team_mi; break;
            case 2: resId = R.drawable.team_srh; break;
            case 3: resId = R.drawable.team_dc; break;
            case 4: resId = R.drawable.team_kkr; break;
            case 5: resId = R.drawable.team_rcb; break;
            case 6: resId = R.drawable.team_csk; break;
            case 7: resId = R.drawable.team_lsg; break;
            case 8: resId = R.drawable.team_gt; break;
            case 9: resId = R.drawable.team_pbks; break;
            case 10: resId = R.drawable.team_rr; break;
        }
        imgTeamBg.setImageResource(resId);
    }

    private void showLogoutConfirmation() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_confirm_logout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView btnCancel = dialog.findViewById(R.id.btnCancel);
        TextView btnLogoutConfirm = dialog.findViewById(R.id.btnConfirmLogout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnLogoutConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            performLogout();
        });

        dialog.show();
    }

    private void performLogout() {
        AppState.get().clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}
