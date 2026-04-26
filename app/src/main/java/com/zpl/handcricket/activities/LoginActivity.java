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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
                            friendlyAuthMessage(r, signup), Toast.LENGTH_SHORT).show();
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
                        "Unable to reach the server. Check your connection and try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String friendlyAuthMessage(Response<AuthResponse> response, boolean signup) {
        String serverMessage = extractServerError(response);
        if (serverMessage != null) {
            return serverMessage;
        }
        if (response.code() == 409 && signup) {
            return "That username already exists. Try a different one.";
        }
        if (response.code() == 401 && !signup) {
            return "Invalid username or password.";
        }
        if (response.code() == 400) {
            return signup
                    ? "Please enter a valid username and password."
                    : "Please check your login details.";
        }
        return signup
                ? "Unable to sign up right now. Please try again."
                : "Unable to log in right now. Please try again.";
    }

    private String extractServerError(Response<AuthResponse> response) {
        if (response.errorBody() == null) return null;
        try {
            String raw = response.errorBody().string();
            if (raw == null || raw.isBlank()) return null;
            JSONObject obj = new JSONObject(raw);
            String message = obj.optString("error", "").trim();
            return message.isEmpty() ? null : message;
        } catch (IOException | JSONException | RuntimeException ex) {
            return null;
        }
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
