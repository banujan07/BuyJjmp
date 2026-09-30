package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpOrange

@Composable
fun BuyJumpLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    showText: Boolean = true,
    tagline: String? = null,
    isLightBackground: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Official BuyJump Emblem with dynamic jumping arrow / lightning swoosh
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.28f))
                .background(
                    Brush.linearGradient(
                        colors = listOf(BuyJumpBlue, Color(0xFF0F172A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_app_icon),
                contentDescription = "BuyJump Logo Icon",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(size * 0.28f))
            )
        }

        if (showText) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BUY",
                        fontSize = (size.value * 0.52f).sp,
                        fontWeight = FontWeight.Black,
                        color = if (isLightBackground) BuyJumpBlue else Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "JUMP",
                        fontSize = (size.value * 0.52f).sp,
                        fontWeight = FontWeight.Black,
                        color = BuyJumpOrange,
                        letterSpacing = (-0.5).sp
                    )
                }
                if (tagline != null) {
                    Text(
                        text = tagline.uppercase(),
                        fontSize = (size.value * 0.22f).sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLightBackground) Color(0xFF64748B) else Color(0xFF94A3B8),
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}
