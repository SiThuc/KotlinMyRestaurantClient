package com.example.myrestaurantclientkotlin.ui.fooddetail

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.example.myrestaurantclientkotlin.R
import com.example.myrestaurantclientkotlin.common.Common
import com.example.myrestaurantclientkotlin.database.CartDatabase
import com.example.myrestaurantclientkotlin.database.CartItem
import com.example.myrestaurantclientkotlin.database.LocalCartDataSource
import com.example.myrestaurantclientkotlin.eventbus.CountCartEvent
import com.example.myrestaurantclientkotlin.model.CommentModel
import com.example.myrestaurantclientkotlin.model.FoodModel
import com.example.myrestaurantclientkotlin.ui.CommentFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class FoodDetailFragment : Fragment(), TextWatcher {

    private lateinit var foodDetailViewModel: FoodDetailViewModel

    private lateinit var addonBottomSheetDialog: BottomSheetDialog

    private val compositeDisposable = CompositeDisposable()
    private lateinit var cartDataSource: LocalCartDataSource

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
    private var img_add_on: ImageView? = null
    private var chip_group_user_selected_addon: ChipGroup? = null

    //Addon Layout
    private var chip_group_addon: ChipGroup? = null
    private var edt_search_addon: EditText? = null

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

        //Addon
        if (Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0) {
            for (addonModel in Common.foodSelected!!.userSelectedAddon!!)
                totalPrice += addonModel.price!!.toDouble()
        }

        //Size
        totalPrice += Common.foodSelected!!.userSelectedSize!!.price.toDouble()

        displayPrice = totalPrice * number_button!!.number.toInt()
        displayPrice = Math.round(displayPrice * 100.0) / 100.0

        food_price!!.text = StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
    }

    private fun initView(root: View) {

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDao())

        addonBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        val layout_user_selected_addon:View = layoutInflater.inflate(R.layout.layout_addon_display, null)
        chip_group_addon = layout_user_selected_addon.findViewById(R.id.chip_group_addon) as ChipGroup
        edt_search_addon = layout_user_selected_addon.findViewById(R.id.edt_search) as EditText
        addonBottomSheetDialog.setContentView(layout_user_selected_addon)

        addonBottomSheetDialog.setOnDismissListener { dialogInterface ->
            displayUserSelectedAddon()
            calculateTotalPrice()
        }

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
        img_add_on = root.findViewById<ImageView>(R.id.img_add_addon)
        chip_group_user_selected_addon = root.findViewById<ChipGroup>(R.id.chip_group_user_selected_addon)

        //Event when we click on Addon Image
        img_add_on!!.setOnClickListener {
            if (Common.foodSelected!!.addon != null) {
                displayAllAddon()
                addonBottomSheetDialog.show()
            }
        }

        (activity as AppCompatActivity).supportActionBar!!.title = Common.foodSelected!!.name

        btnRating!!.setOnClickListener(View.OnClickListener {
            showDialogComment()
        })

        //Show Comment List
        btnShowComment!!.setOnClickListener({
            val commentFragment = CommentFragment.getInstance()
            commentFragment.show(requireActivity().supportFragmentManager, "CommentFragment")
        })

        btnCart!!.setOnClickListener {
            val cartItem = CartItem()

            cartItem.uid = Common.currentUser!!.uid.toString()
            cartItem.userPhone = Common.currentUser!!.phone

            cartItem.foodId = Common.foodSelected!!.id!!
            cartItem.foodName = Common.foodSelected!!.name
            cartItem.foodImage = Common.foodSelected!!.image
            cartItem.foodPrice = Common.foodSelected!!.price.toDouble()
            cartItem.foodQuantity = number_button!!.number.toInt()
            cartItem.foodExtraPrice = Common.calculateExtraPrice(Common.foodSelected!!.userSelectedSize, Common.foodSelected!!.userSelectedAddon)

            if(Common.foodSelected!!.userSelectedAddon != null)
                cartItem.foodAddon = Gson().toJson(Common.foodSelected!!.userSelectedAddon)
            else
                cartItem.foodAddon = "Default"


            if(Common.foodSelected!!.userSelectedSize != null)
                cartItem.foodSize = Gson().toJson(Common.foodSelected!!.userSelectedSize)
            else
                cartItem.foodSize = "Default"

            cartDataSource.getItemWithAllOptionsInCart(
                Common.currentUser!!.uid!!,
                cartItem.foodId,
                cartItem.foodSize!!,
                cartItem.foodAddon!!
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<CartItem> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(t: CartItem) {
                        if (t.equals(cartItem)) {

                            // If item already in database, just update
                            t.foodExtraPrice = cartItem.foodExtraPrice
                            t.foodAddon = cartItem.foodAddon
                            t.foodSize = cartItem.foodSize
                            t.foodQuantity = t.foodQuantity!!.plus(cartItem.foodQuantity!!)

                            cartDataSource.updateCart(t)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : SingleObserver<Int> {
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onSuccess(t: Int) {
                                        Toast.makeText(
                                            context,
                                            "Update Cart Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(
                                            context,
                                            "[UPDATE CART] " + e.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                })
                        } else {

                            // If Item is not available in database, just insert
                            compositeDisposable.add(
                                cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Toast.makeText(
                                            context,
                                            "Add to cart success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }, { t: Throwable? ->
                                        Toast.makeText(
                                            context,
                                            "[INSERT CART]" + t!!.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                            )

                        }

                    }

                    override fun onError(e: Throwable) {
                        if (e.message!!.contains("empty")) {
                            compositeDisposable.add(
                                cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Toast.makeText(
                                            context,
                                            "Add to cart success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }, { t: Throwable? ->
                                        Toast.makeText(
                                            context,
                                            "[INSERT CART]" + t!!.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                            )
                        } else
                            Toast.makeText(context, "[CART ERROR!!]", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }

    private fun displayAllAddon() {
        if (Common.foodSelected!!.addon!!.size > 0) {
            chip_group_addon!!.clearCheck()
            chip_group_addon!!.removeAllViews()
            edt_search_addon!!.addTextChangedListener(this)

            //Add on View
            for (addonModel in Common.foodSelected!!.addon!!) {
                val chip = layoutInflater.inflate(R.layout.layout_chip, null) as Chip
                chip.text = StringBuilder(addonModel.name!!).append("(+€").append(addonModel.price).append(")").toString()

                chip.setOnCheckedChangeListener { compoundButton, b ->

                    if (b) {
                        if (Common.foodSelected!!.userSelectedAddon == null)
                            Common.foodSelected!!.userSelectedAddon = ArrayList()
                        Common.foodSelected!!.userSelectedAddon!!.add(addonModel)
                    }
                }
                chip_group_addon!!.addView(chip)
            }
        }
    }

    private fun displayUserSelectedAddon() {
        if (Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0
        ) {
            chip_group_user_selected_addon!!.removeAllViews()
            for (addonModel in Common.foodSelected!!.userSelectedAddon!!) {
                val chip = layoutInflater.inflate(R.layout.layout_chip_with_delete, null) as Chip
                chip.text = StringBuilder(addonModel.name).append("(+€").append(addonModel.price)
                    .append(")").toString()
                chip.isClickable = false
                chip.setOnCloseIconClickListener { view ->
                    //Remove when user select delete
                    chip_group_user_selected_addon!!.removeView(view)
                    Common.foodSelected!!.userSelectedAddon!!.remove(addonModel)
                    calculateTotalPrice()
                }
                chip_group_user_selected_addon!!.addView(chip)
            }
        } else
            chip_group_user_selected_addon!!.removeAllViews()
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

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
        chip_group_addon!!.clearCheck()
        chip_group_addon!!.removeAllViews()
        for (addonModel in Common.foodSelected!!.addon!!) {
            if (addonModel.name!!.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
                chip.text = StringBuilder(addonModel.name!!).append("(+€").append(addonModel.price).append(")").toString()
                chip.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        if (Common.foodSelected!!.userSelectedAddon == null)
                            Common.foodSelected!!.userSelectedAddon = ArrayList()
                        Common.foodSelected!!.userSelectedAddon!!.add(addonModel)
                    }
                }
                chip_group_addon!!.addView(chip)
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {

    }

}

