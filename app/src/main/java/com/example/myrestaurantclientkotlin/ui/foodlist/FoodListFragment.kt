package com.example.myrestaurantclientkotlin.ui.foodlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myrestaurantclientkotlin.R
import com.example.myrestaurantclientkotlin.adapter.MyCategoriesAdapter
import com.example.myrestaurantclientkotlin.adapter.MyFoodListAdapter
import com.example.myrestaurantclientkotlin.common.Common

class FoodListFragment : Fragment() {

    private lateinit var foodListViewModel: FoodListViewModel

    private var recyclerView: RecyclerView? = null
    private var adapter: MyFoodListAdapter? = null
    private var layoutAnimationController: LayoutAnimationController? = null

    override fun onStop() {
        if(adapter != null)
            adapter!!.onStop()
        super.onStop()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodListViewModel = ViewModelProvider(this).get(FoodListViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_list, container, false)

        initView(root)

        foodListViewModel.getFoodList().observe(viewLifecycleOwner, Observer {
            adapter = MyFoodListAdapter(requireContext(), it)
            recyclerView!!.adapter = adapter
            recyclerView!!.layoutAnimation = layoutAnimationController
        })

        return root
    }

    private fun initView(root: View) {
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        recyclerView = root.findViewById<RecyclerView>(R.id.recycler_foodlist)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        (activity as AppCompatActivity).supportActionBar!!.title = Common.categorySelected!!.name

    }
}