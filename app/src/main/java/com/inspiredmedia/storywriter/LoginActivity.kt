package com.inspiredmedia.storywriter

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import android.app.Activity



class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()


        addTextListeners()


        login_button_login.setOnClickListener {
            performLogin()
        }


        back_to_register_textview_login.setOnClickListener { finish() }



        //todo remove testing buttons

        loginAsTest1.setOnClickListener {
            loginToFirebase("asdasd@gmail.com","asdasd")
        }
        loginAsTest2.setOnClickListener {
            loginToFirebase("asdasd@gmail.com","asdasdasd")
        }
    }


    private fun loginToFirebase(email: String, password: String){

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.e("login", "success login")
//                    val intent = Intent(this, LatestStoriesActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(intent)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
            .addOnFailureListener {
                Log.e("login", "fail")
                progress_bar_login.visibility = View.INVISIBLE

                Snackbar.make(scrollview_login, it.message.toString(), Snackbar.LENGTH_LONG).show()
            }
    }

    private fun performLogin() {

        if (email_tip_login.error != null || password_tip_login.error != null) {
            Log.e("login", "asd")
            return
        }

        progress_bar_login.visibility = View.VISIBLE

        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        loginToFirebase(email,password)

    }

    private fun addTextListeners() {
        email_edittext_login.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var email = email_edittext_login.text.toString().trim()
                if (email.length < 5)
                    email_tip_login.error = "Email too short"
                else
                    email_tip_login.error = null

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }


        })

        password_edittext_login.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var password = password_edittext_login.text.toString().trim()
                if (password.length < 5)
                    password_tip_login.error = "Password too short"
                else
                    password_tip_login.error = null

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

}
