package com.example.myrestaurantclientkotlin.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import io.reactivex.annotations.NonNull;

@Entity(tableName = "Cart")
class CartItem {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "foodId")
    var foodId: String = ""

    @ColumnInfo(name = "foodName")
    var foodName: String? = null

    @ColumnInfo(name = "foodImage")
    var foodImage: String? = null

    @ColumnInfo(name = "foodPrice")
    var foodPrice: Double? = null

    @ColumnInfo(name = "foodQuantity")
    var foodQuantity: Int? = null

    @ColumnInfo(name = "foodAddon")
    var foodAddon: String? = null

    @ColumnInfo(name = "foodSize")
    var foodSize: String? = null

    @ColumnInfo(name = "userPhone")
    var userPhone: String? = null

    @ColumnInfo(name = "foodExtraPrice")
    var foodExtraPrice: Double? = 0.0

    @ColumnInfo(name = "uid")
    var uid: String? = ""

}
