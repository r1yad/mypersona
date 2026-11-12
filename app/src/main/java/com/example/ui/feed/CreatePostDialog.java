package com.example.ui.feed;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.example.util.LiquidGlassHelper;
import com.example.util.MediaHelper;

import java.util.List;

public class CreatePostDialog extends DialogFragment {

    public static final String TAG = "CreatePostDialog";

    private DialogCreatePostBinding binding;
    private FeedViewModel viewModel;
    private PersonaEntity activeAuthor;
    private PostThumbnailsAdapter thumbnailsAdapter;

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
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setWindowAnimations(R.style.GlassDialogAnimation);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            LiquidGlassHelper.enableDialogWindowBlur(window);
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

        LiquidGlassHelper.attachPressFeedback(
                binding.btnCloseDialog,
                binding.btnRoleplayPrompt,
                binding.btnAttachImage,
                binding.btnPublishPost
        );

        thumbnailsAdapter = new PostThumbnailsAdapter((position, path) -> {
            thumbnailsAdapter.removeAt(position);
            updateThumbnailsUi();
        });
        binding.recyclerPostThumbnails.setAdapter(thumbnailsAdapter);

        viewModel.getActivePersona().observe(getViewLifecycleOwner(), persona -> {
            activeAuthor = persona;
            if (persona != null) {
                binding.textPostingAs.setText("Posting as " + persona.getDisplayName() + " (@" + persona.getUsername() + ")");
                if (persona.getAvatarPath() != null) {
                    Bitmap bm = MediaHelper.loadBitmap(requireContext(), persona.getAvatarPath(), 120);
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

        // Attach Image (up to 5)
        binding.btnAttachImage.setOnClickListener(v -> {
            if (thumbnailsAdapter.getItemCount() >= 5) {
                Toast.makeText(requireContext(), "You can attach up to 5 photos", Toast.LENGTH_SHORT).show();
                return;
            }
            imagePickerLauncher.launch("image/*");
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
            List<String> mediaPaths = thumbnailsAdapter.getPaths();
            viewModel.createPostWithMedia(activeAuthor.getId(), content, mediaPaths, () -> {
                if (!isAdded()) return;
                dismiss();
                Toast.makeText(requireContext(), "Post published!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateThumbnailsUi() {
        int count = thumbnailsAdapter.getItemCount();
        if (count > 0) {
            binding.layoutMediaThumbnails.setVisibility(View.VISIBLE);
            binding.textPhotosCounter.setText(count + "/5 photos");
        } else {
            binding.layoutMediaThumbnails.setVisibility(View.GONE);
        }
    }

    private void saveImageToInternalStorage(Uri uri) {
        try {
            String path = MediaHelper.compressAndSaveWebP(requireContext(), uri, "post");
            if (path != null) {
                thumbnailsAdapter.addPath(path);
                updateThumbnailsUi();
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
