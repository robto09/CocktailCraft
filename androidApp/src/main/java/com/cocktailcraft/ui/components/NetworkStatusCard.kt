package com.cocktailcraft.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A reusable network status card component that displays the current network status.
 *
 * @param isNetworkAvailable Whether the network is available
 * @param modifier The modifier for the component
 * @param onlineColor The background color when network is available
 * @param offlineColor The background color when network is unavailable
 * @param textColor The color of the text
 * @param iconSize The size of the icon
 * @param fontSize The font size of the text
 * @param cornerRadius The corner radius of the card
 * @param elevation The elevation of the card
 * @param contentPadding The padding for the content
 */
@Composable
fun NetworkStatusCard(
    isNetworkAvailable: Boolean,
    modifier: Modifier = Modifier,
    onlineColor: Color = Color(0xFF4CAF50), // Green
    offlineColor: Color = Color(0xFFE57373), // Red
    textColor: Color = Color.White,
    iconSize: Int = 28,
    fontSize: Int = 18,
    cornerRadius: Int = 12,
    elevation: Int = 2,
    contentPadding: Int = 16
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNetworkAvailable) onlineColor else offlineColor
        ),
        shape = RoundedCornerShape(cornerRadius.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = contentPadding.dp, horizontal = contentPadding.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isNetworkAvailable)
                    Icons.Default.Wifi
                else
                    Icons.Default.WifiOff,
                contentDescription = if (isNetworkAvailable) "Online" else "Offline",
                tint = textColor,
                modifier = Modifier.size(iconSize.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (isNetworkAvailable) "Network Available" else "Network Unavailable",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize.sp
            )
        }
    }
}
