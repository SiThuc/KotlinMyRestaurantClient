package com.example.myrestaurantclientkotlin.model

class FoodModel {
    var id: String? = null
    var name: String? = null
    var image: String? = null
    var price: Long = 0
    var description: String? = null
    var addon: List<AddonModel> = ArrayList<AddonModel>()
    var size: List<SizeModel> = ArrayList<SizeModel>()
}