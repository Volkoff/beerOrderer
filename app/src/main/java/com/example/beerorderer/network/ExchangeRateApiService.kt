package com.example.beerorderer.network

import com.example.beerorderer.data.ExchangeRateResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API service for fetching exchange rates
 * Using ExchangeRate-API (free tier)
 */
interface ExchangeRateApiService {

    @GET("v6/latest/{base}")
    suspend fun getExchangeRates(
        @Path("base") baseCurrency: String = "USD"
    ): ExchangeRateResponse
}

