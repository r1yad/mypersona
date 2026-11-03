package com.example.ui.feed;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostWithDetails;
import com.example.databinding.ItemPostCardBinding;

import java.io.File;

public class FeedAdapter extends ListAdapter<PostWithDetails, FeedAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClicked(PostWithDetails post);
        void onLikeClicked(PostWithDetails post);
        void onDeleteClicked(PostWithDetails post);
        void onAuthorClicked(PersonaEntity author);
    }

    private final OnPostClickListener listener;
    private String activePersonaId = "";

    public FeedAdapter(OnPostClickListener listener) {
        super(new DiffUtil.ItemCallback<PostWithDetails>() {
            @Override
            public boolean areItemsTheSame(@NonNull PostWithDetails oldItem, @NonNull PostWithDetails newItem) {
                return oldItem.getPost().getId().equals(newItem.getPost().getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull PostWithDetails oldItem, @NonNull PostWithDetails newItem) {
                return oldItem.getPost().getUpdatedAt() == newItem.getPost().getUpdatedAt() &&
                        oldItem.getLikeCount() == newItem.getLikeCount() &&
                        oldItem.getCommentCount() == newItem.getCommentCount() &&
                        oldItem.isLikedByActivePersona() == newItem.isLikedByActivePersona();
            }
        });
        this.listener = listener;
    }

    public void setActivePersonaId(String activePersonaId) {
        this.activePersonaId = activePersonaId != null ? activePersonaId : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostCardBinding binding = ItemPostCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostCardBinding binding;

        public PostViewHolder(ItemPostCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PostWithDetails item) {
            PersonaEntity author = item.getAuthor();
            if (author != null) {
                binding.textPostAuthor.setText(author.getDisplayName());
                CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(
                        item.getPost().getCreatedAt(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                );
                binding.textPostHandleTime.setText("@" + author.getUsername() + " • " + timeSpan);

                if (author.getAvatarPath() != null) {
                    Bitmap bitmap = com.example.util.MediaHelper.loadBitmap(binding.getRoot().getContext(), author.getAvatarPath(), 120);
                    if (bitmap != null) {
                        binding.imagePostAvatar.setImageBitmap(bitmap);
                    } else {
                        binding.imagePostAvatar.setImageResource(R.drawable.ic_person_placeholder);
                    }
                } else {
                    binding.imagePostAvatar.setImageResource(R.drawable.ic_person_placeholder);
                }
            } else {
                binding.textPostAuthor.setText("Unknown Persona");
                binding.textPostHandleTime.setText("@unknown");
                binding.imagePostAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }

            binding.textPostContent.setText(item.getPost().getTextContent());
            binding.textPostLikes.setText(String.valueOf(item.getLikeCount()));
            binding.textPostComments.setText(String.valueOf(item.getCommentCount()));

            if (item.isLikedByActivePersona()) {
                binding.imagePostLikeIcon.setImageResource(R.drawable.ic_heart_filled);
                binding.imagePostLikeIcon.setColorFilter(binding.getRoot().getContext().getColor(R.color.rose_like));
                binding.textPostLikes.setTextColor(binding.getRoot().getContext().getColor(R.color.rose_like));
            } else {
                binding.imagePostLikeIcon.setImageResource(R.drawable.ic_heart_outline);
                binding.imagePostLikeIcon.setColorFilter(binding.getRoot().getContext().getColor(R.color.text_secondary));
                binding.textPostLikes.setTextColor(binding.getRoot().getContext().getColor(R.color.text_secondary));
            }

            // Attached Media Preview
            if (!item.getMediaList().isEmpty()) {
                String mediaPath = item.getMediaList().get(0).getMediaPath();
                Bitmap bm = com.example.util.MediaHelper.loadBitmap(binding.getRoot().getContext(), mediaPath, 1080);
                if (bm != null) {
                    binding.imagePostMedia.setImageBitmap(bm);
                    binding.imagePostMedia.setVisibility(View.VISIBLE);
                } else {
                    binding.imagePostMedia.setVisibility(View.GONE);
                }
            } else {
                binding.imagePostMedia.setVisibility(View.GONE);
            }

            // Delete button visible if author matches active persona
            if (author != null && author.getId().equals(activePersonaId)) {
                binding.btnPostDelete.setVisibility(View.VISIBLE);
                binding.btnPostDelete.setOnClickListener(v -> listener.onDeleteClicked(item));
            } else {
                binding.btnPostDelete.setVisibility(View.GONE);
            }

            if (author != null) {
                binding.imagePostAvatar.setOnClickListener(v -> listener.onAuthorClicked(author));
                binding.textPostAuthor.setOnClickListener(v -> listener.onAuthorClicked(author));
                binding.textPostHandleTime.setOnClickListener(v -> listener.onAuthorClicked(author));
            }

            binding.cardPost.setOnClickListener(v -> listener.onPostClicked(item));
            binding.btnPostLike.setOnClickListener(v -> listener.onLikeClicked(item));
            binding.btnPostComment.setOnClickListener(v -> listener.onPostClicked(item));
        }
    }
}
