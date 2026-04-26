package com.zpl.handcricket.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zpl.handcricket.databinding.SkeletonMatchHistoryItemBinding;

public class SkeletonMatchHistoryAdapter extends RecyclerView.Adapter<SkeletonMatchHistoryAdapter.VH> {

    private int skeletonCount = 5;

    public SkeletonMatchHistoryAdapter(int count) {
        this.skeletonCount = count;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SkeletonMatchHistoryItemBinding binding = SkeletonMatchHistoryItemBinding.inflate(
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
        public VH(SkeletonMatchHistoryItemBinding binding) {
            super(binding.getRoot());
        }
    }
}
