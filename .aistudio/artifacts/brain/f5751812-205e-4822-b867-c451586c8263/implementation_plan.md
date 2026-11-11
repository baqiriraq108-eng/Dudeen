# Unified Design System Refactoring: Dialogs, Outline-Only Selection & Dotted Speech Tempo Slider

This plan unifies the application's interactive patterns across three core pillars: standardized modal/sheet action buttons (`Confirm` / `Cancel` in English and `Bestätigen` / `Abbrechen` in German with matching geometry), a crisp outline-only selection system across every selectable component (zero background tinting), and a high-performance custom dotted Speech Tempo (`Sprech-Tempo`) slider with edge insets and lag-free scrubbing.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following design and behavioral choices have been confirmed during Phase 1 and govern this refactoring:

- **Confirmed Decision 1 — Uniform Dialog & Sheet Action Labels**: All procedural modals, alert dialogs, and bottom sheets across the application (including creation, saving, deletion, settings, and export flows) will standardize on **"Confirm" / "Cancel"** in English and **"Bestätigen" / "Abbrechen"** in German, housed in identically sized action containers (`42.dp` height, equal flex weight or matched minimum width, `10.dp` corner radius).
- **Confirmed Decision 2 — Universal Outline-Only Selection**: Every selectable element—including grammar category tabs, history filter chips, sort chips, notebook cards, backup storage cards, theme/mode selectors, and settings options—will indicate its active/selected state exclusively via a **`1.5.dp–2.dp` accent border stroke**, keeping the interior container background unchanged and free of solid or tinted color fills.
- **Confirmed Decision 3 — Active/Inactive Dotted Slider Track with Edge Insets**: The Speech Tempo (`Sprech-Tempo`) slider track will render a custom Canvas-based dotted pattern inset from both the left and right terminals, where dots to the left of the thumb illuminate in the primary accent color and dots to the right render in a muted outline tone.

---

## 1. Overview & Core Concept

- **What It Does**: Elevates visual consistency and tactile responsiveness across the entire German grammar and vocabulary workspace by eliminating visual clutter (mixed button labels, heavy background fills on selected items, and stepped slider hitching).
- **Target Audience / Persona**: Bilingual (German/English) language learners and power users who rely on fast, distraction-free dictionary lookups, audio pronunciation scrubbing, and multi-notebook organization.
- **Key Value**:
  - **Predictable Modal Ergonomics**: Every dialog and sheet presents identically proportioned primary and secondary action buttons with consistent linguistic framing.
  - **Restrained Visual Hierarchy**: Replacing heavy selection fills with crisp accent borders preserves dark/light surface contrast and keeps text legibility high.
  - **Zero-Lag Audio Control**: Decoupling drag-scrub UI state from TTS engine persistence eliminates frame drops while providing a refined, tactile dotted track.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Procedural Dialogs & Sheets**: Whether creating a notebook, confirming storage changes, clearing history, configuring API keys, or applying a theme in a bottom sheet, the user encounters a symmetrical two-button footer: an outline secondary button (`Cancel` / `Abbrechen`) paired with a filled primary button (`Confirm` / `Bestätigen`) of identical height (`42.dp`), corner radius (`10.dp`), and balanced width.
  2. **Selecting Tabs, Filters, Notebooks & Folders**: Tapping a grammar tab (`Ausdrücke`, `Verb`, `Wörter`, `Adjektive`), a history filter chip, a sort criterion, a language/provider option, or a notebook/storage card immediately highlights its perimeter with a `1.5.dp–2.dp` accent border (`GeoPrimary` or `MaterialTheme.colorScheme.primary`) while preserving the neutral surface background (`GeoSurface` / `GeoSurfaceVariant`) underneath.
  3. **Adjusting Speech Tempo (`Sprech-Tempo`)**: Dragging the speech speed thumb glides continuously at 60–120fps across a custom horizontal dotted track. The extreme left and right ends of the track feature a clean horizontal inset so dots never clip against the thumb boundaries or track edges. Only when the finger releases (or +/- / preset is tapped) does the updated speed commit to persistent storage and the TTS engine.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Precision geometric minimalism with crisp borders and zero visual bloat.
  - *Color Palette & Mood*:
    - **Primary Action ("Confirm" / "Bestätigen")**: Filled with `GeoPrimary` / `MaterialTheme.colorScheme.primary` and `onPrimary` label contrast.
    - **Secondary Action ("Cancel" / "Abbrechen")**: Transparent/surface container with a `1.dp` `GeoBorder` / `outlineVariant` stroke and `GeoOnSurface` label.
    - **Selected State**: `1.5.dp` to `2.dp` `GeoPrimary` border stroke over an unaltered `GeoSurface` or `GeoSurfaceVariant` background (`Color.Transparent` overlay).
    - **Dotted Slider Track**: Active dots in `GeoPrimary`; inactive dots in `GeoOnSurfaceVariant.copy(alpha = 0.35f)`.
  - *Typography & Hierarchy*: Bold `13.sp–14.sp` single-line button and chip labels (`maxLines = 1`, `softWrap = false`); selected items pair the accent border with bold/primary-colored typography and icons for clear dual-coding.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Reusable Dialog Action Bar Component**
  - *Chosen Approach*: Introduce a shared `StandardDialogActionRow` (and aligned `confirmButton` / `dismissButton` button builders) enforcing `Modifier.weight(1f).height(42.dp)` and `RoundedCornerShape(10.dp)` with centralized `strings.confirm` (`Confirm` / `Bestätigen`) and `strings.cancel` (`Cancel` / `Abbrechen`).
  - *Why*: Guarantees pixel-identical button sizing and label consistency across every `AlertDialog` and `ModalBottomSheet` without ad-hoc per-dialog styling drift.
  - *Alternatives Considered*: Styling each dialog's buttons inline—rejected because Material 3 `AlertDialog` default `confirmButton`/`dismissButton` slots wrap content unevenly unless constrained by a shared sizing specification.

