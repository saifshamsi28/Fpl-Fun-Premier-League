package com.zpl.handcricket.utils;

import android.graphics.Color;

import java.util.HashMap;
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
    }

    public static int colorFor(String code) {
        Integer c = code == null ? null : COLORS.get(code);
        return c == null ? Color.parseColor("#1D4ED8") : c;
    }
}
