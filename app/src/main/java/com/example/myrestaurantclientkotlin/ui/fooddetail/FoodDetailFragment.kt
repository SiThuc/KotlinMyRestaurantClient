package com.example.myrestaurantclientkotlin.ui.fooddetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.example.myrestaurantclientkotlin.R
import com.example.myrestaurantclientkotlin.adapter.MyFoodListAdapter
import com.example.myrestaurantclientkotlin.common.Common
import com.example.myrestaurantclientkotlin.model.FoodModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.StringBuilder

class FoodDetailFragment : Fragment() {

    private lateinit var foodDetailViewModel: FoodDetailViewModel

    private var img_food:ImageView?=null
    private var btnCart:CounterFab?=null
    private var btnRating:FloatingActionButton?=null
    private var food_name:TextView?=null
    private var food_description:TextView?=null
    private var food_price:TextView?=null
    private var number_button:ElegantNumberButton?=null
    private var ratingBar:RatingBar?=null
    private var btnShowComment:Button?=null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodDetailViewModel = ViewModelProvider(this).get(FoodDetailViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_detail, container, false)

        initView(root)

        foodDetailViewModel.getFoodDetail().observe(viewLifecycleOwner, Observer {
            displayInfo(it)
        })

        return root
    }

    private fun displayInfo(it: FoodModel?) {
        Glide.with(requireContext()).load(it!!.image).into(img_food!!)
        food_name!!.text = StringBuilder(it.name)
        food_description!!.text = StringBuilder(it.description)
        food_price!!.text = StringBuilder(it.price.toString())

    }

    private fun initView(root: View) {

        img_food = root.findViewById<ImageView>(R.id.img_food_detail)
        btnCart = root.findViewById<CounterFab>(R.id.btnCart)
        btnRating= root.findViewById<FloatingActionButton>(R.id.btn_rating)
        food_name = root.findViewById<TextView>(R.id.food_name)
        food_description = root.findViewById<TextView>(R.id.food_decripstion)
        food_price = root.findViewById<TextView>(R.id.food_price)
        number_button = root.findViewById<ElegantNumberButton>(R.id.number_button)
        ratingBar= root.findViewById<RatingBar>(R.id.ratingBar)
        btnShowComment=root.findViewById<Button>(R.id.btnShowComment)

        (activity as AppCompatActivity).supportActionBar!!.title = Common.foodSelected!!.name

    }
}