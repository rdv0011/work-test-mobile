package io.umain.munchies.feature.restaurant.data.remote

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.HttpClient
import io.ktor.client.statement.*
import io.ktor.http.*

class KtorRestaurantApi(private val client: HttpClient, private val baseUrl: String) : RestaurantApi {

    override suspend fun getFilter(id: String): FilterDto {
        val resp: HttpResponse = client.get("$baseUrl/api/v1/filters")
        if (resp.status.isSuccess()) return resp.body()
        throw RuntimeException("Failed to load filters: ${resp.status}")
    }

    override suspend fun getRestaurants(): RestaurantContainerDto {
        val resp: HttpResponse = client.get("$baseUrl/api/v1/restaurants")
        if (resp.status.isSuccess()) return resp.body()
        throw RuntimeException("Failed to load restaurants: ${resp.status}")
    }

    override suspend fun getOpen(id: String): RestaurantOpenDto? {
        val resp: HttpResponse = client.get("$baseUrl/api/v1/restaurants/$id")
        if (resp.status == HttpStatusCode.NotFound) return null
        if (resp.status.isSuccess()) return resp.body()
        throw RuntimeException("Failed to load restaurant: ${resp.status}")
    }
}
