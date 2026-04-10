package com.curiosityengine.app.feature.lesson

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.curiosityengine.app.data.model.Reference
import com.curiosityengine.app.feature.lesson.block.BlockRenderer
import com.curiosityengine.app.ui.components.CategoryChip
import com.curiosityengine.app.ui.theme.BackgroundDeep
import com.curiosityengine.app.ui.theme.BrandGold
import com.curiosityengine.app.ui.theme.TextPrimary
import com.curiosityengine.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    lessonId: String,
    onNavigateToQuiz: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LessonViewModel = hiltViewModel(),
) {
    LaunchedEffect(lessonId) {
        viewModel.loadLesson(lessonId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showCompletionOverlay by viewModel.showCompletionOverlay.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDeep,
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? LessonUiState.Success)?.lesson?.topic ?: ""
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                },
                actions = {
                    val category = (uiState as? LessonUiState.Success)?.lesson?.category?.displayName
                    if (category != null) {
                        CategoryChip(
                            category = category,
                            modifier = Modifier.padding(end = 12.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDeep,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                is LessonUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BrandGold,
                    )
                }

                is LessonUiState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                    )
                }

                is LessonUiState.Success -> {
                    val lesson = state.lesson
                    val listState = rememberLazyListState()

                    // Compute scroll progress and forward to ViewModel
                    LaunchedEffect(listState) {
                        snapshotFlow {
                            val layoutInfo = listState.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount
                            if (totalItems == 0) return@snapshotFlow 0f
                            val visibleItems = layoutInfo.visibleItemsInfo
                            val lastVisible = visibleItems.lastOrNull() ?: return@snapshotFlow 0f
                            // Fraction of the last visible item that is actually shown
                            val lastFraction = run {
                                val visibleBottom = lastVisible.offset + lastVisible.size
                                val viewportEnd = layoutInfo.viewportEndOffset
                                if (visibleBottom > viewportEnd) {
                                    (viewportEnd - lastVisible.offset).toFloat() / lastVisible.size.toFloat()
                                } else {
                                    1f
                                }
                            }
                            val progress = (lastVisible.index + lastFraction) / totalItems.toFloat()
                            progress.coerceIn(0f, 1f)
                        }.collect { progress ->
                            viewModel.updateScrollProgress(progress)
                        }
                    }

                    var referencesExpanded by remember { mutableStateOf(false) }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        // Emoji + topic header
                        item(key = "header") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                            ) {
                                Text(
                                    text = lesson.emoji,
                                    style = MaterialTheme.typography.displayMedium,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = lesson.topic,
                                    style = MaterialTheme.typography.displaySmall,
                                    color = TextPrimary,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lesson.date,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                )
                            }
                        }

                        // Lesson blocks
                        itemsIndexed(
                            items = lesson.blocks,
                            key = { index, _ -> "block_$index" },
                        ) { _, block ->
                            BlockRenderer(
                                block = block,
                                category = lesson.category.displayName,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        // References section (expandable)
                        if (lesson.references.isNotEmpty()) {
                            item(key = "references_divider") {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = TextSecondary.copy(alpha = 0.2f),
                                )
                            }
                            item(key = "references_header") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { referencesExpanded = !referencesExpanded }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "References",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Icon(
                                        imageVector = if (referencesExpanded) Icons.Filled.ExpandLess
                                        else Icons.Filled.ExpandMore,
                                        contentDescription = if (referencesExpanded) "Collapse" else "Expand",
                                        tint = TextSecondary,
                                    )
                                }
                            }
                            item(key = "references_content") {
                                AnimatedVisibility(
                                    visible = referencesExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically(),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .padding(bottom = 16.dp),
                                    ) {
                                        lesson.references.forEach { ref ->
                                            ReferenceItem(reference = ref)
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom padding
                        item(key = "bottom_spacer") {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }

                    // Completion overlay (ModalBottomSheet)
                    if (showCompletionOverlay) {
                        LessonCompletionOverlay(
                            lesson = lesson,
                            streakRepository = viewModel.streakRepository,
                            onNavigateToQuiz = onNavigateToQuiz,
                            onDismiss = viewModel::dismissCompletionOverlay,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceItem(reference: Reference) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                runCatching { uriHandler.openUri(reference.url) }
            },
    ) {
        Text(
            text = reference.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = BrandGold,
        )
        if (!reference.description.isNullOrBlank()) {
            Text(
                text = reference.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
        Text(
            text = reference.url,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
