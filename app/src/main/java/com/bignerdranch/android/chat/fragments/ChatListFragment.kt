package com.bignerdranch.android.chat.fragments

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.bignerdranch.android.chat.ChatActivity
import com.bignerdranch.android.chat.R
import com.bignerdranch.android.chat.User
import com.bignerdranch.android.chat.adapters.UserListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatListFragment : Fragment() {

    lateinit var db : FirebaseFirestore
    lateinit var chatList : ListView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_list, container, false)
        chatList = view.findViewById(R.id.chat_list)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var currentUser = FirebaseAuth.getInstance().currentUser!!
        db = Firebase.firestore // экземпляр firestore
        var users: ArrayList<User>

        getUsers(currentUser.uid) { userlist ->
            users = userlist
            // Обновляет адаптер в колбэке
            val adapter = UserListAdapter(requireContext(), android.R.layout.simple_list_item_1, users)
            chatList.adapter = adapter

            chatList.setOnItemClickListener { parent, view, position, id ->
                val intent = Intent(requireContext(), ChatActivity::class.java)
                val otherUser = users[position]

                findPrivateChat(currentUser.uid, otherUser.uid) { chatId ->
                    if (chatId != null) {
                        // Документ найден, теперь есть идентификатор чата (chatId)
                        intent.putExtra("CHAT_ID", chatId)
                        intent.putExtra("DISPLAY_NAME", otherUser.displayName)
                        startActivity(intent)
                    } else {
                        // Проверяем, существует ли чат в обратном направлении
                        findPrivateChat(otherUser.uid, currentUser.uid) { reverseChatId ->
                            if (reverseChatId != null) {
                                // Используем существующий чат в обратном направлении
                                intent.putExtra("CHAT_ID", reverseChatId)
                                intent.putExtra("DISPLAY_NAME", otherUser.displayName)
                                startActivity(intent)
                            } else {
                                // Ни чата, ни обратного чата нет, создаем новый документ
                                val data = hashMapOf("uids" to listOf(currentUser.uid, otherUser.uid))
                                db.collection("chats").add(data)
                                    .addOnSuccessListener { document ->
                                        val newChatId = document.id
                                        intent.putExtra("CHAT_ID", newChatId)
                                        intent.putExtra("DISPLAY_NAME", otherUser.displayName)
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e(ContentValues.TAG, "Error adding document: ", exception)
                                    }
                            }
                        }
                    }
                }
            }
        }

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

    fun findPrivateChat(currentUser: String, otherUser: String, onComplete: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        val participantCombinations = listOf(
            listOf(currentUser, otherUser),
            listOf(otherUser, currentUser)
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
                            "participants" to listOf(currentUser, otherUser),
                            "type" to "individual" // Указываем тип чата
                        )

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
}