package com.example.beerorderer.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * Repository for managing order persistence using JSON file storage
 */
class OrderRepository(private val context: Context) {

    private val gson = Gson()
    private val ordersFile = File(context.filesDir, "orders.json")

    /**
     * Save orders to JSON file
     */
    suspend fun saveOrders(orders: List<Beer>) = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(orders)
            FileWriter(ordersFile).use { writer ->
                writer.write(json)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Load orders from JSON file
     */
    suspend fun loadOrders(): List<Beer> = withContext(Dispatchers.IO) {
        try {
            if (ordersFile.exists()) {
                FileReader(ordersFile).use { reader ->
                    val type = object : TypeToken<List<Beer>>() {}.type
                    return@withContext gson.fromJson(reader, type) ?: emptyList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyList()
    }

    /**
     * Clear all orders from JSON file
     */
    suspend fun clearOrders() = withContext(Dispatchers.IO) {
        try {
            if (ordersFile.exists()) {
                ordersFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Check if there are any saved orders
     */
    suspend fun hasOrders(): Boolean = withContext(Dispatchers.IO) {
        ordersFile.exists() && ordersFile.length() > 0
    }
}

