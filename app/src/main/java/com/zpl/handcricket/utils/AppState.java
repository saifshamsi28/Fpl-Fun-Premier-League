package com.zpl.handcricket.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.zpl.handcricket.models.User;

public class AppState {

    private static AppState INSTANCE;
    public static AppState get() {
        if (INSTANCE == null) INSTANCE = new AppState();
        return INSTANCE;
    }

    private static final String PREFS = "zpl_prefs";
    private static final String K_TOKEN = "token";
    private static final String K_USERNAME = "username";
    private static final String K_USER_ID = "user_id";
    private static final String K_TEAM_ID = "team_id";

    private SharedPreferences prefs;
    private User cachedUser;

    public void init(Context ctx) {
        if (prefs == null) prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String getToken() { return prefs == null ? null : prefs.getString(K_TOKEN, null); }
    public String getUserId() { return prefs == null ? null : prefs.getString(K_USER_ID, null); }
    public String getUsername() { return prefs == null ? null : prefs.getString(K_USERNAME, null); }
    public Integer getTeamId() {
        if (prefs == null) return null;
        int v = prefs.getInt(K_TEAM_ID, -1);
        return v == -1 ? null : v;
    }

    public void setAuth(String token, User u) {
        cachedUser = u;
        prefs.edit()
            .putString(K_TOKEN, token)
            .putString(K_USER_ID, u.id)
            .putString(K_USERNAME, u.username)
            .putInt(K_TEAM_ID, u.teamId == null ? -1 : u.teamId)
            .apply();
    }

    public void setTeamId(int teamId) {
        prefs.edit().putInt(K_TEAM_ID, teamId).apply();
        if (cachedUser != null) cachedUser.teamId = teamId;
    }

    public User getCachedUser() { return cachedUser; }
    public void setCachedUser(User u) { cachedUser = u; }

    public void clear() {
        cachedUser = null;
        if (prefs != null) prefs.edit().clear().apply();
    }
}
