package com.example.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.data.entity.MessageEntity;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MessageEntity message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MessageEntity> messages);

    @Update
    void update(MessageEntity message);

    @Delete
    void delete(MessageEntity message);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    LiveData<List<MessageEntity>> getMessagesForConversationLive(String conversationId);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    List<MessageEntity> getMessagesForConversationSync(String conversationId);

    @Query("SELECT * FROM messages")
    List<MessageEntity> getAllMessagesSync();

    @Query("SELECT COUNT(*) FROM messages")
    int getCount();

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    void deleteForConversation(String conversationId);

    @Query("UPDATE messages SET mediaPath = :newPath WHERE mediaPath = :oldPath")
    void updateMessageMediaPath(String oldPath, String newPath);

    @Query("DELETE FROM messages")
    void deleteAll();
}
