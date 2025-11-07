package com.dito.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val username: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val isUsernameValid: Boolean? = null,
    val usernameErrorMessage: String = "",
    val passwordErrorMessage: String = "",
    val passwordConfirmErrorMessage: String = "",
    val isUsernameChecked: Boolean = false,
    val isCheckingUsername: Boolean = false,
    val navigateToNext: Boolean = false
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onUsernameChange(username: String) {
        _uiState.update {
            it.copy(
                username = username,
                isUsernameChecked = false,
                isUsernameValid = null,
                usernameErrorMessage = ""
            )
        }
    }

    fun onPasswordChange(password: String) {
        val passwordErrorMessage =
            if (password.isNotEmpty() && (password.length !in 8..20 ||
                        !password.matches(Regex("^(?=.*[a-z])(?=.*\\d)[a-z\\d]+$"))))
                "비밀번호는 8~20자의 영문 소문자와 숫자입니다." else ""

        val confirmErr =
            if (_uiState.value.passwordConfirm.isNotEmpty() && password != _uiState.value.passwordConfirm)
                "비밀번호와 일치하지 않습니다." else ""

        _uiState.update {
            it.copy(password = password,
                passwordErrorMessage = passwordErrorMessage,
                passwordConfirmErrorMessage = confirmErr)
        }
    }

    fun onPasswordConfirmChange(passwordConfirm: String) {
        val passwordConfirmErrorMessage = if (passwordConfirm.isNotEmpty() && _uiState.value.password != passwordConfirm) {
            "비밀번호와 일치하지 않습니다."
        } else {
            ""
        }
        _uiState.update {
            it.copy(
                passwordConfirm = passwordConfirm,
                passwordConfirmErrorMessage = passwordConfirmErrorMessage
            )
        }
    }

        fun checkUsernameAvailability() {

            viewModelScope.launch {

                val username = _uiState.value.username

                // Client-side validation

                if (username.length < 4 || username.length > 20 || !username.matches(Regex("^[a-z0-9]+$"))) {

                    _uiState.update { it.copy(usernameErrorMessage = "아이디는 4~20자의 영문 소문자, 숫자입니다.") }

                    return@launch

                }

    

                _uiState.update { it.copy(isCheckingUsername = true, usernameErrorMessage = "") }

    

                val result = authRepository.checkUsernameAvailability(username)

    

                result.onSuccess { isAvailable ->

                    if (isAvailable) {

                        _uiState.update {

                            it.copy(

                                isUsernameValid = true,

                                isUsernameChecked = true,

                                usernameErrorMessage = "",

                                isCheckingUsername = false

                            )

                        }

                    } else {

                        _uiState.update {

                            it.copy(

                                isUsernameValid = false,

                                usernameErrorMessage = "이미 사용 중인 아이디입니다.",

                                isCheckingUsername = false

                            )

                        }

                    }

                }.onFailure { exception ->

                    _uiState.update {

                        it.copy(

                            isUsernameValid = false,

                            usernameErrorMessage = exception.message ?: "오류가 발생했습니다.",

                            isCheckingUsername = false

                        )

                    }

                }

            }

        }

    fun onNextClicked() {
        var hasError = false
        val currentState = _uiState.value

        if (!currentState.isUsernameChecked) {
            _uiState.update { it.copy(usernameErrorMessage = "아이디 중복확인이 필요합니다.") }
            hasError = true
        }

        if (currentState.password.length < 8 || currentState.password.length > 20 || !currentState.password.matches(Regex("^(?=.*[a-z])(?=.*\\d)[a-z\\d]+$")))
        {
            _uiState.update { it.copy(passwordErrorMessage = "비밀번호는 8~20자의 영문 소문자 + 숫자입니다.") }
            hasError = true
        }

        if (currentState.password != currentState.passwordConfirm) {
            _uiState.update { it.copy(passwordConfirmErrorMessage = "비밀번호와 일치하지 않습니다.") }
            hasError = true
        }

        if (!hasError) {
            _uiState.update { it.copy(navigateToNext = true) }
        }
    }
    
    fun onNavigated() {
        _uiState.update { it.copy(navigateToNext = false) }
    }
}
