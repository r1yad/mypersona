package com.example.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "conversations",
    foreignKeys = {
        @ForeignKey(
            entity = PersonaEntity.class,
            parentColumns = "id",
            childColumns = "personaAId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = PersonaEntity.class,
            parentColumns = "id",
            childColumns = "personaBId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("personaAId"),
        @Index("personaBId")
    }
)
public class ConversationEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String personaAId;

    @NonNull
    private String personaBId;

    private long createdAt;
    private long lastActivityAt;

    @Nullable
    private String lastMessagePreview;

    public ConversationEntity(@NonNull String id,
                              @NonNull String personaAId,
                              @NonNull String personaBId,
                              long createdAt,
                              long lastActivityAt,
                              @Nullable String lastMessagePreview) {
        this.id = id;
        this.personaAId = personaAId;
        this.personaBId = personaBId;
        this.createdAt = createdAt;
        this.lastActivityAt = lastActivityAt;
        this.lastMessagePreview = lastMessagePreview;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getPersonaAId() {
        return personaAId;
    }

    public void setPersonaAId(@NonNull String personaAId) {
        this.personaAId = personaAId;
    }

    @NonNull
    public String getPersonaBId() {
        return personaBId;
    }

    public void setPersonaBId(@NonNull String personaBId) {
        this.personaBId = personaBId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(long lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    @Nullable
    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(@Nullable String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }
}
