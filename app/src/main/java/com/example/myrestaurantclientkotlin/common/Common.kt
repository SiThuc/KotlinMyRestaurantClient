package com.example.myrestaurantclientkotlin.common

import com.example.myrestaurantclientkotlin.model.CategoryModel
import com.example.myrestaurantclientkotlin.model.FoodModel
import com.example.myrestaurantclientkotlin.model.UserModel

object Common {
    var foodSelected: FoodModel? = null
    var categorySelected: CategoryModel? = null
    val CATEGORY_REF: String = "Category"
    val FULL_WIDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 1
    val BEST_DEALS_REF: String = "BestDeals"
    val POPULAR_REF: String = "MostPopular"
    val USER_REF: String = "Clients"
    var currentUser: UserModel? = null
}