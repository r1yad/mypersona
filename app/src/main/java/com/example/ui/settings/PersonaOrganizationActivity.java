package com.example.ui.settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.data.repository.AppRepository;
import com.example.databinding.ActivityPersonaOrganizationBinding;
import com.example.util.LiquidGlassHelper;

public class PersonaOrganizationActivity extends AppCompatActivity {

    private ActivityPersonaOrganizationBinding binding;
    private AppRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LiquidGlassHelper.enableEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        binding = ActivityPersonaOrganizationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = AppRepository.getInstance(this);

        LiquidGlassHelper.applyTopInsets(binding.appBarPersonaOrg);
        LiquidGlassHelper.applyBottomInsets(binding.scrollPersonaOrg);
        LiquidGlassHelper.attachPressFeedback(binding.btnSavePersonaOrg);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                LiquidGlassHelper.finishWithGlassTransition(PersonaOrganizationActivity.this);
            }
        });

        binding.toolbarPersonaOrg.setNavigationOnClickListener(v -> LiquidGlassHelper.finishWithGlassTransition(this));

        String currentSort = repository.getPersonaSortOrder();
        if (AppRepository.SORT_PERSONAS_NAME_AZ.equals(currentSort)) {
            binding.radioPersonaAz.setChecked(true);
        } else if (AppRepository.SORT_PERSONAS_NAME_ZA.equals(currentSort)) {
            binding.radioPersonaZa.setChecked(true);
        } else {
            binding.radioPersonaRecent.setChecked(true);
        }

        binding.btnSavePersonaOrg.setOnClickListener(v -> {
            int selectedId = binding.radioGroupPersonaSort.getCheckedRadioButtonId();
            String newOrder = AppRepository.SORT_PERSONAS_RECENT;
            if (selectedId == R.id.radio_persona_az) {
                newOrder = AppRepository.SORT_PERSONAS_NAME_AZ;
            } else if (selectedId == R.id.radio_persona_za) {
                newOrder = AppRepository.SORT_PERSONAS_NAME_ZA;
            }

            repository.setPersonaSortOrder(newOrder);
            Toast.makeText(this, "Persona organization updated", Toast.LENGTH_SHORT).show();
            LiquidGlassHelper.finishWithGlassTransition(this);
        });
    }
}
