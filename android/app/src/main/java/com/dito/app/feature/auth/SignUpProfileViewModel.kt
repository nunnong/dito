package com.dito.app.feature.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SignUpProfileUiState(
    val nickname: String = "",
    val birthYear: Int = 1990,
    val birthMonth: Int = 1,
    val birthDay: Int = 1,
    val gender: String = "",
    val nicknameErrorMessage: String = "",
    val navigateToNext: Boolean = false
)

@HiltViewModel
class SignUpProfileViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpProfileUiState())
    val uiState: StateFlow<SignUpProfileUiState> = _uiState.asStateFlow()

    fun onNicknameChange(nickname: String) {
        _uiState.update {
            it.copy(
                nickname = nickname,
                nicknameErrorMessage = "" // Clear error on typing
            )
        }
    }

    fun onBirthYearChange(year: Int) {
        _uiState.update { it.copy(birthYear = year) }
    }

    fun onBirthMonthChange(month: Int) {
        _uiState.update { it.copy(birthMonth = month) }
    }

    fun onBirthDayChange(day: Int) {
        _uiState.update { it.copy(birthDay = day) }
    }

    fun onGenderChange(gender: String) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun onNextClicked() {
        // Validate nickname immediately when Next is clicked
        val error = validateNickname(_uiState.value.nickname)
        _uiState.update { it.copy(nicknameErrorMessage = error) }

        if (validateForm()) {
            _uiState.update { it.copy(navigateToNext = true) }
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(navigateToNext = false) }
    }

    fun validateNicknameOnBlur() {
        _uiState.update {
            it.copy(nicknameErrorMessage = validateNickname(it.nickname))
        }
    }

    private fun validateNickname(nickname: String): String {
        // 1~7자의 영문 또는 한글만 허용
        val pattern = Regex("^[a-zA-Z가-힣]{1,7}$")
        return when {
            nickname.isEmpty() -> ""
            !pattern.matches(nickname) -> "닉네임은 1~7자의 영문, 한글입니다."
            else -> ""
        }
    }

    private fun validateForm(): Boolean {
        val currentState = _uiState.value
        return currentState.nickname.isNotBlank() &&
                currentState.birthYear > 0 &&
                currentState.birthMonth > 0 &&
                currentState.birthDay > 0 &&
                currentState.gender.isNotBlank() &&
                currentState.nicknameErrorMessage.isEmpty()
    }
}
