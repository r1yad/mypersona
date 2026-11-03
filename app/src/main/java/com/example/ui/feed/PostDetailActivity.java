package com.example.ui.feed;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.R;
import com.example.assistant.RoleplayAssistant;
import com.example.data.entity.CommentWithAuthor;
import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostWithDetails;
import com.example.data.repository.AppRepository;
import com.example.databinding.ActivityPostDetailBinding;

import java.io.File;

public class PostDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "extra_post_id";

    private ActivityPostDetailBinding binding;
    private AppRepository repository;
    private String postId;
    private PostWithDetails currentPost;
    private PersonaEntity currentActivePersona;
    private CommentsAdapter commentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (postId == null || postId.isEmpty()) {
            finish();
            return;
        }

        repository = AppRepository.getInstance(this);

        commentsAdapter = new CommentsAdapter(this::onCommentLike);
        binding.recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerComments.setAdapter(commentsAdapter);

        binding.btnBack.setOnClickListener(v -> finish());

        repository.getActivePersonaLive().observe(this, persona -> {
            currentActivePersona = persona;
            if (persona != null) {
                binding.editComment.setHint("Reply as " + persona.getDisplayName() + "…");
            } else {
                binding.editComment.setHint("Select an active persona to reply…");
            }
        });

        // Roleplay Assistant reply suggestion
        binding.btnRoleplayReplyPrompt.setOnClickListener(v -> {
            if (currentActivePersona == null) {
                Toast.makeText(this, "Please select an active persona first", Toast.LENGTH_SHORT).show();
                return;
            }
            String postBody = currentPost != null ? currentPost.getPost().getTextContent() : "";
            String idea = RoleplayAssistant.generateReplyIdea(currentActivePersona, postBody);
            binding.editComment.setText(idea);
            binding.editComment.setSelection(idea.length());
        });

        binding.btnSendComment.setOnClickListener(v -> sendComment());

        loadPostAndComments();
    }

    private void loadPostAndComments() {
        repository.loadPostDetails(postId, post -> {
            if (post == null) {
                finish();
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
                File file = new File(author.getAvatarPath());
                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bitmap != null) {
                        binding.imageDetailAvatar.setImageBitmap(bitmap);
                    } else {
                        binding.imageDetailAvatar.setImageResource(R.drawable.ic_person_placeholder);
                    }
                } else {
                    binding.imageDetailAvatar.setImageResource(R.drawable.ic_person_placeholder);
                }
            } else {
                binding.imageDetailAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }

            binding.layoutAuthorHeader.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.example.ui.persona.PersonaProfileActivity.class);
                intent.putExtra(com.example.ui.persona.PersonaProfileActivity.EXTRA_PERSONA_ID, author.getId());
                startActivity(intent);
            });
        }

        binding.textDetailContent.setText(item.getPost().getTextContent());

        if (!item.getMediaList().isEmpty()) {
            String path = item.getMediaList().get(0).getMediaPath();
            File img = new File(path);
            if (img.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(img.getAbsolutePath());
                if (bm != null) {
                    binding.imageDetailMedia.setImageBitmap(bm);
                    binding.imageDetailMedia.setVisibility(View.VISIBLE);
                } else {
                    binding.imageDetailMedia.setVisibility(View.GONE);
                }
            } else {
                binding.imageDetailMedia.setVisibility(View.GONE);
            }
        } else {
            binding.imageDetailMedia.setVisibility(View.GONE);
        }
    }

    private void loadCommentsList() {
        repository.loadComments(postId, list -> commentsAdapter.submitList(list));
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

        binding.btnSendComment.setEnabled(false);
        repository.addComment(postId, currentActivePersona.getId(), null, text, () -> {
            binding.editComment.setText("");
            binding.btnSendComment.setEnabled(true);
            loadCommentsList();
        });
    }
}
