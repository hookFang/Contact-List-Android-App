package com.hashmaps.contact_list_application_assignment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_reset_password.*
import kotlinx.android.synthetic.main.toolbar_contact.*
import java.lang.Exception

class ResetPassword : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Password Reset"


        resetPasswordButton.setOnClickListener {
            val emailIDProvided = resetPasswordEmail.text.toString().trim()
            if (emailIDProvided.isNotEmpty()) {
                Firebase.auth.sendPasswordResetEmail(emailIDProvided).addOnCompleteListener(this) { task: Task<Void> ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password Rest E-mail has been sent to your E-mail", Toast.LENGTH_LONG).show()
                    } else {
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthInvalidUserException) {
                            Toast.makeText(this, "An account with that E-mail doesn't exist ! Please sign up for a new account.", Toast.LENGTH_LONG)
                                .show()
                        } catch (e: Exception) {
                            Log.e("TAG", e.message!!)
                        }
                    }
                }

            } else {
                Toast.makeText(this, "Please enter you're E-mail", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //This is the action for back button
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}