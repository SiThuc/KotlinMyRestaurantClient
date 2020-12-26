package com.example.myrestaurantclientkotlin.model

import com.example.myrestaurantclientkotlin.database.CartItem

class Order {
    var userId: String? = null
    var userName: String? = null
    var userPhone: String? = null
    var shippingAddress: String? = null
    var comment: String? = null
    var transactionId: String? = null
    var lat: Double = 0.0
    var lng: Double = 0.0
    var totalPayment: Double = 0.0
    var finalPayment: Double = 0.0
    var isCod: Boolean = false
    var discount: Int = 0
    var cartItemList: List<CartItem>? = null
}