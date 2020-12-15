package com.hashmaps.contact_list_application_assignment

import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentUris
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.TransactionTooLargeException
import android.provider.ContactsContract
import android.provider.ContactsContract.RawContacts
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_contact.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.OutputStream


open class ContactsHelper : ReadContactsHelper() {
    //Connect to firebase
    private val db =
        FirebaseAuth.getInstance().currentUser?.uid?.let { FirebaseFirestore.getInstance().collection("users").document(it).collection("contacts") }

    fun addContact(
        contactName: String,
        contactPhone: String,
        contactAddress: String,
        contactWorkPhone: String,
        contactEmail: String,
        bitMapImage: Bitmap?
    ): Int {
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
            )
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

            GlobalScope.launch {
                readContactsAndUploadData(addContactAddress.text.toString())
            }

            return 0;
        } catch (e: TransactionTooLargeException) {
            return 1
        } catch (e: Exception) {
            Log.e("TAG", e.message!!)
        }
        //Return false if adding contact failed
        return 2;
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
                .build()
        )
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
        } catch (e: Exception) {
            Log.e("TAG", e.message!!)
            return false
        }
        return true
    }

    fun editContact(
        contactID: String,
        contactFirebaseID: String,
        contactName: String,
        contactPhone: String,
        contactAddress: String,
        contactWorkPhone: String,
        contactEmail: String,
        bitMapImage: Bitmap?
    ): Boolean {

        //We need raw ID to add the new field in a existing contact
        val rawContactCursor: Cursor? = contentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            null,
            ContactsContract.Data.CONTACT_ID + " = ?", arrayOf(contactID),
            null
        )
        rawContactCursor?.moveToFirst()
        val contactRawID: String = rawContactCursor?.getString(rawContactCursor.getColumnIndex(ContactsContract.RawContacts._ID))!!

        //These are used to see if a phone number already exist in the database
        val (phoneNumValue, workPhoneNumber) = getPhoneNumber(contactID, applicationContext, 1)

        val contact = ArrayList<ContentProviderOperation>()

        // Edit Contact Name
        contact.add(
            ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=? AND " +
                            ContactsContract.Data.MIMETYPE + "='" +
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "'",
                    arrayOf(contactID)
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                    contactName
                ).build()
        )

        //Edit Actual phone number
        if (phoneNumValue != null) {
            contact.add(
                ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=? AND " +
                                ContactsContract.Data.MIMETYPE + "='" +
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' AND " + ContactsContract.CommonDataKinds.Phone.TYPE + "='" + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE + "'",
                        arrayOf(contactID)
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactPhone)
                    .build()
            )
        } else {
            contact.add(
                ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        contactID
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactPhone)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    ).build()
            )
        }

        //Edit Work Phone
        if (workPhoneNumber != null) {
            contact.add(
                ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=? AND " +
                                ContactsContract.Data.MIMETYPE + "='" +
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' AND " + ContactsContract.CommonDataKinds.Phone.TYPE + "='" + ContactsContract.CommonDataKinds.Phone.TYPE_WORK + "'",
                        arrayOf(contactID)
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactWorkPhone)
                    .build()
            )
        } else {
            contact.add(
                ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        contactRawID
                    )
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

        //  Edit Email
        if (getEmail(contactID, applicationContext) != null) {
            contact.add(
                ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=? AND " +
                                ContactsContract.Data.MIMETYPE + "='" +
                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "' AND " + ContactsContract.CommonDataKinds.Email.TYPE + "='" + ContactsContract.CommonDataKinds.Email.TYPE_WORK + "'",
                        arrayOf(contactID)
                    )
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, contactEmail)
                    .build()
            )
        } else {
            contact.add(
                ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        contactRawID
                    )
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
        //https://stackoverflow.com/questions/36855859/changing-contacts-image-to-a-large-photo-via-photo-file-id-in-android/36946702#36946702
        val stream = ByteArrayOutputStream()
        var fd: AssetFileDescriptor? = null
        try {
            fd = contentResolver?.openAssetFileDescriptor(Uri.parse(getContactPhotoUri(contactID).toString()), "r")
        } catch (e: FileNotFoundException) {
            fd = null
            print("No Photo Continue")
        }
        //Checks if the contact already had an image, if not we will insert a new image or update the existing image
        if (fd != null) {
            if (bitMapImage != null) {
                bitMapImage.compress(Bitmap.CompressFormat.JPEG, 50, stream)

                // Adding insert operation to operations list
                // to insert Photo in the table ContactsContract.Data
                contact.add(
                    ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?AND " +
                                    ContactsContract.Data.MIMETYPE + "='" +
                                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'",
                            arrayOf(contactID)
                        )
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray())
                        .build()
                )
            }
        } else {
            //
            if (bitMapImage != null) {
                bitMapImage.compress(Bitmap.CompressFormat.JPEG, 50, stream)

                val rawContactPhotoUri = Uri.withAppendedPath(
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, contactRawID.toLong()),
                    RawContacts.DisplayPhoto.CONTENT_DIRECTORY
                )

                try {
                    val fd = contentResolver.openAssetFileDescriptor(rawContactPhotoUri, "rw")
                    val os: OutputStream = fd!!.createOutputStream()
                    os.write(stream.toByteArray())
                    os.close()
                    fd!!.close()
                } catch (e: Exception) {
                    print("Cannot write the new photo")
                }
            }
        }

        // Creates a new contact using contentResolver and also add the contact to firebase
        try {
            val res: Array<out ContentProviderResult> = contentResolver.applyBatch(ContactsContract.AUTHORITY, contact);

            db?.document(contactFirebaseID)?.update(
                "phoneNumber", contactPhone,
                "contactName", contactName,
                "contactAddress", contactAddress,
                "contactEmail", contactEmail,
                "contactWorkPhone", contactWorkPhone
            )

            return true;
        } catch (e: Exception) {
            Log.e("TAG", e.message!!)
        }
        //Return false if adding contact failed
        return false;
    }
}