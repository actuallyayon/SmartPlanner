package com.smartplanner.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartplanner.model.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val error: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class AuthController @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = repository.isUserLoggedIn()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, error = null)
    }

    fun loginDemo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.loginDemoUser()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun login() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Log in the user by calling loginDemoUser in repository
            repository.loginDemoUser()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun register() {
        val name = _uiState.value.name
        val email = _uiState.value.email
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all fields")
            return
        }

        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "Passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Update the user details and log them in
            repository.updateCurrentUser(name, email)
            repository.loginDemoUser()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            // Reset input state on logout
            _uiState.value = AuthUiState()
        }
    }
}
