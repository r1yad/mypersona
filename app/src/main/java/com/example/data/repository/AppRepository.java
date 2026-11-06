package com.example.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.data.AppDatabase;
import com.example.data.entity.CommentEntity;
import com.example.data.entity.CommentLikeEntity;
import com.example.data.entity.CommentWithAuthor;
import com.example.data.entity.ConversationEntity;
import com.example.data.entity.ConversationWithDetails;
import com.example.data.entity.MessageEntity;
import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostEntity;
import com.example.data.entity.PostLikeEntity;
import com.example.data.entity.PostMediaEntity;
import com.example.data.entity.PostWithDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppRepository {

    private static final String PREFS_NAME = "personanet_prefs";
    private static final String KEY_ACTIVE_PERSONA = "active_persona_id";

    private static volatile AppRepository INSTANCE;

    private final AppDatabase database;
    private final SharedPreferences preferences;
    private final ExecutorService executor;
    private final Handler mainHandler;

    private final MutableLiveData<PersonaEntity> activePersonaLive = new MutableLiveData<>();
    private final MutableLiveData<List<PostWithDetails>> feedPostsLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ConversationWithDetails>> conversationsLive = new MutableLiveData<>(new ArrayList<>());

    private AppRepository(Context context) {
        this.database = AppDatabase.getInstance(context);
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());

        loadActivePersona();
    }

    public static AppRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    // --- Active Persona Management ---

    public LiveData<PersonaEntity> getActivePersonaLive() {
        return activePersonaLive;
    }

    public void setActivePersona(@Nullable PersonaEntity persona) {
        if (persona == null) {
            preferences.edit().remove(KEY_ACTIVE_PERSONA).apply();
            activePersonaLive.postValue(null);
        } else {
            preferences.edit().putString(KEY_ACTIVE_PERSONA, persona.getId()).apply();
            activePersonaLive.postValue(persona);
        }
        refreshFeed();
        refreshConversations();
    }

    private void loadActivePersona() {
        executor.execute(() -> {
            String activeId = preferences.getString(KEY_ACTIVE_PERSONA, null);
            PersonaEntity persona = null;
            if (activeId != null) {
                persona = database.personaDao().getByIdSync(activeId);
            }
            if (persona == null) {
                List<PersonaEntity> all = database.personaDao().getAllPersonasSync();
                if (!all.isEmpty()) {
                    persona = all.get(0);
                    preferences.edit().putString(KEY_ACTIVE_PERSONA, persona.getId()).apply();
                }
            }
            activePersonaLive.postValue(persona);
            refreshFeed();
            refreshConversations();
        });
    }

    // --- Personas ---

    public LiveData<List<PersonaEntity>> getAllPersonasLive() {
        return database.personaDao().getAllPersonasLive();
    }

    public void savePersona(PersonaEntity persona, Runnable onComplete) {
        executor.execute(() -> {
            PersonaEntity existing = database.personaDao().getByIdSync(persona.getId());
            if (existing != null) {
                database.personaDao().update(persona);
            } else {
                database.personaDao().insert(persona);
            }
            PersonaEntity currentActive = activePersonaLive.getValue();
            if (currentActive == null || currentActive.getId().equals(persona.getId())) {
                setActivePersona(persona);
            } else {
                refreshFeed();
                refreshConversations();
            }
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void deletePersona(PersonaEntity persona, Runnable onComplete) {
        executor.execute(() -> {
            database.personaDao().delete(persona);
            PersonaEntity currentActive = activePersonaLive.getValue();
            if (currentActive != null && currentActive.getId().equals(persona.getId())) {
                loadActivePersona();
            }
            refreshFeed();
            refreshConversations();
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public static final String SORT_POSTS_NEWEST = "newest";
    public static final String SORT_POSTS_OLDEST = "oldest";
    public static final String SORT_POSTS_MOST_LIKED = "most_liked";

    public static final String SORT_PERSONAS_RECENT = "recent";
    public static final String SORT_PERSONAS_NAME_AZ = "name_az";
    public static final String SORT_PERSONAS_NAME_ZA = "name_za";
    public static final String SORT_PERSONAS_MOST_POSTS = "most_posts";

    private static final String KEY_SORT_POSTS = "sort_posts_order";
    private static final String KEY_SORT_PERSONAS = "sort_personas_order";

    public String getPostSortOrder() {
        return preferences.getString(KEY_SORT_POSTS, SORT_POSTS_NEWEST);
    }

    public void setPostSortOrder(String order) {
        preferences.edit().putString(KEY_SORT_POSTS, order).apply();
        refreshFeed();
    }

    public String getPersonaSortOrder() {
        return preferences.getString(KEY_SORT_PERSONAS, SORT_PERSONAS_RECENT);
    }

    public void setPersonaSortOrder(String order) {
        preferences.edit().putString(KEY_SORT_PERSONAS, order).apply();
    }

    // --- Feed & Posts ---

    public LiveData<List<PostWithDetails>> getFeedPostsLive() {
        return feedPostsLive;
    }

    public void refreshFeed() {
        executor.execute(() -> {
            List<PostEntity> posts = database.postDao().getAllPostsSync();
            String activeId = preferences.getString(KEY_ACTIVE_PERSONA, "");
            List<PostWithDetails> detailsList = new ArrayList<>();

            for (PostEntity post : posts) {
                PersonaEntity author = database.personaDao().getByIdSync(post.getAuthorPersonaId());
                int likeCount = database.likeDao().getPostLikeCount(post.getId());
                int commentCount = database.commentDao().getCommentCountForPost(post.getId());
                boolean isLiked = !activeId.isEmpty() && database.likeDao().isPostLikedByPersona(post.getId(), activeId) > 0;

                PostWithDetails item = new PostWithDetails(post, author, likeCount, commentCount, isLiked);
                item.setMediaList(database.postDao().getMediaForPostSync(post.getId()));
                detailsList.add(item);
            }

            // Apply post sort order
            String postSort = getPostSortOrder();
            if (SORT_POSTS_OLDEST.equals(postSort)) {
                detailsList.sort((a, b) -> Long.compare(a.getPost().getCreatedAt(), b.getPost().getCreatedAt()));
            } else if (SORT_POSTS_MOST_LIKED.equals(postSort)) {
                detailsList.sort((a, b) -> Integer.compare(b.getLikeCount(), a.getLikeCount()));
            } else {
                // Default: newest first
                detailsList.sort((a, b) -> Long.compare(b.getPost().getCreatedAt(), a.getPost().getCreatedAt()));
            }

            feedPostsLive.postValue(detailsList);
        });
    }

    public void createPost(String authorPersonaId, String textContent, @Nullable String mediaPath, Runnable onComplete) {
        List<String> paths = new ArrayList<>();
        if (mediaPath != null && !mediaPath.trim().isEmpty()) {
            paths.add(mediaPath.trim());
        }
        createPostWithMedia(authorPersonaId, textContent, paths, onComplete);
    }

    public void createPostWithMedia(String authorPersonaId, String textContent, @NonNull List<String> mediaPaths, Runnable onComplete) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            String postId = UUID.randomUUID().toString();
            PostEntity post = new PostEntity(postId, authorPersonaId, textContent, now, now);
            database.postDao().insertPost(post);

            int order = 0;
            for (String path : mediaPaths) {
                if (path != null && !path.trim().isEmpty()) {
                    PostMediaEntity media = new PostMediaEntity(
                            UUID.randomUUID().toString(),
                            postId,
                            path.trim(),
                            "image/webp",
                            order++
                    );
                    database.postDao().insertMedia(media);
                }
            }

            refreshFeed();
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void updatePost(String postId, String newContent, @NonNull List<String> newMediaPaths, Runnable onComplete) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            database.postDao().updatePostContent(postId, newContent, now);
            database.postDao().deleteMediaForPost(postId);

            int order = 0;
            for (String path : newMediaPaths) {
                if (path != null && !path.trim().isEmpty()) {
                    PostMediaEntity media = new PostMediaEntity(
                            UUID.randomUUID().toString(),
                            postId,
                            path.trim(),
                            "image/webp",
                            order++
                    );
                    database.postDao().insertMedia(media);
                }
            }

            refreshFeed();
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void togglePostLike(String postId, Runnable onComplete) {
        executor.execute(() -> {
            String activeId = preferences.getString(KEY_ACTIVE_PERSONA, "");
            if (activeId.isEmpty()) return;

            int count = database.likeDao().isPostLikedByPersona(postId, activeId);
            if (count > 0) {
                database.likeDao().deletePostLike(postId, activeId);
            } else {
                database.likeDao().insertPostLike(new PostLikeEntity(postId, activeId, System.currentTimeMillis()));
            }
            refreshFeed();
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void deletePost(String postId, Runnable onComplete) {
        executor.execute(() -> {
            PostEntity post = database.postDao().getPostByIdSync(postId);
            if (post != null) {
                database.postDao().deleteMediaForPost(postId);
                database.postDao().deletePost(post);
            }
            refreshFeed();
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    // --- Post Details & Comments ---

    public void loadPostDetails(String postId, Callback<PostWithDetails> callback) {
        executor.execute(() -> {
            PostEntity post = database.postDao().getPostByIdSync(postId);
            if (post == null) {
                mainHandler.post(() -> callback.onResult(null));
                return;
            }
            String activeId = preferences.getString(KEY_ACTIVE_PERSONA, "");
            PersonaEntity author = database.personaDao().getByIdSync(post.getAuthorPersonaId());
            int likeCount = database.likeDao().getPostLikeCount(post.getId());
            int commentCount = database.commentDao().getCommentCountForPost(post.getId());
            boolean isLiked = !activeId.isEmpty() && database.likeDao().isPostLikedByPersona(post.getId(), activeId) > 0;

            PostWithDetails details = new PostWithDetails(post, author, likeCount, commentCount, isLiked);
            details.setMediaList(database.postDao().getMediaForPostSync(post.getId()));

            mainHandler.post(() -> callback.onResult(details));
        });
    }

    public void loadPostsByAuthor(String personaId, Callback<List<PostWithDetails>> callback) {
        executor.execute(() -> {
            List<PostEntity> posts = database.postDao().getPostsByAuthorSync(personaId);
            String activeId = preferences.getString(KEY_ACTIVE_PERSONA, "");
            PersonaEntity author = database.personaDao().getByIdSync(personaId);
            List<PostWithDetails> detailsList = new ArrayList<>();

            for (PostEntity post : posts) {
                int likeCount = database.likeDao().getPostLikeCount(post.getId());
                int commentCount = database.commentDao().getCommentCountForPost(post.getId());
                boolean isLiked = !activeId.isEmpty() && database.likeDao().isPostLikedByPersona(post.getId(), activeId) > 0;

                PostWithDetails item = new PostWithDetails(post, author, likeCount, commentCount, isLiked);
                item.setMediaList(database.postDao().getMediaForPostSync(post.getId()));
                detailsList.add(item);
            }

            mainHandler.post(() -> callback.onResult(detailsList));
        });
    }

    public void loadPersonaById(String personaId, Callback<PersonaEntity> callback) {
        executor.execute(() -> {
            PersonaEntity persona = database.personaDao().getByIdSync(personaId);
            mainHandler.post(() -> callback.onResult(persona));
        });
    }

    public void loadComments(String postId, Callback<List<CommentWithAuthor>> callback) {
        executor.execute(() -> {
            List<CommentEntity> rawComments = database.commentDao().getCommentsForPostSync(postId);
            String activeId = preferences.getString(KEY_ACTIVE_PERSONA, "");
            
            // Map from commentId -> CommentWithAuthor
            java.util.Map<String, CommentWithAuthor> itemMap = new java.util.HashMap<>();
            java.util.Map<String, List<CommentWithAuthor>> childrenMap = new java.util.HashMap<>();
            List<CommentWithAuthor> rootComments = new ArrayList<>();

            for (CommentEntity c : rawComments) {
                PersonaEntity author = database.personaDao().getByIdSync(c.getAuthorPersonaId());
                int likeCount = database.likeDao().getCommentLikeCount(c.getId());
                boolean isLiked = !activeId.isEmpty() && database.likeDao().isCommentLikedByPersona(c.getId(), activeId) > 0;
                CommentWithAuthor item = new CommentWithAuthor(c, author, likeCount, isLiked);
                itemMap.put(c.getId(), item);

                String parentId = c.getParentCommentId();
                if (parentId != null && !parentId.trim().isEmpty()) {
                    if (!childrenMap.containsKey(parentId)) {
                        childrenMap.put(parentId, new ArrayList<>());
                    }
                    childrenMap.get(parentId).add(item);
                } else {
                    rootComments.add(item);
                }
            }

            // Build flattened list in DFS order with depth levels and reply targets
            List<CommentWithAuthor> orderedList = new ArrayList<>();
            for (CommentWithAuthor root : rootComments) {
                appendCommentAndDescendants(root, 0, null, childrenMap, orderedList);
            }

            // Fallback: any orphaned comments whose parentId wasn't found
            for (CommentWithAuthor item : rawComments.stream().map(c -> itemMap.get(c.getId())).collect(java.util.stream.Collectors.toList())) {
                if (!orderedList.contains(item)) {
                    appendCommentAndDescendants(item, 0, null, childrenMap, orderedList);
                }
            }

            mainHandler.post(() -> callback.onResult(orderedList));
        });
    }

    private void appendCommentAndDescendants(CommentWithAuthor current,
                                            int depth,
                                            @Nullable String parentAuthorName,
                                            java.util.Map<String, List<CommentWithAuthor>> childrenMap,
                                            List<CommentWithAuthor> outList) {
        if (outList.contains(current)) return;
        current.setDepthLevel(Math.min(depth, 4)); // cap visual depth at 4 for mobile readability
        current.setReplyToAuthorName(parentAuthorName);
        outList.add(current);

        String authorName = current.getAuthor() != null ? current.getAuthor().getDisplayName() : "user";
        List<CommentWithAuthor> children = childrenMap.get(current.getComment().getId());
        if (children != null) {
            for (CommentWithAuthor child : children) {
                appendCommentAndDescendants(child, depth + 1, authorName, childrenMap, outList);
            }
        }
    }

    public void addComment(String postId, String authorPersonaId, @Nullable String parentCommentId, String textContent, Runnable onComplete) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            CommentEntity comment = new CommentEntity(
                    UUID.randomUUID().toString(),
                    postId,
                    parentCommentId,
                    authorPersonaId,
                    textContent,
                    now,
                    now
            );
            database.commentDao().insert(comment);
            refreshFeed();
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void toggleCommentLike(String commentId, Runnable onComplete) {
        executor.execute(() -> {
            String activeId = preferences.getString(KEY_ACTIVE_PERSONA, "");
            if (activeId.isEmpty()) return;

            int count = database.likeDao().isCommentLikedByPersona(commentId, activeId);
            if (count > 0) {
                database.likeDao().deleteCommentLike(commentId, activeId);
            } else {
                database.likeDao().insertCommentLike(new CommentLikeEntity(commentId, activeId, System.currentTimeMillis()));
            }
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    // --- Conversations & Messages ---

    public LiveData<List<ConversationWithDetails>> getConversationsLive() {
        return conversationsLive;
    }

    public void refreshConversations() {
        executor.execute(() -> {
            List<ConversationEntity> list = database.conversationDao().getAllConversationsSync();
            List<ConversationWithDetails> detailsList = new ArrayList<>();

            for (ConversationEntity c : list) {
                PersonaEntity pA = database.personaDao().getByIdSync(c.getPersonaAId());
                PersonaEntity pB = database.personaDao().getByIdSync(c.getPersonaBId());
                detailsList.add(new ConversationWithDetails(c, pA, pB));
            }

            conversationsLive.postValue(detailsList);
        });
    }

    public void startOrGetConversation(String personaAId, String personaBId, Callback<ConversationEntity> callback) {
        executor.execute(() -> {
            ConversationEntity existing = database.conversationDao().findBetweenSync(personaAId, personaBId);
            if (existing != null) {
                mainHandler.post(() -> callback.onResult(existing));
                return;
            }
            long now = System.currentTimeMillis();
            ConversationEntity newConv = new ConversationEntity(
                    UUID.randomUUID().toString(),
                    personaAId,
                    personaBId,
                    now,
                    now,
                    ""
            );
            database.conversationDao().insert(newConv);
            refreshConversations();
            mainHandler.post(() -> callback.onResult(newConv));
        });
    }

    public void loadConversationDetails(String conversationId, Callback<ConversationWithDetails> callback) {
        executor.execute(() -> {
            ConversationEntity c = database.conversationDao().getByIdSync(conversationId);
            if (c == null) {
                mainHandler.post(() -> callback.onResult(null));
                return;
            }
            PersonaEntity pA = database.personaDao().getByIdSync(c.getPersonaAId());
            PersonaEntity pB = database.personaDao().getByIdSync(c.getPersonaBId());
            ConversationWithDetails details = new ConversationWithDetails(c, pA, pB);
            mainHandler.post(() -> callback.onResult(details));
        });
    }

    public LiveData<List<MessageEntity>> getMessagesForConversationLive(String conversationId) {
        return database.messageDao().getMessagesForConversationLive(conversationId);
    }

    public void sendMessage(String conversationId, String senderPersonaId, String textContent, @Nullable String mediaPath, Runnable onComplete) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            MessageEntity message = new MessageEntity(
                    UUID.randomUUID().toString(),
                    conversationId,
                    senderPersonaId,
                    null,
                    textContent,
                    mediaPath,
                    mediaPath != null ? "image/jpeg" : null,
                    now,
                    false,
                    true
            );
            database.messageDao().insert(message);

            ConversationEntity conv = database.conversationDao().getByIdSync(conversationId);
            if (conv != null) {
                conv.setLastActivityAt(now);
                conv.setLastMessagePreview(textContent);
                database.conversationDao().update(conv);
            }

            refreshConversations();
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void clearAllData(Runnable onComplete) {
        executor.execute(() -> {
            database.clearAllTables();
            preferences.edit().clear().apply();
            activePersonaLive.postValue(null);
            refreshFeed();
            refreshConversations();
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public interface Callback<T> {
        void onResult(T result);
    }
}
