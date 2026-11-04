package com.example.ui.feed;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.entity.CommentWithAuthor;
import com.example.data.entity.PersonaEntity;
import com.example.databinding.ItemCommentBinding;

public class CommentsAdapter extends ListAdapter<CommentWithAuthor, CommentsAdapter.CommentViewHolder> {

    public interface OnCommentClickListener {
        void onCommentLikeClicked(CommentWithAuthor comment);
        void onCommentReplyClicked(CommentWithAuthor comment);
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
                        oldItem.getDepthLevel() == newItem.getDepthLevel() &&
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
            Context context = binding.getRoot().getContext();
            PersonaEntity author = item.getAuthor();
            if (author != null) {
                binding.textCommentAuthor.setText(author.getDisplayName());
                if (author.getAvatarPath() != null) {
                    Bitmap bitmap = com.example.util.MediaHelper.loadBitmap(context, author.getAvatarPath(), 100);
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

            // Nested reply indentation and visual line
            int depth = item.getDepthLevel();
            float density = context.getResources().getDisplayMetrics().density;
            int basePaddingStart = (int) (16 * density);
            int indentPerLevel = (int) (24 * density);
            int totalStartPadding = basePaddingStart + (depth * indentPerLevel);

            binding.layoutCommentRoot.setPaddingRelative(
                    totalStartPadding,
                    binding.layoutCommentRoot.getPaddingTop(),
                    binding.layoutCommentRoot.getPaddingEnd(),
                    binding.layoutCommentRoot.getPaddingBottom()
            );

            if (depth > 0) {
                binding.viewCommentNestLine.setVisibility(View.VISIBLE);
                if (item.getReplyToAuthorName() != null) {
                    binding.textCommentReplyTarget.setVisibility(View.VISIBLE);
                    binding.textCommentReplyTarget.setText("Replying to @" + item.getReplyToAuthorName());
                } else {
                    binding.textCommentReplyTarget.setVisibility(View.GONE);
                }
            } else {
                binding.viewCommentNestLine.setVisibility(View.GONE);
                binding.textCommentReplyTarget.setVisibility(View.GONE);
            }

            if (item.isLikedByActivePersona()) {
                binding.imageCommentLikeIcon.setImageResource(R.drawable.ic_heart_filled);
                binding.imageCommentLikeIcon.setColorFilter(ContextCompat.getColor(context, R.color.rose_like));
                binding.textCommentLikes.setTextColor(ContextCompat.getColor(context, R.color.rose_like));
            } else {
                binding.imageCommentLikeIcon.setImageResource(R.drawable.ic_heart_outline);
                binding.imageCommentLikeIcon.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary));
                binding.textCommentLikes.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }

            binding.btnCommentLike.setOnClickListener(v -> {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.imageCommentLikeIcon, View.SCALE_X, 1.0f, 1.35f, 1.0f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.imageCommentLikeIcon, View.SCALE_Y, 1.0f, 1.35f, 1.0f);
                AnimatorSet set = new AnimatorSet();
                set.playTogether(scaleX, scaleY);
                set.setDuration(250);
                set.setInterpolator(new OvershootInterpolator(2.0f));
                set.start();

                listener.onCommentLikeClicked(item);
            });

            binding.btnCommentReply.setOnClickListener(v -> listener.onCommentReplyClicked(item));
        }
    }
}
