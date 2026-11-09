package com.example.ui.persona;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.R;
import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostWithDetails;
import com.example.data.repository.AppRepository;
import com.example.databinding.FragmentMyProfileBinding;
import com.example.ui.feed.EditPostDialog;
import com.example.ui.feed.FeedAdapter;
import com.example.ui.feed.PostDetailActivity;
import com.example.util.MediaHelper;

import java.util.List;

public class MyProfileFragment extends Fragment {

    private FragmentMyProfileBinding binding;
    private AppRepository repository;
    private FeedAdapter feedAdapter;
    private PersonaEntity activePersona;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = AppRepository.getInstance(requireContext());

        feedAdapter = new FeedAdapter(new FeedAdapter.OnPostClickListener() {
            @Override
            public void onPostClicked(PostWithDetails post) {
                Intent intent = new Intent(requireContext(), PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getPost().getId());
                startActivity(intent);
            }

            @Override
            public void onLikeClicked(PostWithDetails post) {
                repository.togglePostLike(post.getPost().getId(), () -> loadActivePersonaPosts());
            }

            @Override
            public void onEditClicked(PostWithDetails post) {
                EditPostDialog dialog = EditPostDialog.newInstance(post);
                dialog.setOnPostUpdatedCallback(() -> loadActivePersonaPosts());
                dialog.show(getChildFragmentManager(), EditPostDialog.TAG);
            }

            @Override
            public void onDeleteClicked(PostWithDetails post) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Delete Post")
                        .setMessage("Are you sure you want to permanently delete this post? This action cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            repository.deletePost(post.getPost().getId(), () -> loadActivePersonaPosts());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onAuthorClicked(PersonaEntity author) {
                // Already on profile
            }
        });

        binding.recyclerMyPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerMyPosts.setAdapter(feedAdapter);

        binding.btnMyProfileEdit.setOnClickListener(v -> {
            if (activePersona != null) {
                Intent intent = new Intent(requireContext(), PersonaEditActivity.class);
                intent.putExtra(PersonaEditActivity.EXTRA_PERSONA_ID, activePersona.getId());
                startActivity(intent);
            }
        });

        binding.btnMyProfileSwitch.setOnClickListener(v -> {
            PersonaSwitcherBottomSheet.newInstance().show(getParentFragmentManager(), PersonaSwitcherBottomSheet.TAG);
        });

        repository.getActivePersonaLive().observe(getViewLifecycleOwner(), persona -> {
            activePersona = persona;
            if (persona != null) {
                feedAdapter.setActivePersonaId(persona.getId());
                displayPersona(persona);
                loadActivePersonaPosts();
            } else {
                displayEmptyPersona();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (activePersona != null) {
            loadActivePersonaPosts();
        }
    }

    private void displayPersona(PersonaEntity persona) {
        binding.textMyProfileName.setText(persona.getDisplayName());
        binding.textMyProfileUsername.setText("@" + persona.getUsername());
        binding.textMyProfileBadge.setVisibility(View.VISIBLE);

        if (persona.getBio() != null && !persona.getBio().trim().isEmpty()) {
            binding.textMyProfileBio.setText(persona.getBio());
            binding.textMyProfileBio.setVisibility(View.VISIBLE);
        } else {
            binding.textMyProfileBio.setVisibility(View.GONE);
        }

        if (persona.getAvatarPath() != null) {
            Bitmap bm = MediaHelper.loadBitmap(requireContext(), persona.getAvatarPath(), 180);
            if (bm != null) {
                binding.imageMyProfileAvatar.setImageBitmap(bm);
            } else {
                binding.imageMyProfileAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }
        } else {
            binding.imageMyProfileAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }

        if (persona.getPersonalityTraits() != null && !persona.getPersonalityTraits().trim().isEmpty()) {
            binding.textMyProfileTraits.setText("Traits: " + persona.getPersonalityTraits());
            binding.textMyProfileTraits.setVisibility(View.VISIBLE);
        } else {
            binding.textMyProfileTraits.setVisibility(View.GONE);
        }

        if (persona.getSpeakingStyle() != null && !persona.getSpeakingStyle().trim().isEmpty()) {
            binding.textMyProfileStyle.setText("Voice: " + persona.getSpeakingStyle());
            binding.textMyProfileStyle.setVisibility(View.VISIBLE);
        } else {
            binding.textMyProfileStyle.setVisibility(View.GONE);
        }
    }

    private void displayEmptyPersona() {
        binding.textMyProfileName.setText("No Active Persona");
        binding.textMyProfileUsername.setText("@none");
        binding.textMyProfileBadge.setVisibility(View.GONE);
        binding.textMyProfileBio.setText("Switch or create a persona to manage your profile.");
        binding.imageMyProfileAvatar.setImageResource(R.drawable.ic_person_placeholder);
        binding.textMyProfileTraits.setVisibility(View.GONE);
        binding.textMyProfileStyle.setVisibility(View.GONE);
        feedAdapter.submitList(null);
        binding.textMyPostsCount.setText("0 posts");
        binding.layoutEmptyMyPosts.setVisibility(View.VISIBLE);
    }

    private void loadActivePersonaPosts() {
        if (activePersona == null) return;
        repository.loadPostsByAuthor(activePersona.getId(), posts -> {
            if (binding == null) return;
            if (posts != null && !posts.isEmpty()) {
                feedAdapter.submitList(posts);
                binding.textMyPostsCount.setText(posts.size() + (posts.size() == 1 ? " post" : " posts"));
                binding.layoutEmptyMyPosts.setVisibility(View.GONE);
            } else {
                feedAdapter.submitList(null);
                binding.textMyPostsCount.setText("0 posts");
                binding.layoutEmptyMyPosts.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
