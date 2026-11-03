package com.example.assistant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.data.entity.PersonaEntity;

import java.util.Random;

public class RoleplayAssistant {

    private static final Random RANDOM = new Random();

    public enum Archetype {
        MYSTIC,
        SARCASTIC,
        ROYAL,
        CYBERPUNK,
        WHIMSICAL,
        PHILOSOPHICAL,
        GENERIC
    }

    public static Archetype detectArchetype(@Nullable PersonaEntity persona) {
        if (persona == null) return Archetype.GENERIC;

        String combined = (persona.getPersonalityTraits() + " " +
                persona.getSpeakingStyle() + " " +
                persona.getRoleplayInstructions() + " " +
                persona.getBio()).toLowerCase();

        if (combined.contains("mystic") || combined.contains("mage") || combined.contains("spell") || combined.contains("cryptic") || combined.contains("arcane")) {
            return Archetype.MYSTIC;
        } else if (combined.contains("sarcas") || combined.contains("cynic") || combined.contains("witty") || combined.contains("ironic") || combined.contains("snark")) {
            return Archetype.SARCASTIC;
        } else if (combined.contains("royal") || combined.contains("noble") || combined.contains("king") || combined.contains("queen") || combined.contains("formal") || combined.contains("majesty")) {
            return Archetype.ROYAL;
        } else if (combined.contains("cyber") || combined.contains("neon") || combined.contains("hack") || combined.contains("synth") || combined.contains("matrix") || combined.contains("glitch")) {
            return Archetype.CYBERPUNK;
        } else if (combined.contains("whims") || combined.contains("playful") || combined.contains("quirky") || combined.contains("chaotic") || combined.contains("fae")) {
            return Archetype.WHIMSICAL;
        } else if (combined.contains("philosoph") || combined.contains("think") || combined.contains("wisdom") || combined.contains("stoic")) {
            return Archetype.PHILOSOPHICAL;
        }
        return Archetype.GENERIC;
    }

    public static String generatePostIdea(@NonNull PersonaEntity persona) {
        Archetype archetype = detectArchetype(persona);
        String name = persona.getDisplayName();

        switch (archetype) {
            case MYSTIC: {
                String[] prompts = {
                        "The constellation whispers of unrest in the outer rings tonight. Keep your wards charged.",
                        "Brewing nightshade essence beneath the eclipse. What omens have visited your dreams?",
                        "Do not trust the shadows between the hourglass grains. Fate shifts when observed.",
                        "An ancient seal cracked in the lower catacombs today. I advise everyone to review their incantations."
                };
                return prompts[RANDOM.nextInt(prompts.length)];
            }
            case SARCASTIC: {
                String[] prompts = {
                        "Surviving another day where common sense is classified as a rare superpower. Congratulations to none of you.",
                        "If I had a coin for every ridiculous decision made here today, I'd buy this entire realm and fire everyone.",
                        "Currently accepting apologies in written form, alphabetized and typed in serif font.",
                        "My expectations were already on the floor, yet somehow someone brought a shovel."
                };
                return prompts[RANDOM.nextInt(prompts.length)];
            }
            case ROYAL: {
                String[] prompts = {
                        "Let it be proclaimed across the kingdom: honor is forged not in idle ceremony, but in resolute action.",
                        "From the high terrace of the citadel, we survey our realm. Prosperity shall crown our allies; justice awaits the treachery of fools.",
                        "Diplomacy remains the shield of empires, yet a sharpened blade must forever linger close.",
                        "A sovereign's burden is solitary, but our resolve remains unbroken. To the prosperity of the Crown."
                };
                return prompts[RANDOM.nextInt(prompts.length)];
            }
            case CYBERPUNK: {
                String[] prompts = {
                        "Neon drizzle flickering off carbon fiber alleyways. Grid security patched the back-channel, but nothing stays dark forever.",
                        "Memory cache overclocked again. If the corporation thinks they own this frequency, they haven't seen the payload.",
                        "Synthesizing encrypted audio logs from Sector 7. The static sounds suspiciously like a distress beacon.",
                        "Another rogue AI escaped sandbox containment. Don't ping standard relay nodes until sunrise."
                };
                return prompts[RANDOM.nextInt(prompts.length)];
            }
            case WHIMSICAL: {
                String[] prompts = {
                        "I just convinced a squirrel that acorns are tiny treasure chests. Now we have an alliance!",
                        "If you spin three times counter-clockwise while holding peppermint tea, the laws of gravity become polite suggestions.",
                        "Today's official agenda: collecting clouds in mason jars and naming every single raindrop.",
                        "What if keys don't unlock doors, but teach doors how to forget being closed?"
                };
                return prompts[RANDOM.nextInt(prompts.length)];
            }
            case PHILOSOPHICAL: {
                String[] prompts = {
                        "We do not cross the same river twice, yet the thirst of the wanderer never changes.",
                        "Reflecting on whether our choices define our destiny, or if destiny merely wears the mask of choice.",
                        "In stillness, the answers we frantically pursue often find their way to our doorstep."
                };
                return prompts[RANDOM.nextInt(prompts.length)];
            }
            default: {
                String[] prompts = {
                        "Notes from the road: unexpected discoveries often come disguised as simple interruptions.",
                        "Taking a pause to reflect on recent developments. Much to consider moving forward.",
                        "Observing the shifting tide of conversations here. What new horizon are you aiming for today?"
                };
                return prompts[RANDOM.nextInt(prompts.length)];
            }
        }
    }

    public static String generateReplyIdea(@NonNull PersonaEntity persona, @Nullable String parentText) {
        Archetype archetype = detectArchetype(persona);

        switch (archetype) {
            case MYSTIC:
                return "The threads of prophecy weave your words into an undeniable pattern. Tread carefully.";
            case SARCASTIC:
                return "Fascinating theory. Did you formulate that all by yourself, or did you consult an oracle of bad ideas?";
            case ROYAL:
                return "Your sentiment is acknowledged and weighed with the gravity due by the court.";
            case CYBERPUNK:
                return "Signal received on 144.2 MHz. Raw telemetry matches your hypothesis—let's route through the proxy.";
            case WHIMSICAL:
                return "Ooh! That gives me the wildest idea involving sparkly chalk and midnight tea!";
            case PHILOSOPHICAL:
                return "A profound observation. Yet consider the inverse: does the shadow not require the light?";
            default:
                return "An intriguing perspective. Let us see how events unfold.";
        }
    }
}
