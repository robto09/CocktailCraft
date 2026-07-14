package com.cocktailcraft.android.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.theme.AppColors

/**
 * One infinite transition driving every shimmer placeholder beneath a loading
 * container. Hoist this at the container level and hand the value to each
 * [shimmerEffect] so N placeholders share one always-ticking animation
 * instead of running N of them.
 */
@Composable
fun rememberShimmerTranslate(): State<Float> {
    val transition = rememberInfiniteTransition(label = "shimmer")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                delayMillis = 300
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
}

/**
 * Shimmer effect driven by a hoisted [rememberShimmerTranslate] value. The
 * animated value is read only at draw time, so each tick invalidates drawing
 * rather than recomposing the placeholder tree.
 */
fun Modifier.shimmerEffect(translate: State<Float>) = composed {
    val shimmerColors = listOf(
        AppColors.Surface.copy(alpha = 0.6f),
        AppColors.Surface.copy(alpha = 0.2f),
        AppColors.Surface.copy(alpha = 0.6f)
    )

    drawBehind {
        drawRect(
            Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(10f, 10f),
                end = Offset(translate.value, translate.value)
            )
        )
    }
}

/**
 * A shimmer loading placeholder for a cocktail item
 */
@Composable
fun CocktailItemShimmer(
    translate: State<Float>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect(translate)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content placeholders
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Title placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(translate)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtitle placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(translate)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price placeholder
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect(translate)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Action buttons placeholders
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .shimmerEffect(translate)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .shimmerEffect(translate)
                )
            }
        }
    }
}

/**
 * A shimmer loading placeholder for a cocktail detail
 */
@Composable
fun CocktailDetailShimmer(
    modifier: Modifier = Modifier
) {
    // Screen-level loading container: one transition for all placeholders
    val translate = rememberShimmerTranslate()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp))
                .shimmerEffect(translate)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Title placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect(translate)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Rating placeholder
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(translate)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(translate)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Chips row
        Row {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmerEffect(translate)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmerEffect(translate)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Description placeholder
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(translate)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Ingredients title
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect(translate)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Ingredients list
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(translate)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
