package com.example.ui.settings;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.backup.BackupManager;
import com.example.data.repository.AppRepository;

public class SettingsViewModel extends AndroidViewModel {

    private final AppRepository repository;
    private final MutableLiveData<BackupManager.BackupSummary> backupResultLive = new MutableLiveData<>();
    private final MutableLiveData<String> databaseStatsLive = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        this.repository = AppRepository.getInstance(application);
        refreshStats();
    }

    public LiveData<BackupManager.BackupSummary> getBackupResultLive() {
        return backupResultLive;
    }

    public LiveData<String> getDatabaseStatsLive() {
        return databaseStatsLive;
    }

    public void refreshStats() {
        repository.getExecutor().execute(() -> {
            int pCount = repository.getDatabase().personaDao().getCount();
            int postCount = repository.getDatabase().postDao().getPostCount();
            int convCount = repository.getDatabase().conversationDao().getCount();
            int msgCount = repository.getDatabase().messageDao().getCount();

            String stats = "• Personas: " + pCount + "\n" +
                    "• Feed Posts: " + postCount + "\n" +
                    "• Conversations: " + convCount + "\n" +
                    "• Messages: " + msgCount;

            databaseStatsLive.postValue(stats);
        });
    }

    public void exportBackup(Uri uri) {
        repository.getExecutor().execute(() -> {
            BackupManager.BackupSummary summary = BackupManager.exportBackup(
                    getApplication(),
                    repository.getDatabase(),
                    uri
            );
            backupResultLive.postValue(summary);
            refreshStats();
        });
    }

    public void importBackup(Uri uri, boolean isRestoreMode) {
        repository.getExecutor().execute(() -> {
            BackupManager.BackupSummary summary = BackupManager.importBackup(
                    getApplication(),
                    repository.getDatabase(),
                    uri,
                    isRestoreMode
            );
            backupResultLive.postValue(summary);
            repository.refreshFeed();
            repository.refreshConversations();
            refreshStats();
        });
    }

    public void clearAllData() {
        repository.clearAllData(this::refreshStats);
    }
}
