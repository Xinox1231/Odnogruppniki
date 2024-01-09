package com.bignerdranch.android.chat

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    lateinit var etMail : EditText
    lateinit var etPassword : EditText
    lateinit var btnLogin : Button
    lateinit var tvForgotPassword : TextView
    lateinit var tvRegister : TextView

    lateinit var pref : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        checkBox()

        btnLogin = findViewById(R.id.login_btn_enter)
        tvForgotPassword = findViewById(R.id.login_btn_recovery_password)
        tvRegister = findViewById(R.id.login_btn_register)
        etMail = findViewById(R.id.login_et_email)
        etPassword = findViewById(R.id.login_et_password)

        btnLogin.setOnClickListener {
            if((isEmptyFieldsInAccountData(etMail,etPassword)) == false){
                    val email: String = etMail.text.toString()
                    val password: String = etPassword.text.toString()

                    signIn(email,password)
                }
            }

        tvRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        tvForgotPassword.setOnClickListener{
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    fun isEmptyFieldsInAccountData(emailView: TextView, passwordView: TextView): Boolean{
        if(TextUtils.isEmpty(emailView.text.toString().trim { it <= ' ' })){
            Toast.makeText(
                this@LoginActivity,
                "Введите e-mail",
                Toast.LENGTH_SHORT
            ).show()
            return true
        }else if(TextUtils.isEmpty(passwordView.text.toString().trim { it <= ' ' })){
            Toast.makeText(
                this@LoginActivity,
                "Введите пароль",
                Toast.LENGTH_SHORT
            ).show()
            return true
        }else return false
    }

    fun signIn(email: String, password: String){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!

                    Toast.makeText(
                        this@LoginActivity,
                        "Вы вошли в аккаунт", Toast.LENGTH_SHORT
                    ).show()
                    val editor = pref.edit()
                    editor.putString("user_id",firebaseUser.uid)
                    editor.apply()

                    val intent = Intent(this@LoginActivity, ChatListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    Log.d("Name", firebaseUser.displayName.toString())
                    Log.d("UID",firebaseUser.uid)
                }
                else{
                    Toast.makeText(this@LoginActivity,
                        task.exception!!.message.toString(),
                        Toast.LENGTH_LONG).show()
                }
            }
    }


    fun checkBox(){
        pref = getSharedPreferences("account_data", MODE_PRIVATE)
        if(pref.contains("user_id")){
            val intent = Intent(this@LoginActivity, ChatListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("user_id", pref.getString("user_id","user"))
            startActivity(intent)
        }
    }
}