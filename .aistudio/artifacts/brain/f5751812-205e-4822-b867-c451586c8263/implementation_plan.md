# Voice Input Search (English & German) & Synchronized Storage Button Sizing

Integrate bidirectional voice search functionality (German & English) into both the main grammar search bar and the history log search bar, and update the "Set Storage as Primary" button with the exact SVG log icon while harmonizing it with the adjacent "Delete Storage" button to an identical, well-proportioned size (38.dp) that preserves all rich icon details.

## User Review & Critical Decisions

> [!IMPORTANT]
> - **Matched Button Sizing & Detail Preservation (Confirmed)**:
>   Both the **"Set as Primary Storage"** button and the adjacent **"Delete Storage"** button will be formatted with **identical dimensions (`38.dp x 38.dp`)**:
>   - **Size Rationale (`38.dp`)**: Neither too large to overwhelm the backup folder row, nor too small to blur the intricate Log cover, folded sheet, "Log" typography, lines, and faceted gold star. The icon within renders at a crisp `26.dp–28.dp`.
>   - **Visual Consistency**: Both buttons share matching rounded corner styling (`RoundedCornerShape(10.dp)`), subtle borders, and elevation, creating a balanced action pair.
>   - **Delete Storage Button**: Styled with a matching `38.dp` container with a soft container background and error-toned `Icons.Default.DeleteOutline`, perfectly mirroring the Primary button's silhouette.
> - **Exact SVG Icon for Primary Storage**:
>   Built using the exact SVG code provided:
>   - Dark navy cover (`#182335` to `#2d3f5d`) with rounded corners.
>   - White sheet with folded bottom-right corner and delicate gradient fold.
>   - Bold "Log" text and 3 horizontal data bars (`#86868b`).
>   - Golden faceted star (`#fff085`, `#fde047`, `#f59e0b`, `#d97706`, `#b45309`) tilted at `-8°`.
> - **Voice Input Flow (German & English)**:
>   - Microphone icons placed inside both search bars next to the clear button.
>   - Tapping opens a quick language selector dialog (`Deutsch` vs `English`) pre-selecting the active app language.
>   - Recognized text populates the search field for user review and editing without auto-submitting.

---

## 1. Overview & Core Concept

1. **Voice Input Search**:
   - Hands-free query input in both the main grammar query field and the history log search field.
   - User can choose German (`de-DE`) or English (`en-US`) on each voice interaction.
   - Built on Android OS `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` with native audio waveform and zero dangerous runtime permissions.
2. **Harmonized Storage Action Buttons**:
   - Updates the "Set as Primary Storage" button to use the user's exact SVG vector / asset.
   - Synchronizes the adjacent "Delete Storage" button to the exact same size (`38.dp`), with consistent container styling, spacing, and touch targets.

---

## 2. User Experience & Visual Design

### Storage Action Buttons Comparison & Hierarchy

```
Non-Primary Backup Folder Row:
┌────────────────────────────────────────────────────────────────────────┐
│  📁 /Storage/Emulated/Documents/Backup                                  │
│  Active • Last sync: 10.11.2026                                         │
│                                            ┌──────────┐  ┌──────────┐  │
│                                            │  [Log★]  │  │   [🗑]   │  │
│                                            │ (38.dp)  │  │ (38.dp)  │  │
│                                            └──────────┘  └──────────┘  │
│                                            Set Primary      Delete     │
└────────────────────────────────────────────────────────────────────────┘

Primary Storage Folder Row:
┌────────────────────────────────────────────────────────────────────────┐
│  ⭐ [Primary Badge] /Storage/PrimaryHistory                            │
│  Active • Last sync: 10.11.2026                          ┌──────────┐  │
│                                                          │   [🗑]   │  │
│                                                          │ (38.dp)  │  │
│                                                          └──────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

- **Set Primary Button**: `38.dp x 38.dp`, `RoundedCornerShape(10.dp)`, filled with `primaryContainer` and bordered with `primary.copy(alpha = 0.7f)`, rendering the full Log document with golden star.
- **Delete Button**: `38.dp x 38.dp`, `RoundedCornerShape(10.dp)`, filled with `errorContainer.copy(alpha = 0.35f)` and bordered with `error.copy(alpha = 0.45f)`, hosting `Icons.Default.DeleteOutline` (20.dp).

### Voice Search Interaction Flow

```
[Tap 🎤 in Search Field]
          │
          ▼
┌────────────────────────────────────────┐
│     Voice Input Language / Sprache     │
│                                        │
│  [ 🇩🇪 Deutsch (de-DE) ]  ← Highlighted │
│  [ 🇬🇧 English (en-US) ]                │
└────────────────────────────────────────┘
          │ (Tap Language)
          ▼
┌────────────────────────────────────────┐
│ Android Native Speech Recognizer (OS) │
│   "Sprechen Sie jetzt..." / Waveform   │
└────────────────────────────────────────┘
          │ (Speech Transcribed)
          ▼
