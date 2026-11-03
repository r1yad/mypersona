package com.example.ui.feed;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.entity.CommentWithAuthor;
import com.example.data.entity.PersonaEntity;
import com.example.databinding.ItemCommentBinding;

import java.io.File;

public class CommentsAdapter extends ListAdapter<CommentWithAuthor, CommentsAdapter.CommentViewHolder> {

    public interface OnCommentClickListener {
        void onCommentLikeClicked(CommentWithAuthor comment);
    }

    private final OnCommentClickListener listener;

    public CommentsAdapter(OnCommentClickListener listener) {
        super(new DiffUtil.ItemCallback<CommentWithAuthor>() {
            @Override
            public boolean areItemsTheSame(@NonNull CommentWithAuthor oldItem, @NonNull CommentWithAuthor newItem) {
                return oldItem.getComment().getId().equals(newItem.getComment().getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull CommentWithAuthor oldItem, @NonNull CommentWithAuthor newItem) {
                return oldItem.getLikeCount() == newItem.getLikeCount() &&
                        oldItem.isLikedByActivePersona() == newItem.isLikedByActivePersona() &&
                        oldItem.getComment().getTextContent().equals(newItem.getComment().getTextContent());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentBinding binding;

        public CommentViewHolder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CommentWithAuthor item) {
            PersonaEntity author = item.getAuthor();
            if (author != null) {
                binding.textCommentAuthor.setText(author.getDisplayName());
                if (author.getAvatarPath() != null) {
                    Bitmap bitmap = com.example.util.MediaHelper.loadBitmap(binding.getRoot().getContext(), author.getAvatarPath(), 100);
                    if (bitmap != null) {
                        binding.imageCommentAvatar.setImageBitmap(bitmap);
                    } else {
                        binding.imageCommentAvatar.setImageResource(R.drawable.ic_person_placeholder);
                    }
                } else {
                    binding.imageCommentAvatar.setImageResource(R.drawable.ic_person_placeholder);
                }
            } else {
                binding.textCommentAuthor.setText("Unknown Persona");
                binding.imageCommentAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }

            CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(
                    item.getComment().getCreatedAt(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            binding.textCommentTime.setText(timeSpan);
            binding.textCommentBody.setText(item.getComment().getTextContent());
            binding.textCommentLikes.setText(String.valueOf(item.getLikeCount()));

            if (item.isLikedByActivePersona()) {
                binding.imageCommentLikeIcon.setImageResource(R.drawable.ic_heart_filled);
                binding.imageCommentLikeIcon.setColorFilter(binding.getRoot().getContext().getColor(R.color.rose_like));
                binding.textCommentLikes.setTextColor(binding.getRoot().getContext().getColor(R.color.rose_like));
            } else {
                binding.imageCommentLikeIcon.setImageResource(R.drawable.ic_heart_outline);
                binding.imageCommentLikeIcon.setColorFilter(binding.getRoot().getContext().getColor(R.color.text_secondary));
                binding.textCommentLikes.setTextColor(binding.getRoot().getContext().getColor(R.color.text_secondary));
            }

            binding.btnCommentLike.setOnClickListener(v -> listener.onCommentLikeClicked(item));
        }
    }
}
