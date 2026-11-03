package com.example.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
    tableName = "comment_likes",
    primaryKeys = {"commentId", "personaId"},
    foreignKeys = {
        @ForeignKey(
            entity = CommentEntity.class,
            parentColumns = "id",
            childColumns = "commentId",
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
        @Index("commentId"),
        @Index("personaId")
    }
)
public class CommentLikeEntity {

    @NonNull
    private String commentId;

    @NonNull
    private String personaId;

    private long createdAt;

    public CommentLikeEntity(@NonNull String commentId, @NonNull String personaId, long createdAt) {
        this.commentId = commentId;
        this.personaId = personaId;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(@NonNull String commentId) {
        this.commentId = commentId;
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
