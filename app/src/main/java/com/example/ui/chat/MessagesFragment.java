package com.example.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.data.entity.ConversationWithDetails;
import com.example.databinding.FragmentMessagesBinding;
import com.example.util.LiquidGlassHelper;

public class MessagesFragment extends Fragment implements ConversationsAdapter.OnConversationClickListener {

    private FragmentMessagesBinding binding;
    private ChatViewModel viewModel;
    private ConversationsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMessagesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        adapter = new ConversationsAdapter(this);
        binding.recyclerConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerConversations.setAdapter(adapter);

        LiquidGlassHelper.attachPressFeedback(binding.fabNewConversation, binding.btnEmptyStartChat);

        viewModel.getConversations().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            if (list == null || list.isEmpty()) {
                binding.layoutEmptyMessages.setVisibility(View.VISIBLE);
                binding.recyclerConversations.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyMessages.setVisibility(View.GONE);
                binding.recyclerConversations.setVisibility(View.VISIBLE);
            }
        });

        binding.fabNewConversation.setOnClickListener(v -> openNewConversationDialog());
        binding.btnEmptyStartChat.setOnClickListener(v -> openNewConversationDialog());
    }

    private void openNewConversationDialog() {
        if (!LiquidGlassHelper.canNavigate()) return;
        NewConversationDialog dialog = new NewConversationDialog();
        dialog.show(getChildFragmentManager(), NewConversationDialog.TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    @Override
    public void onConversationClicked(ConversationWithDetails conversation) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversation.getConversation().getId());
        LiquidGlassHelper.startActivityWithGlassTransition(this, intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
