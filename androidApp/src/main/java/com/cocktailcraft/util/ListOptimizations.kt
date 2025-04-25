package com.cocktailcraft.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Utility functions for optimizing list rendering performance.
 */
object ListOptimizations {
    
    /**
     * Calculate a stable key for a list item.
     * This helps Compose efficiently update only changed items.
     * 
     * @param prefix A prefix to ensure uniqueness across different lists
     * @param id The unique identifier of the item
     * @return A stable string key
     */
    fun itemKey(prefix: String, id: String): String {
        return "${prefix}_$id"
    }
    
    /**
     * Calculate a stable key for a list item with an index.
     * 
     * @param prefix A prefix to ensure uniqueness across different lists
     * @param id The unique identifier of the item
     * @param index The index of the item in the list
     * @return A stable string key
     */
    fun itemKeyWithIndex(prefix: String, id: String, index: Int): String {
        return "${prefix}_${id}_$index"
    }
    
    /**
     * Detect when a lazy list has reached its end to implement pagination.
     * 
     * @param listState The LazyListState to monitor
     * @param buffer Number of items from the end to trigger the load (default: 3)
     * @param onLoadMore Callback to invoke when more items should be loaded
     */
    @Composable
    fun LazyListState.OnBottomReached(
        buffer: Int = 3,
        onLoadMore: () -> Unit
    ) {
        // Create a derived state that checks if we're near the end of the list
        val shouldLoadMore = remember {
            derivedStateOf {
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                    ?: return@derivedStateOf false
                
                // Check if we're at the end of the list minus the buffer
                lastVisibleItem.index >= layoutInfo.totalItemsCount - 1 - buffer
            }
        }
        
        // Use LaunchedEffect to react to changes in the derived state
        LaunchedEffect(shouldLoadMore) {
            snapshotFlow { shouldLoadMore.value }
                .distinctUntilChanged()
                .filter { it }
                .collect {
                    onLoadMore()
                }
        }
    }
    
    /**
     * Detect when a lazy list has been scrolled to implement features like
     * hiding/showing UI elements based on scroll position.
     * 
     * @param listState The LazyListState to monitor
     * @param threshold Scroll position threshold to trigger the callback
     * @param onScrollPastThreshold Callback with a boolean indicating if we're past the threshold
     */
    @Composable
    fun LazyListState.OnScrollPastThreshold(
        threshold: Int = 0,
        onScrollPastThreshold: (Boolean) -> Unit
    ) {
        // Create a derived state that checks if we've scrolled past the threshold
        val isPastThreshold = remember {
            derivedStateOf {
                firstVisibleItemIndex > threshold || 
                (firstVisibleItemIndex == threshold && firstVisibleItemScrollOffset > 0)
            }
        }
        
        // Use LaunchedEffect to react to changes in the derived state
        LaunchedEffect(isPastThreshold) {
            snapshotFlow { isPastThreshold.value }
                .distinctUntilChanged()
                .collect {
                    onScrollPastThreshold(it)
                }
        }
    }
}
