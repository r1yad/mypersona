package com.example.ui.feed;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.R;
import com.example.databinding.ActivityFullscreenImageViewerBinding;
import com.example.util.MediaHelper;

import java.util.ArrayList;
import java.util.List;

public class FullscreenImageViewerActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_PATHS = "extra_image_paths";
    public static final String EXTRA_START_POSITION = "extra_start_position";

    public static void start(android.content.Context context, List<String> paths, int startPos) {
        android.content.Intent intent = new android.content.Intent(context, FullscreenImageViewerActivity.class);
        intent.putStringArrayListExtra(EXTRA_IMAGE_PATHS, new ArrayList<>(paths));
        intent.putExtra(EXTRA_START_POSITION, startPos);
        context.startActivity(intent);
    }

    private ActivityFullscreenImageViewerBinding binding;
    private final List<String> imagePaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullscreenImageViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.topBarViewer, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });

        ArrayList<String> paths = getIntent().getStringArrayListExtra(EXTRA_IMAGE_PATHS);
        if (paths != null) {
            imagePaths.addAll(paths);
        }
        int startPosition = getIntent().getIntExtra(EXTRA_START_POSITION, 0);

        binding.btnCloseViewer.setOnClickListener(v -> finish());

        FullscreenAdapter adapter = new FullscreenAdapter(imagePaths);
        binding.viewPagerFullscreen.setAdapter(adapter);

        if (imagePaths.size() > 1) {
            binding.textViewerCounter.setVisibility(View.VISIBLE);
            updateCounter(startPosition);
        } else {
            binding.textViewerCounter.setVisibility(View.GONE);
        }

        binding.viewPagerFullscreen.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateCounter(position);
            }
        });

        if (startPosition >= 0 && startPosition < imagePaths.size()) {
            binding.viewPagerFullscreen.setCurrentItem(startPosition, false);
        }
    }

    private void updateCounter(int position) {
        if (imagePaths.size() > 1) {
            binding.textViewerCounter.setText((position + 1) + " / " + imagePaths.size());
        }
    }

    static class FullscreenAdapter extends RecyclerView.Adapter<FullscreenAdapter.ViewHolder> {
        private final List<String> paths;

        public FullscreenAdapter(List<String> paths) {
            this.paths = paths;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fullscreen_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String path = paths.get(position);
            Bitmap bm = MediaHelper.loadBitmap(holder.itemView.getContext(), path, 2048);
            if (bm != null) {
                holder.imageView.setImageBitmap(bm);
            } else {
                holder.imageView.setImageResource(R.drawable.ic_image);
            }
        }

        @Override
        public int getItemCount() {
            return paths.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView imageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.imageView = itemView.findViewById(R.id.image_fullscreen);
            }
        }
    }
}
