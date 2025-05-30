package com.cocktailcraft.util

/**
 * Utility functions for list operations.
 * This is the KMP-compatible version without Compose-specific dependencies.
 */
object ListUtils {
    
    /**
     * Calculate a stable key for a list item.
     * This helps UI frameworks efficiently update only changed items.
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
     * Check if we need to load more items based on the current position in a list.
     * 
     * @param currentIndex The current visible item index
     * @param totalCount The total number of items in the list
     * @param buffer Number of items from the end to trigger the load
     * @return true if more items should be loaded
     */
    fun shouldLoadMore(currentIndex: Int, totalCount: Int, buffer: Int = 3): Boolean {
        return currentIndex >= totalCount - 1 - buffer
    }
}