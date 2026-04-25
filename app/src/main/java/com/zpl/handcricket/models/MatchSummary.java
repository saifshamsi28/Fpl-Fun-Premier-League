package com.zpl.handcricket.models;

import com.google.gson.annotations.SerializedName;

public class MatchSummary {
    public String id;
    @SerializedName("opponentName")
    public String opponentName;

    @SerializedName("opponentTeam")
    public String opponentTeam;   // e.g. MUMBAI

    @SerializedName("myName")
    public String myName;

    @SerializedName("myTeam")
    public String myTeam;

    @SerializedName("myRuns")
    public int myRuns;

    @SerializedName("opponentRuns")
    public int opponentRuns;

    @SerializedName("won")
    public boolean won;

    @SerializedName("mode")
    public String mode;           // RANKED | FRIENDLY

    @SerializedName("playedAt")
    public String playedAt;       // ISO-8601
}
