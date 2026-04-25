package com.zpl.handcricket.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class TimeAgo {
    private TimeAgo() {}

    public static String format(String iso) {
        if (iso == null || iso.isEmpty()) return "—";
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            // Strip milliseconds / Z
            String trimmed = iso.length() > 19 ? iso.substring(0, 19) : iso;
            Date d = f.parse(trimmed);
            if (d == null) return iso;
            long diff = (System.currentTimeMillis() - d.getTime()) / 1000;
            if (diff < 0) diff = 0;
            if (diff < 60)      return "just now";
            if (diff < 3600)    return (diff / 60)    + "m ago";
            if (diff < 86400)   return (diff / 3600)  + "h ago";
            if (diff < 604800)  return (diff / 86400) + "d ago";
            return new SimpleDateFormat("dd MMM", Locale.US).format(d);
        } catch (Exception e) {
            return iso;
        }
    }
}
