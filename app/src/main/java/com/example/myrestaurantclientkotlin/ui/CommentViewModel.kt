package com.example.myrestaurantclientkotlin.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myrestaurantclientkotlin.model.CommentModel

class CommentViewModel : ViewModel() {

    var commentListLiveData: MutableLiveData<List<CommentModel>>? = null

    init {
        commentListLiveData = MutableLiveData()
    }

    fun setCommentList(listComment: List<CommentModel>) {
        commentListLiveData!!.value = listComment
    }
}