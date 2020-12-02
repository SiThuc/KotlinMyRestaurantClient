package com.example.myrestaurantclientkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.asksira.loopingviewpager.LoopingPagerAdapter
import com.bumptech.glide.Glide
import com.example.myrestaurantclientkotlin.R
import com.example.myrestaurantclientkotlin.model.BestDealModel

class MyBestDealsAdapter(context: Context,
                         itemList:List<BestDealModel>,
                         isInfinite:Boolean):LoopingPagerAdapter<BestDealModel>(context, itemList, isInfinite) {

    // Return a view to LoopingViewPager
    override fun inflateView(viewType: Int, container: ViewGroup?, listPosition: Int): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.layout_best_deals_item, container, false)
    }

    //Bind view to ViewHolder
    override fun bindView(convertView: View?, position: Int, viewType: Int) {
        val imageView = convertView!!.findViewById<ImageView>(R.id.img_best_deal)
        val textView = convertView!!.findViewById<TextView>(R.id.txt_best_deal)

        //Set data
        Glide.with(context).load(itemList.get(position).image).into(imageView)
        textView.text = itemList.get(position).name
    }

}