package com.example.ui.chat;

import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.entity.MessageEntity;
import com.example.data.entity.PersonaEntity;
import com.example.databinding.ItemMessageIncomingBinding;
import com.example.databinding.ItemMessageOutgoingBinding;
import com.example.util.LiquidGlassHelper;
import com.example.util.MediaHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessagesAdapter extends ListAdapter<MessageEntity, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_OUTGOING = 1;
    private static final int VIEW_TYPE_INCOMING = 2;

    private String currentSpeakerId = "";
    private final Map<String, PersonaEntity> personaMap = new HashMap<>();
    private final Set<String> seenMessageIds = new HashSet<>();
    private boolean initialLoadComplete = false;

    public MessagesAdapter() {
        super(new DiffUtil.ItemCallback<MessageEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull MessageEntity oldItem, @NonNull MessageEntity newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull MessageEntity oldItem, @NonNull MessageEntity newItem) {
                return oldItem.getCreatedAt() == newItem.getCreatedAt() &&
                        oldItem.getTextContent().equals(newItem.getTextContent());
            }
        });
    }

    public void submitMessages(@Nullable List<MessageEntity> list, @Nullable Runnable commitCallback) {
        if (!initialLoadComplete && list != null) {
            for (MessageEntity m : list) {
                seenMessageIds.add(m.getId());
            }
            initialLoadComplete = true;
        }
        submitList(list, commitCallback);
    }

    public void setCurrentSpeakerId(String speakerId) {
        this.currentSpeakerId = speakerId != null ? speakerId : "";
        notifyDataSetChanged();
    }

    public void updatePersonas(PersonaEntity personaA, PersonaEntity personaB) {
        if (personaA != null) personaMap.put(personaA.getId(), personaA);
        if (personaB != null) personaMap.put(personaB.getId(), personaB);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        MessageEntity message = getItem(position);
        if (message.getSenderPersonaId().equals(currentSpeakerId)) {
            return VIEW_TYPE_OUTGOING;
        }
        return VIEW_TYPE_INCOMING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_OUTGOING) {
            ItemMessageOutgoingBinding binding = ItemMessageOutgoingBinding.inflate(inflater, parent, false);
            return new OutgoingViewHolder(binding);
        } else {
            ItemMessageIncomingBinding binding = ItemMessageIncomingBinding.inflate(inflater, parent, false);
            return new IncomingViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageEntity message = getItem(position);
        if (holder instanceof OutgoingViewHolder) {
            ((OutgoingViewHolder) holder).bind(message);
        } else if (holder instanceof IncomingViewHolder) {
            ((IncomingViewHolder) holder).bind(message);
        }
        animateNewMessageIfNeeded(holder.itemView, message.getId());
    }

    private void animateNewMessageIfNeeded(View itemView, String messageId) {
        if (seenMessageIds.contains(messageId) || LiquidGlassHelper.isReducedMotionEnabled(itemView.getContext())) {
            return;
        }
        seenMessageIds.add(messageId);
        itemView.animate().cancel();
        itemView.setAlpha(0f);
        itemView.setTranslationY(24f);
        itemView.setScaleX(0.96f);
        itemView.setScaleY(0.96f);
        itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(210L)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();
    }

    class OutgoingViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageOutgoingBinding binding;

        public OutgoingViewHolder(ItemMessageOutgoingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MessageEntity message) {
            PersonaEntity sender = personaMap.get(message.getSenderPersonaId());
            if (sender != null) {
                binding.textMessageSender.setText(sender.getDisplayName());
            } else {
                binding.textMessageSender.setText("You");
            }

            binding.textMessageBody.setText(message.getTextContent());
            CharSequence time = DateUtils.formatDateTime(
                    binding.getRoot().getContext(),
                    message.getCreatedAt(),
                    DateUtils.FORMAT_SHOW_TIME
            );
            binding.textMessageTime.setText(time);
        }
    }

    class IncomingViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageIncomingBinding binding;

        public IncomingViewHolder(ItemMessageIncomingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MessageEntity message) {
            PersonaEntity sender = personaMap.get(message.getSenderPersonaId());
            if (sender != null) {
                binding.textMessageSender.setText(sender.getDisplayName());
                if (sender.getAvatarPath() != null) {
                    Bitmap bm = MediaHelper.loadBitmap(binding.getRoot().getContext(), sender.getAvatarPath(), 100);
                    if (bm != null) {
                        binding.imageMessageAvatar.setImageBitmap(bm);
                    } else {
                        binding.imageMessageAvatar.setImageResource(R.drawable.ic_person_placeholder);
                    }
                } else {
                    binding.imageMessageAvatar.setImageResource(R.drawable.ic_person_placeholder);
                }
            } else {
                binding.textMessageSender.setText("Participant");
                binding.imageMessageAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }

            binding.textMessageBody.setText(message.getTextContent());
            CharSequence time = DateUtils.formatDateTime(
                    binding.getRoot().getContext(),
                    message.getCreatedAt(),
                    DateUtils.FORMAT_SHOW_TIME
            );
            binding.textMessageTime.setText(time);
        }
    }
}
