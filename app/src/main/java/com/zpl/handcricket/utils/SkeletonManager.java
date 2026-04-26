package com.zpl.handcricket.utils;

import android.view.View;
import android.view.ViewGroup;

public class SkeletonManager {

    public static void showSkeleton(ViewGroup container, View skeletonView) {
        // Hide all child views except skeleton
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child != skeletonView) {
                child.setVisibility(View.GONE);
            }
        }
        skeletonView.setVisibility(View.VISIBLE);
    }

    public static void hideSkeleton(ViewGroup container, View skeletonView) {
        // Show all child views and hide skeleton
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child != skeletonView) {
                child.setVisibility(View.VISIBLE);
            }
        }
        skeletonView.setVisibility(View.GONE);
    }

    public static void showSkeletonContent(View skeletonView) {
        skeletonView.setVisibility(View.VISIBLE);
    }

    public static void hideSkeletonContent(View skeletonView) {
        skeletonView.setVisibility(View.GONE);
    }
}
