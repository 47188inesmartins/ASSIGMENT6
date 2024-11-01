package com.example.assignment4.localStore

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface OfflineItems {
    val itemsRepository: ItemsRepository
}


class AppDataContainer(private val context: Context) : OfflineItems {
    /**
     * Implementation for [ItemsRepository]
     */
    override val itemsRepository: ItemsRepository by lazy {
        OfflineItemsRepository(InventoryDatabase.getDatabase(context).itemDao())
    }
}