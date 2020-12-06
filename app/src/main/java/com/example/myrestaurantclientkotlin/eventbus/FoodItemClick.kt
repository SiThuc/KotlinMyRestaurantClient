package com.example.myrestaurantclientkotlin.eventbus

import com.example.myrestaurantclientkotlin.model.FoodModel

class FoodItemClick(var isSuccess: Boolean, var foodModel: FoodModel) {
}