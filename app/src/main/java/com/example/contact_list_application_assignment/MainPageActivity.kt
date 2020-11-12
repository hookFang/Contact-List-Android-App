package com.example.contact_list_application_assignment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main_page.*
import kotlinx.android.synthetic.main.contact_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainPageActivity : ReadContactsHelper() {

    private var TAG = MainPageActivity::class.qualifiedName
    private val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    private val db = FirebaseAuth.getInstance().currentUser?.uid?.let { FirebaseFirestore.getInstance().collection("users").document(it).collection("contacts") }
    private var adapter: MainPageActivity.ContactAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        GlobalScope.launch {
            // Code referred from - https://medium.com/@manuaravindpta/fetching-contacts-from-device-using-kotlin-6c6d3e76574f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission(
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED  || checkSelfPermission(
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                    Manifest.permission.WRITE_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                //So if we don't have permission we request for permissions from the user, this will execute the overridden onRequestPermissionsResult
                requestPermissions(
                    arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.WRITE_CONTACTS),
                    PERMISSIONS_REQUEST_READ_CONTACTS
                )
                //callback onRequestPermissionsResult
            } else {
                //If we have permission we run our function directly
                readContactsAndUploadData()
            }
        }

        //Set RecyclerView to use linear layout
        contactRecyclerView.layoutManager = LinearLayoutManager(this)
        val query = db?.whereNotEqualTo("phoneNumber", null)

        println("Query is $query")

        //Get all the contacts in phone
        //pass query results to the recycler adapter
        val options = query?.let {
            FirestoreRecyclerOptions.Builder<Contact>().setQuery(
                it,
                Contact::class.java
            ).build()
        }
        adapter = options?.let { ContactAdapter(it) }
        contactRecyclerView.adapter = adapter

        //onclick listener for the + icon to add contact
        addContactFab.setOnClickListener {
            val i = Intent(applicationContext, AddContact::class.java)
            startActivity(i)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readContactsAndUploadData()
            } else {
                Toast.makeText(
                    this,
                    "Cannot read contact from you're phone! Please enable permissions to read contacts.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    //tell adapter to start watching for data changes
    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()
    }

    //Create inner classes needed to bind the data to the recyclerView
    private inner class ContactViewHolder internal constructor(private val view: View) : RecyclerView.ViewHolder(
        view
    )

    //Adapter class
    private inner class ContactAdapter internal constructor(options: FirestoreRecyclerOptions<Contact>) :
        FirestoreRecyclerAdapter<Contact, MainPageActivity.ContactViewHolder>(options) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
            //inflate the item_resturant.xml
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.contact_item,
                parent,
                false
            )
            return ContactViewHolder(view)
        }

        override fun onBindViewHolder(holder: ContactViewHolder, position: Int, model: Contact) {
            // populate teh restaurant name and rating bar matching textview and rating bar
            holder.itemView.contactName.text = model.contactName

            //On click listener to make a call
            holder.itemView.callFab.setOnClickListener{
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:" + model.phoneNumber)
                startActivity(callIntent)
            }

            holder.itemView.messageFab.setOnClickListener {
                val messageIntent = Intent(Intent.ACTION_VIEW)
                messageIntent.data = Uri.parse("sms:" + model.phoneNumber)
                startActivity(messageIntent)
            }
        }
    }
}