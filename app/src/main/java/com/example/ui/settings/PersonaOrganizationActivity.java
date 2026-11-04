package com.example.ui.settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.R;
import com.example.data.repository.AppRepository;
import com.example.databinding.ActivityPersonaOrganizationBinding;

public class PersonaOrganizationActivity extends AppCompatActivity {

    private ActivityPersonaOrganizationBinding binding;
    private AppRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonaOrganizationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = AppRepository.getInstance(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarPersonaOrg, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });

        binding.toolbarPersonaOrg.setNavigationOnClickListener(v -> finish());

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
            finish();
        });
    }
}
