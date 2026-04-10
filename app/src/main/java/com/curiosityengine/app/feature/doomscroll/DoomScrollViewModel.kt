package com.curiosityengine.app.feature.doomscroll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curiosityengine.app.data.repository.LessonRepository
import com.curiosityengine.app.network.SupabaseEdgeFunctions
import com.curiosityengine.app.util.DateTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DoomScrollViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val supabase: SupabaseClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoomScrollUiState())
    val uiState: StateFlow<DoomScrollUiState> = _uiState.asStateFlow()

    // Guard against concurrent prefetch calls
    private var prefetchInFlight = false

    init {
        loadBonusLessons()
    }

    private fun loadBonusLessons() {
        viewModelScope.launch {
            try {
                val userId = currentUserId() ?: return@launch
                val existing = lessonRepository.getBonusLessons(userId).first()
                _uiState.update { it.copy(lessons = existing) }
                if (existing.size < 3) {
                    repeat(3 - existing.size) {
                        prefetchNextLesson()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "DoomScrollViewModel: failed to load bonus lessons")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun prefetchNextLesson() {
        if (prefetchInFlight) return
        prefetchInFlight = true

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            try {
                val userId = currentUserId() ?: return@launch
                val date = DateTimeUtil.formatDate(DateTimeUtil.today())
                SupabaseEdgeFunctions.generateLesson(
                    client = supabase,
                    userId = userId,
                    category = "Surprise Me",
                    date = date,
                    model = "gemini-2.5-flash",
                )
                // Re-fetch bonus lessons from Room after the edge function
                // persists the new lesson (repository handles caching)
                val updated = lessonRepository.getBonusLessons(userId).first()
                _uiState.update { it.copy(lessons = updated, isLoadingMore = false) }
            } catch (e: Exception) {
                Timber.e(e, "DoomScrollViewModel: prefetch failed")
                _uiState.update { it.copy(isLoadingMore = false, error = e.message) }
            } finally {
                prefetchInFlight = false
            }
        }
    }

    fun onPageChanged(index: Int) {
        _uiState.update { it.copy(currentIndex = index) }
        val lessons = _uiState.value.lessons
        if (index >= lessons.size - 1) {
            prefetchNextLesson()
        }
    }

    fun markCurrentRead(lessonId: String) {
        viewModelScope.launch {
            try {
                lessonRepository.markAsRead(lessonId)
            } catch (e: Exception) {
                Timber.e(e, "DoomScrollViewModel: markAsRead failed for $lessonId")
            }
        }
    }

    private fun currentUserId(): String? =
        supabase.auth.currentSessionOrNull()?.user?.id
}
