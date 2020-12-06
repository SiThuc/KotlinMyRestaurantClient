package com.example.myrestaurantclientkotlin.eventbus

import com.example.myrestaurantclientkotlin.model.CategoryModel

class CategoryClick(var isSuccess: Boolean, var category: CategoryModel) {
}