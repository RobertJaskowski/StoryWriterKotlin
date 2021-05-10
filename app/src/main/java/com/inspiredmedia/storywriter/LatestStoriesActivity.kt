package com.inspiredmedia.storywriter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.inspiredmedia.storywriter.models.Room.Companion.KEY_ID
import com.inspiredmedia.storywriter.models.Room.Companion.KEY_TIMESTAMP
import com.inspiredmedia.storywriter.models.Room.Companion.KEY_TITLE
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_stories.*
import kotlinx.android.synthetic.main.create_room_custom_dialog.*
import kotlinx.android.synthetic.main.create_room_custom_dialog.view.*
import kotlinx.android.synthetic.main.latest_story_row.view.*
import java.util.*

class LatestStoriesActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_stories)



    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)

        return super.onCreateOptionsMenu(menu)


    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_new_story -> {

                var dialogBuilder = AlertDialog.Builder(this).create()
                var layoutInflater = this.layoutInflater
                var dialogView = layoutInflater.inflate(R.layout.create_room_custom_dialog, null)

                dialogBuilder.setView(dialogView)
                dialogBuilder.show()

                //val editText = dialogView.findViewById(R.id.title_edittext_custom_dialog) as EditText

                dialogView.create_button_custom_dialog.setOnClickListener {
                    var text = dialogView.title_edittext_custom_dialog.text.toString().trim()
                    if (text.length > 5 && text.isNotBlank()) {

                        //createRoom(text)
                        dialogBuilder.dismiss()
                    } else {
                        dialogBuilder.name_tip_custom_dialog.error = "Title too short"
                    }
                }

            }


            R.id.menu_sign_out -> {

            }

        }
        return super.onOptionsItemSelected(item)
    }



}

