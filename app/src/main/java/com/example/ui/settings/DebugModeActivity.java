package com.example.ui.settings;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.entity.PersonaEntity;
import com.example.data.entity.PostEntity;
import com.example.data.entity.PostMediaEntity;
import com.example.data.repository.AppRepository;
import com.example.databinding.ActivityDebugModeBinding;
import com.example.databinding.ItemDebugPersonaBinding;
import com.example.databinding.ItemDebugPostBinding;
import com.example.util.MediaHelper;

import java.util.ArrayList;
import java.util.List;

public class DebugModeActivity extends AppCompatActivity {

    private ActivityDebugModeBinding binding;
    private AppRepository repository;
    private DebugPersonasAdapter personasAdapter;
    private DebugPostsAdapter postsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDebugModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = AppRepository.getInstance(this);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Setup switch
        boolean isDebugActive = repository.isDebugModeEnabled();
        binding.switchDebugMode.setChecked(isDebugActive);
        updateStatusDescription(isDebugActive);

        binding.switchDebugMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            repository.setDebugModeEnabled(isChecked);
            updateStatusDescription(isChecked);
            loadDebugContent();
            String msg = isChecked
                    ? "Switched to Isolated Debug Database (personanet_debug.db)"
                    : "Switched to Production Database (personanet.db)";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Setup RecyclerViews
        personasAdapter = new DebugPersonasAdapter(persona -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Debug Persona")
                    .setMessage("Delete test persona @" + persona.getUsername() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        repository.getExecutor().execute(() -> {
                            repository.getDebugDatabase().personaDao().delete(persona);
                            runOnUiThread(this::loadDebugContent);
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        postsAdapter = new DebugPostsAdapter(post -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Debug Post")
                    .setMessage("Delete this debug post?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        repository.getExecutor().execute(() -> {
                            repository.getDebugDatabase().postDao().deleteMediaForPost(post.getId());
                            repository.getDebugDatabase().postDao().deletePost(post);
                            runOnUiThread(this::loadDebugContent);
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.recyclerDebugPersonas.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerDebugPersonas.setAdapter(personasAdapter);

        binding.recyclerDebugPosts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerDebugPosts.setAdapter(postsAdapter);

        // Generation buttons
        binding.btnGenerateAll.setOnClickListener(v -> generateContent(true, true));
        binding.btnGeneratePersonas.setOnClickListener(v -> generateContent(true, false));
        binding.btnGeneratePosts.setOnClickListener(v -> generateContent(false, true));

        // Delete all debug data
        binding.btnClearDebugContent.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete All Debug Content")
                    .setMessage("This will wipe all test personas, test posts, test images, and test comments in the isolated debug database. Real data will NOT be touched.")
                    .setPositiveButton("Wipe Debug DB", (dialog, which) -> {
                        setLoading(true, "Clearing debug database…");
                        repository.clearAllDebugData(() -> {
                            runOnUiThread(() -> {
                                setLoading(false, "Debug database wiped clean.");
                                loadDebugContent();
                                Toast.makeText(this, "All debug data cleared", Toast.LENGTH_SHORT).show();
                            });
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        loadDebugContent();
    }

    private void updateStatusDescription(boolean isDebug) {
        if (isDebug) {
            binding.textDebugStatusDescription.setText("Active: Pointing to Isolated Debug Database (personanet_debug.db)");
            binding.textDebugStatusDescription.setTextColor(getColor(R.color.amber_warning));
        } else {
            binding.textDebugStatusDescription.setText("Inactive: Pointing to Production Database (personanet.db)");
            binding.textDebugStatusDescription.setTextColor(getColor(R.color.text_tertiary));
        }
    }

    private void setLoading(boolean loading, String message) {
        binding.progressDebugAction.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnGenerateAll.setEnabled(!loading);
        binding.btnGeneratePersonas.setEnabled(!loading);
        binding.btnGeneratePosts.setEnabled(!loading);
        binding.btnClearDebugContent.setEnabled(!loading);
        binding.textDebugLog.setText(message);
    }

    private void generateContent(boolean personas, boolean posts) {
        if (!repository.isDebugModeEnabled()) {
            binding.switchDebugMode.setChecked(true);
            repository.setDebugModeEnabled(true);
            updateStatusDescription(true);
        }

        setLoading(true, "Generating test content in isolated database…");

        repository.getExecutor().execute(() -> {
            try {
                if (personas && posts) {
                    DebugContentGenerator.generateFullSampleData(this, repository.getDebugDatabase());
                } else if (personas) {
                    DebugContentGenerator.generateSamplePersonas(this, repository.getDebugDatabase());
                } else {
                    List<PersonaEntity> existing = repository.getDebugDatabase().personaDao().getAllPersonasSync();
                    if (existing.isEmpty()) {
                        existing = DebugContentGenerator.generateSamplePersonas(this, repository.getDebugDatabase());
                    }
                    DebugContentGenerator.generateSamplePosts(this, repository.getDebugDatabase(), existing);
                }

                runOnUiThread(() -> {
                    setLoading(false, "Test content generation completed successfully!");
                    loadDebugContent();
                    Toast.makeText(this, "Sample content ready in Debug Database", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoading(false, "Error: " + e.getMessage());
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadDebugContent() {
        repository.getExecutor().execute(() -> {
            List<PersonaEntity> personas = repository.getDebugDatabase().personaDao().getAllPersonasSync();
            List<PostEntity> posts = repository.getDebugDatabase().postDao().getAllPostsSync();

            runOnUiThread(() -> {
                binding.textDebugPersonasCount.setText(personas.size() + " Personas");
                binding.textDebugPostsCount.setText(posts.size() + " Posts");
                personasAdapter.setPersonas(personas);
                postsAdapter.setPosts(posts);
            });
        });
    }

    // Adapters for Debug lists
    private class DebugPersonasAdapter extends RecyclerView.Adapter<DebugPersonasAdapter.ViewHolder> {
        interface OnDeleteListener {
            void onDelete(PersonaEntity persona);
        }

        private final OnDeleteListener listener;
        private final List<PersonaEntity> items = new ArrayList<>();

        DebugPersonasAdapter(OnDeleteListener listener) {
            this.listener = listener;
        }

        void setPersonas(List<PersonaEntity> newItems) {
            items.clear();
            if (newItems != null) items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDebugPersonaBinding b = ItemDebugPersonaBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PersonaEntity item = items.get(position);
            holder.binding.textDebugPersonaName.setText(item.getDisplayName());
            holder.binding.textDebugPersonaHandle.setText("@" + item.getUsername());

            if (item.getAvatarPath() != null && !item.getAvatarPath().isEmpty()) {
                Bitmap bm = MediaHelper.loadBitmap(DebugModeActivity.this, item.getAvatarPath(), 100);
                if (bm != null) {
                    holder.binding.imgDebugAvatar.setImageBitmap(bm);
                } else {
                    holder.binding.imgDebugAvatar.setImageResource(R.drawable.ic_person_placeholder);
                }
            } else {
                holder.binding.imgDebugAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }

            holder.binding.btnDeleteDebugPersona.setOnClickListener(v -> listener.onDelete(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemDebugPersonaBinding binding;
            ViewHolder(ItemDebugPersonaBinding b) {
                super(b.getRoot());
                this.binding = b;
            }
        }
    }

    private class DebugPostsAdapter extends RecyclerView.Adapter<DebugPostsAdapter.ViewHolder> {
        interface OnDeleteListener {
            void onDelete(PostEntity post);
        }

        private final OnDeleteListener listener;
        private final List<PostEntity> items = new ArrayList<>();

        DebugPostsAdapter(OnDeleteListener listener) {
            this.listener = listener;
        }

        void setPosts(List<PostEntity> newItems) {
            items.clear();
            if (newItems != null) items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDebugPostBinding b = ItemDebugPostBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PostEntity item = items.get(position);
            holder.binding.textDebugPostAuthor.setText("Post ID: " + item.getId());
            holder.binding.textDebugPostContent.setText(item.getTextContent());

            // Query first media thumbnail if available
            repository.getExecutor().execute(() -> {
                List<PostMediaEntity> mediaList = repository.getDebugDatabase().postDao().getMediaForPostSync(item.getId());
                runOnUiThread(() -> {
                    if (mediaList != null && !mediaList.isEmpty()) {
                        Bitmap bm = MediaHelper.loadBitmap(DebugModeActivity.this, mediaList.get(0).getMediaPath(), 100);
                        if (bm != null) {
                            holder.binding.imgDebugPostThumb.setImageBitmap(bm);
                        } else {
                            holder.binding.imgDebugPostThumb.setImageResource(R.drawable.ic_feed);
                        }
                    } else {
                        holder.binding.imgDebugPostThumb.setImageResource(R.drawable.ic_feed);
                    }
                });
            });

            holder.binding.btnDeleteDebugPost.setOnClickListener(v -> listener.onDelete(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemDebugPostBinding binding;
            ViewHolder(ItemDebugPostBinding b) {
                super(b.getRoot());
                this.binding = b;
            }
        }
    }
}
