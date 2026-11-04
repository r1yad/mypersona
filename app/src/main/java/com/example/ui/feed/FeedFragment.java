package com.example.ui.feed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostWithDetails;
import com.example.databinding.FragmentFeedBinding;
import com.example.ui.persona.PersonaEditActivity;

public class FeedFragment extends Fragment implements FeedAdapter.OnPostClickListener {

    private FragmentFeedBinding binding;
    private FeedViewModel viewModel;
    private FeedAdapter adapter;
    private LinearLayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(FeedViewModel.class);

        adapter = new FeedAdapter(this);
        layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerFeed.setLayoutManager(layoutManager);
        binding.recyclerFeed.setAdapter(adapter);

        // Disable whole-card change animations so reaction animations stay smooth and flicker-free
        RecyclerView.ItemAnimator animator = binding.recyclerFeed.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        viewModel.getActivePersona().observe(getViewLifecycleOwner(), persona -> {
            String activeId = persona != null ? persona.getId() : "";
            adapter.setActivePersonaId(activeId);
        });

        viewModel.getFeedPosts().observe(getViewLifecycleOwner(), posts -> {
            adapter.submitList(posts);
            if (posts == null || posts.isEmpty()) {
                binding.layoutEmptyFeed.setVisibility(View.VISIBLE);
                binding.recyclerFeed.setVisibility(View.GONE);
                binding.fabGoToTop.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyFeed.setVisibility(View.GONE);
                binding.recyclerFeed.setVisibility(View.VISIBLE);
            }
        });

        // Auto-scroll to top when a new post is published
        viewModel.getPostCreatedEvent().observe(getViewLifecycleOwner(), created -> {
            if (Boolean.TRUE.equals(created)) {
                binding.recyclerFeed.post(() -> binding.recyclerFeed.smoothScrollToPosition(0));
            }
        });

        // Floating Go to Top scroll listener
        binding.recyclerFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                if (firstVisible > 1) {
                    if (binding.fabGoToTop.getVisibility() != View.VISIBLE) {
                        binding.fabGoToTop.show();
                    }
                } else {
                    if (binding.fabGoToTop.getVisibility() == View.VISIBLE) {
                        binding.fabGoToTop.hide();
                    }
                }
            }
        });

        binding.fabGoToTop.setOnClickListener(v -> {
            binding.recyclerFeed.smoothScrollToPosition(0);
        });

        binding.fabRefreshFeed.setOnClickListener(v -> {
            viewModel.refresh();
        });

        binding.fabCreatePost.setOnClickListener(v -> {
            CreatePostDialog dialog = new CreatePostDialog();
            dialog.show(getChildFragmentManager(), CreatePostDialog.TAG);
        });

        binding.btnEmptyCreatePersona.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), PersonaEditActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    @Override
    public void onPostClicked(PostWithDetails post) {
        Intent intent = new Intent(requireContext(), PostDetailActivity.class);
        intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getPost().getId());
        startActivity(intent);
    }

    @Override
    public void onLikeClicked(PostWithDetails post) {
        viewModel.toggleLike(post.getPost().getId());
    }

    @Override
    public void onEditClicked(PostWithDetails post) {
        EditPostDialog dialog = EditPostDialog.newInstance(post);
        dialog.setOnPostUpdatedCallback(() -> viewModel.refresh());
        dialog.show(getChildFragmentManager(), EditPostDialog.TAG);
    }

    @Override
    public void onDeleteClicked(PostWithDetails post) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deletePost(post.getPost().getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onAuthorClicked(PersonaEntity author) {
        if (author == null) return;
        Intent intent = new Intent(requireContext(), com.example.ui.persona.PersonaProfileActivity.class);
        intent.putExtra(com.example.ui.persona.PersonaProfileActivity.EXTRA_PERSONA_ID, author.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
