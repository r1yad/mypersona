package com.example.ui.chat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.R;
import com.example.data.entity.PersonaEntity;
import com.example.databinding.DialogNewConversationBinding;
import com.example.util.LiquidGlassHelper;

import java.util.ArrayList;
import java.util.List;

public class NewConversationDialog extends DialogFragment {

    public static final String TAG = "NewConversationDialog";

    private DialogNewConversationBinding binding;
    private ChatViewModel viewModel;
    private final List<PersonaEntity> personaList = new ArrayList<>();

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setWindowAnimations(R.style.GlassDialogAnimation);
            LiquidGlassHelper.enableDialogWindowBlur(window);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogNewConversationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        LiquidGlassHelper.attachPressFeedback(
                binding.btnCancelConversation,
                binding.btnStartConversation
        );

        viewModel.getAllPersonas().observe(getViewLifecycleOwner(), list -> {
            personaList.clear();
            if (list != null) {
                personaList.addAll(list);
            }

            List<String> names = new ArrayList<>();
            for (PersonaEntity p : personaList) {
                names.add(p.getDisplayName() + " (@" + p.getUsername() + ")");
            }

            ArrayAdapter<String> adapterA = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names);
            ArrayAdapter<String> adapterB = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names);

            binding.spinnerPersonaA.setAdapter(adapterA);
            binding.spinnerPersonaB.setAdapter(adapterB);

            if (names.size() > 1) {
                binding.spinnerPersonaB.setSelection(1);
            }
        });

        binding.btnCancelConversation.setOnClickListener(v -> dismiss());

        binding.btnStartConversation.setOnClickListener(v -> {
            if (personaList.size() < 2) {
                Toast.makeText(requireContext(), "You need at least 2 personas to start a conversation", Toast.LENGTH_SHORT).show();
                return;
            }

            int posA = binding.spinnerPersonaA.getSelectedItemPosition();
            int posB = binding.spinnerPersonaB.getSelectedItemPosition();

            if (posA == posB) {
                Toast.makeText(requireContext(), "Please select two different personas", Toast.LENGTH_SHORT).show();
                return;
            }

            PersonaEntity pA = personaList.get(posA);
            PersonaEntity pB = personaList.get(posB);

            viewModel.startOrGetConversation(pA.getId(), pB.getId(), conv -> {
                FragmentActivity activity = getActivity();
                if (!isAdded()) return;
                dismiss();
                if (activity != null) {
                    Intent intent = new Intent(activity, ChatActivity.class);
                    intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conv.getId());
                    LiquidGlassHelper.startActivityWithGlassTransition(activity, intent);
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
