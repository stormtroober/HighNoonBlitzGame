package com.ds.highnoonblitz.view.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MenuButton(buttonText: String, buttonAction: () -> Unit) {
    Button(modifier = Modifier
            .padding(16.dp)
            .padding(bottom = 5.dp)
            .fillMaxWidth(),
            onClick = buttonAction){
        Text(text = buttonText)
    }

}