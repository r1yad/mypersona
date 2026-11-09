package com.example.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.entity.PostWithDetails;
import com.example.data.repository.AppRepository;
import com.example.databinding.ActivityPostOrganizationBinding;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostOrganizationActivity extends AppCompatActivity implements
        PostOrganizerAdapter.OnStartDragListener,
        PostOrganizerAdapter.OnOrderChangedListener {

    private ActivityPostOrganizationBinding binding;
    private AppRepository repository;
    private PostOrganizerAdapter adapter;
    private ItemTouchHelper itemTouchHelper;
    private boolean hasUnsavedChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostOrganizationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = AppRepository.getInstance(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarPostOrg, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutBottomBar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });

        binding.toolbarPostOrg.setNavigationOnClickListener(v -> finish());

        adapter = new PostOrganizerAdapter(this, this);
        binding.recyclerPostOrganizer.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerPostOrganizer.setAdapter(adapter);

        setupItemTouchHelper();

        binding.btnSavePostOrg.setOnClickListener(v -> saveCustomOrder());

        binding.btnResetChronological.setOnClickListener(v -> {
            List<PostWithDetails> currentList = new ArrayList<>(adapter.getItems());
            currentList.sort((a, b) -> Long.compare(b.getPost().getCreatedAt(), a.getPost().getCreatedAt()));
            adapter.submitList(currentList);
            onOrderChanged();
            Toast.makeText(this, "Arranged by Newest First. Tap Save to apply.", Toast.LENGTH_SHORT).show();
        });

        loadPosts();
    }

    private void setupItemTouchHelper() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getBindingAdapterPosition();
                int toPos = target.getBindingAdapterPosition();
                adapter.swap(fromPos, toPos);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // No swipe to dismiss in organizer
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
                    // Elevated dragging animation & highlight
                    viewHolder.itemView.animate()
                            .scaleX(1.02f)
                            .scaleY(1.02f)
                            .setDuration(150)
                            .start();
                    ViewCompat.setElevation(viewHolder.itemView, 16f);

                    View card = viewHolder.itemView.findViewById(R.id.card_organizer_block);
                    if (card instanceof MaterialCardView) {
                        ((MaterialCardView) card).setStrokeColor(ContextCompat.getColor(PostOrganizationActivity.this, R.color.indigo_primary));
                        ((MaterialCardView) card).setStrokeWidth(4);
                    }
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .start();
                ViewCompat.setElevation(viewHolder.itemView, 2f);

                View card = viewHolder.itemView.findViewById(R.id.card_organizer_block);
                if (card instanceof MaterialCardView) {
                    ((MaterialCardView) card).setStrokeColor(ContextCompat.getColor(PostOrganizationActivity.this, R.color.border_divider));
                    ((MaterialCardView) card).setStrokeWidth(2);
                }
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        };

        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerPostOrganizer);
    }

    private void loadPosts() {
        repository.getFeedPostsLive().observe(this, posts -> {
            if (posts != null && !posts.isEmpty()) {
                binding.layoutEmptyOrganizer.setVisibility(View.GONE);
                binding.recyclerPostOrganizer.setVisibility(View.VISIBLE);
                binding.textPostCountBadge.setText(posts.size() + (posts.size() == 1 ? " post" : " posts"));
                if (!hasUnsavedChanges) {
                    adapter.submitList(new ArrayList<>(posts));
                }
            } else {
                binding.layoutEmptyOrganizer.setVisibility(View.VISIBLE);
                binding.recyclerPostOrganizer.setVisibility(View.GONE);
                binding.textPostCountBadge.setText("0 posts");
                adapter.submitList(new ArrayList<>());
            }
        });
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        if (itemTouchHelper != null) {
            itemTouchHelper.startDrag(viewHolder);
        }
    }

    @Override
    public void onOrderChanged() {
        hasUnsavedChanges = true;
        binding.btnSavePostOrg.setEnabled(true);
        binding.btnSavePostOrg.setAlpha(1.0f);
    }

    private void saveCustomOrder() {
        List<String> orderedIds = adapter.getPostIds();
        repository.saveCustomPostOrder(orderedIds);
        hasUnsavedChanges = false;
        binding.btnSavePostOrg.setEnabled(false);
        Toast.makeText(this, "Feed arrangement saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
