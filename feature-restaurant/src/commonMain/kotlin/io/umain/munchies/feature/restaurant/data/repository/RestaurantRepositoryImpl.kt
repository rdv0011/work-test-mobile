package io.umain.munchies.feature.restaurant.data.repository

import io.umain.munchies.feature.restaurant.domain.model.Filter
import io.umain.munchies.feature.restaurant.domain.model.Restaurant
import io.umain.munchies.feature.restaurant.domain.model.RestaurantStatus
import io.umain.munchies.feature.restaurant.domain.repository.RestaurantRepository
import io.umain.munchies.feature.restaurant.data.mapper.toDomain
import io.umain.munchies.feature.restaurant.data.remote.RestaurantApi

class RestaurantRepositoryImpl(
    // Allow injecting a remote API later. Keep default behaviour using mocks.
    private val api: RestaurantApi? = null
) : RestaurantRepository {
    
    private val mockRestaurants = listOf(
        Restaurant(
            id = "1",
            name = "Pizza Palace",
            description = "Authentic Italian pizza and pasta",
            imageUrl = "https://via.placeholder.com/300x200?text=Pizza+Palace",
            rating = 4.5f,
            reviewCount = 128,
            status = RestaurantStatus.OPEN,
            filterIds = listOf("italian", "pizza")
        ),
        Restaurant(
            id = "2",
            name = "Burger Barn",
            description = "Classic American burgers and fries",
            imageUrl = "https://via.placeholder.com/300x200?text=Burger+Barn",
            rating = 4.2f,
            reviewCount = 95,
            status = RestaurantStatus.OPEN,
            filterIds = listOf("american", "burgers")
        ),
        Restaurant(
            id = "3",
            name = "Sushi Supreme",
            description = "Fresh sushi and Japanese cuisine",
            imageUrl = "https://via.placeholder.com/300x200?text=Sushi+Supreme",
            rating = 4.8f,
            reviewCount = 156,
            status = RestaurantStatus.OPEN,
            filterIds = listOf("japanese", "sushi")
        ),
        Restaurant(
            id = "4",
            name = "Thai Spice",
            description = "Authentic Thai cuisine with spicy flavors",
            imageUrl = "https://via.placeholder.com/300x200?text=Thai+Spice",
            rating = 4.3f,
            reviewCount = 112,
            status = RestaurantStatus.CLOSED,
            filterIds = listOf("thai", "asian")
        ),
        Restaurant(
            id = "5",
            name = "Taco Fiesta",
            description = "Mexican tacos, burritos, and more",
            imageUrl = "https://via.placeholder.com/300x200?text=Taco+Fiesta",
            rating = 4.6f,
            reviewCount = 143,
            status = RestaurantStatus.OPEN,
            filterIds = listOf("mexican", "tacos")
        )
    )
    
    private val mockFilters = listOf(
        Filter(id = "italian", name = "Italian", iconUrl = "https://via.placeholder.com/50x50?text=IT"),
        Filter(id = "pizza", name = "Pizza", iconUrl = "https://via.placeholder.com/50x50?text=Pizza"),
        Filter(id = "american", name = "American", iconUrl = "https://via.placeholder.com/50x50?text=US"),
        Filter(id = "burgers", name = "Burgers", iconUrl = "https://via.placeholder.com/50x50?text=Burger"),
        Filter(id = "japanese", name = "Japanese", iconUrl = "https://via.placeholder.com/50x50?text=JP"),
        Filter(id = "sushi", name = "Sushi", iconUrl = "https://via.placeholder.com/50x50?text=Sushi"),
        Filter(id = "thai", name = "Thai", iconUrl = "https://via.placeholder.com/50x50?text=TH"),
        Filter(id = "asian", name = "Asian", iconUrl = "https://via.placeholder.com/50x50?text=Asia"),
        Filter(id = "mexican", name = "Mexican", iconUrl = "https://via.placeholder.com/50x50?text=MX"),
        Filter(id = "tacos", name = "Tacos", iconUrl = "https://via.placeholder.com/50x50?text=Taco")
    )
    
    override suspend fun getRestaurants(): List<Restaurant> = api?.let { remote ->
        // Prefer remote API when available
        remote.getRestaurants().map { it.toDomain() }
    } ?: mockRestaurants

    override suspend fun getRestaurantById(id: String): Restaurant? = api?.let { remote ->
        remote.getRestaurantById(id)?.toDomain()
    } ?: mockRestaurants.find { it.id == id }

    override suspend fun getFilters(): List<Filter> = api?.let { remote ->
        remote.getFilters().map { it.toDomain() }
    } ?: mockFilters

    override suspend fun getRestaurantsByFilter(filterIds: Set<String>): List<Restaurant> =
        api?.let { remote ->
            val restaurants = remote.getRestaurants().map { it.toDomain() }
            if (filterIds.isEmpty()) restaurants else restaurants.filter { r -> r.filterIds.any { it in filterIds } }
        } ?: if (filterIds.isEmpty()) {
            mockRestaurants
        } else {
            mockRestaurants.filter { restaurant ->
                restaurant.filterIds.any { it in filterIds }
            }
        }
}
