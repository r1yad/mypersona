package com.example.ui.feed;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.util.MediaHelper;

import java.util.ArrayList;
import java.util.List;

public class PostImagesSliderAdapter extends RecyclerView.Adapter<PostImagesSliderAdapter.SliderViewHolder> {

    public interface OnImageClickListener {
        void onImageClick(int position, List<String> allImages);
    }

    private final List<String> imagePaths = new ArrayList<>();
    private final OnImageClickListener listener;

    public PostImagesSliderAdapter(List<String> paths, OnImageClickListener listener) {
        if (paths != null) {
            this.imagePaths.addAll(paths);
        }
        this.listener = listener;
    }

    public void setImages(List<String> paths) {
        this.imagePaths.clear();
        if (paths != null) {
            this.imagePaths.addAll(paths);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slider_image, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        String path = imagePaths.get(position);
        Bitmap bm = MediaHelper.loadBitmap(holder.itemView.getContext(), path, 1080);
        if (bm != null) {
            holder.imageView.setImageBitmap(bm);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(position, imagePaths);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.image_slide);
        }
    }
}
