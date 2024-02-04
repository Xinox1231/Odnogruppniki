package com.bignerdranch.android.chat.data_classes


data class Chat(val id: String, val otherUserId: String, val otherUserDisplayName: String, var latestMessage: String?, val photoURI: String) {
}