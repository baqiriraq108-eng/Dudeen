package com.example.ui.components

import android.util.LruCache
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WordHistoryEntity
import com.example.data.model.extractNounQuickDetails
import com.example.data.model.extractVerbQuickDetails
import com.example.data.model.optSafeString
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.theme.LocalTranslationLanguage
import org.json.JSONObject
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.GeoBannerGradient
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoCardRibbonColor
import com.example.ui.theme.GeoFemininBg
import com.example.ui.theme.GeoFemininText
import com.example.ui.theme.GeoGlowColor
import com.example.ui.theme.GeoIsBoldTheme
import com.example.ui.theme.GeoMaskulinBg
import com.example.ui.theme.GeoMaskulinText
import com.example.ui.theme.GeoNeutrumBg
import com.example.ui.theme.GeoNeutrumText
import com.example.ui.theme.GeoOnPrimaryContainer
import com.example.ui.theme.GeoOnSecondaryContainer
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoOutline
import com.example.ui.theme.GeoPluralBg
import com.example.ui.theme.GeoPluralText
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoSecondaryContainer
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant
import com.example.ui.theme.LocalIsDarkTheme
import com.example.util.isArabicText
import com.example.util.cleanTranslationText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistoryFormatterCache {
    private val dateCache = LruCache<Long, String>(1024)
    private val shortDateCache = LruCache<Long, String>(1024)
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        val cached = dateCache.get(timestamp)
        if (cached != null) return cached
        val formatted = synchronized(dateFormat) {
            dateFormat.format(Date(timestamp))
        }
        dateCache.put(timestamp, formatted)
        return formatted
    }

    fun formatShortDate(timestamp: Long): String {
        val cached = shortDateCache.get(timestamp)
        if (cached != null) return cached
        val formatted = synchronized(shortDateFormat) {
            shortDateFormat.format(Date(timestamp))
        }
        shortDateCache.put(timestamp, formatted)
        return formatted
    }
}

internal data class HistoryBadgeInfo(
    val bg: Color,
    val text: Color,
    val label: String
)

@Composable
internal fun getBadgeInfo(isVerb: Boolean, gender: String): HistoryBadgeInfo {
    if (isVerb) return remember { HistoryBadgeInfo(Color(0xFFFEF3C7), Color(0xFFB45309), "VERB") }

    return remember(gender) {
        val g = gender.lowercase(Locale.ROOT)
        when {
            g == "wort" -> HistoryBadgeInfo(Color(0xFFFFEDD5), Color(0xFFC2410C), "Wort")
            g == "wörter" || g == "woerter" -> HistoryBadgeInfo(Color(0xFFFFEDD5), Color(0xFFC2410C), "Wörter")
            g == "nomen" -> HistoryBadgeInfo(Color(0xFFFFEDD5), Color(0xFFC2410C), "Wort")
            g == "satz" || g == "sentence" || g == "–" || g == "-" -> HistoryBadgeInfo(Color(0xFFEDE9FE), Color(0xFF5B21B6), "Satz")
            g == "maskulin" || g == "masculine" || g == "der" -> HistoryBadgeInfo(GeoMaskulinBg, GeoMaskulinText, "MASKULIN")
            g == "feminin" || g == "feminine" || g == "die" -> HistoryBadgeInfo(GeoFemininBg, GeoFemininText, "FEMININ")
            g == "neutrum" || g == "neuter" || g == "das" -> HistoryBadgeInfo(GeoNeutrumBg, GeoNeutrumText, "NEUTRUM")
            else -> HistoryBadgeInfo(GeoPluralBg, GeoPluralText, "PLURAL")
        }
    }
}

