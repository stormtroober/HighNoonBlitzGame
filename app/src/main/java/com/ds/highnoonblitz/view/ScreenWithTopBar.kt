package com.ds.highnoonblitz.view

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ds.highnoonblitz.ui.theme.HighNoonBlitzTheme
import com.ds.highnoonblitz.view.components.TopBar

@Composable
fun ScreenWithTopBar (activity: Activity, content: @Composable () -> Unit){
    HighNoonBlitzTheme {
        Surface (
            modifier = Modifier.fillMaxSize()
        ){
            Column{
                TopBar(resources = activity.resources)
                content()
            }
        }
    }
}