package com.inspiredmedia.storywriter

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.Query
import com.inspiredmedia.storywriter.Tools.InternetManager
import com.inspiredmedia.storywriter.Tools.InternetManager.Companion.isConnectedToNetworkWithTimer
import com.inspiredmedia.storywriter.models.Message.Companion.KEY_TEXT
import com.inspiredmedia.storywriter.models.Message.Companion.KEY_TIMESTAMP
import com.inspiredmedia.storywriter.models.Room
import com.like.LikeButton
import com.like.OnLikeListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_story.*
import kotlinx.android.synthetic.main.character_description_story.view.*
import kotlinx.android.synthetic.main.create_character_custom_dialog.view.*
import kotlinx.android.synthetic.main.story_row.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class StoryActivity : AppCompatActivity() {



    val adapter = GroupAdapter<ViewHolder>()
    var roomUUID: String? = ""
    var roomTitle: String? = ""

    lateinit var messagesRef: CollectionReference
    lateinit var charactersRef: CollectionReference

    var currentlySelectedCharacter: String = ""


    var snackbarConnectivity: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)
        supportActionBar?.hide()

        roomUUID = intent.getStringExtra(LatestStories.EXTRA_UUID)
        roomTitle = intent.getStringExtra(LatestStories.EXTRA_TITLE)
        messagesRef = FirebaseFirestore.getInstance().collection("rooms").document(roomUUID!!).collection("messages")
        charactersRef =
            FirebaseFirestore.getInstance().collection("rooms").document(roomUUID!!).collection("characters")


        //fetchStoryMessages()

        recycler_story.adapter = adapter

        listenForMessages()


        send_button_story.setOnClickListener {

            performSendMessage()
        }


        setAddCharacterButton()

        fetchCharacters()
        fetchFavoriteStatus()

        setFavoriteButton()


    }


    private fun setAddCharacterButton() {

        addPerson.setOnClickListener {
            var dialogBuilder = AlertDialog.Builder(this).create()
            var layoutInflater = this.layoutInflater
            var dialogView = layoutInflater.inflate(R.layout.create_character_custom_dialog, null)

            dialogBuilder.setView(dialogView)
            dialogBuilder.show()

            //val editText = dialogView.findViewById(R.id.title_edittext_custom_dialog) as EditText

            dialogView.create_button_custom_dialog.setOnClickListener {
                var text = dialogView.title_edittext_custom_dialog.text.toString().trim()
                var desc = dialogView.desc_edittext_custom_dialog.text.toString().trim()
                if (text.length > 2 && text.isNotBlank() && desc.length > 0 && desc.isNotBlank()) {

                    createCharater(text, desc)
                    dialogBuilder.dismiss()
                } else {
                    Toast.makeText(this, "Can't be empty or too short", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    companion object {
        val KEY_ID = "ID"
        val KEY_NAME = "NAME"
        val KEY_DESC = "DESC"
    }

    private fun createCharater(text: String, desc: String) {


        val character = HashMap<String, Any>()
        character.put(KEY_ID, text)
        character.put(KEY_NAME, text)
        character.put(KEY_DESC, desc)
        character.put(KEY_TIMESTAMP, System.currentTimeMillis() / 1000)


        charactersRef.document(text).set(character)
            .addOnSuccessListener {
                Log.e("story", "Sended character to firestore")
                fetchCharacters()
            }

    }

    private fun fetchCharacters() {


        if (charactersLayout.childCount > 0)
            charactersLayout.removeAllViews()

        charactersRef
            .get()
            .addOnSuccessListener {

                for (hero in it) {
                    Log.e("story", "creating button")
                    val button = Button(this, null, R.attr.selectableItemBackgroundBorderless)
                    var scale = resources.displayMetrics.density
                    var dpAsPxels = 18 * scale + 0.5f
                    button.setPadding(0, dpAsPxels.toInt(), dpAsPxels.toInt(), dpAsPxels.toInt())

                    button.layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    )

                    button.text = hero.getString(KEY_NAME).toString()
                    setCharacterButtonListeners(button, hero)


                    charactersLayout.addView(button)
                }

            }
    }


    private fun fetchFavoriteStatus() {
        var userUid = FirebaseAuth.getInstance().uid

        Log.e("story", "fetching geart")

        FirebaseFirestore.getInstance().collection("users").document(userUid!!).collection("rooms").document(roomUUID!!)
            .get().addOnFailureListener {
                Snackbar.make(
                    window.decorView.findViewById(R.id.activity_story_parent_constraint),
                    "Check your internet connection",
                    Snackbar.LENGTH_LONG
                )
            }
            .addOnCompleteListener {
                if (it.result != null) {
                    if (it.getResult()!!.exists()) {
                        favorite_button.isLiked = true
                    } else {
                        favorite_button.isLiked = false

                    }
                } else {
                    Snackbar.make(
                        window.decorView.findViewById(R.id.activity_story_parent_constraint),
                        "Check your internet connection",
                        Snackbar.LENGTH_LONG
                    )
                }


            }
    }

    private fun setFavoriteButton() {
        val userUid = FirebaseAuth.getInstance().uid


        favorite_button.setOnLikeListener(object : OnLikeListener {
            override fun liked(likeButton: LikeButton) {
                Log.e("story", "clicked like")

                val room = HashMap<String, Any>()
                room.put(Room.KEY_ID, roomUUID.toString())
                room.put(Room.KEY_TITLE, roomTitle.toString())
                room.put(Room.KEY_TIMESTAMP, System.currentTimeMillis() / 1000)

                FirebaseFirestore.getInstance().collection("users").document(userUid!!).collection("rooms")
                    .document(roomUUID!!).set(room).addOnSuccessListener {
                        Log.e("story", "clicked like success")
                    }
            }

            override fun unLiked(likeButton: LikeButton) {
                FirebaseFirestore.getInstance().collection("users").document(userUid!!).collection("rooms")
                    .document(roomUUID!!).delete()
            }
        })


    }

    private fun setCharacterButtonListeners(
        button: Button,
        hero: QueryDocumentSnapshot
    ) {

        button.setOnClickListener {}
        button.setOnClickListener {
            if (currentlySelectedCharacter != hero.getString(KEY_NAME).toString()) {
                currentlySelectedCharacter = hero.getString(KEY_NAME).toString()
                text_tip_input_story.hint = currentlySelectedCharacter
            } else {
                currentlySelectedCharacter = ""
                text_tip_input_story.hint = ""
            }
        }

        button.setOnLongClickListener {
            createDialogWithEditCharDescription(hero.getString(KEY_NAME).toString())

            true
        }
    }

    private fun createDialogWithEditCharDescription(name: String) {
        var dialogBuilder = AlertDialog.Builder(this).create()
        var layoutInflater = this.layoutInflater
        var dialogView = layoutInflater.inflate(R.layout.character_description_story, null)

        dialogBuilder.setView(dialogView)
        dialogBuilder.show()

        charactersRef.document(name).get().addOnSuccessListener {
            dialogView.desc_edittext_custom_dialog_editChar.setText(it.getString(KEY_DESC).toString())
        }




        dialogView.update_button_custom_dialog_editChar.setOnClickListener {
            var desc = dialogView.desc_edittext_custom_dialog_editChar.text.toString().trim()
            if (desc.isNotEmpty() && desc.isNotBlank()) {

                createCharater(name, desc)
                dialogBuilder.dismiss()
            } else {
                Toast.makeText(this, "Can't be empty or too short", Toast.LENGTH_LONG).show()
            }
        }


        dialogView.delete_button_custom_dialog_editChar.setOnClickListener {
            charactersRef.document(name).delete()
            dialogBuilder.dismiss()
            fetchCharacters()
        }

    }


    private fun fetchStoryMessages() {
        messagesRef
            .orderBy(KEY_TIMESTAMP)
            .get()
            .addOnSuccessListener { document ->
                document.forEach {

                    var name = it.get(KEY_NAME) ?: ""
                    addToList(

                        it.getString(KEY_TEXT)!!, name.toString()
                    )

                }

                recycler_story.scrollToPosition(adapter.itemCount - 1)

            }

    }

    private fun listenForMessages() {
        messagesRef
            .orderBy(KEY_TIMESTAMP, Query.Direction.ASCENDING)
            .addSnapshotListener { dataSnapshot, exception ->
                if (exception != null) {
                    Log.e("story", "listen failed")
                }

                if (dataSnapshot != null && dataSnapshot.documentChanges.isNotEmpty()) {
                    Log.e("story", "snapshot story")

                    for (dc in dataSnapshot.documentChanges) {
                        when (dc.type) {


                            DocumentChange.Type.ADDED -> {

                                Log.e("story", "added")
                                var name = dc.document.getString(KEY_NAME).toString()

                                addToList(
                                    dc.document.getString(KEY_TEXT)!!, name ?: ""
                                )
                            }
                        }
                    }

                }
            }
    }

    private fun getTextFromLastItemInAdapter(): String? {


        Log.e("story", "items " + adapter.itemCount.toString())

        return recycler_story.findViewHolderForAdapterPosition(recycler_story.adapter!!.itemCount - 1)
            ?.itemView?.message_textview_story?.text.toString()
    }


    private fun performSendMessage() {

        if(!isConnectedToNetworkWithTimer(applicationContext,window)){
            return
        }

        val text = edittext_story.text.toString()

        var message = HashMap<String, Any>()

        message.put(KEY_NAME, currentlySelectedCharacter)
        message.put(KEY_TEXT, text)
        message.put(KEY_TIMESTAMP, System.currentTimeMillis() / 1000)


        sendMessageToFirebase(message)


    }

    private fun sendMessageToFirebase(message: HashMap<String, Any>) {
        messagesRef
            .add(message)
            .addOnSuccessListener {
                Log.e("story", "pushed message")
                edittext_story.text?.clear()
                recycler_story.scrollToPosition(adapter.itemCount - 1)
            }
            .addOnFailureListener {
                Log.e("story", "failed to push message")
            }


    }


    private fun addToList(message: String, hero: String) {
        adapter.add(StoryItem(message, hero))
    }
}


class StoryItem(val message: String, val hero: String) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        if (hero != "" && hero != null) {
            viewHolder.itemView.hero_textview_story.text = hero
            viewHolder.itemView.hero_textview_story.visibility = View.VISIBLE
        } else {
            viewHolder.itemView.hero_textview_story.visibility = View.GONE
        }
        viewHolder.itemView.message_textview_story.text = message

    }


    override fun getLayout(): Int {
        return R.layout.story_row
    }
}
