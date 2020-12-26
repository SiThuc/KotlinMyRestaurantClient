package com.example.myrestaurantclientkotlin.ui.cart

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myrestaurantclientkotlin.R
import com.example.myrestaurantclientkotlin.adapter.MyCartAdapter
import com.example.myrestaurantclientkotlin.callback.IMyButtonCallback
import com.example.myrestaurantclientkotlin.common.Common
import com.example.myrestaurantclientkotlin.common.MySwipeHelper
import com.example.myrestaurantclientkotlin.database.CartDataSource
import com.example.myrestaurantclientkotlin.database.CartDatabase
import com.example.myrestaurantclientkotlin.database.LocalCartDataSource
import com.example.myrestaurantclientkotlin.eventbus.CountCartEvent
import com.example.myrestaurantclientkotlin.eventbus.HideFABCart
import com.example.myrestaurantclientkotlin.eventbus.UpdateItemInCart
import com.example.myrestaurantclientkotlin.model.Order
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.lang.StringBuilder
import java.util.*

class CartFragment : Fragment() {

    private var cartDataSource: CartDataSource? = null
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var recyclerViewState: Parcelable? = null

    private lateinit var cartviewModel: CartViewModel

    var txt_empty: TextView? = null
    var txt_total_price: TextView? = null
    var group_place_holder: CardView? = null
    var recycler_cart: RecyclerView? = null
    var adapter: MyCartAdapter? = null
    var btn_place_holder: MaterialButton? = null

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        EventBus.getDefault().postSticky(HideFABCart(true))
        cartviewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        cartviewModel.initCartDataSource(requireContext())

        val root = inflater.inflate(R.layout.cart_fragment, container, false)
        initView(root)
        initLocation()

        cartviewModel.getMutableLiveDataCartItem().observe(viewLifecycleOwner, Observer {
            if (it == null || it.isEmpty()) {
                recycler_cart!!.visibility = View.GONE
                group_place_holder!!.visibility = View.GONE
                txt_empty!!.visibility = View.VISIBLE
            } else {
                recycler_cart!!.visibility = View.VISIBLE
                group_place_holder!!.visibility = View.VISIBLE
                txt_empty!!.visibility = View.GONE

                adapter = MyCartAdapter(requireContext(), it)
                recycler_cart!!.adapter = adapter
            }
        })

