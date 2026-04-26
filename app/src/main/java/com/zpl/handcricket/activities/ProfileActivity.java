package com.zpl.handcricket.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zpl.handcricket.R;
import com.zpl.handcricket.models.User;
import com.zpl.handcricket.utils.AppState;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtAvatar, txtName, txtUsername, txtUserId, txtDetailTeam;
    private ImageView imgTeamBg, btnBack;
    private View btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txtAvatar = findViewById(R.id.txtProfileAvatar);
        txtName = findViewById(R.id.txtProfileName);
        txtUsername = findViewById(R.id.txtDetailUsername);
        txtUserId = findViewById(R.id.txtDetailUserId);
        txtDetailTeam = findViewById(R.id.txtDetailTeam);
        imgTeamBg = findViewById(R.id.imgTeamBg);
        btnBack = findViewById(R.id.btnProfileBack);
        btnLogout = findViewById(R.id.btnLogout);

        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        loadUser();
    }

    private void loadUser() {
        User u = AppState.get().getCachedUser();
        if (u == null) {
            finish();
            return;
        }

        String name = u.username != null ? u.username : "Player";
        txtAvatar.setText(name.substring(0, 1).toUpperCase());
        txtName.setText(name);
        txtUsername.setText(name);
        txtUserId.setText("ID: " + u.id);
        
        String teamName = u.teamId != null ? getFullTeamName(u.teamId) : "NO TEAM";
        txtDetailTeam.setText(teamName);

        if (u.teamId != null) {
            setTeamSpecifics(u.teamId);
        }
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
            case 9: return "Rajasthan Royals";
            case 10: return "Punjab Kings";
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
            case 9: resId = R.drawable.team_rr; break;
            case 10: resId = R.drawable.team_pbks; break;
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
