package com.example.ui.settings;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostMediaEntity;
import com.example.data.entity.PostWithDetails;
import com.example.databinding.ItemPostOrganizerBinding;
import com.example.util.MediaHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostOrganizerAdapter extends RecyclerView.Adapter<PostOrganizerAdapter.OrganizerViewHolder> {

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public interface OnOrderChangedListener {
        void onOrderChanged();
    }

    private final List<PostWithDetails> items = new ArrayList<>();
    private final OnStartDragListener startDragListener;
    private final OnOrderChangedListener orderChangedListener;

    public PostOrganizerAdapter(OnStartDragListener startDragListener, OnOrderChangedListener orderChangedListener) {
        this.startDragListener = startDragListener;
        this.orderChangedListener = orderChangedListener;
    }

    public void submitList(List<PostWithDetails> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<PostWithDetails> getItems() {
        return items;
    }

    public List<String> getPostIds() {
        List<String> ids = new ArrayList<>();
        for (PostWithDetails item : items) {
            ids.add(item.getPost().getId());
        }
        return ids;
    }

    public void swap(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0 || fromPosition >= items.size() || toPosition >= items.size()) {
            return;
        }
        Collections.swap(items, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        // Refresh position badges in the range
        int start = Math.min(fromPosition, toPosition);
        int count = Math.abs(fromPosition - toPosition) + 1;
        notifyItemRangeChanged(start, count);

        if (orderChangedListener != null) {
            orderChangedListener.onOrderChanged();
        }
    }

    @NonNull
    @Override
    public OrganizerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostOrganizerBinding binding = ItemPostOrganizerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrganizerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizerViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class OrganizerViewHolder extends RecyclerView.ViewHolder {
        final ItemPostOrganizerBinding binding;

        OrganizerViewHolder(ItemPostOrganizerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PostWithDetails item, int position) {
            Context context = itemView.getContext();

            // Position Badge
            binding.textOrganizerPosition.setText("#" + (position + 1));

            // Author Username
            PersonaEntity author = item.getAuthor();
            if (author != null) {
                binding.textOrganizerAuthor.setText("@" + author.getUsername());
            } else {
                binding.textOrganizerAuthor.setText("@unknown");
            }

            // Post Text (Single Line truncated with ellipsis)
            String text = item.getPost().getTextContent();
            if (text != null && !text.trim().isEmpty()) {
                binding.textOrganizerContent.setText(text.replace("\n", " ").trim());
            } else {
                binding.textOrganizerContent.setText("(Photo update)");
            }

            // Relative Timestamp
            CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(
                    item.getPost().getCreatedAt(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );

            // Thumbnail Handling
            List<PostMediaEntity> mediaList = item.getMediaList();
            if (mediaList != null && !mediaList.isEmpty()) {
                String firstImagePath = mediaList.get(0).getMediaPath();
                Bitmap bitmap = MediaHelper.loadBitmap(context, firstImagePath, 100);
                if (bitmap != null) {
                    binding.imageOrganizerThumbnail.setImageBitmap(bitmap);
                    binding.imageOrganizerThumbnail.setVisibility(View.VISIBLE);
                } else {
                    binding.imageOrganizerThumbnail.setVisibility(View.GONE);
                }

                int count = mediaList.size();
                String mediaMeta = count > 1 ? (" • 📷 " + count + " photos") : " • 📷 1 photo";
                binding.textOrganizerMeta.setText(timeSpan + mediaMeta);
            } else {
                binding.imageOrganizerThumbnail.setVisibility(View.GONE);
                binding.textOrganizerMeta.setText(timeSpan);
            }

            // Drag handle touch listener
            binding.imageDragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (startDragListener != null) {
                        startDragListener.onStartDrag(this);
                    }
                }
                return false;
            });
        }
    }
}
