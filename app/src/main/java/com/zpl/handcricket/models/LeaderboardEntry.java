package com.zpl.handcricket.models;

import com.google.gson.annotations.SerializedName;

public class LeaderboardEntry {
    public int rank;
    public String userId;
    public String username;
    public String fullName;
    public String teamName;
    public String city;
    public String favoritePlayer;
    public int matchesPlayed;
    public int matchesWon;
    public int totalRuns;
    public double winRate;
    @SerializedName("you")
    public boolean isYou;
}
