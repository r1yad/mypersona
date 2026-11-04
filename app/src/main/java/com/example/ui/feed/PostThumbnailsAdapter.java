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
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class PostThumbnailsAdapter extends RecyclerView.Adapter<PostThumbnailsAdapter.ThumbViewHolder> {

    private final List<String> paths = new ArrayList<>();
    private final OnRemoveListener listener;

    public interface OnRemoveListener {
        void onRemove(int position, String path);
    }

    public PostThumbnailsAdapter(OnRemoveListener listener) {
        this.listener = listener;
    }

    public void setPaths(List<String> newPaths) {
        paths.clear();
        if (newPaths != null) {
            paths.addAll(newPaths);
        }
        notifyDataSetChanged();
    }

    public void addPath(String path) {
        paths.add(path);
        notifyItemInserted(paths.size() - 1);
    }

    public void removeAt(int position) {
        if (position >= 0 && position < paths.size()) {
            paths.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, paths.size() - position);
        }
    }

    public List<String> getPaths() {
        return new ArrayList<>(paths);
    }

    @NonNull
    @Override
    public ThumbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_thumbnail, parent, false);
        return new ThumbViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbViewHolder holder, int position) {
        String path = paths.get(position);
        Bitmap bm = MediaHelper.loadBitmap(holder.itemView.getContext(), path, 200);
        if (bm != null) {
            holder.imageThumbnail.setImageBitmap(bm);
        } else {
            holder.imageThumbnail.setImageResource(R.drawable.ic_image);
        }

        holder.btnRemoveThumb.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onRemove(pos, paths.get(pos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    static class ThumbViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imageThumbnail;
        ImageView btnRemoveThumb;

        public ThumbViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumbnail = itemView.findViewById(R.id.image_thumbnail);
            btnRemoveThumb = itemView.findViewById(R.id.btn_remove_thumb);
        }
    }
}
