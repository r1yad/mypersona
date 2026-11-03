package com.example.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "media_assets")
public class MediaAssetEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String relativePath;

    @NonNull
    private String mimeType;

    private int width;
    private int height;
    private long fileSizeBytes;
    private long createdAt;

    public MediaAssetEntity(@NonNull String id,
                            @NonNull String relativePath,
                            @NonNull String mimeType,
                            int width,
                            int height,
                            long fileSizeBytes,
                            long createdAt) {
        this.id = id;
        this.relativePath = relativePath;
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
        this.fileSizeBytes = fileSizeBytes;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(@NonNull String relativePath) {
        this.relativePath = relativePath;
    }

    @NonNull
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(@NonNull String mimeType) {
        this.mimeType = mimeType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
