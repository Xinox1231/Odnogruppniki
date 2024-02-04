package com.bignerdranch.android.chat

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.chat.adapters.CurrentChatAdapter
import com.bignerdranch.android.chat.data_classes.Message
import com.bignerdranch.android.chat.data_classes.User
import com.bignerdranch.android.chat.databinding.ActivityChatBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: CurrentChatAdapter
    private lateinit var rcChatList: RecyclerView
    private lateinit var tvMessageText: EditText
    private lateinit var btnSubmitMessage: ImageButton
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bind()
        
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val chatId = intent.getStringExtra("CHAT_ID")!!
        val secondUserDisplayName = intent.getStringExtra("SECOND_USER")
        val db = Firebase.firestore
        val currentChatDocument = db.collection("chats").document(chatId)
        val messagesCollection = currentChatDocument.collection("messages")
        val currentUser = FirebaseAuth.getInstance().currentUser!! // пользователь
        val currentUserDisplayName = currentUser.displayName!!

        if (secondUserDisplayName != null) {
            supportActionBar?.title = secondUserDisplayName
        }

        val listeners: MutableList<ListenerRegistration> = mutableListOf()

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
                    val text = document.getString("text") ?: "standart"
                    val timestamp =
                        document.getTimestamp("timeStamp") ?: document.getTimestamp("timeStamp")

                    // Создаем объект сообщения и обрабатываем его
                    val message = Message(senderName, senderId, text, timestamp)
                    adapter.addMessage(message)
                }
            }
        }

// Добавляем созданный слушатель в список слушателей (например, для последующей возможности отписаться от него)
        listeners.add(registration)

        btnSubmitMessage.setOnClickListener {
            if (tvMessageText.text.toString() != "") {
                val timestamp = Timestamp.now()
                val message = Message(
                    currentUserDisplayName, // Имя
                    currentUser.uid, // UID
                    tvMessageText.text.toString(), // Текст сообщения
                    timestamp
                )
                currentChatDocument.update("latestMessage",tvMessageText.text.toString())
                messagesCollection.add(message)
                tvMessageText.text.clear()
            }
        }
    }

    fun bind() = with(binding) {
        rcChatList = chatList
        adapter = CurrentChatAdapter()
        rcChatList.layoutManager = LinearLayoutManager(this@ChatActivity)
        rcChatList.adapter = adapter
        tvMessageText = chatEdMessageWrite
        btnSubmitMessage = chatIbSubmitMessage
        toolbar = chatToolbar
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return true
    }
}