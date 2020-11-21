package com.example.contact_list_application_assignment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.toolbar_contact.*


class SignUpActivity : AppCompatActivity() {

    var TAG = SignUpActivity::class.qualifiedName
    //Code referred from https://firebase.google.com/docs/auth/android/password-auth#create_a_password-based_account
    var auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        //Action bar to get the back button
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sign Up"
        //Sign Up button onclick -  saves new users to database
        signUpButton.setOnClickListener {
            val email = signUpEmail.text.toString().trim()
            val password = signUpPassword.text.toString().trim()
            val confirmPassword = signUpConfirmPassword.text.toString().trim()
            val firstName = signUpFirstName.text.toString().trim()
            val lastName = signUpLastName.text.toString().trim()
            val userName = signUpUsername.text.toString().trim()

            if(checkValues(email, userName, password, confirmPassword, firstName, lastName)) {
                //Code referred from https://firebase.google.com/docs/auth/android/password-auth#create_a_password-based_account
                auth.createUserWithEmailAndPassword(signUpEmail.text.toString().trim(), signUpPassword.text.toString().trim())
                    .addOnCompleteListener() { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                            //Show confirmation and clear inputs
                            Toast.makeText(this, "A confirmation e-mail has been send.", Toast.LENGTH_LONG).show()

                            //A user variable is created and added to the db collection
                            val user = User(auth.currentUser?.uid, signUpEmail.text.toString().trim(), signUpFirstName.text.toString().trim(), signUpLastName.text.toString().trim(), signUpUsername.text.toString().trim())
                            val db = FirebaseFirestore.getInstance().collection("users")
                            db.document(user.id!!).set(user)

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

    //Function to check if all values are filled in TODO- will be implementing features to check if the same username exists
    private fun checkValues(email: String, userName:String, password: String, confirmPassword: String, firstName: String, lastName: String): Boolean {
        return (email.isNotEmpty() && userName.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && (confirmPassword == password) && firstName.isNotEmpty() && lastName.isNotEmpty())
    }
}