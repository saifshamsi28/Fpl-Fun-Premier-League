package com.zpl.handcricket.models;

import com.google.gson.annotations.SerializedName;

public class Team {
    public int id;
    public String code;
    public String name;
    public String landmark;
    @SerializedName("primaryColor") public String primaryColor;
}
