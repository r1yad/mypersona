package com.example;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.data.entity.PersonaEntity;
import com.example.data.repository.AppRepository;
import com.example.databinding.ActivityMainBinding;
import com.example.ui.chat.MessagesFragment;
import com.example.ui.feed.FeedFragment;
import com.example.ui.persona.PersonaSwitcherBottomSheet;
import com.example.ui.persona.PersonasFragment;
import com.example.ui.settings.SettingsFragment;
import com.example.util.MediaHelper;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppRepository repository;

    private final Fragment feedFragment = new FeedFragment();
    private final Fragment messagesFragment = new MessagesFragment();
    private final Fragment personasFragment = new PersonasFragment();
    private final Fragment myProfileFragment = new com.example.ui.persona.MyProfileFragment();
    private final Fragment settingsFragment = new SettingsFragment();
    private Fragment activeFragment = feedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = AppRepository.getInstance(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });

        // Setup fragment container
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, settingsFragment, "settings").hide(settingsFragment)
                .add(R.id.fragment_container, myProfileFragment, "profile").hide(myProfileFragment)
                .add(R.id.fragment_container, personasFragment, "personas").hide(personasFragment)
                .add(R.id.fragment_container, messagesFragment, "messages").hide(messagesFragment)
                .add(R.id.fragment_container, feedFragment, "feed")
                .commit();

        // Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_feed) {
                switchFragment(feedFragment);
                return true;
            } else if (itemId == R.id.nav_messages) {
                switchFragment(messagesFragment);
                return true;
            } else if (itemId == R.id.nav_personas) {
                switchFragment(personasFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                switchFragment(myProfileFragment);
                return true;
            } else if (itemId == R.id.nav_settings) {
                switchFragment(settingsFragment);
                return true;
            }
            return false;
        });

        // Top Bar Active Persona Switcher
        binding.chipActivePersona.setOnClickListener(v -> {
            PersonaSwitcherBottomSheet bottomSheet = new PersonaSwitcherBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), PersonaSwitcherBottomSheet.TAG);
        });

        // Clicking avatar opens active persona's profile ("My Profile")
        binding.imageActivePersonaAvatar.setOnClickListener(v -> {
            PersonaEntity current = repository.getActivePersonaLive().getValue();
            if (current != null) {
                Intent intent = new Intent(this, com.example.ui.persona.PersonaProfileActivity.class);
                intent.putExtra(com.example.ui.persona.PersonaProfileActivity.EXTRA_PERSONA_ID, current.getId());
                startActivity(intent);
            } else {
                PersonaSwitcherBottomSheet bottomSheet = new PersonaSwitcherBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), PersonaSwitcherBottomSheet.TAG);
            }
        });

        // Observe active persona
        repository.getActivePersonaLive().observe(this, this::updateActivePersonaUI);
    }

    private void switchFragment(Fragment target) {
        if (activeFragment != target) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(target)
                    .commit();
            activeFragment = target;
        }
    }

    private void updateActivePersonaUI(PersonaEntity persona) {
        if (persona != null) {
            binding.textActivePersonaName.setText(persona.getDisplayName());
            if (persona.getAvatarPath() != null) {
                Bitmap bm = MediaHelper.loadBitmap(this, persona.getAvatarPath(), 100);
                if (bm != null) {
                    binding.imageActivePersonaAvatar.setImageBitmap(bm);
                    return;
                }
            }
            binding.imageActivePersonaAvatar.setImageResource(R.drawable.ic_person_placeholder);
        } else {
            binding.textActivePersonaName.setText(getString(R.string.no_active_persona));
            binding.imageActivePersonaAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }
    }
}
