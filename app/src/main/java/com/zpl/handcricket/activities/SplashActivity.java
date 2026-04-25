package com.zpl.handcricket.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.zpl.handcricket.R;
import com.zpl.handcricket.utils.AppState;
import com.zpl.handcricket.views.HandView;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_splash);

        HandView blue = findViewById(R.id.handBlue);
        HandView red  = findViewById(R.id.handRed);
        blue.setHandColor(HandView.COLOR_BLUE);
        blue.setFingers(0);
        red.setHandColor(HandView.COLOR_RED);
        red.setMirrored(true);
        red.setFingers(0);

        ProgressBar pb = findViewById(R.id.progress);
        ValueAnimator va = ValueAnimator.ofInt(0, 100);
        va.setDuration(1800);
        va.addUpdateListener(a -> pb.setProgress((int) a.getAnimatedValue()));
        va.start();

        new Handler(Looper.getMainLooper()).postDelayed(this::route, 2000);
    }

    private void route() {
        String token = AppState.get().getToken();
        Intent next;
        if (token == null) {
            next = new Intent(this, LoginActivity.class);
        } else if (AppState.get().getTeamId() == null) {
            next = new Intent(this, TeamSelectionActivity.class);
        } else {
            next = new Intent(this, HomeActivity.class);
        }
        startActivity(next);
        finish();
    }
}
