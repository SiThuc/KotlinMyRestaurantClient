package com.example.myrestaurantclientkotlin.adapter

import android.content.Context
import android.media.Rating
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myrestaurantclientkotlin.R
import com.example.myrestaurantclientkotlin.model.CommentModel

class MyCommentAdapter(
    val context: Context,
    var commentList: List<CommentModel>
) : RecyclerView.Adapter<MyCommentAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var userName: TextView? = null
        var commentDate: TextView? = null
        var comment: TextView? = null
        var userRating: RatingBar? = null

        init {
            userName = itemView.findViewById(R.id.txt_username) as TextView
            commentDate = itemView.findViewById(R.id.txt_comment_date) as TextView
            comment = itemView.findViewById(R.id.txt_comment) as TextView
            userRating = itemView.findViewById(R.id.rating_bar_cm) as RatingBar
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.layout_comment_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val timeStamp =
            commentList.get(position).commentTimestamp!!["timestamp"].toString().toLong()
        holder.commentDate!!.text = DateUtils.getRelativeTimeSpanString(timeStamp)
        holder.userName!!.text = commentList.get(position).name
        holder.comment!!.text = commentList.get(position).comment
        holder.userRating!!.rating = commentList.get(position).ratingValue
    }

    override fun getItemCount(): Int {
        return commentList.size
    }


}