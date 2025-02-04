package com.hashmaps.contact_list_application_assignment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        if (FirebaseAuth.getInstance().currentUser != null) {
            // Sign in success, update UI with the signed-in user's information
            Log.i("TAG", "Sign In was Successful")
            val user = auth.currentUser
            if (user != null) {
                if (!user.isEmailVerified) {
                    Toast.makeText(
                        this,
                        "Please Verify you're email before you can Login",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val i = Intent(applicationContext, MainPageActivity::class.java)
                    startActivity(i)
                }
            }
        }


        //Sign Up button onclick listener from login page
        signUpButtonFromLogin.setOnClickListener {
            //When the button is pressed the Sign Up activity is opened
            val i = Intent(applicationContext, SignUpActivity::class.java)
            startActivity(i)
        }

        //Reset Button Onclick listener
        forgotPasswordButton.setOnClickListener {
            //This opens the reset password activity
            val i = Intent(applicationContext, ResetPassword::class.java)
            startActivity(i)
        }

        //Login Button onClick listener verifies the login details
        loginButton.setOnClickListener {
            val username = userName.text.toString().trim()
            val passwordSecurity = password.text.toString().trim()

            //If the values or not empty proceed with authentication process.
            //Firebase login code referred from https://firebase.google.com/docs/auth/android/password-auth

            if (username.isNotEmpty() && passwordSecurity.isNotEmpty()) {
                auth.signInWithEmailAndPassword(username, passwordSecurity)
                    .addOnCompleteListener(this) { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i("TAG", "Sign In was Successful")
                            val user = auth.currentUser
                            if (user != null) {
                                if (!user.isEmailVerified) {
                                    Toast.makeText(
                                        this,
                                        "Please Verify you're email before you can Login",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    val i = Intent(applicationContext, MainPageActivity::class.java)
                                    startActivity(i)
                                }
                            }
                        } else {
                            try {
                                throw task.exception!!
                            } catch (e: FirebaseAuthInvalidUserException) {
                                Toast.makeText(this, "The user with this email does not exist, Please sign Up for a Account", Toast.LENGTH_LONG)
                                    .show()
                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(this, "Wrong Password Please Try Again", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Log.e("TAG", e.message!!)
                            }
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter you're Username and Password.", Toast.LENGTH_LONG).show()
            }
        }
    }
}