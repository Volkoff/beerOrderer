package com.example.beerorderer.repository

import com.example.beerorderer.data.Currency
import com.example.beerorderer.network.ExchangeRateClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Repository for managing currency exchange rates
 */
class CurrencyRepository {

    private val apiService = ExchangeRateClient.apiService

    /**
     * Fetch exchange rates from USD base
     */
    suspend fun getExchangeRates(): Result<Map<String, Double>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getExchangeRates("USD")
            if (response.result == "success") {
                Result.success(response.conversionRates)
            } else {
                Result.failure(Exception("Failed to fetch exchange rates"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convert price from USD to target currency. Accepts nullable rates and uses
     * sensible defaults when rates are missing to avoid crashes.
     */
    fun convertPrice(priceInUSD: Double, targetCurrency: Currency, rates: Map<String, Double>?): Double {
        // If target is USD, no conversion needed
        if (targetCurrency == Currency.USD) return priceInUSD

        // Use provided rate when available, otherwise fall back to reasonable defaults
        val rate = when (targetCurrency) {
            Currency.EUR -> rates?.get("EUR") ?: 0.92
            Currency.CZK -> rates?.get("CZK") ?: 23.5
            // default fallback for unexpected currencies
            else -> 1.0
        }

        return priceInUSD * rate
    }

    /**
     * Format price with currency symbol
     */
    fun formatPrice(amount: Double, currency: Currency): String {
        return when (currency) {
            Currency.USD -> "$${String.format(Locale.US, "%.2f", amount)}"
            Currency.EUR -> "${String.format(Locale.US, "%.2f", amount)}€"
            Currency.CZK -> "${String.format(Locale.US, "%.2f", amount)} Kč"
        }
    }
}
