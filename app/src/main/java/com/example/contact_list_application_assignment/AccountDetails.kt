package com.example.contact_list_application_assignment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_account_details.*

class AccountDetails : AppCompatActivity() {

    val db = FirebaseAuth.getInstance().currentUser?.uid?.let { FirebaseFirestore.getInstance().collection("users").document(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        db?.get()?.addOnCompleteListener { document ->
            if (document != null) {
                accountEmail.text = FirebaseAuth.getInstance().currentUser?.email.toString()
                accountName.text = document.result?.get("firstName").toString() + "  " + document.result?.get("lastName").toString()
            } else {
                Log.e("TAG", "No such document")
            }
        }
        accountEmail.text = FirebaseAuth.getInstance().currentUser?.email.toString()

        logoutCardView.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }

        termsCardView.setOnClickListener {
            startActivity(Intent(applicationContext, TermsAndConditionsPage::class.java))
        }

        //Code refereed from https://code.tutsplus.com/tutorials/how-to-code-a-bottom-navigation-bar-for-an-android-app--cms-30305#:~:text=Bottom%20navigation%20bars%20make%20it,refreshes%20the%20currently%20active%20view.
        //Bottom navigation view code to navigate to different pages
        bottomNavigationViewAccountPage.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                //Option to add a new contact
                R.id.addContact -> {
                    val i = Intent(applicationContext, AddContact::class.java)
                    startActivity(i)
                    return@setOnNavigationItemSelectedListener true
                }
                //option to search for a contact
                R.id.contactList -> {
                    finish()
                }
                //Go to account settings
                R.id.accountInfo -> {
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }

    }
}
