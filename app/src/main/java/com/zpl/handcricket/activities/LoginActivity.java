package com.zpl.handcricket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zpl.handcricket.api.ApiClient;
import com.zpl.handcricket.models.AuthResponse;
import com.zpl.handcricket.R;
import com.zpl.handcricket.utils.AppState;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUser, etPass;
    private Button btnLogin, btnSignup;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_login);
        etUser = findViewById(R.id.etUsername);
        etPass = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);

        btnLogin.setOnClickListener(v -> auth(false));
        btnSignup.setOnClickListener(v -> auth(true));
    }

    private void auth(boolean signup) {
        String u = etUser.getText().toString().trim();
        String p = etPass.getText().toString();
        if (u.length() < 2 || p.length() < 4) {
            Toast.makeText(this, "Enter username and password (min 4 chars)", Toast.LENGTH_SHORT).show();
            return;
        }
        setBusy(true);
        Map<String, String> body = new HashMap<>();
        body.put("username", u);
        body.put("password", p);
        Call<AuthResponse> call = signup
                ? ApiClient.get().signup(body)
                : ApiClient.get().login(body);
        call.enqueue(new Callback<AuthResponse>() {
            @Override public void onResponse(Call<AuthResponse> c, Response<AuthResponse> r) {
                setBusy(false);
                if (!r.isSuccessful() || r.body() == null) {
                    Toast.makeText(LoginActivity.this,
                            "Auth failed: " + r.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                AuthResponse body = r.body();
                AppState.get().setAuth(body.token, body.user);
                Intent next = (body.user.teamId == null)
                        ? new Intent(LoginActivity.this, TeamSelectionActivity.class)
                        : new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(next);
                finish();
            }
            @Override public void onFailure(Call<AuthResponse> c, Throwable t) {
                setBusy(false);
                Toast.makeText(LoginActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setBusy(boolean b) {
        btnLogin.setEnabled(!b);
        btnSignup.setEnabled(!b);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // no-op
        View v = findViewById(R.id.etUsername);
        if (v != null) v.requestFocus();
    }
}
