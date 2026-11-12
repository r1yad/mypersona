package com.example.ui.feed;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.entity.CommentWithAuthor;
import com.example.data.entity.PersonaEntity;
import com.example.databinding.ItemCommentBinding;
import com.example.util.LiquidGlassHelper;
import com.example.util.MediaHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommentsAdapter extends ListAdapter<CommentWithAuthor, CommentsAdapter.CommentViewHolder> {

    public interface OnCommentClickListener {
        void onCommentLikeClicked(CommentWithAuthor comment);
        void onCommentReplyClicked(CommentWithAuthor comment);
    }

    private final OnCommentClickListener listener;
    private final List<CommentWithAuthor> fullList = new ArrayList<>();
    private final Set<String> collapsedParentIds = new HashSet<>();
    private final Map<String, Integer> descendantCountMap = new HashMap<>();
    private RecyclerView attachedRecyclerView;

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

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        attachedRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        attachedRecyclerView = null;
    }

    public void expandParent(String parentId) {
        if (parentId != null) {
            collapsedParentIds.remove(parentId);
        }
    }

    public void setFullComments(List<CommentWithAuthor> comments) {
        fullList.clear();
        if (comments != null) {
            fullList.addAll(comments);
        }
        rebuildVisibleList(false);
    }

    private void toggleCollapse(String commentId) {
        if (collapsedParentIds.contains(commentId)) {
            collapsedParentIds.remove(commentId);
        } else {
            collapsedParentIds.add(commentId);
        }
        rebuildVisibleList(true);
    }

    private void rebuildVisibleList(boolean animateTransition) {
        descendantCountMap.clear();
        Map<String, String> parentLookup = new HashMap<>();
        for (CommentWithAuthor item : fullList) {
            String id = item.getComment().getId();
            String parentId = item.getComment().getParentCommentId();
            parentLookup.put(id, parentId);
        }

        // Count total descendants per ancestor comment
        for (CommentWithAuthor item : fullList) {
            String walker = item.getComment().getParentCommentId();
            Set<String> guard = new HashSet<>();
            while (walker != null && !walker.isEmpty() && guard.add(walker)) {
                Integer current = descendantCountMap.get(walker);
                descendantCountMap.put(walker, (current != null ? current : 0) + 1);
                walker = parentLookup.get(walker);
            }
        }

        List<CommentWithAuthor> visible = new ArrayList<>();
        for (CommentWithAuthor item : fullList) {
            boolean hidden = false;
            String walker = item.getComment().getParentCommentId();
            Set<String> guard = new HashSet<>();
            while (walker != null && !walker.isEmpty() && guard.add(walker)) {
                if (collapsedParentIds.contains(walker)) {
                    hidden = true;
                    break;
                }
                walker = parentLookup.get(walker);
            }
            if (!hidden) {
                visible.add(item);
            }
        }

        if (animateTransition && attachedRecyclerView != null &&
                !LiquidGlassHelper.isReducedMotionEnabled(attachedRecyclerView.getContext())) {
            TransitionManager.beginDelayedTransition(attachedRecyclerView);
        }
        submitList(visible);
        notifyDataSetChanged();
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
            LiquidGlassHelper.attachPressFeedback(
                    binding.btnCommentLike,
                    binding.btnCommentReply,
                    binding.btnToggleReplies
            );
        }

        public void bind(CommentWithAuthor item) {
            Context context = binding.getRoot().getContext();
            PersonaEntity author = item.getAuthor();
            if (author != null) {
                binding.textCommentAuthor.setText(author.getDisplayName());
                if (author.getAvatarPath() != null) {
                    Bitmap bitmap = MediaHelper.loadBitmap(context, author.getAvatarPath(), 100);
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

            // Reddit-style multi-level connecting thread lines
            int depth = Math.min(item.getDepthLevel(), 5);
            float density = context.getResources().getDisplayMetrics().density;
            binding.layoutNestLines.removeAllViews();

            if (depth > 0) {
                binding.layoutNestLines.setVisibility(View.VISIBLE);
                int lineWidth = Math.max(2, (int) (2 * density));
                int lineGap = (int) (14 * density);
                int[] lineColors = new int[]{
                        ContextCompat.getColor(context, R.color.glass_border_accent),
                        ContextCompat.getColor(context, R.color.cyan_accent),
                        ContextCompat.getColor(context, R.color.violet_accent),
                        ContextCompat.getColor(context, R.color.glass_border_bright)
                };

                for (int i = 0; i < depth; i++) {
                    View line = new View(context);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            lineWidth,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    );
                    lp.setMarginEnd(i == depth - 1 ? (int) (10 * density) : lineGap);
                    line.setLayoutParams(lp);
                    line.setBackgroundColor(lineColors[i % lineColors.length]);
                    line.setAlpha(i == depth - 1 ? 0.72f : 0.35f);
                    binding.layoutNestLines.addView(line);
                }

                if (item.getReplyToAuthorName() != null) {
                    binding.textCommentReplyTarget.setVisibility(View.VISIBLE);
                    binding.textCommentReplyTarget.setText("Replying to @" + item.getReplyToAuthorName());
                } else {
                    binding.textCommentReplyTarget.setVisibility(View.GONE);
                }
            } else {
                binding.layoutNestLines.setVisibility(View.GONE);
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

            // Expand/Collapse Nested Replies Toggle
            String commentId = item.getComment().getId();
            Integer descendantCountObj = descendantCountMap.get(commentId);
            int descendantCount = descendantCountObj != null ? descendantCountObj : 0;
            if (descendantCount > 0) {
                binding.btnToggleReplies.setVisibility(View.VISIBLE);
                boolean isCollapsed = collapsedParentIds.contains(commentId);
                if (isCollapsed) {
                    binding.btnToggleReplies.setText(descendantCount == 1
                            ? context.getString(R.string.view_replies_one)
                            : context.getString(R.string.view_replies_many, descendantCount));
                } else {
                    binding.btnToggleReplies.setText(R.string.hide_replies);
                }
                binding.btnToggleReplies.setOnClickListener(v -> toggleCollapse(commentId));
            } else {
                binding.btnToggleReplies.setVisibility(View.GONE);
            }

            binding.btnCommentLike.setOnClickListener(v -> {
                if (!LiquidGlassHelper.isReducedMotionEnabled(context)) {
                    binding.imageCommentLikeIcon.animate().cancel();
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.imageCommentLikeIcon, View.SCALE_X, 1.0f, 1.35f, 1.0f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.imageCommentLikeIcon, View.SCALE_Y, 1.0f, 1.35f, 1.0f);
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(scaleX, scaleY);
                    set.setDuration(250);
                    set.setInterpolator(new OvershootInterpolator(2.0f));
                    set.start();
                }
                listener.onCommentLikeClicked(item);
            });

            binding.btnCommentReply.setOnClickListener(v -> listener.onCommentReplyClicked(item));
        }
    }
}
