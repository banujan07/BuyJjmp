package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BuyJumpAmber

@Composable
fun RatingStars(
    rating: Double,
    modifier: Modifier = Modifier,
    reviewCount: Int? = null,
    starSize: Dp = 14.dp,
    showNumeric: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val fullStars = rating.toInt().coerceIn(0, 5)
        val hasHalf = (rating - fullStars) >= 0.5 && fullStars < 5

        for (i in 1..fullStars) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = BuyJumpAmber,
                modifier = Modifier.size(starSize)
            )
        }
        if (hasHalf) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.StarHalf,
                contentDescription = null,
                tint = BuyJumpAmber,
                modifier = Modifier.size(starSize)
            )
        }
        val remaining = 5 - fullStars - (if (hasHalf) 1 else 0)
        for (i in 1..remaining) {
            Icon(
                imageVector = Icons.Default.StarBorder,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(starSize)
            )
        }

        if (showNumeric) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%.1f", rating),
                fontSize = (starSize.value * 0.9f).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (reviewCount != null) {
            Text(
                text = "($reviewCount)",
                fontSize = (starSize.value * 0.8f).sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
