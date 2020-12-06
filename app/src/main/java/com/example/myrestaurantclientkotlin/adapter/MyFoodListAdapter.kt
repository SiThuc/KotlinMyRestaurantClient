package com.example.myrestaurantclientkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myrestaurantclientkotlin.R
import com.example.myrestaurantclientkotlin.callback.IRecyclerItemClickListener
import com.example.myrestaurantclientkotlin.common.Common
import com.example.myrestaurantclientkotlin.eventbus.CategoryClick
import com.example.myrestaurantclientkotlin.eventbus.FoodItemClick
import com.example.myrestaurantclientkotlin.model.FoodModel
import org.greenrobot.eventbus.EventBus

class MyFoodListAdapter(
    var context: Context,
    var foodList: List<FoodModel>
) :
    RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder>() {

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
                EventBus.getDefault().postSticky(FoodItemClick(true, foodList.get(pos)))
            }
        })
    }

    override fun getItemCount(): Int {
        return foodList.size
    }
}