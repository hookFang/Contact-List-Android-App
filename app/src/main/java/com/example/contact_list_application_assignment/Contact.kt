package com.example.contact_list_application_assignment

class Contact {
    //Properties
    var id: String? = null
    var phoneNumber: String? = null
    var contactName: String? = null
    var contactAddress: String? = null
    var contactEmail: String? = null

    //Constructors
    constructor()

    constructor(
        phoneNumber: String?,
        contactName: String?,
        contactAddress: String?,
        contactEmail: String?
    ) {
        this.phoneNumber = phoneNumber
        this.contactName = contactName
        this.contactAddress = contactAddress
        this.contactEmail = contactEmail
    }

}