package com.example.contact_list_application_assignment

import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_details)

        val contactNamePassed = intent.getStringExtra("contactName") ?: "-"
        val contactPhonePassed = intent.getStringExtra("contactPhone") ?: "-"
        val contactEmailPassed = intent.getStringExtra("contactEmail") ?: "-"
        val contactWorkPhonePassed = intent.getStringExtra("contactWorkPhone") ?: "-"
        val contactAddressPassed = intent.getStringExtra("contactAddress") ?: "-"
        rawContactIDPassed = intent.getStringExtra("rawContactID") ?: "-"
        contactFirebaseID = intent.getStringExtra("contactFirebaseID") ?: "-"

        //Instantiate the tool bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = contactNamePassed

        //Sets value from the intent
        contactName.text = contactNamePassed
        contactPhone.text = contactPhonePassed
        contactEmail.text = contactEmailPassed
        contactAddress.text = contactAddressPassed
        contactWorkPhone.text = contactWorkPhonePassed

        try {
            val fd: AssetFileDescriptor? = contentResolver?.openAssetFileDescriptor(Uri.parse(intent.getStringExtra("contactPhotoURI")), "r")
            var photoAsBitmap: Bitmap? = null
            if (fd != null) {
                photoAsBitmap = BitmapFactory.decodeStream(fd.createInputStream())
            };
            contactPhoto.setImageBitmap(photoAsBitmap)
        } catch (e: FileNotFoundException) {
            print("No Photo Continue")
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

            }
            //Option to delete contact
            R.id.action_delete -> {
                //This will delete the contact from phone storage and from the Firestore DB
                rawContactIDPassed?.let { rawContactIDIt ->
                    if (deleteContact(rawContactIDIt)) {
                        contactFirebaseID?.let { db?.document(it)?.delete()
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
}