- **Decision 2: Outline-Only Selection Token System**
  - *Chosen Approach*: Replace all conditional `containerColor = if (isSelected) primary else surfaceVariant` and `Modifier.background(if (isSelected) ...)` patterns with constant neutral surface backgrounds paired with `BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) GeoPrimary else GeoBorder)`.
  - *Why*: Eliminates jarring blocks of solid color across dark and custom themes while keeping active states unmistakable through border weight, accent stroke color, and text/icon tinting.

- **Decision 3: Decoupled Scrubbing State & Custom Canvas Dotted Track for `Sprech-Tempo`**
  - *Chosen Approach*: Maintain a fast, local un-stepped `Float` state during pointer drag gestures, committing the rounded `0.1x` increment via `onValueChangeFinished` (and on +/- or preset taps), paired with a custom `SliderDefaults.Track` (or custom Canvas overlay) that draws evenly spaced circular dots inset by `10.dp` from both track ends.
  - *Why*: Writing to DataStore/SharedPreferences and re-initializing TTS parameters on every intermediate drag pixel causes main-thread stutter; committing on drag completion makes scrubbing instantaneous while keeping the live numeric readout (`1.0x`) responsive in real time.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────────┐
│                        Localization & Design Tokens                        │
│  • SettingsStrings: confirm ("Confirm" / "Bestätigen")                     │
│                     cancel  ("Cancel"  / "Abbrechen")                      │
│  • Selection Style: Neutral Surface + 1.5dp–2dp Accent Border (No Fill)    │
└───────────────────┬────────────────────────────────────┬───────────────────┘
                    │                                    │
                    ▼                                    ▼
┌───────────────────────────────────────┐  ┌─────────────────────────────────┐
│     Unified Modal & Sheet Actions     │  │  Outline-Only Selectable Views  │
│  • Equal-sized 42.dp Action Buttons   │  │  • Grammar Category Tabs        │
│  • RoundedCornerShape(10.dp)          │  │  • History & Sort Filter Chips  │
│  • Applied across:                    │  │  • Notebook & Backup Cards      │
│    - Notebook Create/Rename/Delete    │  │  • AI Provider & Language Pills │
│    - Backup & Primary Storage Modals  │  │  • Theme Mode & Color Swatches  │
│    - Settings / API Key / Clear Logs  │  │  • Export Scope/Format/Paper    │
│    - Color, Sort & Export Dialogs     │  └─────────────────────────────────┘
└───────────────────────────────────────┘
                    │
                    ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                 Optimized Speech Tempo Control (Sprech-Tempo)              │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │ Local Drag State (60fps continuous float) ──► Live Text ("1.2x")     │  │
│  │ Custom Canvas Dotted Track:                                          │  │
│  │   [ 10.dp Inset ]  ●  ●  ●  ●  (Thumb)  ○  ○  ○  [ 10.dp Inset ]     │  │
│  │                    └── Active ──┘       └─ Muted ─┘                  │  │
│  │ onValueChangeFinished ──► Snap 0.1x ──► ViewModel / TTS Persistence  │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────────┘
```

### Interactive Component & State Mapping

1. **Localization Dictionary (`SettingsStrings`)**:
   - Standardize all procedural confirmation strings in `EN_STRINGS` to `"Confirm"` / `"Cancel"` and in `DE_STRINGS` to `"Bestätigen"` / `"Abbrechen"`.
2. **Dialog & Bottom Sheet Footer Standardization**:
   - All `AlertDialog` and `ModalBottomSheet` action rows use matching `42.dp` height, `RoundedCornerShape(10.dp)`, `1.dp` outline border for `"Cancel" / "Abbrechen"`, and filled primary tone for `"Confirm" / "Bestätigen"`.
3. **Outline-Only Selection Conversion**:
   - **Main Screen**: Grammar type tabs (`Ausdrücke`, `Verb`, `Wörter`, `Adjektive`) and result declension/conjugation tabs use neutral `GeoSurfaceVariant` background with a `1.5.dp` `GeoPrimary` border when selected.
   - **History & Sort Sheets**: Category filter chips, sort direction chips, and sort criteria pills remove solid primary background fills and apply a `1.5.dp` `GeoPrimary` border when active.
   - **Notebooks & Storage**: Active notebook items, theme color selectors, and primary/selected storage cards use neutral card backgrounds with a `1.5.dp–2.dp` accent border.
   - **Settings, Color & Export Modals**: AI provider cards, language toggles, theme mode `FilterChip`s (`System`, `Light`, `Dark`), and export scope/format/orientation selectors use transparent/neutral backgrounds with a `1.5.dp` accent border for the selected state.
4. **Speech Tempo Slider (`SprechTempoControl` & `TtsSpeedSlider`)**:
   - **Smooth Scrubbing**: `rawSliderValue` updates purely in local `mutableFloatStateOf` during `onValueChange` without triggering external ViewModel/TTS writes; `onValueChangeFinished` snaps to the nearest `0.1f` and invokes `onSpeedChange`.
   - **Custom Inset Dotted Track**: Custom `track` lambda draws a row of evenly spaced circular dots (`3.5.dp` diameter) across the track width with a `10.dp` horizontal inset on both the left and right ends so no dots appear at the far terminals. Dots at positions `<= thumbFraction` render in `GeoPrimary`, and dots `> thumbFraction` render in `GeoOnSurfaceVariant.copy(alpha = 0.35f)`.
