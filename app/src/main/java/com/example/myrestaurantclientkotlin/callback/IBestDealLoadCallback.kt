package com.example.myrestaurantclientkotlin.callback

import com.example.myrestaurantclientkotlin.model.BestDealModel

interface IBestDealLoadCallback {
    fun onBestDealLoadSuccess(bestDealList: List<BestDealModel>)
    fun onBestDealLoadFailed(message: String)
}