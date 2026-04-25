package com.zpl.handcricket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.zpl.handcricket.R;
import com.zpl.handcricket.views.HandView;

public class FriendMatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_friend_match);

        HandView l = findViewById(R.id.bottomHandL);
        HandView r = findViewById(R.id.bottomHandR);
        l.setHandColor(HandView.COLOR_BLUE);
        l.setFingers(0);
        r.setHandColor(HandView.COLOR_RED);
        r.setMirrored(true);
        r.setFingers(0);

        ImageView back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());
        findViewById(R.id.btnCreate).setOnClickListener(v ->
                startActivity(new Intent(this, CreateRoomActivity.class)));
        findViewById(R.id.btnJoin).setOnClickListener(v ->
                startActivity(new Intent(this, JoinRoomActivity.class)));
    }
}
