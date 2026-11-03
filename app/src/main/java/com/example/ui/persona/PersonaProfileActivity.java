package com.example.ui.persona;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.R;
import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostWithDetails;
import com.example.data.repository.AppRepository;
import com.example.databinding.ActivityPersonaProfileBinding;
import com.example.ui.chat.ChatActivity;
import com.example.ui.feed.FeedAdapter;
import com.example.ui.feed.PostDetailActivity;
import com.example.util.MediaHelper;

import java.io.File;
import java.util.List;

public class PersonaProfileActivity extends AppCompatActivity implements FeedAdapter.OnPostClickListener {

    public static final String EXTRA_PERSONA_ID = "extra_persona_id";

    private ActivityPersonaProfileBinding binding;
    private AppRepository repository;
    private String personaId;
    private PersonaEntity currentPersona;
    private PersonaEntity activePersona;
    private FeedAdapter feedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonaProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = AppRepository.getInstance(this);

        personaId = getIntent().getStringExtra(EXTRA_PERSONA_ID);
        if (personaId == null || personaId.isEmpty()) {
            PersonaEntity active = repository.getActivePersonaLive().getValue();
            if (active != null) {
                personaId = active.getId();
            } else {
                finish();
                return;
            }
        }

        binding.toolbarProfile.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarProfile, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });

        feedAdapter = new FeedAdapter(this);
        binding.recyclerProfilePosts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerProfilePosts.setAdapter(feedAdapter);

        binding.btnProfileEdit.setOnClickListener(v -> {
            if (currentPersona != null) {
                Intent intent = new Intent(this, PersonaEditActivity.class);
                intent.putExtra(PersonaEditActivity.EXTRA_PERSONA_ID, currentPersona.getId());
                startActivity(intent);
            }
        });

        binding.btnProfileSetActive.setOnClickListener(v -> {
            if (currentPersona != null) {
                repository.setActivePersona(currentPersona);
                Toast.makeText(this, "Active persona set to " + currentPersona.getDisplayName(), Toast.LENGTH_SHORT).show();
                updateActiveState();
            }
        });

        binding.btnProfileMessage.setOnClickListener(v -> startChatWithPersona());

        repository.getActivePersonaLive().observe(this, active -> {
            activePersona = active;
            String activeId = active != null ? active.getId() : "";
            feedAdapter.setActivePersonaId(activeId);
            updateActiveState();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPersonaAndPosts();
    }

    private void loadPersonaAndPosts() {
        repository.loadPersonaById(personaId, persona -> {
            if (persona == null) {
                finish();
                return;
            }
            currentPersona = persona;
            displayPersonaInfo(persona);
            loadAuthorPosts();
        });
    }

    private void displayPersonaInfo(PersonaEntity persona) {
        binding.toolbarProfile.setTitle(persona.getDisplayName());
        binding.textProfileName.setText(persona.getDisplayName());
        binding.textProfileUsername.setText("@" + persona.getUsername());

        if (!persona.getBio().isEmpty()) {
            binding.textProfileBio.setText(persona.getBio());
            binding.textProfileBio.setVisibility(View.VISIBLE);
        } else {
            binding.textProfileBio.setVisibility(View.GONE);
        }

        if (persona.getAvatarPath() != null) {
            Bitmap bm = MediaHelper.loadBitmap(this, persona.getAvatarPath(), 400);
            if (bm != null) {
                binding.imageProfileAvatar.setImageBitmap(bm);
            } else {
                binding.imageProfileAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }
        } else {
            binding.imageProfileAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }

        // Traits and style
        if (!persona.getPersonalityTraits().isEmpty()) {
            binding.textProfileTraits.setText("✦ Personality: " + persona.getPersonalityTraits());
            binding.textProfileTraits.setVisibility(View.VISIBLE);
        } else {
            binding.textProfileTraits.setVisibility(View.GONE);
        }

        if (!persona.getSpeakingStyle().isEmpty()) {
            binding.textProfileStyle.setText("✦ Speaking Style: " + persona.getSpeakingStyle());
            binding.textProfileStyle.setVisibility(View.VISIBLE);
        } else {
            binding.textProfileStyle.setVisibility(View.GONE);
        }

        if (!persona.getBackgroundStory().isEmpty()) {
            binding.textProfileStory.setText(persona.getBackgroundStory());
            binding.textProfileStory.setVisibility(View.VISIBLE);
        } else if (!persona.getRoleplayInstructions().isEmpty()) {
            binding.textProfileStory.setText(persona.getRoleplayInstructions());
            binding.textProfileStory.setVisibility(View.VISIBLE);
        } else {
            binding.textProfileStory.setVisibility(View.GONE);
        }

        updateActiveState();
    }

    private void updateActiveState() {
        if (currentPersona == null) return;
        boolean isActive = activePersona != null && currentPersona.getId().equals(activePersona.getId());

        if (isActive) {
            binding.textProfileActiveBadge.setVisibility(View.VISIBLE);
            binding.btnProfileSetActive.setVisibility(View.GONE);
        } else {
            binding.textProfileActiveBadge.setVisibility(View.GONE);
            binding.btnProfileSetActive.setVisibility(View.VISIBLE);
        }
    }

    private void loadAuthorPosts() {
        repository.loadPostsByAuthor(personaId, posts -> {
            feedAdapter.submitList(posts);
            int count = posts != null ? posts.size() : 0;
            binding.textPostsCountBadge.setText(count + (count == 1 ? " post" : " posts"));

            if (posts == null || posts.isEmpty()) {
                binding.layoutEmptyProfilePosts.setVisibility(View.VISIBLE);
                binding.recyclerProfilePosts.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyProfilePosts.setVisibility(View.GONE);
                binding.recyclerProfilePosts.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startChatWithPersona() {
        if (activePersona == null) {
            Toast.makeText(this, "Please select an active persona first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (activePersona.getId().equals(personaId)) {
            Toast.makeText(this, "This is your currently active persona", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.startOrGetConversation(activePersona.getId(), personaId, conv -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conv.getId());
            startActivity(intent);
        });
    }

    @Override
    public void onPostClicked(PostWithDetails post) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getPost().getId());
        startActivity(intent);
    }

    @Override
    public void onLikeClicked(PostWithDetails post) {
        repository.togglePostLike(post.getPost().getId(), this::loadAuthorPosts);
    }

    @Override
    public void onDeleteClicked(PostWithDetails post) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deletePost(post.getPost().getId(), this::loadAuthorPosts);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onAuthorClicked(PersonaEntity author) {
        if (author != null && !author.getId().equals(personaId)) {
            Intent intent = new Intent(this, PersonaProfileActivity.class);
            intent.putExtra(PersonaProfileActivity.EXTRA_PERSONA_ID, author.getId());
            startActivity(intent);
        }
    }
}
