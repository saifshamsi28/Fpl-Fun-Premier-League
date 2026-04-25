package com.zpl.handcricket.models;

import com.google.gson.annotations.SerializedName;

public class User {
    public String id;
    public String username;
    @SerializedName("teamId") public Integer teamId;
    @SerializedName("totalRuns") public int totalRuns;
    @SerializedName("matchesPlayed") public int matchesPlayed;
    @SerializedName("matchesWon") public int matchesWon;
    @SerializedName("matchesLeftToday") public int matchesLeftToday;
}
