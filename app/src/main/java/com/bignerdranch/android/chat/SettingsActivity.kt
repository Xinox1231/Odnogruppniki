package com.bignerdranch.android.chat

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class SettingsActivity : AppCompatActivity() {

    lateinit var imageViewAvatar : ImageView
    lateinit var btnChangeDisplayName : Button
    lateinit var btnChangeAvatar : Button
    lateinit var dialogChangeDisplayName : Dialog
    lateinit var tvDisplayName : TextView
    lateinit var toolbar: Toolbar

    lateinit var currentUser : FirebaseUser
    lateinit var storageRef : StorageReference
    lateinit var usersAvatarsRef : StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        init()
        setSupActionBar()

        btnChangeDisplayName.setOnClickListener{
            showCustomDialog()
        }
        btnChangeAvatar.setOnClickListener {
            getImage()
        }
    }

    private fun init(){
        imageViewAvatar = findViewById(R.id.settings_image_avatar)
        btnChangeDisplayName = findViewById(R.id.settings_btn_change_display_name)
        btnChangeAvatar = findViewById(R.id.settings_btn_change_avatar)
        tvDisplayName = findViewById(R.id.settings_tv_display_name)
        toolbar = findViewById(R.id.settings_toolbar)
        storageRef = FirebaseStorage.getInstance().getReference("Images")
        usersAvatarsRef = storageRef.child("UsersAvatars")
        currentUser = Firebase.auth.currentUser!!
        dialogChangeDisplayName = Dialog(this)  // Инициализация dialogChangeDisplayName

        tvDisplayName.text = currentUser!!.displayName
    }

    private fun setSupActionBar(){
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Настройки"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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

    fun getImage() {
        val intentChooser = Intent(Intent.ACTION_GET_CONTENT)
        intentChooser.type = "image/*"
        startActivityForResult(intentChooser, 1)
    }


    private fun uploadImage(uri: Uri) {
        val fileReference = usersAvatarsRef.child("${currentUser.uid}.jpg")
        fileReference.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                val userDocumentReference = Firebase.firestore.collection("users").document(currentUser.uid)
                Toast.makeText(this, "Фотография успешно обновлена", Toast.LENGTH_SHORT).show()
                //userDocumentReference.update("photoURI", uri)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Ошибка, попробуйте ещё раз", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri = data.data!!
            val profileUpdate = UserProfileChangeRequest.Builder().setPhotoUri(selectedImageUri).build()
            currentUser.updateProfile(profileUpdate).addOnSuccessListener {
                uploadImage(selectedImageUri)
                imageViewAvatar.setImageURI(selectedImageUri)
            }
        }
    }

}