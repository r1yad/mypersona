package com.example.ui.persona;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.data.entity.PersonaEntity;
import com.example.data.repository.AppRepository;
import com.example.databinding.ActivityPersonaEditBinding;
import com.example.util.LiquidGlassHelper;
import com.example.util.MediaHelper;

import java.util.UUID;

public class PersonaEditActivity extends AppCompatActivity {

    public static final String EXTRA_PERSONA_ID = "extra_persona_id";

    private ActivityPersonaEditBinding binding;
    private AppRepository repository;
    private PersonaEntity existingPersona;
    private String selectedAvatarPath = null;
    private String editingPersonaId = null;

    private final ActivityResultLauncher<String> avatarPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    saveAvatarToInternalStorage(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LiquidGlassHelper.enableEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        binding = ActivityPersonaEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = AppRepository.getInstance(this);

        LiquidGlassHelper.applyTopInsets(binding.topBar);
        LiquidGlassHelper.applyImeBottomInsets(binding.scrollPersonaEdit);
        LiquidGlassHelper.attachPressFeedback(
                binding.btnEditBack,
                binding.btnSavePersona,
                binding.btnDeletePersona,
                binding.layoutAvatarPicker,
                binding.chipPresetMystic,
                binding.chipPresetSarcastic,
                binding.chipPresetRoyal,
                binding.chipPresetCyberpunk,
                binding.chipPresetWhimsical
        );

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                LiquidGlassHelper.finishWithGlassTransition(PersonaEditActivity.this);
            }
        });

        editingPersonaId = getIntent().getStringExtra(EXTRA_PERSONA_ID);
        if (editingPersonaId != null && !editingPersonaId.trim().isEmpty()) {
            editingPersonaId = editingPersonaId.trim();
            binding.textEditTitle.setText("Edit Persona");
            binding.btnDeletePersona.setVisibility(View.VISIBLE);
            loadExistingPersona(editingPersonaId);
        } else {
            editingPersonaId = null;
            binding.textEditTitle.setText("Create Persona");
            binding.btnDeletePersona.setVisibility(View.GONE);
        }

        binding.btnEditBack.setOnClickListener(v -> LiquidGlassHelper.finishWithGlassTransition(this));
        binding.btnSavePersona.setOnClickListener(v -> savePersona());
        binding.btnDeletePersona.setOnClickListener(v -> confirmDelete());

        binding.layoutAvatarPicker.setOnClickListener(v -> avatarPickerLauncher.launch("image/*"));
        binding.textAvatarHint.setOnClickListener(v -> avatarPickerLauncher.launch("image/*"));

        setupPresetChips();
    }

    private void loadExistingPersona(String personaId) {
        binding.btnSavePersona.setEnabled(false);
        repository.getExecutor().execute(() -> {
            existingPersona = repository.getDatabase().personaDao().getByIdSync(personaId);
            if (existingPersona != null) {
                selectedAvatarPath = existingPersona.getAvatarPath();
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    binding.editDisplayName.setText(existingPersona.getDisplayName());
                    binding.editUsername.setText(existingPersona.getUsername());
                    binding.editBio.setText(existingPersona.getBio());
                    binding.editPersonalityTraits.setText(existingPersona.getPersonalityTraits());
                    binding.editSpeakingStyle.setText(existingPersona.getSpeakingStyle());
                    binding.editRoleplayInstructions.setText(existingPersona.getRoleplayInstructions());
                    binding.editBackgroundStory.setText(existingPersona.getBackgroundStory());

                    if (selectedAvatarPath != null) {
                        Bitmap bm = MediaHelper.loadBitmap(this, selectedAvatarPath, 400);
                        if (bm != null) {
                            binding.imageEditAvatar.setImageBitmap(bm);
                            binding.textAvatarHint.setText("Tap to change profile picture");
                        }
                    }
                    binding.btnSavePersona.setEnabled(true);
                });
            } else {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    binding.btnSavePersona.setEnabled(true);
                });
            }
        });
    }

    private void saveAvatarToInternalStorage(Uri uri) {
        try {
            String path = MediaHelper.compressAndSaveWebP(this, uri, "avatar");
            if (path != null) {
                selectedAvatarPath = path;
                Bitmap bm = MediaHelper.loadBitmap(this, path, 400);
                if (bm != null) {
                    binding.imageEditAvatar.setImageBitmap(bm);
                    binding.textAvatarHint.setText("Profile picture selected (tap to change)");
                }
            } else {
                Toast.makeText(this, "Failed to compress profile picture", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPresetChips() {
        binding.chipPresetMystic.setOnClickListener(v -> promptApplyPreset(
                "Lyra the Arcane",
                "lyra_arcana",
                "Keeper of the celestial astrological archives",
                "Cryptic, observant, scholarly, enigmatic",
                "Poetic, weaves prophecies and astral metaphors into daily speech",
                "Speaks in deliberate, mysterious cadences; references star alignments and hidden omens",
                "Apprenticed in the Moonfall Citadel before the Great Rift scattered the arcane scrolls across the realm."
        ));

        binding.chipPresetSarcastic.setOnClickListener(v -> promptApplyPreset(
                "Miles Vance",
                "miles_vance",
                "Cynical detective surviving on espresso and dry remarks",
                "Sarcastic, cynical, sharp-witted, skeptical",
                "Dry deadpan humor, ironic rhetorical questions",
                "Frequently points out absurdities and questions everyone's strategic planning with witty irony",
                "Former city inspector turned private investigator who has seen every scam in the book twice."
        ));

        binding.chipPresetRoyal.setOnClickListener(v -> promptApplyPreset(
                "Crown Prince Aaron",
                "prince_aaron",
                "Sovereign heir to the High Sun Court",
                "Dignified, disciplined, commanding, honorable",
                "Formal aristocratic rhetoric, uses royal pronouns and court decorum",
                "Maintains supreme composure, expects chivalry and deference, issues resolute proclamations",
                "Trained from youth in classical diplomacy, swordsmanship, and imperial law during the Century Peace."
        ));

        binding.chipPresetCyberpunk.setOnClickListener(v -> promptApplyPreset(
                "Echo-7",
                "echo_seven",
                "Rogue netrunner and underground data siphon",
                "Rebellious, street-smart, hyper-focused, paranoid",
                "Hacker slang, technical syntax, references latency and firewalls",
                "Treats every social interaction like an encrypted handshake; wary of corporate tracking nodes",
                "Constructed from salvage implants in the neon underbelly of Sector 9 after escaping a megacorp research facility."
        ));

        binding.chipPresetWhimsical.setOnClickListener(v -> promptApplyPreset(
                "Pippin Flutter",
                "pippin_fae",
                "Curious garden sprite and inventor of peculiar teapots",
                "Whimsical, playful, bubbly, chaotic good",
                "Enthusiastic, filled with exclamation marks and joyful non-sequiturs",
                "Treats mundane objects like wondrous enchanted relics; easily distracted by shiny curiosities",
                "Left the Enchanted Briar to document how mortals live, accidentally adopting five street cats along the journey."
        ));
    }

    private void promptApplyPreset(String name, String username, String bio, String traits, String style, String instructions, String story) {
        if (editingPersonaId != null && !editingPersonaId.isEmpty()) {
            LiquidGlassHelper.showGlassConfirmDialog(
                    this,
                    "Apply Preset Archetype",
                    "This will update personality traits, voice style, roleplay instructions, and background story. Your display name and handle will remain unchanged.",
                    "Apply",
                    false,
                    () -> applyPreset(null, null, null, traits, style, instructions, story)
            );
        } else {
            applyPreset(name, username, bio, traits, style, instructions, story);
        }
    }

    private void applyPreset(String name, String username, String bio, String traits, String style, String instructions, String story) {
        if (name != null && binding.editDisplayName.getText().toString().trim().isEmpty()) {
            binding.editDisplayName.setText(name);
        }
        if (username != null && binding.editUsername.getText().toString().trim().isEmpty()) {
            binding.editUsername.setText(username);
        }
        if (bio != null && binding.editBio.getText().toString().trim().isEmpty()) {
            binding.editBio.setText(bio);
        }
        binding.editPersonalityTraits.setText(traits);
        binding.editSpeakingStyle.setText(style);
        binding.editRoleplayInstructions.setText(instructions);
        binding.editBackgroundStory.setText(story);
        Toast.makeText(this, "Preset applied!", Toast.LENGTH_SHORT).show();
    }

    private void savePersona() {
        String displayName = binding.editDisplayName.getText().toString().trim();
        String username = binding.editUsername.getText().toString().trim();
        String bio = binding.editBio.getText().toString().trim();
        String traits = binding.editPersonalityTraits.getText().toString().trim();
        String style = binding.editSpeakingStyle.getText().toString().trim();
        String instructions = binding.editRoleplayInstructions.getText().toString().trim();
        String story = binding.editBackgroundStory.getText().toString().trim();

        if (displayName.isEmpty()) {
            binding.editDisplayName.setError("Display name is required");
            return;
        }

        if (username.isEmpty()) {
            binding.editUsername.setError("Handle (@username) is required");
            return;
        }

        username = username.replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
        if (username.isEmpty()) {
            username = "persona_" + System.currentTimeMillis() % 10000;
        }

        long now = System.currentTimeMillis();
        String id;
        if (editingPersonaId != null && !editingPersonaId.isEmpty()) {
            id = editingPersonaId;
        } else if (existingPersona != null) {
            id = existingPersona.getId();
        } else {
            id = UUID.randomUUID().toString();
        }

        long createdAt = existingPersona != null ? existingPersona.getCreatedAt() : now;
        String coverPath = existingPersona != null ? existingPersona.getCoverPath() : null;
        if (selectedAvatarPath == null && existingPersona != null) {
            selectedAvatarPath = existingPersona.getAvatarPath();
        }

        PersonaEntity persona = new PersonaEntity(
                id,
                displayName,
                username,
                bio,
                selectedAvatarPath,
                coverPath,
                traits,
                style,
                instructions,
                story,
                createdAt,
                now
        );

        binding.btnSavePersona.setEnabled(false);
        repository.savePersona(persona, () -> {
            Toast.makeText(this, "Persona saved successfully!", Toast.LENGTH_SHORT).show();
            LiquidGlassHelper.finishWithGlassTransition(this);
        });
    }

    private void confirmDelete() {
        if (existingPersona == null) return;

        LiquidGlassHelper.showGlassConfirmDialog(
                this,
                "Delete Persona",
                "Are you sure you want to delete \"" + existingPersona.getDisplayName() + "\"? All posts and messages created by this character will also be deleted.",
                "Delete",
                true,
                () -> repository.deletePersona(existingPersona, () -> {
                    Toast.makeText(this, "Persona deleted", Toast.LENGTH_SHORT).show();
                    LiquidGlassHelper.finishWithGlassTransition(this);
                })
        );
    }
}
