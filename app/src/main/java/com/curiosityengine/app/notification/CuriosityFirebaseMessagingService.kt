package com.curiosityengine.app.notification

import com.curiosityengine.app.data.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CuriosityFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: CuriosityNotificationManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var supabaseClient: SupabaseClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("FCM message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val type = data["type"] ?: run {
            Timber.w("FCM message has no type field, ignoring")
            return
        }
        val topic = data["topic"] ?: ""
        val category = data["category"] ?: ""
        val streak = data["streak"]?.toIntOrNull() ?: 0
        val milestone = data["milestone"]?.toIntOrNull() ?: 0

        when (type) {
            "lesson_ready" -> notificationManager.showLessonReadyNotification(
                context = applicationContext,
                topic = topic,
                category = category
            )
            "streak_risk" -> notificationManager.showStreakRiskNotification(
                context = applicationContext,
                streakDays = streak
            )
            "weekly_review" -> notificationManager.showWeeklyReviewNotification(
                context = applicationContext
            )
            "milestone" -> notificationManager.showMilestoneNotification(
                context = applicationContext,
                milestone = milestone
            )
            "keep_learning" -> notificationManager.showKeepLearningNotification(
                context = applicationContext
            )
            else -> Timber.w("Unknown FCM message type: $type")
        }
    }

    override fun onNewToken(token: String) {
        Timber.d("FCM token refreshed: ${token.take(10)}...")

        serviceScope.launch {
            try {
                val userId = supabaseClient.auth.currentSessionOrNull()?.user?.id
                    ?: run {
                        Timber.d("No active session; FCM token will be synced on next sign-in")
                        return@launch
                    }

                val prefs = userRepository.getUserPrefsOnce(userId)
                    ?: run {
                        Timber.d("No user prefs found for userId=$userId; skipping FCM token store")
                        return@launch
                    }

                // Persist the updated prefs — the FCM token itself is stored
                // server-side via the edge function on next lesson fetch; here we
                // just trigger a prefs save to ensure the session is current.
                userRepository.saveUserPrefs(prefs)

                Timber.d("FCM token refresh handled for userId=$userId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to handle FCM token refresh")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
