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
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.R;
import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostMediaEntity;
import com.example.data.entity.PostWithDetails;
import com.example.databinding.ItemPostCardBinding;
import com.example.util.LiquidGlassHelper;
import com.example.util.MediaHelper;

import java.util.ArrayList;
import java.util.List;

public class FeedAdapter extends ListAdapter<PostWithDetails, FeedAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClicked(PostWithDetails post);
        void onLikeClicked(PostWithDetails post);
        void onEditClicked(PostWithDetails post);
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
                boolean sameMedia = oldItem.getMediaList().size() == newItem.getMediaList().size();
                return sameMedia &&
                        oldItem.getPost().getUpdatedAt() == newItem.getPost().getUpdatedAt() &&
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
        private ViewPager2.OnPageChangeCallback activePageCallback;

        public PostViewHolder(ItemPostCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.layoutPostMediaContainer.setClipToOutline(true);
            binding.viewPagerPostMedia.setPageTransformer(LiquidGlassHelper.createSmoothSliderTransformer());
            LiquidGlassHelper.attachPressFeedback(
                    binding.btnPostLike,
                    binding.btnPostComment,
                    binding.btnPostEdit,
                    binding.btnPostDelete
            );
        }

        public void bind(PostWithDetails item) {
            Context context = binding.getRoot().getContext();
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
                    Bitmap bitmap = MediaHelper.loadBitmap(context, author.getAvatarPath(), 120);
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

            String text = item.getPost().getTextContent();
            if (text != null && !text.trim().isEmpty()) {
                binding.textPostContent.setVisibility(View.VISIBLE);
                binding.textPostContent.setText(text);
            } else {
                binding.textPostContent.setVisibility(View.GONE);
            }

            binding.textPostLikes.setText(String.valueOf(item.getLikeCount()));
            binding.textPostComments.setText(String.valueOf(item.getCommentCount()));

            if (item.isLikedByActivePersona()) {
                binding.imagePostLikeIcon.setImageResource(R.drawable.ic_heart_filled);
                binding.imagePostLikeIcon.setColorFilter(ContextCompat.getColor(context, R.color.rose_like));
                binding.textPostLikes.setTextColor(ContextCompat.getColor(context, R.color.rose_like));
            } else {
                binding.imagePostLikeIcon.setImageResource(R.drawable.ic_heart_outline);
                binding.imagePostLikeIcon.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary));
                binding.textPostLikes.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }

            // Unregister previous ViewPager2 callback before rebinding to prevent leaks/jitter
            if (activePageCallback != null) {
                binding.viewPagerPostMedia.unregisterOnPageChangeCallback(activePageCallback);
                activePageCallback = null;
            }

            // Attached Media Slider (ViewPager2)
            List<PostMediaEntity> mediaList = item.getMediaList();
            if (mediaList != null && !mediaList.isEmpty()) {
                binding.layoutPostMediaContainer.setVisibility(View.VISIBLE);
                List<String> paths = new ArrayList<>();
                for (PostMediaEntity m : mediaList) {
                    paths.add(m.getMediaPath());
                }

                PostImagesSliderAdapter sliderAdapter = new PostImagesSliderAdapter(paths, (clickedPos, allImages) -> {
                    FullscreenImageViewerActivity.start(context, allImages, clickedPos);
                });
                binding.viewPagerPostMedia.setAdapter(sliderAdapter);

                int totalCount = paths.size();
                if (totalCount > 1) {
                    binding.textMediaCounter.setVisibility(View.VISIBLE);
                    binding.textMediaCounter.setText("1/" + totalCount);
                    binding.layoutMediaDots.setVisibility(View.VISIBLE);
                    setupDots(binding.layoutMediaDots, totalCount, 0);

                    activePageCallback = new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            super.onPageSelected(position);
                            binding.textMediaCounter.setText((position + 1) + "/" + totalCount);
                            updateDots(binding.layoutMediaDots, position);
                        }
                    };
                    binding.viewPagerPostMedia.registerOnPageChangeCallback(activePageCallback);
                } else {
                    binding.textMediaCounter.setVisibility(View.GONE);
                    binding.layoutMediaDots.setVisibility(View.GONE);
                }
            } else {
                binding.layoutPostMediaContainer.setVisibility(View.GONE);
            }

            // Edit & Delete button visible if author matches active persona
            boolean isOwner = author != null && author.getId().equals(activePersonaId);
            if (isOwner) {
                binding.btnPostEdit.setVisibility(View.VISIBLE);
                binding.btnPostEdit.setOnClickListener(v -> listener.onEditClicked(item));
                binding.btnPostDelete.setVisibility(View.VISIBLE);
                binding.btnPostDelete.setOnClickListener(v -> listener.onDeleteClicked(item));
            } else {
                binding.btnPostEdit.setVisibility(View.GONE);
                binding.btnPostDelete.setVisibility(View.GONE);
            }

            if (author != null) {
                binding.imagePostAvatar.setOnClickListener(v -> listener.onAuthorClicked(author));
                binding.textPostAuthor.setOnClickListener(v -> listener.onAuthorClicked(author));
                binding.textPostHandleTime.setOnClickListener(v -> listener.onAuthorClicked(author));
            }

            binding.cardPost.setOnClickListener(v -> listener.onPostClicked(item));

            // Reaction button with icon-only bounce animation (never animates parent card)
            binding.btnPostLike.setOnClickListener(v -> {
                animateLikeButton(binding.imagePostLikeIcon);
                listener.onLikeClicked(item);
            });

            binding.btnPostComment.setOnClickListener(v -> listener.onPostClicked(item));
        }

        private void animateLikeButton(View iconView) {
            if (LiquidGlassHelper.isReducedMotionEnabled(iconView.getContext())) {
                return;
            }
            iconView.animate().cancel();
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1.0f, 1.38f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1.0f, 1.38f, 1.0f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(scaleX, scaleY);
            set.setDuration(260);
            set.setInterpolator(new OvershootInterpolator(2.0f));
            set.start();
        }

        private void setupDots(LinearLayout dotsLayout, int count, int selectedPosition) {
            dotsLayout.removeAllViews();
            Context ctx = dotsLayout.getContext();
            int margin = (int) (3 * ctx.getResources().getDisplayMetrics().density);

            for (int i = 0; i < count; i++) {
                ImageView dot = new ImageView(ctx);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lp.setMargins(margin, 0, margin, 0);
                dot.setLayoutParams(lp);
                dot.setImageResource(i == selectedPosition ? R.drawable.dot_indicator_active : R.drawable.dot_indicator_inactive);
                dotsLayout.addView(dot);
            }
        }

        private void updateDots(LinearLayout dotsLayout, int selectedPosition) {
            int childCount = dotsLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = dotsLayout.getChildAt(i);
                if (child instanceof ImageView) {
                    ((ImageView) child).setImageResource(i == selectedPosition ? R.drawable.dot_indicator_active : R.drawable.dot_indicator_inactive);
                }
            }
        }
    }
}
