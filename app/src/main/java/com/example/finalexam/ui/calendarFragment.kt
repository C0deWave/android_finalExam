package com.example.finalexam.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.finalexam.R
import com.example.finalexam.dataClass.certificationTestXmlData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_calendar.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import sun.bob.mcalendarview.MarkStyle
import sun.bob.mcalendarview.listeners.OnDateClickListener
import sun.bob.mcalendarview.vo.DateData
import java.io.ByteArrayInputStream
import java.io.IOException
import java.lang.NullPointerException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.collections.ArrayList

class calendarFragment : Fragment() {

    //api의 값
    var Xmldata : ArrayList<certificationTestXmlData>? = null
    //원하는 자격증의 대분류
    var result : String = ""

    //선택한 날짜
    var year = 0
    var month = 0
    var day = 0

    //D-day로 지정한 날짜
    var dDay_Year = 0
    var dDay_Month = 0
    var dDay_Day = 0

    //오늘의 날자
    val calendar = Calendar.getInstance()

    //선택한 날짜
    val dCalendar = Calendar.getInstance()

    val dayOfMilliSecond = (24*60*60*1000)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView.setOnDateClickListener(object : OnDateClickListener(){
            override fun onDateClick(view: View?, date: DateData?) {
                year = date?.year!!
                month = date?.month!!
                day = date?.day!!

                Log.d("click","${year}, ${month}, ${day}")
                tv_day.text = String.format("%d.%d.%d", date?.year, date?.month, date?.day)
                tv_day.visibility = View.VISIBLE
                cardView.visibility = View.VISIBLE

            }
        })

        tv_day.visibility = View.INVISIBLE
        cardView.visibility = View.INVISIBLE


        //디데이 일정 계산하기
        btn_d_day.setOnClickListener {
            cardView.visibility = View.INVISIBLE
            tv_content.visibility = View.VISIBLE

            calendarView.unMarkDate(DateData( dDay_Year, dDay_Month, dDay_Day )
                .setMarkStyle(MarkStyle.LEFTSIDEBAR,Color.YELLOW))
            dCalendar.set(year,month-1,day)

            var t = calendar.timeInMillis
            var d = dCalendar.timeInMillis
            //디데이 날짜에서 오늘 날짜를 뺸 값을 일 단위로 바꾼다.
            var r = (d - t)/(dayOfMilliSecond)

            if (r > 0){
                tv_content.text = "D-day :  -" + r.toString()
            }else if (r == 0L){
                tv_content.text = "D-day :  D-Day!!"
            }else{
                tv_content.text = "D-day :  +" + (r * -1).toString()
            }
            Log.d("d-day","${r.toString()}")

            calendarView.markDate(DateData( year, month, day )
                .setMarkStyle(MarkStyle.LEFTSIDEBAR,Color.YELLOW))
            dCalendar.set(year,month-1,day)

            dDay_Year = year
            dDay_Month = month
            dDay_Day = day
        }

