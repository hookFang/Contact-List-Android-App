package com.example.contact_list_application_assignment

import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentUris
import android.graphics.Bitmap
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream


open class ContactsHelper : AppCompatActivity() {
    //Connect to firebase
    private val db = FirebaseAuth.getInstance().currentUser?.uid?.let { FirebaseFirestore.getInstance().collection("users").document(it).collection("contacts") }
    private val TAG = ContactsHelper::class.qualifiedName

    fun addContact(
        contactName: String,
        contactPhone: String,
        contactAddress: String,
        contactWorkPhone: String,
        contactEmail: String,
        bitMapImage: Bitmap?
    ): Boolean {
        /*Code referred from https://stackoverflow.com/questions/48686429/save-photo-contact-programmatically-android
        https://stackoverflow.com/questions/9096186/how-to-add-contacts-programmatically-in-android

        //This is the list that  gets filled with contact details
        */

        val contact = ArrayList<ContentProviderOperation>()
        contact.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build()
        );

        // Add the contact name to the list
        if (contactName != null) {
            contact.add(
                ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        contactName
                    ).build()
            );
        }

        // Add phone number to the list
        if (contactPhone != null) {
            contact.add(
                ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactPhone)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    ).build()
            );
        }

        // Add work phone number to the list
        if (contactWorkPhone != null) {
            contact.add(
                ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactWorkPhone)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                    ).build()
            );
        }


        //  Add Email to the list
        if (contactEmail != null) {
            contact.add(
                ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, contactEmail)
                    .withValue(
                        ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.TYPE_WORK
                    ).build()
            );
        }

        //Adding Contact Image
        val stream = ByteArrayOutputStream()
        if (bitMapImage != null) {
            bitMapImage.compress(Bitmap.CompressFormat.JPEG, 50, stream)

            // Adding insert operation to operations list
            // to insert Photo in the table ContactsContract.Data
            contact.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray())
                    .build()
            )
        }

        // Creates a new contact using contentResolver and also add the contact to firebase
        try {
             val res: Array<out ContentProviderResult> = contentResolver.applyBatch(ContactsContract.AUTHORITY, contact);

            val myContactUri: Uri = res[0].uri
            val lastSlash: Int = myContactUri.toString().lastIndexOf("/")
            val length: Int = myContactUri.toString().length
            val contactID: String = myContactUri.toString().subSequence(lastSlash + 1, length).toString()

            val contactUri: Uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID.toLong())
            val photoLocation: Uri =  Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)

            //Adding the contact info to firebase
            val newContact = Contact(contactPhone, contactName, contactAddress, contactEmail, contactWorkPhone, photoLocation.toString(), contactID)
            newContact.id = db?.document()?.id
            db?.document(newContact.id!!)?.set(newContact)
            return true;
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
        }
        //Return false if adding contact failed
        return false;
    }

    //Code refereed from
    fun deleteContact(contactID: String): Boolean {
        val operations = ArrayList<ContentProviderOperation>()
        operations.add(
            ContentProviderOperation
                .newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                            + " = ?", arrayOf(contactID)
                )
                .build())
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
            return false
        }
        return true
    }
}