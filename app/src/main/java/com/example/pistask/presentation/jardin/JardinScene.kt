package com.example.pistask.presentation.jardin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pistask.R
import com.example.pistask.presentation.theme.*
import com.example.pistask.util.StorageHelper

@Composable
fun JardinScene(
    initialPoints: Int,
    onPointsChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    var growth by remember { mutableStateOf(StorageHelper.loadGrowth(context)) }
    
    val growthPercentage = (growth * 100).toInt()
    
    // Logique d'évo de la plante
    val plantScale by animateFloatAsState(targetValue = 0.6f + (growth * 0.9f), label = "plantScale")
    
    // Montrer les assets selon l'étape de pousse
    val plantRes = when {
        growth < 0.17f -> R.drawable.pousse_1
        growth < 0.34f -> R.drawable.pousse_2
        growth < 0.51f -> R.drawable.pousse_3
        growth < 0.68f -> R.drawable.pousse_4
        growth < 0.85f -> R.drawable.pousse_5
        else -> R.drawable.pousse_6
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BleuPastel)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = Color.White,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.droplets),
                        contentDescription = "Points",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(BleuTurquoise)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$initialPoints",
                        color = BleuTurquoise,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Image(
                    painter = painterResource(id = R.drawable.pistask),
                    contentDescription = "Logo",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "Pïstask", 
                    color = VertPistacheFoncee, 
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Progès de pousse
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CROISSANCE",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$growthPercentage%",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // espace plante
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 180.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // terre
            Box(
                modifier = Modifier
                    .size(140.dp, 45.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF8B4513))
                    .align(Alignment.BottomCenter)
                    .offset(y = 15.dp)
            )
            
            // La plante
            Image(
                painter = painterResource(id = plantRes),
                contentDescription = "Plant",
                modifier = Modifier
                    .size(180.dp)
                    .graphicsLayer(
                        scaleX = plantScale,
                        scaleY = plantScale,
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    )
                    .align(Alignment.BottomCenter)
                    .offset(y = (-10).dp)
            )
        }

        // interaction utilisateur
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .align(Alignment.BottomCenter)
        ) {
            // Green ground surface
            Surface(
                color = VertPistacheFoncee,
                shape = RoundedCornerShape(topStart = 150.dp, topEnd = 150.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Button(
                        onClick = {
                            if (initialPoints >= 50 && growth < 1.0f) {
                                val newPoints = initialPoints - 50
                                val newGrowth = (growth + 0.05f).coerceAtMost(1.0f)
                                growth = newGrowth
                                onPointsChanged(newPoints)
                                StorageHelper.savePoints(context, newPoints)
                                StorageHelper.saveGrowth(context, newGrowth)
                            }
                        },
                        enabled = initialPoints >= 50 && growth < 1.0f,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = BleuTurquoise,
                            disabledContainerColor = Color.White.copy(alpha = 0.5f),
                            disabledContentColor = BleuTurquoise.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(30.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth(0.7f),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.droplets),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(BleuTurquoise)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ARROSER (-50 pts)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (growth >= 1.0f) "PLANTE AU MAXIMUM !" else "DÉPENSE TES GOUTTES POUR FAIRE POUSSER LA PLANTE",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
