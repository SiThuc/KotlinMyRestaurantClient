package com.example.myrestaurantclientkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.example.myrestaurantclientkotlin.R
import com.example.myrestaurantclientkotlin.database.CartItem
import com.example.myrestaurantclientkotlin.eventbus.UpdateItemInCart
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class MyCartAdapter(
    internal var context: Context,
    internal var cartItemList: List<CartItem>
) :
    RecyclerView.Adapter<MyCartAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var img_cart: ImageView
        lateinit var food_name: TextView
        lateinit var food_price: TextView
        lateinit var num_food: ElegantNumberButton

        init {
            img_cart = itemView.findViewById(R.id.img_food) as ImageView
            food_name = itemView.findViewById(R.id.txt_food_name) as TextView
            food_price = itemView.findViewById(R.id.txt_food_price) as TextView
            num_food = itemView.findViewById(R.id.btn_num_food) as ElegantNumberButton
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_cart_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(cartItemList.get(position).foodImage).into(holder.img_cart!!)
        holder.food_name!!.text = cartItemList.get(position).foodName
        holder.food_price!!.text = StringBuilder("€").append(
            cartItemList.get(position).foodPrice!! +
                    cartItemList.get(position).foodExtraPrice!!
        )
        holder.num_food!!.number = cartItemList.get(position).foodQuantity.toString()

        // When user press Elegant Number Button to increase or decrease the number of cartItem
        holder.num_food!!.setOnValueChangeListener { view, oldValue, newValue ->
            cartItemList.get(position).foodQuantity = newValue
            EventBus.getDefault().postSticky(UpdateItemInCart(cartItemList.get(position)))
        }
    }

    override fun getItemCount(): Int {
        return cartItemList.size
    }
}