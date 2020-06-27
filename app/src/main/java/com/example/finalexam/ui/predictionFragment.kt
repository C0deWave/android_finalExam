package com.example.finalexam.ui

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.anupkumarpanwar.scratchview.ScratchView
import com.example.finalexam.R
import com.example.finalexam.dataClass.predictXmlData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_prediction.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*
import java.lang.IllegalStateException
import java.time.LocalDateTime
import kotlin.random.Random

// 합격예측 프래그먼트의 코틀린파일 입니다.

class predictionFragment : Fragment() {

    //api의 값
    var predictXml : ArrayList<predictXmlData>? = null

    //---------------------------------------------------------------------------------------
    //onCreateView 입니다.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_prediction, container, false)
    }

    //-----------------------------------------------------------------------------------
    //onStart 함수입니다.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        //volley 호출을 합니다.
        //내부적으로 xml과 이미지 세팅도 합니다.
        //volley에서 값을 받은 다음에만 가능하기 때문에 함수를 따로 분리하지 못했습니다.
        val url = "http://openapi.q-net.or.kr/api/service/rest/InquiryStatSVC/getGradPiPassList?serviceKey=5uS4tOJQn0fw3%2FjgZo8p9AY9kOz3VywROItpVQrcjFlHYo6OWPw5yJKuqvm4A%2BJ6mPHQjFhJxuZd%2BqkLBw4T0w%3D%3D&baseYY=2019&"
        requestVolley(url)

        // 스크래치 기능의 리스너를 넣었습니다.
        settingScratchView()

    }//end of onStart

    //--------------------------------------------------------------------------------------
    //volley로 url 호출의 값을 받아오는 역할을 합니다.
    fun requestVolley(url: String) {
        //volley로 합격률 api값 가져오기
        var result : String? = null
        // Volley의 requset queue를 만듭니다.
        val queue = Volley.newRequestQueue(context)

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                // Display the first 500 characters of the response string.
                result = String(response.toByteArray(charset("euc-kr")), charset("euc-kr"))

                //xml 파싱을 위한 준비를 하고 파싱을 합니다.
                xmlparseReady(result!!)

                //volley로 값을 가져온 상태에서만 가능하기 때문에 여기에 위치되어있습니다.
                //랜덤 함수를 해서 합격 예측을 합니다.
                settingPredictAndSettingImage()
            },
            Response.ErrorListener { Log.d("Volley","Volley에러") }
        )

        // url request를 실행합니다.
        queue.add(stringRequest)
    }

    //------------------------------------------------------------------------------------
    //랜덤한 값을 받아서 xml에서 받은 합격률보다 높으면 불합
    //낮으면 합격 처리로 예언하는 함수입니다.
    fun settingPredictAndSettingImage() {
        var predictResult = Random(LocalDateTime.now().hashCode()).nextInt(100)+1
        Log.d("time","${LocalDateTime.now()}")
        var cert1 = ""

        FirebaseAuth.getInstance().currentUser?.uid?.let {
            FirebaseDatabase.getInstance().reference
                .child("users")
                .child(it)
                .addListenerForSingleValueEvent(object  : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        var data = p0.value as Map<String,Any>
                        cert1 = data["cert1"].toString()

                        // 각 합격률에 맞추어서 합격 예측을 합니다.
                        when(cert1){
                            "기능사" -> { setPredict(predictResult , predictXml?.get(5)?.starisyy1) }
                            "기사" -> {setPredict(predictResult , predictXml?.get(2)?.starisyy1)}
                            "기능장" -> {setPredict(predictResult , predictXml?.get(1)?.starisyy1)}
                            "기술사" -> {setPredict(predictResult , predictXml?.get(0)?.starisyy1)}
                            else -> {Toast.makeText(context,"cert1의 값이 이상합니다. ${cert1}",Toast.LENGTH_LONG).show()}
                        }
                    }
                }).toString()
        }
    }

    //------------------------------------------------------------------------------------
    //xml 파싱을 위한 준비를 하고 xml 파싱을 합니다.
    fun xmlparseReady(result: String) {
        val pullParserFactory : XmlPullParserFactory
        try {
            pullParserFactory = XmlPullParserFactory.newInstance()
            val parser = pullParserFactory.newPullParser()
            val inputStream = ByteArrayInputStream(result?.toByteArray(charset("euc-kr")))
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false)
            parser.setInput(inputStream,null)

            //가져온 값을 파싱해줍니다.
            predictXml = parseXml(parser)

        }catch (e : XmlPullParserException){
            e.printStackTrace()
        }catch (e : IOException){
            e.printStackTrace()
        }
    }

    //--------------------------------------------------------------------------------------
    //스크래치 뷰의 리스너를 지정해 줍니다.
    //일정 확률 이상스크래치를 하면 스크래치 뷰가 사라지면서 전체 화면이 나타나게 됩니다.
    //또한 약간이라도 긁으면 손가락 애니메이션이 보이지 않게 만들었습니다.
    fun settingScratchView() {
        val percentOfReveal = 0.1

        scratchView.setRevealListener(object : ScratchView.IRevealListener{
            override fun onRevealed(scratchView: ScratchView?) {
                AnimatorInflater.loadAnimator(context,R.animator.predicr_clear_animator).apply {
                    setTarget(scratchView)
                    start()
                }
            }

            override fun onRevealPercentChangedListener(scratchView: ScratchView?, percent: Float) {
                Log.d("percent","${percent}")

                //핑거를 사라지게 합니다.
                fingerImage.clearAnimation()
                fingerImage.visibility = View.INVISIBLE

                if (percent > percentOfReveal){
                    //0.1퍼센트 이상 긁으면 onRevealed 함수를 진행하고 리스너를 해제합니다.
                    this.onRevealed(scratchView)

                    //리스너를 해제합니다.
                    scratchView?.setRevealListener(object :ScratchView.IRevealListener{
                        override fun onRevealed(scratchView: ScratchView?)
                        {

                        }
                        override fun onRevealPercentChangedListener(
                            scratchView: ScratchView?,
                            percent: Float
                        ) {

                        }
                    })
                }
            }
        })//end of setRevealListener
    }

    //-----------------------------------------------------------------------------------------
    //합격이미지를 설정하고 손가락 애니메이션의 작동을 담당합니다.
    fun setPredict(predictResult: Int, starisyy1: String?) {
        //합격 확률 안쪽으로 들어온 경우
        if (predictResult < starisyy1!!.toInt()){
            predictionResultImageView?.setBackgroundColor( requireContext().getColor(R.color.goodnews) )
            predictionResultImageView?.setImageResource(R.drawable.crystallballgood)
        }else{
            predictionResultImageView?.setBackgroundColor( requireContext().getColor(R.color.badnews) )
            predictionResultImageView?.setImageResource(R.drawable.crystallballbad)
        }

        //이미지가 지정이 되면 예언을 하는 이미지가 사라지면서 손가락으로 스와이프를하는 애니메이션이 생성됩니다.
        //빠르게 화면전환을 했을때 에러가 나는것을 try로 잡았습니다.
        try {
            Toast.makeText(requireContext(),"${predictResult} ${starisyy1}",Toast.LENGTH_LONG).show()
            AnimatorInflater.loadAnimator(requireContext(), R.animator.predicr_clear_animator)
                ?.apply {
                    addListener(object : AnimatorListenerAdapter() {
                        //애니매이션이 끝나면 이미지를 안보이게 해서 수정이 클릭되게 합니다.
                        override fun onAnimationEnd(animation: Animator?) {
                            imageView2?.visibility = View.INVISIBLE
                        }
                    })
                    setTarget(imageView2)
                    start()
                }

            fingerImage?.visibility = View.VISIBLE
            val animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.finger_animation)
            fingerImage?.startAnimation(animation)
        }catch (e : IllegalStateException){
            e.printStackTrace()
        }
    }

    //------------------------------------------------------------------------------------------
    //Xml 파싱하는 것을 담당하는 함수입니다.
    @Throws (XmlPullParserException::class, IOException::class)
    fun parseXml(parser: XmlPullParser?): ArrayList<predictXmlData>? {
        var dataArray : ArrayList<predictXmlData>? = null
        var eventType = parser?.eventType
        var data : predictXmlData? = null

        while (eventType != XmlPullParser.END_DOCUMENT){
            val tagName : String
            when(eventType){
                XmlPullParser.START_DOCUMENT->dataArray = ArrayList()
                XmlPullParser.START_TAG -> {
                    tagName = parser!!.name
                    if (tagName == "item"){
                        data = predictXmlData()
                    }else if (data != null){
                        if (tagName == "statisyy1"){
                            data.starisyy1 = parser.nextText()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    tagName = parser!!.name
                    if (tagName.equals("item", ignoreCase = true) && data != null){
                        dataArray!!.add(data)
                    }
                }
            }
            eventType = parser!!.next()
        }
        return dataArray
    }//end of ParseXml

    //-------------------------------------------------------------------------------------
    //프레그먼트를 생성해서 반환하는 함수입니다.
    companion object {
        fun newInstance(): predictionFragment {
            return predictionFragment()
        }
    }
}
