package com.zpl.handcricket.models;

import com.google.gson.annotations.SerializedName;

public class BallDetail {
    @SerializedName("innings")
    public int innings;      // 1 or 2

    @SerializedName("ballNo")
    public int ballNo;       // 1..6

    @SerializedName("batterName")
    public String batterName;

    @SerializedName("bowlerName")
    public String bowlerName;

    @SerializedName("batterPick")
    public int batterPick;   // 1..6

    @SerializedName("bowlerPick")
    public int bowlerPick;   // 1..6

    @SerializedName("runs")
    public int runs;         // 0 if wicket

    @SerializedName("wicket")
    public boolean wicket;
}
