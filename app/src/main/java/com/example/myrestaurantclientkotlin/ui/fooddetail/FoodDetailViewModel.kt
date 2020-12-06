package com.example.myrestaurantclientkotlin.ui.fooddetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myrestaurantclientkotlin.common.Common
import com.example.myrestaurantclientkotlin.model.CommentModel
import com.example.myrestaurantclientkotlin.model.FoodModel


class FoodDetailViewModel : ViewModel() {
    private var foodDetailMutable: MutableLiveData<FoodModel>? = null
    private var mutableLiveDataComment: MutableLiveData<CommentModel>? = null

    fun getFoodDetail(): MutableLiveData<FoodModel> {
        if (foodDetailMutable == null)
            foodDetailMutable = MutableLiveData()
        foodDetailMutable!!.value = Common.foodSelected

        return foodDetailMutable!!
    }

    fun getComment(): MutableLiveData<CommentModel> {
        if (mutableLiveDataComment == null)
            mutableLiveDataComment = MutableLiveData()
        return mutableLiveDataComment!!
    }


    fun setCommentModel(commentModel: CommentModel) {
        if(mutableLiveDataComment != null)
            mutableLiveDataComment!!.value = commentModel
    }

    fun setFoodModel(foodModel: FoodModel) {

       if(foodDetailMutable != null)
           foodDetailMutable!!.value = foodModel

    }
}