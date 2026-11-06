package com.example;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.assistant.RoleplayAssistant;
import com.example.data.entity.PersonaEntity;

public class ExampleUnitTest {

    @Test
    public void testRoleplayAssistantArchetypeDetection() {
        PersonaEntity mystic = new PersonaEntity(
                "1", "Lyra", "lyra", "Arcane scholar", null, null,
                "Cryptic, mystical, observant", "Poetic prophecies", "Speaks in riddles", "Moonfall keeper",
                0, 0
        );

        RoleplayAssistant.Archetype archetype = RoleplayAssistant.detectArchetype(mystic);
        assertEquals(RoleplayAssistant.Archetype.MYSTIC, archetype);

        String idea = RoleplayAssistant.generatePostIdea(mystic);
        assertNotNull(idea);
        assertFalse(idea.isEmpty());
    }

    @Test
    public void testRoleplayAssistantSarcastic() {
        PersonaEntity sarcastic = new PersonaEntity(
                "2", "Miles", "miles", "Detective", null, null,
                "Sarcastic, witty, cynical", "Dry humor", "Questions everything", "Ex-inspector",
                0, 0
        );

        RoleplayAssistant.Archetype archetype = RoleplayAssistant.detectArchetype(sarcastic);
        assertEquals(RoleplayAssistant.Archetype.SARCASTIC, archetype);

        String reply = RoleplayAssistant.generateReplyIdea(sarcastic, "Hello world");
        assertNotNull(reply);
        assertTrue(reply.contains("theory") || reply.contains("oracle") || reply.length() > 5);
    }

    @Test
    public void testPersonaEntityNullSafeGetters() {
        PersonaEntity persona = new PersonaEntity(
                "p-null", null, null, null, null, null, null, null, null, null, 0, 0
        );

        assertNotNull(persona.getId());
        assertNotNull(persona.getDisplayName());
        assertNotNull(persona.getUsername());
        assertNotNull(persona.getBio());
        assertNotNull(persona.getPersonalityTraits());
        assertNotNull(persona.getSpeakingStyle());
        assertNotNull(persona.getRoleplayInstructions());
        assertNotNull(persona.getBackgroundStory());
    }

    @Test
    public void testPersonaEntityFieldUpdates() {
        PersonaEntity persona = new PersonaEntity(
                "persona-123",
                "Old Name",
                "old_handle",
                "Old Bio",
                "avatar.webp",
                null,
                "Old Traits",
                "Old Style",
                "Old Instructions",
                "Old Story",
                1000L,
                1000L
        );

        // Update traits and profile fields
        persona.setPersonalityTraits("Sharp, witty, mysterious");
        persona.setSpeakingStyle("Eloquent and dry");
        persona.setBio("Updated Bio");
        persona.setUpdatedAt(2000L);

        // ID and creation time remain unchanged
        assertEquals("persona-123", persona.getId());
        assertEquals(1000L, persona.getCreatedAt());
        assertEquals("avatar.webp", persona.getAvatarPath());
        assertEquals("Sharp, witty, mysterious", persona.getPersonalityTraits());
        assertEquals("Eloquent and dry", persona.getSpeakingStyle());
        assertEquals("Updated Bio", persona.getBio());
        assertEquals(2000L, persona.getUpdatedAt());
    }
}
