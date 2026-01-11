package com.example.beerorderer.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class RatingDeserializer : JsonDeserializer<Rating> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Rating {
        return try {
            when {
                json == null || json.isJsonNull -> Rating(0.0, 0)
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    val average = obj.get("average")?.asDouble ?: 0.0
                    val reviews = obj.get("reviews")?.asInt ?: 0
                    Rating(average, reviews)
                }
                json.isJsonPrimitive -> {
                    val average = json.asString.toDoubleOrNull() ?: 0.0
                    Rating(average, 0)
                }
                else -> Rating(0.0, 0)
            }
        } catch (_: Exception) {
            Rating(0.0, 0)
        }
    }
}

