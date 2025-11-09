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
    val equipMessage: String? = null,
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

    fun equipItem(itemId: Long) {
        viewModelScope.launch {
            android.util.Log.d("ClosetViewModel", "equipItem called for itemId: $itemId")
            _uiState.update { it.copy(isLoading = true, error = null, equipMessage = null) }

            closetRepository.equipItem(itemId)
                .onSuccess { equipResponse ->
                    android.util.Log.d("ClosetViewModel", "equipItem onSuccess: ${equipResponse.message}")
                    _uiState.update { currentState ->
                        val updatedItems = currentState.items.map { item ->
                            if (item.itemId == itemId) {
                                item.copy(isEquipped = true)
                            } else if (item.isEquipped) { // If another item is equipped, unequip it
                                item.copy(isEquipped = false)
                            } else {
                                item
                            }
                        }
                        currentState.copy(
                            isLoading = false,
                            items = updatedItems, // Optimistically update
                            equipMessage = equipResponse.message,
                            error = null
                        )
                    }
                    // After optimistic update, trigger a full reload to get accurate item status
                    loadItems(isInitialLoad = true)
                }
                .onFailure { error ->
                    android.util.Log.e("ClosetViewModel", "equipItem onFailure: ${error.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "아이템 적용에 실패했습니다.",
                            equipMessage = null
                        )
                    }
                }
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
