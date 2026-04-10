package com.curiosityengine.app.sync

import com.curiosityengine.app.data.repository.LessonRepository
import com.curiosityengine.app.data.repository.StreakRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.createRealtimeChannel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeSyncManager @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val streakRepository: StreakRepository,
    @Suppress("UnusedPrivateMember")
    private val lessonRepository: LessonRepository,
) {
    private var realtimeChannel: RealtimeChannel? = null
    private var syncScope: CoroutineScope? = null

    fun startSync(userId: String, coroutineScope: CoroutineScope) {
        syncScope = coroutineScope

        realtimeChannel = supabaseClient.realtime.createRealtimeChannel("user-$userId")

        realtimeChannel!!
            .postgresChangeFlow<PostgresAction.Update>(schema = "public") { table = "streaks" }
            .onEach { _ ->
                streakRepository.syncStreak(userId)
            }
            .launchIn(coroutineScope)

        coroutineScope.launch {
            supabaseClient.realtime.connect()
            realtimeChannel!!.subscribe()
        }
    }

    fun stopSync() {
        val channel = realtimeChannel ?: return
        val scope = syncScope
        if (scope != null) {
            scope.launch {
                runCatching { channel.unsubscribe() }
            }
        }
        realtimeChannel = null
        syncScope = null
    }
}
