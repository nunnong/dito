package com.dito.app.feature.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.dito.app.R
import com.dito.app.core.ui.component.BottomTab
import com.dito.app.core.ui.component.DitoBottomAppBar
import com.dito.app.core.ui.designsystem.*

/** 상점 화면 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onBackClick: () -> Unit = {},
    onNavigateHome: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(ShopTab.COSTUME) }
    var userCoins by remember { mutableStateOf(100) }
    val ownedItemIds = remember { mutableStateOf(setOf("item_1")) }

    var showDialog by remember { mutableStateOf(false) }
    var showInsufficientCoinsDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ShopItem?>(null) }

    if (showDialog) {
        ShopConfirmDialog(
            onConfirm = {
                selectedItem?.let { item ->
                    if (userCoins >= item.price) {
                        userCoins -= item.price
                        ownedItemIds.value = ownedItemIds.value + item.id
                    }
                }
                showDialog = false
                selectedItem = null
            },
            onDismiss = {
                showDialog = false
                selectedItem = null
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

    Scaffold(
        bottomBar = {
            DitoBottomAppBar(
                selectedTab = BottomTab.HOME,
                onTabSelected = { tab ->
                    if (tab == BottomTab.HOME) {
                        onNavigateHome()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            ShopHeader(onBackClick = onBackClick)

            TabAndCoinSection(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                coins = userCoins
            )

            ItemGrid(
                selectedTab = selectedTab,
                ownedItemIds = ownedItemIds.value,
                userCoins = userCoins,
                onPurchase = { item ->
                    if (userCoins >= item.price) {
                        selectedItem = item
                        showDialog = true
                    } else {
                        showInsufficientCoinsDialog = true
                    }
                },
                contentPadding = innerPadding
            )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(end = 23.dp),
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
        CoinDisplay(coins = coins)
    }
}

@Composable
private fun CoinDisplay(coins: Int) {
    Row(
        modifier = Modifier
            .width(81.dp)
            .height(36.dp)
            .hardShadow(DitoHardShadow.ButtonSmall.copy(cornerRadius = 48.dp))
            .background(Primary, CircleShape)
            .border(2.dp, Color.Black, CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center // Center the group
    ) {
        Text(
            text = coins.toString(),
            style = DitoCustomTextStyles.titleDLarge, // 22sp
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(4.dp)) // Small space between text and image
        Image(
            painter = painterResource(id = R.drawable.lemon),
            contentDescription = "Coin",
            modifier = Modifier.size(28.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ItemGrid(
    selectedTab: ShopTab,
    ownedItemIds: Set<String>,
    userCoins: Int,
    onPurchase: (ShopItem) -> Unit,
    contentPadding: PaddingValues
) {
    val items = remember(selectedTab) {
        List(12) { index ->
            ShopItem(
                id = "item_${index + 1}",
                name = if (selectedTab == ShopTab.COSTUME) "의상 ${index + 1}" else "배경 ${index + 1}",
                price = 100,
                imageRes = R.drawable.dito
            )
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 10.dp,
            bottom = contentPadding.calculateBottomPadding() + 10.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(items) { item ->
            ShopItemCard(
                item = item,
                isOwned = ownedItemIds.contains(item.id),
                canAfford = userCoins >= item.price,
                onPurchase = { onPurchase(item) }
            )
        }
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    isOwned: Boolean,
    canAfford: Boolean,
    onPurchase: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.67.dp)
            .height(139.dp)
            .background(Color.White, RoundedCornerShape(4.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .background(Color(0xFFF5EBD2)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                modifier = Modifier.size(70.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isOwned) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(Primary, CircleShape)
                    .border(1.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "보유중",
                    style = DitoCustomTextStyles.titleDSmall,
                    color = Color.Black
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(Color.Black, CircleShape)
                    .clickable { onPurchase() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.price.toString(),
                        style = DitoCustomTextStyles.titleDSmall,
                        color = Color.White
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
}

enum class ShopTab {
    COSTUME,
    BACKGROUND
}

data class ShopItem(
    val id: String,
    val name: String,
    val price: Int,
    val imageRes: Int
)

@Preview(showBackground = true)
@Composable
fun ShopScreenPreview() {
    ShopScreen(onBackClick = {}, onNavigateHome = {})
}