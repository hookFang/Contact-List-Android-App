package com.example.contact_list_application_assignment

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*


open class ReadContactsHelper : AppCompatActivity() {

    //Connect to firebase
    private val db =
        FirebaseAuth.getInstance().currentUser?.uid?.let { FirebaseFirestore.getInstance().collection("users").document(it).collection("contacts") }

    //Helper to get Email
    private fun getEmail(id: String, applicationContext: Context): String? {
        //Code referred from https://stackoverflow.com/questions/15243205/cant-get-the-email-address-from-contactscontract
        //We gets the emails - we do a selection based on the ID
        var contactEmail: String? = null
        val emails = applicationContext.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id,
            null,
            null
        )
        if (emails != null) {
            while (emails.moveToNext()) {
                contactEmail =
                    emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                break
            }
        }
        return contactEmail
    }

    //Helper to get Phone Number
    private fun getPhoneNumber(
        id: String,
        applicationContext: Context,
        phoneNumber: Int
    ): Pair<String?, String?> {
        var phoneNumValue: String? = null
        var workPhoneNumValue: String? = null
        if (phoneNumber > 0) {
            val cursorPhone = applicationContext.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                arrayOf(id),
                null
            )

            if (cursorPhone != null && cursorPhone.count > 0) {
                while (cursorPhone.moveToNext()) {
                    if (cursorPhone.getInt(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)) == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                        phoneNumValue = cursorPhone.getString(
                            cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        )
                    }
                    if (cursorPhone.getInt(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)) == ContactsContract.CommonDataKinds.Phone.TYPE_WORK) {
                        workPhoneNumValue = cursorPhone.getString(
                            cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        )
                    }
                }
            }
        }
        return Pair(phoneNumValue, workPhoneNumValue)
    }

    //Get address function
    private fun getAddress(id: String, applicationContext: Context): String? {
        var contactAddress: String? = null
        val addressCursor = applicationContext.contentResolver.query(
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = " + id,
            null,
            null
        )
        if (addressCursor != null) {
            while (addressCursor.moveToNext()) {
                contactAddress =
                    addressCursor.getString(addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)) + ","
                contactAddress +=
                    addressCursor.getString(addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)) + ","
                contactAddress +=
                    addressCursor.getString(addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE))
            }
        }
        return contactAddress
    }

    private fun getContactPhotoUri(id: String): Uri {
        val contactUri: Uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id.toLong())
        return Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)
    }

    fun readContactsAndUploadData() {
        // the Content resolver here reads the contacts from your phone.
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        //We now loop through the all the contacts and upload them to our database in firebase.
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                //ID is used to retrieve  the phone number and Email
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val contactName =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phoneNumber =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                        .toInt()

                //We use functions in our helper class which is inherited to get the phoneNumber , email, address
                val email = getEmail(id, applicationContext)
                val (phoneNumValue, workPhoneNumber) = getPhoneNumber(id, applicationContext, phoneNumber)
                val address = getAddress(id, applicationContext)
                val contactPhotoUri = getContactPhotoUri(id).toString()


                if (phoneNumValue != null || workPhoneNumber != null) {
                    //Will make sure same contact is not added twice
                    db?.whereEqualTo("phoneNumber", phoneNumValue)?.get()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (task.result?.documents?.size == 0) {
                                val tempContact = Contact(phoneNumValue, contactName.toLowerCase(Locale.ROOT).capitalize(), address, email, workPhoneNumber, contactPhotoUri, id)
                                tempContact.id = db?.document()?.id
                                db?.document(tempContact.id!!)?.set(tempContact)
                            } else {
                                Log.i("TAG", "Document already exist ")
                            }
                        } else {
                            Log.e("TAG", "get failed with ", task.exception)
                        }
                    }
                }
            }
        }
    }
}
