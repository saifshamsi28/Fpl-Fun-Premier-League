package com.zpl.handcricket.utils;

import com.google.gson.JsonObject;

/**
 * Holds the payload of the last "match_found" event so GameActivity can pick it up
 * after being launched from MatchmakingActivity / CreateRoomActivity / JoinRoomActivity.
 */
public class MatchContext {
    private static MatchContext INSTANCE;
    public static synchronized MatchContext get() {
        if (INSTANCE == null) INSTANCE = new MatchContext();
        return INSTANCE;
    }

    public JsonObject matchFound;
    public JsonObject lastMatchEnd;

    public void clear() {
        matchFound = null;
        lastMatchEnd = null;
    }
}
