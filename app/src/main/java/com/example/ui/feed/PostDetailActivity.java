package com.example.ui.feed;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.R;
import com.example.assistant.RoleplayAssistant;
import com.example.data.entity.CommentWithAuthor;
import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostMediaEntity;
import com.example.data.entity.PostWithDetails;
import com.example.data.repository.AppRepository;
import com.example.databinding.ActivityPostDetailBinding;
import com.example.ui.persona.PersonaProfileActivity;
import com.example.util.LiquidGlassHelper;
import com.example.util.MediaHelper;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "extra_post_id";

    private ActivityPostDetailBinding binding;
    private AppRepository repository;
    private String postId;
    private PostWithDetails currentPost;
    private PersonaEntity currentActivePersona;
    private CommentsAdapter commentsAdapter;
    private CommentWithAuthor replyingToComment = null;
    private ViewPager2.OnPageChangeCallback detailPageCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LiquidGlassHelper.enableEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (postId == null || postId.isEmpty()) {
            finish();
            return;
        }

        repository = AppRepository.getInstance(this);

        LiquidGlassHelper.applyTopInsets(binding.topBar);
        LiquidGlassHelper.applyImeBottomInsets(binding.layoutCommentInputContainer);
        binding.layoutDetailMediaContainer.setClipToOutline(true);
        binding.viewPagerDetailMedia.setPageTransformer(LiquidGlassHelper.createSmoothSliderTransformer());

        LiquidGlassHelper.attachPressFeedback(
                binding.btnBack,
                binding.btnDetailEdit,
                binding.btnDetailDelete,
                binding.btnDetailLike,
                binding.btnDetailComments,
                binding.btnRoleplayReplyPrompt,
                binding.btnSendComment,
                binding.btnCancelReply
        );

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                LiquidGlassHelper.finishWithGlassTransition(PostDetailActivity.this);
            }
        });

        commentsAdapter = new CommentsAdapter(new CommentsAdapter.OnCommentClickListener() {
            @Override
            public void onCommentLikeClicked(CommentWithAuthor comment) {
                onCommentLike(comment);
            }

            @Override
            public void onCommentReplyClicked(CommentWithAuthor comment) {
                setReplyingTo(comment);
            }
        });

        binding.recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerComments.setAdapter(commentsAdapter);
        binding.recyclerComments.setNestedScrollingEnabled(false);

        binding.btnBack.setOnClickListener(v -> LiquidGlassHelper.finishWithGlassTransition(this));

        repository.getActivePersonaLive().observe(this, persona -> {
            currentActivePersona = persona;
            updateInputHint();
            if (currentPost != null) {
                updateOwnerActions();
            }
        });

        // Roleplay Assistant reply suggestion
        binding.btnRoleplayReplyPrompt.setOnClickListener(v -> {
            if (currentActivePersona == null) {
                Toast.makeText(this, "Please select an active persona first", Toast.LENGTH_SHORT).show();
                return;
            }
            LiquidGlassHelper.pulseView(v);
            String postBody = currentPost != null ? currentPost.getPost().getTextContent() : "";
            String idea = RoleplayAssistant.generateReplyIdea(currentActivePersona, postBody);
            binding.editComment.setText(idea);
            binding.editComment.setSelection(idea.length());
        });

        binding.btnCancelReply.setOnClickListener(v -> cancelReply());
        binding.btnSendComment.setOnClickListener(v -> sendComment());

        loadPostAndComments();
    }

    private void updateInputHint() {
        if (replyingToComment != null && replyingToComment.getAuthor() != null) {
            binding.editComment.setHint("Replying to @" + replyingToComment.getAuthor().getUsername() + "…");
        } else if (currentActivePersona != null) {
            binding.editComment.setHint("Reply as " + currentActivePersona.getDisplayName() + "…");
        } else {
            binding.editComment.setHint("Select an active persona to reply…");
        }
    }

    private void setReplyingTo(CommentWithAuthor comment) {
        replyingToComment = comment;
        if (comment != null && comment.getAuthor() != null) {
            binding.layoutReplyingBanner.setVisibility(View.VISIBLE);
            binding.textReplyingBanner.setText("Replying to " + comment.getAuthor().getDisplayName()
                    + " (@" + comment.getAuthor().getUsername() + ")");
            updateInputHint();
            binding.editComment.requestFocus();
        } else {
            cancelReply();
        }
    }

    private void cancelReply() {
        replyingToComment = null;
        binding.layoutReplyingBanner.setVisibility(View.GONE);
        updateInputHint();
    }

    private void loadPostAndComments() {
        repository.loadPostDetails(postId, post -> {
            if (post == null) {
                LiquidGlassHelper.finishWithGlassTransition(this);
                return;
            }
            currentPost = post;
            displayPostDetails(post);
            loadCommentsList();
        });
    }

    private void displayPostDetails(PostWithDetails item) {
        PersonaEntity author = item.getAuthor();
        if (author != null) {
            binding.textDetailAuthor.setText(author.getDisplayName());
            CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(
                    item.getPost().getCreatedAt(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            binding.textDetailHandleTime.setText("@" + author.getUsername() + " • " + timeSpan);

            if (author.getAvatarPath() != null) {
                Bitmap bitmap = MediaHelper.loadBitmap(this, author.getAvatarPath(), 120);
                if (bitmap != null) {
                    binding.imageDetailAvatar.setImageBitmap(bitmap);
                } else {
                    binding.imageDetailAvatar.setImageResource(R.drawable.ic_person_placeholder);
                }
            } else {
                binding.imageDetailAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }

            binding.layoutAuthorHeader.setOnClickListener(v -> {
                Intent intent = new Intent(this, PersonaProfileActivity.class);
                intent.putExtra(PersonaProfileActivity.EXTRA_PERSONA_ID, author.getId());
                LiquidGlassHelper.startActivityWithGlassTransition(this, intent);
            });
        }

        String text = item.getPost().getTextContent();
        if (text != null && !text.trim().isEmpty()) {
            binding.textDetailContent.setVisibility(View.VISIBLE);
            binding.textDetailContent.setText(text);
        } else {
            binding.textDetailContent.setVisibility(View.GONE);
        }

        binding.textDetailLikes.setText(String.valueOf(item.getLikeCount()));
        binding.textDetailComments.setText(String.valueOf(item.getCommentCount()));

        updateLikeUi(item.isLikedByActivePersona());
        updateOwnerActions();

        binding.btnDetailLike.setOnClickListener(v -> {
            animateLikeIcon(binding.imageDetailLikeIcon);
            repository.togglePostLike(postId, this::loadPostAndComments);
        });

        binding.btnDetailComments.setOnClickListener(v -> {
            binding.scrollPostDetail.smoothScrollTo(0, binding.layoutCommentsHeader.getTop());
            binding.editComment.requestFocus();
        });

        if (detailPageCallback != null) {
            binding.viewPagerDetailMedia.unregisterOnPageChangeCallback(detailPageCallback);
            detailPageCallback = null;
        }

        // Attached Media Slider (ViewPager2)
        List<PostMediaEntity> mediaList = item.getMediaList();
        if (mediaList != null && !mediaList.isEmpty()) {
            binding.layoutDetailMediaContainer.setVisibility(View.VISIBLE);
            List<String> paths = new ArrayList<>();
            for (PostMediaEntity m : mediaList) {
                paths.add(m.getMediaPath());
            }

            int currentPos = binding.viewPagerDetailMedia.getCurrentItem();
            PostImagesSliderAdapter adapter = new PostImagesSliderAdapter(paths, (clickedPos, allImages) -> {
                FullscreenImageViewerActivity.start(this, allImages, clickedPos);
            });
            binding.viewPagerDetailMedia.setAdapter(adapter);
            int clampedPos = (currentPos >= 0 && currentPos < paths.size()) ? currentPos : 0;
            binding.viewPagerDetailMedia.setCurrentItem(clampedPos, false);

            int total = paths.size();
            if (total > 1) {
                binding.textDetailMediaCounter.setVisibility(View.VISIBLE);
                binding.textDetailMediaCounter.setText((clampedPos + 1) + "/" + total);
                binding.layoutDetailMediaDots.setVisibility(View.VISIBLE);
                setupDots(binding.layoutDetailMediaDots, total, clampedPos);

                detailPageCallback = new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        binding.textDetailMediaCounter.setText((position + 1) + "/" + total);
                        updateDots(binding.layoutDetailMediaDots, position);
                    }
                };
                binding.viewPagerDetailMedia.registerOnPageChangeCallback(detailPageCallback);
            } else {
                binding.textDetailMediaCounter.setVisibility(View.GONE);
                binding.layoutDetailMediaDots.setVisibility(View.GONE);
            }
        } else {
            binding.layoutDetailMediaContainer.setVisibility(View.GONE);
        }
    }

    private void updateLikeUi(boolean isLiked) {
        if (isLiked) {
            binding.imageDetailLikeIcon.setImageResource(R.drawable.ic_heart_filled);
            binding.imageDetailLikeIcon.setColorFilter(ContextCompat.getColor(this, R.color.rose_like));
            binding.textDetailLikes.setTextColor(ContextCompat.getColor(this, R.color.rose_like));
        } else {
            binding.imageDetailLikeIcon.setImageResource(R.drawable.ic_heart_outline);
            binding.imageDetailLikeIcon.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));
            binding.textDetailLikes.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void updateOwnerActions() {
        if (currentPost == null) return;
        PersonaEntity author = currentPost.getAuthor();
        boolean isOwner = author != null && currentActivePersona != null && author.getId().equals(currentActivePersona.getId());
        if (isOwner) {
            binding.btnDetailEdit.setVisibility(View.VISIBLE);
            binding.btnDetailEdit.setOnClickListener(v -> {
                if (!LiquidGlassHelper.canNavigate()) return;
                EditPostDialog dialog = EditPostDialog.newInstance(currentPost);
                dialog.setOnPostUpdatedCallback(this::loadPostAndComments);
                dialog.show(getSupportFragmentManager(), EditPostDialog.TAG);
            });

            binding.btnDetailDelete.setVisibility(View.VISIBLE);
            binding.btnDetailDelete.setOnClickListener(v -> {
                LiquidGlassHelper.showGlassConfirmDialog(
                        this,
                        getString(R.string.delete_post_title),
                        getString(R.string.delete_post_message),
                        getString(R.string.delete),
                        true,
                        () -> repository.deletePost(postId, () -> {
                            Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show();
                            LiquidGlassHelper.finishWithGlassTransition(this);
                        })
                );
            });
        } else {
            binding.btnDetailEdit.setVisibility(View.GONE);
            binding.btnDetailDelete.setVisibility(View.GONE);
        }
    }

    private void animateLikeIcon(View iconView) {
        if (LiquidGlassHelper.isReducedMotionEnabled(this)) return;
        iconView.animate().cancel();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1.0f, 1.4f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1.0f, 1.4f, 1.0f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(260);
        set.setInterpolator(new OvershootInterpolator(2.0f));
        set.start();
    }

    private void setupDots(LinearLayout dotsLayout, int count, int selectedPosition) {
        dotsLayout.removeAllViews();
        int margin = (int) (3 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
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

    private void loadCommentsList() {
        repository.loadComments(postId, list -> {
            commentsAdapter.setFullComments(list);
            if (binding == null) return;
            int count = list != null ? list.size() : 0;
            binding.textDetailComments.setText(String.valueOf(count));
            binding.textCommentsHeaderCount.setText(count + (count == 1 ? " comment" : " comments"));
            if (count == 0) {
                binding.layoutEmptyComments.setVisibility(View.VISIBLE);
                binding.recyclerComments.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyComments.setVisibility(View.GONE);
                binding.recyclerComments.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onCommentLike(CommentWithAuthor comment) {
        if (currentActivePersona == null) {
            Toast.makeText(this, "Please select an active persona first", Toast.LENGTH_SHORT).show();
            return;
        }
        repository.toggleCommentLike(comment.getComment().getId(), this::loadCommentsList);
    }

    private void sendComment() {
        if (currentActivePersona == null) {
            Toast.makeText(this, "Please select an active persona first", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = binding.editComment.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }

        String parentId = replyingToComment != null ? replyingToComment.getComment().getId() : null;

        binding.btnSendComment.setEnabled(false);
        repository.addComment(postId, currentActivePersona.getId(), parentId, text, () -> {
            binding.editComment.setText("");
            binding.btnSendComment.setEnabled(true);
            if (parentId != null) {
                commentsAdapter.expandParent(parentId);
            }
            cancelReply();
            loadCommentsList();
            binding.scrollPostDetail.post(() -> binding.scrollPostDetail.fullScroll(View.FOCUS_DOWN));
        });
    }
}
