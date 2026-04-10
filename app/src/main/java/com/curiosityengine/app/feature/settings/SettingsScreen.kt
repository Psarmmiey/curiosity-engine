package com.curiosityengine.app.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.curiosityengine.app.BuildConfig
import com.curiosityengine.app.util.Constants
import com.curiosityengine.app.ui.theme.BackgroundDeep
import com.curiosityengine.app.ui.theme.BrandGold
import com.curiosityengine.app.ui.theme.LatoFamily
import com.curiosityengine.app.ui.theme.SurfaceCard
import com.curiosityengine.app.ui.theme.SurfaceElevated
import com.curiosityengine.app.ui.theme.TextPrimary
import com.curiosityengine.app.ui.theme.TextSecondary

private val DAILY_TARGET_OPTIONS = listOf(5, 10, 15, 30, 60)
private val MODEL_OPTIONS = listOf(
    "gemini-2.5-pro" to "Gemini 2.5 Pro",
    "gemini-2.5-flash" to "Gemini 2.5 Flash",
    "gemini-2.5-flash-preview" to "Gemini Flash Preview",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDeep,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                actions = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp),
                            color = BrandGold,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                    } else {
                        TextButton(onClick = { viewModel.save() }) {
                            Text(
                                text = "Save",
                                color = BrandGold,
                                fontFamily = LatoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDeep,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Section: Learning
            item {
                SectionHeader(title = "Learning")
            }
            item {
                SectionCard {
                    DailyGoalRow(
                        selected = uiState.dailyTargetMinutes,
                        onSelect = { viewModel.setDailyTarget(it) },
                    )
                    HorizontalDivider(color = SurfaceElevated, modifier = Modifier.padding(vertical = 12.dp))
                    CategoriesRow(
                        selectedCategories = uiState.selectedCategories,
                        onToggle = { viewModel.toggleCategory(it) },
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // Section: AI Models
            item {
                SectionHeader(title = "AI Models")
            }
            item {
                SectionCard {
                    ModelDropdownRow(
                        selectedModel = uiState.preferredModel,
                        onSelect = { viewModel.setPreferredModel(it) },
                    )
                    HorizontalDivider(color = SurfaceElevated, modifier = Modifier.padding(vertical = 8.dp))
                    ToggleRow(
                        label = "Include images",
                        checked = uiState.enableImages,
                        onCheckedChange = { viewModel.toggleImages() },
                    )
                    HorizontalDivider(color = SurfaceElevated, modifier = Modifier.padding(vertical = 8.dp))
                    ToggleRow(
                        label = "Include videos",
                        checked = uiState.enableVideos,
                        onCheckedChange = { viewModel.toggleVideos() },
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // Section: Notifications
            item {
                SectionHeader(title = "Notifications")
            }
            item {
                SectionCard {
                    ToggleRow(
                        label = "Daily reminder",
                        checked = uiState.notificationEnabled,
                        onCheckedChange = { viewModel.setNotificationEnabled(it) },
                    )
                    if (uiState.notificationEnabled) {
                        HorizontalDivider(color = SurfaceElevated, modifier = Modifier.padding(vertical = 8.dp))
                        ReminderTimeRow(
                            hour = uiState.notificationHour,
                            minute = uiState.notificationMinute,
                            onTimeChange = { h, m -> viewModel.setNotificationTime(h, m) },
                        )
                    }
                    HorizontalDivider(color = SurfaceElevated, modifier = Modifier.padding(vertical = 8.dp))
                    ToggleRow(
                        label = "Email nudges",
                        checked = uiState.emailDailyNudge,
                        onCheckedChange = { viewModel.setEmailNudge(it) },
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // Section: About
            item {
                SectionHeader(title = "About")
            }
            item {
                val context = LocalContext.current
                SectionCard {
                    InfoRow(
                        label = "App version",
                        value = BuildConfig.VERSION_NAME,
                    )
                    HorizontalDivider(color = SurfaceElevated, modifier = Modifier.padding(vertical = 8.dp))
                    LinkRow(
                        label = "Privacy Policy",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://curiosityengine.app/privacy"))
                            context.startActivity(intent)
                        },
                    )
                    HorizontalDivider(color = SurfaceElevated, modifier = Modifier.padding(vertical = 8.dp))
                    LinkRow(
                        label = "Open Source Licenses",
                        onClick = {
                            // Could open OssLicensesMenuActivity if desired
                        },
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        ),
        color = BrandGold,
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp),
    )
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(16.dp),
    ) {
        Column { content() }
    }
}

@Composable
private fun DailyGoalRow(
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Column {
        Text(
            text = "Daily Goal",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            DAILY_TARGET_OPTIONS.forEach { minutes ->
                val isSelected = selected == minutes
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BrandGold else SurfaceElevated)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) BrandGold else SurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .clickable { onSelect(minutes) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${minutes}m",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color(0xFF0F1E35) else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoriesRow(
    selectedCategories: Set<String>,
    onToggle: (String) -> Unit,
) {
    Column {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Constants.CATEGORIES.forEach { cat ->
                val isSelected = selectedCategories.contains(cat)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) BrandGold.copy(alpha = 0.2f) else SurfaceElevated)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) BrandGold else SurfaceElevated,
                            shape = RoundedCornerShape(16.dp),
                        )
                        .clickable { onToggle(cat) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) BrandGold else TextSecondary,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelDropdownRow(
    selectedModel: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = MODEL_OPTIONS.firstOrNull { it.first == selectedModel }?.second
        ?: MODEL_OPTIONS.first().second

    Column {
        Text(
            text = "Lesson model",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = BrandGold,
                    unfocusedBorderColor = SurfaceElevated,
                    focusedContainerColor = SurfaceElevated,
                    unfocusedContainerColor = SurfaceElevated,
                    focusedTrailingIconColor = BrandGold,
                    unfocusedTrailingIconColor = TextSecondary,
                ),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = SurfaceCard,
            ) {
                MODEL_OPTIONS.forEach { (modelId, modelLabel) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = modelLabel,
                                color = if (selectedModel == modelId) BrandGold else TextPrimary,
                            )
                        },
                        onClick = {
                            onSelect(modelId)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF0F1E35),
                checkedTrackColor = BrandGold,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceElevated,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeRow(
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val timeLabel = "%02d:%02d".format(hour, minute)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showTimePicker = true }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Reminder time",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = BrandGold,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
    }

    if (showTimePicker) {
        val pickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true,
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(pickerState.hour, pickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK", color = BrandGold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            text = {
                TimePicker(state = pickerState)
            },
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}

@Composable
private fun LinkRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}
