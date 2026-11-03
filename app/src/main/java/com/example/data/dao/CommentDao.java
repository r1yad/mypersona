package com.example.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.data.entity.CommentEntity;

import java.util.List;

@Dao
public interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CommentEntity comment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CommentEntity> comments);

    @Update
    void update(CommentEntity comment);

    @Delete
    void delete(CommentEntity comment);

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    LiveData<List<CommentEntity>> getCommentsForPostLive(String postId);

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    List<CommentEntity> getCommentsForPostSync(String postId);

    @Query("SELECT * FROM comments")
    List<CommentEntity> getAllCommentsSync();

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    int getCommentCountForPost(String postId);

    @Query("DELETE FROM comments")
    void deleteAll();
}
