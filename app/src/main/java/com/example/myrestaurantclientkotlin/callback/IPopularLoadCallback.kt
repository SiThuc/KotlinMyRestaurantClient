package com.example.myrestaurantclientkotlin.callback

import com.example.myrestaurantclientkotlin.model.PopularCategoryModel

interface IPopularLoadCallback {
    fun onPopularLoadSuccess(popularModelList: List<PopularCategoryModel>)
    fun onPopularLoadFailed(message: String)
}