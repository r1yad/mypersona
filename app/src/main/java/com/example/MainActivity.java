package com.example;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.data.entity.PersonaEntity;
import com.example.data.repository.AppRepository;
import com.example.databinding.ActivityMainBinding;
import com.example.ui.chat.MessagesFragment;
import com.example.ui.feed.FeedFragment;
import com.example.ui.persona.MyProfileFragment;
import com.example.ui.persona.PersonaProfileActivity;
import com.example.ui.persona.PersonaSwitcherBottomSheet;
import com.example.ui.persona.PersonasFragment;
import com.example.ui.settings.SettingsFragment;
import com.example.util.LiquidGlassHelper;
import com.example.util.MediaHelper;

public class MainActivity extends AppCompatActivity {

    private static final String STATE_ACTIVE_TAG = "active_fragment_tag";
    private static final String TAG_FEED = "feed";
    private static final String TAG_MESSAGES = "messages";
    private static final String TAG_PERSONAS = "personas";
    private static final String TAG_PROFILE = "profile";
    private static final String TAG_SETTINGS = "settings";

    private ActivityMainBinding binding;
    private AppRepository repository;

    private Fragment feedFragment;
    private Fragment messagesFragment;
    private Fragment personasFragment;
    private Fragment myProfileFragment;
    private Fragment settingsFragment;
    private Fragment activeFragment;
    private String activeTag = TAG_FEED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LiquidGlassHelper.enableEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = AppRepository.getInstance(this);

        LiquidGlassHelper.applyTopInsets(binding.topBar);
        LiquidGlassHelper.applyBottomInsets(binding.bottomNavigation);
        LiquidGlassHelper.attachPressFeedback(binding.chipActivePersona);

        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null) {
            feedFragment = new FeedFragment();
            messagesFragment = new MessagesFragment();
            personasFragment = new PersonasFragment();
            myProfileFragment = new MyProfileFragment();
            settingsFragment = new SettingsFragment();
            activeTag = TAG_FEED;
            activeFragment = feedFragment;

            fm.beginTransaction()
                    .add(R.id.fragment_container, settingsFragment, TAG_SETTINGS).hide(settingsFragment)
                    .add(R.id.fragment_container, myProfileFragment, TAG_PROFILE).hide(myProfileFragment)
                    .add(R.id.fragment_container, personasFragment, TAG_PERSONAS).hide(personasFragment)
                    .add(R.id.fragment_container, messagesFragment, TAG_MESSAGES).hide(messagesFragment)
                    .add(R.id.fragment_container, feedFragment, TAG_FEED)
                    .commit();
        } else {
            activeTag = savedInstanceState.getString(STATE_ACTIVE_TAG, TAG_FEED);
            feedFragment = findOrCreate(fm, TAG_FEED, FeedFragment.class);
            messagesFragment = findOrCreate(fm, TAG_MESSAGES, MessagesFragment.class);
            personasFragment = findOrCreate(fm, TAG_PERSONAS, PersonasFragment.class);
            myProfileFragment = findOrCreate(fm, TAG_PROFILE, MyProfileFragment.class);
            settingsFragment = findOrCreate(fm, TAG_SETTINGS, SettingsFragment.class);
            activeFragment = fragmentForTag(activeTag);
        }

        // Bottom Navigation with smooth fragment transitions & subtle icon pop
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment target = null;
            String tag = null;
            if (itemId == R.id.nav_feed) {
                target = feedFragment;
                tag = TAG_FEED;
            } else if (itemId == R.id.nav_messages) {
                target = messagesFragment;
                tag = TAG_MESSAGES;
            } else if (itemId == R.id.nav_personas) {
                target = personasFragment;
                tag = TAG_PERSONAS;
            } else if (itemId == R.id.nav_profile) {
                target = myProfileFragment;
                tag = TAG_PROFILE;
            } else if (itemId == R.id.nav_settings) {
                target = settingsFragment;
                tag = TAG_SETTINGS;
            }

            if (target != null && tag != null) {
                switchFragment(target, tag);
                View navItemView = binding.bottomNavigation.findViewById(itemId);
                LiquidGlassHelper.pulseView(navItemView);
                return true;
            }
            return false;
        });

        // Top Bar Active Persona Switcher
        binding.chipActivePersona.setOnClickListener(v -> {
            if (!LiquidGlassHelper.canNavigate()) return;
            PersonaSwitcherBottomSheet bottomSheet = new PersonaSwitcherBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), PersonaSwitcherBottomSheet.TAG);
        });

        // Clicking avatar opens active persona's profile ("My Profile")
        binding.imageActivePersonaAvatar.setOnClickListener(v -> {
            PersonaEntity current = repository.getActivePersonaLive().getValue();
            if (current != null) {
                Intent intent = new Intent(this, PersonaProfileActivity.class);
                intent.putExtra(PersonaProfileActivity.EXTRA_PERSONA_ID, current.getId());
                LiquidGlassHelper.startActivityWithGlassTransition(this, intent);
            } else {
                if (!LiquidGlassHelper.canNavigate()) return;
                PersonaSwitcherBottomSheet bottomSheet = new PersonaSwitcherBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), PersonaSwitcherBottomSheet.TAG);
            }
        });

        // Observe active persona
        repository.getActivePersonaLive().observe(this, this::updateActivePersonaUI);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_ACTIVE_TAG, activeTag);
    }

    private Fragment findOrCreate(FragmentManager fm, String tag, Class<? extends Fragment> cls) {
        Fragment existing = fm.findFragmentByTag(tag);
        if (existing != null) return existing;
        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return new FeedFragment();
        }
    }

    private Fragment fragmentForTag(String tag) {
        if (TAG_MESSAGES.equals(tag)) return messagesFragment;
        if (TAG_PERSONAS.equals(tag)) return personasFragment;
        if (TAG_PROFILE.equals(tag)) return myProfileFragment;
        if (TAG_SETTINGS.equals(tag)) return settingsFragment;
        return feedFragment;
    }

    private void switchFragment(Fragment target, String tag) {
        if (activeFragment != target) {
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            if (!LiquidGlassHelper.isReducedMotionEnabled(this)) {
                tx.setCustomAnimations(R.anim.glass_fragment_in, R.anim.glass_fragment_out);
            }
            tx.hide(activeFragment)
                    .show(target)
                    .commit();
            activeFragment = target;
            activeTag = tag;
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
