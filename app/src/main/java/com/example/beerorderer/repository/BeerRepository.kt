package com.example.beerorderer.repository

import com.example.beerorderer.data.Beer
import com.example.beerorderer.network.RetrofitClient

class BeerRepository {
    private val apiService = RetrofitClient.beerApiService

    suspend fun getBeers(): Result<List<Beer>> {
        return try {
            val beers = apiService.getBeers()
            Result.success(beers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

