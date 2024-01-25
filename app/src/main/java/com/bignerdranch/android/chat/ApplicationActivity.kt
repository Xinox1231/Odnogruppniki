package com.bignerdranch.android.chat

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bignerdranch.android.chat.adapters.UserListAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ApplicationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var db : FirebaseFirestore
    lateinit var chatList : ListView
    lateinit var navigationView : NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application)

        chatList = findViewById(R.id.application_chat_list)
        val toolbar: Toolbar = findViewById(R.id.application_toolbar)
        navigationView = findViewById(R.id.application_navigation_view)
        val headerLayout: View = navigationView.getHeaderView(0)
        val headerTextView: TextView = headerLayout.findViewById(R.id.navigation_header_display_name)
        drawerLayout = findViewById(R.id.application_drawer_layout)

        navigationView.setNavigationItemSelectedListener(this)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val currentUser = Firebase.auth.currentUser
        headerTextView.text = currentUser?.displayName.toString()
        db = Firebase.firestore // экземпляр firestore
        var users: ArrayList<User>

        getUsers(currentUser!!.uid) { userlist ->
            users = userlist
            // Обновляет адаптер в колбэке
            val adapter = UserListAdapter(this, android.R.layout.simple_list_item_1, users)
            chatList.adapter = adapter

            chatList.setOnItemClickListener { parent, view, position, id ->
                val intent = Intent(this, ChatActivity::class.java)
                val otherUser = users[position]

                findPrivateChat(currentUser.uid, otherUser.uid) { chatId ->
                    if (chatId != null) {
                        // Документ найден, теперь есть идентификатор чата (chatId)
                        intent.putExtra("CHAT_ID", chatId)
                        intent.putExtra("SECOND_USER", otherUser)
                        startActivity(intent)
                    } else{
                        Toast.makeText(this,"Ошибка на нашей стороне, уже чиним!",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        navigationView.setCheckedItem(R.id.nav_menu_chat)
    }

    fun getUsers(userId: String, callback: (ArrayList<User>) -> Unit) { //Получаю список пользователей всего приложения
        val users = arrayListOf<User>()
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if(document.data.get("uid") as String != userId){
                        users.add(User(
                            document.data.get("uid") as String,
                            document.data.get("displayName") as String,
                            document.data.get("email") as String,
                            document.data.get("photoUrl") as String
                        ))
                    }
                }

                callback(users)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                callback(ArrayList())
            }
    }

    fun findPrivateChat(currentUserUID: String, otherUserUID: String, onComplete: (String?) -> Unit) {// Возвращает ссылку на документ с чатом
        val db = FirebaseFirestore.getInstance()

        val participantCombinations = listOf(
            listOf(currentUserUID, otherUserUID),
            listOf(otherUserUID, currentUserUID),
        )

        db.collection("chats")
            .whereIn("participants", participantCombinations)
            .limit(1)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents: QuerySnapshot? = task.result
                    if (documents != null && !documents.isEmpty) {
                        // Найден документ, передаем его идентификатор в onComplete
                        onComplete.invoke(documents.documents[0].id)
                    } else {
                        // Документ не найден, создаем новый чат
                        val data = hashMapOf(
                            "participants" to listOf(currentUserUID, otherUserUID),
                            "type" to "individual")

                        db.collection("chats").add(data)
                            .addOnSuccessListener { documentReference ->
                                onComplete.invoke(documentReference.id)
                            }
                            .addOnFailureListener { exception ->
                                // Обработка ошибки при создании чата
                                exception.printStackTrace()
                                onComplete.invoke(null)
                            }
                    }
                } else {
                    // Обработка ошибки при выполнении запроса
                    val exception: Exception? = task.exception
                    exception?.printStackTrace()
                    onComplete.invoke(null)
                }
            }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
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

}