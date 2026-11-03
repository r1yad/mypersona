package com.example.ui.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.R;
import com.example.assistant.RoleplayAssistant;
import com.example.data.entity.ConversationWithDetails;
import com.example.data.entity.MessageEntity;
import com.example.data.entity.PersonaEntity;
import com.example.data.repository.AppRepository;
import com.example.databinding.ActivityChatBinding;

import java.io.File;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";

    private ActivityChatBinding binding;
    private AppRepository repository;
    private String conversationId;

    private ConversationWithDetails conversationDetails;
    private PersonaEntity currentSpeaker;
    private MessagesAdapter messagesAdapter;
    private String lastMessageText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        if (conversationId == null || conversationId.isEmpty()) {
            finish();
            return;
        }

        repository = AppRepository.getInstance(this);

        messagesAdapter = new MessagesAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerMessages.setLayoutManager(layoutManager);
        binding.recyclerMessages.setAdapter(messagesAdapter);

        binding.btnChatBack.setOnClickListener(v -> finish());

        // Toggle speaker mid-chat
        binding.chipSwitchSpeaker.setOnClickListener(v -> toggleSpeaker());

        // Roleplay Assistant idea prompt
        binding.btnRoleplayChatPrompt.setOnClickListener(v -> {
            if (currentSpeaker == null) return;
            String prompt = RoleplayAssistant.generateReplyIdea(currentSpeaker, lastMessageText);
            binding.editChatMessage.setText(prompt);
            binding.editChatMessage.setSelection(prompt.length());
        });

        binding.btnSendMessage.setOnClickListener(v -> sendMessage());

        loadConversation();
    }

    private void loadConversation() {
        repository.loadConversationDetails(conversationId, details -> {
            if (details == null) {
                finish();
                return;
            }
            conversationDetails = details;
            PersonaEntity pA = details.getPersonaA();
            PersonaEntity pB = details.getPersonaB();

            String title = (pA != null ? pA.getDisplayName() : "A") + " & " + (pB != null ? pB.getDisplayName() : "B");
            binding.textChatTitle.setText(title);

            messagesAdapter.updatePersonas(pA, pB);

            // Default current speaker
            PersonaEntity active = repository.getActivePersonaLive().getValue();
            if (active != null && pB != null && active.getId().equals(pB.getId())) {
                setSpeaker(pB);
            } else if (pA != null) {
                setSpeaker(pA);
            } else if (pB != null) {
                setSpeaker(pB);
            }

            observeMessages();
        });
    }

    private void setSpeaker(PersonaEntity speaker) {
        currentSpeaker = speaker;
        if (speaker != null) {
            binding.textSpeakerName.setText(speaker.getDisplayName());
            binding.editChatMessage.setHint("Message as " + speaker.getDisplayName() + "…");

            if (speaker.getAvatarPath() != null) {
                File f = new File(speaker.getAvatarPath());
                if (f.exists()) {
                    Bitmap bm = BitmapFactory.decodeFile(f.getAbsolutePath());
                    if (bm != null) binding.imageSpeakerAvatar.setImageBitmap(bm);
                    else binding.imageSpeakerAvatar.setImageResource(R.drawable.ic_person_placeholder);
                } else {
                    binding.imageSpeakerAvatar.setImageResource(R.drawable.ic_person_placeholder);
                }
            } else {
                binding.imageSpeakerAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }

            messagesAdapter.setCurrentSpeakerId(speaker.getId());
        }
    }

    private void toggleSpeaker() {
        if (conversationDetails == null) return;
        PersonaEntity pA = conversationDetails.getPersonaA();
        PersonaEntity pB = conversationDetails.getPersonaB();

        if (pA == null || pB == null) return;

        if (currentSpeaker != null && currentSpeaker.getId().equals(pA.getId())) {
            setSpeaker(pB);
        } else {
            setSpeaker(pA);
        }

        Toast.makeText(this, "Now speaking as: " + currentSpeaker.getDisplayName(), Toast.LENGTH_SHORT).show();
    }

    private void observeMessages() {
        repository.getMessagesForConversationLive(conversationId).observe(this, list -> {
            messagesAdapter.submitList(list, () -> {
                if (list != null && !list.isEmpty()) {
                    binding.recyclerMessages.scrollToPosition(list.size() - 1);
                    lastMessageText = list.get(list.size() - 1).getTextContent();
                }
            });
        });
    }

    private void sendMessage() {
        if (currentSpeaker == null) {
            Toast.makeText(this, "No speaker persona selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = binding.editChatMessage.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }

        binding.btnSendMessage.setEnabled(false);
        repository.sendMessage(conversationId, currentSpeaker.getId(), text, null, () -> {
            binding.editChatMessage.setText("");
            binding.btnSendMessage.setEnabled(true);
        });
    }
}
