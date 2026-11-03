package com.example.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "post_media",
    foreignKeys = @ForeignKey(
        entity = PostEntity.class,
        parentColumns = "id",
        childColumns = "postId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("postId")}
)
public class PostMediaEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String postId;

    @NonNull
    private String mediaPath;

    @NonNull
    private String mimeType;

    private int orderIndex;

    public PostMediaEntity(@NonNull String id,
                           @NonNull String postId,
                           @NonNull String mediaPath,
                           @NonNull String mimeType,
                           int orderIndex) {
        this.id = id;
        this.postId = postId;
        this.mediaPath = mediaPath;
        this.mimeType = mimeType;
        this.orderIndex = orderIndex;
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

    @NonNull
    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(@NonNull String mediaPath) {
        this.mediaPath = mediaPath;
    }

    @NonNull
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(@NonNull String mimeType) {
        this.mimeType = mimeType;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
