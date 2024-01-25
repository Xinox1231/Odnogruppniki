package com.bignerdranch.android.chat

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import com.bignerdranch.android.chat.adapters.ChatAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {

    lateinit var lvChat : ListView
    lateinit var tvMessageText : EditText
    lateinit var btnSubmitMessage : ImageButton
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        lvChat = findViewById(R.id.chat_list)
        tvMessageText = findViewById(R.id.chat_ed_message_write)
        btnSubmitMessage = findViewById(R.id.chat_ib_submit_message)
        toolbar = findViewById(R.id.chat_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val db = Firebase.firestore
        val currentUser = FirebaseAuth.getInstance().currentUser // пользователь
        val secondUser = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("DATA", User::class.java)
        } else {
            intent.getParcelableExtra<User>("DATA")
        }

        val chatId = intent.getStringExtra("CHAT_ID")


        if (secondUser != null) {
            supportActionBar?.title = secondUser.displayName
        }

        val userName = currentUser!!.displayName!!
        val messagesList = arrayListOf<Message>() // список сообщений

        val messagesCollection = FirebaseFirestore.getInstance().collection("chats").document(chatId!!).collection("messages") // коллекция сообщений
        val listeners: MutableList<ListenerRegistration> = mutableListOf()
        val adapter = ChatAdapter(this, android.R.layout.simple_list_item_1, messagesList)

        // Очищаем список перед заполнением новыми данными
        messagesList.clear()

// Добавляем слушателя для документа
        val registration = messagesCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Обработка ошибки при получении данных
                return@addSnapshotListener
            }

            for (documentChange in snapshot!!.documentChanges) {
                // Проверяем, является ли изменение добавлением нового документа
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val document = documentChange.document

                    // Получаем данные нового документа
                    val senderId = document.getString("senderId") ?: ""
                    val senderName = document.getString("senderName") ?: ""
                    val text = document.getString("text") ?: ""
                    val timestamp = document.getTimestamp("timeStamp") ?: document.getTimestamp("timeStamp")

                    // Создаем объект сообщения и обрабатываем его
                    val message = Message(senderName!!, senderId!!, text!!, timestamp)
                    messagesList.add(message)

                    // Обновляем адаптер после добавления нового сообщения
                    adapter.notifyDataSetChanged()
                    lvChat.adapter = adapter
                }
            }
        }

// Добавляем созданный слушатель в список слушателей (например, для последующей возможности отписаться от него)
        listeners.add(registration)

        btnSubmitMessage.setOnClickListener{
            if(tvMessageText.text.toString() != ""){
                val timestamp = Timestamp.now()
                val message = Message(
                    userName, // Имя
                    currentUser.uid, // UID
                    tvMessageText.text.toString(), // Текст сообщения
                    timestamp) // Время отправления
                val messagesCollection = db.collection("chats").document(chatId!!).collection("messages")
                messagesCollection.add(message)
                tvMessageText.text.clear()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()
        return true
    }
}