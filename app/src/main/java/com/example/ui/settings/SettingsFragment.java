package com.example.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.R;
import com.example.data.repository.AppRepository;
import com.example.databinding.FragmentSettingsBinding;
import com.example.util.LiquidGlassHelper;
import com.example.util.MediaHelper;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

    // Export Document Launcher
    private final ActivityResultLauncher<String> createDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/zip"), uri -> {
                if (uri != null) {
                    binding.textBackupStatus.setText("Exporting ZIP archive…");
                    viewModel.exportBackup(uri);
                }
            });

    // Import Document Launcher
    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    boolean isRestoreMode = binding.radioFullRestore.isChecked();
                    binding.textBackupStatus.setText("Reading and restoring ZIP archive…");
                    viewModel.importBackup(uri, isRestoreMode);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        LiquidGlassHelper.attachPressFeedback(
                binding.btnSettingPostOrg,
                binding.btnSettingPersonaOrg,
                binding.btnSettingDebugMode,
                binding.btnExportBackup,
                binding.btnImportBackup,
                binding.btnDeduplicateMedia,
                binding.btnClearAllData
        );

        viewModel.getDatabaseStatsLive().observe(getViewLifecycleOwner(), stats -> {
            binding.textDbStats.setText(stats);
        });

        viewModel.getBackupResultLive().observe(getViewLifecycleOwner(), summary -> {
            if (summary == null) return;

            if (summary.success) {
                String report = "✔ " + summary.message + "\n\n" +
                        "• Personas: " + summary.personasCount + "\n" +
                        "• Feed Posts: " + summary.postsCount + "\n" +
                        "• Conversations: " + summary.conversationsCount + "\n" +
                        "• Messages: " + summary.messagesCount + "\n" +
                        "• Media Images: " + summary.mediaFilesCount;
                binding.textBackupStatus.setText(report);
                binding.textBackupStatus.setTextColor(requireContext().getColor(R.color.emerald_success));
                Toast.makeText(requireContext(), summary.message, Toast.LENGTH_SHORT).show();
            } else {
                String report = "✖ " + summary.message;
                binding.textBackupStatus.setText(report);
                binding.textBackupStatus.setTextColor(requireContext().getColor(R.color.red_error));
                Toast.makeText(requireContext(), summary.message, Toast.LENGTH_LONG).show();
            }
        });

        // Content Organization with smooth transitions
        binding.btnSettingPostOrg.setOnClickListener(v ->
                LiquidGlassHelper.startActivityWithGlassTransition(
                        this, new Intent(requireContext(), PostOrganizationActivity.class)));

        binding.btnSettingPersonaOrg.setOnClickListener(v ->
                LiquidGlassHelper.startActivityWithGlassTransition(
                        this, new Intent(requireContext(), PersonaOrganizationActivity.class)));

        binding.btnSettingDebugMode.setOnClickListener(v ->
                LiquidGlassHelper.startActivityWithGlassTransition(
                        this, new Intent(requireContext(), DebugModeActivity.class)));

        updateOrganizationSummaries();

        // Export Backup
        binding.btnExportBackup.setOnClickListener(v -> {
            String filename = "MyPersona_Backup_" + (System.currentTimeMillis() / 1000) + ".zip";
            createDocumentLauncher.launch(filename);
        });

        // Import Backup
        binding.btnImportBackup.setOnClickListener(v -> {
            boolean isRestoreMode = binding.radioFullRestore.isChecked();
            if (isRestoreMode) {
                LiquidGlassHelper.showGlassConfirmDialog(
                        requireContext(),
                        "Full Wipe & Restore",
                        "Full Restore mode will wipe all local data and replace it entirely with the contents of the backup. Proceed?",
                        "Select Archive",
                        true,
                        () -> openDocumentLauncher.launch(new String[]{
                                "application/zip", "application/x-zip-compressed", "application/octet-stream", "*/*"
                        })
                );
            } else {
                openDocumentLauncher.launch(new String[]{
                        "application/zip", "application/x-zip-compressed", "application/octet-stream", "*/*"
                });
            }
        });

        // SHA-256 Content Deduplication
        binding.btnDeduplicateMedia.setOnClickListener(v -> {
            binding.btnDeduplicateMedia.setEnabled(false);
            AppRepository repo = AppRepository.getInstance(requireContext());
            repo.getExecutor().execute(() -> {
                int removed = MediaHelper.deduplicateMediaDatabase(requireContext(), repo.getDatabase());
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.btnDeduplicateMedia.setEnabled(true);
                    viewModel.refreshStats();
                    String msg = removed > 0
                            ? "✔ Successfully cleaned " + removed + " duplicate image files (SHA-256 matching)!"
                            : "✔ Storage clean: No duplicate images detected.";
                    binding.textBackupStatus.setText(msg);
                    binding.textBackupStatus.setTextColor(requireContext().getColor(R.color.emerald_success));
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                });
            });
        });

        // Clear Database
        binding.btnClearAllData.setOnClickListener(v ->
                LiquidGlassHelper.showGlassConfirmDialog(
                        requireContext(),
                        "Clear All Data",
                        "This will delete all personas, posts, comments, likes, and messages from your device. Are you sure?",
                        "Clear Everything",
                        true,
                        () -> {
                            viewModel.clearAllData();
                            binding.textBackupStatus.setText("Database cleared. Ready for fresh character creation.");
                            binding.textBackupStatus.setTextColor(requireContext().getColor(R.color.text_secondary));
                            Toast.makeText(requireContext(), "Database cleared", Toast.LENGTH_SHORT).show();
                        }
                )
        );
    }

    private void updateOrganizationSummaries() {
        AppRepository repo = AppRepository.getInstance(requireContext());
        String postOrder = repo.getPostSortOrder();
        if (AppRepository.SORT_POSTS_OLDEST.equals(postOrder)) {
            binding.textPostOrgSummary.setText("Oldest posts first");
        } else if (AppRepository.SORT_POSTS_MOST_LIKED.equals(postOrder)) {
            binding.textPostOrgSummary.setText("Most liked posts first");
        } else {
            binding.textPostOrgSummary.setText("Custom Drag & Drop / Newest first");
        }

        String personaOrder = repo.getPersonaSortOrder();
        if (AppRepository.SORT_PERSONAS_NAME_AZ.equals(personaOrder)) {
            binding.textPersonaOrgSummary.setText("Alphabetical (A to Z)");
        } else if (AppRepository.SORT_PERSONAS_NAME_ZA.equals(personaOrder)) {
            binding.textPersonaOrgSummary.setText("Alphabetical (Z to A)");
        } else {
            binding.textPersonaOrgSummary.setText("Recently updated first");
        }

        if (repo.isDebugModeEnabled()) {
            binding.textDebugModeSummary.setText("Active (Isolated Debug DB)");
            binding.textDebugModeSummary.setTextColor(requireContext().getColor(R.color.amber_warning));
        } else {
            binding.textDebugModeSummary.setText("Disabled (Production DB)");
            binding.textDebugModeSummary.setTextColor(requireContext().getColor(R.color.text_secondary));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshStats();
        }
        updateOrganizationSummaries();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
