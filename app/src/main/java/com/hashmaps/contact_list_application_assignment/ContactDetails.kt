package com.hashmaps.contact_list_application_assignment

import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_contact_details.*
import kotlinx.android.synthetic.main.toolbar_contact.*
import java.io.FileNotFoundException

class ContactDetails : ContactsHelper() {
    private val db =
        FirebaseAuth.getInstance().currentUser?.uid?.let { FirebaseFirestore.getInstance().collection("users").document(it).collection("contacts") }
    private var rawContactIDPassed: String? = null
    private var contactFirebaseID: String? = null
    private var contactNamePassed: String? = null
    private var contactPhonePassed: String? = null
    private var contactEmailPassed: String? = null
    private var contactWorkPhonePassed: String? = null
    private var contactAddressPassed: String? = null
    private var contactPhotoURIPassed: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_details)
        contactFirebaseID = intent.getStringExtra("contactFirebaseID")

        //Instantiate the tool bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = contactNamePassed


        db?.document(contactFirebaseID!!)?.get()?.addOnCompleteListener { document ->
            contactNamePassed = document.result?.get("contactName")?.toString() ?: "-"
            contactPhonePassed = document.result?.get("phoneNumber")?.toString() ?: "-"
            contactEmailPassed = document.result?.get("contactEmail")?.toString() ?: "-"
            contactWorkPhonePassed = document.result?.get("contactWorkPhone")?.toString() ?: "-"
            contactAddressPassed = document.result?.get("contactAddress")?.toString() ?: "-"
            rawContactIDPassed = document.result?.get("rawContactID")?.toString() ?: "-"
            contactPhotoURIPassed = document.result?.get("contactPhotoURI")?.toString() ?: "-"

            //Sets value from the intent
            contactName.text = contactNamePassed
            contactPhone.text = contactPhonePassed
            contactEmail.text = contactEmailPassed
            contactAddress.text = contactAddressPassed
            contactWorkPhone.text = contactWorkPhonePassed

            try {
                val fd: AssetFileDescriptor? = contentResolver?.openAssetFileDescriptor(Uri.parse(contactPhotoURIPassed), "r")
                var photoAsBitmap: Bitmap? = null
                if (fd != null) {
                    photoAsBitmap = BitmapFactory.decodeStream(fd.createInputStream())
                };
                contactPhoto.setImageBitmap(photoAsBitmap)
            } catch (e: FileNotFoundException) {
                print("No Photo Continue")
            }
        }

        //Code refereed from https://code.tutsplus.com/tutorials/how-to-code-a-bottom-navigation-bar-for-an-android-app--cms-30305#:~:text=Bottom%20navigation%20bars%20make%20it,refreshes%20the%20currently%20active%20view.
        //Bottom navigation view code to navigate to different pages
        bottomNavigationViewContactDetailPage.setOnNavigationItemSelectedListener {
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
                    startActivity(Intent(this, AccountDetails::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //inflate the main menu to add the call, message, edit and delete items to the toolbar
        menuInflater.inflate(R.menu.contact_menu, menu)
        return true;
    }

    //Decides what to do when an option is selected in this activity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //Option to make a call
            R.id.action_call -> {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:" + contactPhone.text)
                startActivity(callIntent)
                return true
            }
            //option to send a message
            R.id.action_message -> {
                val messageIntent = Intent(Intent.ACTION_VIEW)
                messageIntent.data = Uri.parse("sms:" + contactPhone.text)
                startActivity(messageIntent)
            }
            //Option to edit contact
            R.id.action_editContact -> {
                val editContactIntent = Intent(applicationContext, EditContact::class.java)
                editContactIntent.putExtra("contactName", contactNamePassed)
                editContactIntent.putExtra("contactPhotoURI", contactPhotoURIPassed)
                editContactIntent.putExtra("contactWorkPhone", contactWorkPhonePassed)
                editContactIntent.putExtra("contactAddress", contactAddressPassed)
                editContactIntent.putExtra("contactPhone", contactPhonePassed)
                editContactIntent.putExtra("contactEmail", contactEmailPassed)
                editContactIntent.putExtra("rawContactID", rawContactIDPassed)
                editContactIntent.putExtra("contactFirebaseID", contactFirebaseID)
                startActivity(editContactIntent)
            }
            //Option to delete contact
            R.id.action_delete -> {
                //This will delete the contact from phone storage and from the Firestore DB
                rawContactIDPassed?.let { rawContactIDIt ->
                    if (deleteContact(rawContactIDIt)) {
                        contactFirebaseID?.let {
                            db?.document(it)?.delete()
                            Toast.makeText(this, "Contact Deleted", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //This is the action for back button
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}