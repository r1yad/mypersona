# PersonaNet — Offline Role-Playing Social Media Simulator

PersonaNet is a 100% native Java Android application built for offline role-playing and character-driven social simulation. Users can create, switch between, and manage multiple fictional personas, publish timeline posts, interact via nested comments and likes, and direct message between characters—all with zero external network dependencies and complete backward/forward-compatible JSON/ZIP backup and restore.

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural and design decisions below incorporate your answers from the clarification phase. Please review before proceeding.

- **Visual Theme & Palette (Confirmed)**: Deep Indigo and Slate Dark Mode featuring midnight slate backgrounds (`#0B0E14`), deep indigo surfaces (`#161B26` / `#1F2637`), radiant indigo accents (`#6366F1`), and electric violet accents (`#8B5CF6`).
- **Initial Data State (Confirmed)**: Starts completely empty with zero pre-seeded personas. First launch presents an inviting onboarding empty state with a "Create Your First Persona" call to action.
- **Layout & Navigation (Confirmed)**: Card-based timeline feed with Material 3 Bottom Navigation, top app bar character badge with quick-switch BottomSheetDialogFragment, and dedicated screens for Feed, Messages (DMs), Personas, and Settings.
- **100% Native Java Enforcement**: No Kotlin source files or hybrid Kotlin artifacts. Utilizes Java `ExecutorService`, `LiveData`, ViewBinding, and Room Java annotations (`@Entity`, `@Dao`, `@Database`).

---

## 1. Overview & Core Concept

PersonaNet allows writers, tabletop gamers, and creative storytellers to simulate social network interactions between their original characters entirely on their own Android device. Users can:
- Author and maintain multiple distinct personas with rich traits (personality, speaking style, instructions, background lore, avatars, banners).
- Seamlessly switch their "Active Persona" via a persistent top-bar switcher.
- Post feed updates with text and images, like posts from any persona, and leave threaded comments.
- Roleplay bilateral direct messages (DMs) where the user can swap speaking perspectives dynamically mid-conversation.
- Leverage an offline, rule-based Roleplay Assistant Engine tailored to character traits (Mystic, Sarcastic, Royal, Cyberpunk, Whimsical) for creative dialogue and post suggestions.
- Export and import complete archives (metadata, Room tables in `data.json`, and extracted media files in `media/`) matching the legacy PersonaNet `.zip` backup specification.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **First Launch & Persona Creation**:
   - Empty state explains the concept with an illustrative graphic and prompt: "Your fictional social network awaits. Create your first persona to start publishing and chatting."
   - Persona Creation Form: display name, unique handle `@username`, bio, avatar/banner selection, and roleplay attributes (personality traits, speaking style, instructions, background story).
2. **Feed & Timeline**:
   - Header shows the currently active persona badge; tapping opens the quick-switch bottom sheet.
   - Posts render as elevated cards with avatar, display name, handle, timestamp, body text, multi-image preview, and interactive like and comment buttons.
   - Floating Action Button (FAB) opens post composer with persona selector, character count, image picker, and Roleplay Assistant idea prompt.
   - Tapping a post opens the Post Detail Screen with full comments list, reply input, and like toggle.
3. **Direct Messages (DMs)**:
   - Conversation list displays character pairs, timestamp, and last message preview.
   - Chat view displays styled bubbles (incoming vs. outgoing based on current speaker). A quick persona toggle button in the chat header lets the user roleplay both sides of the dialogue fluidly.
4. **Persona Directory**:
   - Grid or card list of all created personas, showing their stats (post count, conversations), edit button, and "Set as Active" action.
5. **Settings, Backup & Restore**:
   - System controls for Full Restore (clean wipe and unpack) and Merge Import (preserve existing data, resolve conflicts by appending `_imported`).
   - Export Archive creates a compliant `.zip` containing `manifest.json`, `data.json`, and `media/` image assets.

### Visual Identity & Theme Tokens
- **Background**: `#0B0E14` (Deep Midnight Slate)
- **Surface**: `#161B26` (Dark Navy/Slate Surface)
- **Surface Variant / Cards**: `#1F2637` (Elevated Charcoal Navy)
- **Primary / Accent**: `#6366F1` (Indigo Glow)
- **Secondary / Accent**: `#8B5CF6` (Electric Violet)
- **Text Primary**: `#F1F5F9` (High-contrast Off-White)
- **Text Secondary / Muted**: `#94A3B8` (Slate Gray)
- **Divider & Borders**: `#2E384D`
- **Success / Positive**: `#10B981` (Emerald)

---

## 3. Key Product Decisions & Trade-Offs

- **Java-Exclusive Toolchain**:
  - *Chosen Approach*: Configure `build.gradle.kts` for Java ViewBinding, Room Java compiler annotation processing (`annotationProcessor` / `ksp`), and remove all Kotlin files (`MainActivity.kt`, theme files, etc.) replacing them with clean Java implementations.
  - *Why*: Satisfies strict 100% Java architectural mandate while maintaining Android Gradle Plugin 9.x compatibility.
- **Classic Android Views + Material Components 3**:
  - *Chosen Approach*: XML layouts with Material 3 styling (`Theme.Material3.DayNight.NoActionBar`), `MaterialCardView`, `BottomNavigationView`, `FloatingActionButton`, and `RecyclerView`.
  - *Why*: Guarantees rock-solid native Java performance, robust ViewBinding type-safety, and seamless compatibility without Compose runtime dependencies.
