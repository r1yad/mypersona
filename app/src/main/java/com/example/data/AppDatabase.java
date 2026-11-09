package com.example.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.data.dao.CommentDao;
import com.example.data.dao.ConversationDao;
import com.example.data.dao.LikeDao;
import com.example.data.dao.MediaAssetDao;
import com.example.data.dao.MessageDao;
import com.example.data.dao.PersonaDao;
import com.example.data.dao.PostDao;
import com.example.data.entity.CommentEntity;
import com.example.data.entity.CommentLikeEntity;
import com.example.data.entity.ConversationEntity;
import com.example.data.entity.MediaAssetEntity;
import com.example.data.entity.MessageEntity;
import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostEntity;
import com.example.data.entity.PostLikeEntity;
import com.example.data.entity.PostMediaEntity;

@Database(
    entities = {
        PersonaEntity.class,
        PostEntity.class,
        PostMediaEntity.class,
        CommentEntity.class,
        PostLikeEntity.class,
        CommentLikeEntity.class,
        ConversationEntity.class,
        MessageEntity.class,
        MediaAssetEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract PersonaDao personaDao();
    public abstract PostDao postDao();
    public abstract CommentDao commentDao();
    public abstract LikeDao likeDao();
    public abstract ConversationDao conversationDao();
    public abstract MessageDao messageDao();
    public abstract MediaAssetDao mediaAssetDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "personanet.db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }

    private static volatile AppDatabase DEBUG_INSTANCE;

    public static AppDatabase getDebugInstance(Context context) {
        if (DEBUG_INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (DEBUG_INSTANCE == null) {
                    DEBUG_INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "personanet_debug.db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return DEBUG_INSTANCE;
    }
}
