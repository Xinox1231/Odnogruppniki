package com.bignerdranch.android.chat.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bignerdranch.android.chat.ApplicationActivity
import com.bignerdranch.android.chat.R
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SettingsFragment : Fragment() {

    lateinit var btnChangeDisplayName : Button
    lateinit var dialogChangeDisplayName : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        btnChangeDisplayName = view.findViewById(R.id.fragment_settings_change_display_name)
        dialogChangeDisplayName = Dialog(requireContext())  // Инициализация dialogChangeDisplayName

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnChangeDisplayName.setOnClickListener{
            showCustomDialog()
        }
    }

    private fun showCustomDialog(){
        dialogChangeDisplayName.setContentView(R.layout.custom_dialog_change_nickname)
        dialogChangeDisplayName.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val displayName : EditText = dialogChangeDisplayName.findViewById(R.id.dialog_change_display_name_edit_text)
        val btnSubmitChangeDisplayName : Button = dialogChangeDisplayName.findViewById(R.id.dialog_change_display_name_submit_button)
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
                                        requireContext(),
                                        "Имя успешно изменено!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    restartApplication()
                                } else {
                                    // Обработка ошибки обновления
                                    Toast.makeText(
                                        requireContext(),
                                        "Ошибка на нашей стороне, чиним!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка на нашей стороне, уже чиним!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        dialogChangeDisplayName.show()
    }
    private fun restartApplication() {
        val intent = Intent(requireContext(), ApplicationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        requireActivity().finish()
    }
}
