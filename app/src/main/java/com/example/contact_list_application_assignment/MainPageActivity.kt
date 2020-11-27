package com.example.contact_list_application_assignment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main_page.*
import kotlinx.android.synthetic.main.contact_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class MainPageActivity : ReadContactsHelper() {

    private val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    private val db =
        FirebaseAuth.getInstance().currentUser?.uid?.let { FirebaseFirestore.getInstance().collection("users").document(it).collection("contacts") }
    private var adapter: MainPageActivity.ContactAdapter? = null
    private var searchContactVisibility = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        searchContact.visibility = View.GONE

        GlobalScope.launch(context = Dispatchers.Main) {
            delay(2000)
            progressBarMainPage.visibility = View.GONE
        }

        GlobalScope.launch {
            // Code referred from - https://medium.com/@manuaravindpta/fetching-contacts-from-device-using-kotlin-6c6d3e76574f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission(
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
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
        var query = db?.whereNotEqualTo("contactName", null)?.orderBy("contactName")

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

        searchContact.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //this is when you press submit
            override fun onQueryTextSubmit(searchString: String): Boolean {
                val newQuery = db?.whereNotEqualTo("contactName", null)?.orderBy("contactName")?.whereGreaterThanOrEqualTo(
                    "contactName",
                    searchString.toLowerCase(Locale.ROOT).capitalize()
                )
                val newOptions = newQuery?.let {
                    FirestoreRecyclerOptions.Builder<Contact>().setQuery(
                        it,
                        Contact::class.java
                    ).build()
                }
                if (newOptions != null) {
                    adapter?.updateOptions(newOptions)
                }
                return true
            }

            //this actually runs each time a text change is found
            override fun onQueryTextChange(searchString: String): Boolean {
                val newQuery = db?.whereNotEqualTo("contactName", null)?.orderBy("contactName")?.whereGreaterThanOrEqualTo(
                    "contactName",
                    searchString.toLowerCase(Locale.ROOT).capitalize()
                )
                val newOptions = newQuery?.let {
                    FirestoreRecyclerOptions.Builder<Contact>().setQuery(
                        it,
                        Contact::class.java
                    ).build()
                }
                if (newOptions != null) {
                    adapter?.updateOptions(newOptions)
                }
                return true
            }
        })

        //Code refereed from https://code.tutsplus.com/tutorials/how-to-code-a-bottom-navigation-bar-for-an-android-app--cms-30305#:~:text=Bottom%20navigation%20bars%20make%20it,refreshes%20the%20currently%20active%20view.
        //Bottom navigation view code to navigate to different pages
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                //Option to add a new contact
                R.id.addContact -> {
                    val i = Intent(applicationContext, AddContact::class.java)
                    startActivity(i)
                    return@setOnNavigationItemSelectedListener true
                }
                //option to search for a contact
                R.id.searchContact -> {
                    if (!searchContactVisibility) {
                        searchContact.visibility = View.VISIBLE
                        searchContactVisibility = true
                    } else {
                        searchContact.visibility = View.GONE
                        searchContactVisibility = false
                    }
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
            //inflate the contact_item.xml
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.contact_item,
                parent,
                false
            )
            return ContactViewHolder(view)
        }

        override fun onBindViewHolder(holder: ContactViewHolder, position: Int, model: Contact) {
            // populate the data in the page
            holder.itemView.contactName.text = model.contactName

            //On click listener to make a call
            holder.itemView.callFab.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:" + model.phoneNumber)
                startActivity(callIntent)
            }

            //On click listener to send a message
            holder.itemView.messageFab.setOnClickListener {
                val messageIntent = Intent(Intent.ACTION_VIEW)
                messageIntent.data = Uri.parse("sms:" + model.phoneNumber)
                startActivity(messageIntent)
            }

            //On click listener on the contact item
            holder.itemView.setOnClickListener {
                //We pass in all the contact values to the contact Details activity
                val intent = Intent(applicationContext, ContactDetails::class.java)
                intent.putExtra("contactName", model.contactName)
                intent.putExtra("contactPhotoURI", model.contactPhotoURI)
                intent.putExtra("contactWorkPhone", model.contactWorkPhone)
                intent.putExtra("contactAddress", model.contactAddress)
                intent.putExtra("contactPhone", model.phoneNumber)
                intent.putExtra("contactEmail", model.contactEmail)
                intent.putExtra("rawContactID", model.rawContactID)
                intent.putExtra("contactFirebaseID", model.id)
                startActivity(intent)
            }
        }
    }

    //Setup the toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //inflate the main menu to add the call, message, edit and delete items to the toolbar
        menuInflater.inflate(R.menu.main_page_menu, menu)
        return true;
    }
}