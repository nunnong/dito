package com.dito.app.feature.closet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.dito.app.core.ui.designsystem.*

/** 옷장 화면 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosetScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(ClosetTab.BACKGROUND) }
    var appliedCostumeId by remember { mutableStateOf<String?>("costume_1") }
    var appliedBackgroundId by remember { mutableStateOf<String?>("background_1") }

    val ownedCostumes = remember {
        mutableStateOf(setOf("costume_1", "costume_2", "costume_3", "costume_4", "costume_5"))
    }
    val ownedBackgrounds = remember {
        mutableStateOf(setOf("background_1", "background_2", "background_3", "background_4", "background_5"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ClosetHeader(onBackClick = onBackClick)

        CharacterPreview(
            selectedTab = selectedTab,
            appliedCostumeId = appliedCostumeId,
            appliedBackgroundId = appliedBackgroundId
        )

        TabSection(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        ItemGrid(
            selectedTab = selectedTab,
            ownedCostumes = ownedCostumes.value,
            ownedBackgrounds = ownedBackgrounds.value,
            appliedCostumeId = appliedCostumeId,
            appliedBackgroundId = appliedBackgroundId,
            onApply = { itemId ->
                when (selectedTab) {
                    ClosetTab.COSTUME -> appliedCostumeId = itemId
                    ClosetTab.BACKGROUND -> appliedBackgroundId = itemId
                }
            }
        )
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
    appliedCostumeId: String?,
    appliedBackgroundId: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .background(Color(0xFFF5EBD2)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.dito),
            contentDescription = "Character Preview",
            modifier = Modifier.size(146.dp),
            contentScale = ContentScale.Fit
        )
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
    selectedTab: ClosetTab,
    ownedCostumes: Set<String>,
    ownedBackgrounds: Set<String>,
    appliedCostumeId: String?,
    appliedBackgroundId: String?,
    onApply: (String) -> Unit
) {
    val items = remember(selectedTab) {
        when (selectedTab) {
            ClosetTab.COSTUME -> {
                ownedCostumes.mapIndexed { index, id ->
                    ClosetItem(
                        id = id,
                        name = "의상 ${index + 1}",
                        imageRes = R.drawable.dito
                    )
                }
            }
            ClosetTab.BACKGROUND -> {
                ownedBackgrounds.mapIndexed { index, id ->
                    ClosetItem(
                        id = id,
                        name = "배경 ${index + 1}",
                        imageRes = R.drawable.dito
                    )
                }
            }
        }
    }

    val appliedItemId = when (selectedTab) {
        ClosetTab.COSTUME -> appliedCostumeId
        ClosetTab.BACKGROUND -> appliedBackgroundId
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 16.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            ClosetItemCard(
                item = item,
                isApplied = appliedItemId == item.id,
                onApply = { onApply(item.id) }
            )
        }
    }
}

@Composable
private fun ClosetItemCard(
    item: ClosetItem,
    isApplied: Boolean,
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
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                modifier = Modifier.size(70.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isApplied) {
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

enum class ClosetTab {
    COSTUME,
    BACKGROUND
}

data class ClosetItem(
    val id: String,
    val name: String,
    val imageRes: Int
)

@Preview(showBackground = true)
@Composable
fun ClosetScreenPreview() {
    ClosetScreen(onBackClick = {})
}