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
}
