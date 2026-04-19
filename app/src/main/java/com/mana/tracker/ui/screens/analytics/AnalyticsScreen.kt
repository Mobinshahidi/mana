package com.mana.tracker.ui.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mana.tracker.R
import com.mana.tracker.core.theme.ManaWarmAccent

@Composable
fun AnalyticsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.analytics_title), style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.spending_trend))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val step = size.width / 6f
                    val points = listOf(0.8f, 0.6f, 0.7f, 0.4f, 0.5f, 0.3f)
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = ManaWarmAccent,
                            start = Offset(i * step, size.height * points[i]),
                            end = Offset((i + 1) * step, size.height * points[i + 1]),
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.daily_heatmap))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val cols = 14
                    val rows = 4
                    val w = size.width / cols
                    val h = size.height / rows
                    for (x in 0 until cols) {
                        for (y in 0 until rows) {
                            val alpha = ((x + y) % 5 + 1) / 5f
                            drawRect(
                                color = ManaWarmAccent.copy(alpha = alpha),
                                topLeft = Offset(x * w + 2, y * h + 2),
                                size = androidx.compose.ui.geometry.Size(w - 4, h - 4)
                            )
                        }
                    }
                }
            }
        }
    }
}
