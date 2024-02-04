package com.bignerdranch.android.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.chat.adapters.UserListAdapter
import com.bignerdranch.android.chat.data_classes.Chat
import com.bignerdranch.android.chat.databinding.ActivityApplicationBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ApplicationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    UserListAdapter.Listener {

    lateinit var binding: ActivityApplicationBinding
    lateinit var chatList: RecyclerView
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    val currentUser = Firebase.auth.currentUser!!
    lateinit var database: FirebaseFirestore
    val adapter = UserListAdapter(this)

    lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bind()

        chatList.adapter = adapter
        chatList.layoutManager = LinearLayoutManager(this)

        database = Firebase.firestore
        val headerLayout: View = navigationView.getHeaderView(0)
        val headerTextView: TextView =
            headerLayout.findViewById(R.id.navigation_header_display_name)
        headerTextView.text = currentUser?.displayName.toString()

        navigationView.setNavigationItemSelectedListener(this)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        findPrivateChats(currentUser.uid)
    }

    fun bind() = with(binding) {
        drawerLayout = applicationDrawerLayout
        chatList = applicationChatList
        navigationView = applicationNavigationView
        toolbar = applicationToolbar
    }

    override fun onResume() {
        super.onResume()
        navigationView.setCheckedItem(R.id.nav_menu_chat)
    }

    fun findPrivateChats(currentUserUID: String) { // Получаю существующие чаты
        database.collection("chats").whereArrayContains("participants", currentUserUID).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) { //
                        // Обработка результатов запроса
                        val chatData = document.data // Возвращает map полей
                        val participants: List<String> = chatData["participants"] as List<String>
                        val otherUserId = if (participants[0] != currentUserUID) {
                            participants[0]
                        } else {
                            participants[1]
                        }
                        Log.d("users", otherUserId)
                        findUserById(otherUserId) { otherUserData ->
                            val chatId = document.id
                            val otherUserDisplayName: String =
                                otherUserData!!["displayName"] as String
                            val otherUserId: String = otherUserData["uid"] as String
                            val latestMessage = chatData["latestMessage"] as String
                            val photoURI = "hol"
                            val chat = Chat(chatId, otherUserId, otherUserDisplayName, latestMessage, photoURI)
                            adapter.addChat(chat)
                        }
                    }
                } else {
                    Toast.makeText(this, "Ошибка, перезапустите приложение", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    fun findUserById(userId: String, callback: (Map<String, Any>?) -> Unit) {
        val usersCollection = database.collection("users")
        val userToFound = usersCollection.document(userId)

        userToFound.get()
            .addOnSuccessListener { documentSnapshot ->
                val userData = if (documentSnapshot.exists()) {
                    documentSnapshot.data
                } else {
                    null
                }

                callback(userData)
            }
            .addOnFailureListener { exception ->
                // Обработка ошибки при получении документа
                println("Ошибка при получении документа: $exception")
                callback(null)
            }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_menu_settings -> {
                navigationView.setCheckedItem(R.id.nav_menu_chat)
                intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            R.id.nav_menu_logout -> {
                val pref = getSharedPreferences("account_data", MODE_PRIVATE)
                val edit = pref.edit()
                edit.clear()
                edit.apply()
                drawerLayout.closeDrawer(GravityCompat.START)
                val intent = Intent(this@ApplicationActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        return true
    }

    override fun onClick(chat: Chat) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("CHAT_ID", chat.id)
        intent.putExtra("SECOND_USER", chat.otherUserDisplayName)
        startActivity(intent)
    }
}