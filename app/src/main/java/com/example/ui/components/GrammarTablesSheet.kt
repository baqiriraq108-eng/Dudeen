package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.example.data.repository.GrammarTablesRepository
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import com.example.R
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GrammarExample
import com.example.data.model.GrammarReferenceTable
import com.example.data.model.GrammarTablesData
import com.example.data.model.PrepositionGroup
import com.example.data.model.PronunciationRule
import com.example.data.model.TwoWayPrepositionItem
import com.example.data.model.VerbConjugationResult
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoCardRibbonColor
import com.example.ui.theme.GeoIsBoldTheme
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoOutline
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoSecondary
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant
import com.example.ui.theme.LocalIsDarkTheme
import java.util.Locale
import kotlin.math.round
import kotlinx.coroutines.launch

private val SPEECH_SPEED_OPTIONS: List<Float> = listOf(0.3f, 0.5f, 0.7f, 1.0f, 1.3f, 1.5f, 2.0f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarTablesSheet(
    onDismiss: () -> Unit,
    onSpeak: (String, String) -> Unit,
    currentlySpokenId: String?,
    ttsSpeed: Float,
    onTtsSpeedChange: (Float) -> Unit,
    verbResult: VerbConjugationResult? = null,
    isVerbLoading: Boolean = false,
    verbError: String? = null,
    onSearchVerb: (String) -> Unit = {},
    onClearVerb: () -> Unit = {},
    appLanguage: String = "de",
    modifier: Modifier = Modifier
) {
    var allowDismissFromTop by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            if (sheetValue == SheetValue.Hidden) {
                allowDismissFromTop
            } else {
                true
            }
        }
    )
    val coroutineScope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(GrammarTablesRepository.lastSelectedTabIndex) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTabIndex) {
        GrammarTablesRepository.lastSelectedTabIndex = selectedTabIndex
    }

    fun dismissSheet() {
        allowDismissFromTop = true
        coroutineScope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    val categories = remember { GrammarTablesData.CATEGORIES }
    val tabTitles = remember(appLanguage) {
        categories.map { it.localizedTitle(appLanguage) } + listOf(
            when (appLanguage.lowercase()) {
                "en" -> "Pronunciation Guide"
                "ar" -> "دليل النطق"
                else -> "Aussprache-Guide"
            }
        )
    }

    val blockSheetNestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // Absorb any leftover downward/upward scroll from content
                // so it never bubbles up to trigger ModalBottomSheet dragging/dismissal.
                return available
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                // Absorb any leftover fling velocity so it doesn't fling the sheet closed
                return available
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            allowDismissFromTop = true
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = GeoSurface,
        dragHandle = {
            // Top Dismiss Area: Drag handle area swiping down dismisses the window
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .pointerInput(Unit) {
                        var totalDrag = 0f
                        detectVerticalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                                if (totalDrag > 25f) {
                                    dismissSheet()
                                }
                            }
                        )
                    }
                    .padding(top = 16.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(GeoOutline.copy(alpha = 0.5f))
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag("grammar_tables_modal_sheet")
    ) {
        // Rest of the window: Scrolls content only, never dismisses from reading area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .nestedScroll(blockSheetNestedScroll)
                .padding(bottom = 12.dp)
        ) {
            // Header Bar: Part of top dismiss area (dismissible by swiping down here as well)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        var totalDrag = 0f
                        detectVerticalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                                if (totalDrag > 35f) {
                                    dismissSheet()
                                }
                            }
                        )
                    }
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Open Grammar Book Emblem (MenuBook matching the Grammar button app-wide)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    GeoPrimary.copy(alpha = 0.16f),
                                    GeoSecondary.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(1.dp, GeoPrimary.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
                        .testTag("grammar_header_book_icon")
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = GeoPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    val headerTitle = when (appLanguage.lowercase()) {
                        "en" -> "Grammar Tables"
                        "ar" -> "جداول القواعد"
                        else -> "Grammatik-Tabellen"
                    }
                    val headerSubtitle = when (appLanguage.lowercase()) {
                        "en" -> "Overviews, Rules & Pronunciation"
                        "ar" -> "نظرة عامة، القواعد والنطق"
                        else -> "Übersichten, Regeln & Aussprache"
                    }

                    ScrollableCardTitle(
                        text = headerTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnSurface
                    )
                    ScrollableCardSubtitle(
                        text = headerSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = GeoOnSurfaceVariant
                    )
                }

                // Polished Dual-Tone Gradient Capsule for Speech Rate with Circular Speedometer Badge
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    GeoPrimary.copy(alpha = 0.14f),
                                    GeoSecondary.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    GeoPrimary.copy(alpha = 0.45f),
                                    GeoSecondary.copy(alpha = 0.28f)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { showSpeedDialog = true }
                        .padding(start = 5.dp, end = 9.dp)
                        .testTag("speech_rate_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(GeoPrimary, GeoSecondary)
                                    )
                                )
                                .border(1.2.dp, Color.White.copy(alpha = 0.72f), CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_playback_speed),
                                contentDescription = "Sprechtempo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.width(44.dp)
                        ) {
                            FixedTabularSpeedText(
                                speedText = formatSpeechSpeed(ttsSpeed),
                                color = GeoPrimary,
                                maxFontSize = 11.5.sp,
                                minFontSize = 8.5.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Unified Popup Dialog for Speech Rate Selection
            if (showSpeedDialog) {
                SpeechSpeedSelectionDialog(
                    initialSpeed = ttsSpeed,
                    onConfirmSpeed = onTtsSpeedChange,
                    onDismiss = { showSpeedDialog = false },
                    appLanguage = appLanguage
                )
            }

            // Category Tabs: [ Pronouns ]  [ Prepositions ]  [ Pronunciation Guide ]
            val safeTabIndex = selectedTabIndex.coerceIn(0, (tabTitles.size - 1).coerceAtLeast(0))
            ScrollableTabRow(
                selectedTabIndex = safeTabIndex,
                containerColor = GeoSurface,
                contentColor = GeoPrimary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (safeTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[safeTabIndex]),
                            color = GeoPrimary,
                            height = 3.dp
                        )
                    }
                },
                divider = { HorizontalDivider(color = GeoBorder) }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val isSelected = safeTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GeoPrimary else GeoOnSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Per-tab scroll state restoration
            val cachedScroll = remember(safeTabIndex) {
                GrammarTablesRepository.getScrollPosition(safeTabIndex)
            }
            val lazyListState = rememberLazyListState(
                initialFirstVisibleItemIndex = cachedScroll?.first ?: 0,
                initialFirstVisibleItemScrollOffset = cachedScroll?.second ?: 0
            )

            LaunchedEffect(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset) {
                GrammarTablesRepository.saveScrollPosition(
                    safeTabIndex,
                    lazyListState.firstVisibleItemIndex,
                    lazyListState.firstVisibleItemScrollOffset
                )
            }

            val currentCategory = if (safeTabIndex < categories.size) categories[safeTabIndex] else null

            // Unified vertical scrollable view with clean, un-nested premium tables (144Hz high refresh rate optimized)
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                if (currentCategory != null) {
                    // Dedicated Premium Preposition Tables (Präpositionen)
                    if (currentCategory.id == "praepositionen") {
                        // Section 1: Fixed Prepositions (Akkusativ, Dativ, Genitiv) - One clean table per case
                        items(
                            items = GrammarTablesRepository.fixedPrepositionGroups,
                            key = { it.kasusId },
                            contentType = { "preposition_group_card" }
                        ) { group ->
                            PremiumPrepositionCaseTableCard(
                                group = group,
                                appLanguage = appLanguage,
                                onSpeak = onSpeak,
                                currentlySpokenId = currentlySpokenId
                            )
                        }

                        // Section 2: Two-Way Prepositions (Wechselpräpositionen) - One clean unified table
                        item(
                            key = "twoway_prepositions_card",
                            contentType = "twoway_prepositions_card"
                        ) {
                            PremiumTwoWayPrepositionsTableCard(
                                items = GrammarTablesRepository.twoWayPrepositions,
                                appLanguage = appLanguage,
                                onSpeak = onSpeak,
                                currentlySpokenId = currentlySpokenId
                            )
                        }
                    } else {
                        // Pronouns Tab (Personalpronomen, Possessivartikel)
                        items(
                            items = currentCategory.tables,
                            key = { it.id },
                            contentType = { "grammar_reference_table" }
                        ) { table ->
                            PremiumGrammarReferenceTableCard(
                                table = table,
                                appLanguage = appLanguage,
                                onSpeak = onSpeak,
                                currentlySpokenId = currentlySpokenId
                            )
                        }
                    }
                } else {
                    // Pronunciation Guide Tab
                    item(key = "pronunciation_header", contentType = "guide_header") {
                        PronunciationGuideHeader(appLanguage = appLanguage)
                    }

                    items(
                        items = GrammarTablesRepository.pronunciationGuide,
                        key = { it.soundLetter },
                        contentType = { "pronunciation_rule" }
                    ) { rule ->
                        PremiumPronunciationRuleCard(
                            rule = rule,
                            appLanguage = appLanguage,
                            onSpeak = onSpeak,
                            currentlySpokenId = currentlySpokenId
                        )
                    }
                }
            }
        }
    }
}