@Composable
fun HistoryHeaderRow(
    filteredCount: Int,
    hasItems: Boolean,
    onRequestClear: () -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: String = LocalAppLanguage.current
) {
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = GeoPrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = strings.historyTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GeoOnSurface,
                maxLines = 1,
                softWrap = false
            )
            if (hasItems) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GeoPrimaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$filteredCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnPrimaryContainer
                    )
                }
            }
        }

        if (hasItems) {
            TextButton(
                onClick = onRequestClear,
                modifier = Modifier.testTag("clear_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = strings.clearHistoryDesc,
                    modifier = Modifier.size(16.dp),
                    tint = GeoOnSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.clearButton,
                    style = MaterialTheme.typography.labelMedium,
                    color = GeoOnSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun HistorySearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearchTrigger: () -> Unit = {},
    onOpenSortSheet: () -> Unit = {},
    isSortActive: Boolean = false,
    onVoiceSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    appLanguage: String = LocalAppLanguage.current
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Compact search input field
        Row(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GeoSurfaceVariant)
                .border(
                    1.dp,
                    if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.5f) else GeoBorder,
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = GeoPrimary,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = strings.searchHistoryPlaceholder,
                        fontSize = 12.sp,
                        color = GeoOnSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    maxLines = 1,
                    textStyle = TextStyle(
                        fontSize = 12.sp,
                        color = GeoOnSurface
                    ),
                    cursorBrush = SolidColor(GeoPrimary),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearchTrigger()
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input_field")
                )
            }

            // Voice Search Microphone Icon inside search container
            IconButton(
                onClick = onVoiceSearchClick,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("history_voice_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = strings.voiceSearchDesc,
                    tint = GeoPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }

            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier
                        .size(22.dp)
                        .testTag("history_search_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = strings.clearSearchInputDesc,
                        tint = GeoOutline,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // Dedicated Search Action Button next to search input
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GeoPrimary)
                .clickable {
                    onSearchTrigger()
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                .testTag("history_search_action_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = strings.searchActionDesc,
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
        }

        // Sort Icon Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSortActive) GeoPrimaryContainer else GeoSurfaceVariant)
                .border(
                    1.dp,
                    if (isSortActive) GeoPrimary else if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.5f) else GeoBorder,
                    RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onOpenSortSheet)
                .testTag("history_sort_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = strings.sortActionDesc,
                tint = if (isSortActive) GeoPrimary else GeoOnSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun HistoryFilterPills(
    selectedFilterIndex: Int,
    onFilterChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: String = LocalAppLanguage.current
) {
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    val categories = remember(strings) { strings.historyCategories() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        categories.forEachIndexed { index, title ->
            val isSelected = selectedFilterIndex == index
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GeoSurfaceVariant)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) GeoPrimary else GeoBorder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onFilterChanged(index) }
                    .padding(horizontal = 11.dp, vertical = 4.dp)
                    .testTag("history_filter_$index"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) GeoPrimary else GeoOnSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun HistoryEmptyCard(
    searchQuery: String,
    onResetSearch: () -> Unit = {},
    modifier: Modifier = Modifier,
    appLanguage: String = LocalAppLanguage.current
) {
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, GeoBorder),
        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = GeoOutline
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (searchQuery.isNotBlank()) strings.noMatchesTitle else strings.noHistoryTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = GeoOnSurface,
                fontSize = 13.sp,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (searchQuery.isNotBlank()) {
                    strings.noMatchesSubtitle(searchQuery)
                } else {
                    strings.noHistorySubtitle
                },
                style = MaterialTheme.typography.bodySmall,
                color = GeoOnSurfaceVariant,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (searchQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onResetSearch,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GeoPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("reset_history_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = GeoPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.resetSearchButton,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun ClearHistoryConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    appLanguage: String = LocalAppLanguage.current
) {
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GeoSurface,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                strings.clearHistoryConfirmTitle,
                fontWeight = FontWeight.Bold,
                color = GeoOnSurface
            )
        },
        text = {
            Text(
                strings.clearHistoryConfirmText,
                color = GeoOnSurfaceVariant
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GeoBorder)
                ) {
                    Text(
                        text = strings.cancel,
                        color = GeoOnSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("confirm_clear_history"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary)
                ) {
                    Text(
                        text = strings.confirm,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    )
}

@Composable
fun DeleteHistoryItemConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    appLanguage: String = LocalAppLanguage.current
) {
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GeoSurface,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                strings.deleteEntryDialogTitle,
                fontWeight = FontWeight.Bold,
                color = GeoOnSurface
            )
        },
        text = {
            Text(
                strings.deleteEntryDialogMessage,
                color = GeoOnSurfaceVariant
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GeoBorder)
                ) {
                    Text(
                        text = strings.cancel,
                        color = GeoOnSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("confirm_delete_history_item"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary)
                ) {
                    Text(
                        text = strings.confirm,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    )
}

fun LazyListScope.historySectionItems(
    historyList: List<WordHistoryEntity>,
    filteredList: List<WordHistoryEntity>,
    selectedFilterIndex: Int,
    historySearchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterChanged: (Int) -> Unit,
    onSelectWord: (WordHistoryEntity) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onRequestClearAll: () -> Unit,
    onOpenSortSheet: () -> Unit = {},
    isSortActive: Boolean = false,
    onSpeak: (String, String) -> Unit,
    currentlySpokenText: String? = null,
    currentlySpokenId: String? = null,
    onVoiceSearchClick: () -> Unit = {}
) {
    item(key = "history_header_row") {
        HistoryHeaderRow(
            filteredCount = filteredList.size,
            hasItems = historyList.isNotEmpty(),
            onRequestClear = onRequestClearAll
        )
    }

    item(key = "history_search_box") {
        HistorySearchBar(
            searchQuery = historySearchQuery,
            onQueryChange = onSearchQueryChange,
            onSearchTrigger = {},
            onOpenSortSheet = onOpenSortSheet,
            isSortActive = isSortActive,
            onVoiceSearchClick = onVoiceSearchClick
        )
    }

    if (historyList.isNotEmpty()) {
        item(key = "history_filter_pills") {
            HistoryFilterPills(
                selectedFilterIndex = selectedFilterIndex,
                onFilterChanged = onFilterChanged
            )
        }
    }

    if (filteredList.isEmpty()) {
        item(key = "history_empty_card") {
            HistoryEmptyCard(
                searchQuery = historySearchQuery,
                onResetSearch = { onSearchQueryChange("") }
            )
        }
    } else {
        val cleanFilteredList = filteredList.mapNotNull { item ->
            val sanitized = com.example.data.model.sanitizeHistoryWord(item.word, item.data.ifBlank { item.rawJsonResult }, item.type)
            if (sanitized.isBlank() || sanitized.equals("null", ignoreCase = true)) null
            else {
                var repaired = item
                if (sanitized != item.word) repaired = repaired.copy(word = sanitized)
                if (repaired.genderArticle.equals("null", ignoreCase = true)) repaired = repaired.copy(genderArticle = "")
                if (repaired.gender.equals("null", ignoreCase = true)) repaired = repaired.copy(gender = "")
                repaired
            }
        }
        items(
            items = cleanFilteredList,
            key = { it.id },
            contentType = { "history_item_card" }
        ) { item ->
            HistoryItemCard(
                item = item,
                onClick = { onSelectWord(item) },
                onDelete = { onDeleteItem(item.id) },
                onSpeak = onSpeak,
                currentlySpokenText = currentlySpokenText,
                currentlySpokenId = currentlySpokenId,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun HistorySection(
    historyList: List<WordHistoryEntity>,
    onSelectWord: (WordHistoryEntity) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearAll: () -> Unit,
    onSpeak: (String, String) -> Unit = { _, _ -> },
    onExportAll: () -> Unit = {},
    selectedFilterIndex: Int = 0,
    onFilterChanged: (Int) -> Unit = {},
    currentlySpokenText: String? = null,
    currentlySpokenId: String? = null,
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var itemToDeleteId by remember { mutableStateOf<Long?>(null) }
    var historySearchQuery by rememberSaveable { mutableStateOf("") }
    var isSortSheetOpen by rememberSaveable { mutableStateOf(false) }
    var sortOptionsMap by rememberSaveable {
        mutableStateOf(
            mapOf(
                0 to com.example.util.HistorySortOption(),
                1 to com.example.util.HistorySortOption(),
                2 to com.example.util.HistorySortOption(),
                3 to com.example.util.HistorySortOption(),
                4 to com.example.util.HistorySortOption()
            )
        )
    }

    val currentSortOption = sortOptionsMap[selectedFilterIndex] ?: com.example.util.HistorySortOption()
    val isSortActive = !currentSortOption.isDefault

    BackHandler(enabled = showClearConfirmDialog || itemToDeleteId != null || isSortSheetOpen) {
        if (isSortSheetOpen) {
            isSortSheetOpen = false
        } else {
            showClearConfirmDialog = false
            itemToDeleteId = null
        }
    }

    val filteredList by produceState(
        initialValue = historyList,
        keys = arrayOf(historyList, selectedFilterIndex, historySearchQuery, currentSortOption)
    ) {
        withContext(Dispatchers.Default) {
            value = com.example.util.HistoryFilterHelper.filterAndSortHistory(
                items = historyList,
                filterIndex = selectedFilterIndex,
                query = historySearchQuery,
                sortOption = currentSortOption
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        HistoryHeaderRow(
            filteredCount = filteredList.size,
            hasItems = historyList.isNotEmpty(),
            onRequestClear = { showClearConfirmDialog = true }
        )

        HistorySearchBar(
            searchQuery = historySearchQuery,
            onQueryChange = { historySearchQuery = it },
            onOpenSortSheet = { isSortSheetOpen = true },
            isSortActive = isSortActive
        )

        if (historyList.isNotEmpty()) {
            HistoryFilterPills(
                selectedFilterIndex = selectedFilterIndex,
                onFilterChanged = onFilterChanged
            )
        }

        if (filteredList.isEmpty()) {
            HistoryEmptyCard(
                searchQuery = historySearchQuery,
                onResetSearch = { historySearchQuery = "" }
            )
        } else {
            val cleanFilteredList = remember(filteredList) {
                filteredList.mapNotNull { item ->
                    val sanitized = com.example.data.model.sanitizeHistoryWord(item.word, item.data.ifBlank { item.rawJsonResult }, item.type)
                    if (sanitized.isBlank() || sanitized.equals("null", ignoreCase = true)) null
                    else {
                        var repaired = item
                        if (sanitized != item.word) repaired = repaired.copy(word = sanitized)
                        if (repaired.genderArticle.equals("null", ignoreCase = true)) repaired = repaired.copy(genderArticle = "")
                        if (repaired.gender.equals("null", ignoreCase = true)) repaired = repaired.copy(gender = "")
                        repaired
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                cleanFilteredList.forEach { item ->
                    key(item.id) {
                        HistoryItemCard(
                            item = item,
                            onClick = { onSelectWord(item) },
                            onDelete = { itemToDeleteId = item.id },
                            onSpeak = onSpeak,
                            currentlySpokenText = currentlySpokenText,
                            currentlySpokenId = currentlySpokenId
                        )
                    }
                }
            }
        }
    }

    if (isSortSheetOpen) {
        HistorySortBottomSheet(
            currentSortOption = currentSortOption,
            selectedCategoryIndex = selectedFilterIndex,
            onSortOptionChanged = { newOption ->
                val newMap = sortOptionsMap.toMutableMap()
                newMap[selectedFilterIndex] = newOption
                sortOptionsMap = newMap
            },
            onDismissRequest = { isSortSheetOpen = false }
        )
    }

    if (showClearConfirmDialog) {
        ClearHistoryConfirmDialog(
            onConfirm = {
                onClearAll()
                showClearConfirmDialog = false
            },
            onDismiss = { showClearConfirmDialog = false }
        )
    }

    itemToDeleteId?.let { id ->
        DeleteHistoryItemConfirmDialog(
            onConfirm = {
                onDeleteItem(id)
                itemToDeleteId = null
            },
            onDismiss = { itemToDeleteId = null }
        )
    }
}

@Composable
fun HistoryItemCard(
    item: WordHistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSpeak: (String, String) -> Unit = { _, _ -> },
    currentlySpokenText: String? = null,
    currentlySpokenId: String? = null,
    modifier: Modifier = Modifier
) {
    val isVerb = remember(item.type, item.rawJsonResult) {
        com.example.util.HistoryFilterHelper.isVerbItem(item)
    }
    val isAdjektiv = remember(item.type, item.gender, item.rawJsonResult) {
        com.example.util.HistoryFilterHelper.isAdjektivItem(item)
    }
    val isNomenList = remember(item.type, item.gender, item.rawJsonResult) {
        com.example.util.HistoryFilterHelper.isNomenListItem(item)
    }
    val isSentence = remember(item.type, item.gender, item.word, item.rawJsonResult) {
        com.example.util.HistoryFilterHelper.isSentenceItem(item)
    }

    if (isAdjektiv) {
        AdjektivHistoryCard(
            item = item,
            onClick = onClick,
            onDelete = onDelete,
            onSpeak = onSpeak,
            currentlySpokenId = currentlySpokenId,
            modifier = modifier
        )
        return
    }
    val badge = getBadgeInfo(isVerb, item.gender)
    val formattedDate = remember(item.timestamp) { HistoryFormatterCache.formatDate(item.timestamp) }

    val verbDetails = remember(item, isVerb) {
        if (isVerb) extractVerbQuickDetails(item) else null
    }

    val nounDetails = remember(item, isVerb, isSentence, isNomenList) {
        if (!isVerb && !isSentence && !isNomenList) extractNounQuickDetails(item) else null
    }

    val pluralText = remember(item.rawJsonResult, isVerb, isSentence, isNomenList) {
        if (isVerb || isSentence || isNomenList) "" else extractPluralForm(item.rawJsonResult)
    }

    val singularDisplay = remember(isVerb, isSentence, isNomenList, item.genderArticle, item.word) {
        val cleanWord = item.word.trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
        val cleanArt = item.genderArticle.trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
        if (!isVerb && !isSentence && !isNomenList && cleanArt.isNotBlank()) {
            if (cleanWord.startsWith("$cleanArt ", ignoreCase = true)) cleanWord else "$cleanArt $cleanWord"
        } else {
            cleanWord
        }
    }

    if (isSentence || isNomenList) {
        val sentenceScrollState = rememberScrollState()
        val translationScrollState = rememberScrollState()
        val shortDate = remember(item.timestamp) {
            HistoryFormatterCache.formatShortDate(item.timestamp)
        }
        val contentTextColor = GeoOnSurface
        val isDark = LocalIsDarkTheme.current

        val isArabic = remember(item) {
            item.translationLanguage.equals("ar", ignoreCase = true) || isArabicText(item.meaningEnglish)
        }
        val prefix = if (isArabic) "الترجمة العربية : " else "Eng.: "
        val greenColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)

        val rawAiTranslation = remember(item.meaningEnglish, item.rawJsonResult, isArabic) {
            var raw = item.meaningEnglish.trim()
            if (raw.isBlank() && item.rawJsonResult.isNotBlank()) {
                try {
                    val obj = JSONObject(item.rawJsonResult)
                    raw = obj.optString("meaningEnglish", obj.optString("english", obj.optString("meaning", ""))).trim()
                } catch (_: Exception) {}
            }
            cleanTranslationText(raw, isArabic)
        }

        val translationAnnotated = remember(rawAiTranslation, prefix, greenColor, contentTextColor) {
            buildAnnotatedString {
                withStyle(SpanStyle(color = greenColor, fontWeight = FontWeight.Bold)) {
                    append(prefix)
                }
                withStyle(SpanStyle(color = contentTextColor, fontWeight = FontWeight.Normal)) {
                    append(if (rawAiTranslation.isNotBlank()) rawAiTranslation else "—")
                }
            }
        }

        val altTranslationAnnotated = remember(item.manualTranslation, isDark, contentTextColor) {
            if (item.manualTranslation.isBlank()) null
            else {
                val isAltArabic = isArabicText(item.manualTranslation)
                val cleanManual = cleanTranslationText(item.manualTranslation.trim(), isAltArabic)
                val altPrefixColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
                buildAnnotatedString {
                    withStyle(SpanStyle(color = altPrefixColor, fontWeight = FontWeight.Bold)) {
                        append("Alt.: ")
                    }
                    withStyle(SpanStyle(color = contentTextColor, fontWeight = FontWeight.Normal)) {
                        append(cleanManual)
                    }
                }
            }
        }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer { clip = false }
                .clickable(onClick = onClick)
                .testTag("history_item_${item.id}"),
            shape = RoundedCornerShape(12.dp),
            border = if (GeoIsBoldTheme) {
                BorderStroke(1.5.dp, GeoCardRibbonColor.copy(alpha = 0.55f))
            } else {
                BorderStroke(1.dp, GeoBorder.copy(alpha = 0.6f))
            },
            colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
        ) {
            if (GeoIsBoldTheme) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .background(Brush.horizontalGradient(GeoBannerGradient))
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 10.dp)
            ) {
                // Header: Original sentence with horizontal scrolling and action icons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(sentenceScrollState),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = item.word,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (GeoIsBoldTheme) GeoPrimary else if (LocalIsDarkTheme.current) MaterialTheme.colorScheme.primary else Color(0xFF1E3A8A)
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CopyResultButton(
                            textToCopy = item.word,
                            size = 28.dp,
                            iconSize = 16.dp,
                            testTag = "copy_history_sentence_${item.id}"
                        )

                        val sentenceSpeakerId = "history_sentence_${item.id}"
                        val isPlayingSentence = currentlySpokenId != null && currentlySpokenId == sentenceSpeakerId
                        IconButton(
                            onClick = { onSpeak(item.word, sentenceSpeakerId) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("pronounce_history_${item.id}")
                        ) {
                            Icon(
                                imageVector = if (isPlayingSentence) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isPlayingSentence) "Stoppen" else "Satz anhören",
                                tint = if (isPlayingSentence) MaterialTheme.colorScheme.error else GeoOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("delete_history_item_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eintrag löschen",
                                tint = GeoOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Divider
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.35f) else GeoBorder.copy(alpha = 0.5f))
                )

                // Row 2: Translation - horizontally scrollable, strictly no wrapping or truncation
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .horizontalScroll(translationScrollState),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = translationAnnotated,
                        fontSize = 14.sp,
                        softWrap = false
                    )
                }

                if (item.manualTranslation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    ManualTranslationDisplay(
                        manualTranslation = item.manualTranslation,
                        isDark = isDark,
                        fontSize = 13.5.sp,
                        textColor = contentTextColor,
                        testTag = "history_manual_translation_${item.id}"
                    )
                }

                // Divider
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.35f) else GeoBorder.copy(alpha = 0.5f))
                )

                // Row 3: Date with Calendar Emoji 📅 (Bottom-Left) + Type Badge (Bottom-Right)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Date with emoji: 📅 15.09.2026
                    Text(
                        text = "📅 $shortDate",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GeoOnSurfaceVariant
                        ),
                        maxLines = 1,
                        softWrap = false
                    )

                    // 2. Type badge (Satz / Wort / Wörter) - Pill-style matching noun badges
                    val badgeText = if (isSentence) {
                        "Satz"
                    } else {
                        val isMultiple = item.gender.equals("Wörter", ignoreCase = true) ||
                                (!item.gender.equals("Wort", ignoreCase = true) && com.example.data.model.isMultipleWordsInput(item.word))
                        if (isMultiple) "Wörter" else "Wort"
                    }

                    NounGenderBadge(
                        gender = badgeText,
                        modifier = Modifier.testTag(if (isSentence) "sentence_type_badge_${item.id}" else "word_type_badge_${item.id}")
                    )
                }
            }
        }
    } else if (isVerb) {
        // 🎯 Unified Card Design for Verbs (Dynamic Theming)
        // Fixed/flexible min 160dp height, unified dynamic GeoSurfaceVariant container, 12dp start/end padding, 8dp top padding, 14dp bottom padding
        // 4 clearly separated rows with 1dp dynamic dividers (GeoBorder)
        Card(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp)
                .graphicsLayer { clip = false }
                .clickable(onClick = onClick)
                .testTag("history_item_${item.id}"),
            shape = RoundedCornerShape(12.dp),
            border = if (GeoIsBoldTheme) {
                BorderStroke(1.5.dp, GeoCardRibbonColor.copy(alpha = 0.55f))
            } else {
                BorderStroke(1.dp, GeoBorder.copy(alpha = 0.6f))
            },
            colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
        ) {
            if (GeoIsBoldTheme) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .background(Brush.horizontalGradient(GeoBannerGradient))
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 14.dp)
            ) {
                // Element 1: Header (word) - 32dp height, bold font, distinctive color
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .horizontalScroll(rememberScrollState()),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = item.word,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (GeoIsBoldTheme) GeoPrimary else if (LocalIsDarkTheme.current) MaterialTheme.colorScheme.primary else Color(0xFF1E3A8A)
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Quick action icons (speak and delete) on top right
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CopyResultButton(
                            textToCopy = item.word,
                            size = 28.dp,
                            iconSize = 16.dp,
                            testTag = "copy_history_verb_${item.id}"
                        )

                        val verbSpeakerId = "history_verb_${item.id}"
                        val isPlayingVerb = currentlySpokenId != null && currentlySpokenId == verbSpeakerId
                        IconButton(
                            onClick = { onSpeak(item.word, verbSpeakerId) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("pronounce_history_${item.id}")
                        ) {
                            Icon(
                                imageVector = if (isPlayingVerb) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isPlayingVerb) "Stoppen" else "Wort anhören",
                                tint = if (isPlayingVerb) MaterialTheme.colorScheme.error else GeoOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("delete_history_item_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eintrag löschen",
                                tint = GeoOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Thin horizontal divider: 2dp top margin, 1dp height
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.35f) else GeoBorder.copy(alpha = 0.5f))
                )

                // Element 2: Conjugations row (German) - 40dp height, scrollable horizontally
                Spacer(modifier = Modifier.height(4.dp))
                val conjugationsScrollState = rememberScrollState()

                val aux = remember(verbDetails?.auxiliary) {
                    val raw = verbDetails?.auxiliary.orEmpty().trim()
                        .removePrefix("Hilfsv.:").removePrefix("Hilfsv:").removePrefix("HV.:").removePrefix("HV:")
                        .removePrefix("Hilfsverb:").removePrefix("Hilfsverb")
                        .trim()
                        .removeSurrounding("(", ")")
                        .trim()
                    when {
                        raw.equals("haben", ignoreCase = true) -> "hat"
                        raw.equals("sein", ignoreCase = true) -> "ist"
                        else -> raw
                    }
                }

                val p1 = remember(verbDetails?.partizip1, item.word) {
                    val raw = verbDetails?.partizip1.orEmpty().trim()
                        .removePrefix("Part I:").removePrefix("Part I").removePrefix("Partizip I:").removePrefix("Partizip I")
                        .trim()
                    if (raw.isBlank() && item.word.isNotBlank()) {
                        if (item.word.endsWith("d", ignoreCase = true)) item.word else "${item.word}d"
                    } else {
                        raw
                    }
                }

                val p2 = remember(verbDetails?.partizip2) {
                    verbDetails?.partizip2.orEmpty().trim()
                        .removePrefix("Part II:").removePrefix("Part II").removePrefix("Partizip II:").removePrefix("Partizip II")
                        .trim()
                }

                val contentTextColor = GeoOnSurface

                val conjugationsAnnotated = remember(aux, p1, p2, contentTextColor) {
                    val normalColor = contentTextColor
                    val blueColor = Color(0xFF1565C0)
                    val orangeColor = Color(0xFFE65100)

                    val parts = mutableListOf<Triple<String, String, Color>>()
                    if (aux.isNotBlank()) {
                        parts.add(Triple("Hilfsv.:", aux, blueColor))
                    }
                    if (p1.isNotBlank()) {
                        parts.add(Triple("Part I:", p1, orangeColor))
                    }
                    if (p2.isNotBlank()) {
                        parts.add(Triple("Part II:", p2, orangeColor))
                    }

                    if (parts.isEmpty()) return@remember null

                    buildAnnotatedString {
                        parts.forEachIndexed { index, (indicator, value, indColor) ->
                            if (index > 0) {
                                withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                                    append(" · ")
                                }
                            }
                            withStyle(SpanStyle(color = indColor, fontWeight = FontWeight.Bold)) {
                                append(indicator)
                            }
                            withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                                append(" $value")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .horizontalScroll(conjugationsScrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (conjugationsAnnotated != null) {
                        Text(
                            text = conjugationsAnnotated,
                            fontSize = 14.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    } else {
                        Text(
                            text = "—",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = contentTextColor
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Thin horizontal divider: 2dp top margin, 1dp height
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(GeoBorder.copy(alpha = 0.5f))
                )

                // Element 3: Translation row (English / Arabic) - scrollable horizontally
                Spacer(modifier = Modifier.height(4.dp))
                val rawVerbAiMeaning = verbDetails?.englishMeaning?.takeIf { it.isNotBlank() } ?: item.meaningEnglish.trim()
                val isArabic = remember(item, rawVerbAiMeaning) {
                    item.translationLanguage.equals("ar", ignoreCase = true) || isArabicText(rawVerbAiMeaning)
                }
                val prefix = if (isArabic) "الترجمة العربية : " else "Eng.: "
                val isDarkTheme = LocalIsDarkTheme.current
                val greenColor = if (isDarkTheme) Color(0xFF81C784) else Color(0xFF2E7D32)
                val translationScrollState = rememberScrollState()
                val translationText = remember(rawVerbAiMeaning, isArabic) {
                    val clean = cleanTranslationText(rawVerbAiMeaning, isArabic)
                    if (clean.contains(",")) {
                        clean.split(",").map { it.trim() }.filter { it.isNotBlank() }.joinToString(" · ")
                    } else {
                        clean
                    }
                }

                val translationAnnotated = remember(translationText, prefix, contentTextColor, greenColor) {
                    val normalColor = contentTextColor
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = greenColor, fontWeight = FontWeight.Bold)) {
                            append(prefix)
                        }
                        withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                            append(if (translationText.isNotBlank()) translationText else "—")
                        }
                    }
                }

                val altTranslationAnnotated = remember(item.manualTranslation, isDarkTheme, contentTextColor) {
                    if (item.manualTranslation.isBlank()) null
                    else {
                        val isAltArabic = isArabicText(item.manualTranslation)
                        val cleanManual = cleanTranslationText(item.manualTranslation.trim(), isAltArabic)
                        val altPrefixColor = if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFFD97706)
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = altPrefixColor, fontWeight = FontWeight.Bold)) {
                                append("Alt.: ")
                            }
                            withStyle(SpanStyle(color = contentTextColor, fontWeight = FontWeight.Normal)) {
                                append(cleanManual)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .horizontalScroll(translationScrollState),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = translationAnnotated,
                        fontSize = 14.sp,
                        softWrap = false
                    )
                }

                if (item.manualTranslation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    ManualTranslationDisplay(
                        manualTranslation = item.manualTranslation,
                        isDark = isDarkTheme,
                        fontSize = 13.5.sp,
                        textColor = contentTextColor,
                        testTag = "verb_history_manual_translation_${item.id}"
                    )
                }

                // Thin horizontal divider: 2dp top margin, 1dp height
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.35f) else GeoBorder.copy(alpha = 0.5f))
                )

                // Element 3: Divider
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.35f) else GeoBorder.copy(alpha = 0.5f))
                )

                // Element 5: Metadata row - 28dp height
                Spacer(modifier = Modifier.height(4.dp))
                val shortDate = remember(item.timestamp) {
                    HistoryFormatterCache.formatShortDate(item.timestamp)
                }
                val praetValue = remember(verbDetails) {
                    if (verbDetails == null) return@remember ""
                    val form = verbDetails.praeteritum.trim().ifBlank { verbDetails.partizip2.trim() }
                    form.removePrefix("Prät.:").removePrefix("Prät:").removePrefix("Prät.").trim()
                }

                val caseObjValue = remember(verbDetails, item.word) {
                    val rawCase = verbDetails?.caseObject.orEmpty().trim()
                    val clean = when {
                        rawCase.contains("akk", ignoreCase = true) && rawCase.contains("dat", ignoreCase = true) -> "Akkusativ / Dativ"
                        rawCase.contains("akk", ignoreCase = true) -> "Akkusativ"
                        rawCase.contains("dat", ignoreCase = true) -> "Dativ"
                        rawCase.contains("gen", ignoreCase = true) -> "Genitiv"
                        rawCase == "-" || rawCase.equals("none", ignoreCase = true) || rawCase.equals("kein", ignoreCase = true) -> ""
                        else -> {
                            val trimmed = rawCase.removeSuffix(":").trim()
                            when {
                                trimmed.equals("Akk.", ignoreCase = true) || trimmed.equals("Akk", ignoreCase = true) -> "Akkusativ"
                                trimmed.equals("Dat.", ignoreCase = true) || trimmed.equals("Dat", ignoreCase = true) -> "Dativ"
                                trimmed.equals("Gen.", ignoreCase = true) || trimmed.equals("Gen", ignoreCase = true) -> "Genitiv"
                                else -> trimmed
                            }
                        }
                    }
                    val candidate = if (clean.isBlank()) {
                        com.example.data.model.getFallbackVerbCaseObject(item.word)
                    } else {
                        clean
                    }
                    when {
                        candidate.contains("akk", ignoreCase = true) && candidate.contains("dat", ignoreCase = true) -> "Akkusativ / Dativ"
                        candidate.contains("akk", ignoreCase = true) -> "Akkusativ"
                        candidate.contains("dat", ignoreCase = true) -> "Dativ"
                        candidate.contains("gen", ignoreCase = true) -> "Genitiv"
                        else -> candidate.removeSuffix(":").trim()
                    }
                }

                val tertiaryColor = MaterialTheme.colorScheme.tertiary
                val isDark = LocalIsDarkTheme.current
                val normalTextColor = GeoOnSurface

                val pastAndCaseAnnotated = remember(praetValue, caseObjValue, tertiaryColor, isDark, normalTextColor) {
                    if (praetValue.isBlank() && caseObjValue.isBlank()) return@remember null
                    val redColor = if (isDark) Color(0xFFEF5350) else Color(0xFFC62828)

                    buildAnnotatedString {
                        var hasPreceding = false

                        // 1. Präteritum form: Prät.: bildete
                        if (praetValue.isNotBlank()) {
                            withStyle(SpanStyle(color = redColor, fontWeight = FontWeight.Bold)) {
                                append("Prät.:")
                            }
                            withStyle(SpanStyle(color = normalTextColor, fontWeight = FontWeight.Normal)) {
                                append(" $praetValue")
                            }
                            hasPreceding = true
                        }

                        // 2. Case Object: · Akkusativ / Dativ / Genitiv (no abbreviation, no colon, no extra text)
                        if (caseObjValue.isNotBlank()) {
                            if (hasPreceding) {
                                withStyle(SpanStyle(color = normalTextColor, fontWeight = FontWeight.Normal)) {
                                    append(" · ")
                                }
                            }
                            val caseLabel = caseObjValue.removeSuffix(":")
                            withStyle(SpanStyle(color = tertiaryColor, fontWeight = FontWeight.Bold)) {
                                append(caseLabel)
                            }
                        }
                    }
                }

                val metaScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(metaScrollState),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Date with emoji: 📅 08.09.2026
                            Text(
                                text = "📅 $shortDate",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = GeoOnSurfaceVariant
                                ),
                                maxLines = 1,
                                softWrap = false
                            )

                            // 2. Past tense label and Case object
                            if (pastAndCaseAnnotated != null) {
                                Text(
                                    text = pastAndCaseAnnotated,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    VerbTypeBadge()
                }
            }
        }
    } else {
        // Noun Unified Card Layout matching Verb Card 4-row structure
        Card(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp)
                .graphicsLayer { clip = false }
                .clickable(onClick = onClick)
                .testTag("history_item_${item.id}"),
            shape = RoundedCornerShape(12.dp),
            border = if (GeoIsBoldTheme) {
                BorderStroke(1.5.dp, GeoCardRibbonColor.copy(alpha = 0.55f))
            } else {
                BorderStroke(1.dp, GeoBorder.copy(alpha = 0.6f))
            },
            colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
        ) {
            if (GeoIsBoldTheme) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .background(Brush.horizontalGradient(GeoBannerGradient))
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 14.dp)
            ) {
                val titleWord = remember(nounDetails, singularDisplay, item.word) {
                    val fromNoun = nounDetails?.singular
                        ?.replace(Regex("(?i)^null\\s+"), "")
                        ?.replace(Regex("(?i)\\s+null$"), "")
                        ?.trim()
                        ?.takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }
                    val fromSingularDisplay = singularDisplay
                        .replace(Regex("(?i)^null\\s+"), "")
                        .replace(Regex("(?i)\\s+null$"), "")
                        .trim()
                        .takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }
                    val fromItemWord = item.word.trim().takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }

                    fromNoun ?: fromSingularDisplay ?: fromItemWord ?: ""
                }

                // Element 1: Header (word) - 32dp height, bold font, distinctive color
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .horizontalScroll(rememberScrollState()),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = titleWord,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (GeoIsBoldTheme) GeoPrimary else if (LocalIsDarkTheme.current) MaterialTheme.colorScheme.primary else Color(0xFF1E3A8A)
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Quick action icons (speak and delete) on top right
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CopyResultButton(
                            textToCopy = titleWord.ifBlank { item.word },
                            size = 28.dp,
                            iconSize = 16.dp,
                            testTag = "copy_history_noun_${item.id}"
                        )

                        val nounSpeakerId = "history_noun_${item.id}"
                        val isPlayingNoun = currentlySpokenId != null && currentlySpokenId == nounSpeakerId
                        IconButton(
                            onClick = { onSpeak(titleWord, nounSpeakerId) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("pronounce_history_${item.id}")
                        ) {
                            Icon(
                                imageVector = if (isPlayingNoun) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isPlayingNoun) "Stoppen" else "Wort anhören",
                                tint = if (isPlayingNoun) MaterialTheme.colorScheme.error else GeoOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("delete_history_item_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eintrag löschen",
                                tint = GeoOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Thin horizontal divider: 2dp top margin, 1dp height
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(GeoBorder.copy(alpha = 0.5f))
                )

                // Element 2: Declensions row (German) - 40dp height, scrollable horizontally
                Spacer(modifier = Modifier.height(4.dp))
                val declensionsScrollState = rememberScrollState()

                val pluralVal = remember(nounDetails, pluralText) {
                    val p = nounDetails?.plural?.ifBlank { pluralText } ?: pluralText
                    p.trim()
                }
                val genVal = remember(nounDetails) {
                    nounDetails?.genitiveSingular.orEmpty().trim()
                }
                val datVal = remember(nounDetails) {
                    nounDetails?.dativePlural.orEmpty().trim()
                }

                val isDark = LocalIsDarkTheme.current
                val contentTextColor = GeoOnSurface

                val declensionsAnnotated = remember(pluralVal, genVal, datVal, contentTextColor, isDark) {
                    val normalColor = contentTextColor
                    val purpleColor = if (isDark) Color(0xFFBA68C8) else Color(0xFF7B1FA2)
                    val blueColor = if (isDark) Color(0xFF64B5F6) else Color(0xFF1565C0)
                    val orangeColor = if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)

                    val parts = mutableListOf<Triple<String, String, Color>>()
                    if (pluralVal.isNotBlank() && pluralVal != "—") {
                        parts.add(Triple("Pl.:", pluralVal, purpleColor))
                    }
                    if (genVal.isNotBlank() && genVal != "—") {
                        parts.add(Triple("Gen.:", genVal, blueColor))
                    }
                    if (datVal.isNotBlank() && datVal != "—") {
                        parts.add(Triple("Dat. Pl.:", datVal, orangeColor))
                    }

                    if (parts.isEmpty()) return@remember null

                    buildAnnotatedString {
                        parts.forEachIndexed { index, (indicator, value, indColor) ->
                            if (index > 0) {
                                withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                                    append(" · ")
                                }
                            }
                            withStyle(SpanStyle(color = indColor, fontWeight = FontWeight.Bold)) {
                                append(indicator)
                            }
                            withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                                append(" $value")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .horizontalScroll(declensionsScrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (declensionsAnnotated != null) {
                        Text(
                            text = declensionsAnnotated,
                            fontSize = 14.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    } else {
                        Text(
                            text = "—",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = contentTextColor
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Thin horizontal divider: 2dp top margin, 1dp height
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.35f) else GeoBorder.copy(alpha = 0.5f))
                )

                // Element 3: Translation row (English / Arabic) - scrollable horizontally
                Spacer(modifier = Modifier.height(4.dp))
                val rawNounAiMeaning = nounDetails?.englishMeaning?.takeIf { it.isNotBlank() } ?: item.meaningEnglish.trim()
                val isArabic = remember(item, rawNounAiMeaning) {
                    item.translationLanguage.equals("ar", ignoreCase = true) || isArabicText(rawNounAiMeaning)
                }
                val prefix = if (isArabic) "الترجمة العربية : " else "Eng.: "
                val isDarkTheme = LocalIsDarkTheme.current
                val greenColor = if (isDarkTheme) Color(0xFF81C784) else Color(0xFF2E7D32)
                val translationScrollState = rememberScrollState()
                val translationText = remember(rawNounAiMeaning, isArabic) {
                    val clean = cleanTranslationText(rawNounAiMeaning, isArabic)
                    if (clean.contains(",")) {
                        clean.split(",").map { it.trim() }.filter { it.isNotBlank() }.joinToString(" · ")
                    } else {
                        clean
                    }
                }

                val translationAnnotated = remember(translationText, prefix, contentTextColor, greenColor) {
                    val normalColor = contentTextColor
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = greenColor, fontWeight = FontWeight.Bold)) {
                            append(prefix)
                        }
                        withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                            append(if (translationText.isNotBlank()) translationText else "—")
                        }
                    }
                }

                val altTranslationAnnotated = remember(item.manualTranslation, isDarkTheme, contentTextColor) {
                    if (item.manualTranslation.isBlank()) null
                    else {
                        val isAltArabic = isArabicText(item.manualTranslation)
                        val cleanManual = cleanTranslationText(item.manualTranslation.trim(), isAltArabic)
                        val altPrefixColor = if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFFD97706)
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = altPrefixColor, fontWeight = FontWeight.Bold)) {
                                append("Alt.: ")
                            }
                            withStyle(SpanStyle(color = contentTextColor, fontWeight = FontWeight.Normal)) {
                                append(cleanManual)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .horizontalScroll(translationScrollState),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = translationAnnotated,
                        fontSize = 14.sp,
                        softWrap = false
                    )
                }

                if (item.manualTranslation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    ManualTranslationDisplay(
                        manualTranslation = item.manualTranslation,
                        isDark = isDarkTheme,
                        fontSize = 13.5.sp,
                        textColor = contentTextColor,
                        testTag = "noun_history_manual_translation_${item.id}"
                    )
                }

                // Thin horizontal divider: 2dp top margin, 1dp height
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.35f) else GeoBorder.copy(alpha = 0.5f))
                )

                // Element 4: Metadata row - 28dp height
                Spacer(modifier = Modifier.height(4.dp))
                val shortDate = remember(item.timestamp) {
                    HistoryFormatterCache.formatShortDate(item.timestamp)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Date with emoji: 📅 08.09.2026 (left)
                    Text(
                        text = "📅 $shortDate",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GeoOnSurfaceVariant
                        ),
                        maxLines = 1,
                        softWrap = false
                    )

                    // 2. Gender badge moved to far right (colored container)
                    NounGenderBadge(gender = item.gender.ifBlank { nounDetails?.gender.orEmpty() })
                }
            }
        }
    }
}

