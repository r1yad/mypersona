package com.example.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ConversationWithDetails {
    private ConversationEntity conversation;
    private PersonaEntity personaA;
    private PersonaEntity personaB;

    public ConversationWithDetails(@NonNull ConversationEntity conversation,
                                   @Nullable PersonaEntity personaA,
                                   @Nullable PersonaEntity personaB) {
        this.conversation = conversation;
        this.personaA = personaA;
        this.personaB = personaB;
    }

    @NonNull
    public ConversationEntity getConversation() {
        return conversation;
    }

    public void setConversation(@NonNull ConversationEntity conversation) {
        this.conversation = conversation;
    }

    @Nullable
    public PersonaEntity getPersonaA() {
        return personaA;
    }

    public void setPersonaA(@Nullable PersonaEntity personaA) {
        this.personaA = personaA;
    }

    @Nullable
    public PersonaEntity getPersonaB() {
        return personaB;
    }

    public void setPersonaB(@Nullable PersonaEntity personaB) {
        this.personaB = personaB;
    }

    @Nullable
    public PersonaEntity getOtherPersona(@NonNull String currentPersonaId) {
        if (personaA != null && !personaA.getId().equals(currentPersonaId)) {
            return personaA;
        }
        return personaB;
    }
}
