package com.curiosityengine.app.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {

    fun create(url: String, anonKey: String): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = anonKey
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
}
