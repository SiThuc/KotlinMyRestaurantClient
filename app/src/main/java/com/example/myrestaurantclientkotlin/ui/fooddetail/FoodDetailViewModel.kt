package com.example.myrestaurantclientkotlin.ui.fooddetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myrestaurantclientkotlin.common.Common
import com.example.myrestaurantclientkotlin.model.FoodModel


class FoodDetailViewModel : ViewModel() {
    private var foodDetailMutable: MutableLiveData<FoodModel>? = null

    fun getFoodDetail(): MutableLiveData<FoodModel> {
        if (foodDetailMutable == null)
            foodDetailMutable = MutableLiveData()
        foodDetailMutable!!.value = Common.foodSelected

        return foodDetailMutable!!
    }
}