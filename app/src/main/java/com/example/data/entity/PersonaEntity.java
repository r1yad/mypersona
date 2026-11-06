package com.example.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "personas")
public class PersonaEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String displayName;

    @NonNull
    private String username;

    @NonNull
    private String bio;

    @Nullable
    private String avatarPath;

    @Nullable
    private String coverPath;

    @NonNull
    private String personalityTraits;

    @NonNull
    private String speakingStyle;

    @NonNull
    private String roleplayInstructions;

    @NonNull
    private String backgroundStory;

    private long createdAt;
    private long updatedAt;

    public PersonaEntity(@NonNull String id,
                         @NonNull String displayName,
                         @NonNull String username,
                         @NonNull String bio,
                         @Nullable String avatarPath,
                         @Nullable String coverPath,
                         @NonNull String personalityTraits,
                         @NonNull String speakingStyle,
                         @NonNull String roleplayInstructions,
                         @NonNull String backgroundStory,
                         long createdAt,
                         long updatedAt) {
        this.id = id;
        this.displayName = displayName;
        this.username = username;
        this.bio = bio;
        this.avatarPath = avatarPath;
        this.coverPath = coverPath;
        this.personalityTraits = personalityTraits;
        this.speakingStyle = speakingStyle;
        this.roleplayInstructions = roleplayInstructions;
        this.backgroundStory = backgroundStory;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public String getId() {
        return id != null ? id : "";
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getDisplayName() {
        return displayName != null ? displayName : "";
    }

    public void setDisplayName(@NonNull String displayName) {
        this.displayName = displayName;
    }

    @NonNull
    public String getUsername() {
        return username != null ? username : "";
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getBio() {
        return bio != null ? bio : "";
    }

    public void setBio(@NonNull String bio) {
        this.bio = bio;
    }

    @Nullable
    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(@Nullable String avatarPath) {
        this.avatarPath = avatarPath;
    }

    @Nullable
    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(@Nullable String coverPath) {
        this.coverPath = coverPath;
    }

    @NonNull
    public String getPersonalityTraits() {
        return personalityTraits != null ? personalityTraits : "";
    }

    public void setPersonalityTraits(@NonNull String personalityTraits) {
        this.personalityTraits = personalityTraits;
    }

    @NonNull
    public String getSpeakingStyle() {
        return speakingStyle != null ? speakingStyle : "";
    }

    public void setSpeakingStyle(@NonNull String speakingStyle) {
        this.speakingStyle = speakingStyle;
    }

    @NonNull
    public String getRoleplayInstructions() {
        return roleplayInstructions != null ? roleplayInstructions : "";
    }

    public void setRoleplayInstructions(@NonNull String roleplayInstructions) {
        this.roleplayInstructions = roleplayInstructions;
    }

    @NonNull
    public String getBackgroundStory() {
        return backgroundStory != null ? backgroundStory : "";
    }

    public void setBackgroundStory(@NonNull String backgroundStory) {
        this.backgroundStory = backgroundStory;
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
