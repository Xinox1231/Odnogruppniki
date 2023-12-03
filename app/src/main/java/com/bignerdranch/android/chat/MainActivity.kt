package com.bignerdranch.android.chat

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.bignerdranch.android.chat.adapters.UserListAdapter

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    lateinit var db : FirebaseFirestore
    lateinit var lvChats : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lvChats = findViewById(R.id.main_layout)

        var user = FirebaseAuth.getInstance().currentUser // пользователь
        val userId = intent.getStringExtra("user_id") // id пользователя
        val displayName = intent.getStringExtra("display_name") // email пользователя

        db = Firebase.firestore // экземпляр firebase

        var userList: ArrayList<User>


        if (userId != null) {
            getUsers(userId) { userlist ->
                userList = userlist

                // Update the adapter inside the callback
                val adapter = UserListAdapter(this, android.R.layout.simple_list_item_1, userList)
                lvChats.adapter = adapter

                lvChats.setOnItemClickListener { parent, view, position, id ->
                    val intent = Intent(this@MainActivity, ChatActivity::class.java)
                    findChatDocument(userId, userList.get(position).uid) { chatId ->
                        if (chatId != null) {
                            // Документ найден, теперь есть идентификатор чата (chatId)
                            intent.putExtra("chatId",chatId)
                            startActivity(intent)
                            adapter.notifyDataSetChanged()
                        } else {
                            // Документ не найден
                            println("Чат не найден.")
                        }
                    }


                }
            }
        }


    }

    fun getUsers(userId: String, callback: (ArrayList<User>) -> Unit) {
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
                Log.w(TAG, "Error getting documents: ", exception)
                callback(ArrayList())
            }
    }

    fun findChatDocument(uid1: String, uid2: String, onComplete: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("chats")
            .whereArrayContainsAny("uids", listOf(uid1, uid2))
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents: QuerySnapshot? = task.result
                    if (documents != null && !documents.isEmpty) {
                        // Найден документ, передаем его идентификатор в onComplete
                        onComplete.invoke(documents.documents[0].id)
                    } else {
                        // Документ не найден, передаем null в onComplete
                        onComplete.invoke(null)
                    }
                } else {
                    // Обработка ошибки при выполнении запроса
                    val exception: FirebaseFirestoreException? = task.exception as FirebaseFirestoreException?
                    // Ваш код обработки ошибки
                    onComplete.invoke(null)
                }
            }
    }


}