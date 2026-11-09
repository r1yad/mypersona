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

    @Test
    public void testCustomPostOrderingLogic() {
        // Test custom post sorting priority
        java.util.List<String> customOrder = java.util.Arrays.asList("post_c", "post_a", "post_b");

        java.util.List<com.example.data.entity.PostEntity> posts = new java.util.ArrayList<>();
        posts.add(new com.example.data.entity.PostEntity("post_a", "p1", "Post A", 1000L, 1000L));
        posts.add(new com.example.data.entity.PostEntity("post_b", "p1", "Post B", 2000L, 2000L));
        posts.add(new com.example.data.entity.PostEntity("post_c", "p1", "Post C", 3000L, 3000L));
        posts.add(new com.example.data.entity.PostEntity("post_new", "p1", "New Post", 4000L, 4000L)); // Newly created post

        posts.sort((p1, p2) -> {
            int idx1 = customOrder.indexOf(p1.getId());
            int idx2 = customOrder.indexOf(p2.getId());

            // Brand new posts not yet in custom order go to the top
            if (idx1 == -1 && idx2 == -1) {
                return Long.compare(p2.getCreatedAt(), p1.getCreatedAt());
            }
            if (idx1 == -1) return -1;
            if (idx2 == -1) return 1;
            return Integer.compare(idx1, idx2);
        });

        // 1st: post_new (newly created at top)
        assertEquals("post_new", posts.get(0).getId());
        // 2nd: post_c (first in custom order)
        assertEquals("post_c", posts.get(1).getId());
        // 3rd: post_a (second in custom order)
        assertEquals("post_a", posts.get(2).getId());
        // 4th: post_b (third in custom order)
        assertEquals("post_b", posts.get(3).getId());
    }
}
