package com.curiosityengine.app.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.curiosityengine.app.ui.theme.BackgroundDeep
import com.curiosityengine.app.ui.theme.BrandGold
import com.curiosityengine.app.ui.theme.DmSerifDisplayFamily
import com.curiosityengine.app.ui.theme.SurfaceCard
import com.curiosityengine.app.ui.theme.SurfaceElevated
import com.curiosityengine.app.ui.theme.TextMuted
import com.curiosityengine.app.ui.theme.TextPrimary
import com.curiosityengine.app.ui.theme.TextSecondary
import com.curiosityengine.app.ui.theme.categoryColor

private data class CategoryItem(val name: String, val emoji: String)

private val categories = listOf(
    CategoryItem("Science", "\uD83D\uDD2C"),
    CategoryItem("Nature", "\uD83C\uDF3F"),
    CategoryItem("History", "\uD83D\uDCDC"),
    CategoryItem("Psychology", "\uD83E\uDDE0"),
    CategoryItem("Technology", "\uD83D\uDCBB"),
    CategoryItem("Art & Culture", "\uD83C\uDFA8"),
    CategoryItem("Economics", "\uD83D\uDCCA"),
    CategoryItem("Philosophy", "\uD83E\uDD14"),
    CategoryItem("Surprise Me", "\u2728"),
)

private val dailyTargetOptions = listOf(5, 10, 15, 30, 60)

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    userId: String,
    onComplete: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* granted or denied — proceed either way */ }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundDeep,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar: back button + step dots ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.currentStep > 0) {
                    IconButton(onClick = { viewModel.prevStep() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                StepIndicator(currentStep = state.currentStep, totalSteps = 4)

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp))
            }

            // ── Step content ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
            ) {
                when (state.currentStep) {
                    0 -> StepWelcome(onNext = { viewModel.nextStep() })
                    1 -> StepCategories(
                        selected = state.selectedCategories,
                        onToggle = { viewModel.toggleCategory(it) },
                        onNext = { viewModel.nextStep() },
                    )
                    2 -> StepDailyTarget(
                        currentTarget = state.dailyTargetMinutes,
                        onTargetSelected = { viewModel.setDailyTarget(it) },
                        onNext = { viewModel.nextStep() },
                    )
                    3 -> StepNotifications(
                        notificationEnabled = state.notificationEnabled,
                        onToggleNotification = { viewModel.setNotificationEnabled(it) },
                        hour = state.notificationHour,
                        minute = state.notificationMinute,
                        onTimeChange = { h, m -> viewModel.setNotificationTime(h, m) },
                        isLoading = state.isLoading,
                        onStartLearning = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            viewModel.completeOnboarding(userId, onComplete)
                        },
                    )
                }
            }
        }
    }
}

// ── Step indicator ────────────────────────────────────────────────────────────

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val isCurrent = index == currentStep
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 10.dp else 7.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent) BrandGold else TextMuted),
            )
        }
    }
}

// ── Step 0 — Welcome ─────────────────────────────────────────────────────────

@Composable
private fun StepWelcome(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "\uD83E\uDDE0",
            fontSize = 72.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Welcome to\nCuriosity Engine",
            fontFamily = DmSerifDisplayFamily,
            fontSize = 36.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 44.sp,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Discover a new topic every day. From science to philosophy, we'll deliver a personalised lesson in just a few minutes.",
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
        Spacer(modifier = Modifier.height(48.dp))
        PrimaryButton(text = "Get Started", onClick = onNext)
    }
}

// ── Step 1 — Categories ───────────────────────────────────────────────────────

@Composable
private fun StepCategories(
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "What are you curious about?",
            fontFamily = DmSerifDisplayFamily,
            fontSize = 28.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pick your favourite topics. You can change these later.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(28.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(categories) { item ->
                val isSelected = item.name in selected
                val chipColor = if (isSelected) categoryColor(item.name) else SurfaceCard
                val borderColor = if (isSelected) categoryColor(item.name) else SurfaceElevated

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(chipColor)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { onToggle(item.name) }
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(text = item.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.name,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else TextSecondary,
                        textAlign = TextAlign.Center,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(
            text = "Continue",
            onClick = onNext,
            enabled = selected.isNotEmpty(),
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Step 2 — Daily Target ─────────────────────────────────────────────────────

@Composable
private fun StepDailyTarget(
    currentTarget: Int,
    onTargetSelected: (Int) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "How much time can you dedicate?",
            fontFamily = DmSerifDisplayFamily,
            fontSize = 28.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Even 5 minutes a day adds up to something remarkable.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "$currentTarget min",
            fontFamily = DmSerifDisplayFamily,
            fontSize = 52.sp,
            color = BrandGold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "per day",
            fontSize = 14.sp,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Segmented button row for options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            dailyTargetOptions.forEach { option ->
                val isSelected = option == currentTarget
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BrandGold else SurfaceCard)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) BrandGold else SurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .clickable { onTargetSelected(option) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$option",
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF0F1E35) else TextSecondary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "minutes",
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(48.dp))
        PrimaryButton(text = "Continue", onClick = onNext)
    }
}

// ── Step 3 — Notifications ────────────────────────────────────────────────────

@Composable
private fun StepNotifications(
    notificationEnabled: Boolean,
    onToggleNotification: (Boolean) -> Unit,
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit,
    isLoading: Boolean,
    onStartLearning: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "\uD83C\uDF89",
            fontSize = 56.sp,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "You're all set!",
            fontFamily = DmSerifDisplayFamily,
            fontSize = 32.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Get a daily nudge to keep your curiosity alive.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Notification toggle
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SurfaceCard,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Daily reminder",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                    )
                    Text(
                        text = "We'll remind you to learn something new",
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                }
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = onToggleNotification,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0F1E35),
                        checkedTrackColor = BrandGold,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceElevated,
                    ),
                )
            }
        }

        if (notificationEnabled) {
            Spacer(modifier = Modifier.height(16.dp))

            // Hour selector
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceCard,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Reminder time",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                        )
                        Text(
                            text = "%02d:%02d".format(hour, minute),
                            fontSize = 20.sp,
                            fontFamily = DmSerifDisplayFamily,
                            color = BrandGold,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Hour: $hour", fontSize = 12.sp, color = TextSecondary)
                    Slider(
                        value = hour.toFloat(),
                        onValueChange = { onTimeChange(it.toInt(), minute) },
                        valueRange = 0f..23f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = BrandGold,
                            activeTrackColor = BrandGold,
                            inactiveTrackColor = SurfaceElevated,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Minute: $minute", fontSize = 12.sp, color = TextSecondary)
                    Slider(
                        value = minute.toFloat(),
                        onValueChange = { onTimeChange(hour, it.toInt()) },
                        valueRange = 0f..59f,
                        steps = 58,
                        colors = SliderDefaults.colors(
                            thumbColor = BrandGold,
                            activeTrackColor = BrandGold,
                            inactiveTrackColor = SurfaceElevated,
                        ),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        PrimaryButton(
            text = if (isLoading) "Setting up…" else "Start Learning",
            onClick = onStartLearning,
            enabled = !isLoading,
        )
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandGold,
            contentColor = Color(0xFF0F1E35),
            disabledContainerColor = BrandGold.copy(alpha = 0.4f),
            disabledContentColor = Color(0xFF0F1E35).copy(alpha = 0.4f),
        ),
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
