package com.dito.app.feature.group

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.group.GroupInfo
import com.dito.app.core.data.group.RankingItem
import com.dito.app.core.repository.GroupRepository
import com.dito.app.core.storage.GroupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.OutputStream
import javax.inject.Inject

data class ChallengeResultUiState(
    val isLoading: Boolean = false,
    val rankings: List<RankingItem> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ChallengeResultViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val groupManager: GroupManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeResultUiState())
    val uiState: StateFlow<ChallengeResultUiState> = _uiState.asStateFlow()

    private val _saveSuccess = MutableStateFlow<Boolean?>(null)
    val saveSuccess: StateFlow<Boolean?> = _saveSuccess.asStateFlow()

    init {
        loadRanking()
    }

    fun loadRanking() {
        val groupId = groupManager.getGroupId()
        if (groupId == 0L) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "그룹 정보를 찾을 수 없습니다"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            groupRepository.getRanking(groupId).fold(
                onSuccess = { ranking ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        rankings = ranking.rankings,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "랭킹 조회에 실패했습니다"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun saveScreenshot(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val filename = "challenge_result_${System.currentTimeMillis()}.png"
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Dito")
                    }
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                uri?.let {
                    val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
                    outputStream?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                    _saveSuccess.value = true
                } ?: run {
                    _saveSuccess.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _saveSuccess.value = false
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = null
    }
}
