package com.example.myrestaurantclientkotlin.ui.cart

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myrestaurantclientkotlin.common.Common
import com.example.myrestaurantclientkotlin.database.CartDataSource
import com.example.myrestaurantclientkotlin.database.CartDatabase
import com.example.myrestaurantclientkotlin.database.CartItem
import com.example.myrestaurantclientkotlin.database.LocalCartDataSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CartViewModel : ViewModel() {
    private val compositeDisposable: CompositeDisposable
    private lateinit var cartDataSource: CartDataSource
    private var mutableLiveDatCartItem: MutableLiveData<List<CartItem>>? = null

    init {
        compositeDisposable = CompositeDisposable()
    }

    fun initCartDataSource(context: Context){
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDao())
    }

    fun getMutableLiveDataCartItem(): MutableLiveData<List<CartItem>> {
        if (mutableLiveDatCartItem == null)
            mutableLiveDatCartItem = MutableLiveData()
        getCartItems()
        return mutableLiveDatCartItem!!
    }

    private fun getCartItems() {

        compositeDisposable.addAll(
            cartDataSource!!.getAllCart(Common.currentUser!!.uid!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ cartItems ->
                    mutableLiveDatCartItem!!.value = cartItems
                }, {t: Throwable? -> mutableLiveDatCartItem!!.value == null})
        )
    }

    fun onStop(){
        compositeDisposable.clear()
    }
}