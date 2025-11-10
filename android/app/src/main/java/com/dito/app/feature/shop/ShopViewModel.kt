package com.dito.app.feature.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.shop.ShopItem
import com.dito.app.core.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ShopTab {
    COSTUME, BACKGROUND
}

data class ShopUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val items: List<ShopItem> = emptyList(),
    val error: String? = null,
    val purchaseMessage: String? = null,
    val canPaginate: Boolean = false,
    val currentPage: Int = 0,
    val coinBalance: Int = 0,
    val selectedTab: ShopTab = ShopTab.COSTUME
)

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        loadItems(isInitialLoad = true)
    }

    fun onTabSelected(tab: ShopTab) {
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

    fun purchaseItem(itemId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, purchaseMessage = null) } // Show loading, clear previous error and message

            // Find the item to log its price
            val itemToPurchase = _uiState.value.items.find { it.itemId == itemId }
            android.util.Log.d("ShopViewModel", "Attempting to purchase item: ${itemToPurchase?.name} (ID: $itemId, Price: ${itemToPurchase?.price})")

            shopRepository.purchaseItem(itemId)
                .onSuccess { purchaseResponse ->
                    _uiState.update { currentState ->
                        // Find the purchased item and mark it as purchased
                        val updatedItems = currentState.items.map { item ->
                            if (item.itemId == itemId) item.copy(isPurchased = true) else item
                        }
                        currentState.copy(
                            isLoading = false,
                            items = updatedItems, // Optimistically update
                            purchaseMessage = purchaseResponse.message,
                            error = null
                        )
                    }
                    // Trigger a full reload to get accurate coin balance and item status
                    loadItems(isInitialLoad = true)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "아이템 구매에 실패했습니다.",
                            purchaseMessage = null
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

            shopRepository.getShopItems(type = type, page = pageToLoad)
                .onSuccess { response ->
                    _uiState.update { currentState ->
                        val newItems =
                            if (isInitialLoad) response.data.firstOrNull()?.items ?: emptyList()
                            else currentState.items + (response.data.firstOrNull()?.items
                                ?: emptyList())

                        currentState.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            items = newItems,
                            coinBalance = response.data.firstOrNull()?.coinBalance
                                ?: currentState.coinBalance,
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
