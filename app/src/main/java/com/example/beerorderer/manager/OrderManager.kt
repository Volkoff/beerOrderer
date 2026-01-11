package com.example.beerorderer.manager

import com.example.beerorderer.data.Beer

/**
 * Singleton manager for handling order operations
 * Works with OrderRepository for persistence
 */
object OrderManager {
    private val _orders = mutableListOf<Beer>()
    val orders: List<Beer> get() = _orders.toList()

    private var onOrderChangedListener: (() -> Unit)? = null

    fun addOrder(beer: Beer) {
        _orders.add(beer)
        onOrderChangedListener?.invoke()
    }

    fun removeOrder(beer: Beer) {
        _orders.remove(beer)
        onOrderChangedListener?.invoke()
    }

    fun clearOrders() {
        _orders.clear()
        onOrderChangedListener?.invoke()
    }

    fun setOrders(orders: List<Beer>) {
        _orders.clear()
        _orders.addAll(orders)
        onOrderChangedListener?.invoke()
    }

    fun getTotalPrice(): Double {
        return _orders.sumOf {
            it.price.replace("$", "").toDoubleOrNull() ?: 0.0
        }
    }

    fun getOrderCount(): Int = _orders.size

    fun setOnOrderChangedListener(listener: () -> Unit) {
        onOrderChangedListener = listener
    }

    fun removeOnOrderChangedListener() {
        onOrderChangedListener = null
    }
}

