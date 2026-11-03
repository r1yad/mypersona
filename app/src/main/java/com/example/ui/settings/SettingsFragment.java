package com.example.ui.settings;

import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.R;
import com.example.backup.BackupManager;
import com.example.databinding.FragmentSettingsBinding;

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

        // Export Backup
        binding.btnExportBackup.setOnClickListener(v -> {
            String filename = "PersonaNet_Backup_" + (System.currentTimeMillis() / 1000) + ".zip";
            createDocumentLauncher.launch(filename);
        });

        // Import Backup
        binding.btnImportBackup.setOnClickListener(v -> {
            boolean isRestoreMode = binding.radioFullRestore.isChecked();
            if (isRestoreMode) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Full Wipe & Restore")
                        .setMessage("Full Restore mode will wipe all local data and replace it entirely with the contents of the backup. Proceed?")
                        .setPositiveButton("Select Archive", (dialog, which) -> {
                            openDocumentLauncher.launch(new String[]{"application/zip", "application/x-zip-compressed", "application/octet-stream", "*/*"});
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                openDocumentLauncher.launch(new String[]{"application/zip", "application/x-zip-compressed", "application/octet-stream", "*/*"});
            }
        });

        // Clear Database
        binding.btnClearAllData.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Clear All Data")
                    .setMessage("This will delete all personas, posts, comments, likes, and messages from your device. Are you sure?")
                    .setPositiveButton("Clear Everything", (dialog, which) -> {
                        viewModel.clearAllData();
                        binding.textBackupStatus.setText("Database cleared. Ready for fresh character creation.");
                        binding.textBackupStatus.setTextColor(requireContext().getColor(R.color.text_secondary));
                        Toast.makeText(requireContext(), "Database cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshStats();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
