package com.example.beerorderer.data

import com.google.gson.annotations.SerializedName

/**
 * Response model for exchange rate API
 */
data class ExchangeRateResponse(
    @SerializedName("result")
    val result: String,

    @SerializedName("conversion_rates")
    val conversionRates: Map<String, Double>
)