/**
 * Robust cleaner ensuring TTS speaks strictly the single word:
 * - Never pronounces full sequences (e.g. "an, auf, hinter, in...").
 * - Never pronounces English or Arabic translations (e.g. "(my)", "at / on", "to begin / يبدأ").
 * - Never pronounces case/category prefixes (e.g. "Akkusativ:").
 * - Strips parentheses and extracts clean German single word (e.g. "(an)statt" -> "anstatt", "1. Sg. (ich)" -> "ich").
 */
fun cleanWordForPronunciation(raw: String): String {
    var text = raw.trim()
    if (text.isBlank() || text == "-" || text == "–" || text.contains("keine Endung", ignoreCase = true) || text.contains("selten", ignoreCase = true)) return ""

    // Strip case/category prefixes like "Akkusativ:", "Dativ:", "Genitiv:", "Nominativ:"
    val colonIndex = text.indexOf(':')
    if (colonIndex != -1 && colonIndex < 25) {
        text = text.substring(colonIndex + 1).trim()
    }

    // Never pronounce comma-separated sequences! Take only the single first word
    if (text.contains(',')) {
        text = text.substringBefore(',').trim()
    }

    // Strip slash translations/alternatives (e.g. "sie / Sie" or "er / es" or "an / at" or "to begin / يبدأ")
    if (text.contains('/')) {
        text = text.substringBefore('/').trim()
    }

    // Handle parentheses:
    // e.g. "(an)statt" -> "anstatt"
    if (text.startsWith("(") && text.contains(")") && !text.endsWith(")")) {
        val inside = text.substringAfter("(").substringBefore(")")
        val remainder = text.substringAfter(")")
        text = "$inside$remainder"
    } else if (text.contains("(") && text.contains(")")) {
        val beforeParen = text.substringBefore("(").trim()
        val insideParen = text.substringAfter("(").substringBefore(")").trim()
        text = if (beforeParen.contains("Sg.") || beforeParen.contains("Pl.") || beforeParen.contains("Höflich") || beforeParen.isBlank()) {
            insideParen
        } else {
            beforeParen
        }
    }

    // If string still has multiple words, take the first word if it's a pronoun or article
    val words = text.split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (words.size > 1) {
        val firstLower = words[0].lowercase().removeSuffix(".")
        val possessiveOrArticle = setOf(
            "mein", "meine", "meinen", "meinem", "meiner", "meines",
            "dein", "deine", "deinen", "deinem", "deiner", "deines",
            "sein", "seine", "seinen", "seinem", "seiner", "seines",
            "ihr", "ihre", "ihren", "ihrem", "ihrer", "ihres",
            "unser", "unsere", "unseren", "unserem", "unserer", "unseres",
            "euer", "eure", "euren", "eurem", "eurer", "eures",
            "der", "die", "das", "den", "dem", "des",
            "ein", "eine", "einen", "einem", "einer", "eines",
            "kein", "keine", "keinen", "keinem", "keiner", "keines",
            "dieser", "diese", "dieses", "diesen", "diesem",
            "mich", "dich", "sich", "uns", "euch",
            "wer", "wen", "wem", "wessen", "was",
            "man", "jemand", "niemand", "etwas", "nichts", "alle", "viele", "wenige"
        )
        if (possessiveOrArticle.contains(firstLower)) {
            text = words[0]
        }
    }

    val cleaned = text.trim().trim('.', ',', ';', '!', '?', '"', '\'', '–', '-').trim()
    if (cleaned.isBlank() || cleaned == "-" || cleaned == "–") return ""
    return cleaned
}

