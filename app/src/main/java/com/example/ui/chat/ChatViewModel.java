package com.example.ui.chat;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.data.entity.ConversationEntity;
import com.example.data.entity.ConversationWithDetails;
import com.example.data.entity.MessageEntity;
import com.example.data.entity.PersonaEntity;
import com.example.data.repository.AppRepository;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final AppRepository repository;
    private final LiveData<List<ConversationWithDetails>> conversations;
    private final LiveData<PersonaEntity> activePersona;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.repository = AppRepository.getInstance(application);
        this.conversations = repository.getConversationsLive();
        this.activePersona = repository.getActivePersonaLive();
    }

    public LiveData<List<ConversationWithDetails>> getConversations() {
        return conversations;
    }

    public LiveData<PersonaEntity> getActivePersona() {
        return activePersona;
    }

    public LiveData<List<PersonaEntity>> getAllPersonas() {
        return repository.getAllPersonasLive();
    }

    public void startOrGetConversation(String personaAId, String personaBId, AppRepository.Callback<ConversationEntity> callback) {
        repository.startOrGetConversation(personaAId, personaBId, callback);
    }

    public void loadConversationDetails(String convId, AppRepository.Callback<ConversationWithDetails> callback) {
        repository.loadConversationDetails(convId, callback);
    }

    public LiveData<List<MessageEntity>> getMessagesForConversation(String conversationId) {
        return repository.getMessagesForConversationLive(conversationId);
    }

    public void sendMessage(String conversationId, String senderPersonaId, String text, @Nullable String mediaPath, Runnable onComplete) {
        repository.sendMessage(conversationId, senderPersonaId, text, mediaPath, onComplete);
    }

    public void refresh() {
        repository.refreshConversations();
    }
}
