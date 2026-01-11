package com.example.beerorderer.network

import com.example.beerorderer.data.Beer
import retrofit2.http.GET

interface BeerApiService {
    @GET("beers/ale")
    suspend fun getBeers(): List<Beer>
}

