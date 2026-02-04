package io.umain.munchies.feature.restaurant.data.remote

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class KtorRestaurantApi(private val client: HttpClient, private val baseUrl: String) : RestaurantApi {
    override suspend fun getRestaurants(): List<RestaurantDto> {
        val resp: HttpResponse = client.get("$baseUrl/restaurants")
        if (resp.status.isSuccess()) return resp.body()
        throw RuntimeException("Failed to load restaurants: ${resp.status}")
    }

    override suspend fun getRestaurantById(id: String): RestaurantDto? {
        val resp: HttpResponse = client.get("$baseUrl/restaurants/$id")
        if (resp.status == HttpStatusCode.NotFound) return null
        if (resp.status.isSuccess()) return resp.body()
        throw RuntimeException("Failed to load restaurant: ${resp.status}")
    }

    override suspend fun getFilters(): List<FilterDto> {
        val resp: HttpResponse = client.get("$baseUrl/filters")
        if (resp.status.isSuccess()) return resp.body()
        throw RuntimeException("Failed to load filters: ${resp.status}")
    }
}
