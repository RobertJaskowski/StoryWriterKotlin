package com.inspiredmedia.storywriter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_profile.*
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.ads.consent.ConsentInformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.create_room_custom_dialog.*
import kotlinx.android.synthetic.main.create_room_custom_dialog.view.*
import kotlinx.android.synthetic.main.create_room_custom_dialog.view.name_tip_custom_dialog
import java.util.*
import kotlin.collections.HashMap


class Profile : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        loadProfileImage()
        setUpButtons()
    }

    private fun loadProfileImage() {
        val userUID = FirebaseAuth.getInstance().uid
        FirebaseFirestore.getInstance().collection("users").document(userUID!!).get().addOnSuccessListener {

            var linkToImage = it.get("PROFILE_IMAGE_URI")
            if (linkToImage != null && linkToImage != "")
                Glide.with(activity!!).load(linkToImage).apply(RequestOptions.circleCropTransform()).into(image_button_profile)


        }
    }

    private fun setUpButtons() {

        revert_consent.setOnClickListener {
            ConsentInformation.getInstance(activity).reset()
            Toast.makeText(activity, "Consent has been revoked", Toast.LENGTH_SHORT).show()
            ConsentInfo.showBox(context!!)
        }


        Glide.with(this).load(R.drawable.discord).into(profile_discord)
        profile_discord.setOnClickListener {
            openUrl("https://discord.gg/uCx3eWR")
        }

        Glide.with(this).load(R.drawable.twitter).into(profile_twitter)
        profile_twitter.setOnClickListener {
            openUrl("https://twitter.com/rjjaskowski")
        }

        profile_feedback.setOnClickListener {
            var dialogBuilder = AlertDialog.Builder(activity!!).create()
            var layoutInflater = this.layoutInflater
            var dialogView = layoutInflater.inflate(R.layout.create_room_custom_dialog, null)

            dialogBuilder.setView(dialogView)
            dialogBuilder.show()

            //val editText = dialogView.findViewById(R.id.title_edittext_custom_dialog) as EditText

            dialogView.create_button_custom_dialog.setOnClickListener {
                var text = dialogView.title_edittext_custom_dialog.text.toString().trim()
                if (text.length > 5 && text.isNotBlank()) {
                    Log.e("profile",text)
                    dialogBuilder.dismiss()
                    sendFeedbackToFirestore(text)
                } else {
                    dialogBuilder.name_tip_custom_dialog.error = "Title too short"
                }
            }
        }

        profile_sign_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }


    private fun sendFeedbackToFirestore(text: String) {
        var feedbackData = HashMap<String, Any>()
        var UID = FirebaseAuth.getInstance().uid.toString()
        feedbackData.put("UID", UID)
        feedbackData.put("TIME",Calendar.getInstance().time.toString())//
        feedbackData.put("FEEDBACK", text)

        FirebaseFirestore.getInstance().collection("feedback").document(UID).set(feedbackData).addOnSuccessListener {
            Log.e("profile","feedback sent")
            Toast.makeText(activity,"Thank you for your feedback",Toast.LENGTH_LONG).show()
        }
    }

    private fun openUrl(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }


    companion object {
        fun newInstance(): Profile = Profile()
    }
}
