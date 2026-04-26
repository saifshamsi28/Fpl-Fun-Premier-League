package com.zpl.handcricket.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zpl.handcricket.databinding.SkeletonRecentMatchItemBinding;

public class SkeletonRecentMatchAdapter extends RecyclerView.Adapter<SkeletonRecentMatchAdapter.VH> {

    private int skeletonCount = 3;

    public SkeletonRecentMatchAdapter(int count) {
        this.skeletonCount = count;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SkeletonRecentMatchItemBinding binding = SkeletonRecentMatchItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        // Skeleton just displays - no data binding needed
    }

    @Override
    public int getItemCount() {
        return skeletonCount;
    }

    public static class VH extends RecyclerView.ViewHolder {
        public VH(SkeletonRecentMatchItemBinding binding) {
            super(binding.getRoot());
        }
    }
}
