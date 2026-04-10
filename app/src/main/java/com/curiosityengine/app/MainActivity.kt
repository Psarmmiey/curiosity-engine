package com.curiosityengine.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.curiosityengine.app.sync.RealtimeSyncManager
import com.curiosityengine.app.ui.navigation.CuriosityNavHost
import com.curiosityengine.app.ui.theme.CuriosityTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var realtimeSyncManager: RealtimeSyncManager

    @Inject
    lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start Realtime sync if user is already logged in
        val currentSession = supabaseClient.auth.currentSessionOrNull()
        if (currentSession != null) {
            val userId = currentSession.user?.id ?: ""
            if (userId.isNotBlank()) {
                realtimeSyncManager.startSync(userId, lifecycleScope)
            }
        }

        setContent {
            CuriosityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    CuriosityNavHost(
                        navController = navController,
                        onStartRealtimeSync = { userId ->
                            realtimeSyncManager.startSync(userId, lifecycleScope)
                        },
                        onStopRealtimeSync = {
                            realtimeSyncManager.stopSync()
                        },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realtimeSyncManager.stopSync()
    }
}
