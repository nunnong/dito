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
    val selectedTab: ClosetTab = ClosetTab.COSTUME,
    val equippedCostumeUrl: String? = null,
    val equippedBackgroundUrl: String? = null
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
            // _uiState.update { it.copy(isLoading = true, error = null, equipMessage = null) } // Removed isLoading update

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

                        // Get newly equipped item URL
                        val newlyEquippedUrl = updatedItems.firstOrNull { it.itemId == itemId }?.imageUrl

                        currentState.copy(
                            // isLoading = false, // Removed isLoading update
                            items = updatedItems, // Optimistically update
                            equipMessage = equipResponse.message,
                            error = null,
                            equippedCostumeUrl = if (currentState.selectedTab == ClosetTab.COSTUME) newlyEquippedUrl else currentState.equippedCostumeUrl,
                            equippedBackgroundUrl = if (currentState.selectedTab == ClosetTab.BACKGROUND) newlyEquippedUrl else currentState.equippedBackgroundUrl
                        )
                    }
                    // After optimistic update, trigger a full reload to get accurate item status
                    // loadItems(isInitialLoad = true) // Removed this line
                }
                .onFailure { error ->
                    android.util.Log.e("ClosetViewModel", "equipItem onFailure: ${error.message}")
                    _uiState.update {
                        it.copy(
                            // isLoading = false, // Removed isLoading update
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
                // For a true initial load, reset items. For the recursive part, we don't.
                if (_uiState.value.items.isEmpty()) {
                    _uiState.update { it.copy(isLoading = true) }
                }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            val pageToLoad = if (isInitialLoad) 0 else _uiState.value.currentPage + 1
            val type = _uiState.value.selectedTab.name

            closetRepository.getClosetItems(type = type, page = pageToLoad)
                .onSuccess { response ->
                    _uiState.update { currentState ->
                        val currentItems = if (isInitialLoad && pageToLoad == 0) emptyList() else currentState.items
                        val newItems = currentItems + response.data
                        val equippedItem = newItems.firstOrNull { it.isEquipped }

                        currentState.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            items = newItems,
                            canPaginate = response.pageInfo.hasNext,
                            currentPage = response.pageInfo.page,
                            error = null,
                            equippedCostumeUrl = if (currentState.selectedTab == ClosetTab.COSTUME && equippedItem != null) equippedItem.imageUrl else currentState.equippedCostumeUrl,
                            equippedBackgroundUrl = if (currentState.selectedTab == ClosetTab.BACKGROUND && equippedItem != null) equippedItem.imageUrl else currentState.equippedBackgroundUrl
                        )
                    }

                    // If this was part of an initial load, we haven't found the equipped item yet, and there are more pages, load the next page.
                    val equippedItemFound = _uiState.value.items.any { it.isEquipped }
                    if (isInitialLoad && !equippedItemFound && _uiState.value.canPaginate) {
                        loadItems(isInitialLoad = false) // Continue loading the next page
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
