package com.example.ui.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.entity.ConversationWithDetails;
import com.example.data.entity.PersonaEntity;
import com.example.databinding.ItemConversationBinding;

import java.io.File;

public class ConversationsAdapter extends ListAdapter<ConversationWithDetails, ConversationsAdapter.ConvViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClicked(ConversationWithDetails conversation);
    }

    private final OnConversationClickListener listener;

    public ConversationsAdapter(OnConversationClickListener listener) {
        super(new DiffUtil.ItemCallback<ConversationWithDetails>() {
            @Override
            public boolean areItemsTheSame(@NonNull ConversationWithDetails oldItem, @NonNull ConversationWithDetails newItem) {
                return oldItem.getConversation().getId().equals(newItem.getConversation().getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull ConversationWithDetails oldItem, @NonNull ConversationWithDetails newItem) {
                return oldItem.getConversation().getLastActivityAt() == newItem.getConversation().getLastActivityAt() &&
                        (oldItem.getConversation().getLastMessagePreview() != null &&
                                oldItem.getConversation().getLastMessagePreview().equals(newItem.getConversation().getLastMessagePreview()));
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConversationBinding binding = ItemConversationBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ConvViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConvViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ConvViewHolder extends RecyclerView.ViewHolder {
        private final ItemConversationBinding binding;

        public ConvViewHolder(ItemConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ConversationWithDetails item) {
            PersonaEntity pA = item.getPersonaA();
            PersonaEntity pB = item.getPersonaB();

            String nameA = pA != null ? pA.getDisplayName() : "Persona A";
            String nameB = pB != null ? pB.getDisplayName() : "Persona B";
            binding.textConvTitle.setText(nameA + " & " + nameB);

            if (pA != null && pA.getAvatarPath() != null) {
                File f = new File(pA.getAvatarPath());
                if (f.exists()) {
                    Bitmap bm = BitmapFactory.decodeFile(f.getAbsolutePath());
                    if (bm != null) binding.imagePersonaA.setImageBitmap(bm);
                }
            } else {
                binding.imagePersonaA.setImageResource(R.drawable.ic_person_placeholder);
            }

            if (pB != null && pB.getAvatarPath() != null) {
                File f = new File(pB.getAvatarPath());
                if (f.exists()) {
                    Bitmap bm = BitmapFactory.decodeFile(f.getAbsolutePath());
                    if (bm != null) binding.imagePersonaB.setImageBitmap(bm);
                }
            } else {
                binding.imagePersonaB.setImageResource(R.drawable.ic_person_placeholder);
            }

            String preview = item.getConversation().getLastMessagePreview();
            if (preview != null && !preview.isEmpty()) {
                binding.textConvLastMessage.setText(preview);
            } else {
                binding.textConvLastMessage.setText("Tap to begin conversation…");
            }

            CharSequence time = DateUtils.getRelativeTimeSpanString(
                    item.getConversation().getLastActivityAt(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            binding.textConvTime.setText(time);

            binding.cardConversation.setOnClickListener(v -> listener.onConversationClicked(item));
        }
    }
}
