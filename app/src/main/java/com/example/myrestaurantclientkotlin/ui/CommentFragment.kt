package com.example.myrestaurantclientkotlin.ui

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myrestaurantclientkotlin.R
import com.example.myrestaurantclientkotlin.adapter.MyCommentAdapter
import com.example.myrestaurantclientkotlin.callback.ICommentCallBackListener
import com.example.myrestaurantclientkotlin.common.Common
import com.example.myrestaurantclientkotlin.model.CommentModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog

class CommentFragment : BottomSheetDialogFragment(), ICommentCallBackListener {

    private var commentViewModel: CommentViewModel? = null
    private lateinit var listener: ICommentCallBackListener

    private var recyler_comment: RecyclerView? = null

    private var dialog: AlertDialog? = null


    init {
        listener = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.comment_fragment, container, false)
        initViews(itemView)
        loadCommentFromFirebase()
        commentViewModel!!.commentListLiveData!!.observe(viewLifecycleOwner, Observer {
            val adapter = MyCommentAdapter(requireContext(), it)
            recyler_comment!!.adapter = adapter
        })
        return itemView
    }

    private fun loadCommentFromFirebase() {
        dialog!!.show()

        val commentModels = ArrayList<CommentModel>()

        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
            .child(Common.foodSelected!!.id!!)
            .orderByChild("commentTimeStamp")
            .limitToLast(100)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (commentSnapShot in snapshot.children) {
                        val commentModel = commentSnapShot.getValue(CommentModel::class.java)
                        commentModels.add(commentModel!!)
                    }

                    listener.onLoadCommentSuccess(commentModels)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onCommentLoadFailed(error.message)
                }

            })


    }

    private fun initViews(itemView: View?) {
        commentViewModel = ViewModelProvider(this).get(CommentViewModel::class.java)
        dialog = SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()

        recyler_comment = itemView!!.findViewById(R.id.recycler_comment) as RecyclerView
        recyler_comment!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        recyler_comment!!.layoutManager = layoutManager
        recyler_comment!!.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                layoutManager.orientation
            )
        )
    }

    override fun onLoadCommentSuccess(commentList: List<CommentModel>) {
        dialog!!.dismiss()
        commentViewModel!!.setCommentList(commentList)
    }

    override fun onCommentLoadFailed(message: String) {
        Toast.makeText(requireContext(), "" + message, Toast.LENGTH_SHORT).show()
        dialog!!.dismiss()
    }

    companion object {
        private var instance: CommentFragment? = null

        fun getInstance(): CommentFragment {
            if (instance == null)
                instance = CommentFragment()
            return instance!!
        }
    }


}