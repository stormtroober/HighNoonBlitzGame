package com.ds.highnoonblitz.view.components

import android.content.res.Resources
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.ds.highnoonblitz.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(resources: Resources) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val colors =
        TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)

    CenterAlignedTopAppBar(
        title = {
                Text(text = "\uD83D\uDD2B ${resources.getString(R.string.app_name)} ⌛",
                    style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold))
                },
        scrollBehavior = scrollBehavior,
        colors = colors)
}