package com.hashmaps.contact_list_application_assignment

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.opencsv.CSVWriter
import kotlinx.android.synthetic.main.activity_account_details.*
import java.io.FileWriter
import java.io.IOException

class AccountDetails : AppCompatActivity() {

    val db = FirebaseAuth.getInstance().currentUser?.uid?.let { FirebaseFirestore.getInstance().collection("users").document(it) }
    val csvWritePath =
        Environment.getExternalStorageDirectory().absolutePath.toString() + "/Contacts.csv" // Here csv file name is MyCsvFile.csv


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)
        progressBarForExcelExport.visibility = View.GONE

        db?.get()?.addOnCompleteListener { document ->
            if (document != null) {
                accountEmail.text = FirebaseAuth.getInstance().currentUser?.email.toString()
                accountName.text = document.result?.get("firstName").toString() + "  " + document.result?.get("lastName").toString()
            } else {
                Log.e("TAG", "No such document")
            }
        }

        accountEmail.text = FirebaseAuth.getInstance().currentUser?.email.toString()

        //Logout the user
        logoutCardView.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }

        //Displays terms and conditions
        termsCardView.setOnClickListener {
            startActivity(Intent(applicationContext, TermsAndConditionsPage::class.java))
        }

        //Code Reffered From https://stackoverflow.com/questions/11341931/how-to-create-a-csv-on-android
        //Export the contacts to an excel file
        exportContactExcelCardView.setOnClickListener {
            progressBarForExcelExport.visibility = View.VISIBLE
            var writer: CSVWriter? = null
            val data: MutableList<Array<String>> = ArrayList()
            data.add(arrayOf("Name", "Phone"))
            try {
                writer = CSVWriter(FileWriter(csvWritePath))
                db?.collection("contacts")?.get()?.addOnCompleteListener { task ->
                    for (document in task.result?.documents!!) {
                        data.add(arrayOf(document.get("contactName")?.toString() ?: "-", document.get("phoneNumber")?.toString() ?: "-"))
                    }
                    writer.writeAll(data) // data is adding to csv
                    writer.close()
                    progressBarForExcelExport.visibility = View.GONE
                    Toast.makeText(this, "File has been created and saved", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Toast.makeText(this, "File cannot be saved", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
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
            return@setOnNavigationItemSelectedListener true
        }

    }
}
