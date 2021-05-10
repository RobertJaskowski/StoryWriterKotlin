package com.inspiredmedia.storywriter


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.inspiredmedia.storywriter.LatestStories.Companion.EXTRA_TITLE
import com.inspiredmedia.storywriter.LatestStories.Companion.EXTRA_UUID
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.create_room_custom_dialog.*
import kotlinx.android.synthetic.main.create_room_custom_dialog.view.*
import kotlinx.android.synthetic.main.create_room_custom_dialog.view.name_tip_custom_dialog
import kotlinx.android.synthetic.main.fragment_favorite_rooms.*
import kotlinx.android.synthetic.main.latest_story_row.view.*

class FavoriteRooms : Fragment() {

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_favorite_rooms, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fetchListOfFavoriteRooms()


    }

    private fun fetchListOfFavoriteRooms() {


        adapter.clear()


        Log.e("latest", "fetching list of favorite rooms")

        val userUUID = FirebaseAuth.getInstance().uid

        FirebaseFirestore.getInstance().collection("users").document(userUUID!!).collection("rooms").get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.forEach {
                    adapter.add(FavoriteStoryItem(it.getString("TITLE")!!, it.getString("ID")!!))
                    Log.e(it.getString("TITLE"), "fetching list of rooms")

                }
                checkIfRoomsFavorited()

            }.addOnFailureListener {
            Log.e("latest", "fail to catch")
        }

        recycler_favorite_rooms!!.adapter = adapter
        adapter.setOnItemClickListener { item, _ ->
            openStory(item.extras.get(EXTRA_UUID).toString(), item.extras.get(EXTRA_TITLE).toString())

        }

        //swipeContainer_favorite_rooms.isRefreshing = false
    }

    private fun checkIfRoomsFavorited() {

        if (adapter.itemCount == 0) {
            activity?.layoutInflater?.inflate(R.layout.no_favorite_rooms,favorite_rooms_constraint)

        }
    }



    private fun openStory(ROOM_UUID: String, ROOM_TITLE: String) {
        val intent = Intent(activity, StoryActivity::class.java)
        intent.putExtra(EXTRA_UUID, ROOM_UUID)
        intent.putExtra(EXTRA_TITLE, ROOM_TITLE)
        startActivity(intent)
    }

    companion object {
        fun newInstance(): FavoriteRooms = FavoriteRooms()
    }


}

class FavoriteStoryItem(val title: String, val roomUUID: String) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.title_textview_latest_story.text = title
        viewHolder.extras.put(LatestStories.EXTRA_UUID, roomUUID)
        viewHolder.extras.put(LatestStories.EXTRA_TITLE, title)

    }

    override fun getLayout(): Int {
        return R.layout.latest_story_row
    }
}