- **Unified Legacy Backup Engine**:
  - *Chosen Approach*: Streamlined `BackupManager` using native `ZipInputStream`, `ZipOutputStream`, and `org.json.JSONObject` / `org.json.JSONArray`.
  - *Why*: Guarantees zero regression on legacy archives without needing heavy reflection or third-party serialization libraries.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Presentation Layer (Java)                       │
│  ┌────────────────────┐  ┌──────────────────┐  ┌────────────────────┐  │
│  │    FeedFragment    │  │ MessagesFragment │  │  PersonasFragment  │  │
│  └─────────┬──────────┘  └────────┬─────────┘  └─────────┬──────────┘  │
│            │                      │                      │             │
│            ▼                      ▼                      ▼             │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │            ViewModels (FeedViewModel, ChatViewModel, etc.)       │  │
│  │                    LiveData & MutableLiveData                    │  │
│  └──────────────────────────────────┬───────────────────────────────┘  │
└─────────────────────────────────────┼──────────────────────────────────┘
                                      │
┌─────────────────────────────────────▼──────────────────────────────────┐
│                           Repository Layer                             │
│   AppRepository (Java ExecutorService for async background tasks)      │
│   BackupManager (Zip & JSON parser for legacy export/import)          │
│   RoleplayAssistant (Rule-based prompt & dialogue generator)           │
└─────────────────────────────────────┬──────────────────────────────────┘
                                      │
┌─────────────────────────────────────▼──────────────────────────────────┐
│                           Room Database (Java)                         │
│  AppDatabase:                                                          │
│  - PersonaDao          - PostDao            - CommentDao               │
│  - LikeDao             - ConversationDao    - MessageDao               │
│  Entities: PersonaEntity, PostEntity, PostMediaEntity, CommentEntity,  │
│            PostLikeEntity, CommentLikeEntity, ConversationEntity,      │
│            MessageEntity, MediaAssetEntity                             │
│  Storage: SQLite on device + internal filesDir/media for image files   │
└────────────────────────────────────────────────────────────────────────┘
```

### Complete Room Entity Specification
1. `PersonaEntity`: `id` (PK, UUID string), `displayName`, `username`, `bio`, `avatarPath`, `coverPath`, `personalityTraits`, `speakingStyle`, `roleplayInstructions`, `backgroundStory`, `createdAt`, `updatedAt`.
2. `PostEntity`: `id` (PK), `authorPersonaId`, `textContent`, `createdAt`, `updatedAt`.
3. `PostMediaEntity`: `id` (PK), `postId`, `mediaPath`, `mimeType`, `orderIndex`.
4. `CommentEntity`: `id` (PK), `postId`, `parentCommentId`, `authorPersonaId`, `textContent`, `createdAt`, `updatedAt`.
5. `PostLikeEntity`: composite PK (`postId`, `personaId`), `createdAt`.
6. `CommentLikeEntity`: composite PK (`commentId`, `personaId`), `createdAt`.
7. `ConversationEntity`: `id` (PK), `personaAId`, `personaBId`, `createdAt`, `lastActivityAt`, `lastMessagePreview`.
8. `MessageEntity`: `id` (PK), `conversationId`, `senderPersonaId`, `replyToMessageId`, `textContent`, `mediaPath`, `mimeType`, `createdAt`, `isEdited`, `isRead`.
9. `MediaAssetEntity`: `id` (PK), `relativePath`, `mimeType`, `width`, `height`, `fileSizeBytes`, `createdAt`.

---

## 5. Execution Plan

1. **Build & Toolchain Configuration**:
   - Update `app/build.gradle.kts` to enable ViewBinding, configure Room compiler for Java, remove Kotlin compose dependencies, and set `minSdk = 26`.
   - Update `metadata.json` and `res/values/strings.xml` to set application name to "PersonaNet".
2. **Theme, Colors & Drawables**:
   - Create Material 3 XML styles, custom colors (Deep Indigo/Slate), and vector drawables for navigation, avatars, and actions.
3. **Database & Data Layer (Room & DAOs in Java)**:
   - Implement all 9 entities, corresponding DAOs, and `AppDatabase`.
   - Implement `AppRepository` managing disk I/O on Java `ExecutorService`.
4. **Backup & Restore System**:
   - Implement `BackupManager` supporting full wipe restore and conflict-merging restore using `ZipInputStream`/`ZipOutputStream` and `org.json`.
5. **UI & Screens (Java + XML ViewBinding)**:
   - `MainActivity`: Bottom navigation coordinator, top bar active persona chip, and quick-switch modal sheet.
   - `FeedFragment` & `PostDetailActivity`: Posts list, post creator with roleplay assistant prompt, comments recycler view.
   - `MessagesFragment` & `ChatActivity`: Conversation list and chat thread with bidirectional persona switcher.
   - `PersonasFragment` & `PersonaEditActivity`: Persona cards and profile editor.
   - `SettingsFragment`: Export and Import actions with summary display.
6. **Compilation & Verification**:
   - Run `compile_applet` to verify clean build without warnings or missing symbols.
