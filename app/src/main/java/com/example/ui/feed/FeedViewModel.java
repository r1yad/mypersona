package com.example.ui.feed;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostWithDetails;
import com.example.data.repository.AppRepository;

import java.util.List;

public class FeedViewModel extends AndroidViewModel {

    private final AppRepository repository;
    private final LiveData<List<PostWithDetails>> feedPosts;
    private final LiveData<PersonaEntity> activePersona;

    public FeedViewModel(@NonNull Application application) {
        super(application);
        this.repository = AppRepository.getInstance(application);
        this.feedPosts = repository.getFeedPostsLive();
        this.activePersona = repository.getActivePersonaLive();
    }

    public LiveData<List<PostWithDetails>> getFeedPosts() {
        return feedPosts;
    }

    public LiveData<PersonaEntity> getActivePersona() {
        return activePersona;
    }

    private final androidx.lifecycle.MutableLiveData<Boolean> postCreatedEvent = new androidx.lifecycle.MutableLiveData<>();

    public LiveData<Boolean> getPostCreatedEvent() {
        return postCreatedEvent;
    }

    public void refresh() {
        repository.refreshFeed();
    }

    public void createPost(String authorPersonaId, String content, @Nullable String mediaPath, Runnable onComplete) {
        repository.createPost(authorPersonaId, content, mediaPath, () -> {
            postCreatedEvent.postValue(true);
            if (onComplete != null) onComplete.run();
        });
    }

    public void createPostWithMedia(String authorPersonaId, String content, @NonNull List<String> mediaPaths, Runnable onComplete) {
        repository.createPostWithMedia(authorPersonaId, content, mediaPaths, () -> {
            postCreatedEvent.postValue(true);
            if (onComplete != null) onComplete.run();
        });
    }

    public void updatePost(String postId, String newContent, @NonNull List<String> newMediaPaths, Runnable onComplete) {
        repository.updatePost(postId, newContent, newMediaPaths, onComplete);
    }

    public void toggleLike(String postId) {
        repository.togglePostLike(postId, null);
    }

    public void deletePost(String postId) {
        repository.deletePost(postId, null);
    }
}
