package com.zpl.handcricket.models;

import java.util.List;

public class MatchDetail {
    public String id;
    public String myName;
    public String myTeam;
    public String opponentName;
    public String opponentTeam;
    public int myRuns;
    public int opponentRuns;
    public boolean won;
    public String mode;
    public String playedAt;
    public List<BallDetail> balls;
}
