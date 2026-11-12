package com.example.ui.persona;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.R;
import com.example.data.entity.PersonaEntity;
import com.example.databinding.BottomSheetPersonaSwitcherBinding;
import com.example.util.LiquidGlassHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PersonaSwitcherBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "PersonaSwitcherBottomSheet";

    public static PersonaSwitcherBottomSheet newInstance() {
        return new PersonaSwitcherBottomSheet();
    }

    private BottomSheetPersonaSwitcherBinding binding;
    private PersonaViewModel viewModel;
    private PersonaSwitcherAdapter adapter;

    @Override
    public int getTheme() {
        return R.style.Theme_PersonaNet_BottomSheetDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            LiquidGlassHelper.enableDialogWindowBlur(dialog.getWindow());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetPersonaSwitcherBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LiquidGlassHelper.applyBottomInsets(binding.layoutBottomSheetSwitcher);
        LiquidGlassHelper.attachPressFeedback(binding.btnSwitcherCreateNew);

        viewModel = new ViewModelProvider(requireActivity()).get(PersonaViewModel.class);

        adapter = new PersonaSwitcherAdapter(new PersonaSwitcherAdapter.OnPersonaSelectListener() {
            @Override
            public void onSelected(PersonaEntity persona) {
                viewModel.setActivePersona(persona);
                dismiss();
            }

            @Override
            public void onProfileClicked(PersonaEntity persona) {
                FragmentActivity activity = getActivity();
                dismiss();
                if (activity != null) {
                    Intent intent = new Intent(activity, PersonaProfileActivity.class);
                    intent.putExtra(PersonaProfileActivity.EXTRA_PERSONA_ID, persona.getId());
                    LiquidGlassHelper.startActivityWithGlassTransition(activity, intent);
                }
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
            FragmentActivity activity = getActivity();
            dismiss();
            if (activity != null) {
                LiquidGlassHelper.startActivityWithGlassTransition(
                        activity, new Intent(activity, PersonaEditActivity.class));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
