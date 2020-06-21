package com.example.finalexam.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.finalexam.ConfigurationActivity
import com.example.finalexam.MainActivity
import com.example.finalexam.R
import com.example.finalexam.loginActivity
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_account_configuration.view.*

class accountConfigurationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val inflater = inflater.inflate(R.layout.fragment_account_configuration, container, false)
        inflater.logoutButton.setOnClickListener {
            (activity as MainActivity).signOut()
        }
        return inflater
    }


    companion object {
        fun newInstance(): accountConfigurationFragment {
            return accountConfigurationFragment()
        }
    }
}