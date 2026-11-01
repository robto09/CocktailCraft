package com.cocktailcraft.data.repository

/** When the catalog repositories refetch what the database already holds. */
internal object CatalogRefreshPolicy {
    /**
     * How long a fetched category page or cocktail counts as fresh. Within
     * it a non-forced refresh is skipped and the stored rows are served as
     * they are.
     */
    const val FRESHNESS_MS: Long = 5 * 60 * 1000L

    /** Message of the Result.Error a refresh returns when it skipped the network because the app is offline. */
    const val OFFLINE_MESSAGE = "Offline: showing cached cocktails"
}
