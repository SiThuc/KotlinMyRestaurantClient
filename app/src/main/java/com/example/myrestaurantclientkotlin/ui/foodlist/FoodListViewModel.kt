package com.example.myrestaurantclientkotlin.ui.foodlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myrestaurantclientkotlin.common.Common
import com.example.myrestaurantclientkotlin.model.CategoryModel
import com.example.myrestaurantclientkotlin.model.FoodModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FoodListViewModel : ViewModel() {
    private var foodListMutable: MutableLiveData<List<FoodModel>>? = null

    fun getFoodList(): MutableLiveData<List<FoodModel>> {
        if (foodListMutable == null)
            foodListMutable = MutableLiveData()
        foodListMutable!!.value = Common.categorySelected!!.foods

        return foodListMutable!!
    }
}