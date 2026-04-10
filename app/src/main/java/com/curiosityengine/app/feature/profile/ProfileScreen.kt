package com.curiosityengine.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.curiosityengine.app.ui.components.CuriosityCard
import com.curiosityengine.app.ui.theme.BackgroundDeep
import com.curiosityengine.app.ui.theme.BrandGold
import com.curiosityengine.app.ui.theme.DmSerifDisplayFamily
import com.curiosityengine.app.ui.theme.ErrorRed
import com.curiosityengine.app.ui.theme.LatoFamily
import com.curiosityengine.app.ui.theme.SurfaceCard
import com.curiosityengine.app.ui.theme.SurfaceElevated
import com.curiosityengine.app.ui.theme.TextPrimary
import com.curiosityengine.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDeep,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDeep,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))

            // Avatar + name + email
            AvatarSection(
                displayName = uiState.displayName,
                email = uiState.email,
                avatarUrl = uiState.avatarUrl,
            )

            Spacer(Modifier.height(24.dp))

            // Stats row
            StatsRow(
                chainStreak = uiState.streak?.chainStreak ?: 0,
                totalLessons = uiState.totalLessonsCompleted,
                weeklyStreak = uiState.streak?.weeklyStreak ?: 0,
            )

            Spacer(Modifier.height(24.dp))

            // Achievements section
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            )

            AchievementsGrid(badges = uiState.badges)

            Spacer(Modifier.height(32.dp))

            // Sign Out button
            TextButton(
                onClick = {
                    viewModel.signOut()
                    onSignOut()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ErrorRed,
                ),
            ) {
                Text(
                    text = "Sign Out",
                    fontFamily = LatoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AvatarSection(
    displayName: String,
    email: String,
    avatarUrl: String?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .border(2.dp, BrandGold, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                )
            } else {
                // Initials on gold background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BrandGold),
                    contentAlignment = Alignment.Center,
                ) {
                    val initials = displayName
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .joinToString("") { it.first().uppercase() }
                        .ifEmpty { "?" }
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = DmSerifDisplayFamily,
                        ),
                        color = Color(0xFF0F1E35),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Text(
            text = displayName.ifEmpty { "Curious Mind" },
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
        )

        if (email.isNotBlank()) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun StatsRow(
    chainStreak: Int,
    totalLessons: Int,
    weeklyStreak: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            emoji = "🔥",
            value = "$chainStreak",
            label = "day streak",
        )
        StatCard(
            modifier = Modifier.weight(1f),
            emoji = "📚",
            value = "$totalLessons",
            label = "lessons",
        )
        StatCard(
            modifier = Modifier.weight(1f),
            emoji = "⚡",
            value = "$weeklyStreak",
            label = "this week",
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String,
) {
    CuriosityCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandGold,
                ),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AchievementsGrid(badges: List<Badge>) {
    // Non-lazy grid: badges list is small (fixed at 5 items)
    val rows = badges.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowBadges ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowBadges.forEach { badge ->
                    BadgeCard(
                        badge = badge,
                        modifier = Modifier.weight(1f),
                    )
                }
                // Fill remaining slot if odd number of badges in last row
                if (rowBadges.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BadgeCard(
    badge: Badge,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        CuriosityCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = badge.emoji,
                        fontSize = 32.sp,
                        modifier = if (!badge.isUnlocked) Modifier.then(
                            Modifier.background(Color.Transparent)
                        ) else Modifier,
                        color = if (badge.isUnlocked) Color.Unspecified
                        else Color.Unspecified.copy(alpha = 0.3f),
                    )
                    if (!badge.isUnlocked) {
                        // Greyed out overlay
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(SurfaceCard.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "🔒", fontSize = 16.sp)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (badge.isUnlocked) TextPrimary else TextSecondary,
                    textAlign = TextAlign.Center,
                    fontWeight = if (badge.isUnlocked) FontWeight.Bold else FontWeight.Normal,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (!badge.isUnlocked) {
            // Semi-transparent overlay on the whole card
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard.copy(alpha = 0.4f)),
            )
        }
    }
}
