package com.example.finalexam

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import java.io.*

class ConfigurationActivity : AppCompatActivity() {

    lateinit var googleSignInClient: GoogleSignInClient
    var arr = arrayListOf<String>()
    var jmcdList = arrayListOf<Int>()
    var certificatPosition1 : String = ""
    var certificatPosition2 : String = ""
    var jmcdData : Int = 0


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        //액션바 숨기
        supportActionBar?.hide()

        setContentView(R.layout.activity_main)

        //버튼에 로그아웃 기능을 넣는다.
        signOutButton.setOnClickListener{
            signOut()
        }
        //세션 로그아웃 구현
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        //구글 클라이언트를 연결시킵니다.
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        //유저 정보 가져오기
        var user = FirebaseAuth.getInstance().currentUser!!

        //파이어 베이스에서 닉네임을 지정했는지 확인합니다.
        //닉네임을 정했으면 MainActivity로 넘어갑니다.
        nickNameText.hint = user.displayName
        //다음 화면으로 넘어가는 기능을 제공합니다.
        createNickButton.setOnClickListener {
            pushDatabase()
            if (nickNameText.text.toString().isNullOrBlank()){
                Toast.makeText(applicationContext,"닉네임을 그대로 합니다.",Toast.LENGTH_LONG).show()
                startActivity(MainActivity.getLaunchIntent(this))
            }else{
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName("${nickNameText.text}")
                    .build()

                user?.updateProfile(profileUpdate)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(applicationContext,"닉네임을 지정했습니다.",Toast.LENGTH_LONG).show()
                            user.reload()
                        }
                    }
                startActivity(MainActivity.getLaunchIntent(this))
            }
    }
        //Json파일을 불러온다.
        val certificationData = readJson()
        val jsonarr = JSONArray(certificationData)

        // 인덱스 1,2,3,4 순서이다.
        var certificationList = arrayOf("기술사","기능장","기사","기능사")
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
                certificatPosition1 = certificationList[position]
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
                var adpt = ArrayAdapter<String>(this@ConfigurationActivity,android.R.layout.simple_spinner_dropdown_item,arr)
                bottomSpinner.adapter = adpt

                // 세부 선택 스피너의 리스너입니다.
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
            }

        }

//        var thread = NetworkThread()
//        thread.start()
    }

    fun pushDatabase() {
//        //기능사인지 기사인지 구분하는 기능을 합니다.
//        certificatPosition1
//        //세부 자격증을 표시하는 역할을 합니다.
//        certificatPosition2
//        //자격증 종목 코드입니다.
//        jmcdData
        val userinfo = UserInfo(certificatPosition1,certificatPosition2,jmcdData)

        //파이어베이스 데이터베이스에 저장합니다.
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child("${FirebaseAuth.getInstance().currentUser?.uid}")
            .setValue(userinfo)
    }

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
//
//    inner class NetworkThread() : Thread() {
//        override fun run() {
//
//
//            Log.d("Thread", "스레드 시작")
//
//            var site =
//                URL("http://openapi.q-net.or.kr/api/service/rest/InquiryStatSVC/getGradPiPassList?serviceKey=QR6Eti6A0zHnybQlRIidIklUWdIf9bl5bt3coyBro2ldfN%2FsxvuIwJ8O4HNQAhBV%2Fia8yktKc1xmVa29qQaDMA%3D%3D&baseYY=2019&")
//            var conn: HttpURLConnection = site.openConnection() as HttpURLConnection
//            conn.connectTimeout = 30000
//            conn.readTimeout = 30000
//            var input = conn.inputStream
//            var isr = InputStreamReader(input)
//            var br = BufferedReader(isr)
//
//            var str: String? = null
//            var buf = StringBuffer()
//            do {
//                str = br.readLine()
//                println(str)
//
//                if (str != null) {
//                    Log.d("Thread", "${str}")
//                    buf.append(str)
//                }
//            } while (str != null)
//
//            runOnUiThread {
//                textView5.setText(str)
//            }
//            Log.d("Thread", "스레드 종료")
//            println(buf.toString())
//        }
//    }
    //-------------네트워크 스레드 종료

    // 세션 로그아웃 함수
    private fun signOut() {
        startActivity(loginActivity.getLaunchIntent(this))
        FirebaseAuth.getInstance().signOut();
        // 구글 세션 로그 아웃
        googleSignInClient?.signOut()
        //페이스북 세션 로그아웃
        LoginManager.getInstance().logOut()
        finish()
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, ConfigurationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }
}

