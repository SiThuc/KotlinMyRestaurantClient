package com.example.myrestaurantclientkotlin.ui.cart

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myrestaurantclientkotlin.R
import com.example.myrestaurantclientkotlin.adapter.MyCartAdapter
import com.example.myrestaurantclientkotlin.common.Common
import com.example.myrestaurantclientkotlin.database.CartDataSource
import com.example.myrestaurantclientkotlin.database.CartDatabase
import com.example.myrestaurantclientkotlin.database.LocalCartDataSource
import com.example.myrestaurantclientkotlin.eventbus.HideFABCart
import com.example.myrestaurantclientkotlin.eventbus.UpdateItemInCart
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.StringBuilder

class CartFragment : Fragment() {

    private var cartDataSource:CartDataSource?=null
    private var compositeDisposable:CompositeDisposable = CompositeDisposable()
    private var recyclerViewState: Parcelable?= null

    private lateinit var cartviewModel: CartViewModel

    var txt_empty: TextView? = null
    var txt_total_price: TextView? = null
    var group_place_holder: CardView? = null
    var recycler_cart: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        EventBus.getDefault().postSticky(HideFABCart(true))
        cartviewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        cartviewModel.initCartDataSource(requireContext())

        val root = inflater.inflate(R.layout.cart_fragment, container, false)
        initView(root)

        cartviewModel.getMutableLiveDataCartItem().observe(viewLifecycleOwner, Observer {
            if(it == null || it.isEmpty()){
                recycler_cart!!.visibility = View.GONE
                group_place_holder!!.visibility = View.GONE
                txt_empty!!.visibility = View.VISIBLE
            }else{
                recycler_cart!!.visibility = View.VISIBLE
                group_place_holder!!.visibility = View.VISIBLE
                txt_empty!!.visibility = View.GONE

                val adapter = MyCartAdapter(requireContext(), it)
                recycler_cart!!.adapter = adapter
            }


        })

        return root
    }

    private fun initView(root:View) {
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDao())

        txt_empty = root.findViewById(R.id.txt_empty) as TextView
        txt_total_price = root.findViewById(R.id.txt_total_price) as TextView
        group_place_holder= root.findViewById(R.id.group_place_holder) as CardView

        recycler_cart = root.findViewById(R.id.recycler_cart) as RecyclerView
        recycler_cart!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recycler_cart!!.layoutManager = layoutManager
        recycler_cart!!.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
    }

    override fun onResume() {
        super.onResume()
        calculateTotalPrice()
    }

    override fun onStart() {
        super.onStart()
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        // Send EventBus to HomeActivity to show the fabCart before fragment goes into Stopped State
        EventBus.getDefault().postSticky(HideFABCart(false))

        //Clear the compositeDispose from MyCartAdapter
        cartviewModel.onStop()

        // Clear the compositeDispose from this fragment
        compositeDisposable!!.clear()

        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        super.onStop()
    }

    //EventBus for listening when user click on the elegant Button
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateItemInCart(event: UpdateItemInCart) {
        if(event.cartItem != null){
            recyclerViewState = recycler_cart!!.layoutManager!!.onSaveInstanceState()
            cartDataSource!!.updateCart(event.cartItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: SingleObserver<Int>{
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(t: Int) {
                        calculateTotalPrice();
                        recycler_cart!!.layoutManager!!.onRestoreInstanceState(recyclerViewState)
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "[UPDATE CART] "+e.message, Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }

    private fun calculateTotalPrice() {
        cartDataSource!!.sumPrice(Common.currentUser!!.uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: SingleObserver<Double>{
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: Double) {
                    txt_total_price!!.text = StringBuilder("€")
                        .append(Common.formatPrice(t))
                }

                override fun onError(e: Throwable) {

                }

            })
    }

}