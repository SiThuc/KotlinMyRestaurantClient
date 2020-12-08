package com.example.myrestaurantclientkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myrestaurantclientkotlin.R
import com.example.myrestaurantclientkotlin.callback.IRecyclerItemClickListener
import com.example.myrestaurantclientkotlin.common.Common
import com.example.myrestaurantclientkotlin.database.CartDataSource
import com.example.myrestaurantclientkotlin.database.CartDatabase
import com.example.myrestaurantclientkotlin.database.CartItem
import com.example.myrestaurantclientkotlin.database.LocalCartDataSource
import com.example.myrestaurantclientkotlin.eventbus.CountCartEvent
import com.example.myrestaurantclientkotlin.eventbus.FoodItemClick
import com.example.myrestaurantclientkotlin.model.FoodModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

class MyFoodListAdapter(
    var context: Context,
    var foodList: List<FoodModel>
) :
    RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder>() {

    private val compositeDisposable: CompositeDisposable
    private val cartDataSource : CartDataSource

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDao())
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener{
        var food_name: TextView? = null
        var food_price: TextView? = null
        var food_image: ImageView? = null
        var fav_image: ImageView? = null
        var cart_image: ImageView? = null

        internal var listener: IRecyclerItemClickListener? = null

        fun setListener(listener: IRecyclerItemClickListener) {
            this.listener = listener
        }

        init {
            food_name = itemView.findViewById(R.id.txt_food_name) as TextView
            food_price = itemView.findViewById(R.id.txt_food_price) as TextView
            food_image = itemView.findViewById(R.id.img_food) as ImageView
            fav_image = itemView.findViewById(R.id.img_fav) as ImageView
            cart_image = itemView.findViewById(R.id.img_cart) as ImageView

            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener!!.onItemClick(v!!, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.food_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(foodList.get(position).image)
            .into(holder.food_image!!)
        holder.food_name!!.setText(foodList.get(position).name)

        holder.food_price!!.setText("€ " + foodList.get(position).price.toString())

        //Event on Item Click
        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                Common.foodSelected = foodList.get(pos)
                Common.foodSelected!!.key = pos.toString()
                EventBus.getDefault().postSticky(FoodItemClick(true, foodList.get(pos)))
            }
        })

        //Event when user click on the Cart Image
        holder.cart_image!!.setOnClickListener {
            val cartItem = CartItem()

            cartItem.uid = Common.currentUser!!.uid
            cartItem.userPhone = Common.currentUser!!.phone

            cartItem.foodId = foodList.get(position).id!!
            cartItem.foodName = foodList.get(position).name
            cartItem.foodImage = foodList.get(position).image
            cartItem.foodPrice = foodList.get(position).price.toDouble()
            cartItem.foodQuantity = 1
            cartItem.foodExtraPrice = 0.0
            cartItem.foodAddon = "Default"
            cartItem.foodSize = "Default"

            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    Toast.makeText(context, "Add to cart success", Toast.LENGTH_SHORT).show()
                    EventBus.getDefault().postSticky(CountCartEvent(true))
                }, {
                    t:Throwable? -> Toast.makeText(context, "[INSERT CART]"+t!!.message, Toast.LENGTH_SHORT).show()
            }))
        }
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    fun onStop(){
        if(compositeDisposable != null)
            compositeDisposable.clear()
    }

}