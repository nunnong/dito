package com.dito.app.feature.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
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
import com.dito.app.core.data.shop.ShopItem
import com.dito.app.core.ui.designsystem.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import coil.compose.rememberAsyncImagePainter

@Composable
fun BounceClickable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (isPressed: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content(isPressed)
    }
}

@Composable
fun ShopScreen(
    viewModel: ShopViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showPurchaseConfirmDialog by remember { mutableStateOf(false) }
    var showInsufficientCoinsDialog by remember { mutableStateOf(false) }
    var selectedItemForPurchase by remember { mutableStateOf<ShopItem?>(null) }

    // Observe purchase messages from ViewModel
    LaunchedEffect(uiState.purchaseMessage) {
        uiState.purchaseMessage?.let { message ->
            // TODO: Show a Snackbar or Toast with the message
            // For now, just log it
            android.util.Log.d("ShopScreen", "Purchase Message: $message")
            // Clear the message after showing
            // viewModel.clearPurchaseMessage() // Need to add this to ViewModel
        }
    }

    if (showPurchaseConfirmDialog && selectedItemForPurchase != null) {
        ShopConfirmDialog(
            itemImage = rememberAsyncImagePainter(selectedItemForPurchase!!.imageUrl),
            isCostume = uiState.selectedTab == ShopTab.COSTUME,
            onConfirm = {
                selectedItemForPurchase?.let { item ->
                    viewModel.purchaseItem(item.itemId)
                }
            },
            onApply = {
                selectedItemForPurchase?.let { item ->
                    viewModel.equipItem(item.itemId)
                }
            },
            onDismiss = {
                showPurchaseConfirmDialog = false
                selectedItemForPurchase = null
            }
        )
    }

    if (showInsufficientCoinsDialog) {
        ShopInsufficientCoinsDialog(
            onDismiss = {
                showInsufficientCoinsDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ShopHeader(onBackClick = onBackClick)

        TabAndCoinSection(
            selectedTab = uiState.selectedTab,
            onTabSelected = viewModel::onTabSelected,
            coins = uiState.coinBalance
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
                    onPurchase = { item ->
                        if (uiState.coinBalance >= item.price) {
                            selectedItemForPurchase = item
                            showPurchaseConfirmDialog = true
                        } else {
                            showInsufficientCoinsDialog = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ShopHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color.Black)
            .padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BounceClickable(
            onClick = onBackClick,
            modifier = Modifier.size(28.dp)
        ) { isPressed ->
            Image(
                painter = painterResource(id = R.drawable.angle_left),
                contentDescription = "Back",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                colorFilter = if (isPressed) ColorFilter.tint(Primary) else ColorFilter.tint(Color.White)
            )
        }
        Text(
            text = "상점",
            style = DitoTypography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun TabAndCoinSection(
    selectedTab: ShopTab,
    onTabSelected: (ShopTab) -> Unit,
    coins: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // 중앙 정렬된 탭
        Row(
            modifier = Modifier
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "의상",
                style = DitoCustomTextStyles.titleDLarge,
                color = if (selectedTab == ShopTab.COSTUME) Color.Black else Color(0xFFC9C4CE),
                modifier = Modifier.clickable { onTabSelected(ShopTab.COSTUME) }
            )
            Text(
                text = "|",
                style = DitoCustomTextStyles.titleDLarge,
                color = Color.Black
            )
            Text(
                text = "배경",
                style = DitoCustomTextStyles.titleDLarge,
                color = if (selectedTab == ShopTab.BACKGROUND) Color.Black else Color(0xFFC9C4CE),
                modifier = Modifier.clickable { onTabSelected(ShopTab.BACKGROUND) }
            )
        }

        // 오른쪽 정렬된 잔액 표시
        CoinDisplay(
            coins = coins,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun CoinDisplay(
    coins: Int,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        label = "coin_scale"
    )
    val lemonRotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    scope.launch {
                        for (i in 0..1) {
                            lemonRotation.animateTo(targetValue = -15f, animationSpec = tween(75))
                            lemonRotation.animateTo(targetValue = 15f, animationSpec = tween(75))
                        }
                        lemonRotation.animateTo(targetValue = 0f, animationSpec = tween(75))
                    }
                }
            )
            .width(81.dp)
            .height(36.dp)
            .hardShadow(DitoHardShadow.ButtonSmall.copy(cornerRadius = 48.dp))
            .background(Primary, CircleShape)
            .border(2.dp, Color.Black, CircleShape)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        // Removed horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) { // Use a Box to manage internal alignment
            Text(
                text = coins.toString(),
                style = DitoCustomTextStyles.titleDMedium,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterStart) // Align text to start
            )
            Image(
                painter = painterResource(id = R.drawable.lemon),
                contentDescription = "Coin",
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        rotationZ = lemonRotation.value
                    }
                    .align(Alignment.CenterEnd), // Align image to end
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun ItemGrid(
    items: List<ShopItem>,
    canPaginate: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onPurchase: (ShopItem) -> Unit
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
            ShopItemCard(
                item = item,
                onPurchase = { onPurchase(item) }
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
private fun ShopItemCard(
    item: ShopItem,
    onPurchase: () -> Unit
) {

    Box(modifier = Modifier
        .width(120.67.dp)
        .height(139.dp)
    ) {
        Column( // This is the card itself
            modifier = Modifier
                .fillMaxSize() // Fill the parent Box
                .background(Color.White, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box( // This is the image area
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF5EBD2)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(70.dp)
                        .then(if (item.isPurchased) Modifier.alpha(0.3f) else Modifier), // Revert to translucent image
                    contentScale = ContentScale.Fit,
                    onSuccess = {
                        android.util.Log.d("ShopItemCard", "Image loaded successfully: ${item.imageUrl}")
                    },
                    onError = { error ->
                        android.util.Log.e("ShopItemCard", "Image loading failed for ${item.imageUrl}: ${error.result.throwable?.message}")
                    }
                )

                // Removed the semi-transparent light gray overlay box here
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (item.isPurchased) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .background(Color.Black, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "보유중",
                                    style = DitoCustomTextStyles.titleDSmall,
                                    color = Color.White
                                )
                            }            } else {
                var isPressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.85f else 1f,
                    label = "purchase_button_scale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .background(Primary, CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                },
                                onTap = {
                                    onPurchase()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.price.toString(),
                            style = DitoCustomTextStyles.titleDSmall,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(
                            painter = painterResource(id = R.drawable.lemon),
                            contentDescription = "Coin",
                            modifier = Modifier.size(17.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

        if (item.isPurchased) {
            // Owned stamp icon
            Image(
                painter = painterResource(id = R.drawable.owned), // Use owned.png
                contentDescription = "Owned stamp",
                modifier = Modifier
                    .size(60.dp) // Larger size
                    .align(Alignment.TopStart) // Top-left corner
                    .padding(start = 0.dp, top = 4.dp), // Keep inside card
                contentScale = ContentScale.Fit
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ShopScreenPreview() {
    ShopScreen(onBackClick = {})
}