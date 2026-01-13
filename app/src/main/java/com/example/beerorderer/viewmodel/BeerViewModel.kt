package com.example.beerorderer.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.example.beerorderer.data.Beer
import com.example.beerorderer.data.Currency
import com.example.beerorderer.data.OrderRepository
import com.example.beerorderer.manager.OrderManager
import com.example.beerorderer.repository.BeerRepository
import com.example.beerorderer.repository.CurrencyRepository
import kotlinx.coroutines.launch

enum class SortOption {
    NONE, PRICE_ASC, PRICE_DESC, RATING_ASC, RATING_DESC, REVIEWS_ASC, REVIEWS_DESC
}

enum class FilterOption {
    ALL, IPA, ALE, STOUT, LAGER, PILSNER, PORTER, OTHER
}

/**
 * ViewModel for managing beer data and orders following MVVM architecture
 * Handles configuration changes (rotation, night mode) automatically
 */
class BeerViewModel(application: Application) : AndroidViewModel(application) {
    private val beerRepository = BeerRepository()
    private val orderRepository = OrderRepository(application.applicationContext)
    private val currencyRepository = CurrencyRepository()

    private var allBeers: List<Beer> = emptyList()
    private var currentSort = SortOption.NONE
    private var currentFilter = FilterOption.ALL
    private var exchangeRates: Map<String, Double> = emptyMap()

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "pref_currency") {
            val value = prefs.getString(key, "USD") ?: "USD"
            val currency = when (value) {
                "EUR" -> Currency.EUR
                "CZK" -> Currency.CZK
                else -> Currency.USD
            }
            setCurrency(currency)
        }
    }

    private val _beers = MutableLiveData<List<Beer>>()
    val beers: LiveData<List<Beer>> = _beers

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _orderCount = MutableLiveData<Int>(0)
    val orderCount: LiveData<Int> = _orderCount

    private val _orders = MutableLiveData<List<Beer>>(emptyList())
    val orders: LiveData<List<Beer>> = _orders

    private val _totalPrice = MutableLiveData<String>("$0.00")
    val totalPrice: LiveData<String> = _totalPrice

    private val _currentCurrency = MutableLiveData<Currency>(Currency.USD)
    val currentCurrency: LiveData<Currency> = _currentCurrency

    init {
        // Load saved currency preference
        val savedCurrency = prefs.getString("pref_currency", "USD") ?: "USD"
        _currentCurrency.value = when (savedCurrency) {
            "EUR" -> Currency.EUR
            "CZK" -> Currency.CZK
            else -> Currency.USD
        }

        // Register preference listener
        prefs.registerOnSharedPreferenceChangeListener(prefListener)

        // Load saved orders on initialization
        loadSavedOrders()

        // Load exchange rates
        loadExchangeRates()

        // Listen for order changes to auto-save
        OrderManager.setOnOrderChangedListener {
            saveOrders()
        }
    }

    /**
     * Load beers from API
     */
    fun loadBeers() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = beerRepository.getBeers()
            result.onSuccess { beerList ->
                allBeers = beerList
                applyFilterAndSort()
                _loading.value = false
            }.onFailure { exception ->
                _error.value = exception.message ?: "Unknown error occurred"
                _loading.value = false
            }
        }
    }

    /**
     * Load saved orders from JSON file
     */
    private fun loadSavedOrders() {
        viewModelScope.launch {
            val savedOrders = orderRepository.loadOrders()
            if (savedOrders.isNotEmpty()) {
                OrderManager.setOrders(savedOrders)
                updateOrders()
            }
        }
    }

    /**
     * Save current orders to JSON file
     */
    private fun saveOrders() {
        viewModelScope.launch {
            orderRepository.saveOrders(OrderManager.orders)
            updateOrders()
        }
    }

    /**
     * Set sort option and reapply
     */
    fun setSortOption(sortOption: SortOption) {
        currentSort = sortOption
        applyFilterAndSort()
    }

    /**
     * Set filter option and reapply
     */
    fun setFilterOption(filterOption: FilterOption) {
        currentFilter = filterOption
        applyFilterAndSort()
    }

    /**
     * Apply current filter and sort settings
     */
    private fun applyFilterAndSort() {
        var filtered = when (currentFilter) {
            FilterOption.ALL -> allBeers
            FilterOption.IPA -> allBeers.filter { it.name.contains("IPA", ignoreCase = true) }
            FilterOption.ALE -> allBeers.filter { it.name.contains("Ale", ignoreCase = true) && !it.name.contains("IPA", ignoreCase = true) }
            FilterOption.STOUT -> allBeers.filter { it.name.contains("Stout", ignoreCase = true) }
            FilterOption.LAGER -> allBeers.filter { it.name.contains("Lager", ignoreCase = true) }
            FilterOption.PILSNER -> allBeers.filter { it.name.contains("Pilsner", ignoreCase = true) }
            FilterOption.PORTER -> allBeers.filter { it.name.contains("Porter", ignoreCase = true) }
            FilterOption.OTHER -> allBeers.filter { beer ->
                !beer.name.contains("IPA", ignoreCase = true) &&
                !beer.name.contains("Ale", ignoreCase = true) &&
                !beer.name.contains("Stout", ignoreCase = true) &&
                !beer.name.contains("Lager", ignoreCase = true) &&
                !beer.name.contains("Pilsner", ignoreCase = true) &&
                !beer.name.contains("Porter", ignoreCase = true)
            }
        }

        filtered = when (currentSort) {
            SortOption.NONE -> filtered
            SortOption.PRICE_ASC -> filtered.sortedBy { it.price.replace("$", "").toDoubleOrNull() ?: 0.0 }
            SortOption.PRICE_DESC -> filtered.sortedByDescending { it.price.replace("$", "").toDoubleOrNull() ?: 0.0 }
            SortOption.RATING_ASC -> filtered.sortedBy { it.rating.average }
            SortOption.RATING_DESC -> filtered.sortedByDescending { it.rating.average }
            SortOption.REVIEWS_ASC -> filtered.sortedBy { it.rating.reviews }
            SortOption.REVIEWS_DESC -> filtered.sortedByDescending { it.rating.reviews }
        }

        _beers.value = filtered
    }

    /**
     * Add beer to order and save to JSON
     */
    fun addToOrder(beer: Beer) {
        OrderManager.addOrder(beer)
        updateOrderCount()
        // saveOrders() is called automatically via listener
    }

    /**
     * Remove beer from order and save to JSON
     */
    fun removeFromOrder(beer: Beer) {
        OrderManager.removeOrder(beer)
        updateOrderCount()
        // saveOrders() is called automatically via listener
    }

    /**
     * Clear all orders and delete JSON file
     */
    fun clearAllOrders() {
        viewModelScope.launch {
            OrderManager.clearOrders()
            orderRepository.clearOrders()
            updateOrders()
        }
    }

    /**
     * Send order (simulate) - clears orders after sending
     */
    fun sendOrder() {
        viewModelScope.launch {
            // Here you would implement actual order sending logic
            // For now, we just clear the orders
            clearAllOrders()
        }
    }

    /**
     * Update order count
     */
    fun updateOrderCount() {
        _orderCount.value = OrderManager.getOrderCount()
    }

    /**
     * Update orders list and total price
     */
    private fun updateOrders() {
        _orders.value = OrderManager.orders
        _orderCount.value = OrderManager.getOrderCount()
        val totalInUSD = OrderManager.getTotalPrice()
        val currency = _currentCurrency.value ?: Currency.USD
        val convertedTotal = currencyRepository.convertPrice(totalInUSD, currency, exchangeRates)
        _totalPrice.value = currencyRepository.formatPrice(convertedTotal, currency)
    }

    /**
     * Load exchange rates from API
     */
    private fun loadExchangeRates() {
        viewModelScope.launch {
            val result = currencyRepository.getExchangeRates()
            result.onSuccess { rates ->
                exchangeRates = rates
                // Update displayed prices
                updateOrders()
            }.onFailure {
                // Use default rates if API fails
                exchangeRates = mapOf(
                    "USD" to 1.0,
                    "EUR" to 0.92,
                    "CZK" to 23.5
                )
                updateOrders()
            }
        }
    }

    /**
     * Change display currency
     */
    fun setCurrency(currency: Currency) {
        _currentCurrency.value = currency
        updateOrders()
    }

    /**
     * Get converted price for a beer
     */
    fun getConvertedPrice(priceString: String): String {
        val priceInUSD = priceString.replace("$", "").toDoubleOrNull() ?: 0.0
        val currency = _currentCurrency.value ?: Currency.USD
        val convertedPrice = currencyRepository.convertPrice(priceInUSD, currency, exchangeRates)
        return currencyRepository.formatPrice(convertedPrice, currency)
    }

    override fun onCleared() {
        super.onCleared()
        // Remove listener when ViewModel is cleared
        OrderManager.removeOnOrderChangedListener()
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
    }
}
