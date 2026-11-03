package com.example.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
    tableName = "post_likes",
    primaryKeys = {"postId", "personaId"},
    foreignKeys = {
        @ForeignKey(
            entity = PostEntity.class,
            parentColumns = "id",
            childColumns = "postId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = PersonaEntity.class,
            parentColumns = "id",
            childColumns = "personaId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("postId"),
        @Index("personaId")
    }
)
public class PostLikeEntity {

    @NonNull
    private String postId;

    @NonNull
    private String personaId;

    private long createdAt;

    public PostLikeEntity(@NonNull String postId, @NonNull String personaId, long createdAt) {
        this.postId = postId;
        this.personaId = personaId;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getPostId() {
        return postId;
    }

    public void setPostId(@NonNull String postId) {
        this.postId = postId;
    }

    @NonNull
    public String getPersonaId() {
        return personaId;
    }

    public void setPersonaId(@NonNull String personaId) {
        this.personaId = personaId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
