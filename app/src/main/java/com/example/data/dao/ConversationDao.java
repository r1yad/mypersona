package com.example.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.data.entity.ConversationEntity;

import java.util.List;

@Dao
public interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ConversationEntity conversation);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ConversationEntity> conversations);

    @Update
    void update(ConversationEntity conversation);

    @Delete
    void delete(ConversationEntity conversation);

    @Query("SELECT * FROM conversations ORDER BY lastActivityAt DESC")
    LiveData<List<ConversationEntity>> getAllConversationsLive();

    @Query("SELECT * FROM conversations ORDER BY lastActivityAt DESC")
    List<ConversationEntity> getAllConversationsSync();

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    ConversationEntity getByIdSync(String id);

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    LiveData<ConversationEntity> getByIdLive(String id);

    @Query("SELECT * FROM conversations WHERE (personaAId = :p1 AND personaBId = :p2) OR (personaAId = :p2 AND personaBId = :p1) LIMIT 1")
    ConversationEntity findBetweenSync(String p1, String p2);

    @Query("SELECT COUNT(*) FROM conversations")
    int getCount();

    @Query("DELETE FROM conversations")
    void deleteAll();
}