        return root
    }

    private fun initLocation() {
        buildLocationRequest()
        buildLocationCallback()
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                currentLocation = p0!!.lastLocation
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(5000)
        locationRequest.setFastestInterval(3000)
        locationRequest.setSmallestDisplacement(10f)
    }

    private fun initView(root: View) {
        setHasOptionsMenu(true)

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDao())

        txt_empty = root.findViewById(R.id.txt_empty) as TextView
        txt_total_price = root.findViewById(R.id.txt_total_price) as TextView
        group_place_holder = root.findViewById(R.id.group_place_holder) as CardView
        btn_place_holder = root.findViewById(R.id.btn_place_order) as MaterialButton

        recycler_cart = root.findViewById(R.id.recycler_cart) as RecyclerView
        recycler_cart!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recycler_cart!!.layoutManager = layoutManager
        recycler_cart!!.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))

        val swipe = object : MySwipeHelper(requireContext(), recycler_cart!!, 200) {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(
                    MyButton(context!!,
                        "Delete",
                        30,
                        0,
                        Color.parseColor("#FF3C30"),
                        object : IMyButtonCallback {
                            override fun onClick(pos: Int) {
                                val deleteItem = adapter!!.getItemAtPosition(pos)
                                cartDataSource!!.deleteCart(deleteItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : SingleObserver<Int> {
                                        override fun onSubscribe(d: Disposable) {

                                        }

                                        override fun onSuccess(t: Int) {
                                            adapter!!.notifyItemRemoved(pos)
                                            sumCart()
                                            EventBus.getDefault().postSticky(CountCartEvent(true))
                                            Toast.makeText(
                                                context,
                                                "Delete item success",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        override fun onError(e: Throwable) {
                                            Toast.makeText(
                                                context,
                                                "" + e.message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    })
                            }

                        })
                )
            }

        }

        //Event when click Button

        btn_place_holder!!.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("On more step")

            val view = LayoutInflater.from(context).inflate(R.layout.layout_place_order, null)

            val edt_address = view.findViewById(R.id.edt_address) as EditText
            val edt_comment = view.findViewById(R.id.edt_comment) as EditText
            val txt_detail_address = view.findViewById(R.id.txt_detail_address) as TextView

            val rdi_deli_home = view.findViewById(R.id.rdi_deli_home) as RadioButton
            val rdi_deli_other = view.findViewById(R.id.rdi_deli_other) as RadioButton
            val rdi_deli_specific = view.findViewById(R.id.rdi_deli_specific) as RadioButton

            val rdi_pay_cod = view.findViewById(R.id.rdi_pay_cod) as RadioButton
            val rdi_pay_braintree = view.findViewById(R.id.rdi_pay_braintree) as RadioButton

            // Data
            edt_address.setText(Common.currentUser!!.address) // By Default, we selected rdi_home => show the user's address

            //Event
            rdi_deli_home.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    edt_address.setText(Common.currentUser!!.address)
                }
            }
            rdi_deli_other.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    edt_address.setText("")
                    txt_detail_address.visibility = View.GONE
                }
            }
            rdi_deli_specific.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    txt_detail_address.text = "Implement later with Goodle API"
                    txt_detail_address.visibility = View.VISIBLE
                }
            }

            builder.setView(view)
            builder.setNegativeButton("NO", { dialog, _ -> dialog.dismiss() })
                .setPositiveButton(
                    "YES",
                    { dialog, _ ->
                        if(rdi_pay_cod.isChecked)
                            paymentCOD(edt_address.text.toString(), edt_comment.text.toString())
                    })
            val dialog = builder.create()
            dialog.show()


        }

    }

    private fun paymentCOD(address: String, comment: String) {
            compositeDisposable.add(
                cartDataSource!!.getAllCart(Common.currentUser!!.uid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ cartItemList ->
                        // When we have all cartItems, we will get total price
                        cartDataSource!!.sumPrice(Common.currentUser!!.uid)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : SingleObserver<Double> {
                                override fun onSubscribe(d: Disposable) {

                                }

                                override fun onSuccess(t: Double) {
                                    val finalPrice = t
                                    val order = Order()
                                    order.userId = Common.currentUser!!.uid
                                    order.userName = Common.currentUser!!.name
                                    order.userPhone = Common.currentUser!!.phone
                                    order.shippingAddress = address
                                    order.comment = comment

                                    if(currentLocation != null){
                                        order.lat = currentLocation.latitude
                                        order.lng = currentLocation.longitude
                                    }

                                    order.cartItemList = cartItemList
                                    order.totalPayment = t
                                    order.finalPayment = finalPrice
                                    order.discount = 0
                                    order.isCod = true
                                    order.transactionId = "Cash On Delivery"

                                    // Submit to FIrebase
                                    writeOrderToFirebase(order)


                                }

                                override fun onError(e: Throwable) {
                                    Toast.makeText(context, ""+e.message, Toast.LENGTH_SHORT).show()
                                }

                            })
                    }, { throwable ->
                        Toast.makeText(context, "" + throwable.message, Toast.LENGTH_SHORT).show()
                    })
            )
    }

    private fun writeOrderToFirebase(order: Order) {
        FirebaseDatabase.getInstance()
            .getReference(Common.ORDER_REF)
            .child(Common.createOrderNumber())
            .setValue(order)
            .addOnFailureListener{e -> Toast.makeText(context, ""+e.message, Toast.LENGTH_SHORT).show()}
            .addOnCompleteListener { task ->
                //Clean cart
                if(task.isSuccessful){
                    cartDataSource!!.cleanCart(Common.currentUser!!.uid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : SingleObserver<Int> {
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onSuccess(t: Int) {
                                Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_SHORT).show()

                            }

                            override fun onError(e: Throwable) {
                                Toast.makeText(context, ""+e.message, Toast.LENGTH_SHORT).show()
                            }

                        }

                        )
                }
            }
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double): Any {
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        var result: String? = null
        try {
            val addressList = geoCoder.getFromLocation(latitude, longitude, 1)
            if (addressList != null && addressList.size > 0) {
                val address = addressList[0]
                val sb = StringBuilder(address.getAddressLine(0))
                result = sb.toString()
            }else
                result = "Address not found!"
            return result
        } catch (e: IOException) {
            return e.message!!
        }
    }

    private fun sumCart() {
        cartDataSource!!.sumPrice(Common.currentUser!!.uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: Double) {
                    txt_total_price!!.text = StringBuilder("Total:").append(t)
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                        Toast.makeText(context, "" + e.message!!, Toast.LENGTH_SHORT).show()
                }

            })
    }

    override fun onResume() {
        super.onResume()
        calculateTotalPrice()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.getMainLooper()
            )
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        // Send EventBus to HomeActivity to show the fabCart before fragment goes into Stopped State
        EventBus.getDefault().postSticky(HideFABCart(false))

        //Clear the compositeDispose from MyCartAdapter
        cartviewModel.onStop()

        // Clear the compositeDispose from this fragment
        compositeDisposable!!.clear()

        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        super.onStop()
    }

    //EventBus for listening when user click on the elegant Button
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateItemInCart(event: UpdateItemInCart) {
        if (event.cartItem != null) {
            recyclerViewState = recycler_cart!!.layoutManager!!.onSaveInstanceState()
            cartDataSource!!.updateCart(event.cartItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(t: Int) {
                        calculateTotalPrice();
                        recycler_cart!!.layoutManager!!.onRestoreInstanceState(recyclerViewState)
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "[UPDATE CART] " + e.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                })
        }
    }

    private fun calculateTotalPrice() {
        cartDataSource!!.sumPrice(Common.currentUser!!.uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: Double) {
                    txt_total_price!!.text = StringBuilder("€")
                        .append(Common.formatPrice(t))
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                        Toast.makeText(context, "[SUM CART]" + e.message, Toast.LENGTH_SHORT).show()

                }

            })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu!!.findItem(R.id.action_settings).setVisible(false)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater!!.inflate(R.menu.cart_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item!!.itemId == R.id.action_clear_cart)
            cartDataSource!!.cleanCart(Common.currentUser!!.uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(t: Int) {
                        Toast.makeText(context, "Clear Cart Success", Toast.LENGTH_SHORT)
                        EventBus.getDefault().postSticky(CountCartEvent(true))
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                    }

                })
        return true
        return super.onOptionsItemSelected(item)
    }

}

