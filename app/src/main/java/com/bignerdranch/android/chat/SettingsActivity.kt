package com.bignerdranch.android.chat

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SettingsActivity : AppCompatActivity() {

    lateinit var btnChangeDisplayName : Button
    lateinit var dialogChangeDisplayName : Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        btnChangeDisplayName = findViewById(R.id.settings_btn_change_display_name)
        val toolbar : Toolbar = findViewById(R.id.settings_toolbar)
        val displayName : TextView = findViewById(R.id.settings_tv_display_name)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Настройки"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        dialogChangeDisplayName = Dialog(this)  // Инициализация dialogChangeDisplayName
        btnChangeDisplayName.setOnClickListener{
            showCustomDialog()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        displayName.text = currentUser!!.displayName
    }
    private fun showCustomDialog(){
        dialogChangeDisplayName.setContentView(R.layout.custom_dialog_change_nickname)
        dialogChangeDisplayName.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val displayName : EditText = dialogChangeDisplayName.findViewById(R.id.change_display_name_et)
        val btnSubmitChangeDisplayName : Button = dialogChangeDisplayName.findViewById(R.id.change_display_name_btn_submit)
        val firebaseUser = Firebase.auth.currentUser
        val userId = firebaseUser!!.uid

        btnSubmitChangeDisplayName.setOnClickListener{
            val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(displayName.text.toString()).build()
            firebaseUser!!.updateProfile(profileUpdates).addOnCompleteListener { updateTask ->
                if (updateTask.isSuccessful) {
                    val usersCollection = FirebaseFirestore.getInstance().collection("users")
                    val userDocument = usersCollection.document(userId)

                    // Создаем карту с данными, которые обновляем
                    val updates = hashMapOf<String, Any>(
                        "displayName" to displayName.text.toString()
                    )

                    // Обновляем документ
                    userDocument.update(updates)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                // Обновление прошло успешно
                                Toast.makeText(
                                    this,
                                    "Имя успешно изменено!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                restartApplication()
                            } else {
                                // Обработка ошибки обновления
                                Toast.makeText(
                                    this,
                                    "Ошибка на нашей стороне, уже чиним!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Ошибка на нашей стороне, уже чиним!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        dialogChangeDisplayName.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()
        return true
    }
    private fun restartApplication() {
        val intent = Intent(this, ApplicationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        this.finish()
    }
}