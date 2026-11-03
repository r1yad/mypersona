package com.example.ui.persona;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.entity.PersonaEntity;
import com.example.databinding.ItemPersonaCardBinding;

import java.io.File;

public class PersonasAdapter extends ListAdapter<PersonaEntity, PersonasAdapter.PersonaViewHolder> {

    public interface OnPersonaActionListener {
        void onSetActive(PersonaEntity persona);
        void onEdit(PersonaEntity persona);
        void onViewProfile(PersonaEntity persona);
    }

    private final OnPersonaActionListener listener;
    private String activePersonaId = "";

    public PersonasAdapter(OnPersonaActionListener listener) {
        super(new DiffUtil.ItemCallback<PersonaEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull PersonaEntity oldItem, @NonNull PersonaEntity newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull PersonaEntity oldItem, @NonNull PersonaEntity newItem) {
                return oldItem.getUpdatedAt() == newItem.getUpdatedAt() &&
                        oldItem.getDisplayName().equals(newItem.getDisplayName()) &&
                        oldItem.getUsername().equals(newItem.getUsername()) &&
                        oldItem.getBio().equals(newItem.getBio());
            }
        });
        this.listener = listener;
    }

    public void setActivePersonaId(String activePersonaId) {
        this.activePersonaId = activePersonaId != null ? activePersonaId : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PersonaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPersonaCardBinding binding = ItemPersonaCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new PersonaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonaViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class PersonaViewHolder extends RecyclerView.ViewHolder {
        private final ItemPersonaCardBinding binding;

        public PersonaViewHolder(ItemPersonaCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PersonaEntity persona) {
            binding.textPersonaName.setText(persona.getDisplayName());
            binding.textPersonaUsername.setText("@" + persona.getUsername());

            if (persona.getBio().isEmpty()) {
                binding.textPersonaBio.setVisibility(View.GONE);
            } else {
                binding.textPersonaBio.setText(persona.getBio());
                binding.textPersonaBio.setVisibility(View.VISIBLE);
            }

            if (!persona.getPersonalityTraits().isEmpty()) {
                binding.badgeTraits.setText("Traits: " + persona.getPersonalityTraits());
                binding.badgeTraits.setVisibility(View.VISIBLE);
            } else {
                binding.badgeTraits.setVisibility(View.GONE);
            }

            if (!persona.getSpeakingStyle().isEmpty()) {
                binding.badgeStyle.setText("Style: " + persona.getSpeakingStyle());
                binding.badgeStyle.setVisibility(View.VISIBLE);
            } else {
                binding.badgeStyle.setVisibility(View.GONE);
            }

            if (persona.getAvatarPath() != null) {
                Bitmap bm = com.example.util.MediaHelper.loadBitmap(binding.getRoot().getContext(), persona.getAvatarPath(), 160);
                if (bm != null) {
                    binding.imagePersonaAvatar.setImageBitmap(bm);
                } else {
                    binding.imagePersonaAvatar.setImageResource(R.drawable.ic_person_placeholder);
                }
            } else {
                binding.imagePersonaAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }

            boolean isActive = persona.getId().equals(activePersonaId);
            if (isActive) {
                binding.textActiveIndicator.setVisibility(View.VISIBLE);
                binding.btnSetActive.setVisibility(View.GONE);
            } else {
                binding.textActiveIndicator.setVisibility(View.GONE);
                binding.btnSetActive.setVisibility(View.VISIBLE);
            }

            binding.btnSetActive.setOnClickListener(v -> listener.onSetActive(persona));
            binding.btnPersonaEdit.setOnClickListener(v -> listener.onEdit(persona));
            binding.cardPersona.setOnClickListener(v -> listener.onViewProfile(persona));
            binding.imagePersonaAvatar.setOnClickListener(v -> listener.onViewProfile(persona));
            binding.textPersonaName.setOnClickListener(v -> listener.onViewProfile(persona));
        }
    }
}
