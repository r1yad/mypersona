package com.example.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.data.entity.CommentLikeEntity;
import com.example.data.entity.PostLikeEntity;

import java.util.List;

@Dao
public interface LikeDao {

    // Post Likes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPostLike(PostLikeEntity like);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllPostLikes(List<PostLikeEntity> likes);

    @Delete
    void deletePostLike(PostLikeEntity like);

    @Query("DELETE FROM post_likes WHERE postId = :postId AND personaId = :personaId")
    void deletePostLike(String postId, String personaId);

    @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId")
    int getPostLikeCount(String postId);

    @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId AND personaId = :personaId")
    int isPostLikedByPersona(String postId, String personaId);

    @Query("SELECT * FROM post_likes")
    List<PostLikeEntity> getAllPostLikesSync();

    @Query("DELETE FROM post_likes")
    void deleteAllPostLikes();

    // Comment Likes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCommentLike(CommentLikeEntity like);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllCommentLikes(List<CommentLikeEntity> likes);

    @Delete
    void deleteCommentLike(CommentLikeEntity like);

    @Query("DELETE FROM comment_likes WHERE commentId = :commentId AND personaId = :personaId")
    void deleteCommentLike(String commentId, String personaId);

    @Query("SELECT COUNT(*) FROM comment_likes WHERE commentId = :commentId")
    int getCommentLikeCount(String commentId);

    @Query("SELECT COUNT(*) FROM comment_likes WHERE commentId = :commentId AND personaId = :personaId")
    int isCommentLikedByPersona(String commentId, String personaId);

    @Query("SELECT * FROM comment_likes")
    List<CommentLikeEntity> getAllCommentLikesSync();

    @Query("DELETE FROM comment_likes")
    void deleteAllCommentLikes();
}
