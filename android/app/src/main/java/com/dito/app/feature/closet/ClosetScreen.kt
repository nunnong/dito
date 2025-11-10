package com.dito.app.feature.closet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dito.app.R
import com.dito.app.core.data.closet.ClosetItem
import com.dito.app.core.ui.designsystem.*

/** 옷장 화면 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosetScreen(
    viewModel: ClosetViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ClosetHeader(onBackClick = onBackClick)

        CharacterPreview(
            selectedTab = uiState.selectedTab,
            equippedCostumeImageUrl = uiState.items.firstOrNull { it.isEquipped && uiState.selectedTab == ClosetTab.COSTUME }?.imageUrl,
            equippedBackgroundImageUrl = uiState.items.firstOrNull { it.isEquipped && uiState.selectedTab == ClosetTab.BACKGROUND }?.imageUrl
        )

        TabSection(
            selectedTab = uiState.selectedTab,
            onTabSelected = viewModel::onTabSelected
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "오류가 발생했습니다.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red
                )
            } else {
                ItemGrid(
                    items = uiState.items,
                    canPaginate = uiState.canPaginate,
                    isLoadingMore = uiState.isLoadingMore,
                    onLoadMore = viewModel::loadMoreItems,
                    onApply = { itemId ->
                        viewModel.equipItem(itemId)
                    }
                )
            }
        }
    }
}

@Composable
private fun ClosetHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color.Black)
            .padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.angle_left),
            contentDescription = "Back",
            modifier = Modifier
                .size(28.dp)
                .clickable { onBackClick() },
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White)
        )
        Text(
            text = "옷장",
            style = DitoTypography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun CharacterPreview(
    selectedTab: ClosetTab,
    equippedCostumeImageUrl: String?,
    equippedBackgroundImageUrl: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .background(Color(0xFFF5EBD2)),
        contentAlignment = Alignment.Center
    ) {
        // Background Image
        if (equippedBackgroundImageUrl != null) {
            AsyncImage(
                model = equippedBackgroundImageUrl,
                contentDescription = "Equipped Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback background color or default image
            Spacer(modifier = Modifier.fillMaxSize().background(Color(0xFFF5EBD2)))
        }

        // Costume Image or Character Base Image
        if (equippedCostumeImageUrl != null) {
            // Show equipped costume
            AsyncImage(
                model = equippedCostumeImageUrl,
                contentDescription = "Equipped Costume",
                modifier = Modifier.size(146.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            // Show default character when no costume is equipped
            Image(
                painter = painterResource(id = R.drawable.dito),
                contentDescription = "Character Base",
                modifier = Modifier.size(146.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun TabSection(
    selectedTab: ClosetTab,
    onTabSelected: (ClosetTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "의상",
                style = DitoCustomTextStyles.titleDLarge,
                color = if (selectedTab == ClosetTab.COSTUME) Color.Black else Color(0xFFC9C4CE),
                modifier = Modifier.clickable { onTabSelected(ClosetTab.COSTUME) }
            )
            Text(
                text = "|",
                style = DitoCustomTextStyles.titleDLarge,
                color = Color.Black
            )
            Text(
                text = "배경",
                style = DitoCustomTextStyles.titleDLarge,
                color = if (selectedTab == ClosetTab.BACKGROUND) Color.Black else Color(0xFFC9C4CE),
                modifier = Modifier.clickable { onTabSelected(ClosetTab.BACKGROUND) }
            )
        }
    }
}

@Composable
private fun ItemGrid(
    items: List<ClosetItem>,
    canPaginate: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onApply: (Long) -> Unit // Changed to Long for itemId
) {
    val gridState = rememberLazyGridState()

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        itemsIndexed(items) { index, item ->
            ClosetItemCard(
                item = item,
                onApply = {
                    android.util.Log.d("ClosetScreen", "ItemGrid: onApply called for itemId: ${item.itemId}")
                    onApply(item.itemId)
                }
            )

            // Check if we need to load more items
            val isLastItem = index == items.lastIndex
            if (isLastItem && canPaginate && !isLoadingMore) {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
            }
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ClosetItemCard(
    item: ClosetItem,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.67.dp)
            .height(141.dp)
            .background(Color.White, RoundedCornerShape(4.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(85.dp)
                .background(Color(0xFFF5EBD2)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier.size(70.dp),
                contentScale = ContentScale.Fit,
                onSuccess = {
                    android.util.Log.d("ClosetItemCard", "Image loaded successfully: ${item.imageUrl}")
                },
                onError = { error ->
                    android.util.Log.e("ClosetItemCard", "Image loading failed for ${item.imageUrl}: ${error.result.throwable?.message}")
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (item.isEquipped) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(Primary, CircleShape)
                    .border(1.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "적용중",
                    style = DitoTypography.labelMedium,
                    color = Color.Black
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(Color.Black, CircleShape)
                    .clickable { onApply() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "적용하기",
                    style = DitoTypography.labelMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClosetScreenPreview() {
    ClosetScreen(onBackClick = {})
}
