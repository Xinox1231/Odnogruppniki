package com.bignerdranch.android.chat

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bignerdranch.android.chat.fragments.ChatListFragment
import com.bignerdranch.android.chat.fragments.SettingsFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ApplicationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application)

        val toolbar: Toolbar = findViewById(R.id.chat_list_toolbar)
        val navigationView : NavigationView = findViewById(R.id.chat_list_nav_view)
        val headerLayout: View = navigationView.getHeaderView(0)
        val headerTextView: TextView = headerLayout.findViewById(R.id.user_display_name)
        drawerLayout = findViewById(R.id.chat_list_drawer_layout)

        navigationView.setNavigationItemSelectedListener(this)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val currentUser = Firebase.auth.currentUser
        headerTextView.text = currentUser?.displayName.toString()

        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction().replace(R.id.chat_list_frame_layout, ChatListFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_menu_chat_list)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_menu_chat_list -> supportFragmentManager.beginTransaction().replace(R.id.chat_list_frame_layout, ChatListFragment()).commit()
            R.id.nav_menu_settings -> supportFragmentManager.beginTransaction().replace(R.id.chat_list_frame_layout, SettingsFragment()).commit()
            R.id.nav_menu_logout -> {
                val pref = getSharedPreferences("account_data", MODE_PRIVATE)
                val edit = pref.edit()
                edit.clear()
                edit.apply()
                val intent = Intent(this@ApplicationActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}