package com.curiosityengine.app.di

import com.curiosityengine.app.BuildConfig
import com.curiosityengine.app.network.ClaudeClient
import com.curiosityengine.app.network.GeminiClient
import com.curiosityengine.app.network.SupabaseClientProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient =
        SupabaseClientProvider.create(
            url = BuildConfig.SUPABASE_URL,
            anonKey = BuildConfig.SUPABASE_ANON_KEY
        )

    @Provides
    @Singleton
    fun provideGeminiClient(): GeminiClient =
        GeminiClient(apiKey = BuildConfig.GEMINI_API_KEY)

    @Provides
    @Singleton
    fun provideClaudeClient(): ClaudeClient =
        ClaudeClient()
        // Note: Claude is called via Supabase Edge Functions only — no direct API key on device
}