private fun extractPluralForm(rawJson: String): String {
    if (rawJson.isBlank()) return ""
    return try {
        val json = JSONObject(rawJson)
        val pluralNoun = json.optSafeString("pluralNoun").trim()
        if (pluralNoun.isNotBlank() && !pluralNoun.equals("null", ignoreCase = true)) return pluralNoun
        val pluralObj = json.optJSONObject("plural")
        val nominativObj = pluralObj?.optJSONObject("nominativ")
        val definite = nominativObj?.optSafeString("definite").orEmpty().trim()
        if (definite.isNotBlank() && !definite.equals("null", ignoreCase = true)) return definite
        nominativObj?.optSafeString("noArticle").orEmpty().trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
    } catch (e: Exception) {
        ""
    }
}

internal data class AdjektivQuickDetails(
    val komparativ: String,
    val superlativ: String
)

internal fun extractAdjektivQuickDetails(item: WordHistoryEntity): AdjektivQuickDetails {
    if (item.rawJsonResult.isNotBlank()) {
        try {
            val obj = JSONObject(item.rawJsonResult)
            val komp = obj.optSafeString("komparativ", "").trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
            val sup = obj.optSafeString("superlativ", "").trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
            if (komp.isNotBlank() || sup.isNotBlank()) {
                return AdjektivQuickDetails(komp, sup)
            }
        } catch (_: Exception) {}
    }
    return AdjektivQuickDetails("", "")
}

