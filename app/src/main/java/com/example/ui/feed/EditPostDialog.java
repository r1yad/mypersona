package com.example.ui.feed;

import android.app.Dialog;
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

import com.example.data.entity.PostMediaEntity;
import com.example.data.entity.PostWithDetails;
import com.example.databinding.DialogEditPostBinding;

import java.util.ArrayList;
import java.util.List;

public class EditPostDialog extends DialogFragment {

    public static final String TAG = "EditPostDialog";
    private static final String ARG_POST_ID = "arg_post_id";
    private static final String ARG_CONTENT = "arg_content";
    private static final String ARG_MEDIA_LIST = "arg_media_list";

    private DialogEditPostBinding binding;
    private FeedViewModel viewModel;
    private String postId;
    private String initialContent;
    private ArrayList<String> initialMediaPaths = new ArrayList<>();
    private PostThumbnailsAdapter thumbnailsAdapter;
    private Runnable onPostUpdatedCallback;

    public static EditPostDialog newInstance(PostWithDetails postWithDetails) {
        EditPostDialog dialog = new EditPostDialog();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postWithDetails.getPost().getId());
        args.putString(ARG_CONTENT, postWithDetails.getPost().getTextContent());
        ArrayList<String> mediaPaths = new ArrayList<>();
        if (postWithDetails.getMediaList() != null) {
            for (PostMediaEntity media : postWithDetails.getMediaList()) {
                mediaPaths.add(media.getMediaPath());
            }
        }
        args.putStringArrayList(ARG_MEDIA_LIST, mediaPaths);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnPostUpdatedCallback(Runnable callback) {
        this.onPostUpdatedCallback = callback;
    }

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    saveImageToInternalStorage(uri);
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
            initialContent = getArguments().getString(ARG_CONTENT);
            ArrayList<String> paths = getArguments().getStringArrayList(ARG_MEDIA_LIST);
            if (paths != null) {
                initialMediaPaths.addAll(paths);
            }
        }
    }

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
        binding = DialogEditPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FeedViewModel.class);

        binding.editPostContent.setText(initialContent != null ? initialContent : "");
        if (initialContent != null) {
            binding.editPostContent.setSelection(initialContent.length());
        }

        thumbnailsAdapter = new PostThumbnailsAdapter((position, path) -> {
            thumbnailsAdapter.removeAt(position);
            updateThumbnailsUi();
        });
        thumbnailsAdapter.setPaths(initialMediaPaths);
        binding.recyclerPostThumbnails.setAdapter(thumbnailsAdapter);
        updateThumbnailsUi();

        binding.btnAttachImage.setOnClickListener(v -> {
            if (thumbnailsAdapter.getItemCount() >= 5) {
                Toast.makeText(requireContext(), "You can attach up to 5 photos", Toast.LENGTH_SHORT).show();
                return;
            }
            imagePickerLauncher.launch("image/*");
        });

        binding.btnCloseDialog.setOnClickListener(v -> dismiss());

        binding.btnSavePost.setOnClickListener(v -> {
            String newContent = binding.editPostContent.getText().toString().trim();
            if (newContent.isEmpty()) {
                binding.editPostContent.setError("Post text cannot be empty");
                return;
            }

            binding.btnSavePost.setEnabled(false);
            List<String> currentMedia = thumbnailsAdapter.getPaths();
            viewModel.updatePost(postId, newContent, currentMedia, () -> {
                dismiss();
                Toast.makeText(requireContext(), "Post updated!", Toast.LENGTH_SHORT).show();
                if (onPostUpdatedCallback != null) {
                    onPostUpdatedCallback.run();
                }
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
            String path = com.example.util.MediaHelper.compressAndSaveWebP(requireContext(), uri, "post");
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
