package com.example.backup;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.data.AppDatabase;
import com.example.data.entity.CommentEntity;
import com.example.data.entity.CommentLikeEntity;
import com.example.data.entity.ConversationEntity;
import com.example.data.entity.MediaAssetEntity;
import com.example.data.entity.MessageEntity;
import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostEntity;
import com.example.data.entity.PostLikeEntity;
import com.example.data.entity.PostMediaEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupManager {

    public static class BackupSummary {
        public boolean success;
        public String message;
        public int personasCount;
        public int postsCount;
        public int conversationsCount;
        public int messagesCount;
        public int mediaFilesCount;

        public BackupSummary(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static BackupSummary exportBackup(@NonNull Context context, @NonNull AppDatabase db, @NonNull Uri destinationUri) {
        try (OutputStream os = context.getContentResolver().openOutputStream(destinationUri);
             ZipOutputStream zos = new ZipOutputStream(os)) {

            long now = System.currentTimeMillis();

            // Fetch all tables
            List<PersonaEntity> personas = db.personaDao().getAllPersonasSync();
            List<PostEntity> posts = db.postDao().getAllPostsSync();
            List<PostMediaEntity> postMedia = db.postDao().getAllMediaSync();
            List<CommentEntity> comments = db.commentDao().getAllCommentsSync();
            List<PostLikeEntity> postLikes = db.likeDao().getAllPostLikesSync();
            List<CommentLikeEntity> commentLikes = db.likeDao().getAllCommentLikesSync();
            List<ConversationEntity> conversations = db.conversationDao().getAllConversationsSync();
            List<MessageEntity> messages = db.messageDao().getAllMessagesSync();
            List<MediaAssetEntity> mediaAssets = db.mediaAssetDao().getAllSync();

            // 1. Build manifest.json
            JSONObject manifest = new JSONObject();
            manifest.put("app", "My Persona");
            manifest.put("version", 1);
            manifest.put("exportedAt", now);
            manifest.put("personasCount", personas.size());
            manifest.put("postsCount", posts.size());
            manifest.put("conversationsCount", conversations.size());
            manifest.put("messagesCount", messages.size());

            zos.putNextEntry(new ZipEntry("manifest.json"));
            zos.write(manifest.toString(2).getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 2. Build data.json
            JSONObject data = new JSONObject();
            data.put("version", 1);
            data.put("exportedAt", now);

            // Personas
            JSONArray personasArr = new JSONArray();
            for (PersonaEntity p : personas) {
                JSONObject obj = new JSONObject();
                obj.put("id", p.getId());
                obj.put("displayName", p.getDisplayName());
                obj.put("username", p.getUsername());
                obj.put("bio", p.getBio());
                obj.put("avatarPath", p.getAvatarPath() != null ? p.getAvatarPath() : JSONObject.NULL);
                obj.put("coverPath", p.getCoverPath() != null ? p.getCoverPath() : JSONObject.NULL);
                obj.put("personalityTraits", p.getPersonalityTraits());
                obj.put("speakingStyle", p.getSpeakingStyle());
                obj.put("roleplayInstructions", p.getRoleplayInstructions());
                obj.put("backgroundStory", p.getBackgroundStory());
                obj.put("createdAt", p.getCreatedAt());
                obj.put("updatedAt", p.getUpdatedAt());
                personasArr.put(obj);
            }
            data.put("personas", personasArr);

            // Posts
            JSONArray postsArr = new JSONArray();
            for (PostEntity p : posts) {
                JSONObject obj = new JSONObject();
                obj.put("id", p.getId());
                obj.put("authorPersonaId", p.getAuthorPersonaId());
                obj.put("textContent", p.getTextContent());
                obj.put("createdAt", p.getCreatedAt());
                obj.put("updatedAt", p.getUpdatedAt());
                postsArr.put(obj);
            }
            data.put("posts", postsArr);

            // PostMedia
            JSONArray postMediaArr = new JSONArray();
            for (PostMediaEntity m : postMedia) {
                JSONObject obj = new JSONObject();
                obj.put("id", m.getId());
                obj.put("postId", m.getPostId());
                obj.put("mediaPath", m.getMediaPath());
                obj.put("mimeType", m.getMimeType());
                obj.put("orderIndex", m.getOrderIndex());
                postMediaArr.put(obj);
            }
            data.put("postMedia", postMediaArr);

            // Comments
            JSONArray commentsArr = new JSONArray();
            for (CommentEntity c : comments) {
                JSONObject obj = new JSONObject();
                obj.put("id", c.getId());
                obj.put("postId", c.getPostId());
                obj.put("parentCommentId", c.getParentCommentId() != null ? c.getParentCommentId() : JSONObject.NULL);
                obj.put("authorPersonaId", c.getAuthorPersonaId());
                obj.put("textContent", c.getTextContent());
                obj.put("createdAt", c.getCreatedAt());
                obj.put("updatedAt", c.getUpdatedAt());
                commentsArr.put(obj);
            }
            data.put("comments", commentsArr);

            // Post Likes
            JSONArray postLikesArr = new JSONArray();
            for (PostLikeEntity l : postLikes) {
                JSONObject obj = new JSONObject();
                obj.put("postId", l.getPostId());
                obj.put("personaId", l.getPersonaId());
                obj.put("createdAt", l.getCreatedAt());
                postLikesArr.put(obj);
            }
            data.put("postLikes", postLikesArr);

            // Comment Likes
            JSONArray commentLikesArr = new JSONArray();
            for (CommentLikeEntity cl : commentLikes) {
                JSONObject obj = new JSONObject();
                obj.put("commentId", cl.getCommentId());
                obj.put("personaId", cl.getPersonaId());
                obj.put("createdAt", cl.getCreatedAt());
                commentLikesArr.put(obj);
            }
            data.put("commentLikes", commentLikesArr);

            // Conversations
            JSONArray conversationsArr = new JSONArray();
            for (ConversationEntity conv : conversations) {
                JSONObject obj = new JSONObject();
                obj.put("id", conv.getId());
                obj.put("personaAId", conv.getPersonaAId());
                obj.put("personaBId", conv.getPersonaBId());
                obj.put("createdAt", conv.getCreatedAt());
                obj.put("lastActivityAt", conv.getLastActivityAt());
                obj.put("lastMessagePreview", conv.getLastMessagePreview() != null ? conv.getLastMessagePreview() : "");
                conversationsArr.put(obj);
            }
            data.put("conversations", conversationsArr);

            // Messages
            JSONArray messagesArr = new JSONArray();
            for (MessageEntity m : messages) {
                JSONObject obj = new JSONObject();
                obj.put("id", m.getId());
                obj.put("conversationId", m.getConversationId());
                obj.put("senderPersonaId", m.getSenderPersonaId());
                obj.put("replyToMessageId", m.getReplyToMessageId() != null ? m.getReplyToMessageId() : JSONObject.NULL);
                obj.put("textContent", m.getTextContent());
                obj.put("mediaPath", m.getMediaPath() != null ? m.getMediaPath() : JSONObject.NULL);
                obj.put("mimeType", m.getMimeType() != null ? m.getMimeType() : JSONObject.NULL);
                obj.put("createdAt", m.getCreatedAt());
                obj.put("isEdited", m.isEdited());
                obj.put("isRead", m.isRead());
                messagesArr.put(obj);
            }
            data.put("messages", messagesArr);

            // MediaAssets
            JSONArray mediaAssetsArr = new JSONArray();
            for (MediaAssetEntity a : mediaAssets) {
                JSONObject obj = new JSONObject();
                obj.put("id", a.getId());
                obj.put("relativePath", a.getRelativePath());
                obj.put("mimeType", a.getMimeType());
                obj.put("width", a.getWidth());
                obj.put("height", a.getHeight());
                obj.put("fileSizeBytes", a.getFileSizeBytes());
                obj.put("createdAt", a.getCreatedAt());
                mediaAssetsArr.put(obj);
            }
            data.put("mediaAssets", mediaAssetsArr);

            zos.putNextEntry(new ZipEntry("data.json"));
            zos.write(data.toString(2).getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 3. Media folder packaging
            File filesDir = context.getFilesDir();
            File mediaDir = new File(filesDir, "media");
            int mediaFilesCount = 0;
            if (mediaDir.exists() && mediaDir.isDirectory()) {
                File[] files = mediaDir.listFiles();
                if (files != null) {
                    byte[] buffer = new byte[8192];
                    for (File f : files) {
                        if (f.isFile()) {
                            zos.putNextEntry(new ZipEntry("media/" + f.getName()));
                            try (FileInputStream fis = new FileInputStream(f)) {
                                int length;
                                while ((length = fis.read(buffer)) > 0) {
                                    zos.write(buffer, 0, length);
                                }
                            }
                            zos.closeEntry();
                            mediaFilesCount++;
                        }
                    }
                }
            }

            BackupSummary summary = new BackupSummary(true, "Backup archive exported successfully");
            summary.personasCount = personas.size();
            summary.postsCount = posts.size();
            summary.conversationsCount = conversations.size();
            summary.messagesCount = messages.size();
            summary.mediaFilesCount = mediaFilesCount;
            return summary;

        } catch (Exception e) {
            return new BackupSummary(false, "Export failed: " + e.getMessage());
        }
    }

    public static BackupSummary importBackup(@NonNull Context context, @NonNull AppDatabase db, @NonNull Uri sourceUri, boolean isRestoreMode) {
        try (InputStream is = context.getContentResolver().openInputStream(sourceUri);
             ZipInputStream zis = new ZipInputStream(is)) {

            String manifestJsonStr = null;
            String dataJsonStr = null;
            File filesDir = context.getFilesDir();
            File mediaDir = new File(filesDir, "media");
            if (!mediaDir.exists()) {
                mediaDir.mkdirs();
            }

            byte[] buffer = new byte[8192];
            ZipEntry entry;
            int restoredMediaCount = 0;

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.equals("manifest.json")) {
                    manifestJsonStr = readEntryAsString(zis);
                } else if (name.equals("data.json")) {
                    dataJsonStr = readEntryAsString(zis);
                } else if (name.startsWith("media/") && !entry.isDirectory()) {
                    String fileName = name.substring("media/".length());
                    File target = new File(mediaDir, fileName);
                    try (FileOutputStream fos = new FileOutputStream(target)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    restoredMediaCount++;
                }
                zis.closeEntry();
            }

            if (dataJsonStr == null) {
                return new BackupSummary(false, "Invalid archive: data.json not found");
            }

            JSONObject data = new JSONObject(dataJsonStr);

            // Execute database insertions inside a Room transaction
            int pCount = 0, postCount = 0, convCount = 0, msgCount = 0;

            db.runInTransaction(() -> {
                try {
                    if (isRestoreMode) {
                        db.clearAllTables();
                    }

                    // 1. Personas
                    if (data.has("personas")) {
                        JSONArray pArr = data.getJSONArray("personas");
                        Set<String> existingUsernames = new HashSet<>();
                        for (PersonaEntity existing : db.personaDao().getAllPersonasSync()) {
                            existingUsernames.add(existing.getUsername().toLowerCase());
                        }

                        for (int i = 0; i < pArr.length(); i++) {
                            JSONObject o = pArr.getJSONObject(i);
                            String id = o.getString("id");
                            String displayName = o.getString("displayName");
                            String username = o.getString("username");
                            String bio = o.optString("bio", "");
                            String avatarPath = o.isNull("avatarPath") ? null : o.optString("avatarPath", null);
                            String coverPath = o.isNull("coverPath") ? null : o.optString("coverPath", null);
                            String traits = o.optString("personalityTraits", "");
                            String style = o.optString("speakingStyle", "");
                            String instructions = o.optString("roleplayInstructions", "");
                            String story = o.optString("backgroundStory", "");
                            long createdAt = o.optLong("createdAt", System.currentTimeMillis());
                            long updatedAt = o.optLong("updatedAt", createdAt);

                            if (!isRestoreMode) {
                                // Merge mode conflict handling
                                PersonaEntity existing = db.personaDao().getByIdSync(id);
                                if (existing != null) {
                                    if (updatedAt > existing.getUpdatedAt()) {
                                        existing.setDisplayName(displayName);
                                        existing.setBio(bio);
                                        existing.setAvatarPath(avatarPath);
                                        existing.setCoverPath(coverPath);
                                        existing.setPersonalityTraits(traits);
                                        existing.setSpeakingStyle(style);
                                        existing.setRoleplayInstructions(instructions);
                                        existing.setBackgroundStory(story);
                                        existing.setUpdatedAt(updatedAt);
                                        db.personaDao().update(existing);
                                    }
                                    continue;
                                }

                                // Check username conflict
                                if (existingUsernames.contains(username.toLowerCase())) {
                                    username = username + "_imported";
                                }
                                existingUsernames.add(username.toLowerCase());
                            }

                            PersonaEntity persona = new PersonaEntity(id, displayName, username, bio, avatarPath, coverPath,
                                    traits, style, instructions, story, createdAt, updatedAt);
                            db.personaDao().insert(persona);
                        }
                    }

                    // 2. Posts
                    if (data.has("posts")) {
                        JSONArray postsArr = data.getJSONArray("posts");
                        for (int i = 0; i < postsArr.length(); i++) {
                            JSONObject o = postsArr.getJSONObject(i);
                            String id = o.getString("id");
                            String authorId = o.getString("authorPersonaId");
                            String content = o.getString("textContent");
                            long createdAt = o.optLong("createdAt", System.currentTimeMillis());
                            long updatedAt = o.optLong("updatedAt", createdAt);

                            if (!isRestoreMode && db.postDao().getPostByIdSync(id) != null) {
                                continue;
                            }
                            // Only insert if author exists
                            if (db.personaDao().getByIdSync(authorId) != null) {
                                db.postDao().insertPost(new PostEntity(id, authorId, content, createdAt, updatedAt));
                            }
                        }
                    }

                    // 3. PostMedia
                    if (data.has("postMedia")) {
                        JSONArray mediaArr = data.getJSONArray("postMedia");
                        for (int i = 0; i < mediaArr.length(); i++) {
                            JSONObject o = mediaArr.getJSONObject(i);
                            String id = o.getString("id");
                            String postId = o.getString("postId");
                            String mediaPath = o.getString("mediaPath");
                            String mimeType = o.optString("mimeType", "image/jpeg");
                            int orderIndex = o.optInt("orderIndex", 0);

                            if (db.postDao().getPostByIdSync(postId) != null) {
                                db.postDao().insertMedia(new PostMediaEntity(id, postId, mediaPath, mimeType, orderIndex));
                            }
                        }
                    }

                    // 4. Comments
                    if (data.has("comments")) {
                        JSONArray commentsArr = data.getJSONArray("comments");
                        for (int i = 0; i < commentsArr.length(); i++) {
                            JSONObject o = commentsArr.getJSONObject(i);
                            String id = o.getString("id");
                            String postId = o.getString("postId");
                            String parentId = o.isNull("parentCommentId") ? null : o.optString("parentCommentId", null);
                            String authorId = o.getString("authorPersonaId");
                            String text = o.getString("textContent");
                            long createdAt = o.optLong("createdAt", System.currentTimeMillis());
                            long updatedAt = o.optLong("updatedAt", createdAt);

                            if (db.postDao().getPostByIdSync(postId) != null && db.personaDao().getByIdSync(authorId) != null) {
                                db.commentDao().insert(new CommentEntity(id, postId, parentId, authorId, text, createdAt, updatedAt));
                            }
                        }
                    }

                    // 5. PostLikes
                    if (data.has("postLikes")) {
                        JSONArray postLikesArr = data.getJSONArray("postLikes");
                        for (int i = 0; i < postLikesArr.length(); i++) {
                            JSONObject o = postLikesArr.getJSONObject(i);
                            String postId = o.getString("postId");
                            String personaId = o.getString("personaId");
                            long createdAt = o.optLong("createdAt", System.currentTimeMillis());

                            if (db.postDao().getPostByIdSync(postId) != null && db.personaDao().getByIdSync(personaId) != null) {
                                db.likeDao().insertPostLike(new PostLikeEntity(postId, personaId, createdAt));
                            }
                        }
                    }

                    // 6. CommentLikes
                    if (data.has("commentLikes")) {
                        JSONArray clArr = data.getJSONArray("commentLikes");
                        for (int i = 0; i < clArr.length(); i++) {
                            JSONObject o = clArr.getJSONObject(i);
                            String commentId = o.getString("commentId");
                            String personaId = o.getString("personaId");
                            long createdAt = o.optLong("createdAt", System.currentTimeMillis());

                            if (db.personaDao().getByIdSync(personaId) != null) {
                                db.likeDao().insertCommentLike(new CommentLikeEntity(commentId, personaId, createdAt));
                            }
                        }
                    }

                    // 7. Conversations
                    if (data.has("conversations")) {
                        JSONArray convArr = data.getJSONArray("conversations");
                        for (int i = 0; i < convArr.length(); i++) {
                            JSONObject o = convArr.getJSONObject(i);
                            String id = o.getString("id");
                            String pA = o.getString("personaAId");
                            String pB = o.getString("personaBId");
                            long createdAt = o.optLong("createdAt", System.currentTimeMillis());
                            long lastActivityAt = o.optLong("lastActivityAt", createdAt);
                            String preview = o.optString("lastMessagePreview", "");

                            if (!isRestoreMode && db.conversationDao().getByIdSync(id) != null) {
                                continue;
                            }

                            if (db.personaDao().getByIdSync(pA) != null && db.personaDao().getByIdSync(pB) != null) {
                                db.conversationDao().insert(new ConversationEntity(id, pA, pB, createdAt, lastActivityAt, preview));
                            }
                        }
                    }

                    // 8. Messages
                    if (data.has("messages")) {
                        JSONArray msgArr = data.getJSONArray("messages");
                        for (int i = 0; i < msgArr.length(); i++) {
                            JSONObject o = msgArr.getJSONObject(i);
                            String id = o.getString("id");
                            String convId = o.getString("conversationId");
                            String senderId = o.getString("senderPersonaId");
                            String replyId = o.isNull("replyToMessageId") ? null : o.optString("replyToMessageId", null);
                            String text = o.getString("textContent");
                            String mediaPath = o.isNull("mediaPath") ? null : o.optString("mediaPath", null);
                            String mimeType = o.isNull("mimeType") ? null : o.optString("mimeType", null);
                            long createdAt = o.optLong("createdAt", System.currentTimeMillis());
                            boolean isEdited = o.optBoolean("isEdited", false);
                            boolean isRead = o.optBoolean("isRead", true);

                            if (db.conversationDao().getByIdSync(convId) != null && db.personaDao().getByIdSync(senderId) != null) {
                                db.messageDao().insert(new MessageEntity(id, convId, senderId, replyId, text, mediaPath, mimeType, createdAt, isEdited, isRead));
                            }
                        }
                    }

                    // 9. MediaAssets
                    if (data.has("mediaAssets")) {
                        JSONArray assetArr = data.getJSONArray("mediaAssets");
                        for (int i = 0; i < assetArr.length(); i++) {
                            JSONObject o = assetArr.getJSONObject(i);
                            String id = o.getString("id");
                            String relPath = o.getString("relativePath");
                            String mimeType = o.optString("mimeType", "image/jpeg");
                            int width = o.optInt("width", 0);
                            int height = o.optInt("height", 0);
                            long sizeBytes = o.optLong("fileSizeBytes", 0);
                            long createdAt = o.optLong("createdAt", System.currentTimeMillis());
                            db.mediaAssetDao().insert(new MediaAssetEntity(id, relPath, mimeType, width, height, sizeBytes, createdAt));
                        }
                    }

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            BackupSummary summary = new BackupSummary(true, isRestoreMode ? "Full restore completed successfully" : "Merge import completed successfully");
            summary.personasCount = db.personaDao().getCount();
            summary.postsCount = db.postDao().getPostCount();
            summary.conversationsCount = db.conversationDao().getCount();
            summary.messagesCount = db.messageDao().getCount();
            summary.mediaFilesCount = restoredMediaCount;
            return summary;

        } catch (Exception e) {
            return new BackupSummary(false, "Import failed: " + e.getMessage());
        }
    }

    private static String readEntryAsString(ZipInputStream zis) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int read;
        while ((read = zis.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }
}
