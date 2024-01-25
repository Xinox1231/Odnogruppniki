package com.bignerdranch.android.chat

import com.google.firebase.Timestamp

data class Message(val senderName: String, val senderId: String, val text: String, val timeStamp: Timestamp?) {
}