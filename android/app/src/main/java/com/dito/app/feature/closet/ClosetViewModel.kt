package com.dito.app.feature.closet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.closet.ClosetItem
import com.dito.app.core.repository.ClosetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ClosetTab {
    COSTUME, BACKGROUND
}

data class ClosetUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val items: List<ClosetItem> = emptyList(),
    val error: String? = null,
    val canPaginate: Boolean = false,
    val currentPage: Int = 0,
    val selectedTab: ClosetTab = ClosetTab.COSTUME
)

@HiltViewModel
class ClosetViewModel @Inject constructor(
    private val closetRepository: ClosetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClosetUiState())
    val uiState: StateFlow<ClosetUiState> = _uiState.asStateFlow()

    init {
        loadItems(isInitialLoad = true)
    }

    fun onTabSelected(tab: ClosetTab) {
        if (tab == _uiState.value.selectedTab) return

        _uiState.update {
            it.copy(
                selectedTab = tab,
                items = emptyList(),
                currentPage = 0,
                canPaginate = false
            )
        }
        loadItems(isInitialLoad = true)
    }

    fun loadMoreItems() {
        if (_uiState.value.canPaginate && !_uiState.value.isLoadingMore) {
            loadItems(isInitialLoad = false)
        }
    }

    private fun loadItems(isInitialLoad: Boolean) {
        viewModelScope.launch {
            if (isInitialLoad) {
                _uiState.update { it.copy(isLoading = true) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            val pageToLoad = if (isInitialLoad) 0 else _uiState.value.currentPage + 1
            val type = _uiState.value.selectedTab.name

            closetRepository.getClosetItems(type = type, page = pageToLoad)
                .onSuccess { response ->
                    _uiState.update { currentState ->
                        val newItems = if (isInitialLoad) response.data
                                       else currentState.items + response.data
                        
                        currentState.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            items = newItems,
                            canPaginate = response.pageInfo.hasNext,
                            currentPage = response.pageInfo.page,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = error.message ?: "아이템을 불러오는데 실패했습니다."
                        )
                    }
                }
        }
    }
}
