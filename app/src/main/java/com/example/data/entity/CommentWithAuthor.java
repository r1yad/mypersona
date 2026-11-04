package com.example.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CommentWithAuthor {
    private CommentEntity comment;
    private PersonaEntity author;
    private int likeCount;
    private boolean isLikedByActivePersona;
    private int depthLevel = 0;
    private String replyToAuthorName = null;

    public CommentWithAuthor(@NonNull CommentEntity comment,
                             @Nullable PersonaEntity author,
                             int likeCount,
                             boolean isLikedByActivePersona) {
        this.comment = comment;
        this.author = author;
        this.likeCount = likeCount;
        this.isLikedByActivePersona = isLikedByActivePersona;
    }

    public int getDepthLevel() {
        return depthLevel;
    }

    public void setDepthLevel(int depthLevel) {
        this.depthLevel = depthLevel;
    }

    @Nullable
    public String getReplyToAuthorName() {
        return replyToAuthorName;
    }

    public void setReplyToAuthorName(@Nullable String replyToAuthorName) {
        this.replyToAuthorName = replyToAuthorName;
    }

    @NonNull
    public CommentEntity getComment() {
        return comment;
    }

    public void setComment(@NonNull CommentEntity comment) {
        this.comment = comment;
    }

    @Nullable
    public PersonaEntity getAuthor() {
        return author;
    }

    public void setAuthor(@Nullable PersonaEntity author) {
        this.author = author;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLikedByActivePersona() {
        return isLikedByActivePersona;
    }

    public void setLikedByActivePersona(boolean likedByActivePersona) {
        isLikedByActivePersona = likedByActivePersona;
    }
}
