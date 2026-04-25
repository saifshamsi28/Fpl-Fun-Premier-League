package com.zpl.handcricket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.zpl.handcricket.R;
import com.zpl.handcricket.api.ApiClient;
import com.zpl.handcricket.models.Room;
import com.zpl.handcricket.utils.MatchContext;
import com.zpl.handcricket.ws.GameSocket;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinRoomActivity extends AppCompatActivity implements GameSocket.Listener {

    private EditText[] digits;
    private Button btnJoin;
    private String targetRoomId;
    private boolean joinRequestSent;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_join_room);

        digits = new EditText[] {
                findViewById(R.id.d1),
                findViewById(R.id.d2),
                findViewById(R.id.d3),
                findViewById(R.id.d4),
                findViewById(R.id.d5)
        };
        btnJoin = findViewById(R.id.btnJoin);
        btnJoin.setEnabled(false);

        ImageView back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        Button create = findViewById(R.id.btnCreateInstead);
        create.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateRoomActivity.class));
            finish();
        });

        for (int i = 0; i < digits.length; i++) {
            final int idx = i;
            digits[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s1, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s1, int a, int b, int c) {}
                @Override public void afterTextChanged(Editable e) {
                    if (e.length() == 1 && idx < digits.length - 1) {
                        digits[idx + 1].requestFocus();
                    }
                    btnJoin.setEnabled(fullCode().length() == 5);
                    btnJoin.setBackgroundResource(
                            btnJoin.isEnabled() ? R.drawable.bg_button_blue : R.drawable.bg_code_box);
                }
            });
        }

        btnJoin.setOnClickListener(v -> join());
    }

    private String fullCode() {
        StringBuilder sb = new StringBuilder();
        for (EditText e : digits) sb.append(e.getText().toString().trim());
        return sb.toString();
    }

    private void join() {
        String code = fullCode();
        if (code.length() != 5) return;
        btnJoin.setEnabled(false);
        Map<String, String> body = new HashMap<>();
        body.put("code", code);
        ApiClient.get().joinRoom(body).enqueue(new Callback<Room>() {
            @Override public void onResponse(Call<Room> c, Response<Room> r) {
                if (!r.isSuccessful() || r.body() == null) {
                    Toast.makeText(JoinRoomActivity.this,
                            "Invalid room code", Toast.LENGTH_SHORT).show();
                    btnJoin.setEnabled(true);
                    return;
                }
                targetRoomId = r.body().id;
                joinRequestSent = false;
                GameSocket.get().setListener(JoinRoomActivity.this);
                GameSocket.get().connect();
            }
            @Override public void onFailure(Call<Room> c, Throwable t) {
                Toast.makeText(JoinRoomActivity.this,
                        "Network error", Toast.LENGTH_SHORT).show();
                btnJoin.setEnabled(true);
            }
        });
    }

    @Override
    public void onOpenConn() {
        tryJoinRoom();
    }

    @Override
    public void onEvent(String type, JsonObject data) {
        runOnUiThread(() -> {
            if ("auth_ok".equals(type)) {
                tryJoinRoom();
            } else if ("match_found".equals(type)) {
                MatchContext.get().matchFound = data;
                startActivity(new Intent(this, GameActivity.class));
                finish();
            } else if ("auth_err".equals(type)) {
                String msg = data.has("error") ? data.get("error").getAsString() : "Session expired. Please log in again";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                btnJoin.setEnabled(true);
            } else if ("error".equals(type)) {
                Toast.makeText(this,
                        data.has("message") ? data.get("message").getAsString()
                                : data.has("error") ? data.get("error").getAsString() : "Error",
                        Toast.LENGTH_SHORT).show();
                btnJoin.setEnabled(true);
            }
        });
    }

    @Override
    public void onClosedConn() {
        runOnUiThread(() -> btnJoin.setEnabled(true));
    }

    private void tryJoinRoom() {
        if (joinRequestSent || targetRoomId == null) return;
        // join_friendly can be safely queued until auth completes.
        GameSocket.get().joinFriendly(targetRoomId);
        joinRequestSent = true;
    }
}