@Composable
fun AdjektivHistoryCard(
    item: WordHistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSpeak: (String, String) -> Unit,
    currentlySpokenId: String? = null,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val contentTextColor = GeoOnSurface
    // Fixed turquoise color (does not change with app theme)
    val turquoiseLabelColor = Color(0xFF00897B)

    val adjDetails = remember(item.rawJsonResult) {
        extractAdjektivQuickDetails(item)
    }

    val isArabic = remember(item) {
        item.translationLanguage.equals("ar", ignoreCase = true) || isArabicText(item.meaningEnglish)
    }
    val prefix = if (isArabic) "الترجمة العربية : " else "Eng.: "
    val greenColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)

    val rawAiTranslation = remember(item.meaningEnglish, item.rawJsonResult, isArabic) {
        var raw = item.meaningEnglish.trim()
        if (raw.isBlank() && item.rawJsonResult.isNotBlank()) {
            try {
                val obj = JSONObject(item.rawJsonResult)
                raw = obj.optString("meaningEnglish", obj.optString("english", obj.optString("meaning", ""))).trim()
            } catch (_: Exception) {}
        }
        cleanTranslationText(raw, isArabic)
    }

    val translationAnnotated = remember(rawAiTranslation, prefix, greenColor, contentTextColor) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = greenColor, fontWeight = FontWeight.Bold)) {
                append(prefix)
            }
            withStyle(SpanStyle(color = contentTextColor, fontWeight = FontWeight.Normal)) {
                append(if (rawAiTranslation.isNotBlank()) rawAiTranslation else "—")
            }
        }
    }

    val altTranslationAnnotated = remember(item.manualTranslation, isDark, contentTextColor) {
        if (item.manualTranslation.isBlank()) null
        else {
            val isAltArabic = isArabicText(item.manualTranslation)
            val cleanManual = cleanTranslationText(item.manualTranslation.trim(), isAltArabic)
            val altPrefixColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
            buildAnnotatedString {
                withStyle(SpanStyle(color = altPrefixColor, fontWeight = FontWeight.Bold)) {
                    append("Alt.: ")
                }
                withStyle(SpanStyle(color = contentTextColor, fontWeight = FontWeight.Normal)) {
                    append(cleanManual)
                }
            }
        }
    }

    val compSupAnnotated = remember(adjDetails, isDark) {
        val labelColor = if (isDark) Color(0xFF80CBC4) else Color(0xFF00695C)
        buildAnnotatedString {
            withStyle(SpanStyle(color = labelColor, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)) {
                append("Komp.: ")
            }
            withStyle(SpanStyle(color = contentTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) {
                append(adjDetails.komparativ.ifBlank { "–" })
            }
            append("   ·   ")
            withStyle(SpanStyle(color = labelColor, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)) {
                append("Sup.: ")
            }
            withStyle(SpanStyle(color = contentTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) {
                append(adjDetails.superlativ.ifBlank { "–" })
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { clip = false }
            .clickable(onClick = onClick)
            .testTag("history_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        border = if (GeoIsBoldTheme) {
            BorderStroke(1.5.dp, GeoCardRibbonColor.copy(alpha = 0.55f))
        } else {
            BorderStroke(1.dp, GeoBorder.copy(alpha = 0.6f))
        },
        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Row 1: Base form (schön) at top + Speaker & Delete buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = item.word,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (GeoIsBoldTheme) GeoPrimary else if (LocalIsDarkTheme.current) MaterialTheme.colorScheme.primary else Color(0xFF1E3A8A)
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CopyResultButton(
                        textToCopy = item.word,
                        size = 28.dp,
                        iconSize = 16.dp,
                        testTag = "copy_history_adjektiv_${item.id}"
                    )

                    val adjSpeakerId = "history_adjektiv_${item.id}"
                    val isPlaying = currentlySpokenId != null && currentlySpokenId == adjSpeakerId
                    IconButton(
                        onClick = { onSpeak(item.word, adjSpeakerId) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("pronounce_history_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isPlaying) "Stoppen" else "Adjektiv anhören",
                            tint = if (isPlaying) MaterialTheme.colorScheme.error else GeoOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("delete_history_item_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eintrag löschen",
                            tint = GeoOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Row 2: Translation below base form
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .horizontalScroll(rememberScrollState()),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = translationAnnotated,
                    style = TextStyle(
                        fontSize = 13.5.sp,
                        color = contentTextColor,
                        lineHeight = 18.sp
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            }

            if (item.manualTranslation.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                ManualTranslationDisplay(
                    manualTranslation = item.manualTranslation,
                    isDark = isDark,
                    fontSize = 13.5.sp,
                    textColor = contentTextColor,
                    testTag = "word_list_history_manual_translation_${item.id}"
                )
            }

            // Row 3: Declensions (horizontal scrolling) + 12dp margin + Turquoise Label on the right (No date, no examples, no full tables)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = compSupAnnotated,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = contentTextColor
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Horizontal safe margin of at least 12dp between text and label
                Spacer(modifier = Modifier.width(12.dp))

                // Turquoise Pill Label
                NounGenderBadge(
                    gender = "Adjektiv",
                    modifier = Modifier.testTag("adjektiv_type_badge_${item.id}")
                )
            }
        }
    }
}

