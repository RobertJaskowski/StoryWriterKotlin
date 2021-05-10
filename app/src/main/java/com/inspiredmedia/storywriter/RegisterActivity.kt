package com.inspiredmedia.storywriter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import com.google.ads.consent.*;
import com.google.ads.consent.ConsentStatus
import com.google.ads.consent.ConsentFormListener
import com.google.ads.consent.ConsentForm
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import java.net.MalformedURLException
import java.net.URL


class RegisterActivity : AppCompatActivity() {

    var selectedPhotoUri: Uri? = null

//    var form: ConsentForm? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.hide()

        image_button_register.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        register_button_register.setOnClickListener {
            performRegister()
        }

        already_have_account_textView.setOnClickListener {


            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent, 1)
        }

        addTextListeners()


        // if (ConsentInformation.getInstance(this).isRequestLocationInEeaOrUnknown)
//        setUpConsent()

        ConsentInfo.showBox(this)


//        resetConsent_register.setOnClickListener {
//            ConsentInformation.getInstance(this).reset()
//            Toast.makeText(this, "Consent has been revoked", Toast.LENGTH_SHORT).show()
//            setUpConsent()
//        }

    }

    private fun setUpConsent() {

//
//        val consentInformation = ConsentInformation.getInstance(this)
//        val publisherIds = arrayOf("pub-8569563470793396")
//        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
//            override fun onConsentInfoUpdated(consentStatus: ConsentStatus?) {
//
//            }
//
//            override fun onFailedToUpdateConsentInfo(reason: String?) {
//                Toast.makeText(this@RegisterActivity, "Failed to load consent", Toast.LENGTH_SHORT).show()
//            }
//        })
//
//        var privacyUrl: URL? = null
//        try {
//            privacyUrl =
//                URL("https://raw.githubusercontent.com/RobertJaskowski/RobertJaskowski.github.io/master/privacypolicy")
//        } catch (e: MalformedURLException) {
//            e.printStackTrace()
//            // Handle error.
//        }
//
//        form = ConsentForm.Builder(this, privacyUrl)
//            .withListener(object : ConsentFormListener() {
//                override fun onConsentFormLoaded() {
//                    // Consent form loaded successfully.
//                    form?.show()
//                }
//
//                override fun onConsentFormOpened() {
//                    // Consent form was displayed.
//                }
//
//                override fun onConsentFormClosed(
//                    consentStatus: ConsentStatus?, userPrefersAdFree: Boolean?
//                ) {
//                    // Consent form was closed.
//                    val extras = Bundle()
//                    extras.putString("npa", "1")
//
//                    val request = AdRequest.Builder()
//                        .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
//                        .build()
//
//
//                }
//
//                override fun onConsentFormError(errorDescription: String?) {
//                    // Consent form error.
//                }
//            })
//            .withPersonalizedAdsOption()
//            .withNonPersonalizedAdsOption()
//            .withAdFreeOption()
//            .build()
//
//        form!!.load()
//

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            Glide.with(this).load(bitmap).apply(RequestOptions.circleCropTransform()).into(image_imageview_register)
            image_button_register.alpha = 0f
        }

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            finishAndGoToMain()
        }
    }

    private fun performRegister() {


        if (username_tip_register.error != null || email_tip_register.error != null || password_tip_register.error != null || username_edittext_register.text.isNullOrBlank() || email_edittext_register.text.isNullOrBlank() || password_edittext_register.text.isNullOrBlank()) {
            return
        }

        val email = email_edittext_register.text.toString().trim()
        val password = password_edittext_register.text.toString().trim()

        progress_bar_register.visibility = View.VISIBLE


        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.e("RegisterActivity", "Success created with uid: ${it.result?.user?.uid}")

                uploadImageToFirebaseStorage()

            }
            .addOnFailureListener {
                Snackbar.make(scrollview_register, it.message.toString(), Snackbar.LENGTH_LONG).show()
                progress_bar_register.visibility = View.INVISIBLE
            }
    }

    private fun addTextListeners() {
        username_edittext_register.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var username = username_edittext_register.text.toString().trim()
                if (username.isEmpty() || username.length < 5) {
                    Log.e("RegisterActivity", "errrrrrrrrrrr")
                    username_tip_register.error = "Username is too short"
                } else {
                    username_tip_register.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })


        email_edittext_register.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var email = email_edittext_register.text.toString().trim()
                if (email.isEmpty() || email.length < 5) {
                    email_tip_register.error = "Email is too short"

                } else if (!email.contains("@")) {
                    email_tip_register.error = "Not valid email"

                } else {
                    email_tip_register.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        password_edittext_register.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var password = password_edittext_register.text.toString().trim()
                if (password.isEmpty() || password.length < 6) {
                    password_tip_register.error = "Password is too short"
                } else {
                    password_tip_register.error = null

                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

    }


    private fun uploadImageToFirebaseStorage() {

        val filename = FirebaseAuth.getInstance().uid
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")


        if (selectedPhotoUri != null) {
            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.e("RegisterActivity", " success upload image")
                    ref.downloadUrl.addOnSuccessListener {
                        saveUserToFirebaseDatabase(it.toString())
                    }
                }
        } else {
            saveUserToFirebaseDatabase("")
        }
    }


    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""


        val userInfo = HashMap<String, Any>()
        userInfo.put("UID", uid)
        userInfo.put("USERNAME", username_edittext_register.text.toString())

        if (profileImageUrl != "")
            userInfo.put("PROFILE_IMAGE_URI", profileImageUrl)

        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(userInfo)
            .addOnSuccessListener {
                Log.e("RegisterActivity", "Saved to firestore")
//                val intent = Intent(this, LatestStoriesActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
                finishAndGoToMain()

            }
            .addOnFailureListener {
                Log.e("RegisterActivity", "Failed to save to firestore")
                progress_bar_register.visibility = View.INVISIBLE

            }
    }

    private fun finishAndGoToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}

