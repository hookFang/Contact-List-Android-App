package com.hashmaps.contact_list_application_assignment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import kotlinx.android.synthetic.main.activity_terms_and_conditons_page.*
import kotlinx.android.synthetic.main.toolbar_contact.*

class TermsAndConditionsPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditons_page)

        //Instantiate the tool bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "HashMaps Terms & Conditions"

        termsAndConditonsTextView.movementMethod = ScrollingMovementMethod()

        //Code refereed from https://code.tutsplus.com/tutorials/how-to-code-a-bottom-navigation-bar-for-an-android-app--cms-30305#:~:text=Bottom%20navigation%20bars%20make%20it,refreshes%20the%20currently%20active%20view.
        //Bottom navigation view code to navigate to different pages
        bottomNavigationViewTermsPage.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                //Option to add a new contact
                R.id.addContact -> {
                    val i = Intent(applicationContext, AddContact::class.java)
                    startActivity(i)
                    return@setOnNavigationItemSelectedListener true
                }
                //option to search for a contact
                R.id.contactList -> {
                    val i = Intent(applicationContext, MainPageActivity::class.java)
                    startActivity(i)
                    return@setOnNavigationItemSelectedListener true
                }
                //Go to account settings
                R.id.accountInfo -> {
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
    }

    //This is the action for back button
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}