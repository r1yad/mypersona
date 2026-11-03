package com.example.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "comments",
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
            childColumns = "authorPersonaId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("postId"),
        @Index("authorPersonaId"),
        @Index("parentCommentId")
    }
)
public class CommentEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String postId;

    @Nullable
    private String parentCommentId;

    @NonNull
    private String authorPersonaId;

    @NonNull
    private String textContent;

    private long createdAt;
    private long updatedAt;

    public CommentEntity(@NonNull String id,
                         @NonNull String postId,
                         @Nullable String parentCommentId,
                         @NonNull String authorPersonaId,
                         @NonNull String textContent,
                         long createdAt,
                         long updatedAt) {
        this.id = id;
        this.postId = postId;
        this.parentCommentId = parentCommentId;
        this.authorPersonaId = authorPersonaId;
        this.textContent = textContent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getPostId() {
        return postId;
    }

    public void setPostId(@NonNull String postId) {
        this.postId = postId;
    }

    @Nullable
    public String getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(@Nullable String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    @NonNull
    public String getAuthorPersonaId() {
        return authorPersonaId;
    }

    public void setAuthorPersonaId(@NonNull String authorPersonaId) {
        this.authorPersonaId = authorPersonaId;
    }

    @NonNull
    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(@NonNull String textContent) {
        this.textContent = textContent;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
