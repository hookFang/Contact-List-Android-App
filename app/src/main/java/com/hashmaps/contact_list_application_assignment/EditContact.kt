package com.hashmaps.contact_list_application_assignment

import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_edit_contact.*
import kotlinx.android.synthetic.main.toolbar_contact.*
import java.io.FileNotFoundException

class EditContact : ContactsHelper() {
    private val db =
        FirebaseAuth.getInstance().currentUser?.uid?.let { FirebaseFirestore.getInstance().collection("users").document(it).collection("contacts") }
    private var contactNamePassed: String? = null
    private var contactPhonePassed: String? = null
    private var contactEmailPassed: String? = null
    private var contactWorkPhonePassed: String? = null
    private var contactAddressPassed: String? = null
    private var contactPhotoURIPassed: String? = null
    private var globalFireBaseContactID: String? = null


    var bitmapImage: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_contact)

        //Setup toolbar to set the back button
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Contact"

        contactNamePassed = intent.getStringExtra("contactName")
        contactPhonePassed = intent.getStringExtra("contactPhone")
        contactEmailPassed = intent.getStringExtra("contactEmail")
        contactWorkPhonePassed = intent.getStringExtra("contactWorkPhone")
        contactAddressPassed = intent.getStringExtra("contactAddress")
        val rawContactIDPassed = intent.getStringExtra("rawContactID")
        val contactFirebaseID = intent.getStringExtra("contactFirebaseID")
        contactPhotoURIPassed = intent.getStringExtra("contactPhotoURI")
        globalFireBaseContactID = contactFirebaseID

        //Set values
        contactNameTextView.text = contactNamePassed
        if(contactNamePassed != "-") {
            editContactName.setText(contactNamePassed)
        }
        if(contactAddressPassed != "-") {
            editContactAddress.setText(contactAddressPassed)
        }
        if(contactEmailPassed != "-") {
            editContactEmail.setText(contactEmailPassed)
        }
        if(contactPhonePassed != "-") {
            editContactPhone.setText(contactPhonePassed)
        }
        if(contactWorkPhonePassed != "-") {
            editContactWorkPhone.setText(contactWorkPhonePassed)
        }

        //Set photo if there is any
        try {
            val fd: AssetFileDescriptor? = contentResolver?.openAssetFileDescriptor(Uri.parse(contactPhotoURIPassed), "r")
            if (fd != null) {
                bitmapImage = BitmapFactory.decodeStream(fd.createInputStream())
            };
            editContactPhoto.setImageBitmap(bitmapImage)
        } catch (e: FileNotFoundException) {
            print("No Photo Continue")
        }

        //Photo selector
        editContactPhoto.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1001)
        }

        editContactSave.setOnClickListener {
            val contactName = editContactName.text.toString()
            val contactPhone = editContactPhone.text.toString()
            val contactAddress = editContactAddress.text.toString()
            val contactWorkPhone = editContactWorkPhone.text.toString()
            val contactEmail = editContactEmail.text.toString()

            //Adds contact to the Firestore DB
            if (contactName.isNotEmpty() || contactPhone.isNotEmpty()) {
                if (!editContact(rawContactIDPassed!!, contactFirebaseID!!, contactName, contactPhone, contactAddress, contactWorkPhone, contactEmail, bitmapImage)) {
                    Toast.makeText(this, "Contact was not saved", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Contact was saved", Toast.LENGTH_LONG).show()
                    val contactDetails = Intent(applicationContext, ContactDetails::class.java)
                    contactDetails.putExtra("contactFirebaseID", contactFirebaseID)
                    startActivity(contactDetails)
                }
            } else {
                Toast.makeText(this, "Please make sure the Name and Phone number and filled in", Toast.LENGTH_LONG).show()
            }
        }

    }

    //To get the image from the gallery
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && data != null) {
            val selectedImage: Uri? = data.data
            bitmapImage = if (Build.VERSION.SDK_INT >= 29){
                //To handle old versions
                selectedImage?.let { ImageDecoder.createSource(this.contentResolver, it) }?.let { ImageDecoder.decodeBitmap(it) }
            } else{
                //For older versions
                MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
            }
            print(bitmapImage)
            editContactPhoto.setImageBitmap(bitmapImage)
        }
    }

    //This is the action for back button
    override fun onSupportNavigateUp(): Boolean {
        val contactDetails = Intent(applicationContext, ContactDetails::class.java)
        contactDetails.putExtra("contactFirebaseID", globalFireBaseContactID)
        startActivity(contactDetails)
        return true
    }
}