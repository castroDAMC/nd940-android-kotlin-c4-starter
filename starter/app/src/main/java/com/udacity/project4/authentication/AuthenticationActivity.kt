package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.IdpResponse.*
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import kotlinx.android.synthetic.main.activity_authentication.*

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private val authenticationViewModel by viewModels<AuthenticationViewModel>()

    companion object {
        const val TAG = "RemindersActivity"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
        observeAuthenticationState()

//          TODO: If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        observeAuthenticationState()
        btn_login.setOnClickListener {
            if (authenticationViewModel.authenticationState.value == AuthenticationViewModel.AuthenticationState.AUTHENTICATED) {
                Toast.makeText(
                    this,
                    "Signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                launchSignInFlow()
            }

        }

        txt_new_account.setOnClickListener {
            if (authenticationViewModel.authenticationState.value == AuthenticationViewModel.AuthenticationState.AUTHENTICATED) {
                FirebaseAuth.getInstance().signOut()
            } else {
                Toast.makeText(this, "User not signed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.i(
                    TAG,
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    /**
     * Observes the authentication state and changes the UI accordingly.
     * If there is a logged in user: (1) show a logout button and (2) display their name.
     * If there is no logged in user: show a login button
     */
    private fun observeAuthenticationState() {
        authenticationViewModel.authenticationState.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    val user = FirebaseAuth.getInstance().currentUser?.email.toString()
                    txt_welcome_app.text = "Signed in user " + user
                }
                else -> {
                    txt_welcome_app.text = "User not signed"
                }
            }
        })
    }

}
