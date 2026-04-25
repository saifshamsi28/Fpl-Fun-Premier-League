package com.zpl.handcricket.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.zpl.handcricket.R;
import com.zpl.handcricket.utils.AppState;
import com.zpl.handcricket.utils.MatchContext;
import com.zpl.handcricket.ws.GameSocket;

public class MatchmakingActivity extends AppCompatActivity implements GameSocket.Listener {

    private TextView oppInitial, oppName, youInitial, youName;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final String[] reel = {"Ravi","Priya","Arjun","Meera","Kabir","Rohan","Ishaan","Neha","Virat","Suresh"};
    private int reelIdx = 0;
    private Runnable reelTick;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_matchmaking);

        oppInitial = findViewById(R.id.oppInitial);
        oppName = findViewById(R.id.oppName);
        youInitial = findViewById(R.id.youInitial);
        youName = findViewById(R.id.youName);

        String me = AppState.get().getUsername();
        if (me == null) me = "You";
        youName.setText(me);
        youInitial.setText(String.valueOf(Character.toUpperCase(me.charAt(0))));

        // Animate the "scanning" rings
        ObjectAnimator.ofFloat(findViewById(R.id.youCircle), "rotation", 0f, 360f)
                .setDuration(4000).start();

        ImageView cancel = findViewById(R.id.btnCancel);
        cancel.setOnClickListener(v -> {
            GameSocket.get().cancelQueue();
            GameSocket.get().close();
            finish();
        });

        // Start reel
        reelTick = () -> {
            reelIdx = (reelIdx + 1) % reel.length;
            String name = reel[reelIdx];
            oppInitial.setText(String.valueOf(name.charAt(0)));
            oppName.setText("Finding Player...");
            handler.postDelayed(reelTick, 220);
        };
        handler.post(reelTick);

        // Connect + queue
        GameSocket.get().setListener(this);
        GameSocket.get().connect();
    }

    @Override public void onOpenConn() {
        runOnUiThread(() -> GameSocket.get().queueRanked());
    }

    @Override public void onClosedConn() {
        runOnUiThread(() -> {
            Toast.makeText(MatchmakingActivity.this,
                    "Connection lost", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public void onEvent(String type, JsonObject data) {
        runOnUiThread(() -> {
            if ("queued".equals(type)) {
                // keep animating
            } else if ("match_found".equals(type)) {
                handler.removeCallbacks(reelTick);
                MatchContext.get().matchFound = data;
                // Reveal opponent
                String oppUsername = data.has("opponent")
                        ? data.getAsJsonObject("opponent").get("username").getAsString()
                        : "Opponent";
                oppInitial.setText(String.valueOf(Character.toUpperCase(oppUsername.charAt(0))));
                oppName.setText(oppUsername);
                handler.postDelayed(() -> {
                    startActivity(new Intent(this, GameActivity.class));
                    finish();
                }, 900);
            } else if ("error".equals(type)) {
                Toast.makeText(this,
                        data.has("message") ? data.get("message").getAsString() : "Error",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(reelTick);
    }
}
