package com.curiosityengine.app.feature.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import timber.log.Timber

object GoogleAuthHelper {

    // Replace with your actual Web Client ID from Google Cloud Console / Supabase Google OAuth setup
    private const val SERVER_CLIENT_ID = "YOUR_GOOGLE_SERVER_CLIENT_ID"

    /**
     * Requests a Google ID token using the modern Credential Manager API.
     * Returns the ID token string on success, or null on failure/cancellation.
     */
    suspend fun getGoogleIdToken(context: Context): String? {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(SERVER_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            googleIdTokenCredential.idToken
        } catch (e: GetCredentialException) {
            Timber.w(e, "GoogleAuthHelper: credential request failed")
            null
        } catch (e: Exception) {
            Timber.e(e, "GoogleAuthHelper: unexpected error")
            null
        }
    }
}
