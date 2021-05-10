package com.inspiredmedia.storywriter

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

//    private lateinit var textMessage: TextView
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.favorite -> {
                val favoriteRooms = FavoriteRooms.newInstance()
                openFragment(favoriteRooms)

                return@OnNavigationItemSelectedListener true
            }
            R.id.latest -> {
                val latestStories = LatestStories.newInstance()
                openFragment(latestStories)
                return@OnNavigationItemSelectedListener true
            }
            R.id.profile -> {
                val profile = Profile.newInstance()
                openFragment(profile)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.navigationView)
        supportActionBar?.hide()
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        var logged = verifyUserIsLogged()
        if(logged)
        {
            val favoriteRooms = FavoriteRooms.newInstance()
            openFragment(favoriteRooms)
        }

    }


    private fun verifyUserIsLogged(): Boolean{
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return false
        }
        return true
    }

    private fun openFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
