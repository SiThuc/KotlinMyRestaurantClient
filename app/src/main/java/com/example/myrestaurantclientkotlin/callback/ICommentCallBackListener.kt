package com.example.myrestaurantclientkotlin.callback

import com.example.myrestaurantclientkotlin.model.CommentModel

interface ICommentCallBackListener {
    fun onLoadCommentSuccess(commentList: List<CommentModel>)
    fun onCommentLoadFailed(message: String)
}