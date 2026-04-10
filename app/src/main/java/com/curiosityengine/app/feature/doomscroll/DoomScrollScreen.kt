package com.curiosityengine.app.feature.doomscroll

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.curiosityengine.app.ui.theme.BackgroundDeep
import com.curiosityengine.app.ui.theme.BrandGold
import com.curiosityengine.app.ui.theme.TextPrimary

@Composable
fun DoomScrollScreen(
    onNavigateToLesson: (String) -> Unit,
    onExit: () -> Unit,
    viewModel: DoomScrollViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lessons = uiState.lessons

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { if (lessons.isEmpty()) 1 else lessons.size },
    )

    // Notify ViewModel when the visible page changes so it can prefetch
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }

    // Mark lesson as read when the pager fully settles on a page
    LaunchedEffect(pagerState.settledPage) {
        val index = pagerState.settledPage
        if (index < lessons.size) {
            viewModel.markCurrentRead(lessons[index].id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        if (lessons.isEmpty() && uiState.isLoadingMore) {
            // Initial loading state — nothing to page through yet
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = BrandGold,
            )
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                if (page < lessons.size) {
                    DoomScrollLessonCard(
                        lesson = lessons[page],
                        onOpenFullLesson = onNavigateToLesson,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    // Placeholder page shown while next lesson is being fetched
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = BrandGold)
                    }
                }
            }
        }

        // Exit button — top-right corner
        TextButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp),
        ) {
            Text(
                text = "✕ Exit",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
            )
        }

        // Loading indicator at bottom when prefetching next lesson
        if (uiState.isLoadingMore && lessons.isNotEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .size(24.dp),
                color = BrandGold,
                strokeWidth = 2.dp,
            )
        }
    }
}
