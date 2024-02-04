package com.bignerdranch.android.chat.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bignerdranch.android.chat.data_classes.Message
import com.bignerdranch.android.chat.databinding.ItemLeftMessageBinding
import com.bignerdranch.android.chat.databinding.ItemRightMessageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class CurrentChatAdapter : RecyclerView.Adapter<CurrentChatAdapter.ViewHolder>()  {
    private val messages = ArrayList<Message>()
    private val MESSAGE_LEFT_CODE = 0
    private val MESSAGE_RIGHT_CODE = 1
    private var currentUser = FirebaseAuth.getInstance().currentUser

    class ViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message : Message) {
            when (binding) {
                is ItemLeftMessageBinding -> {
                    binding.itemMessage.text = message.text
                    Log.d("LEFT", message.text)
                }
                is ItemRightMessageBinding -> {
                    binding.itemMessage.text = message.text
                    Log.d("RIGHT", message.text)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            MESSAGE_RIGHT_CODE -> {
                val binding = ItemRightMessageBinding.inflate(inflater, parent, false)
                ViewHolder(binding)
            }
            else -> {
                val binding = ItemLeftMessageBinding.inflate(inflater, parent, false)
                ViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(itemCount - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUser!!.uid) {
            MESSAGE_RIGHT_CODE
        } else {
            MESSAGE_LEFT_CODE
        }
    }
}