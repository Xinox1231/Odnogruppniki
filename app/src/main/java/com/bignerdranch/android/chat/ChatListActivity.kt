package com.bignerdranch.android.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bignerdranch.android.chat.fragments.HomeFragment
import com.bignerdranch.android.chat.fragments.SettingsFragment
import com.google.android.material.navigation.NavigationView

class ChatListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        val toolbar: Toolbar = findViewById(R.id.chat_list_toolbar)
        drawerLayout = findViewById(R.id.chat_list_drawer_layout)
        val navigationView : NavigationView = findViewById(R.id.chat_list_nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        val toggle : ActionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction().replace(R.id.chat_list_frame_layout, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_menu_home)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_menu_home -> supportFragmentManager.beginTransaction().replace(R.id.chat_list_frame_layout, HomeFragment()).commit()
            R.id.nav_menu_settings -> supportFragmentManager.beginTransaction().replace(R.id.chat_list_frame_layout, SettingsFragment()).commit()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}