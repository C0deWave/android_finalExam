package com.example.finalexam.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.finalexam.ConfigurationActivity
import com.example.finalexam.MainActivity
import com.example.finalexam.R
import com.example.finalexam.loginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_account_configuration.*
import kotlinx.android.synthetic.main.fragment_account_configuration.view.*

//사용자 계정 프레그먼트의 코틀린파일입니다.

class accountConfigurationFragment : Fragment() {

    val user = FirebaseAuth.getInstance().currentUser

    //-------------------------------------------------------------------------------
    //onCreateView함수입니다.
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
    }//end of onCreateView

    //------------------------------------------------------------------------------
    //onStart 함수입니다.
    override fun onStart() {
        super.onStart()

        //화면에 유저 정보를 불러줍니다.
        bindingUserInfo()

        //정보 변경을 위한 버튼 리스너 입니다.
        userInfoChangeButton.setOnClickListener {
            goUserInfoChange()
        }

        //회원 탈퇴를 하는 기능입니다.
        userWithdrawalButton.setOnClickListener {
            deleteUserInfo()
        }
    }//end of onStart

    //-------------------------------------------------------------------------------------------
    //유저의 회원 탙퇴를 맡습니다.
    //계정의 삭제와 지정한 자격증 데이터 베이스를 삭제하는 기능을 합니다.
    fun deleteUserInfo() {
        //파이어베이스 데이터베이스에서 유저의 데이터를 삭제합니다.
        //파이어베이스 데이터베이스에 저장합니다.
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child("${FirebaseAuth.getInstance().currentUser?.uid}")
            .removeValue()

        // 파이어베이스 Auth의 유저를 삭제하는 기능입니다.
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(context,"회원 탈퇴를 했습니다.",Toast.LENGTH_LONG).show()
                    startActivity(loginActivity.getLaunchIntent(requireContext()))
                    activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                }
            }
    }

    //-----------------------------------------------------------------------------------------
    //유저 정보를 바꾸기 위한 화면으로 넘어갑니다.
    private fun goUserInfoChange() {
        //여기서는 백스택을 남기면서 이동합니다.
        startActivity(Intent(requireContext(),ConfigurationActivity::class.java))
        activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        // 정보를 바로 고치기 위해서 onDestroy를 호출해 줍니다.
        onDestroy()
    }

    //-------------------------------------------------------------------------------------------
    // 화면에 유저정보를 바인딩해주는 함수입니다.
    fun bindingUserInfo() {
        // 닉네임을 설정해서 보여주는 역할을 합니다.
        userName.text = user?.displayName.toString()

        // 이메일을 보여줍니다.
        if (userEmailText.text.isNullOrBlank()){
            userEmailText.text = ""
        }else{
            userEmailText.text = user?.email.toString()
        }

        // 유저 이미지를 가져와서 보여줍니다.
        Picasso.get()
            .load(user!!.photoUrl )
            .into(userImage)

        //원하는 자격증을 바인딩합니다.
        if (user != null) {
            FirebaseDatabase.getInstance().reference
                .child("users")
                .child(user.uid)
                .addListenerForSingleValueEvent(object  : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        var data = p0?.value as Map<String,Any>
                        userCertificationText.text = data["cert2"].toString()
                    }
                }).toString()
        }
    }//end of bindUserInfo

    //--------------------------------------------------------------------------------------
    //Fragment 인스턴스를 생성해주는 함수입니다.
    companion object {
        fun newInstance(): accountConfigurationFragment {
            return accountConfigurationFragment()
        }
    }
}