package com.example.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.data.AppDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MediaHelper {

    private static final String TAG = "MediaHelper";
    private static final int MAX_DIMENSION = 1920;
    private static final int WEBP_QUALITY = 80;

    /**
     * Resolves an image path from direct path, relative path, or old legacy device path.
     * Handles missing paths, directory changes, and .webp/.jpg extension variations.
     */
    @Nullable
    public static File resolveFile(@NonNull Context context, @Nullable String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            return null;
        }

        // 1. Direct file check
        File direct = new File(rawPath);
        if (direct.exists() && direct.isFile() && direct.length() > 0) {
            return direct;
        }

        File mediaDir = new File(context.getFilesDir(), "media");
        String filename = direct.getName();

        // 2. Check inside filesDir/media/<filename>
        File inMedia = new File(mediaDir, filename);
        if (inMedia.exists() && inMedia.isFile() && inMedia.length() > 0) {
            return inMedia;
        }

        // 3. Check inside filesDir/<filename>
        File inFilesDir = new File(context.getFilesDir(), filename);
        if (inFilesDir.exists() && inFilesDir.isFile() && inFilesDir.length() > 0) {
            return inFilesDir;
        }

        // 4. Try alternate extension if format was changed (.webp <-> .jpg <-> .png)
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            String base = filename.substring(0, dotIndex);
            String[] alternates = new String[]{".webp", ".jpg", ".jpeg", ".png"};
            for (String ext : alternates) {
                File altFile = new File(mediaDir, base + ext);
                if (altFile.exists() && altFile.isFile() && altFile.length() > 0) {
                    return altFile;
                }
            }
        }

        return null;
    }

    /**
     * Safely loads and scales a Bitmap using inSampleSize to prevent OutOfMemory errors.
     */
    @Nullable
    public static Bitmap loadBitmap(@NonNull Context context, @Nullable String rawPath, int maxTargetDimension) {
        File file = resolveFile(context, rawPath);
        if (file == null) {
            return null;
        }

        try {
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), boundsOptions);

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return null;
            }

            int inSampleSize = 1;
            int maxDim = Math.max(boundsOptions.outWidth, boundsOptions.outHeight);
            if (maxTargetDimension > 0 && maxDim > maxTargetDimension) {
                inSampleSize = Math.round((float) maxDim / (float) maxTargetDimension);
            }

            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = Math.max(1, inSampleSize);
            decodeOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), decodeOptions);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to load bitmap for: " + rawPath, t);
            return null;
        }
    }

    /**
     * Computes the SHA-256 hash of a file.
     */
    @Nullable
    public static String computeSha256(@NonNull File file) {
        if (!file.exists() || !file.isFile()) return null;
        try (InputStream is = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int read;
            while ((read = is.read(buf)) != -1) {
                digest.update(buf, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            Log.e(TAG, "Failed to compute SHA-256 for: " + file.getAbsolutePath(), e);
            return null;
        }
    }

    /**
     * Computes the SHA-256 hash of byte array data.
     */
    @Nullable
    public static String computeSha256(@NonNull byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            Log.e(TAG, "Failed to compute SHA-256 for data", e);
            return null;
        }
    }

    /**
     * Compresses an image picked from URI into WebP Lossy format and saves to filesDir/media/.
     * Includes SHA-256 deduplication: if an identical image already exists in media/,
     * the existing canonical file path is returned instead of duplicating storage.
     */
    @Nullable
    public static String compressAndSaveWebP(@NonNull Context context, @NonNull Uri sourceUri, @NonNull String prefix) {
        try {
            byte[] rawBytes;
            try (InputStream is = context.getContentResolver().openInputStream(sourceUri)) {
                if (is == null) return null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int read;
                while ((read = is.read(buf)) != -1) {
                    baos.write(buf, 0, read);
                }
                rawBytes = baos.toByteArray();
            }

            if (rawBytes.length == 0) return null;

            File mediaDir = new File(context.getFilesDir(), "media");
            if (!mediaDir.exists()) mediaDir.mkdirs();

            // 1. Check if an identical file already exists in media/ via SHA-256
            String incomingSha = computeSha256(rawBytes);
            if (incomingSha != null && mediaDir.exists()) {
                File[] existingFiles = mediaDir.listFiles();
                if (existingFiles != null) {
                    for (File f : existingFiles) {
                        if (f.isFile() && f.length() == rawBytes.length) {
                            String existingSha = computeSha256(f);
                            if (incomingSha.equalsIgnoreCase(existingSha)) {
                                Log.d(TAG, "Deduplication: Reusing existing image " + f.getName());
                                return f.getAbsolutePath();
                            }
                        }
                    }
                }
            }

            // 2. Decode with sample bounds to keep max dimension <= MAX_DIMENSION (1920)
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.length, boundsOptions);

            int inSampleSize = 1;
            int maxDim = Math.max(boundsOptions.outWidth, boundsOptions.outHeight);
            if (maxDim > MAX_DIMENSION) {
                inSampleSize = Math.round((float) maxDim / (float) MAX_DIMENSION);
            }

            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = Math.max(1, inSampleSize);
            Bitmap bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.length, decodeOptions);
            if (bitmap == null) return null;

            // 3. Check if compressed bitmap matches an existing WebP in media/
            ByteArrayOutputStream webpStream = new ByteArrayOutputStream();
            Bitmap.CompressFormat format;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                format = Bitmap.CompressFormat.WEBP_LOSSY;
            } else {
                format = Bitmap.CompressFormat.WEBP;
            }
            bitmap.compress(format, WEBP_QUALITY, webpStream);
            byte[] webpBytes = webpStream.toByteArray();
            String webpSha = computeSha256(webpBytes);

            if (webpSha != null) {
                File[] existingFiles = mediaDir.listFiles();
                if (existingFiles != null) {
                    for (File f : existingFiles) {
                        if (f.isFile() && f.length() == webpBytes.length) {
                            String fSha = computeSha256(f);
                            if (webpSha.equalsIgnoreCase(fSha)) {
                                Log.d(TAG, "Deduplication: WebP content already exists in " + f.getName());
                                bitmap.recycle();
                                return f.getAbsolutePath();
                            }
                        }
                    }
                }
            }

            // 4. Save new WebP file
            String newFilename = prefix + "_" + UUID.randomUUID().toString() + ".webp";
            File targetFile = new File(mediaDir, newFilename);
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(webpBytes);
            }

            bitmap.recycle();
            return targetFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Error compressing image to WebP", e);
            return null;
        }
    }

    /**
     * Scans the media directory, finds files with identical SHA-256 hashes,
     * keeps the earliest canonical file, re-points all Room database entity references,
     * and deletes duplicate files from disk.
     */
    public static int deduplicateMediaDatabase(@NonNull Context context, @NonNull AppDatabase db) {
        File mediaDir = new File(context.getFilesDir(), "media");
        if (!mediaDir.exists() || !mediaDir.isDirectory()) {
            return 0;
        }

        File[] files = mediaDir.listFiles();
        if (files == null || files.length <= 1) {
            return 0;
        }

        // Group files by SHA-256
        Map<String, List<File>> hashMap = new HashMap<>();
        for (File f : files) {
            if (f.isFile() && f.length() > 0) {
                String sha = computeSha256(f);
                if (sha != null) {
                    List<File> list = hashMap.get(sha);
                    if (list == null) {
                        list = new ArrayList<>();
                        hashMap.put(sha, list);
                    }
                    list.add(f);
                }
            }
        }

        int removedDuplicatesCount = 0;

        for (Map.Entry<String, List<File>> entry : hashMap.entrySet()) {
            List<File> group = entry.getValue();
            if (group.size() <= 1) continue;

            // Sort by last modified time ascending (oldest/first is canonical)
            Collections.sort(group, Comparator.comparingLong(File::lastModified));

            File canonicalFile = group.get(0);
            String canonicalAbsolutePath = canonicalFile.getAbsolutePath();
            String canonicalRelName = canonicalFile.getName();

            for (int i = 1; i < group.size(); i++) {
                File duplicate = group.get(i);
                String dupAbsolutePath = duplicate.getAbsolutePath();
                String dupRelName = duplicate.getName();

                // Re-point database references in personas, post_media, messages
                db.personaDao().updateAvatarPath(dupAbsolutePath, canonicalAbsolutePath);
                db.personaDao().updateCoverPath(dupAbsolutePath, canonicalAbsolutePath);
                db.personaDao().updateAvatarPath("media/" + dupRelName, canonicalAbsolutePath);
                db.personaDao().updateCoverPath("media/" + dupRelName, canonicalAbsolutePath);
                db.personaDao().updateAvatarPath(dupRelName, canonicalAbsolutePath);
                db.personaDao().updateCoverPath(dupRelName, canonicalAbsolutePath);

                db.postDao().updatePostMediaPath(dupAbsolutePath, canonicalAbsolutePath);
                db.postDao().updatePostMediaPath("media/" + dupRelName, canonicalAbsolutePath);
                db.postDao().updatePostMediaPath(dupRelName, canonicalAbsolutePath);

                db.messageDao().updateMessageMediaPath(dupAbsolutePath, canonicalAbsolutePath);
                db.messageDao().updateMessageMediaPath("media/" + dupRelName, canonicalAbsolutePath);
                db.messageDao().updateMessageMediaPath(dupRelName, canonicalAbsolutePath);

                // Delete the duplicate file
                if (duplicate.delete()) {
                    removedDuplicatesCount++;
                    Log.d(TAG, "Deduplicated and removed: " + dupRelName + " -> kept " + canonicalRelName);
                }
            }
        }

        return removedDuplicatesCount;
    }
}
