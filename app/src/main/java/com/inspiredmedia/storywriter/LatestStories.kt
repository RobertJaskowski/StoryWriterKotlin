package com.inspiredmedia.storywriter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.inspiredmedia.storywriter.LatestStories.Companion.EXTRA_TITLE
import com.inspiredmedia.storywriter.LatestStories.Companion.EXTRA_UUID
import com.inspiredmedia.storywriter.Tools.InternetManager
import com.inspiredmedia.storywriter.models.Room
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.create_room_custom_dialog.*
import kotlinx.android.synthetic.main.create_room_custom_dialog.view.*
import kotlinx.android.synthetic.main.fragment_latest_stories.*
import kotlinx.android.synthetic.main.latest_story_row.view.*
import java.util.*

class LatestStories : Fragment() {

    companion object {
        fun newInstance(): LatestStories = LatestStories()
        val EXTRA_UUID = "UUID"
        val EXTRA_TITLE = "TITLE"
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_latest_stories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        fetchListOfRooms()

        setRefreshListener()
        setFab()

    }

    private fun setFab() {
        fab_latest_stories.setOnClickListener {


            var dialogBuilder = AlertDialog.Builder(activity!!).create()
            var layoutInflater = this.layoutInflater
            var dialogView = layoutInflater.inflate(R.layout.create_room_custom_dialog, null)

            dialogBuilder.setView(dialogView)
            dialogBuilder.show()

            //val editText = dialogView.findViewById(R.id.title_edittext_custom_dialog) as EditText

            dialogView.create_button_custom_dialog.setOnClickListener {
                var text = dialogView.title_edittext_custom_dialog.text.toString().trim()
                if (text.length > 5 && text.isNotBlank()) {

                    createRoom(text)
                    dialogBuilder.dismiss()
                    fetchListOfRooms()
                } else {
                    dialogBuilder.name_tip_custom_dialog.error = "Title too short"
                }
            }
        }
    }

    private fun setRefreshListener() {


        Log.e("Latest", "set up list")
        swipeContainer_latest_stories!!.setOnRefreshListener {
            Log.e("Latest", "asdasdasd")
            fetchListOfRooms()

        }
    }

    private fun fetchListOfRooms() {

        val adapter = GroupAdapter<ViewHolder>()

        adapter.clear()
        Log.e("latest", "fetching list of rooms")


        FirebaseFirestore.getInstance().collection("rooms").get().addOnSuccessListener { querySnapshot ->
            querySnapshot.forEach {
                adapter.add(LatestStoryItem(it.getString("TITLE")!!, it.getString("ID")!!))
                Log.e(it.getString("TITLE"), "fetching list of rooms")

            }
        }.addOnFailureListener {
            Log.e("latest", "fail to catch")
        }


        //adapter item click listener

        recycler_latest_stories!!.adapter = adapter
        adapter.setOnItemClickListener { item, _ ->
            openStory(item.extras.get(EXTRA_UUID).toString(),item.extras.get(EXTRA_TITLE).toString())
        }


        swipeContainer_latest_stories?.isRefreshing = false


    }

    private fun openStory(ROOM_UUID: String, ROOM_TITLE: String) {
        if (context != null)
            if (InternetManager.isConnectedToNetwork(context!!)) {
                val intent = Intent(activity, StoryActivity::class.java)
                intent.putExtra(EXTRA_UUID, ROOM_UUID)
                intent.putExtra(EXTRA_TITLE, ROOM_TITLE)
                startActivity(intent)
            } else {
                if (view != null) {


                    var snackbar = Snackbar.make(view!!, "Check internet connection and refresh", Snackbar.LENGTH_LONG)
                    snackbar.view.setBackgroundColor(
                        ContextCompat.getColor(
                            context!!,
                            R.color.colorPrimaryDark
                        )
                    )
                    snackbar.show()

                }
            }


    }

    private fun createRoom(title: String) {
        var uuid = UUID.randomUUID().toString()


        val room = HashMap<String, Any>()
        room.put(Room.KEY_ID, uuid)
        room.put(Room.KEY_TITLE, title)
        room.put(Room.KEY_TIMESTAMP, System.currentTimeMillis() / 1000)


        //todo room participating members

        FirebaseFirestore.getInstance().collection("rooms").document(uuid).set(room)
            .addOnSuccessListener {
                Log.e("Latest", "Sended to firestore")

                openStory(uuid,title)
            }

        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().uid!!)
            .collection("rooms").document(uuid)
            .set(room)
            .addOnSuccessListener {
                Log.e("Latest", "Sended to user/rooms node")

            }
        //add to user room


    }
}

class LatestStoryItem(val title: String, val roomUUID: String) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.title_textview_latest_story.text = title
        viewHolder.extras.put(EXTRA_UUID, roomUUID)
        viewHolder.extras.put(EXTRA_TITLE, title)

    }

    override fun getLayout(): Int {
        return R.layout.latest_story_row
    }
}