package com.example.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "messages",
    foreignKeys = {
        @ForeignKey(
            entity = ConversationEntity.class,
            parentColumns = "id",
            childColumns = "conversationId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = PersonaEntity.class,
            parentColumns = "id",
            childColumns = "senderPersonaId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("conversationId"),
        @Index("senderPersonaId")
    }
)
public class MessageEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String conversationId;

    @NonNull
    private String senderPersonaId;

    @Nullable
    private String replyToMessageId;

    @NonNull
    private String textContent;

    @Nullable
    private String mediaPath;

    @Nullable
    private String mimeType;

    private long createdAt;
    private boolean isEdited;
    private boolean isRead;

    public MessageEntity(@NonNull String id,
                         @NonNull String conversationId,
                         @NonNull String senderPersonaId,
                         @Nullable String replyToMessageId,
                         @NonNull String textContent,
                         @Nullable String mediaPath,
                         @Nullable String mimeType,
                         long createdAt,
                         boolean isEdited,
                         boolean isRead) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderPersonaId = senderPersonaId;
        this.replyToMessageId = replyToMessageId;
        this.textContent = textContent;
        this.mediaPath = mediaPath;
        this.mimeType = mimeType;
        this.createdAt = createdAt;
        this.isEdited = isEdited;
        this.isRead = isRead;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    @NonNull
    public String getSenderPersonaId() {
        return senderPersonaId;
    }

    public void setSenderPersonaId(@NonNull String senderPersonaId) {
        this.senderPersonaId = senderPersonaId;
    }

    @Nullable
    public String getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(@Nullable String replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    @NonNull
    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(@NonNull String textContent) {
        this.textContent = textContent;
    }

    @Nullable
    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(@Nullable String mediaPath) {
        this.mediaPath = mediaPath;
    }

    @Nullable
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(@Nullable String mimeType) {
        this.mimeType = mimeType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
