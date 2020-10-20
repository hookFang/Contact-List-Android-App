package com.example.contact_list_application_assignment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Sign Up button onclick listener from login page
        signUpButtonFromLogin.setOnClickListener {
            //When the button is pressed the Sign Up activity is opened
            val i = Intent(applicationContext, SignUp::class.java)
            startActivity(i)
        }

        //Login Button onClick listener verifies the login details
        loginButton.setOnClickListener {

        }

    }
}