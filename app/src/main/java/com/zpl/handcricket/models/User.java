package com.zpl.handcricket.models;

import com.google.gson.annotations.SerializedName;

public class User {
    public String id;
    public String username;
    @SerializedName("fullName") public String fullName;
    @SerializedName("email") public String email;
    @SerializedName("city") public String city;
    @SerializedName("favoritePlayer") public String favoritePlayer;
    @SerializedName("teamId") public Integer teamId;
    @SerializedName("totalRuns") public int totalRuns;
    @SerializedName("matchesPlayed") public int matchesPlayed;
    @SerializedName("matchesWon") public int matchesWon;
    @SerializedName("matchesLeftToday") public int matchesLeftToday;
    @SerializedName("rank") public Integer rank;
}
