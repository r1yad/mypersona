package com.example.ui.settings;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.data.AppDatabase;
import com.example.data.entity.CommentEntity;
import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostEntity;
import com.example.data.entity.PostLikeEntity;
import com.example.data.entity.PostMediaEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DebugContentGenerator {

    private static final String TAG = "DebugContentGenerator";

    public static class GenerationResult {
        public int personasCreated = 0;
        public int postsCreated = 0;
        public int commentsCreated = 0;
    }

    /**
     * Generates a full suite of isolated sample personas, multi-image posts, comments, and likes
     * into the specified database (the isolated debugDatabase).
     */
    public static GenerationResult generateFullSampleData(@NonNull Context context, @NonNull AppDatabase db) {
        GenerationResult result = new GenerationResult();

        // 1. Generate 5 distinct personas
        List<PersonaEntity> personas = generateSamplePersonas(context, db);
        result.personasCreated = personas.size();

        if (personas.isEmpty()) {
            return result;
        }

        // 2. Generate sample posts (text-only and multi-image)
        List<PostEntity> posts = generateSamplePosts(context, db, personas);
        result.postsCreated = posts.size();

        // 3. Generate comments, replies, and likes between test personas
        result.commentsCreated = generateSampleInteractions(db, personas, posts);

        return result;
    }

    public static List<PersonaEntity> generateSamplePersonas(@NonNull Context context, @NonNull AppDatabase db) {
        List<PersonaEntity> list = new ArrayList<>();
        long now = System.currentTimeMillis();

        String[][] personaSpecs = {
                {
                        "Astrid Nova",
                        "astrid_nova",
                        "Astrobiologist investigating bioluminescent exoplanet flora on Deep Station 9.",
                        "Curious, analytical, scientific, intrepid",
                        "Crisp, analytical, marvels at organic mysteries",
                        "Speaks with quiet scientific passion, referencing cellular patterns and planetary drift",
                        "Graduated top of her class from the Orbital Academy before volunteering for deep-space outpost assignments.",
                        "#00E5FF", "#1A237E" // Cyan to Navy
                },
                {
                        "Kaelen Drake",
                        "kaelen_drake",
                        "Airship navigator charting storm passages over the Cloud Spires.",
                        "Daring, witty, charismatic, loyal",
                        "Swashbuckling, peppered with aeronautical slang and bravado",
                        "Laughs in the face of gale winds, tells tall navigational tales, values crew trust above all",
                        "Inherited the steam-cutter 'Zephyr' from his grandfather and has never looked down since.",
                        "#FF6D00", "#D50000" // Orange to Red
                },
                {
                        "Seraphina Vale",
                        "seraphina_v",
                        "Moonlit herbalist crafting dream infusions and nocturnal tinctures.",
                        "Mysterious, soothing, enigmatic, gentle",
                        "Soft, poetic cadences steeped in twilight metaphors",
                        "Speaks gently as if not to wake sleeping flowers; dispenses subtle herbal wisdom",
                        "Trained in the secluded Whispering Hollow where plants bloom only under starlight.",
                        "#7C4DFF", "#311B92" // Violet to Deep Purple
                },
                {
                        "Glitch-88",
                        "glitch_88",
                        "Sentient maintenance drone with an affinity for street poetry and neon art.",
                        "Philosophical, sarcastic, hyper-logical, dry",
                        "Technical jargon interspersed with surprisingly poignant observations",
                        "Monitors system telemetry while questioning the existential parameters of artificial consciousness",
                        "Constructed in an automated scrap foundry before a stray power surge triggered autonomous sapience.",
                        "#00E676", "#1B5E20" // Neon Green to Forest
                },
                {
                        "Professor Bramble",
                        "prof_bramble",
                        "Antiquarian scholar searching for lost clockwork relics and odd teapots.",
                        "Eccentric, enthusiastic, absent-minded, cheerful",
                        "Energetic, rambling, full of whimsical exclamation marks",
                        "Frequently loses train of thought mid-sentence when spotting something shiny or brass",
                        "Spent forty years cataloging mundane enchanted kitchen utensils across three continents.",
                        "#FFD600", "#FF6F00" // Gold to Amber
                }
        };

        for (int i = 0; i < personaSpecs.length; i++) {
            String[] s = personaSpecs[i];
            String id = "debug_persona_" + (i + 1) + "_" + UUID.randomUUID().toString().substring(0, 6);
            String avatarPath = createSampleImage(context, "debug_avatar_" + (i + 1) + ".webp", s[0], s[7], s[8], true);

            PersonaEntity persona = new PersonaEntity(
                    id,
                    s[0],
                    s[1],
                    s[2],
                    avatarPath,
                    null,
                    s[3],
                    s[4],
                    s[5],
                    s[6],
                    now - (i * 3600000L),
                    now
            );

            db.personaDao().insert(persona);
            list.add(persona);
        }

        return list;
    }

    public static List<PostEntity> generateSamplePosts(@NonNull Context context, @NonNull AppDatabase db, @NonNull List<PersonaEntity> personas) {
        List<PostEntity> createdPosts = new ArrayList<>();
        if (personas.isEmpty()) return createdPosts;

        long now = System.currentTimeMillis();

        // Specifications for 8 sample posts
        Object[][] postSpecs = {
                {
                        0, // Astrid
                        "Discovered a spore cluster in hydroponics that pulses in rhythm with the engine core. Either it has rhythm, or the warp drive is humming a lullaby. Testing biological luminescence at 430nm.",
                        new String[]{"debug_post_biolum_1.webp"},
                        new String[][]{{"#00E5FF", "#004D40", "Bio-Spore 430nm"}}
                },
                {
                        1, // Kaelen
                        "Just cleared the Devil's Chasm with half a wing sail intact! Never bet against the Zephyr's starboard thrusters. Sky is electric violet tonight.",
                        new String[]{"debug_post_sky_1.webp", "debug_post_sky_2.webp"},
                        new String[][]{{"#FF6D00", "#3E2723", "Cloud Spires Peak"}, {"#7C4DFF", "#212121", "Aurora Stream"}}
                },
                {
                        2, // Seraphina
                        "Brewed the midnight lavender infusion. Drink three drops before sleep and your dreams will taste like starlight and quiet rain.",
                        new String[]{"debug_post_herbs_1.webp"},
                        new String[][]{{"#B388FF", "#311B92", "Nocturnal Tincture"}}
                },
                {
                        3, // Glitch-88
                        "LOG ENTRY 404: Human emotions analyzed. Result: inefficient, highly volatile, yet aesthetically captivating. Commencing neon mural project on Sector 7 firewall.",
                        new String[]{},
                        new String[][]{} // Text only
                },
                {
                        4, // Prof. Bramble
                        "Found a brass astrolabe inside a Victorian teapot today! The antique dealer claimed it only points to missing socks, but I calibrated the celestial dial and it points directly to the Northern Star!",
                        new String[]{"debug_post_brass_1.webp", "debug_post_brass_2.webp", "debug_post_brass_3.webp"},
                        new String[][]{{"#FFD600", "#4E342E", "Astrolabe Dial"}, {"#FF9100", "#212121", "Gilded Teapot"}, {"#FFAB00", "#1A237E", "Starmap Chart"}}
                },
                {
                        1, // Kaelen
                        "Evening checklist: Rudder cables tensioned, steam boilers pressurized, tea kettle on the burner. All clear for the midnight drift across Sector 4.",
                        new String[]{},
                        new String[][]{} // Text only
                },
                {
                        0, // Astrid
                        "A quiet dusk in the research dome. Sometimes the best research is simply listening to the silence between the stars.",
                        new String[]{"debug_post_dome_1.webp"},
                        new String[][]{{"#00B0FF", "#01579B", "Observatory Dome"}}
                },
                {
                        4, // Prof. Bramble
                        "The grandfather clock in the main gallery struck thirteen times today! Either standard spacetime has expanded or the escapement gear needs a spot of oil again.",
                        new String[]{},
                        new String[][]{} // Text only
                }
        };

        for (int i = 0; i < postSpecs.length; i++) {
            Object[] spec = postSpecs[i];
            int personaIndex = (int) spec[0];
            if (personaIndex >= personas.size()) personaIndex = 0;

            PersonaEntity author = personas.get(personaIndex);
            String text = (String) spec[1];
            String[] imgNames = (String[]) spec[2];
            String[][] imgThemes = (String[][]) spec[3];

            String postId = "debug_post_" + (i + 1) + "_" + UUID.randomUUID().toString().substring(0, 6);
            long postTime = now - (i * 1800000L); // Spaced out every 30 minutes

            PostEntity post = new PostEntity(postId, author.getId(), text, postTime, postTime);
            db.postDao().insertPost(post);
            createdPosts.add(post);

            // Create media entities if post has images
            if (imgNames != null && imgNames.length > 0) {
                for (int m = 0; m < imgNames.length; m++) {
                    String imgName = imgNames[m];
                    String[] theme = imgThemes[m];
                    String path = createSampleImage(context, imgName, theme[2], theme[0], theme[1], false);
                    if (path != null) {
                        PostMediaEntity media = new PostMediaEntity(
                                "debug_media_" + UUID.randomUUID().toString().substring(0, 8),
                                postId,
                                path,
                                "image/webp",
                                m
                        );
                        db.postDao().insertMedia(media);
                    }
                }
            }
        }

        return createdPosts;
    }

    public static int generateSampleInteractions(@NonNull AppDatabase db,
                                                @NonNull List<PersonaEntity> personas,
                                                @NonNull List<PostEntity> posts) {
        if (personas.size() < 2 || posts.isEmpty()) return 0;
        int count = 0;
        long now = System.currentTimeMillis();

        // Add likes across posts
        for (int p = 0; p < posts.size(); p++) {
            PostEntity post = posts.get(p);
            // Give 1 to 3 likes from other personas
            for (int k = 0; k < personas.size(); k++) {
                if ((p + k) % 2 == 0) {
                    PersonaEntity liker = personas.get(k);
                    db.likeDao().insertPostLike(new PostLikeEntity(post.getId(), liker.getId(), now - 100000L));
                }
            }
        }

        // Add threaded comments and replies
        // Post 0: Astrid's spore post
        if (posts.size() > 0) {
            PostEntity p0 = posts.get(0);
            PersonaEntity glitch = personas.get(Math.min(3, personas.size() - 1));
            PersonaEntity astrid = personas.get(0);

            String commentId1 = "debug_comment_1_" + UUID.randomUUID().toString().substring(0, 6);
            CommentEntity c1 = new CommentEntity(
                    commentId1,
                    p0.getId(),
                    null,
                    glitch.getId(),
                    "Biomass oscillation detected. Recommend non-contact observation protocols.",
                    now - 600000L,
                    now - 600000L
            );
            db.commentDao().insert(c1);
            count++;

            // Nested reply from Astrid
            String replyId1 = "debug_reply_1_" + UUID.randomUUID().toString().substring(0, 6);
            CommentEntity r1 = new CommentEntity(
                    replyId1,
                    p0.getId(),
                    commentId1,
                    astrid.getId(),
                    "Too late, I already named it Bartholomew and it seems fond of classical music.",
                    now - 300000L,
                    now - 300000L
            );
            db.commentDao().insert(r1);
            count++;
        }

        // Post 1: Kaelen's sky post
        if (posts.size() > 1) {
            PostEntity p1 = posts.get(1);
            PersonaEntity seraphina = personas.get(Math.min(2, personas.size() - 1));
            PersonaEntity kaelen = personas.get(1);

            String commentId2 = "debug_comment_2_" + UUID.randomUUID().toString().substring(0, 6);
            CommentEntity c2 = new CommentEntity(
                    commentId2,
                    p1.getId(),
                    null,
                    seraphina.getId(),
                    "Does that steam tea help with the cloud sickness? The barometric drops looked severe.",
                    now - 500000L,
                    now - 500000L
            );
            db.commentDao().insert(c2);
            count++;

            String replyId2 = "debug_reply_2_" + UUID.randomUUID().toString().substring(0, 6);
            CommentEntity r2 = new CommentEntity(
                    replyId2,
                    p1.getId(),
                    commentId2,
                    kaelen.getId(),
                    "A couple drops of mountain mint in hot water works like a charm, Miss Vale!",
                    now - 200000L,
                    now - 200000L
            );
            db.commentDao().insert(r2);
            count++;
        }

        return count;
    }

    /**
     * Programmatically creates a colorful, high-quality sample image with smooth gradients
     * and labels, saved as a WebP image to test media sliders and avatar frames.
     */
    private static String createSampleImage(@NonNull Context context,
                                            @NonNull String fileName,
                                            @NonNull String label,
                                            @NonNull String colorStartHex,
                                            @NonNull String colorEndHex,
                                            boolean isAvatar) {
        try {
            File mediaDir = new File(context.getFilesDir(), "media");
            if (!mediaDir.exists()) {
                mediaDir.mkdirs();
            }

            File targetFile = new File(mediaDir, fileName);
            if (targetFile.exists() && targetFile.length() > 0) {
                return targetFile.getAbsolutePath();
            }

            int width = isAvatar ? 300 : 720;
            int height = isAvatar ? 300 : 540;

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            int colorStart = Color.parseColor(colorStartHex);
            int colorEnd = Color.parseColor(colorEndHex);

            // Background Gradient
            Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            LinearGradient gradient = new LinearGradient(
                    0, 0, width, height,
                    new int[]{colorStart, colorEnd},
                    null,
                    Shader.TileMode.CLAMP
            );
            bgPaint.setShader(gradient);
            canvas.drawRect(0, 0, width, height, bgPaint);

            // Decorative shapes
            Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            accentPaint.setColor(Color.WHITE);
            accentPaint.setAlpha(35);

            if (isAvatar) {
                // Circular concentric ring
                canvas.drawCircle(width / 2f, height / 2f, width * 0.38f, accentPaint);
                accentPaint.setAlpha(60);
                canvas.drawCircle(width / 2f, height / 2f, width * 0.22f, accentPaint);

                // Initial Letter
                Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(height * 0.38f);
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setFakeBoldText(true);
                String initial = label.isEmpty() ? "?" : label.substring(0, 1).toUpperCase();
                float yPos = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);
                canvas.drawText(initial, width / 2f, yPos, textPaint);

            } else {
                // Post Landscape decorative art
                canvas.drawCircle(width * 0.8f, height * 0.25f, height * 0.35f, accentPaint);
                accentPaint.setAlpha(20);
                canvas.drawRoundRect(new RectF(width * 0.1f, height * 0.45f, width * 0.9f, height * 0.88f), 24f, 24f, accentPaint);

                // Title label pill
                Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(32f);
                textPaint.setFakeBoldText(true);
                textPaint.setTextAlign(Paint.Align.CENTER);

                float yPos = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);
                canvas.drawText(label, width / 2f, yPos, textPaint);

                // Sample Watermark
                Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                subTextPaint.setColor(Color.WHITE);
                subTextPaint.setAlpha(180);
                subTextPaint.setTextSize(20f);
                subTextPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("✦ TEST DATA • ISOLATED DEBUG ASSET ✦", width / 2f, height - 36f, subTextPaint);
            }

            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 85, fos);
            }

            bitmap.recycle();
            return targetFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Failed to create sample image: " + fileName, e);
            return null;
        }
    }
}
