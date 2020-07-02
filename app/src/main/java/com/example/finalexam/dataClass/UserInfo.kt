package com.example.finalexam.dataClass

import sun.bob.mcalendarview.vo.DateData

//파이어베이스에 저장할 사용자의 정보를 담는 클래스 입니다.
//cert1 : 대분류 -> 기사 기능사 등등
//cert2 : 소분류 -> 정보처리 기능사
//jmcdData : 자격증 인식 코드 -> 가스기술사 : 0752
class UserInfo( val cert1 : String,
                val cert2 : String,
                val jmcdData : Int) {

    var d_day_year : Int? = null
    var d_day_month : Int? = null
    var d_day_day : Int? = null
}