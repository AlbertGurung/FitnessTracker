package com.example.fitnesstracker.openfoodfacts

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API call used to talk to OpenFoodFacts.
 * - Defines the network request to fetch a product using its barcode.
 * - The {barcode} in the URL is replaced with the actual barcode value.
 * - Returns the API response mapped into OpenFoodFactsResponse.
 */
interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): OpenFoodFactsResponse

    /**
     * Search for products by name using the OpenFoodFacts search API.
     * - search_terms: the product name to search for
     * - page_size: number of results to return (limited to top suggestions)
     * - Returns search response with list of products
     */
    @GET("cgi/search.pl?search_simple=1&json=1&fields=product_name,nutriments")
    suspend fun searchProducts(
        @Query("search_terms") searchTerms: String,
        @Query("page_size") pageSize: Int = 3
    ): OpenFoodFactsSearchResponse
}