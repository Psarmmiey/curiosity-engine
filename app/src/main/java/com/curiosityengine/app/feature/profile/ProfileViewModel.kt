package com.curiosityengine.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curiosityengine.app.data.model.Streak
import com.curiosityengine.app.data.repository.LessonRepository
import com.curiosityengine.app.data.repository.StreakRepository
import com.curiosityengine.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val streakRepository: StreakRepository,
    private val lessonRepository: LessonRepository,
    private val supabase: SupabaseClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var userId: String = ""

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val prefs = userRepository.getUserPrefsOnce("") ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "User not found")
                    return@launch
                }
                userId = prefs.userId

                combine(
                    userRepository.getUserPrefs(userId),
                    streakRepository.getStreak(userId),
                    lessonRepository.getReadCount(userId),
                ) { latestPrefs, streak, readCount ->
                    val currentPrefs = latestPrefs ?: prefs
                    val badges = computeBadges(streak, readCount)
                    ProfileUiState(
                        isLoading = false,
                        displayName = currentPrefs.displayName ?: "",
                        email = currentPrefs.email ?: "",
                        avatarUrl = currentPrefs.avatarUrl,
                        streak = streak,
                        totalLessonsCompleted = readCount,
                        badges = badges,
                        error = null,
                    )
                }
                    .catch { e ->
                        Timber.e(e, "ProfileViewModel: error observing profile data")
                        emit(_uiState.value.copy(isLoading = false, error = e.message ?: "Unknown error"))
                    }
                    .collect { state ->
                        _uiState.value = state
                    }
            } catch (e: Exception) {
                Timber.e(e, "ProfileViewModel: failed to load profile")
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }

    private fun computeBadges(streak: Streak?, totalLessons: Int): List<Badge> {
        val chainStreak = streak?.chainStreak ?: 0
        return listOf(
            Badge(
                id = "first_step",
                emoji = "🌱",
                title = "First Step",
                description = "Read your first lesson",
                isUnlocked = totalLessons >= 1,
            ),
            Badge(
                id = "week_warrior",
                emoji = "⚔️",
                title = "Week Warrior",
                description = "Maintain a 7-day chain streak",
                isUnlocked = chainStreak >= 7,
            ),
            Badge(
                id = "fortnight",
                emoji = "🌙",
                title = "Fortnight",
                description = "Maintain a 14-day chain streak",
                isUnlocked = chainStreak >= 14,
            ),
            Badge(
                id = "month_master",
                emoji = "🏆",
                title = "Month Master",
                description = "Maintain a 30-day chain streak",
                isUnlocked = chainStreak >= 30,
            ),
            Badge(
                id = "century",
                emoji = "💯",
                title = "Century",
                description = "Complete 100 lessons",
                isUnlocked = totalLessons >= 100,
            ),
        )
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                if (userId.isNotEmpty()) {
                    userRepository.clearUserPrefs(userId)
                }
            } catch (e: Exception) {
                Timber.e(e, "ProfileViewModel: sign-out failed")
            }
        }
    }
}