┌────────────────────────────────────────┐
│ Search field populated with text       │
│  (User reviews, edits, or taps Search) │
└────────────────────────────────────────┘
```

---

## 3. Technical Architecture & File Changes

```
┌───────────────────────────────────────────────────────────────────────────┐
│                                MainScreen                                 │
│                                                                           │
│   ┌────────────────────────────────────────────────────────────────────┐  │
│   │ Main Search Bar                                                    │  │
│   │ [🔍 Search] [Query Input] [🎤 Mic Button] [✖ Clear] [Suchen Button] │  │
│   └────────────────────────────────────────────────────────────────────┘  │
│                                     │                                     │
│   ┌─────────────────────────────────┼──────────────────────────────────┐  │
│   │ HistorySection                  ▼                                  │  │
│   │ ┌────────────────────────────────────────────────────────────────┐ │  │
│   │ │ History Search Bar                                             │ │  │
│   │ │ [🔍 Filter] [History Query] [🎤 Mic Button] [✖ Clear]          │ │  │
│   │ └────────────────────────────────────────────────────────────────┘ │  │
│   └─────────────────────────────────┬──────────────────────────────────┘  │
│                                     │                                     │
│                                     ▼                                     │
│   ┌────────────────────────────────────────────────────────────────────┐  │
│   │ VoiceSearchLanguageDialog                                          │  │
│   │ • 🇩🇪 Deutsch (de-DE)                                              │  │
│   │ • 🇬🇧 English (en-US)                                              │  │
│   └─────────────────────────────────┬──────────────────────────────────┘  │
│                                     ▼                                     │
│   ┌────────────────────────────────────────────────────────────────────┐  │
│   │ RecognizerIntent.ACTION_RECOGNIZE_SPEECH                           │  │
│   │ • Populates searchQuery or historySearchQuery                     │  │
│   └────────────────────────────────────────────────────────────────────┘  │
│                                                                           │
│   ┌────────────────────────────────────────────────────────────────────┐  │
│   │ NotebookManagerDialog (NotebookComponents.kt)                      │  │
│   │ • Set Primary Button: 38.dp Surface with exact Log★ SVG            │  │
│   │ • Delete Button: 38.dp Surface with Delete icon                    │  │
│   └────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────┘
```

### Planned File Edits

1. **`app/src/main/res/drawable/ic_set_primary_storage_log.xml`**:
   - Add the exact SVG vector asset converted for Android (256x256 viewport, `#182335` to `#2d3f5d` cover, white sheet with fold, "Log" typography, lines, faceted golden star).
   - Also copy `public/assets/aistudio/log_icon_96_437e062a.png` as drawable asset `ic_primary_storage_log_png.png`.

2. **`app/src/main/java/com/example/ui/components/NotebookComponents.kt`**:
   - Update `NotebookManagerDialog`:
     - Set primary button: `Modifier.size(38.dp)`, displaying the exact Log SVG icon (`26.dp`), with `testTag = "set_primary_button_${folder.uriString.hashCode()}"`.
     - Delete button: `Surface` of `Modifier.size(38.dp)` with `RoundedCornerShape(10.dp)`, containing `Icons.Default.DeleteOutline` (20.dp) with `testTag = "delete_folder_button_${folder.uriString.hashCode()}"`.

3. **`app/src/main/java/com/example/ui/components/VoiceSearchLanguageDialog.kt`** (New):
   - M3 Dialog offering immediate choice between `🇩🇪 Deutsch (de-DE)` and `🇬🇧 English (en-US)`.
   - Passes selected locale string to the speech recognition launcher.

4. **`app/src/main/java/com/example/ui/MainScreen.kt`**:
   - Register `rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult())`.
   - Add the microphone icon button inside the main search bar next to the clear button.
   - On recognized text, update `uiState.searchQuery` via `viewModel.onQueryChange(text)`.

5. **`app/src/main/java/com/example/ui/components/HistorySection.kt`**:
   - Add the microphone icon button inside `HistorySearchBar` next to the clear button.
   - On recognized text, update `historySearchQuery` via `onSearchQueryChange(text)`.

6. **`app/src/main/java/com/example/ui/i18n/SettingsLocalization.kt`**:
   - Add localized strings across `DE_STRINGS`, `EN_STRINGS`, and `AR_STRINGS` for voice search prompts and descriptions.

---

## 4. Verification & Testing

1. **Compilation**: Run `compile_applet` to confirm vector drawables, activities, and Compose layouts compile cleanly.
2. **Unit Tests**: Run `gradle :app:testDebugUnitTest --tests "com.example.util.HistoryFilterHelperTest"` to ensure search ranking and filtering are completely unaffected.
3. **UI Verification**:
   - Inspect the primary storage and delete buttons in the backup folder section to confirm they match at `38.dp` and that the Log + Star details are sharp and clear.
   - Inspect the microphone button in both search bars.
   - Verify the voice language dialog opens and launches speech recognition without errors.
