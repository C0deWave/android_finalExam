package com.example.finalexam.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class GalleryViewModel : ViewModel() {

    val user = FirebaseAuth.getInstance().currentUser

    private val _text = MutableLiveData<String>().apply {
        value = "${user?.displayName}"
    }
    val text: LiveData<String> = _text
}