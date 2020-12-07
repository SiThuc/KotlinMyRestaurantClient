package com.example.myrestaurantclientkotlin.ui.fooddetail

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
import com.example.myrestaurantclientkotlin.model.CommentModel
import com.example.myrestaurantclientkotlin.model.FoodModel
import com.example.myrestaurantclientkotlin.ui.CommentFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog
import java.lang.StringBuilder

class FoodDetailFragment : Fragment() {

    private lateinit var foodDetailViewModel: FoodDetailViewModel

    private var img_food: ImageView? = null
    private var btnCart: CounterFab? = null
    private var btnRating: FloatingActionButton? = null
    private var food_name: TextView? = null
    private var food_description: TextView? = null
    private var food_price: TextView? = null
    private var number_button: ElegantNumberButton? = null
    private var ratingBar: RatingBar? = null
    private var btnShowComment: Button? = null
    private var rdi_group_size: RadioGroup? = null

    private var waitingDialog: Dialog? = null


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

        foodDetailViewModel.getComment().observe(viewLifecycleOwner, Observer {
            submitRatingToFirebase(it)
        })

        return root
    }

    private fun submitRatingToFirebase(commentModel: CommentModel?) {
        waitingDialog!!.show()

        // Firstly, we will submit to Comment Ref
        FirebaseDatabase.getInstance()
            .getReference(Common.COMMENT_REF)
            .child(Common.foodSelected!!.id!!)
            .push()
            .setValue(commentModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //We upload the value of Rating to foodModel, then update it
                    addRatingToFood(commentModel!!.ratingValue.toDouble())
                }
                waitingDialog!!.dismiss()
            }

    }

    private fun addRatingToFood(ratingValue: Double) {
        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REF)
            .child(Common.categorySelected!!.menu_id!!)
            .child("foods")
            .child(Common.foodSelected!!.key!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val foodModel = snapshot.getValue(FoodModel::class.java)
                        foodModel!!.key = Common.foodSelected!!.key

                        //Apply Rating
                        val sumRating = foodModel.ratingValue + ratingValue
                        val ratingCount = foodModel.ratingCount + 1
                        val result = sumRating / ratingCount

                        val updateData = HashMap<String, Any>()
                        updateData["ratingValue"] = sumRating
                        updateData["ratingCount"] = ratingCount
                        updateData["averageRating"] = result

                        //Update data to the foodModel
                        foodModel.ratingValue = sumRating
                        foodModel.ratingCount = ratingCount
                        foodModel.averageRating = result

                        snapshot.ref
                            .updateChildren(updateData)
                            .addOnCompleteListener { task ->
                                Common.foodSelected = foodModel
                                foodDetailViewModel.setFoodModel(foodModel)
                                Toast.makeText(
                                    requireContext(),
                                    "Successfully! Thank you for commenting",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "" + error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun displayInfo(it: FoodModel?) {
        Glide.with(requireContext()).load(it!!.image).into(img_food!!)
        food_name!!.text = StringBuilder(it.name)
        food_description!!.text = StringBuilder(it.description)
        food_price!!.text = StringBuilder(it.price.toString())
        ratingBar!!.rating = it.averageRating.toFloat()

        //Create RadioButtons and adding them into RadioGroup Size
        for (sizeModel in it!!.size) {
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener { compoundButton, b ->
                if (b)
                    Common.foodSelected!!.userSelectedSize = sizeModel
                calculateTotalPrice()
            }
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
            radioButton.layoutParams = params
            radioButton.text = sizeModel.name
            radioButton.tag = sizeModel.price

            rdi_group_size!!.addView(radioButton)
        }

        //Default first radiobutton select
        if (rdi_group_size!!.childCount > 0) {
            val radioButton = rdi_group_size!!.getChildAt(0) as RadioButton
            radioButton.isChecked = true
        }
    }

    private fun calculateTotalPrice() {
        var totalPrice = Common.foodSelected!!.price.toDouble()
        var displayPrice = 0.0

        //Size
        totalPrice += Common.foodSelected!!.userSelectedSize!!.price.toDouble()

        displayPrice = totalPrice * number_button!!.number.toInt()
        displayPrice = Math.round(displayPrice * 100.0) / 100.0

        food_price!!.text = StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
    }

    private fun initView(root: View) {

        waitingDialog =
            SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()

        img_food = root.findViewById<ImageView>(R.id.img_food_detail)
        btnCart = root.findViewById<CounterFab>(R.id.btnCart)
        btnRating = root.findViewById<FloatingActionButton>(R.id.btn_rating)
        food_name = root.findViewById<TextView>(R.id.food_name)
        food_description = root.findViewById<TextView>(R.id.food_decripstion)
        food_price = root.findViewById<TextView>(R.id.food_price)
        number_button = root.findViewById<ElegantNumberButton>(R.id.number_button)
        ratingBar = root.findViewById<RatingBar>(R.id.ratingBar)
        btnShowComment = root.findViewById<Button>(R.id.btnShowComment)
        rdi_group_size = root.findViewById<RadioGroup>(R.id.rdi_group_size)

        (activity as AppCompatActivity).supportActionBar!!.title = Common.foodSelected!!.name

        btnRating!!.setOnClickListener(View.OnClickListener {
            showDialogComment()
        })

        //Show Comment List
        btnShowComment!!.setOnClickListener({
            val commentFragment = CommentFragment.getInstance()
            commentFragment.show(requireActivity().supportFragmentManager, "CommentFragment")
        })

    }

    private fun showDialogComment() {
        // Show the dialog comment here
        var builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Rating Food")
        builder.setMessage("Please leave a comment below...")

        val itemView =
            LayoutInflater.from(requireContext()).inflate(R.layout.layout_rating_comment, null)
        var rating_bar_comment = itemView.findViewById<RatingBar>(R.id.rating_bar)
        var comment_rating_bar = itemView.findViewById<EditText>(R.id.edt_comment)

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
        builder.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
            val commentModel = CommentModel()
            commentModel.name = Common.currentUser!!.name
            commentModel.uid = Common.currentUser!!.uid
            commentModel.comment = comment_rating_bar.text.toString()
            commentModel.ratingValue = rating_bar_comment.rating

            val serverTimestamp = HashMap<String, Any>()
            serverTimestamp["timestamp"] = ServerValue.TIMESTAMP
            commentModel.commentTimestamp = serverTimestamp

            foodDetailViewModel.setCommentModel(commentModel)
        }

        builder.create()
        builder.show()

    }
}

