package com.curiosityengine.app.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curiosityengine.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val supabase: SupabaseClient,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            try {
                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    val userId = session.user?.id ?: run {
                        _authState.value = AuthState.Unauthenticated
                        return@launch
                    }
                    _authState.value = AuthState.Authenticated(userId)
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                Timber.e(e, "AuthViewModel: session check failed")
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val idToken = GoogleAuthHelper.getGoogleIdToken(context)
                if (idToken == null) {
                    _authState.value = AuthState.Unauthenticated
                    return@launch
                }
                supabase.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    provider = Google
                }
                val session = supabase.auth.currentSessionOrNull()
                val userId = session?.user?.id
                if (userId != null) {
                    _authState.value = AuthState.Authenticated(userId)
                } else {
                    _authState.value = AuthState.Error("Sign-in succeeded but no user ID found.")
                }
            } catch (e: Exception) {
                Timber.e(e, "AuthViewModel: Google sign-in failed")
                _authState.value = AuthState.Error(e.message ?: "Sign-in failed. Please try again.")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                val userId = (authState.value as? AuthState.Authenticated)?.userId
                supabase.auth.signOut()
                if (userId != null) {
                    userRepository.clearUserPrefs(userId)
                }
            } catch (e: Exception) {
                Timber.e(e, "AuthViewModel: sign-out failed")
            } finally {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
}
