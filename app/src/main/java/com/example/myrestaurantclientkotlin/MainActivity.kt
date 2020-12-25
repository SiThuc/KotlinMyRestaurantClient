package com.example.myrestaurantclientkotlin

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import com.example.myrestaurantclientkotlin.common.Common
import com.example.myrestaurantclientkotlin.model.UserModel
import com.example.myrestaurantclientkotlin.utils.toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dmax.dialog.SpotsDialog
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var dialog: AlertDialog
    private var providers: List<AuthUI.IdpConfig>? = null
    private lateinit var userRef: DatabaseReference
    private val compositeDisposable = CompositeDisposable()
    private lateinit var token: String

    companion object {
        private val APP_REQUEST_CODE = 1601
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        if (listener != null)
            firebaseAuth.removeAuthStateListener(listener)
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        providers = Arrays.asList<AuthUI.IdpConfig>(AuthUI.IdpConfig.PhoneBuilder().build())
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REF)
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setCancelable(false).setContext(this).build()

        listener = FirebaseAuth.AuthStateListener { firebaseAuth ->

            Dexter.withContext(this@MainActivity)
                .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object: PermissionListener{
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        val user = firebaseAuth.currentUser
                        if (user != null){
                            checkUserFromFirebase(user)
                        }else{
                            phoneLogin()
                        }

                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        toast("You muss accept this permission to use app")

                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {

                    }
                }).check()
        }
    }

    private fun phoneLogin() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers!!).build(), APP_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == APP_REQUEST_CODE){
            val response = IdpResponse.fromResultIntent(data)
            if(resultCode == Activity.RESULT_OK){
                val user = FirebaseAuth.getInstance().currentUser
            }else{
                Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserFromFirebase(user: FirebaseUser) {
        dialog.show()
        userRef.child(user.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            val userModel = snapshot.getValue(UserModel::class.java)
                            goToHomeActivity(userModel)
                        }else{
                            showRegisterDialog(user)
                        }

                        dialog.dismiss()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@MainActivity, ""+error.message, Toast.LENGTH_SHORT).show()
                    }
                })

    }

    private fun showRegisterDialog(user: FirebaseUser) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("REGISTER")
        builder.setMessage("Please fill information")

        val itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null)

        val edt_name = itemView.findViewById<EditText>(R.id.edt_name)
        val edt_address = itemView.findViewById<EditText>(R.id.edt_address)
        val edt_phone = itemView.findViewById<EditText>(R.id.edt_phone)

        //Set phone number for editText
        edt_phone.setText(user.phoneNumber.toString())

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL") {dialogInterface, i -> dialogInterface.dismiss()}
        builder.setPositiveButton("REGISTER") {dialogInterface , i ->
            if(TextUtils.isDigitsOnly(edt_name.text.toString())){
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }else if (TextUtils.isDigitsOnly(edt_address.text.toString())){
                Toast.makeText(this, "Please enter your address", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }


        val userModel = UserModel()
        userModel.uid = user.uid
        userModel.name = edt_name.text.toString()
        userModel.address = edt_address.text.toString()
        userModel.phone = edt_phone.text.toString()

        userRef.child(user.uid)
            .setValue(userModel)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    dialogInterface.dismiss()
                    Toast.makeText(this,
                    "Congratulation! Regisrer success!",
                    Toast.LENGTH_SHORT).show()

                    goToHomeActivity(userModel)
                }
            }
        }

        val dialog = builder.create()
        dialog.show()

    }

    private fun goToHomeActivity(userModel: UserModel?) {
        Common.currentUser = userModel!!
        startActivity(Intent(this, HomeActivity::class.java))
        finish()

    }
}