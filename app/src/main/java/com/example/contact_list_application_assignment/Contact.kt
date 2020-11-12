package com.example.contact_list_application_assignment

import android.net.Uri

class Contact {
    //Properties
    var id: String? = null
    var phoneNumber: String? = null
    var contactName: String? = null
    var contactAddress: String? = null
    var contactEmail: String? = null
    var contactWorkPhone: String? = null
    var contactPhotoURI: String? = null

    //Constructors
    constructor()
    constructor(
        phoneNumber: String?,
        contactName: String?,
        contactAddress: String?,
        contactEmail: String?,
        contactWorkPhone: String?,
        contactPhotoURI: String?
    ) {
        this.phoneNumber = phoneNumber
        this.contactName = contactName
        this.contactAddress = contactAddress
        this.contactEmail = contactEmail
        this.contactWorkPhone = contactWorkPhone
        this.contactPhotoURI = contactPhotoURI
    }


}