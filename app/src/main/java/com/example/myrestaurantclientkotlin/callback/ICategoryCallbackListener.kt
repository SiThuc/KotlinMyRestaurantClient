package com.example.myrestaurantclientkotlin.callback

import com.example.myrestaurantclientkotlin.model.CategoryModel

interface ICategoryCallbackListener {
    fun onCategoryLoadSuccess(categoriesList: List<CategoryModel>)
    fun onCategoryLoadFailed(message: String)
}