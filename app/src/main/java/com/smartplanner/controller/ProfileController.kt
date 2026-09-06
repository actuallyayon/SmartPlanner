package com.smartplanner.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartplanner.model.Repository
import com.smartplanner.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val editName: String = "",
    val editEmail: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val message: String? = null,
    val isError: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileController @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    companion object {
        // App-wide Dark/Light Theme toggle state
        val isDarkTheme = MutableStateFlow(true) 
    }

    private val _editState = MutableStateFlow<ProfileUiState?>(null)
    
    val uiState: StateFlow<ProfileUiState> = combine(
        repository.getCurrentUser(),
        _editState
    ) { user, editState ->
        if (editState != null) {
            editState.copy(user = user)
        } else {
            ProfileUiState(
                user = user,
                editName = user?.name ?: "",
                editEmail = user?.email ?: ""
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState(isLoading = true)
    )

    fun onNameChanged(name: String) {
        val currentState = uiState.value
        _editState.value = currentState.copy(editName = name, message = null)
    }

    fun onEmailChanged(email: String) {
        val currentState = uiState.value
        _editState.value = currentState.copy(editEmail = email, message = null)
    }

    fun onNewPasswordChanged(password: String) {
        val currentState = uiState.value
        _editState.value = currentState.copy(newPassword = password, message = null)
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        val currentState = uiState.value
        _editState.value = currentState.copy(confirmPassword = confirmPassword, message = null)
    }

    fun saveProfile() {
        val currentState = uiState.value
        val name = currentState.editName
        val email = currentState.editEmail
        val newPassword = currentState.newPassword
        val confirmPassword = currentState.confirmPassword

        if (name.isBlank() || email.isBlank()) {
            _editState.value = currentState.copy(
                message = "Name and Email cannot be blank",
                isError = true
            )
            return
        }

        if (newPassword.isNotBlank() || confirmPassword.isNotBlank()) {
            if (newPassword != confirmPassword) {
                _editState.value = currentState.copy(
                    message = "Passwords do not match",
                    isError = true
                )
                return
            }
        }

        viewModelScope.launch {
            repository.updateCurrentUser(name, email)
            _editState.value = currentState.copy(
                newPassword = "",
                confirmPassword = "",
                message = "Profile updated successfully!",
                isError = false
            )
        }
    }

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }
}
