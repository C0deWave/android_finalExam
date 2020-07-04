package com.example.finalexam

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalexam.dataClass.UserInfo
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import java.io.IOException
import java.io.InputStream

//로그인을 마치고 원하는 자격증과 닉네임을 정하는 부분입니다.

class ConfigurationActivity : AppCompatActivity() {

    //----------------------------------------------------------------------------------
    //전역변수 설정
    //구글 클라이언트입니다. 로그아웃, 정보가져오기를 담는 역할을 합니다.
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var user : FirebaseUser

    //자격증 리스트를 보여주기 위한 리스트 입니다.
    var arr = arrayListOf<String>()
    var jmcdList = arrayListOf<Int>()

    //userInfo Class에 저장하기 위한 변수입니다.
    var certificatPosition1 : String = ""
    var certificatPosition2 : String = ""
    var jmcdData : Int = 0

    // 인덱스 1,2,3,4 순서이다.
    var certificationList = arrayOf("기술사","기능장","기사","기능사")

    //JSON을 파싱한 값이다.
    val certificationData by lazy { readJson() }
    val jsonarr by lazy { JSONArray(certificationData) }

    //---------------------------------------------------------------------------------------------
    //onCreate 함수입니다.
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        //액션바 숨기기
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        //처음 변수들의 초기화를 담당합니다.
        initData()

        //버튼에 로그아웃 기능을 넣는다.
        signOutButton.setOnClickListener{
            signOut()
        }

        //닉네임을 지정하고 다음 화면으로 넘어가는 기능을 제공합니다.
        createNickButton.setOnClickListener {
            pushDatabase()
            pushNickName()
            goNextActivity()
        }

        //스피너에 어댑터와 리스너를 연결합니다.
        //세부사항 스피너를 개선하는 역할을 합니다.
        settingSpinner()

    }//end of onCreate

    //----------------------------------------------------------------------------------------
    //스피너를 설정하는 함수입니다.
    private fun settingSpinner() {
        topSpinner.adapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,certificationList)
        topSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(applicationContext,"자격증 종류를 선택해 주세요.",Toast.LENGTH_LONG).show()
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                //하위 자격증 항목 리스트인 arr을 새로 만들어 줍니다.
                settingCertficationList(position)

                //하위 스피너의 어댑터입니다.
                var adpt = ArrayAdapter<String>(this@ConfigurationActivity,android.R.layout.simple_spinner_dropdown_item,arr)
                bottomSpinner.adapter = adpt

                // 하위 스피너의 리스너입니다.
                bottomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        certificatPosition2 = arr[position]
                        jmcdData = jmcdList[position]
                    }
                }
            }//end of on Item Selected -> 상위스피너
        }//end of SpinnerListener -> 상위 스피너
    }//end of setting Spinner

    //-------------------------------------------------------------------------------------------
    //상위 자격분류에 맞는 하위 자격분류를 만들어 줍니다.
    fun settingCertficationList(position: Int) {
        certificatPosition1 = certificationList[position]

        //세부사항 스피너의 값을 수정합니다.
        Toast.makeText(applicationContext,"${certificationList[position]}를 선택했습니다.",Toast.LENGTH_LONG).show()
        arr.clear()
        jmcdList.clear()
        for (i in 0 .. jsonarr.length() - 1)
        {
            var jsonobj = jsonarr.getJSONObject(i)
            if (jsonobj.getString("SERIESNM") == certificationList[position]){
                arr.add(jsonobj.getString("JMFLDNM"))
                jmcdList.add(jsonobj.getInt("JMCD"))
            }

        }
    }

    //---------------------------------------------------------------------------------------
    //처음 변수들을 초기화 해주는 함수입니다.
    fun initData(){
        //세션 로그아웃 지정
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        //구글 클라이언트를 연결시킵니다.
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        //유저 정보 가져오기
        user = FirebaseAuth.getInstance().currentUser!!

        //파이어 베이스에서 닉네임을 지정했는지 확인합니다.
        //닉네임을 정했으면 MainActivity로 넘어갑니다.
        nickNameText.hint = user.displayName
    }//end of initData

    //---------------------------------------------------------------------------------------
    //다음 페이지로 이동하는 역할을 합니다.
    fun goNextActivity(){
        startActivity(MainActivity.getLaunchIntent(this))
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
    }

    //---------------------------------------------------------------------------------------
    //닉네임이 변경되면 파이어베이스 데이터 베이스에 저장하는 역할을 합니다.
    fun pushNickName (){

        //기존 닉네임이 없는데 닉네임을 적지 않은 경우 나는 바보입니다. 로 닉네임을 지정합니다.
        if (nickNameText.text.isNullOrBlank() && nickNameText.hint.isNullOrBlank()) {
            nickNameText.setText("나는 바보입니다.")

            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName("${nickNameText.text}")
                .setPhotoUri(Uri.parse("https://firebasestorage.googleapis.com/v0/b/finalexam-77fdc.appspot.com/o/user.png?alt=media&token=8696e110-d8f1-4218-8cb9-e28088f60c1f"))
                .build()

            user?.updateProfile(profileUpdate)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext,"닉네임을 지정했습니다.",Toast.LENGTH_LONG).show()
                        user.reload()
                    }
                }
        }

        if (nickNameText.text.toString().isNullOrBlank()){
            Toast.makeText(applicationContext,"닉네임을 그대로 합니다.",Toast.LENGTH_LONG).show()

        }else{
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName("${nickNameText.text}")
                .setPhotoUri(Uri.parse("https://firebasestorage.googleapis.com/v0/b/finalexam-77fdc.appspot.com/o/user.png?alt=media&token=8696e110-d8f1-4218-8cb9-e28088f60c1f"))
                .build()

            user?.updateProfile(profileUpdate)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext,"닉네임을 지정했습니다.",Toast.LENGTH_LONG).show()
                        user.reload()
                    }
                }
        }
    }//end of pushNickName

    //---------------------------------------------------------------------------------------
    //선택한 자격증을 파이어베이스 데이터베이스에 저장하는 역할을 합니다.
    fun pushDatabase() {
        //기능사인지 기사인지 구분하는 기능을 합니다.
        //certificatPosition1
        //세부 자격증을 표시하는 역할을 합니다.
        //certificatPosition2
        //자격증 종목 코드입니다.
        //jmcdData

        val userinfo = UserInfo(
            certificatPosition1,
            certificatPosition2,
            jmcdData
        )

        //파이어베이스 데이터베이스에 저장합니다.
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child("${FirebaseAuth.getInstance().currentUser?.uid}")
            .setValue(userinfo)
    }//end of pushDatabase

    //---------------------------------------------------------------------------------------
    //에셋 폴더에서 JSON을 읽어 옵니다.
    fun readJson(): String? {
        var json : String? = null
        try {
            val inputStream : InputStream = assets.open("certification.json")
            json = inputStream.bufferedReader().use { it.readText() }

        }catch (e : IOException){

        }

        return json
    }

    //-------------------------------------------------------------------------------------------
    // 세션 로그아웃 함수
    private fun signOut() {
        startActivity(loginActivity.getLaunchIntent(this))
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        FirebaseAuth.getInstance().signOut();
        // 구글 세션 로그 아웃
        googleSignInClient?.signOut()
        //페이스북 세션 로그아웃
        LoginManager.getInstance().logOut()
        finish()
    }

    //-----------------------------------------------------------------------------------------
    //백스택을 쌓지 않고 화면을 넘기는 역할을 합니다.
    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, ConfigurationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }
}

