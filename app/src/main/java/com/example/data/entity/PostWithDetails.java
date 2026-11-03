package com.example.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PostWithDetails {
    private PostEntity post;
    private PersonaEntity author;
    private int likeCount;
    private int commentCount;
    private boolean isLikedByActivePersona;
    private List<PostMediaEntity> mediaList = new ArrayList<>();

    public PostWithDetails(@NonNull PostEntity post,
                           @Nullable PersonaEntity author,
                           int likeCount,
                           int commentCount,
                           boolean isLikedByActivePersona) {
        this.post = post;
        this.author = author;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isLikedByActivePersona = isLikedByActivePersona;
    }

    @NonNull
    public PostEntity getPost() {
        return post;
    }

    public void setPost(@NonNull PostEntity post) {
        this.post = post;
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

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isLikedByActivePersona() {
        return isLikedByActivePersona;
    }

    public void setLikedByActivePersona(boolean likedByActivePersona) {
        isLikedByActivePersona = likedByActivePersona;
    }

    public List<PostMediaEntity> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<PostMediaEntity> mediaList) {
        this.mediaList = mediaList;
    }
}
