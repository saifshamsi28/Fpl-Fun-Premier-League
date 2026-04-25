package com.zpl.handcricket.models;

import com.google.gson.annotations.SerializedName;

public class Room {
    public String id;
    public String code;
    @SerializedName("hostUserId") public String hostUserId;
    @SerializedName("guestUserId") public String guestUserId;
    public String status;
}
