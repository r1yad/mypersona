# Image Deduplication, WebP Compression, Legacy Backup Restoration, Natural Aspect Ratio & System Insets Plan

Implement SHA-256 content deduplication for image storage, high-efficiency WebP lossy compression on upload, path and format normalization for legacy `.webp` backups, natural aspect ratio scaling for timeline and post views, and system window insets clearance across all screens.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following implementation specifications have been confirmed based on your requirements:
> - **SHA-256 Image Deduplication**:
>   - *On Upload*: Calculate SHA-256 hash of image data before persisting. If an existing image in `filesDir/media/` matches the hash, reuse the canonical file path rather than creating a duplicate.
>   - *On Backup Import*: Run an automatic deduplication pass during backup extraction/restore. Scan all restored media files, identify duplicate SHA-256 hashes, re-point all Room database references (`personas.avatarPath`, `coverPath`, `post_media.mediaPath`, `messages.mediaPath`, `media_assets.relativePath`) to the first/canonical file, and delete the redundant duplicate files.
>   - *Canonical Retention*: Retain the first encountered file path for matching SHA-256 hashes and cleanly re-link all referencing entities.
> - **WebP Lossy Compression**: Compress newly uploaded avatars and post attachments into WebP format (quality 80, downscaled if exceeding 1920px max dimension).
> - **Legacy Backup Image Restoration**: Bridge legacy `.webp` image paths (absolute paths from previous devices or relative `media/` paths) via path normalization so old character avatars and media display seamlessly.
> - **Natural Aspect Ratio**: Remove fixed 200dp cropping (`centerCrop`) and enable `adjustViewBounds="true"` with `scaleType="fitCenter"` so photos fill screen width naturally without cut-offs.
> - **System Window Insets**: Programmatically bind status bar top insets and navigation bar bottom insets via `ViewCompat.setOnApplyWindowInsetsListener` across all activities.

---

### 1. Overview & Core Concept

1. **SHA-256 Content Deduplication**:
   Users frequently upload the same picture for different characters or multiple posts, and backup archives can accumulate redundant duplicates under different filenames. Using cryptographic SHA-256 hashing:
   - Newly picked images are checked against existing media hashes; matches reuse the existing local file.
   - Restoring backups automatically runs a deduplication sweep: identical files are merged, Room entity foreign paths are updated in a single transaction, and orphan duplicate files are removed from disk.
2. **WebP Compression on Upload**:
   When choosing photos from the device gallery, the image is decoded with memory-efficient sample bounds, converted into WebP format (`Bitmap.CompressFormat.WEBP` / `WEBP_LOSSY`), and saved to internal app storage.
3. **Legacy Backup WebP Compatibility**:
   A path-resolution pipeline resolves legacy absolute paths (e.g. `/data/user/0/com.previous.app/files/media/...`), relative paths (`media/...`), and handles `.webp` and `.jpg` extension variations.
4. **Uncropped Natural Aspect Ratio**:
   Post cards and detail views display images in their native aspect ratio (portrait, landscape, square) bounded by screen width.
5. **System Bar Inset Clearance**:
   Status bar and navigation bar insets are explicitly applied to top app bars, back buttons, and bottom action rows across all activities.

---

### 2. User Experience & Visual Design

- **Storage Efficiency & Integrity**:
  - Re-uploading or restoring duplicate images consumes zero extra disk space.
  - Avatars and post images remain sharp and render fast from unified WebP assets.
- **Flawless Header & Navigation Clearance**:
  - Active persona chip and title in `MainActivity` sit cleanly below the system status icons.
  - Back buttons, title text, and action buttons in `PersonaProfileActivity`, `PersonaEditActivity`, `PostDetailActivity`, and `ChatActivity` have full 48dp minimum touch target clearance without overlapping status bar indicators.
  - Floating bottom input bars in `ChatActivity` and `PostDetailActivity` respect the navigation bar and keyboard insets.
- **Responsive Media Timeline**:
  - Full-width images scale smoothly to their true aspect ratio.
  - Vertical photos (e.g. character art, portrait captures) are never awkwardly truncated at 200dp.

---

### 3. Key Product Decisions & Trade-Offs

