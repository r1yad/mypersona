package com.example.ui.settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.R;
import com.example.data.repository.AppRepository;
import com.example.databinding.ActivityPostOrganizationBinding;

public class PostOrganizationActivity extends AppCompatActivity {

    private ActivityPostOrganizationBinding binding;
    private AppRepository repository;

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

        binding.toolbarPostOrg.setNavigationOnClickListener(v -> finish());

        String currentSort = repository.getPostSortOrder();
        if (AppRepository.SORT_POSTS_OLDEST.equals(currentSort)) {
            binding.radioPostOldest.setChecked(true);
        } else if (AppRepository.SORT_POSTS_MOST_LIKED.equals(currentSort)) {
            binding.radioPostMostLiked.setChecked(true);
        } else {
            binding.radioPostNewest.setChecked(true);
        }

        binding.btnSavePostOrg.setOnClickListener(v -> {
            int selectedId = binding.radioGroupPostSort.getCheckedRadioButtonId();
            String newOrder = AppRepository.SORT_POSTS_NEWEST;
            if (selectedId == R.id.radio_post_oldest) {
                newOrder = AppRepository.SORT_POSTS_OLDEST;
            } else if (selectedId == R.id.radio_post_most_liked) {
                newOrder = AppRepository.SORT_POSTS_MOST_LIKED;
            }

            repository.setPostSortOrder(newOrder);
            Toast.makeText(this, "Post organization updated", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
