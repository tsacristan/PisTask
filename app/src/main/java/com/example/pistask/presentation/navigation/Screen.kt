package com.example.pistask.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pistask.R
import com.example.pistask.presentation.theme.Marron
import com.example.pistask.presentation.theme.VertPistacheFoncee
import com.example.pistask.presentation.theme.PisTaskTheme

sealed class Screen(val route : String, val title : String, @param:DrawableRes val iconRes : Int){
    object Tache : Screen("taches", "TÂCHES", R.drawable.leaf)
    object Jardin : Screen("jardin", "JARDIN", R.drawable.birdhouse)
}

@Composable
fun MyBottomNavigationBar(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    centerButtonSizeDp: Int = 110,
    centerIconSizeDp: Int = 48,
    centerOnClick: () -> Unit = {},
    centerVerticalOffsetDp: Int? = null
) {
    val barHeight = 84.dp
    val centerSize = centerButtonSizeDp.dp
    val defaultOffset = -centerSize / 2
    val centerOffset = centerVerticalOffsetDp?.dp ?: defaultOffset

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(barHeight + centerSize / 2)
        .navigationBarsPadding()) {

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Marron)
                    .padding(horizontal = 16.dp),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    items.forEach { screen ->
                        val selected = currentRoute == screen.route

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = screen.iconRes),
                                    contentDescription = screen.title,
                                    tint = if (selected) VertPistacheFoncee else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(if (selected) 32.dp else 28.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    color = if (selected) VertPistacheFoncee else MaterialTheme.colorScheme.outline,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = selected,
                            onClick = { onItemClick(screen.route) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = centerOffset)) {
            PistacheCenterButton(
                modifier = Modifier,
                sizeDp = centerButtonSizeDp,
                iconSizeDp = centerIconSizeDp,
                onClick = centerOnClick
            )
        }
    }
}

@Composable
fun PistacheCenterButton(
    modifier: Modifier = Modifier,
    sizeDp: Int = 150,
    iconSizeDp: Int = 36,
    onClick: () -> Unit = {}
) {
    val size = sizeDp.dp
    val iconSize = iconSizeDp.dp

    Box(
        modifier = modifier
            .size(size)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.pistache),
            contentDescription = "Pistache",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size)
        )

        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Ajouter",
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 140)
@Composable
fun MyBottomNavigationBarPreview() {
    PisTaskTheme(dynamicColor = false) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(220.dp), contentAlignment = Alignment.BottomCenter) {
            MyBottomNavigationBar(
                items = listOf(Screen.Tache, Screen.Jardin),
                currentRoute = Screen.Tache.route,
                onItemClick = { /* route -> */ },
                centerVerticalOffsetDp = -24
            )
        }
    }
}