        btn_save.setOnClickListener {
            cardView.visibility = View.INVISIBLE
            tv_content.visibility = View.VISIBLE

            when(rg.checkedRadioButtonId) {
                R.id.rbtn_complete -> {
                    calendarView.markDate(DateData(
                        year,
                        month,
                        day
                    ).setMarkStyle(MarkStyle.DOT, Color.RED))

                }

                R.id.rbtn_uncomplete -> {
                    calendarView.unMarkDate(DateData(
                        year,
                        month,
                        day
                    ).setMarkStyle(MarkStyle.DOT, Color.RED))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //원하는 자격증이 뭔지 확인하는 함수입니다.
        whatWantCertificat()

        //원하는자격증에 따라 url을 호출합니다.
        callUrlAndXmlParse(result)

    }

    override fun onPause() {
        super.onPause()
        discheckTestDate()
    }

    fun checkTestDate() {

        var date: LocalDate? = null

        try {


            for (data in this!!.Xmldata!!) {
                //필기시험 일자
                try {
                    if (data.docexamdt != "XXXXXXXX" || data.docexamdt != "") {
                        date = LocalDate.parse(
                            data.docexamdt,
                            DateTimeFormatter.ofPattern("yyyyMMdd")
                        )
                        calendarView.markDate(
                            DateData(
                                date.year,
                                date.monthValue,
                                date.dayOfMonth
                            ).setMarkStyle(MarkStyle.BACKGROUND, Color.MAGENTA)
                        )
                        Toast.makeText(requireContext(), "마킹됨", Toast.LENGTH_LONG).show()
                        Log.d("date", "${date.year} , ${date.monthValue} , ${date.dayOfMonth}")
                    }
                    if (data.docpassdt != "XXXXXXXX") {
                        //필기시험 합격자 발표일
                        date = LocalDate.parse(
                            data.docpassdt,
                            DateTimeFormatter.ofPattern("yyyyMMdd")
                        )
                        calendarView.markDate(
                            DateData(
                                date.year,
                                date.monthValue,
                                date.dayOfMonth
                            ).setMarkStyle(MarkStyle.BACKGROUND, Color.CYAN)
                        )
                    }
                }catch (e:DateTimeParseException){
                    e.printStackTrace()
                }
            }
        }catch (e : NullPointerException){
            e.printStackTrace()
        }
    }

    //--------------------------------------------------------------------------------------------
    fun discheckTestDate() {

        var date : LocalDate? = null
        try {
            for (data in this!!.Xmldata!!) {

                try {

                    //필기시험 일자
                    if (data.docexamdt != "XXXXXXXX") {
                        date = LocalDate.parse(
                            data.docexamdt,
                            DateTimeFormatter.ofPattern("yyyyMMdd")
                        )
                        calendarView.unMarkDate(
                            DateData(
                                date.year,
                                date.monthValue,
                                date.dayOfMonth
                            ).setMarkStyle(MarkStyle.BACKGROUND, Color.MAGENTA)
                        )
                        Toast.makeText(requireContext(), "마킹됨", Toast.LENGTH_LONG).show()
                        Log.d("date", "${date.year} , ${date.monthValue} , ${date.dayOfMonth}")
                    }
                    if (data.docpassdt != "XXXXXXXX") {
                        //필기시험 합격자 발표일
                        date = LocalDate.parse(
                            data.docpassdt,
                            DateTimeFormatter.ofPattern("yyyyMMdd")
                        )
                        calendarView.unMarkDate(
                            DateData(
                                date.year,
                                date.monthValue,
                                date.dayOfMonth
                            ).setMarkStyle(MarkStyle.BACKGROUND, Color.CYAN)
                        )
                    }
                }catch (e:DateTimeParseException){
                    e.printStackTrace()
                }
            }
        }catch (e : NullPointerException){
            e.printStackTrace()
        }
    }

    //----------------------------------------------------------------------------------------
    fun whatWantCertificat(){

        val user = FirebaseAuth.getInstance().currentUser
        //원하는 자격증을 바인딩합니다.
        if (user != null) {
            FirebaseDatabase.getInstance().reference
                .child("users")
                .child(user.uid)
                .addListenerForSingleValueEvent(object  : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        try {
                            var data = p0?.value as Map<String, Any>
                            result = data["cert1"].toString()
                        }catch (e: TypeCastException){
                            e.printStackTrace()
                        }
                        //원하는자격증에 따라 url을 호출합니다.
                        callUrlAndXmlParse(result)

                    }
                }).toString()
        }
    }

