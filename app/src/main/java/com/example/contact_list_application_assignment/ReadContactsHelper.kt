package com.example.contact_list_application_assignment

import android.content.Context
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

open class ReadContactsHelper : AppCompatActivity() {

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
    private fun getPhoneNumber(id: String, applicationContext: Context, phoneNumber: Int): String? {
        var phoneNumValue: String? = null
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
                    phoneNumValue = cursorPhone.getString(
                        cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    )
                }
            }
        }
        return phoneNumValue
    }

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

    fun readContacts() {
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
                val phoneNumValue = getPhoneNumber(id, applicationContext, phoneNumber)
                val address = getAddress(id, applicationContext)

                println(email)
                println(phoneNumValue)
                println(address)

                val contact = Contact(phoneNumValue, contactName, address, email)

                //Connect to firebase
                val db = FirebaseFirestore.getInstance().collection("contacts")
                contact.id = db.document().id
                db.document(contact.id!!).set(contact)
            }
        }
    }
}