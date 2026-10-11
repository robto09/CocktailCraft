package com.cocktailcraft.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.theme.LocalReducedMotion
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.offline_you_are_offline

/**
 * A banner shown while the device has no network connection. Cached
 * cocktails keep rendering underneath it. It is a polite live region, so
 * TalkBack announces it when the connection drops. Reduce Motion keeps the
 * fade but drops the expand/shrink.
 *
 * @param isOffline Whether the network is currently down
 */
@Composable
fun OfflineModeIndicator(isOffline: Boolean) {
    val reducedMotion = LocalReducedMotion.current
    AnimatedVisibility(
        visible = isOffline,
        enter = if (reducedMotion) fadeIn() else fadeIn() + expandVertically(),
        exit = if (reducedMotion) fadeOut() else fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.error)
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null, // the message says it all
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Text(
                    text = stringResource(MR.strings.offline_you_are_offline),
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
