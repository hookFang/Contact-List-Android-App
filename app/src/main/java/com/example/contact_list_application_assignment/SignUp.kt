package com.example.contact_list_application_assignment

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUp : AppCompatActivity() {

    var TAG = SignUp::class.qualifiedName
    //Code referred from https://firebase.google.com/docs/auth/android/password-auth#create_a_password-based_account
    var auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        //Action bar to get the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Sign Up button onclick -  saves new users to database
        signUpButton.setOnClickListener {
            if(signUpEmail.text.isNotEmpty() && signUpUsername.text.isNotEmpty() && signUpPassword.text.isNotEmpty() && signUpConfirmPassword.text.isNotEmpty() && (signUpPassword.text.toString().trim() == signUpConfirmPassword.text.toString().trim())) {
                //A user variable is created and password is hashed using BCrypt.
                //val user = User(signUpEmail.text.toString().trim(), signUpUsername.text.toString().trim(),  BCrypt.withDefaults().hashToString(12, signUpPassword.text.toString().trim().toCharArray()))

                //Code referred from https://firebase.google.com/docs/auth/android/password-auth#create_a_password-based_account
                auth.createUserWithEmailAndPassword(signUpEmail.text.toString().trim(), signUpPassword.text.toString().trim())
                    .addOnCompleteListener() { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                            //Show confirmation and clear inputs
                            Toast.makeText(this, "A confirmation e-mail has been send.", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            try {
                                throw task.exception!!
                            } catch (e: FirebaseAuthWeakPasswordException) {
                                Toast.makeText(this, "The password you entered is weak, please try again with a new password", Toast.LENGTH_LONG).show()
                            } catch (e: FirebaseAuthUserCollisionException) {
                                Toast.makeText(this, "The email address is already in use by another account.", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Log.e(TAG, e.message!!)
                            }
                        }
                    }
            } else {
                Toast.makeText(this, "Please make sure the fields are all filled in", Toast.LENGTH_LONG).show()
            }
        }
    }

    //This is the action for back button
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}