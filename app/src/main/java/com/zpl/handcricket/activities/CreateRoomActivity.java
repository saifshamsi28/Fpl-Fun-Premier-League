package com.zpl.handcricket.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.zpl.handcricket.R;
import com.zpl.handcricket.api.ApiClient;
import com.zpl.handcricket.models.Room;
import com.zpl.handcricket.utils.MatchContext;
import com.zpl.handcricket.ws.GameSocket;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateRoomActivity extends AppCompatActivity implements GameSocket.Listener {

    private TextView tvRoomCode;
    private Room myRoom;
    private boolean hostRequestSent;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_create_room);

        tvRoomCode = findViewById(R.id.tvRoomCode);
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnCopy = findViewById(R.id.btnCopy);
        Button btnInvite = findViewById(R.id.btnInvite);

        btnBack.setOnClickListener(v -> finish());
        btnCopy.setOnClickListener(v -> copyCode());
        btnInvite.setOnClickListener(v -> shareInvite());

        createRoom();
    }

    private void createRoom() {
        ApiClient.get().createRoom().enqueue(new Callback<Room>() {
            @Override public void onResponse(Call<Room> c, Response<Room> r) {
                if (!r.isSuccessful() || r.body() == null) {
                    Toast.makeText(CreateRoomActivity.this,
                            "Failed to create room", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                myRoom = r.body();
                hostRequestSent = false;
                renderCode(myRoom.code);
                // Open ws and host
                GameSocket.get().setListener(CreateRoomActivity.this);
                GameSocket.get().connect();
            }
            @Override public void onFailure(Call<Room> c, Throwable t) {
                Toast.makeText(CreateRoomActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void renderCode(String code) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            sb.append(code.charAt(i));
            if (i < code.length() - 1) sb.append(' ');
        }
        tvRoomCode.setText(sb.toString());
    }

    private void copyCode() {
        if (myRoom == null) return;
        ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cb.setPrimaryClip(ClipData.newPlainText("ZPL room", myRoom.code));
        Toast.makeText(this, "Code copied", Toast.LENGTH_SHORT).show();
    }

    private void shareInvite() {
        if (myRoom == null) return;
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT,
                "Join my ZPL Hand Cricket match! Room code: " + myRoom.code);
        startActivity(Intent.createChooser(i, "Share room"));
    }

    @Override
    public void onOpenConn() {
        tryHostRoom();
    }

    @Override
    public void onEvent(String type, JsonObject data) {
        runOnUiThread(() -> {
            if ("auth_ok".equals(type)) {
                tryHostRoom();
            } else if ("match_found".equals(type)) {
                MatchContext.get().matchFound = data;
                startActivity(new Intent(this, GameActivity.class));
                finish();
            } else if ("auth_err".equals(type)) {
                String msg = data.has("error") ? data.get("error").getAsString() : "Session expired. Please log in again";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                finish();
            } else if ("error".equals(type)) {
                Toast.makeText(this,
                        data.has("message") ? data.get("message").getAsString()
                                : data.has("error") ? data.get("error").getAsString() : "Error",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void tryHostRoom() {
        if (hostRequestSent || myRoom == null) return;
        // host_friendly can be safely queued until auth completes.
        GameSocket.get().hostFriendly(myRoom.id);
        hostRequestSent = true;
    }
}
