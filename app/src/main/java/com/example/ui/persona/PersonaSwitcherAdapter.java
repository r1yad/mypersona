package com.example.ui.persona;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.entity.PersonaEntity;
import com.example.databinding.ItemSwitcherPersonaBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PersonaSwitcherAdapter extends RecyclerView.Adapter<PersonaSwitcherAdapter.SwitcherViewHolder> {

    public interface OnPersonaSelectListener {
        void onSelected(PersonaEntity persona);
        void onProfileClicked(PersonaEntity persona);
    }

    private final OnPersonaSelectListener listener;
    private final List<PersonaEntity> personas = new ArrayList<>();
    private String activePersonaId = "";

    public PersonaSwitcherAdapter(OnPersonaSelectListener listener) {
        this.listener = listener;
    }

    public void setPersonas(List<PersonaEntity> list, String activeId) {
        this.personas.clear();
        if (list != null) {
            this.personas.addAll(list);
        }
        this.activePersonaId = activeId != null ? activeId : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SwitcherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSwitcherPersonaBinding binding = ItemSwitcherPersonaBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SwitcherViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SwitcherViewHolder holder, int position) {
        holder.bind(personas.get(position));
    }

    @Override
    public int getItemCount() {
        return personas.size();
    }

    class SwitcherViewHolder extends RecyclerView.ViewHolder {
        private final ItemSwitcherPersonaBinding binding;

        public SwitcherViewHolder(ItemSwitcherPersonaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PersonaEntity persona) {
            binding.textSwitcherName.setText(persona.getDisplayName());
            binding.textSwitcherUsername.setText("@" + persona.getUsername());

            if (persona.getAvatarPath() != null) {
                Bitmap bm = com.example.util.MediaHelper.loadBitmap(binding.getRoot().getContext(), persona.getAvatarPath(), 120);
                if (bm != null) {
                    binding.imageSwitcherAvatar.setImageBitmap(bm);
                } else {
                    binding.imageSwitcherAvatar.setImageResource(R.drawable.ic_person_placeholder);
                }
            } else {
                binding.imageSwitcherAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }

            boolean isActive = persona.getId().equals(activePersonaId);
            binding.textSwitcherActiveBadge.setVisibility(isActive ? View.VISIBLE : View.GONE);

            binding.layoutSwitcherItem.setOnClickListener(v -> listener.onSelected(persona));
            binding.btnSwitcherProfile.setOnClickListener(v -> listener.onProfileClicked(persona));
        }
    }
}