- **Hash-Based Content Addressing vs. Filename Matching**:
  - *Chosen Approach*: Compute SHA-256 on byte streams.
  - *Why*: Duplicate images in user backups or exports have different filenames (e.g. `avatar_a1b2.webp` vs `post_c3d4.webp` or `IMG_2026.jpg`). Only SHA-256 provides 100% collision-free identification of identical media content.
- **Automatic Auto-Clean During Import**:
  - *Chosen Approach*: Execute deduplication immediately inside the Room restore transaction.
  - *Why*: Eliminates duplicates before they can fragment the database and frees up device storage immediately upon backup completion without requiring extra manual steps from the user.
- **Preserve First Encountered File**:
  - *Chosen Approach*: Keep the existing first file on disk and re-point all subsequent duplicate references to it.
  - *Why*: Preserves file timestamps, avoids file-rename race conditions, and requires only fast SQL `UPDATE` statements for referencing rows.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   Image Selection / Restore            │
├────────────────────────────────────────────────────────┤
│ 1. Read Stream & Compute SHA-256 Digest                │
│    MessageDigest.getInstance("SHA-256")                │
├────────────────────────────────────────────────────────┤
│ 2. Check Existing Media Hashes in Media Directory      │
│    ┌───────────────────────┬─────────────────────────┐ │
│    │ MATCH FOUND           │ NO MATCH                │ │
│    │ Reuse canonical file  │ Compress to WebP Lossy  │ │
│    │ Re-point DB reference │ Save new .webp file     │ │
│    └───────────────────────┴─────────────────────────┘ │
├────────────────────────────────────────────────────────┤
│ 3. Database Sync & Reference Updates (Room Transaction)│
│    - Update personas.avatarPath / coverPath            │
│    - Update post_media.mediaPath                       │
│    - Update messages.mediaPath                         │
│    - Delete duplicate file on disk                     │
├────────────────────────────────────────────────────────┤
│ 4. UI Rendering (MediaHelper.resolveFile & Aspect)     │
│    - adjustViewBounds="true", scaleType="fitCenter"    │
│    - ViewCompat status bar and navigation bar insets   │
└────────────────────────────────────────────────────────┘
```

#### Detailed Execution Steps

1. **`MediaHelper.java`**:
   - `calculateSha256(File file)` and `calculateSha256(InputStream is)`: computes 64-char hex string.
   - `findFileBySha256(Context context, String sha256)`: scans `getFilesDir()/media/` for an existing matching file.
   - `compressAndSaveWebP(Context context, Uri uri, String prefix)`:
     - Calculates SHA-256 of incoming stream.
     - If matching file exists, returns existing file's absolute path.
     - Otherwise, decodes bitmap with downsampling (max dimension 1920px), compresses to WebP Lossy (quality 80), and writes to `media/<prefix>_<uuid>.webp`.
   - `resolveFile(Context context, String path)`:
     - Handles direct file paths.
     - Normalizes legacy backup paths (`/data/user/0/...` -> local `files/media/<filename>`).
     - Tries `.webp` / `.jpg` fallback extensions.
   - `deduplicateMediaDatabase(Context context, AppDatabase db)`:
     - Scans all files in `media/`, groups by SHA-256.
     - For duplicate groups: elects canonical first file, re-points referencing rows in `PersonaEntity`, `PostMediaEntity`, and `MessageEntity`, then deletes duplicate files from disk.

2. **`BackupManager.java`**:
   - During `importBackup`, normalize all file paths to local app storage.
   - Run `MediaHelper.deduplicateMediaDatabase(context, db)` within the import process to automatically deduplicate restored assets.

3. **UI Layouts & Insets**:
   - `item_post_card.xml` & `activity_post_detail.xml`: Set `layout_height="wrap_content"`, `adjustViewBounds="true"`, `scaleType="fitCenter"`, and `maxHeight="520dp"` on media views.
   - `MainActivity`, `PostDetailActivity`, `PersonaProfileActivity`, `PersonaEditActivity`, `ChatActivity`: Apply `ViewCompat.setOnApplyWindowInsetsListener` to inject status bar top padding into top toolbars/headers and navigation bar bottom padding into bottom action rows.
