package com.curiosityengine.app.network

/**
 * Claude quiz generation is handled exclusively through Supabase Edge Functions.
 * This class serves as a marker/placeholder for the DI graph.
 * Direct Anthropic API calls are never made from the Android client.
 */
class ClaudeClient {
    // All Claude calls go via Supabase Edge Function /generate-quiz
    // which holds the ANTHROPIC_API_KEY in Supabase Vault
}
