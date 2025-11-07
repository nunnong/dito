package com.dito.app.feature.auth

import android.util.Log
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

/**
 * 인증 화면 ViewModel
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "AuthViewModel"
    }

    /**
     * 로그인
     */
    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.signIn(username, password)

            result.onSuccess {
                Log.d(TAG, "로그인 성공")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorMessage = null
                    )
                }
            }.onFailure { exception ->
                Log.e(TAG, "로그인 실패: ${exception.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        errorMessage = "아이디와 비밀번호를 확인해주세요."
                    )
                }
            }
        }
    }

    /**
     * 회원가입
     * 참고: 실제 회원가입은 SignUpPermissionViewModel에서 처리됨
     * 이 메서드는 레거시 호환성을 위해 유지
     */
    fun signUp(
        username: String,
        password: String,
        nickname: String,
        birth: String,
        gender: String,
        job: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.signUp(username, password, nickname, birth, gender, job)

            result.onSuccess {
                Log.d(TAG, "회원가입 성공")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorMessage = null
                    )
                }
            }.onFailure { exception ->
                Log.e(TAG, "회원가입 실패: ${exception.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        errorMessage = exception.message ?: "회원가입에 실패했습니다"
                    )
                }
            }
        }
    }

    /**
     * 로그아웃
     */
    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = authRepository.signOut()

            result.onSuccess {
                Log.d(TAG, "로그아웃 성공")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        errorMessage = null
                    )
                }
            }.onFailure { exception ->
                Log.e(TAG, "로그아웃 실패: ${exception.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "로그아웃에 실패했습니다"
                    )
                }
            }
        }
    }

    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 로그인 상태 초기화
     */
    init {
        _uiState.update { it.copy(isLoggedIn = authRepository.isLoggedIn()) }
    }
}

/**
 * Auth UI 상태
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)
