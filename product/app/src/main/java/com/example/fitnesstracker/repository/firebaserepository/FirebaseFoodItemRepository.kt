package com.example.fitnesstracker.repository.firebaserepository

import com.example.fitnesstracker.data.food.FoodItem
import com.example.fitnesstracker.openfoodfacts.OpenFoodFactsService
import com.example.fitnesstracker.openfoodfacts.toFoodItem
import com.example.fitnesstracker.repository.repositoryinterface.FoodItemRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * FirebaseFoodItemRepository to save and load FoodItem objects in Firestore for the current user.
 * - Food items stored in users/{uid}/foodItems/{itemId}.
 */
class FirebaseFoodItemRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : FoodItemRepository {


    /**
     * Returns today's date as a string in yyyy-mm-dd format.
     */
    private fun todayDateString() = LocalDate.now().toString()


    /**
    * Returns the Firestore collection path for the current user's food items.
    * - Throws exception if no user is logged in.
    */
    private fun currentFoodItemsPath() =
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("foodItems")
        } ?: throw IllegalStateException("User not logged in")

    /**
     * Adds a food item for today under the current user.
     */
    override suspend fun addFoodItem(item: FoodItem) {
        val itemWithDate = item.copy(dateAdded = todayDateString())
        val path = currentFoodItemsPath().document() // auto-generated ID
        path.set(itemWithDate).await()
    }

    /**
     * Gets all food items added today for the current user.
     */
    override suspend fun getTodayFoodItems(): List<FoodItem> {
        val snapshot = currentFoodItemsPath()
            .whereEqualTo("dateAdded", todayDateString())
            .get() // Gets food items collection of the user for today's date.
            .await()

        // Convert Firestore documents to FoodItem objects and include document ID
        return snapshot.documents.mapNotNull { doc -> // Returns list without any null results.
            doc.toObject(FoodItem::class.java) // maps Firestore's food item fields to FoodItem object class.
                ?.copy(id = doc.id) // .copy(... ensures that the Food item's id is set even though Firestore doesn't store ID in the document.
        }
    }

    /**
     * Deletes a food item from Firestore.
     * - Requires the item's ID to find it in the database.
     */
    override suspend fun deleteFoodItem(item: FoodItem) {
        val itemId = item.id ?: throw IllegalArgumentException("FoodItem need ID to be deleted")

        currentFoodItemsPath()
            .document(itemId) // Finds the specific document to delete from its ID
            .delete()         // Delete document for firebase.
            .await()
    }

    /**
     * Fetches food data from OpenFoodFacts using a barcode.
     * - Calls the external API.
     * - Converts the API response into a FoodItem object.
     */
    override suspend fun getFoodDataByBarcode(barcode: String): FoodItem {
        val response = OpenFoodFactsService.api.getProduct(barcode)
        if (response.status != 1 || response.product == null) {
            throw IllegalArgumentException("Food item is not found")
        }
        return response.product.toFoodItem()
    }

    /**
     * Searches for food items by name using the OpenFoodFacts API.
     * - Calls the external search API.
     * - Converts the search results into FoodItem objects.
     * - Returns top 3 suggestions based on the query.
     */
    override suspend fun searchFoodByName(query: String): List<FoodItem> {
        val response = OpenFoodFactsService.api.searchProducts(query, pageSize = 3)
        return response.products.map { it.toFoodItem() }
    }

    /**
     * Gets all food items between two dates for creating historical data.
     * - Queries Firestore for food items within the specified date range.
     */
    override suspend fun getFoodItemsForDateRange(startDate: String, endDate: String): List<FoodItem> {
        val snapshot = currentFoodItemsPath()
            .whereGreaterThanOrEqualTo("dateAdded", startDate) // Filters items from start date onwards.
            .whereLessThanOrEqualTo("dateAdded", endDate) // Filters items up to end date.
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc -> // Maps Firestore documents to FoodItem objects .
            doc.toObject(FoodItem::class.java)?.copy(id = doc.id) // Converts doc to FoodItem and sets the ID.
        }
    }
}