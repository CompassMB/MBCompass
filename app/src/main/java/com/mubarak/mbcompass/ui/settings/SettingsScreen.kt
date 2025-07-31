// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.ui.theme.MBCompassTheme
import com.mubarak.mbcompass.ui.theme.iconDefaultSize
import com.mubarak.mbcompass.ui.theme.spacingLarge
import com.mubarak.mbcompass.ui.theme.spacingMedium
import com.mubarak.mbcompass.ui.theme.spacingSmall
import com.mubarak.mbcompass.utils.Const.APP_PAGE
import com.mubarak.mbcompass.utils.Const.AUTHOR_EMAIL
import com.mubarak.mbcompass.utils.Const.LICENSE_PAGE
import com.mubarak.mbcompass.utils.ThemeConfig

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {}
    )
    SettingsScreen(
        uiState = uiState,
        onBackClicked = onBack,
        onThemeOptionClicked = viewModel::setTheme,
        onAuthorPageClicked = {
            sendMail(context, launcher)
        },
        onLicensesClicked = {
            uriHandler.openUri(LICENSE_PAGE)
        },
        onSourceClicked = {
            uriHandler.openUri(APP_PAGE)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsViewModel.SettingsUiState,
    onBackClicked: () -> Unit,
    onThemeOptionClicked: (String) -> Unit,
    onAuthorPageClicked: () -> Unit,
    onLicensesClicked: () -> Unit,
    onSourceClicked: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        var isThemeDialogVisible by remember { mutableStateOf(false) }
        SettingsList(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            onThemeItemClicked = { isThemeDialogVisible = true },
            onLicensesClicked = onLicensesClicked,
            onAuthorPageClicked = onAuthorPageClicked,
            onSourceClicked = onSourceClicked
        )
        ThemeDialog(
            isDialogVisible = isThemeDialogVisible,
            onDismissRequest = { isThemeDialogVisible = false },
            currentSelection = uiState.theme,
            options = uiState.themeDialogOptions,
            onOptionClicked = onThemeOptionClicked,
        )
    }
}

@Composable
private fun SettingsList(
    modifier: Modifier = Modifier,
    uiState: SettingsViewModel.SettingsUiState,
    onThemeItemClicked: () -> Unit,
    onAuthorPageClicked: () -> Unit = {},
    onLicensesClicked: () -> Unit = {},
    onSourceClicked: () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        val listState = rememberLazyListState()
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            state = listState,
        ) {
            item(key = "__displayHeader") {
                Text(
                    text = stringResource(R.string.display),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.requiredSize(spacingMedium))
            }
            item(key = "__themeItem") {
                SettingsItem(
                    title = stringResource(R.string.theme),
                    icon = R.drawable.theme_icon24px,
                    subtitle = getThemeName(option = uiState.theme),
                    onClick = onThemeItemClicked,
                )
            }
            item(key = "__aboutHeader") {
                Spacer(modifier = Modifier.requiredSize(spacingMedium))
                Text(
                    text = stringResource(R.string.settings_about),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.requiredSize(spacingMedium))
            }
            item(key = "__aboutItem") {
                SettingsItem(
                    title = stringResource(R.string.author),
                    icon = R.drawable.person_icon24px,
                    subtitle = stringResource(R.string.developer),
                    onClick = onAuthorPageClicked,
                )
            }
            item(key = "__licenseItem") {
                SettingsItem(
                    title = stringResource(R.string.licenses),
                    icon = R.drawable.license_icon24px,
                    subtitle = stringResource(R.string.app_license),
                    onClick = onLicensesClicked,
                )
            }
            item(key = "__sourcecodeItem") {
                SettingsItem(
                    title = stringResource(R.string.source_code),
                    icon = R.drawable.code_icon24px,
                    subtitle = stringResource(R.string.github),
                    onClick = onSourceClicked,
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    @DrawableRes icon: Int? = null,
) {

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onClick)
                .padding(vertical = spacingMedium)
                .padding(start = spacingLarge),
        ) {

            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.requiredSize(iconDefaultSize),
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeDialog(
    isDialogVisible: Boolean,
    onDismissRequest: () -> Unit,
    currentSelection: String,
    options: List<String>,
    onOptionClicked: (String) -> Unit,
) {
    if (isDialogVisible) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(text = stringResource(R.string.choose_theme))
            },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacingSmall),
                    modifier = Modifier.verticalScroll(scrollState),
                ) {
                    for (option in options) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacingMedium),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOptionClicked(option)
                                    onDismissRequest()
                                }
                                .padding(vertical = spacingSmall),
                        ) {
                            RadioButton(
                                selected = option == currentSelection,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.tertiary,
                                ),
                            )
                            Text(
                                text = getThemeName(option = option),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onDismissRequest,
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
fun getThemeName(option: String): String {
    return when (option) {
        ThemeConfig.FOLLOW_SYSTEM.prefName -> stringResource(R.string.sys_default)
        ThemeConfig.LIGHT.prefName -> stringResource(R.string.light_theme)
        ThemeConfig.DARK.prefName -> stringResource(R.string.dark_theme)
        else -> throw IllegalArgumentException("Unknown theme")
    }
}

fun sendMail(context: Context, launcher: ActivityResultLauncher<Intent>) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:$AUTHOR_EMAIL".toUri()
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        launcher.launch(intent)
    } else {
        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showSystemUi = false, showBackground = false, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    MBCompassTheme {
        SettingsScreen(
            uiState = SettingsViewModel.SettingsUiState(),
            onBackClicked = {},
            onThemeOptionClicked = {},
            onLicensesClicked = {},
            onAuthorPageClicked = {},
            onSourceClicked = {}
        )
    }
}