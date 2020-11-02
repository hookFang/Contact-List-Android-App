package com.example.contact_list_application_assignment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.platforminfo.GlobalLibraryVersionRegistrar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainPage : ReadContactsHelper() {

    var TAG = MainPage::class.qualifiedName
    val PERMISSIONS_REQUEST_READ_CONTACTS = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        GlobalScope.launch {
            // Code referred from - https://medium.com/@manuaravindpta/fetching-contacts-from-device-using-kotlin-6c6d3e76574f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //So if we don't have permission we request for permissions from the user, this will execute the overridden onRequestPermissionsResult
                requestPermissions(
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    PERMISSIONS_REQUEST_READ_CONTACTS
                )
                //callback onRequestPermissionsResult
            } else {
                //If we have permission we run our function directly
                readContacts()
            }
        }

        //Set RecyclerView to use linear layout
        //contactRecyclerView.layoutManager = LinearLayoutManager(this)

        //Get all the contacts in phone
        //ContactsContract.Contacts.CONTENT_URI
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readContacts()
            } else {
                Toast.makeText(
                    this,
                    "Cannot read contact from you're phone! Please enable permissions to read contacts.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}