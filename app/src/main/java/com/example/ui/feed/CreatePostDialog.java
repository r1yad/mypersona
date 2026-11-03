package com.example.ui.feed;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.R;
import com.example.assistant.RoleplayAssistant;
import com.example.data.entity.PersonaEntity;
import com.example.databinding.DialogCreatePostBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class CreatePostDialog extends DialogFragment {

    public static final String TAG = "CreatePostDialog";

    private DialogCreatePostBinding binding;
    private FeedViewModel viewModel;
    private PersonaEntity activeAuthor;
    private String selectedMediaPath = null;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    saveImageToInternalStorage(uri);
                }
            });

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogCreatePostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(FeedViewModel.class);

        viewModel.getActivePersona().observe(getViewLifecycleOwner(), persona -> {
            activeAuthor = persona;
            if (persona != null) {
                binding.textPostingAs.setText("Posting as " + persona.getDisplayName() + " (@" + persona.getUsername() + ")");
                if (persona.getAvatarPath() != null) {
                    Bitmap bm = com.example.util.MediaHelper.loadBitmap(requireContext(), persona.getAvatarPath(), 120);
                    if (bm != null) {
                        binding.imageAuthorAvatar.setImageBitmap(bm);
                    } else {
                        binding.imageAuthorAvatar.setImageResource(R.drawable.ic_person_placeholder);
                    }
                } else {
                    binding.imageAuthorAvatar.setImageResource(R.drawable.ic_person_placeholder);
                }
            } else {
                binding.textPostingAs.setText("No active persona selected");
                binding.imageAuthorAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }
        });

        // Roleplay Assistant idea prompt
        binding.btnRoleplayPrompt.setOnClickListener(v -> {
            if (activeAuthor != null) {
                String idea = RoleplayAssistant.generatePostIdea(activeAuthor);
                binding.editPostContent.setText(idea);
                binding.editPostContent.setSelection(idea.length());
            } else {
                Toast.makeText(requireContext(), "Please create or select an active persona first", Toast.LENGTH_SHORT).show();
            }
        });

        // Attach Image
        binding.btnAttachImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Remove Attached Image
        binding.btnRemoveImage.setOnClickListener(v -> {
            selectedMediaPath = null;
            binding.layoutMediaPreview.setVisibility(View.GONE);
        });

        binding.btnCloseDialog.setOnClickListener(v -> dismiss());

        // Publish Post
        binding.btnPublishPost.setOnClickListener(v -> {
            if (activeAuthor == null) {
                Toast.makeText(requireContext(), "Please create or select an active persona first", Toast.LENGTH_SHORT).show();
                return;
            }

            String content = binding.editPostContent.getText().toString().trim();
            if (content.isEmpty()) {
                binding.editPostContent.setError("Post text cannot be empty");
                return;
            }

            binding.btnPublishPost.setEnabled(false);
            viewModel.createPost(activeAuthor.getId(), content, selectedMediaPath, () -> {
                dismiss();
                Toast.makeText(requireContext(), "Post published!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void saveImageToInternalStorage(Uri uri) {
        try {
            String path = com.example.util.MediaHelper.compressAndSaveWebP(requireContext(), uri, "post");
            if (path != null) {
                selectedMediaPath = path;
                Bitmap bm = com.example.util.MediaHelper.loadBitmap(requireContext(), path, 800);
                if (bm != null) {
                    binding.imagePreview.setImageBitmap(bm);
                    binding.layoutMediaPreview.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(requireContext(), "Failed to compress image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to attach image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