    fun callUrlAndXmlParse(result: String) {

        var url = ""

        when(result){
            "기사"  -> {url = "http://openapi.q-net.or.kr/api/service/rest/InquiryTestInformationNTQSVC/getEList?serviceKey=QR6Eti6A0zHnybQlRIidIklUWdIf9bl5bt3coyBro2ldfN%2FsxvuIwJ8O4HNQAhBV%2Fia8yktKc1xmVa29qQaDMA%3D%3D&"}
            "기능사" -> {url = "http://openapi.q-net.or.kr/api/service/rest/InquiryTestInformationNTQSVC/getCList?serviceKey=QR6Eti6A0zHnybQlRIidIklUWdIf9bl5bt3coyBro2ldfN%2FsxvuIwJ8O4HNQAhBV%2Fia8yktKc1xmVa29qQaDMA%3D%3D&"}
            "기능장" -> {url = "http://openapi.q-net.or.kr/api/service/rest/InquiryTestInformationNTQSVC/getMCList?serviceKey=QR6Eti6A0zHnybQlRIidIklUWdIf9bl5bt3coyBro2ldfN%2FsxvuIwJ8O4HNQAhBV%2Fia8yktKc1xmVa29qQaDMA%3D%3D&"}
            "기술사" -> {url = "http://openapi.q-net.or.kr/api/service/rest/InquiryTestInformationNTQSVC/getPEList?serviceKey=QR6Eti6A0zHnybQlRIidIklUWdIf9bl5bt3coyBro2ldfN%2FsxvuIwJ8O4HNQAhBV%2Fia8yktKc1xmVa29qQaDMA%3D%3D&"}
        }

        requestVolley(url)
    }

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
                result = String(response.toByteArray())

                //xml 파싱을 위한 준비를 하고 파싱을 합니다.
                xmlparseReady(result!!)

            },
            Response.ErrorListener { Log.d("Volley","Volley에러") }
        )

        // url request를 실행합니다.
        queue.add(stringRequest)
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
            Xmldata = parseXml(parser)


            //파싱이 완료되고 실행하는 기능입니다.
            //시험 일정을 체크하는 함수입니다.
            checkTestDate()
            Log.d("checkComplete","checkComplete")

        }catch (e : XmlPullParserException){
            e.printStackTrace()
        }catch (e : IOException){
            e.printStackTrace()
        }
    }

    //------------------------------------------------------------------------------------------
    //Xml 파싱하는 것을 담당하는 함수입니다.
    @Throws (XmlPullParserException::class, IOException::class)
    fun parseXml(parser: XmlPullParser?): ArrayList<certificationTestXmlData>? {
        var dataArray : ArrayList<certificationTestXmlData>? = null
        var eventType = parser?.eventType
        var data : certificationTestXmlData? = null

        while (eventType != XmlPullParser.END_DOCUMENT){
            val tagName : String
            when(eventType){
                XmlPullParser.START_DOCUMENT->dataArray = ArrayList()
                XmlPullParser.START_TAG -> {
                    tagName = parser!!.name
                    if (tagName == "item"){
                        data = certificationTestXmlData()
                    }else if (data != null){
                        if (tagName == "description"){
                            data.description = parser.nextText()
                        }
                        if (tagName == "docexamdt"){
                            data.docexamdt = parser.nextText()
                        }
                        if (tagName == "docpassdt"){
                            data.docpassdt = parser.nextText()
                        }
                        if (tagName == "docregenddt"){
                            data.docregenddt = parser.nextText()
                        }
                        if (tagName == "docregstartdt"){
                            data.docregstartdt = parser.nextText()
                        }
                        if (tagName == "pracexamenddt"){
                            data.pracexamenddt = parser.nextText()
                        }
                        if (tagName == "pracexamstartdt"){
                            data.pracexamstartdt = parser.nextText()
                        }
                        if (tagName == "pracpassdt"){
                            data.pracpassdt = parser.nextText()
                        }
                        if (tagName == "pracregenddt"){
                            data.pracregenddt = parser.nextText()
                        }
                        if (tagName == "pracregstartdt"){
                            data.pracregstartdt = parser.nextText()
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

    //---------------------------------------------------------------------------------------------

    companion object{
        fun newInstance(): calendarFragment {
            return calendarFragment()
        }
    }

}