package com.example.contact_list_application_assignment

class User {
    //Properties
    var id: String? = null
    var email: String? = null
    var userName: Int? = null
    var password: String? = null

    //Constructors
    constructor()

    constructor(id: String?, email: String?, userName: Int?, password: String?) {
        this.id = id
        this.email = email
        this.userName = userName
        this.password = password
    }

    constructor(email: String?, userName: Int?, password: String?) {
        this.email = email
        this.userName = userName
        this.password = password
    }

}