package com.zpl.handcricket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

    private EditText etUser, etPass, etFullName, etEmail, etCity, etFavoritePlayer;
    private Button btnLogin, btnSignup;
    private TextView tabLogin, tabSignup, txtModeHint;
    private View signupFields;
    private boolean signupMode = false;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_login);

        etUser = findViewById(R.id.etUsername);
        etPass = findViewById(R.id.etPassword);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etCity = findViewById(R.id.etCity);
        etFavoritePlayer = findViewById(R.id.etFavoritePlayer);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        tabLogin = findViewById(R.id.tabLogin);
        tabSignup = findViewById(R.id.tabSignup);
        txtModeHint = findViewById(R.id.txtModeHint);
        signupFields = findViewById(R.id.signupFields);

        tabLogin.setOnClickListener(v -> setMode(false));
        tabSignup.setOnClickListener(v -> setMode(true));

        btnLogin.setOnClickListener(v -> auth(signupMode));
        btnSignup.setOnClickListener(v -> setMode(!signupMode));

        setMode(false);
    }

    private void auth(boolean signup) {
        String u = etUser.getText().toString().trim();
        String p = etPass.getText().toString();
        if (u.length() < 3) {
            Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (p.length() < 6 && signup) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        setBusy(true);
        Map<String, String> body = new HashMap<>();
        body.put("username", u);
        body.put("password", p);

        if (signup) {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String favoritePlayer = etFavoritePlayer.getText().toString().trim();

            if (fullName.length() < 2) {
                setBusy(false);
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                setBusy(false);
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (city.length() < 2) {
                setBusy(false);
                Toast.makeText(this, "Please enter your city", Toast.LENGTH_SHORT).show();
                return;
            }

            body.put("fullName", fullName);
            body.put("email", email);
            body.put("city", city);
            body.put("favoritePlayer", favoritePlayer);
        }

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

    private void setMode(boolean signup) {
        this.signupMode = signup;

        signupFields.setVisibility(signup ? View.VISIBLE : View.GONE);
        txtModeHint.setText(signup ? "Create your profile to start ranking" : "Log in to continue");

        tabLogin.setBackgroundResource(signup ? R.drawable.bg_auth_tab_inactive : R.drawable.bg_auth_tab_active);
        tabSignup.setBackgroundResource(signup ? R.drawable.bg_auth_tab_active : R.drawable.bg_auth_tab_inactive);

        tabLogin.setTextColor(signup ? 0xFF0E1A3B : 0xFFFFFFFF);
        tabSignup.setTextColor(signup ? 0xFFFFFFFF : 0xFF0E1A3B);

        btnLogin.setText(signup ? "Create Account" : "Log In");
        btnLogin.setBackgroundResource(signup ? R.drawable.bg_cta_red : R.drawable.bg_continue_btn);
        btnSignup.setText(signup ? "Already have an account? Log In" : "Don't have an account? Create one");
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
        tabLogin.setEnabled(!b);
        tabSignup.setEnabled(!b);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // no-op
        View v = findViewById(R.id.etUsername);
        if (v != null) v.requestFocus();
    }
}
