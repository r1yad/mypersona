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

import com.example.data.entity.PostWithDetails;
import com.example.databinding.FragmentFeedBinding;
import com.example.ui.persona.PersonaEditActivity;

public class FeedFragment extends Fragment implements FeedAdapter.OnPostClickListener {

    private FragmentFeedBinding binding;
    private FeedViewModel viewModel;
    private FeedAdapter adapter;

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
        binding.recyclerFeed.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerFeed.setAdapter(adapter);

        viewModel.getActivePersona().observe(getViewLifecycleOwner(), persona -> {
            String activeId = persona != null ? persona.getId() : "";
            adapter.setActivePersonaId(activeId);
        });

        viewModel.getFeedPosts().observe(getViewLifecycleOwner(), posts -> {
            adapter.submitList(posts);
            if (posts == null || posts.isEmpty()) {
                binding.layoutEmptyFeed.setVisibility(View.VISIBLE);
                binding.recyclerFeed.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyFeed.setVisibility(View.GONE);
                binding.recyclerFeed.setVisibility(View.VISIBLE);
            }
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
    public void onDeleteClicked(PostWithDetails post) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deletePost(post.getPost().getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onAuthorClicked(com.example.data.entity.PersonaEntity author) {
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
