package com.example.contact_list_application_assignment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_add_contact.*


class AddContact : ContactsHelper() {
    var bitmapImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        //Photo selector
        addContactPhoto.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1001)
        }

        //Cancel button takes the user back to the contact list
        addContactCancel.setOnClickListener {
            finish()
        }

        //Save button OnClick listener
        addContactSave.setOnClickListener {
            val contactName = addContactName.text.toString()
            val contactPhone = addContactPhone.text.toString()
            val contactAddress = addContactAddress.text.toString()
            val contactWorkPhone = addContactWorkPhone.text.toString()
            val contactEmail = addContactEmail.text.toString()

            //Adds contact to the Firestore DB
            if (contactName.isNotEmpty() || contactPhone.isNotEmpty()) {
                bitmapImage?.let { it1 ->
                    if (!addContact(contactName, contactPhone, contactAddress, contactWorkPhone, contactEmail, it1)) {
                        Toast.makeText(this, "Contact was not saved", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Contact was saved", Toast.LENGTH_LONG).show()
                        finish()
                    }
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
            bitmapImage = selectedImage?.let { ImageDecoder.createSource(this.contentResolver, it) }?.let { ImageDecoder.decodeBitmap(it) }
            print(bitmapImage)
        }
    }
}