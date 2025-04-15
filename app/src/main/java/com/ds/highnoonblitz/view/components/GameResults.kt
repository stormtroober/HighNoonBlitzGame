package com.ds.highnoonblitz.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameResults(gameResults: SnapshotStateList<Pair<String, Long>>) {
    Text(text =
    "Game results",
        modifier = Modifier
            .padding(top = 5.dp, bottom = 5.dp),
        style = MaterialTheme.typography.headlineMedium)
    val sortedResults = gameResults.sortedBy { it.second }
    LazyColumn(modifier = Modifier.height(100.dp)) {
        itemsIndexed(sortedResults) { index, device ->
            val deviceName = device.first
            val deviceTime = device.second
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                Text(text = "$deviceName: $deviceTime ms")
            }
        }
    }
}