package com.example.beerorderer.data

import com.google.gson.annotations.SerializedName

data class Beer(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("price")
    val price: String = "$0.00",

    @SerializedName("rating")
    val rating: Rating = Rating(),

    @SerializedName("image")
    val image: String? = null
)

data class Rating(
    @SerializedName("average")
    val average: Double = 0.0,

    @SerializedName("reviews")
    val reviews: Int = 0
)

