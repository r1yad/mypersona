package com.example.ui.persona;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.data.entity.PersonaEntity;
import com.example.databinding.FragmentPersonasBinding;

public class PersonasFragment extends Fragment implements PersonasAdapter.OnPersonaActionListener {

    private FragmentPersonasBinding binding;
    private PersonaViewModel viewModel;
    private PersonasAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPersonasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(PersonaViewModel.class);

        adapter = new PersonasAdapter(this);
        binding.recyclerPersonas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPersonas.setAdapter(adapter);

        viewModel.getActivePersona().observe(getViewLifecycleOwner(), active -> {
            String activeId = active != null ? active.getId() : "";
            adapter.setActivePersonaId(activeId);
        });

        viewModel.getAllPersonas().observe(getViewLifecycleOwner(), personas -> {
            adapter.submitList(personas);
            if (personas == null || personas.isEmpty()) {
                binding.layoutEmptyPersonas.setVisibility(View.VISIBLE);
                binding.recyclerPersonas.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyPersonas.setVisibility(View.GONE);
                binding.recyclerPersonas.setVisibility(View.VISIBLE);
            }
        });

        binding.fabCreatePersona.setOnClickListener(v -> openPersonaEditor(null));
        binding.btnEmptyCreateFirstPersona.setOnClickListener(v -> openPersonaEditor(null));
    }

    private void openPersonaEditor(@Nullable PersonaEntity persona) {
        Intent intent = new Intent(requireContext(), PersonaEditActivity.class);
        if (persona != null) {
            intent.putExtra(PersonaEditActivity.EXTRA_PERSONA_ID, persona.getId());
        }
        startActivity(intent);
    }

    @Override
    public void onSetActive(PersonaEntity persona) {
        viewModel.setActivePersona(persona);
        Toast.makeText(requireContext(), "Active identity set to: " + persona.getDisplayName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEdit(PersonaEntity persona) {
        openPersonaEditor(persona);
    }

    @Override
    public void onViewProfile(PersonaEntity persona) {
        if (persona == null) return;
        Intent intent = new Intent(requireContext(), PersonaProfileActivity.class);
        intent.putExtra(PersonaProfileActivity.EXTRA_PERSONA_ID, persona.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
