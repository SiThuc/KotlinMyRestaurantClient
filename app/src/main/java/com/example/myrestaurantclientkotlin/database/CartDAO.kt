package com.example.myrestaurantclientkotlin.database

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface CartDAO {
    @Query("Select * from Cart where uid=:uid")
    fun getAllCart(uid: String): Flowable<List<CartItem>>

    @Query("Select COUNT(*) from Cart where uid=:uid")
    fun countItemInCart(uid: String): Single<Int>

    @Query("Select SUM(foodQuantity*foodPrice)+(foodExtraPrice * foodQuantity) from Cart where uid=:uid")
    fun sumPrice(uid: String): Single<Long>

    @Query("Select * from Cart where foodId=:foodId AND uid=:uid")
    fun getItemInCart(foodId: String, uid: String): Single<CartItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceAll(vararg cartItems: CartItem): Completable

    @Delete
    fun deleteCart(cart: CartItem): Single<Int>

    @Query("Delete from Cart where uid=:uid")
    fun cleanCart(uid: String): Single<Int>

}