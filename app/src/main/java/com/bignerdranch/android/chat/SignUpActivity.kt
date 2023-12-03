package com.bignerdranch.android.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    lateinit var btnRegister: Button
    lateinit var btnToLoginActivity: Button
    lateinit var tvEmail: EditText
    lateinit var tvPassword: EditText
    lateinit var tvDisplayName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btnRegister = findViewById(R.id.signup_btn_signup)
        btnToLoginActivity = findViewById(R.id.signup_btn_login)
        tvEmail = findViewById(R.id.signup_tv_email)
        tvPassword = findViewById(R.id.signup_tv_password)
        tvDisplayName = findViewById(R.id.signup_tv_displayName)

        btnRegister.setOnClickListener {
            when {
                TextUtils.isEmpty(tvEmail.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Введите e-mail",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(tvPassword.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Введите пароль",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(tvDisplayName.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Введите имя пользователя",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {

                    val email: String = tvEmail.text.toString().trim { it <= ' ' }
                    val password: String = tvPassword.text.toString().trim { it <= ' ' }
                    val displayName: String = tvDisplayName.text.toString().trim { it <= ' ' }

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) { //если создан
                                val firebaseUser: FirebaseUser? = task.result?.user

                                if (firebaseUser != null) {
                                    // Регистрация успешна
                                    val usersCollection = Firebase.firestore.collection("users")
                                    val userData = hashMapOf(
                                        "displayName" to displayName,
                                        "email" to email,
                                        "photoUrl" to "",
                                        "uid" to firebaseUser.uid
                                    )

                                    // Добавляем информацию в Firestore
                                    usersCollection.document(firebaseUser.uid)
                                        .set(userData)
                                        .addOnCompleteListener { firestoreTask ->
                                            if (firestoreTask.isSuccessful) {
                                                // Информация о пользователе успешно добавлена в Firestore

                                                // Обновляем профиль пользователя
                                                val profileUpdates =
                                                    UserProfileChangeRequest.Builder()
                                                        .setDisplayName(displayName)
                                                        .build()

                                                firebaseUser.updateProfile(profileUpdates)
                                                    .addOnCompleteListener { updateTask ->
                                                        if (updateTask.isSuccessful) {
                                                            // Имя пользователя успешно установлено

                                                            // Код, зависящий от актуальной информации о пользователе
                                                            val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                            intent.putExtra("user_id", firebaseUser.uid)
                                                            intent.putExtra("display_name", displayName)
                                                            startActivity(intent)
                                                            finish()
                                                        } else {
                                                            // Ошибка при установке имени пользователя
                                                            val exception = updateTask.exception
                                                            // Обработка ошибки
                                                        }
                                                    }
                                            } else {
                                                // Ошибка при добавлении информации о пользователе в Firestore
                                                val exception = firestoreTask.exception
                                            }
                                        }
                                } else {
                                    // Обработка сценария, когда firebaseUser == null
                                }
                            } else {
                                // Обработка ошибки при регистрации
                                val exception = task.exception
                            }
                        }
                    }
                }
        }
        btnToLoginActivity.setOnClickListener{
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}