/**
 * Reusable horizontal scrollable single-line title for grammar cards.
 * If title is short, displays fully without scrolling.
 * If title is long, user can smoothly scroll horizontally without wrapping or truncation.
 */
@Composable
fun ScrollableCardTitle(
    text: String,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = GeoOnSurface,
    modifier: Modifier = Modifier
) {
    if (text.isBlank()) return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Text(
            text = text,
            style = style,
            fontWeight = fontWeight,
            color = color,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Reusable horizontal scrollable single-line subtitle for grammar cards.
 * If subtitle is short, displays fully without scrolling.
 * If subtitle is long, user can smoothly scroll horizontally without wrapping or truncation.
 */
@Composable
fun ScrollableCardSubtitle(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    fontWeight: FontWeight = FontWeight.Medium,
    color: Color = GeoOnSurfaceVariant,
    modifier: Modifier = Modifier
) {
    if (text.isBlank()) return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Text(
            text = text,
            style = style,
            fontWeight = fontWeight,
            color = color,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * High-performance, ultra-lightweight pronunciation button for individual words/cell items.
 * Optimized for 144Hz displays with minimal allocation overhead.
 * Guarantees pronunciation of only the single word when isSentence = false.
 */
@Composable
fun SmallItemSpeakerButton(
    textToSpeak: String,
    speakerId: String,
    currentlySpokenId: String?,
    onSpeak: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    isSentence: Boolean = false,
    activeColor: Color = Color(0xFFD32F2F),
    idleColor: Color = GeoPrimary,
    size: Int = 22,
    iconSize: Int = 12
) {
    val cleanText = if (isSentence) textToSpeak.trim().removePrefix("•").trim() else cleanWordForPronunciation(textToSpeak)
    if (cleanText.isBlank() || cleanText == "-" || cleanText == "–") return
    val isPlaying = currentlySpokenId == speakerId

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .clickable(
                role = Role.Button,
                onClick = { onSpeak(cleanText, speakerId) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Aussprache: $cleanText",
            tint = if (isPlaying) activeColor else idleColor,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

/**
 * 1. Preposition Case Table Card:
 * - NO speaker icon on heading (Dativ, Akkusativ, Genitiv).
 * - Horizontal scrolling for card title and subtitle without wrapping.
 * - Small speaker icon next to EACH preposition item (aus, bei, mit, etc.) pronouncing ONLY the single word.
 * - Small speaker icon next to EACH example sentence.
 * - Horizontal scrolling for table, strictly single-line, NO text wrapping, dynamic column widths.
 */
@Composable
fun PremiumPrepositionCaseTableCard(
    group: PrepositionGroup,
    appLanguage: String,
    onSpeak: (String, String) -> Unit,
    currentlySpokenId: String?,
    modifier: Modifier = Modifier
) {
    val kasusAccentColor = when (group.kasusId) {
        "akkusativ" -> Color(0xFFE53935)
        "dativ" -> Color(0xFF1E88E5)
        "genitiv" -> Color(0xFF8E24AA)
        else -> GeoPrimary
    }

    val prepHeader = when (appLanguage.lowercase()) {
        "en" -> "Preposition"
        "ar" -> "حرف الجر"
        else -> "Präposition"
    }
    val meaningHeader = when (appLanguage.lowercase()) {
        "en" -> "Meaning"
        "ar" -> "المعنى"
        else -> "Bedeutung"
    }
    val exampleHeader = when (appLanguage.lowercase()) {
        "en" -> "Example Sentence"
        "ar" -> "مثال توضيحي"
        else -> "Beispiel"
    }

    val prepColWidth = remember(group, prepHeader) {
        val maxLen = maxOf(prepHeader.length, group.prepositions.maxOfOrNull { it.word.length } ?: 8)
        maxOf(130.dp, (maxLen * 10 + 48).dp)
    }
    val meaningColWidth = remember(group, appLanguage, meaningHeader) {
        val maxLen = maxOf(meaningHeader.length, group.prepositions.maxOfOrNull { it.localizedMeaning(appLanguage).length } ?: 12)
        maxOf(160.dp, (maxLen * 8.5f + 32).dp)
    }
    val exampleColWidth = remember(group, exampleHeader) {
        val maxLen = maxOf(exampleHeader.length, group.prepositions.maxOfOrNull { it.exampleDe.length } ?: 25)
        maxOf(320.dp, (maxLen * 8.5f + 56).dp)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (GeoIsBoldTheme) 1.5.dp else 1.dp,
            kasusAccentColor.copy(alpha = 0.4f)
        ),
        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Heading: Scrollable single line title & subtitle, NO text wrapping, NO speaker icon on heading
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                ScrollableCardTitle(
                    text = group.localizedKasusTitle(appLanguage),
                    color = kasusAccentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                ScrollableCardSubtitle(
                    text = group.localizedQuestion(appLanguage)
                )
            }

            // Merkhilfe / Mnemonic callout bar (Clean, single container with horizontal scroll)
            val mnemonic = group.localizedMnemonic(appLanguage)
            if (mnemonic.isNotBlank()) {
                val mnemonicScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(kasusAccentColor.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .horizontalScroll(mnemonicScrollState),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = kasusAccentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = mnemonic,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoOnSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // 2 & 3. The Clean Table: Small speaker icon per item + Horizontal Scroll + Single Line (No wrapping)
            val tableScrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(tableScrollState)
                    .padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, GeoBorder, RoundedCornerShape(10.dp))
                        .background(GeoSurface)
                ) {
                    Column {
                        // Table Header Row (Single line, no wrap)
                        Row(
                            modifier = Modifier
                                .background(kasusAccentColor.copy(alpha = 0.12f))
                                .padding(vertical = 9.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prepHeader,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = kasusAccentColor,
                                modifier = Modifier.width(prepColWidth),
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = meaningHeader,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = kasusAccentColor,
                                modifier = Modifier.width(meaningColWidth),
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = exampleHeader,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = kasusAccentColor,
                                modifier = Modifier.width(exampleColWidth),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        HorizontalDivider(color = GeoBorder)

                        // Data Rows: Small speaker icon next to EACH preposition and next to EACH example!
                        group.prepositions.forEachIndexed { index, prep ->
                            val rowBg = if (index % 2 == 1) GeoSurfaceVariant.copy(alpha = 0.35f) else GeoSurface

                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .padding(vertical = 7.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Preposition Word + Small Speaker Icon (Pronounces ONLY the single word)
                                Row(
                                    modifier = Modifier.width(prepColWidth),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = prep.word,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GeoOnSurface,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    SmallItemSpeakerButton(
                                        textToSpeak = prep.word,
                                        speakerId = "prep_${group.kasusId}_${prep.word}",
                                        currentlySpokenId = currentlySpokenId,
                                        onSpeak = onSpeak,
                                        isSentence = false,
                                        idleColor = kasusAccentColor
                                    )
                                }

                                // Meaning
                                Text(
                                    text = prep.localizedMeaning(appLanguage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GeoOnSurfaceVariant,
                                    modifier = Modifier.width(meaningColWidth),
                                    maxLines = 1,
                                    softWrap = false
                                )

                                // Example Sentence + Small Speaker Icon (Pronounces example sentence)
                                Row(
                                    modifier = Modifier.width(exampleColWidth),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = prep.exampleDe,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = GeoOnSurface,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (prep.exampleDe.isNotBlank()) {
                                        SmallItemSpeakerButton(
                                            textToSpeak = prep.exampleDe,
                                            speakerId = "ex_${group.kasusId}_${prep.word}",
                                            currentlySpokenId = currentlySpokenId,
                                            onSpeak = onSpeak,
                                            isSentence = true,
                                            idleColor = kasusAccentColor
                                        )
                                    }
                                }
                            }

                            if (index < group.prepositions.size - 1) {
                                HorizontalDivider(color = GeoBorder.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. Two-Way Prepositions Table Card (Wechselpräpositionen)
 * - NO speaker icon on heading.
 * - Horizontal scrolling for card title and subtitle without wrapping.
 * - Small speaker icon next to EACH preposition (an, auf, hinter, in, etc.) pronouncing ONLY the single word itself.
 * - Small speaker icon next to EACH Akkusativ example and EACH Dativ example.
 * - Horizontal scrolling, strictly single-line, NO text wrapping, dynamic column widths.
 */
@Composable
fun PremiumTwoWayPrepositionsTableCard(
    items: List<TwoWayPrepositionItem>,
    appLanguage: String,
    onSpeak: (String, String) -> Unit,
    currentlySpokenId: String?,
    modifier: Modifier = Modifier
) {
    val accentColor = GeoPrimary

    val prepHeader = when (appLanguage.lowercase()) {
        "en" -> "Preposition"
        "ar" -> "حرف الجر"
        else -> "Präposition"
    }
    val meaningHeader = when (appLanguage.lowercase()) {
        "en" -> "Meaning"
        "ar" -> "المعنى"
        else -> "Bedeutung"
    }
    val akkHeader = when (appLanguage.lowercase()) {
        "en" -> "Akkusativ (Where to?)"
        "ar" -> "Akkusativ (إلى أين؟)"
        else -> "Akkusativ (Wohin?)"
    }
    val datHeader = when (appLanguage.lowercase()) {
        "en" -> "Dativ (Where?)"
        "ar" -> "Dativ (أين؟)"
        else -> "Dativ (Wo?)"
    }

    val prepColWidth = remember(items, prepHeader) {
        val maxLen = maxOf(prepHeader.length, items.maxOfOrNull { it.word.length } ?: 8)
        maxOf(105.dp, (maxLen * 9.5f + 42).dp)
    }
    val meaningColWidth = remember(items, appLanguage, meaningHeader) {
        val maxLen = maxOf(meaningHeader.length, items.maxOfOrNull { it.localizedMeaning(appLanguage).length } ?: 10)
        maxOf(115.dp, (maxLen * 8f + 26).dp)
    }
    val akkColWidth = remember(items, akkHeader) {
        val maxLen = maxOf(akkHeader.length, items.maxOfOrNull { it.akkusativGerman.length } ?: 22)
        maxOf(240.dp, (maxLen * 8f + 48).dp)
    }
    val datColWidth = remember(items, datHeader) {
        val maxLen = maxOf(datHeader.length, items.maxOfOrNull { it.dativGerman.length } ?: 22)
        maxOf(240.dp, (maxLen * 8f + 48).dp)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (GeoIsBoldTheme) 1.5.dp else 1.dp,
            accentColor.copy(alpha = 0.4f)
        ),
        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Heading: Scrollable single line title & subtitle, NO text wrapping, NO speaker icon on heading
            Column(modifier = Modifier.fillMaxWidth()) {
                ScrollableCardTitle(
                    text = when (appLanguage.lowercase()) {
                        "en" -> "Wechselpräpositionen"
                        "ar" -> "حروف الجر المشتركة (Wechselpräpositionen)"
                        else -> "Wechselpräpositionen"
                    },
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                ScrollableCardSubtitle(
                    text = when (appLanguage.lowercase()) {
                        "en" -> "Wohin? → Akkusativ  |  Wo? → Dativ"
                        "ar" -> "Wohin? (حركة) → Akkusativ  |  Wo? (سكون) → Dativ"
                        else -> "Wohin? → Akkusativ  |  Wo? → Dativ"
                    }
                )
            }

            // 2 & 3. The Matrix Table with Horizontal Scroll & Single Line
            val tableScrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(tableScrollState)
                    .padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, GeoBorder, RoundedCornerShape(10.dp))
                        .background(GeoSurface)
                ) {
                    Column {
                        // Header Row (Single line, no wrap)
                        Row(
                            modifier = Modifier
                                .background(accentColor.copy(alpha = 0.12f))
                                .padding(vertical = 9.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prepHeader,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier.width(prepColWidth),
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = meaningHeader,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier.width(meaningColWidth),
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = akkHeader,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE53935),
                                modifier = Modifier.width(akkColWidth),
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = datHeader,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E88E5),
                                modifier = Modifier.width(datColWidth),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        HorizontalDivider(color = GeoBorder)

                        // Data Rows: Small speaker icon next to EACH preposition (pronouncing ONLY the word itself)
                        // and next to Akkusativ and Dativ examples (pronouncing the example sentence)
                        items.forEachIndexed { index, item ->
                            val rowBg = if (index % 2 == 1) GeoSurfaceVariant.copy(alpha = 0.35f) else GeoSurface

                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .padding(vertical = 7.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Preposition Word + Small Speaker Icon (Pronounces ONLY the single word e.g. "an")
                                Row(
                                    modifier = Modifier.width(prepColWidth),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = item.word,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GeoOnSurface,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    SmallItemSpeakerButton(
                                        textToSpeak = item.word,
                                        speakerId = "twoway_word_${item.word}",
                                        currentlySpokenId = currentlySpokenId,
                                        onSpeak = onSpeak,
                                        isSentence = false,
                                        idleColor = accentColor
                                    )
                                }

                                // Meaning
                                Text(
                                    text = item.localizedMeaning(appLanguage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GeoOnSurfaceVariant,
                                    modifier = Modifier.width(meaningColWidth),
                                    maxLines = 1,
                                    softWrap = false
                                )

                                // Akkusativ Example Cell + Small Speaker
                                Row(
                                    modifier = Modifier.width(akkColWidth),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = item.akkusativGerman,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = GeoOnSurface,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    SmallItemSpeakerButton(
                                        textToSpeak = item.akkusativGerman,
                                        speakerId = "twoway_akk_${item.word}",
                                        currentlySpokenId = currentlySpokenId,
                                        onSpeak = onSpeak,
                                        isSentence = true,
                                        idleColor = Color(0xFFE53935)
                                    )
                                }

                                // Dativ Example Cell + Small Speaker
                                Row(
                                    modifier = Modifier.width(datColWidth),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = item.dativGerman,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = GeoOnSurface,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    SmallItemSpeakerButton(
                                        textToSpeak = item.dativGerman,
                                        speakerId = "twoway_dat_${item.word}",
                                        currentlySpokenId = currentlySpokenId,
                                        onSpeak = onSpeak,
                                        isSentence = true,
                                        idleColor = Color(0xFF1E88E5)
                                    )
                                }
                            }

                            if (index < items.size - 1) {
                                HorizontalDivider(color = GeoBorder.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean Examples List Component
 * Each example on two lines: German (bold) + English/Arabic (gray) + Small speaker on the right.
 */
@Composable
fun CleanExamplesList(
    examples: List<GrammarExample>,
    appLanguage: String,
    kasusAccentColor: Color,
    onSpeak: (String, String) -> Unit,
    currentlySpokenId: String?,
    idPrefix: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ScrollableCardSubtitle(
            text = when (appLanguage.lowercase()) {
                "en" -> "Anwendungsbeispiele:"
                "ar" -> "أمثلة تطبيقية:"
                else -> "Anwendungsbeispiele:"
            },
            fontWeight = FontWeight.Bold,
            color = GeoOnSurfaceVariant
        )

        examples.forEachIndexed { exIndex, ex ->
            val exSpeakerId = "ex_${idPrefix}_$exIndex"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "• ${ex.german}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoOnSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    val trans = ex.localizedTranslation(appLanguage)
                    if (trans.isNotBlank()) {
                        Text(
                            text = "  $trans",
                            style = MaterialTheme.typography.bodySmall,
                            color = GeoOnSurfaceVariant,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                SmallItemSpeakerButton(
                    textToSpeak = ex.german,
                    speakerId = exSpeakerId,
                    currentlySpokenId = currentlySpokenId,
                    onSpeak = onSpeak,
                    isSentence = true,
                    idleColor = kasusAccentColor,
                    size = 28,
                    iconSize = 16
                )
            }
        }
    }
}

/**
 * Multilingual Grammar Reference Table Card (Pronouns, etc.):
 * - NO speaker icon on heading.
 * - Horizontal scrolling for card title and subtitle without wrapping.
 * - Small speaker icon next to EACH individual word/cell item pronouncing ONLY the single word itself.
 * - Horizontal scrolling, strictly single-line, NO text wrapping, dynamic column widths.
 */
@Composable
fun PremiumGrammarReferenceTableCard(
    table: GrammarReferenceTable,
    appLanguage: String,
    onSpeak: (String, String) -> Unit,
    currentlySpokenId: String?,
    modifier: Modifier = Modifier
) {
    val localizedHeaders = remember(table, appLanguage) { table.localizedHeaders(appLanguage) }
    val colWidths = remember(table, appLanguage, localizedHeaders) {
        val numCols = localizedHeaders.size
        (0 until numCols).map { colIndex ->
            val headerLen = localizedHeaders.getOrNull(colIndex)?.length ?: 8
            val maxCellLen = if (colIndex == 0) {
                table.rows.maxOfOrNull { it.localizedLabel(appLanguage).length } ?: 8
            } else {
                table.rows.maxOfOrNull { it.cells.getOrNull(colIndex - 1)?.length ?: 0 } ?: 8
            }
            val maxCharLen = maxOf(headerLen, maxCellLen)
            val baseMin = if (colIndex == 0) 115.dp else 110.dp
            val computed = (maxCharLen * 9f + 44).dp
            if (computed > baseMin) computed else baseMin
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (GeoIsBoldTheme) 1.5.dp else 1.dp,
            if (GeoIsBoldTheme) GeoCardRibbonColor.copy(alpha = 0.5f) else GeoBorder
        ),
        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Table Header: Scrollable single line title & subtitle, NO text wrapping, NO title speaker icon
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                ScrollableCardTitle(
                    text = table.localizedTitle(appLanguage),
                    color = GeoOnSurface
                )
                val subtitle = table.localizedSubtitle(appLanguage)
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    ScrollableCardSubtitle(
                        text = subtitle,
                        color = GeoPrimary
                    )
                }
            }

            // 2 & 3. The Matrix Table with Horizontal Scroll & Small Speaker per Individual Cell Item
            val horizontalScrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
                    .padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, GeoBorder, RoundedCornerShape(10.dp))
                        .background(GeoSurface)
                ) {
                    Column {
                        // Header Row (Single line, no wrap)
                        Row(
                            modifier = Modifier
                                .background(GeoPrimary.copy(alpha = 0.12f))
                                .padding(vertical = 9.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            localizedHeaders.forEachIndexed { i, header ->
                                val width = colWidths.getOrElse(i) { 145.dp }
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoPrimary,
                                    modifier = Modifier
                                        .width(width)
                                        .padding(end = 8.dp),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        HorizontalDivider(color = GeoBorder)

                        // Data Rows: Small speaker icon next to EACH individual word/cell item (pronouncing ONLY the word itself)
                        table.rows.forEachIndexed { rowIndex, row ->
                            val rowBg = if (rowIndex % 2 == 1) GeoSurfaceVariant.copy(alpha = 0.35f) else GeoSurface
                            val labelWidth = colWidths.getOrElse(0) { 170.dp }

                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .padding(vertical = 7.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Row Label (e.g. ich, du, er, etc.) + Small Speaker Icon (Pronounces ONLY the single word)
                                Row(
                                    modifier = Modifier
                                        .width(labelWidth)
                                        .padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = row.localizedLabel(appLanguage),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GeoOnSurface,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    SmallItemSpeakerButton(
                                        textToSpeak = row.speakText.ifBlank { row.label },
                                        speakerId = "row_lbl_${table.id}_$rowIndex",
                                        currentlySpokenId = currentlySpokenId,
                                        onSpeak = onSpeak,
                                        isSentence = false,
                                        idleColor = GeoPrimary
                                    )
                                }

                                // Individual Cells + Small Speaker Icon beside EACH word (Pronounces ONLY the word itself)
                                row.cells.forEachIndexed { cellIndex, cell ->
                                    val cellWidth = colWidths.getOrElse(cellIndex + 1) { 145.dp }
                                    Row(
                                        modifier = Modifier
                                            .width(cellWidth)
                                            .padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = cell,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GeoOnSurface,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        val isSentenceCell = cell.contains(' ') && (cell.endsWith('.') || cell.endsWith('?') || cell.endsWith('!') || cell.split("\\s+".toRegex()).size >= 3)
                                        SmallItemSpeakerButton(
                                            textToSpeak = cell,
                                            speakerId = "cell_${table.id}_${rowIndex}_$cellIndex",
                                            currentlySpokenId = currentlySpokenId,
                                            onSpeak = onSpeak,
                                            isSentence = isSentenceCell,
                                            idleColor = GeoOutline
                                        )
                                    }
                                }
                            }

                            if (rowIndex < table.rows.size - 1) {
                                HorizontalDivider(color = GeoBorder.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // Explanatory Note (Horizontal scrollable, strictly single line)
            val localizedNotes = table.localizedNotes(appLanguage)
            if (localizedNotes.isNotBlank()) {
                val notesScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GeoPrimary.copy(alpha = 0.08f))
                        .padding(10.dp)
                        .horizontalScroll(notesScrollState),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = GeoPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = localizedNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = GeoOnSurface,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Clean Examples List with Small Speaker Icons
            if (table.examples.isNotEmpty()) {
                CleanExamplesList(
                    examples = table.examples,
                    appLanguage = appLanguage,
                    kasusAccentColor = GeoPrimary,
                    onSpeak = onSpeak,
                    currentlySpokenId = currentlySpokenId,
                    idPrefix = "table_${table.id}"
                )
            }
        }
    }
}

@Composable
fun PronunciationGuideHeader(
    appLanguage: String = "de",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GeoPrimary.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, GeoPrimary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = GeoPrimary,
                    modifier = Modifier.size(24.dp)
                )
                ScrollableCardTitle(
                    text = when (appLanguage.lowercase()) {
                        "en" -> "German Pronunciation & Phonetics"
                        "ar" -> "النطق الصوتي في اللغة الألمانية"
                        else -> "Deutsche Aussprache & Phonetik"
                    },
                    color = GeoPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = when (appLanguage.lowercase()) {
                        "en" -> "Tap the speaker icon next to any word or sound to hear its exact native pronunciation."
                        "ar" -> "اضغط على أيقونة السماعة بجانب أي كلمة للاستماع إلى النطق السليم."
                        else -> "Tippe auf das Lautsprechersymbol neben einem Wort, um die präzise Aussprache anzuhören."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = GeoOnSurface,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/**
 * Premium Pronunciation Rule Card:
 * - NO heading speaker icon.
 * - Small speaker icon next to EACH example word (pronouncing ONLY the single word itself).
 */
@Composable
fun PremiumPronunciationRuleCard(
    rule: PronunciationRule,
    appLanguage: String = "de",
    onSpeak: (String, String) -> Unit,
    currentlySpokenId: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GeoBorder),
        colors = CardDefaults.cardColors(containerColor = GeoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Sound letter + IPA (NO heading speaker icon)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GeoPrimary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = rule.soundLetter,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = rule.ipa,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GeoPrimary
                )
            }

            // Explanation (clean text with horizontal scrolling)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = rule.localizedExplanation(appLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = GeoOnSurface,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Example clickable words with horizontal scrolling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (appLanguage.lowercase()) {
                        "en" -> "Examples:"
                        "ar" -> "أمثلة:"
                        else -> "Beispiele:"
                    },
                    fontSize = 12.sp,
                    color = GeoOnSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
                rule.exampleWords.forEach { word ->
                    val wordSpeakerId = "word_${word.hashCode()}"

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GeoSurfaceVariant)
                            .border(1.dp, GeoBorder, RoundedCornerShape(8.dp))
                            .padding(start = 8.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = word,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoOnSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                        SmallItemSpeakerButton(
                            textToSpeak = word,
                            speakerId = wordSpeakerId,
                            currentlySpokenId = currentlySpokenId,
                            onSpeak = onSpeak,
                            isSentence = false,
                            idleColor = GeoPrimary
                        )
                    }
                }
            }
        }
    }
}
