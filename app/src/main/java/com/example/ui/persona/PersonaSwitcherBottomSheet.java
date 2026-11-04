package com.example.ui.persona;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.data.entity.PersonaEntity;
import com.example.databinding.BottomSheetPersonaSwitcherBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PersonaSwitcherBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "PersonaSwitcherBottomSheet";

    public static PersonaSwitcherBottomSheet newInstance() {
        return new PersonaSwitcherBottomSheet();
    }

    private BottomSheetPersonaSwitcherBinding binding;
    private PersonaViewModel viewModel;
    private PersonaSwitcherAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetPersonaSwitcherBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(PersonaViewModel.class);

        adapter = new PersonaSwitcherAdapter(new PersonaSwitcherAdapter.OnPersonaSelectListener() {
            @Override
            public void onSelected(PersonaEntity persona) {
                viewModel.setActivePersona(persona);
                dismiss();
            }

            @Override
            public void onProfileClicked(PersonaEntity persona) {
                dismiss();
                Intent intent = new Intent(requireContext(), PersonaProfileActivity.class);
                intent.putExtra(PersonaProfileActivity.EXTRA_PERSONA_ID, persona.getId());
                startActivity(intent);
            }
        });

        binding.recyclerSwitcherPersonas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerSwitcherPersonas.setAdapter(adapter);

        viewModel.getAllPersonas().observe(getViewLifecycleOwner(), list -> {
            String activeId = viewModel.getActivePersona().getValue() != null ?
                    viewModel.getActivePersona().getValue().getId() : "";
            adapter.setPersonas(list, activeId);
        });

        viewModel.getActivePersona().observe(getViewLifecycleOwner(), active -> {
            String activeId = active != null ? active.getId() : "";
            adapter.setPersonas(viewModel.getAllPersonas().getValue(), activeId);
        });

        binding.btnSwitcherCreateNew.setOnClickListener(v -> {
            dismiss();
            startActivity(new Intent(requireContext(), PersonaEditActivity.class));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
