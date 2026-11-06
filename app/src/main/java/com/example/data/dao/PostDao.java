package com.example.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.data.entity.PostEntity;
import com.example.data.entity.PostMediaEntity;

import androidx.room.Upsert;

import java.util.List;

@Dao
public interface PostDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPost(PostEntity post);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAllPosts(List<PostEntity> posts);

    @Upsert
    void upsertPost(PostEntity post);

    @Upsert
    void upsertAllPosts(List<PostEntity> posts);

    @Update
    void updatePost(PostEntity post);

    @Query("UPDATE posts SET textContent = :content, updatedAt = :updatedAt WHERE id = :postId")
    void updatePostContent(String postId, String content, long updatedAt);

    @Delete
    void deletePost(PostEntity post);

    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    LiveData<List<PostEntity>> getAllPostsLive();

    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    List<PostEntity> getAllPostsSync();

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    PostEntity getPostByIdSync(String id);

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    LiveData<PostEntity> getPostByIdLive(String id);

    @Query("SELECT * FROM posts WHERE authorPersonaId = :personaId ORDER BY createdAt DESC")
    LiveData<List<PostEntity>> getPostsByAuthorLive(String personaId);

    @Query("SELECT * FROM posts WHERE authorPersonaId = :personaId ORDER BY createdAt DESC")
    List<PostEntity> getPostsByAuthorSync(String personaId);

    @Query("SELECT COUNT(*) FROM posts WHERE authorPersonaId = :personaId")
    int getPostCountByAuthor(String personaId);

    @Query("SELECT COUNT(*) FROM posts")
    int getPostCount();

    @Query("DELETE FROM posts")
    void deleteAllPosts();

    // Media
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMedia(PostMediaEntity media);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllMedia(List<PostMediaEntity> mediaList);

    @Query("SELECT * FROM post_media WHERE postId = :postId ORDER BY orderIndex ASC")
    List<PostMediaEntity> getMediaForPostSync(String postId);

    @Query("SELECT * FROM post_media WHERE postId = :postId ORDER BY orderIndex ASC")
    LiveData<List<PostMediaEntity>> getMediaForPostLive(String postId);

    @Query("SELECT * FROM post_media")
    List<PostMediaEntity> getAllMediaSync();

    @Query("DELETE FROM post_media WHERE postId = :postId")
    void deleteMediaForPost(String postId);

    @Query("UPDATE post_media SET mediaPath = :newPath WHERE mediaPath = :oldPath")
    void updatePostMediaPath(String oldPath, String newPath);

    @Query("DELETE FROM post_media")
    void deleteAllMedia();
}
