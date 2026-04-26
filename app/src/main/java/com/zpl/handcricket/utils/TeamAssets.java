package com.zpl.handcricket.utils;

import android.graphics.Color;

import com.zpl.handcricket.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps team codes to primary accent colors, used when rendering the scorecard pills
 * and the team banner badges.
 */
public class TeamAssets {
    public static final Map<String, Integer> COLORS = new HashMap<>();
    static {
        COLORS.put("MUM", Color.parseColor("#F77F00"));
        COLORS.put("HYD", Color.parseColor("#E63946"));
        COLORS.put("DEL", Color.parseColor("#1D4ED8"));
        COLORS.put("KOL", Color.parseColor("#7C3AED"));
        COLORS.put("BLR", Color.parseColor("#F59E0B"));
        COLORS.put("CHE", Color.parseColor("#FB7185"));

        // Future-proof aliases for additional teams/codes.
        COLORS.put("RCB", Color.parseColor("#B91C1C"));
        COLORS.put("CSK", Color.parseColor("#CA8A04"));
        COLORS.put("DC", Color.parseColor("#1D4ED8"));
        COLORS.put("KKR", Color.parseColor("#6D28D9"));
        COLORS.put("MI", Color.parseColor("#1D4ED8"));
        COLORS.put("SRH", Color.parseColor("#EA580C"));
        COLORS.put("RR", Color.parseColor("#DB2777"));
        COLORS.put("PBKS", Color.parseColor("#DC2626"));
        COLORS.put("LSG", Color.parseColor("#0891B2"));
        COLORS.put("GT", Color.parseColor("#334155"));
    }

    public static int colorFor(String code) {
        Integer c = code == null ? null : COLORS.get(code);
        return c == null ? Color.parseColor("#1D4ED8") : c;
    }

    public static int imageFor(String code, String name) {
        String c = code == null ? "" : code.trim().toUpperCase(Locale.US);
        String n = name == null ? "" : name.trim().toLowerCase(Locale.US);

        switch (c) {
            case "BLR":
            case "RCB":
                return R.drawable.team_rcb;
            case "CHE":
            case "CSK":
                return R.drawable.team_csk;
            case "DEL":
            case "DC":
                return R.drawable.team_dc;
            case "KOL":
            case "KKR":
                return R.drawable.team_kkr;
            case "MUM":
            case "MI":
                return R.drawable.team_mi;
            case "HYD":
            case "SRH":
                return R.drawable.team_srh;
            case "RR":
                return R.drawable.team_rr;
            case "PBKS":
                return R.drawable.team_pbks;
            case "LSG":
                return R.drawable.team_lsg;
            case "GT":
                return R.drawable.team_gt;
            default:
                break;
        }

        if (n.contains("bangalore")) return R.drawable.team_rcb;
        if (n.contains("chennai")) return R.drawable.team_csk;
        if (n.contains("delhi")) return R.drawable.team_dc;
        if (n.contains("kolkata")) return R.drawable.team_kkr;
        if (n.contains("mumbai")) return R.drawable.team_mi;
        if (n.contains("hyderabad")) return R.drawable.team_srh;
        if (n.contains("rajasthan")) return R.drawable.team_rr;
        if (n.contains("punjab")) return R.drawable.team_pbks;
        if (n.contains("lucknow")) return R.drawable.team_lsg;
        if (n.contains("gujarat")) return R.drawable.team_gt;
        return R.drawable.team_placeholder;
    }
}
